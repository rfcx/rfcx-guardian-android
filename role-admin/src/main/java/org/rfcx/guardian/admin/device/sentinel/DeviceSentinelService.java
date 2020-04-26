package org.rfcx.guardian.admin.device.sentinel;

import org.rfcx.guardian.admin.RfcxGuardian;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.rfcx.guardian.admin.device.android.system.DeviceUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

public class DeviceSentinelService extends Service {

	private static final String SERVICE_NAME = "DeviceSentinel";

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "DeviceSentinelService");
	
	private RfcxGuardian app;
	
	private boolean runFlag = false;
	private DeviceSentinelSvc deviceSentinelSvc;

	private int referenceCycleDuration = 1;

	private int innerLoopIncrement = 0;
	private int innerLoopsPerCaptureCycle = 1;
	private long innerLoopDelayRemainderInMilliseconds = 0;

	// Sampling adds to the duration of the overall capture cycle, so we cut it short slightly based on an EMPIRICALLY DETERMINED percentage
	// This can help ensure, for example, that a 60 second capture loop actually returns values with an interval of 60 seconds, instead of 61 or 62 seconds
	private double captureCycleLastDurationPercentageMultiplier = 0.98;
	private long captureCycleLastStartTime = 0;
	private long[] captureCycleMeasuredDurations = new long[] { 0, 0, 0 };
	private double[] captureCyclePercentageMultipliers = new double[] { 0, 0, 0 };

	private int outerLoopIncrement = 0;
	private int outerLoopCaptureCount = 0;
	private boolean isReducedCaptureModeActive = true;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.deviceSentinelSvc = new DeviceSentinelSvc();
		app = (RfcxGuardian) getApplication();
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.v(logTag, "Starting service: "+logTag);
		this.runFlag = true;
		app.rfcxServiceHandler.setRunState(SERVICE_NAME, true);
		try {
			this.deviceSentinelSvc.start();
		} catch (IllegalThreadStateException e) {
			RfcxLog.logExc(logTag, e);
		}
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		app.rfcxServiceHandler.setRunState(SERVICE_NAME, false);
		this.deviceSentinelSvc.interrupt();
		this.deviceSentinelSvc = null;
	}
	
	
	private class DeviceSentinelSvc extends Thread {
		
		public DeviceSentinelSvc() {
			super("DeviceSentinelService-DeviceSentinelSvc");
		}
		
		@Override
		public void run() {
			DeviceSentinelService deviceSentinelService = DeviceSentinelService.this;

			app = (RfcxGuardian) getApplication();

			while (deviceSentinelService.runFlag) {

				try {

					confirmOrSetCaptureParameters();

					// Inner Loop Behavior
					innerLoopIncrement = triggerOrSkipInnerLoopBehavior(innerLoopIncrement, innerLoopsPerCaptureCycle);

					if (innerLoopIncrement < innerLoopsPerCaptureCycle) {

						Thread.sleep(innerLoopDelayRemainderInMilliseconds);

					} else {

//						Log.e(logTag, SERVICE_NAME+" - "+ DateTimeUtils.getDateTime());

						app.rfcxServiceHandler.reportAsActive(SERVICE_NAME);

						// Outer Loop Behavior
						outerLoopIncrement = triggerOrSkipOuterLoopBehavior(outerLoopIncrement, outerLoopCaptureCount);

						if (app.rfcxPrefs.getPrefAsBoolean("admin_enable_sentinel_capture")) {

					//		if (app.sentinelPowerUtils.confirmConnection()) {

							app.sentinelPowerUtils.updateSentinelPowerValues();
							app.sentinelPowerUtils.saveSentinelPowerValuesToDatabase(app.getApplicationContext(), false);

					//		}

						} else {
							Log.d(logTag, "SentinelStats is explicitly disabled in prefs.");
						}
					}

				} catch (InterruptedException e) {
					deviceSentinelService.runFlag = false;
					app.rfcxServiceHandler.setRunState(SERVICE_NAME, false);
					RfcxLog.logExc(logTag, e);
				}

			}
			Log.v(logTag, "Stopping service: " + logTag);
		}		
	}

	private int triggerOrSkipInnerLoopBehavior(int innerLoopIncrement, int innerLoopsPerCaptureCycle) {

		innerLoopIncrement++;
		if (innerLoopIncrement > innerLoopsPerCaptureCycle) {
			innerLoopIncrement = 0;
		}

		//	Log.e(logTag, "RUN INNER LOOP BEHAVIOR...");

		return innerLoopIncrement;
	}

	private int triggerOrSkipOuterLoopBehavior(int outerLoopIncrement, int outerLoopCaptureCount) {

		outerLoopIncrement++;

		if (outerLoopIncrement >= outerLoopCaptureCount) {
			outerLoopIncrement = 0;
		}

		if (outerLoopIncrement == 1) {

			isReducedCaptureModeActive = DeviceUtils.isReducedCaptureModeActive(app.getApplicationContext());

			//	Log.e(logTag, "RUN OUTER LOOP BEHAVIOR...");

		} else {

		}

		return outerLoopIncrement;
	}

	private boolean confirmOrSetCaptureParameters() {

		if (app != null) {

			if (innerLoopIncrement == 0) {

				this.captureCycleLastStartTime = System.currentTimeMillis();

				int audioCycleDuration = app.rfcxPrefs.getPrefAsInt("audio_cycle_duration");

				// when audio capture is disabled (for any number of reasons), we continue to capture system stats...
				// however, we slow the capture cycle by the multiple indicated in DeviceUtils.inReducedCaptureModeExtendCaptureCycleByFactorOf
				int prefsReferenceCycleDuration = this.isReducedCaptureModeActive ? audioCycleDuration : (audioCycleDuration * DeviceUtils.inReducedCaptureModeExtendCaptureCycleByFactorOf);

				if (this.referenceCycleDuration != prefsReferenceCycleDuration) {

					this.referenceCycleDuration = prefsReferenceCycleDuration;
					this.innerLoopsPerCaptureCycle = DeviceUtils.getInnerLoopsPerCaptureCycle(prefsReferenceCycleDuration);
					this.outerLoopCaptureCount = DeviceUtils.getOuterLoopCaptureCount(prefsReferenceCycleDuration);

					long samplingOperationDuration = 0;
					this.innerLoopDelayRemainderInMilliseconds = DeviceUtils.getInnerLoopDelayRemainder(prefsReferenceCycleDuration, this.captureCycleLastDurationPercentageMultiplier, samplingOperationDuration);

					Log.d(logTag, "SentinelStats Capture" + (this.isReducedCaptureModeActive ? "" : " (currently limited)") + ": " +
							"Snapshots (all metrics) taken every " + Math.round(DeviceUtils.getCaptureCycleDuration(prefsReferenceCycleDuration) / 1000) + " seconds.");
				}
			}

		} else {
			return false;
		}

		return true;
	}

}