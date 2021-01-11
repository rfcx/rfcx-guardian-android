package org.rfcx.guardian.utility.asset;

import android.content.Context;
import android.os.Environment;

import org.rfcx.guardian.utility.misc.FileUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

public class RfcxClassifierFileUtils {

	public RfcxClassifierFileUtils(Context context, String appRole) {
		this.logTag = RfcxLog.generateLogTag(appRole, "RfcxClassifierFileUtils");
		this.appRole = appRole;
		initializeClassifierDirectories(context);
	}

	private String logTag;
	private String appRole = "Utils";
	
//	private static final SimpleDateFormat dirDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	private static final String classifierFileType = "tflite";
	
	public static void initializeClassifierDirectories(Context context) {

		FileUtils.initializeDirectoryRecursively(classifierDownloadDir(context), false);
		FileUtils.initializeDirectoryRecursively(classifierGalleryDir(context), false);
		FileUtils.initializeDirectoryRecursively(classifierActiveDir(context), false);
	}
	
	public static String classifierDownloadDir(Context context) {
		return context.getFilesDir().toString() + "/classifiers/download";
	}

	public static String classifierGalleryDir(Context context) {
		return context.getFilesDir().toString() + "/classifiers/gallery";
	}

	public static String classifierActiveDir(Context context) {
		return context.getFilesDir().toString() + "/classifiers/active";
	}

	public static String getClassifierFileLocation_Download(Context context, long timestamp) {
		return classifierDownloadDir(context) + "/" + timestamp + "." + classifierFileType;
	}

	public static String getClassifierFileLocation_Gallery(Context context, long timestamp) {
		return classifierGalleryDir(context) + "/" + timestamp + "." + classifierFileType;
	}

	public static String getClassifierFileLocation_Active(Context context, long timestamp) {
		return classifierActiveDir(context) + "/" + timestamp + "." + classifierFileType;
	}




	
}
