package org.rfcx.guardian.updater.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.rfcx.guardian.updater.RfcxGuardian;
import org.rfcx.guardian.utility.install.InstallUtils;
import org.rfcx.guardian.utility.misc.FileUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

public class InstallAppService extends Service {

    public static final String SERVICE_NAME = "InstallApp";

    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "InstallAppService");

    private RfcxGuardian app;

    private boolean runFlag = false;
    private InstallApp installApp;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.installApp = new InstallApp();
        app = (RfcxGuardian) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.v(logTag, "Starting service: " + logTag);
        this.runFlag = true;
        app.rfcxSvc.setRunState(SERVICE_NAME, true);
        try {
            this.installApp.start();
        } catch (IllegalThreadStateException e) {
            RfcxLog.logExc(logTag, e);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.runFlag = false;
        app.rfcxSvc.setRunState(SERVICE_NAME, false);
        this.installApp.interrupt();
        this.installApp = null;
    }

    private class InstallApp extends Thread {

        public InstallApp() {
            super("InstallAppService-InstallApp");
        }

        @Override
        public void run() {
            InstallAppService installAppService = InstallAppService.this;

            try {

                String installedVersion = RfcxRole.getRoleVersionByName(app.installUtils.installRole, RfcxGuardian.APP_ROLE, app.getApplicationContext());
                int installedVersionValue = InstallUtils.calculateVersionValue(installedVersion);
                int versionValueToInstall = InstallUtils.calculateVersionValue(app.installUtils.installVersion);

                if (versionValueToInstall > installedVersionValue) {
                    if (app.installUtils.installApkAndVerify()) {
                        Log.d(logTag, "Installation Successful: " + app.installUtils.installRole + ", " + app.installUtils.installVersion);
                        app.apiUpdateRequestUtils.attemptToTriggerUpdateRequest(true, true);
                    } else {
                        Log.e(logTag, "Installation Failed: " + app.installUtils.installRole + ", " + app.installUtils.installVersion);
                    }
                } else {
                    Log.e(logTag, "Installation Cancelled: Newer version already installed. " + app.installUtils.installRole + ", (installed: " + installedVersion + ", attempted: " + app.installUtils.installVersion + ")");
                }
            } catch (Exception e) {
                RfcxLog.logExc(logTag, e);
            } finally {
                FileUtils.delete(app.installUtils.apkPathExternal);
                app.apiUpdateRequestUtils.lastUpdateRequestTriggered = 0;
            }

            app.rfcxSvc.setRunState(SERVICE_NAME, false);
            app.rfcxSvc.stopService(SERVICE_NAME);
        }
    }


}
