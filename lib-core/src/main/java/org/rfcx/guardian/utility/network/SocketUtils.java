package org.rfcx.guardian.utility.network;

import android.util.Log;

import org.json.JSONObject;
import org.rfcx.guardian.utility.misc.StringUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SocketUtils {

    private static final String logTag = RfcxLog.generateLogTag("Utils", "SocketUtils");
    public Thread serverThread = null;
    public boolean isServerRunning = false;
    public boolean isReceivingMessageFromClient = false;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private DataInputStream streamInput = null;
    private DataOutputStream streamOutput = null;
    private int socketServerPort;

    private Timer timerThread = null;

    public boolean isConnectingWithCompanion = false;

    public void setSocketPort(int socketServerPort) {
        this.socketServerPort = socketServerPort;
    }

    private void publishText(String textToPublish) throws IOException {
        this.streamOutput.writeUTF(textToPublish);
        this.streamOutput.flush();
    }

    public void serverSetup() throws IOException {
        this.serverSocket = new ServerSocket(this.socketServerPort);
        this.serverSocket.setReuseAddress(true);
    }

    public InputStream socketSetup() throws IOException {
        this.socket = this.serverSocket.accept();
        this.socket.setTcpNoDelay(true);
        return this.socket.getInputStream();
    }

    public String streamSetup(InputStream socketInput) throws IOException {
        this.streamInput = new DataInputStream(socketInput);
        String jsonStr = this.streamInput.readUTF();
        this.streamOutput = new DataOutputStream(this.socket.getOutputStream());
        return jsonStr;
    }

    public InputStream streamFileSetup(InputStream socketInput) throws IOException {
        this.streamOutput = new DataOutputStream(this.socket.getOutputStream());
        return socketInput;
    }

    public void stopServer() {

        //	if (isServerRunning) {
        try {

            if (serverThread != null) {
                serverThread.interrupt();
            }
            if (streamInput != null) {
                streamInput.close();
            }
            if (streamOutput != null) {
                streamOutput.flush();
                streamOutput.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (timerThread != null) {
                timerThread.cancel();
            }

        } catch (IOException e) {
            if (!e.getMessage().equalsIgnoreCase("Socket closed")) {
                RfcxLog.logExc(logTag, e);
            }
        }

        serverThread = null;
        streamInput = null;
        streamOutput = null;
        socket = null;
        serverSocket = null;

        timerThread = null;

        isServerRunning = false;
//		}
    }


    public boolean isSocketServerEnablable(boolean verboseLogging, RfcxPrefs rfcxPrefs) {

        boolean prefsEnableSocketServer = rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ADMIN_ENABLE_SOCKET_SERVER);

        String prefsWifiFunction = rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.ADMIN_WIFI_FUNCTION);
        boolean isWifiEnabled = prefsWifiFunction.equals("hotspot") || prefsWifiFunction.equals("client");

        String prefsBluetoothFunction = rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.ADMIN_BLUETOOTH_FUNCTION);
        boolean isBluetoothEnabled = prefsBluetoothFunction.equals("pan");

        if (verboseLogging && prefsEnableSocketServer && !isWifiEnabled && !isBluetoothEnabled) {
            Log.e(logTag, "Socket Server could not be enabled because '" + RfcxPrefs.Pref.ADMIN_WIFI_FUNCTION + "' and '" + RfcxPrefs.Pref.ADMIN_BLUETOOTH_FUNCTION + "' are set to off.");
        }

        return prefsEnableSocketServer && (isWifiEnabled || isBluetoothEnabled);
    }


    public boolean sendJson(String jsonStr, boolean areSocketInteractionsAllowed) {

        boolean isSent = false;

        if (areSocketInteractionsAllowed) {
            try {
                String gZipJson = StringUtils.stringToGZipBase64(jsonStr);
                publishText(gZipJson);
                isSent = true;
                isConnectingWithCompanion = true;

            } catch (Exception e) {

                handleSocketJsonPublicationExceptions(e);
                isConnectingWithCompanion = false;
            }
        }

        return isSent;
    }

    public boolean sendAudioSocketJson(List<String> jsonListStr, boolean areSocketInteractionsAllowed) {

        boolean isSent = false;

        if (areSocketInteractionsAllowed) {
            try {
                for (String jsonStr : jsonListStr) {
                    publishText(jsonStr);
                }
                isSent = true;

            } catch (Exception e) {

                handleSocketJsonPublicationExceptions(e);
            }
        }

        return isSent;
    }


    private void handleSocketJsonPublicationExceptions(Exception inputExc) {

        try {
            String excStr = RfcxLog.getExceptionContentAsString(inputExc);

            // This is where we would put contingencies and reactions for various exceptions. See ApiMqttUtils for reference.

        } catch (Exception e) {
            RfcxLog.logExc(logTag, e, "handleSocketPingPublicationExceptions");
        }
    }

    public void setupTimerForClientConnection() {
        timerThread = new Timer();
        timerThread.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isReceivingMessageFromClient) {
                    // Check every minute that Server still receiving message from Client
                    Log.d(logTag, "Set client state to disconnected");
                    isReceivingMessageFromClient = false;
                }
            }
        }, 0, 60 * 1000);
    }
}
