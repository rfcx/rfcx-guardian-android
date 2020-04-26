package org.rfcx.guardian.utility.device;

import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.utility.device.hardware.DeviceHardwareUtils;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.util.ArrayList;
import java.util.List;

public class AppProcessInfo {

	public AppProcessInfo(Context context) {
		this.context = context;
	}
	
	private Context context;

	private static final String logTag = RfcxLog.generateLogTag("Utils", "AppProcessInfo");

	public static int getAppProcessId() {
		int appPid = android.os.Process.myPid();
		return appPid;
	}

	public static int getAppUserId() {
		int appUid = android.os.Process.myUid();
		return appUid;
	}

	public static int[] getProcessInfoFromRole(String appRole, Context context) {

		int[] processIds = new int[] { 0, 0 };

		Cursor processInfoCursor = context.getContentResolver().query(
				RfcxComm.getUri(appRole, "process", null),
				RfcxComm.getProjection(appRole, "process"),null, null, null);

		if ((processInfoCursor != null) && (processInfoCursor.getCount() > 0)) { if (processInfoCursor.moveToFirst()) { try { do {
			processIds[0] = processInfoCursor.getInt(1);
			processIds[1] = processInfoCursor.getInt(2);
		} while (processInfoCursor.moveToNext()); } finally { processInfoCursor.close(); } } }

		return processIds;
	}

	public static String getPackageName(Context context) {
		return context.getPackageName();
	}

//	public static void killMyProcess() {
//		android.os.Process.killProcess(android.os.Process.myPid());
//	}

}