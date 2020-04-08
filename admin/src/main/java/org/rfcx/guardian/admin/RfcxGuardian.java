package org.rfcx.guardian.admin;

import org.rfcx.guardian.admin.device.android.capture.PhotoCaptureJobService;
import org.rfcx.guardian.admin.device.android.control.ADBSetService;
import org.rfcx.guardian.admin.device.android.control.BluetoothStateSetService;
import org.rfcx.guardian.admin.sms.SmsSendReceiveService;
import org.rfcx.guardian.admin.device.android.control.WifiStateSetService;
import org.rfcx.guardian.admin.device.android.system.DeviceDataTransferDb;
import org.rfcx.guardian.admin.device.android.system.DeviceDiskDb;
import org.rfcx.guardian.admin.device.android.system.DeviceRebootDb;
import org.rfcx.guardian.admin.device.android.system.DeviceSensorDb;
import org.rfcx.guardian.admin.device.android.system.DeviceSystemDb;
import org.rfcx.guardian.admin.device.android.system.DeviceSystemService;
import org.rfcx.guardian.admin.device.android.system.DeviceUtils;
import org.rfcx.guardian.utility.device.DeviceBattery;
import org.rfcx.guardian.utility.device.DeviceCPU;
import org.rfcx.guardian.utility.device.DeviceNetworkStats;
import org.rfcx.guardian.utility.device.control.DeviceBluetooth;
import org.rfcx.guardian.utility.device.control.DeviceWallpaper;
import org.rfcx.guardian.utility.device.hardware.DeviceHardware_OrangePi_3G_IOT;
import org.rfcx.guardian.utility.datetime.DateTimeUtils;
import org.rfcx.guardian.utility.device.DeviceConnectivity;
import org.rfcx.guardian.utility.device.control.DeviceAirplaneMode;
import org.rfcx.guardian.utility.device.hardware.DeviceHardware_Huawei_U8150;
import org.rfcx.guardian.utility.rfcx.RfcxDeviceGuid;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;
import org.rfcx.guardian.utility.rfcx.RfcxRole;
import org.rfcx.guardian.utility.service.RfcxServiceHandler;

import org.rfcx.guardian.admin.device.android.capture.DeviceLogCatDb;
import org.rfcx.guardian.admin.device.android.capture.DeviceLogCatCaptureService;
import org.rfcx.guardian.admin.device.android.capture.DeviceScreenShotDb;
import org.rfcx.guardian.admin.device.android.capture.DeviceScreenShotCaptureService;
import org.rfcx.guardian.admin.device.android.capture.ScheduledLogCatCaptureService;
import org.rfcx.guardian.admin.device.android.capture.ScheduledScreenShotCaptureService;
import org.rfcx.guardian.admin.device.android.control.AirplaneModeToggleService;
import org.rfcx.guardian.admin.device.android.control.AirplaneModeEnableService;
import org.rfcx.guardian.admin.device.android.control.ScheduledRebootService;
import org.rfcx.guardian.admin.device.android.control.DateTimeSntpSyncJobService;
import org.rfcx.guardian.admin.device.android.control.ForceRoleRelaunchService;
import org.rfcx.guardian.admin.device.android.control.RebootTriggerJobService;
import org.rfcx.guardian.admin.device.sentinel.DeviceSentinelService;
import org.rfcx.guardian.admin.device.sentinel.SentinelPowerDb;
import org.rfcx.guardian.admin.device.sentinel.SentinelPowerUtils;
import org.rfcx.guardian.admin.receiver.AirplaneModeReceiver;
import org.rfcx.guardian.admin.receiver.ConnectivityReceiver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

public class RfcxGuardian extends Application {
	
	public String version;
	
	public static final String APP_ROLE = "Admin";

	private static final String logTag = RfcxLog.generateLogTag(APP_ROLE, RfcxGuardian.class);

	public RfcxDeviceGuid rfcxDeviceGuid = null; 
	public RfcxPrefs rfcxPrefs = null;
	public RfcxServiceHandler rfcxServiceHandler = null;
	
	public DeviceScreenShotDb deviceScreenShotDb = null;
	public DeviceLogCatDb deviceLogCatDb = null;
	public SentinelPowerDb sentinelPowerDb = null;
	public DeviceSystemDb deviceSystemDb = null;
    public DeviceSensorDb deviceSensorDb = null;
    public DeviceRebootDb rebootDb = null;
    public DeviceDataTransferDb deviceDataTransferDb = null;
    public DeviceDiskDb deviceDiskDb = null;

	public SentinelPowerUtils sentinelPowerUtils = null;
	
	public DeviceConnectivity deviceConnectivity = new DeviceConnectivity(APP_ROLE);
	public DeviceAirplaneMode deviceAirplaneMode = new DeviceAirplaneMode(APP_ROLE);

