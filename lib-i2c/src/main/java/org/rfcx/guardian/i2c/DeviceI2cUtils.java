package org.rfcx.guardian.i2c;

import android.util.Log;

import org.rfcx.guardian.utility.misc.ArrayUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DeviceI2cUtils {

    private final String logTag;
    private I2cTools i2cTools;
    private int i2cAdapterReceipt;
    // i2cInterface should be a low integer, including zero, as in /dev/i2c-0 or /dev/i2c-1 or /dev/i2c-2
    private int i2cInterface = 0;

    public DeviceI2cUtils(String appRole) {
        this.logTag = RfcxLog.generateLogTag(appRole, "DeviceI2cUtils");
    }

    private static Number twosComplementHexToDec(String hexStr) {

        if (hexStr == null) {
            throw new NullPointerException("twosComplementHexToDec: hex String is null.");
        }

        if (hexStr.equals("")) {
            return Byte.valueOf("0");
        }

        // If you want to pad "FFF" to "0FFF" do it here.
//		hex = hex+"FFF";

        hexStr = hexStr.toUpperCase();

        BigInteger numVal;

        //	Check if high bit is set.
//		if (	hexStr.startsWith("8") || hexStr.startsWith("9")
//			||	hexStr.startsWith("A") || hexStr.startsWith("B")
//			||	hexStr.startsWith("C") || hexStr.startsWith("D")
//			||	hexStr.startsWith("E") || hexStr.startsWith("F")
//		) {
//			// Negative number
//			numVal = new BigInteger(hexStr, 16);
//			BigInteger subtrahend = BigInteger.ONE.shiftLeft(hexStr.length() * 4);
//			numVal = numVal.subtract(subtrahend);
//		} else {
        // Positive number
        numVal = new BigInteger(hexStr, 16);
//		}
//
        // Cut BigInteger down to size and return value
        if (hexStr.length() <= 2) {
            return numVal.byteValue();
        }
        if (hexStr.length() <= 4) {
            return numVal.shortValue();
        }
        if (hexStr.length() <= 8) {
            return numVal.intValue();
        }
        if (hexStr.length() <= 16) {
            return numVal.longValue();
        }
        return numVal;
    }

    public static long twosComplementHexToDecAsLong(String hexStr) {
        return Long.parseLong(twosComplementHexToDec(hexStr) + "");
    }

    public void setInterface(int i2cInterface) {
        this.i2cInterface = i2cInterface;
    }

    public void initializeOrReInitialize() {

        Log.i(logTag, "Attempting to initialize I2C interface '/dev/i2c-" + this.i2cInterface + "'");

        if (isI2cHandlerAccessible()) {

            this.i2cTools = new I2cTools();
            this.i2cTools.i2cDeInit(this.i2cInterface);
            this.i2cAdapterReceipt = i2cTools.i2cInit(this.i2cInterface);

            if (isInitialized(true)) {
                Log.i(logTag, "I2C interface '/dev/i2c-" + this.i2cInterface + "' successfully initialized.");
            }

        } else {
            Log.e(logTag, "I2C handler '/dev/i2c-" + this.i2cInterface + "' is NOT accessible. Initialization failed.");
        }
    }

    public boolean isInitialized(boolean printFeedbackInLog) {

        boolean isInitialized = (this.i2cAdapterReceipt >= 0) && isI2cHandlerAccessible();

        if (printFeedbackInLog && !isInitialized) {
            Log.e(logTag, "I2C interface '/dev/i2c-" + this.i2cInterface + "' is NOT initialized.");
        }

        return isInitialized;
    }

    // i2cSET

    private void throwExceptionIfNotInitialized() throws Exception {
        if (!isInitialized(false)) {
            throw new Exception("I2C Initialization Failed");
        }
    }

    // i2cGET

    public boolean isI2cHandlerAccessible() {
        return (new File("/dev/i2c-" + this.i2cInterface)).canRead();
    }

    public boolean i2cSet(String subAddr, String mainAddr, int data, boolean isWord) {
        List<String[]> i2cLabelsAndSubAddresses = new ArrayList<String[]>();
        i2cLabelsAndSubAddresses.add(new String[]{"no-label", subAddr, "0x" + Integer.toHexString(data & 0xFF)});
        boolean isSet = i2cSet(i2cLabelsAndSubAddresses, mainAddr, isWord);
        return isSet;
    }

    public boolean i2cSet(List<String[]> i2cLabelsAddressesValues, String mainAddr, boolean isWord/*, boolean parseAsHex*/) {

        boolean isSet = (i2cLabelsAddressesValues.size() == 0);

        for (String[] i2cRow : i2cLabelsAddressesValues) {

            try {

                throwExceptionIfNotInitialized();

                isSet = this.i2cTools.i2cSet(i2cAdapterReceipt, mainAddr, i2cRow[1], i2cRow[2], isWord);

            } catch (Exception e) {
                RfcxLog.logExc(logTag, e);
            }
        }

        return isSet;
    }

    public byte i2cGetAsByte(String subAddr, String mainAddr, boolean parseAsHex, boolean isWord) {
        String rtrnValAsString = i2cGetAsString(subAddr, mainAddr, parseAsHex, isWord);
        byte rtrnVal = 0;
        if (rtrnValAsString != null) {
            try {
                rtrnVal = Byte.parseByte(rtrnValAsString);
            } catch (Exception e) {
                RfcxLog.logExc(logTag, e);
            }
        }
        return rtrnVal;
    }

    public byte[] i2cGetBlockAsByteArr(String startAddr, String mainAddr, byte[] buffer, boolean parseAsHex, boolean isWord) {

        int startAddrInt = Integer.decode(startAddr);
        for (int i = 0; i < buffer.length; i++) {
            String subAddr = "0x" + Integer.toHexString((startAddrInt + i) & 0xFF);
            buffer[i] = i2cGetAsByte(subAddr, mainAddr, parseAsHex, isWord);
        }
        return buffer;
    }

    public long i2cGetAsLong(String subAddr, String mainAddr, boolean parseAsHex, boolean isWord) {
        String rtrnValAsString = i2cGetAsString(subAddr, mainAddr, parseAsHex, isWord);
        long rtrnVal = 0;
        if (rtrnValAsString != null) {
            try {
                rtrnVal = Long.parseLong(rtrnValAsString);
            } catch (Exception e) {
                RfcxLog.logExc(logTag, e);
            }
        }
        return rtrnVal;
    }

    public String i2cGetAsString(String subAddr, String mainAddr, boolean parseAsHex, boolean isWord) {
        List<String[]> i2cLabelsAndSubAddresses = new ArrayList<String[]>();
        i2cLabelsAndSubAddresses.add(new String[]{"no-label", subAddr});
        List<String[]> i2cReturn = i2cGet(i2cLabelsAndSubAddresses, mainAddr, parseAsHex, isWord, new String[]{});
        String rtrnVal = null;
        if (i2cReturn.size() > 0) {
            try {
                rtrnVal = i2cReturn.get(0)[1];
            } catch (Exception e) {
                RfcxLog.logExc(logTag, e);
            }
        }
        return rtrnVal;
    }

    public List<String[]> i2cGet(List<String[]> i2cLabelsAndSubAddresses, String mainAddr, boolean parseAsHex, boolean isWord) {
        return i2cGet(i2cLabelsAndSubAddresses, mainAddr, parseAsHex, isWord, new String[]{});
    }

    public List<String[]> i2cGet(List<String[]> i2cLabelsAndSubAddresses, String mainAddr, boolean parseAsHex, boolean isWord, String[] rtrnValsWithoutTwosComplement) {

        List<String[]> i2cLabelsAndOutputValues = new ArrayList<String[]>();
        List<String> i2cValues = new ArrayList<String>();

        try {

            throwExceptionIfNotInitialized();

            for (String[] i2cRow : i2cLabelsAndSubAddresses) {
                String i2cValue = i2cTools.i2cGet(i2cAdapterReceipt, mainAddr, i2cRow[1], false, isWord);
                i2cValues.add(i2cValue);
            }

            int lineIndex = 0;
            for (String i2cValue : i2cValues) {

                String i2cStrValue = i2cValue;

                if (parseAsHex && (i2cValue.indexOf("0x") == 0)) {

                    if (!doesStringArrayContainString(rtrnValsWithoutTwosComplement, i2cLabelsAndSubAddresses.get(lineIndex)[0])) {
                        i2cStrValue = twosComplementHexToDec(i2cValue.substring(1 + i2cValue.indexOf("x"))) + "";
                    } else {
                        i2cStrValue = "" + Integer.parseInt(i2cValue.substring(1 + i2cValue.indexOf("x")), 16);
                    }

                } else if (parseAsHex) {
                    i2cStrValue = null;
                }

                i2cLabelsAndOutputValues.add(new String[]{i2cLabelsAndSubAddresses.get(lineIndex)[0], i2cStrValue});
                lineIndex++;
            }

        } catch (Exception e) {
            RfcxLog.logExc(logTag, e);
        }
        return i2cLabelsAndOutputValues;
    }

    private boolean doesStringArrayContainString(String[] strArr, String strInd) {
        boolean doesContain = false;
        for (String sInd : strArr) {
            if (sInd.equalsIgnoreCase(strInd)) {
                doesContain = true;
                break;
            }
        }
        return doesContain;
    }

}
