package org.rfcx.guardian.guardian.companion;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.guardian.RfcxGuardian;
import org.rfcx.guardian.utility.network.SocketUtils;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

public class CompanionSocketUtils {

    private static final String[] includePingFields = new String[]{
            "battery", "prefs_full", "software", "library", "device", "companion", "swm", "active-classifier"
    };
    private static final String[] excludeFromLogs = new String[]{};
    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "CompanionSocketUtils");
    public SocketUtils socketUtils;
    private final RfcxGuardian app;
    private String pingJson = (new JSONObject()).toString();
    public CompanionSocketUtils(Context context) {
        this.app = (RfcxGuardian) context.getApplicationContext();
        this.socketUtils = new SocketUtils();
        this.socketUtils.setSocketPort(RfcxComm.TCP_PORTS.GUARDIAN.SOCKET.JSON);
    }

    public JSONObject getCompanionPingJsonObj() {
        JSONObject companionObj = new JSONObject();
        try {

            JSONObject guardianObj = new JSONObject();
            guardianObj.put("guid", app.rfcxGuardianIdentity.getGuid());
            guardianObj.put("name", app.rfcxGuardianIdentity.getName());
            guardianObj.put("token", app.rfcxGuardianIdentity.getAuthToken());

            companionObj.put("guardian", guardianObj);

            companionObj.put("is_registered", app.isGuardianRegistered());

            companionObj.put("checkin", getLatestAllSentCheckInType());

            companionObj.put("system_time_utc", System.currentTimeMillis());

            companionObj.put("system_timezone", TimeZone.getDefault().getID());

            Pair<Boolean, String> isCapturing =  isAudioCapturing();
            companionObj.put("is_audio_capturing", isCapturing.first);
            companionObj.put("audio_capturing_message", isCapturing.second);

        } catch (JSONException e) {
            RfcxLog.logExc(logTag, e);
        }
        return companionObj;
    }

    private Pair<Boolean, String> isAudioCapturing() {
        Pair<Boolean, String> isDisabled = app.audioCaptureUtils.isAudioCaptureDisabled(false);
        Pair<Boolean, String> isAllowed = app.audioCaptureUtils.isAudioCaptureAllowed(true, false);
        boolean capturing = false;
        StringBuilder msg = new StringBuilder();

        if (!isDisabled.first && isAllowed.first) {
            capturing = true;
        }

        if (!isDisabled.second.isEmpty()) {
            msg.append(isDisabled.second);
        }

        if (!isAllowed.second.isEmpty()) {
            msg.append(isAllowed.second);
        }

        return new Pair<>(capturing, msg.toString());
    }

    private JSONObject getLatestAllSentCheckInType() throws JSONException {
        JSONObject checkIn = new JSONObject();

        String mqtt = app.apiCheckInUtils.getLastCheckinDateTime();
        JSONObject mqttObj = new JSONObject();
        mqttObj.put("created_at", mqtt);
        if (mqtt.length() > 0) {
            checkIn.put("mqtt", mqttObj);
        }

        JSONArray sms = RfcxComm.getQuery(
                "admin",
                "sms_latest",
                null,
                app.getContentResolver());
        if (sms.length() > 0) {
            checkIn.put("sms", sms.getJSONObject(0));
        }

        JSONArray sbd = RfcxComm.getQuery(
                "admin",
                "sbd_latest",
                null,
                app.getContentResolver());
        if (sbd.length() > 0) {
            checkIn.put("sbd", sbd.getJSONObject(0));
        }

        JSONArray swm = RfcxComm.getQuery(
                "admin",
                "swm_latest",
                null,
                app.getContentResolver());
        if (swm.length() > 0) {
            checkIn.put("swm", swm.getJSONObject(0));
        }

        return checkIn;
    }

    public void updatePingJson(boolean printJsonToLogs) {
        try {
            pingJson = app.apiPingJsonUtils.buildPingJson(false, includePingFields, 0, printJsonToLogs, excludeFromLogs, false);
        } catch (JSONException e) {
            RfcxLog.logExc(logTag, e, "updatePingJson");
        }
    }

    public boolean sendSocketPing() {
        return this.socketUtils.sendJson(pingJson, areSocketInteractionsAllowed());
    }


    private boolean areSocketInteractionsAllowed() {

        if ((app != null)
                && socketUtils.isServerRunning
        ) {
            return true;
        }
        Log.d(logTag, "Socket interaction blocked.");
        return false;
    }

    private void processReceivedJson(String jsonStr) {
        app.apiCommandUtils.processApiCommandJson(jsonStr, "socket");
        socketUtils.isReceivingMessageFromClient = true;
    }


    public void startServer() {

//		if (!socketUtils.isServerRunning) {
        socketUtils.serverThread = new Thread(() -> {
            Looper.prepare();
            try {
                socketUtils.serverSetup();
                while (true) {
                    if (socketUtils.serverThread.isInterrupted()) {
                        Log.d(logTag, "interrupted");
                        Looper.myLooper().quit();
                        return;
                    }
                    InputStream socketInput = socketUtils.socketSetup();
                    if (socketInput != null) {
                        String jsonStr = socketUtils.streamSetup(socketInput);
                        if (jsonStr != null) {
                            processReceivedJson(jsonStr);
                        }
                    }
                }
            } catch (IOException | NullPointerException e ) {
                // Mostly on server socket get closed from its service to keep socket alive all time.
                Looper.myLooper().quit();
            }
            Looper.loop();
        });
        socketUtils.serverThread.start();
        socketUtils.isServerRunning = true;
        //	}
    }

}
