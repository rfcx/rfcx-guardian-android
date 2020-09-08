package org.rfcx.guardian.guardian.socket

import android.app.IntentService
import android.content.Intent
import android.util.Log
import org.rfcx.guardian.guardian.RfcxGuardian
import org.rfcx.guardian.utility.rfcx.RfcxLog
import org.rfcx.guardian.utility.service.RfcxServiceHandler

class WifiCommunicationService : IntentService("WifiCommunication") {

    private val SERVICE_NAME = "WifiCommunication"

    private val logTag = RfcxLog.generateLogTag(
        RfcxGuardian.APP_ROLE,
        "WifiCommunicationService"
    )

    override fun onHandleIntent(p0: Intent?) {
        Log.d(logTag, "Starting WifiCommunication service")
        val intent =
            Intent(RfcxServiceHandler.intentServiceTags(false, RfcxGuardian.APP_ROLE, SERVICE_NAME))
        sendBroadcast(
            intent, RfcxServiceHandler.intentServiceTags(true, RfcxGuardian.APP_ROLE, SERVICE_NAME))

        val app = application as RfcxGuardian

        app.rfcxServiceHandler.reportAsActive(SERVICE_NAME)

        val prefsAdminEnableWifiSocket = app.rfcxPrefs.getPrefAsBoolean("admin_enable_wifi_socket")

        val prefsAdminEnableWifi = app.rfcxPrefs.getPrefAsBoolean("admin_enable_wifi")
        if (prefsAdminEnableWifiSocket && !prefsAdminEnableWifi) {
            Log.e( logTag, "WiFi Socket Server could not be enabled because 'admin_enable_wifi' is disabled")
        }
        try {
            if (prefsAdminEnableWifiSocket && prefsAdminEnableWifi) {
                Log.d(logTag, "Starting WifiCommunication service")
                SocketManager.stopServerSocket()
                SocketManager.startServerSocket(applicationContext)
            } else {
                Log.d(logTag, "Stopping WifiCommunication service")
                SocketManager.stopServerSocket()
            }
        } catch (e: Exception) {
            RfcxLog.logExc(logTag, e)
        }
    }
}
