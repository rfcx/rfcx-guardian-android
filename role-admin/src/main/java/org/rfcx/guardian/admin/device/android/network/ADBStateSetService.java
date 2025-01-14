package org.rfcx.guardian.admin.device.android.network;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.rfcx.guardian.admin.RfcxGuardian;
import org.rfcx.guardian.utility.device.control.DeviceSystemProperties;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;
import org.rfcx.guardian.utility.rfcx.RfcxSvc;

public class ADBStateSetService extends IntentService {

    public static final String SERVICE_NAME = "ADBStateSet";

    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "ADBStateSetService");

    public ADBStateSetService() {
        super(logTag);
    }

    @Override
    protected void onHandleIntent(Intent inputIntent) {
        Intent intent = new Intent(RfcxSvc.intentServiceTags(false, RfcxGuardian.APP_ROLE, SERVICE_NAME));
        sendBroadcast(intent, RfcxSvc.intentServiceTags(true, RfcxGuardian.APP_ROLE, SERVICE_NAME));

        RfcxGuardian app = (RfcxGuardian) getApplication();

        // set ADB networking state
        boolean prefsAdminEnableAdb = app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ADMIN_ENABLE_ADB_SERVER);
        String prefsAdminWifiFunction = app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.ADMIN_WIFI_FUNCTION);
        boolean isWifiEnabled = !prefsAdminWifiFunction.equalsIgnoreCase("off");

        if (prefsAdminEnableAdb && !isWifiEnabled) {
            Log.e(logTag, "ADB over TCP could not be enabled because '" + RfcxPrefs.Pref.ADMIN_WIFI_FUNCTION + "' is set to 'off'");
        }

        boolean enableOrDisable = prefsAdminEnableAdb && isWifiEnabled;
        Log.v(logTag, ((enableOrDisable) ? "Enabling" : "Disabling") + " ADB over TCP on port " + RfcxComm.TCP_PORTS.ADMIN.ADB);
        DeviceSystemProperties.setVal("persist.adb.tcp.port", (enableOrDisable) ? "" + RfcxComm.TCP_PORTS.ADMIN.ADB : "");

    }


}
