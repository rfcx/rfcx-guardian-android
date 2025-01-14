package org.rfcx.guardian.utility.network;

import android.content.Context;
import android.util.Log;

import org.rfcx.guardian.utility.misc.DateTimeUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadSpeedTest extends HttpGet {

    private final Context context;
    private final String logTag;
    public DownloadSpeedTest(Context context, String appRole) {
        super(context, appRole);
        this.context = context;
        this.logTag = RfcxLog.generateLogTag(appRole, "DownloadSpeedTest");
    }

    public double getDownloadSpeedTest(String fullUrl) throws IOException {
        long startTime = System.currentTimeMillis();
        StringBuilder url = (new StringBuilder()).append(fullUrl);

        Log.v(logTag, "Initializing request to " + url.toString());

        String testPath = context.getFilesDir().getAbsolutePath() + "test_download.txt";
        File file = new File(testPath);
        if (file.exists()) {
            file.delete();
        }
        InputStream inputStream = httpGetFileInputStream(url.toString());
        FileOutputStream fileOutputStream = httpGetFileOutputStream(file.getAbsolutePath(), this.logTag);

        if ((inputStream != null) && (fileOutputStream != null)) {
            int downloadedContent = writeFileForTest(inputStream, fileOutputStream, this.logTag);
            fileOutputStream.flush();
            fileOutputStream.close();
            long downloadTime = System.currentTimeMillis() - startTime;
            double downloadSpeed = (downloadedContent * 1.0) / downloadTime;
            Log.v(logTag, "Completed (" + DateTimeUtils.milliSecondDurationAsReadableString(downloadTime) + ") from " + fullUrl);
            return downloadSpeed;
        }
        Log.e(logTag, "Download Failed (" + DateTimeUtils.milliSecondDurationAsReadableString((System.currentTimeMillis() - startTime)) + ") from " + fullUrl);
        return -1;
    }

    private int writeFileForTest(InputStream inputStream, FileOutputStream fileOutputStream, String logTag) {
        int downloadedContent = 0;
        try {
            byte[] buffer = new byte[8192];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) != -1) {
                downloadedContent += bufferLength;
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            return downloadedContent;
        } catch (IOException e) {
            RfcxLog.logExc(logTag, e);
            return downloadedContent;
        }
    }
}
