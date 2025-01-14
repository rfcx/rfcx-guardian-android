package org.rfcx.guardian.utility.rfcx;

import android.content.ContentResolver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.utility.misc.ArrayUtils;
import org.rfcx.guardian.utility.misc.DateTimeUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RfcxStatus {

    public static final long[] localCacheExpirationBounds = new long[]{5000, 15000};
    public static final double ratioExpirationToAudioCycleDuration = 10;
    protected static final String[] statusTypes = new String[]{Type.ALLOWED, Type.ENABLED};
    protected static final boolean[] statusDefaults = new boolean[]{true, true};

    protected static final String[] statusGroups = new String[]{Group.AUDIO_CAPTURE, Group.API_CHECKIN, Group.SBD_COMMUNICATION};
    private static final int ratioLocalToFetchedCacheExpiration = 3;
    private static final long[] fetchedExpirationBounds = new long[]{ratioLocalToFetchedCacheExpiration * localCacheExpirationBounds[0], ratioLocalToFetchedCacheExpiration * localCacheExpirationBounds[1]};
    private final RfcxGuardianIdentity rfcxGuardianIdentity;
    private final ContentResolver contentResolver;
    private final Map<Integer, boolean[]> lastLocalValue = new HashMap<>();
    private final Map<Integer, long[]> lastLocalValueSetAt = new HashMap<>();
    private final boolean[] hasLocalCache = new boolean[statusGroups.length];
    private final String fetchTargetRole;
    protected String logTag;
    private Map<Integer, boolean[]> lastFetchedValue = new HashMap<>();
    private long lastFetchedValueSetAt = 0;
    private boolean hasFetchedCache = false;
    private boolean isFetchQueryInFlight = false;
    private long localValueCacheExpiresAfter = Math.round((localCacheExpirationBounds[0] + localCacheExpirationBounds[1]) / 2.0);
    private long fetchedValueCacheExpiresAfter = Math.round((fetchedExpirationBounds[0] + fetchedExpirationBounds[1]) / 2.0);
    public RfcxStatus(String appRole, String fetchTargetRole, RfcxGuardianIdentity rfcxGuardianIdentity, ContentResolver contentResolver) {
        this.logTag = RfcxLog.generateLogTag(appRole, "RfcxStatus");
        this.fetchTargetRole = ArrayUtils.doesStringArrayContainString(RfcxRole.ALL_ROLES, fetchTargetRole) ? fetchTargetRole.toLowerCase(Locale.US) : null;
        this.rfcxGuardianIdentity = rfcxGuardianIdentity;
        this.contentResolver = contentResolver;
        initializeCaches();
    }

    protected static boolean isGroup(String tag, int group) {
        return group == getGroup(tag);
    }

    protected static boolean isStatusType(String tag, int statusType) {
        return statusType == getStatusType(tag);
    }

    protected static int getGroup(String group) {
        return ArrayUtils.indexOfStringInStringArray(statusGroups, group);
    }
    //
    // The function above should be over-ridden within each role that makes use of this class
    //

    protected static int getStatusType(String statusType) {
        return ArrayUtils.indexOfStringInStringArray(statusTypes, statusType);
    }

    //
    // The function below should be over-ridden within each role that makes use of this class
    //
    protected boolean[] getStatusBasedOnRoleSpecificLogic(int group, boolean[] fallbackValues, boolean printFeedbackInLog) {
        boolean[] statusValues = fallbackValues;
        boolean reportUpdate = false;
        Log.e(logTag, "Using Generic 'getStatusBasedOnRoleSpecificLogic' function. No role-specific functionality has been declared.");
        // This is where the role specific logic would be.
        // Just in case this function is not over-ridden, we report an error below, so that we'll be informed.
        if (reportUpdate) {
            Log.w(logTag, "Refreshed local status cache for '" + statusGroups[group] + "'");
        }
        return statusValues;
    }

    private void updateLocalStatus(int group, boolean printFeedbackInLog) {
        boolean[] updatedStatus = lastLocalValue.containsKey(group) ? lastLocalValue.get(group) : statusDefaults;
        updatedStatus = getStatusBasedOnRoleSpecificLogic(group, updatedStatus, printFeedbackInLog);
        lastLocalValue.put(group, updatedStatus);
        lastLocalValueSetAt.put(group, new long[]{System.currentTimeMillis(), System.currentTimeMillis()});
        hasLocalCache[group] = true;
    }

    public void forceUpdatesForAllGroups(boolean printFeedbackInLog) {
        updateFetchedStatus();
        for (int group = 0; group < statusGroups.length; group++) {
            updateLocalStatus(group, printFeedbackInLog);
        }
    }

    private void updateFetchedStatus() {
        isFetchQueryInFlight = true;
        Map<Integer, boolean[]> _lastFetchedValue = lastFetchedValue;
        try {
            if ((fetchTargetRole != null) && isGuardianRegistered()) {
                Log.w(logTag, "Refreshing fetched status values via Content Provider...");
                JSONArray jsonArray = RfcxComm.getQuery(fetchTargetRole, "status", "*", contentResolver);
                if (jsonArray.length() > 0) {
                    JSONObject jsonObj = jsonArray.getJSONObject(0);
                    for (int group = 0; group < statusGroups.length; group++) {
                        boolean[] updatedStatus = _lastFetchedValue.containsKey(group) ? _lastFetchedValue.get(group) : statusDefaults;
                        if (jsonObj.has(statusGroups[group])) {
                            JSONObject groupStatusObj = jsonObj.getJSONObject(statusGroups[group]);
                            for (int statusType = 0; statusType < statusTypes.length; statusType++) {
                                if (groupStatusObj.has("is_" + statusTypes[statusType])) {
                                    updatedStatus[statusType] = groupStatusObj.getBoolean("is_" + statusTypes[statusType]);
                                }
                            }
                        }
                        _lastFetchedValue.put(group, updatedStatus);
                    }
                }
            }
        } catch (JSONException e) {
            RfcxLog.logExc(logTag, e);
        }
        lastFetchedValue = _lastFetchedValue;
        lastFetchedValueSetAt = System.currentTimeMillis();
        hasFetchedCache = true;
        isFetchQueryInFlight = false;
    }

    private boolean getFetchedStatus(int group, int statusType) {
        if (isFetchQueryInFlight || (hasFetchedCache && (Math.abs(DateTimeUtils.timeStampDifferenceFromNowInMilliSeconds(lastFetchedValueSetAt)) <= fetchedValueCacheExpiresAfter))) {
            return lastFetchedValue.get(group)[statusType];
        } else {
            updateFetchedStatus();
            return lastFetchedValue.containsKey(group) ? lastFetchedValue.get(group)[statusType] : statusDefaults[statusType];
        }
    }

    private boolean getLocalStatus(int group, int statusType, boolean printFeedbackInLog) {
        if (hasLocalCache[group] && lastLocalValueSetAt.containsKey(group)
                && (Math.abs(DateTimeUtils.timeStampDifferenceFromNowInMilliSeconds(lastLocalValueSetAt.get(group)[statusType])) <= localValueCacheExpiresAfter)) {
            return lastLocalValue.get(group)[statusType];
        } else {
            updateLocalStatus(group, printFeedbackInLog);
            return lastLocalValue.containsKey(group) ? lastLocalValue.get(group)[statusType] : statusDefaults[statusType];
        }
    }

    public boolean getFetchedStatus(String group, String statusType) {
        return getFetchedStatus(getGroup(group), getStatusType(statusType));
    }

    public boolean getLocalStatus(String group, String statusType, boolean printFeedbackInLog) {
        return getLocalStatus(getGroup(group), getStatusType(statusType), printFeedbackInLog);
    }

    private JSONObject getLocalStatusAsJsonObj(int group) throws JSONException {
        JSONObject statusObj = new JSONObject();
        for (int statusType = 0; statusType < statusTypes.length; statusType++) {
            statusObj.put("is_" + statusTypes[statusType], getLocalStatus(group, statusType, false));
        }
        return statusObj;
    }

    private JSONObject getLocalStatusAsJsonObj(String group) throws JSONException {
        return getLocalStatusAsJsonObj(getGroup(group));
    }

    public JSONArray getCompositeLocalStatusAsJsonArr() {
        JSONArray compositeStatusArr = new JSONArray();
        JSONObject compositeStatusObj = new JSONObject();
        try {
            for (int group = 0; group < statusGroups.length; group++) {
                compositeStatusObj.put(statusGroups[group], getLocalStatusAsJsonObj(group));
            }
        } catch (JSONException e) {
            RfcxLog.logExc(logTag, e);
        }
        compositeStatusArr.put(compositeStatusObj);
        return compositeStatusArr;
    }

    private void initializeCaches() {
        for (int group = 0; group < statusGroups.length; group++) {
            boolean[] valueArr = new boolean[statusTypes.length];
            long[] valueSetAtArr = new long[statusTypes.length];
            for (int statusType = 0; statusType < statusTypes.length; statusType++) {
                valueArr[statusType] = statusDefaults[statusType];
                valueSetAtArr[statusType] = 0;
            }

            lastLocalValue.put(group, valueArr);
            lastLocalValueSetAt.put(group, valueSetAtArr);
            hasLocalCache[group] = false;

            lastFetchedValue.put(group, valueArr);
            lastFetchedValueSetAt = 0;
            hasFetchedCache = false;
            isFetchQueryInFlight = false;
        }
    }

    public void setOrResetCacheExpirations(int audioCycleDurationInSeconds) {
        long audioCycleDuration = audioCycleDurationInSeconds * 1000;
        localValueCacheExpiresAfter = Math.min(Math.max(Math.round(audioCycleDuration / ratioExpirationToAudioCycleDuration), localCacheExpirationBounds[0]), localCacheExpirationBounds[1]);
        fetchedValueCacheExpiresAfter = Math.min(Math.max(Math.round(ratioLocalToFetchedCacheExpiration * audioCycleDuration / ratioExpirationToAudioCycleDuration), fetchedExpirationBounds[0]), fetchedExpirationBounds[1]);
        Log.w(logTag, "Updated Status Cache Timeouts "
                + "- Local: " + DateTimeUtils.milliSecondDurationAsReadableString(localValueCacheExpiresAfter)
                + ", Fetched: " + DateTimeUtils.milliSecondDurationAsReadableString(fetchedValueCacheExpiresAfter));
    }

    private boolean isGuardianRegistered() {
        return (rfcxGuardianIdentity.getAuthToken() != null);
    }

    public static final class Group {
        public static final String AUDIO_CAPTURE = "audio_capture";
        public static final String API_CHECKIN = "api_checkin";
        public static final String SBD_COMMUNICATION = "sbd_comm";
    }

    public static final class Type {
        public static final String ALLOWED = "allowed";
        public static final String ENABLED = "enabled";
    }

}
