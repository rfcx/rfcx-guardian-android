package org.rfcx.guardian.utility.device.capture;

import java.util.Date;

import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

public class DeviceMobileNetwork {

	public DeviceMobileNetwork(String appRole) { this.logTag = RfcxLog.generateLogTag(appRole, "DeviceMobileNetwork"); }

	private String logTag;

	public TelephonyManager telephonyManager = null;
	public SignalStrength signalStrength = null;


	public JSONArray getSignalStrengthAsJsonArray() {
		JSONArray signalJsonArray = new JSONArray();
		try {
			JSONObject signalJson = new JSONObject();

			signalJson.put("signal", this.signalStrength.getGsmSignalStrength());
			signalJsonArray.put(signalJson);

		} catch (Exception e) {
			RfcxLog.logExc(logTag, e);

		} finally {
			return signalJsonArray;
		}
	}


	public String[] getMobileNetworkSummary() {
		
		// array indices are: measured_at, signal_strength (dBm), network_type, carrier_name
		String[] mobileNetworkSummary = new String[] { ""+(new Date()).getTime(), "", "", "" };

		// GSM values
		boolean	isGsmActive = this.signalStrength.isGsm();
		int gsmBitErrorRate = this.signalStrength.getGsmBitErrorRate(); // bit error rate values (0-7, 99) as defined in TS 27.007 8.5
		int	gsmSignalStrength = this.signalStrength.getGsmSignalStrength(); // strength values (0-31, 99) as defined in TS 27.007 8.5
		int gsmSignalStrength_dBm = ( -113 + ( 2 * gsmSignalStrength ) ); // converting signal strength to decibel-milliwatts (dBm)
		
		// CDMA values
//		int	cdmaRssi = signalStrength.getCdmaDbm(); // CDMA RSSI value in dBm
//		int	cdmaEcIo = signalStrength.getCdmaEcio(); //CDMA Ec/Io value in dB*10
		
		// EVDO values
//		int	evdoRssi = signalStrength.getEvdoDbm(); //EVDO RSSI value in dBm
//		int	evdoEcIo = signalStrength.getEvdoEcio(); //EVDO Ec/Io value in dB*10
//		int	evdoSnr = signalStrength.getEvdoSnr(); //signal to noise ratio. Valid values are 0-8. 8 is the highest.
		
		if (gsmSignalStrength_dBm > 0) {
			mobileNetworkSummary[1] = "0";
		} else {
			mobileNetworkSummary[1] = ""+gsmSignalStrength_dBm;
			mobileNetworkSummary[2] = getNetworkTypeCategoryAsString(this.telephonyManager.getNetworkType());
			mobileNetworkSummary[3] = this.telephonyManager.getNetworkOperatorName();
		}
		
		return mobileNetworkSummary;
	}
	
	private static String getNetworkTypeCategoryAsString(int getNetworkType) {
		String networkTypeCategory = null;
	    switch (getNetworkType) {
	        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
	        	networkTypeCategory = "unknown";
	            break;
	        case TelephonyManager.NETWORK_TYPE_IDEN:
	        	networkTypeCategory = "iden";
	            break;
	        case TelephonyManager.NETWORK_TYPE_GPRS:
	        	networkTypeCategory = "gprs";
	            break;
	        case TelephonyManager.NETWORK_TYPE_EDGE:
	        	networkTypeCategory = "edge";
	            break;
	        case TelephonyManager.NETWORK_TYPE_UMTS:
	        	networkTypeCategory = "umts";
	            break;
	        case TelephonyManager.NETWORK_TYPE_CDMA:
	        	networkTypeCategory = "cdma";
	            break;
	        case TelephonyManager.NETWORK_TYPE_1xRTT:
	        	networkTypeCategory = "1xrtt";
	            break;
	        case TelephonyManager.NETWORK_TYPE_EVDO_0:
	        	networkTypeCategory = "evdo0";
	            break;
	        case TelephonyManager.NETWORK_TYPE_EVDO_A:
	        	networkTypeCategory = "evdoA";
	            break;
	        case TelephonyManager.NETWORK_TYPE_EVDO_B:
	        	networkTypeCategory = "evdoB";
	            break;
	        case TelephonyManager.NETWORK_TYPE_HSDPA:
	        	networkTypeCategory = "hsdpa";
	            break;
	        case TelephonyManager.NETWORK_TYPE_HSUPA:
	        	networkTypeCategory = "hsupa";
	            break;
	        case TelephonyManager.NETWORK_TYPE_HSPA:
	        	networkTypeCategory = "hspa";
	            break;
	        default:
	        	networkTypeCategory = null;
	    }
	    return networkTypeCategory;
	}


}
