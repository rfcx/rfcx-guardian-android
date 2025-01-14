package org.rfcx.guardian.admin.companion;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.admin.RfcxGuardian;
import org.rfcx.guardian.utility.misc.StringUtils;
import org.rfcx.guardian.utility.network.SocketUtils;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;

import java.io.IOException;
import java.io.InputStream;

public class CompanionSocketUtils {

    private static final String[] includePingFields = new String[]{
            "network", "sentinel_power", "sentinel_sensor", "cpu", "storage", "companion"
    };
    private static final String[] excludeFromLogs = new String[]{};
    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "CompanionSocketUtils");
    public SocketUtils socketUtils;
    private final RfcxGuardian app;
    private String pingJson = (new JSONObject()).toString();
    public CompanionSocketUtils(Context context) {
        this.app = (RfcxGuardian) context.getApplicationContext();
        this.socketUtils = new SocketUtils();
        this.socketUtils.setSocketPort(RfcxComm.TCP_PORTS.ADMIN.SOCKET.JSON);
    }

    public void updatePingJson(boolean printJsonToLogs) {
        try {
            pingJson = app.companionPingJsonUtils.buildPingJson(false, includePingFields, 0, printJsonToLogs, excludeFromLogs);
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
            } catch (IOException | NullPointerException e) {
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
