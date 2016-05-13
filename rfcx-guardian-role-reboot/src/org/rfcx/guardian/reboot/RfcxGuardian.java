package org.rfcx.guardian.reboot;

import org.rfcx.guardian.reboot.database.RebootDb;
import org.rfcx.guardian.reboot.service.RebootIntentService;
import org.rfcx.guardian.reboot.service.ServiceMonitorIntentService;
import org.rfcx.guardian.utility.DateTimeUtils;
import org.rfcx.guardian.utility.rfcx.RfcxDeviceId;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;
import org.rfcx.guardian.utility.rfcx.RfcxRole;
import org.rfcx.guardian.utility.service.RfcxServiceHandler;

import android.app.Application;
import android.content.Context;

public class RfcxGuardian extends Application {
	
	public String version;
	Context context;
	
	public static final String APP_ROLE = "Reboot";

	private static final String TAG = "Rfcx-"+APP_ROLE+"-"+RfcxGuardian.class.getSimpleName();

	public RfcxDeviceId rfcxDeviceId = null; 
	public RfcxPrefs rfcxPrefs = null;
	public RfcxServiceHandler rfcxServiceHandler = null;
	
	// database access helpers
	public RebootDb rebootDb = null;
	
	@Override
	public void onCreate() {

		super.onCreate();

		this.rfcxDeviceId = new RfcxDeviceId(this, APP_ROLE);
		this.rfcxPrefs = new RfcxPrefs(this, APP_ROLE);
		this.rfcxServiceHandler = new RfcxServiceHandler(this, APP_ROLE);
		
		this.version = RfcxRole.getRoleVersion(this, TAG);
		this.rfcxPrefs.writeVersionToFile(this.version);
		
		setDbHandlers();
		setServiceHandlers();
		
		initializeRoleServices();
	}
	
	public void onTerminate() {
		super.onTerminate();
	}
	
	public void appResume() {

	}
	
	public void appPause() {
		
	}
	
	public void initializeRoleServices() {
		
		if (!this.rfcxServiceHandler.hasRun("OnLaunchServiceSequence")) {
			this.rfcxServiceHandler.triggerServiceSequence(
				"OnLaunchServiceSequence", 
					new String[] { 
						"RebootIntentService"+"|"+DateTimeUtils.nextOccurenceOf(23,55,0).getTimeInMillis()+"|"+(24*60*60*1000),
//						"ServiceMonitor"+"|"+"0"+"|"+(3*this.rfcxPrefs.getPrefAsInt("audio_cycle_duration"))
						}, 
				true);
		}
	}
	
	private void setDbHandlers() {
		int versionNumber = RfcxRole.getRoleVersionValue(this.version);
		this.rebootDb = new RebootDb(this,versionNumber);
	}

	private void setServiceHandlers() {
		this.rfcxServiceHandler.addService("ServiceMonitor", ServiceMonitorIntentService.class);
		this.rfcxServiceHandler.addService("RebootIntentService", RebootIntentService.class);
	}
    
}