	// Android Device Handlers
    public DeviceBattery deviceBattery = new DeviceBattery(APP_ROLE);
    public DeviceNetworkStats deviceNetworkStats = new DeviceNetworkStats(APP_ROLE);
    public DeviceCPU deviceCPU = new DeviceCPU(APP_ROLE);
    public DeviceUtils deviceUtils = null;
    public DeviceBluetooth deviceBluetooth = null;

	// Receivers
	private final BroadcastReceiver connectivityReceiver = new ConnectivityReceiver();
	private final BroadcastReceiver airplaneModeReceiver = new AirplaneModeReceiver();

//	private Activity rfcxCurrentActivity = null;
//	public Activity getCurrentActivity() { return rfcxCurrentActivity; }
//	public void setCurrentActivity(Activity rfcxCurrentActivity) { this.rfcxCurrentActivity = rfcxCurrentActivity; }
	
	public String[] RfcxCoreServices = 
			new String[] {
			//	"DeviceSystem",
				"DeviceSentinel"
			};
	
	@Override
	public void onCreate() {

		super.onCreate();

		this.rfcxDeviceGuid = new RfcxDeviceGuid(this, APP_ROLE);
		this.rfcxPrefs = new RfcxPrefs(this, APP_ROLE);
		this.rfcxServiceHandler = new RfcxServiceHandler(this, APP_ROLE);

		this.version = RfcxRole.getRoleVersion(this, logTag);
		this.rfcxPrefs.writeVersionToFile(this.version);

		this.registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		this.registerReceiver(airplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));

		setDbHandlers();
		setServiceHandlers();

		this.deviceUtils = new DeviceUtils(this);
		this.sentinelPowerUtils = new SentinelPowerUtils(this);

		// Hardware-specific hacks and modifications
		runHardwareSpecificModifications();

		// Android-Build-specific hacks and modifications
		// This is not necessary if this app role is running as "system"
		// DateTimeUtils.resetDateTimeReadWritePermissions(this);

//		if (DeviceUtils.isAppRoleApprovedForGeoPositionAccess(this)) {
//			ActivityCompat.requestPermissions(getCurrentActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, 32000);
//		}

		initializeRoleServices();
	}
	
	public void onTerminate() {
		super.onTerminate();

		this.unregisterReceiver(connectivityReceiver);
		this.unregisterReceiver(airplaneModeReceiver);
	}
	
	public void appResume() {

	}
	
	public void appPause() {

	}

	
	public void initializeRoleServices() {
		
		if (!this.rfcxServiceHandler.hasRun("OnLaunchServiceSequence")) {
			
			String[] runOnceOnlyOnLaunch = new String[] {
					"ServiceMonitor"
							+"|"+DateTimeUtils.nowPlusThisLong("00:02:00").getTimeInMillis() // waits two minutes before running
							+"|"+ServiceMonitor.SERVICE_MONITOR_CYCLE_DURATION
							,
					"ScheduledReboot"
							+"|"+DateTimeUtils.nextOccurenceOf(this.rfcxPrefs.getPrefAsString("reboot_forced_daily_at")).getTimeInMillis()
							+"|"+( 24 * 60 * 60 * 1000 ) // repeats daily
							,
					"ScheduledScreenShotCapture"
							+"|"+DateTimeUtils.nowPlusThisLong("00:00:45").getTimeInMillis() // waits forty five seconds before running
							+"|"+( this.rfcxPrefs.getPrefAsLong("admin_screenshot_capture_cycle") * 60 * 1000 )
							,
					"ScheduledLogCatCapture"
							+"|"+DateTimeUtils.nowPlusThisLong("00:03:00").getTimeInMillis() // waits three minutes before running
							+"|"+( this.rfcxPrefs.getPrefAsLong("admin_log_capture_cycle") * 60 * 1000 )
							,
					"ADBSet"
							+"|"+DateTimeUtils.nowPlusThisLong("00:00:10").getTimeInMillis() // waits ten seconds before running
							+"|"+"0" 																	// no repeat
							,
					"BluetoothStateSet"
							+"|"+DateTimeUtils.nowPlusThisLong("00:00:30").getTimeInMillis() // waits thirty seconds before running
							+"|"+"0" 																	// no repeat
							,
					"WifiStateSet"
							+"|"+DateTimeUtils.nowPlusThisLong("00:01:00").getTimeInMillis() // waits one minute before running
							+"|"+"0" 																	// no repeat
			};
			
			String[] onLaunchServices = new String[ RfcxCoreServices.length + runOnceOnlyOnLaunch.length ];
			System.arraycopy(RfcxCoreServices, 0, onLaunchServices, 0, RfcxCoreServices.length);
			System.arraycopy(runOnceOnlyOnLaunch, 0, onLaunchServices, RfcxCoreServices.length, runOnceOnlyOnLaunch.length);
			this.rfcxServiceHandler.triggerServiceSequence("OnLaunchServiceSequence", onLaunchServices, true, 0);
		}
	}
	
	private void setDbHandlers() {
		
		this.sentinelPowerDb = new SentinelPowerDb(this, this.version);
		this.deviceScreenShotDb = new DeviceScreenShotDb(this, this.version);
		this.deviceLogCatDb = new DeviceLogCatDb(this, this.version);
		this.deviceSystemDb = new DeviceSystemDb(this, this.version);
        this.deviceSensorDb = new DeviceSensorDb(this, this.version);
        this.rebootDb = new DeviceRebootDb(this, this.version);
        this.deviceDataTransferDb = new DeviceDataTransferDb(this, this.version);
        this.deviceDiskDb = new DeviceDiskDb(this, this.version);
	}

	private void setServiceHandlers() {
		this.rfcxServiceHandler.addService("ServiceMonitor", ServiceMonitor.class);
		this.rfcxServiceHandler.addService("RebootTrigger", RebootTriggerJobService.class);
		this.rfcxServiceHandler.addService("ScheduledReboot", ScheduledRebootService.class);
		this.rfcxServiceHandler.addService("AirplaneModeToggle", AirplaneModeToggleService.class);
		this.rfcxServiceHandler.addService("AirplaneModeEnable", AirplaneModeEnableService.class);
		this.rfcxServiceHandler.addService("BluetoothStateSet", BluetoothStateSetService.class);
		this.rfcxServiceHandler.addService("WifiStateSet", WifiStateSetService.class);
		this.rfcxServiceHandler.addService("ADBSet", ADBSetService.class);
        this.rfcxServiceHandler.addService("SmsSendReceive", SmsSendReceiveService.class);
		this.rfcxServiceHandler.addService("DateTimeSntpSyncJob", DateTimeSntpSyncJobService.class);
		this.rfcxServiceHandler.addService("DeviceSentinel", DeviceSentinelService.class);
		this.rfcxServiceHandler.addService("ForceRoleRelaunch", ForceRoleRelaunchService.class);

		this.rfcxServiceHandler.addService("DeviceSystem", DeviceSystemService.class);

		this.rfcxServiceHandler.addService("ScreenShotCapture", DeviceScreenShotCaptureService.class);
		this.rfcxServiceHandler.addService("ScheduledScreenShotCapture", ScheduledScreenShotCaptureService.class);

		this.rfcxServiceHandler.addService("LogCatCapture", DeviceLogCatCaptureService.class);
		this.rfcxServiceHandler.addService("ScheduledLogCatCapture", ScheduledLogCatCaptureService.class);

		this.rfcxServiceHandler.addService("PhotoCapture", PhotoCaptureJobService.class);

	}

	public String onPrefReSync(String prefKey) {

		if (prefKey.equalsIgnoreCase("admin_enable_bluetooth") || prefKey.equalsIgnoreCase("admin_enable_tcp_adb")) {
			rfcxServiceHandler.triggerService("BluetoothStateSet", false);
			rfcxServiceHandler.triggerService("ADBSet", false);
		}
		if (prefKey.equalsIgnoreCase("admin_enable_wifi") || prefKey.equalsIgnoreCase("admin_enable_tcp_adb")) {
			rfcxServiceHandler.triggerService("WifiStateSet", false);
			rfcxServiceHandler.triggerService("ADBSet", false);
		}
		return this.rfcxPrefs.getPrefAsString(prefKey);
	}

	private void runHardwareSpecificModifications() {

		if (DeviceHardware_OrangePi_3G_IOT.isDevice_OrangePi_3G_IOT()) {

			// Disable Sensor Listeners for sensors the don't exist on the OrangePi
			this.deviceUtils.disableSensorListener("accel"); // accelerometer
			this.deviceUtils.disableSensorListener("light");  // light meter

			Log.d(logTag, "Sensor Light:"+ this.deviceUtils.isSensorListenerAllowed("light"));
			Log.d(logTag, "Sensor Accel:"+ this.deviceUtils.isSensorListenerAllowed("accel"));

			// Set Desktop Wallpaper to empty black
			DeviceWallpaper.setWallpaper(this, R.drawable.black);

			// Rename Device Hardware with /system/build.prop.
			// Only occurs once, on initial launch, and requires reboot once complete.
			DeviceHardware_OrangePi_3G_IOT.checkSetDeviceHardwareIdentification(this);

		}

	}
    
}
