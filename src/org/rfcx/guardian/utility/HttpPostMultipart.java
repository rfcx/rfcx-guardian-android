package org.rfcx.guardian.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.text.TextUtils;
import android.util.Log;

public class HttpPostMultipart {

	private static final String TAG = HttpPostMultipart.class.getSimpleName();
	private static final String NULL_EXC = "Exception thrown, but exception itself is null.";

    // need to make these longer...
	private static int requestReadTimeout = 10000;
	private static int requestConnectTimeout = 30000;
	
	public static String doMultipartPost(String fullUrl, List<String[]> keyValueParameters, List<String[]> keyFilepathMimeAttachments) {
		MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			for (String[] keyFilepathMime : keyFilepathMimeAttachments) {
				ContentBody contentBody = new FileBody(
						(new File(keyFilepathMime[1])),
						keyFilepathMime[1].substring(1+keyFilepathMime[1].lastIndexOf("/")), 
						keyFilepathMime[2], null);
				requestEntity.addPart(keyFilepathMime[0], contentBody);
			}
			for (String[] keyValue : keyValueParameters) {
				requestEntity.addPart(keyValue[0], new StringBody(URLEncoder.encode(keyValue[1])));
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,(e!=null) ? TextUtils.join(" | ", e.getStackTrace()) : NULL_EXC);
		} catch (Exception e) {
			Log.e(TAG,(e!=null) ? TextUtils.join(" | ", e.getStackTrace()) : NULL_EXC);
		}
		return executeMultipartPost(fullUrl, requestEntity);
	}
    
	private static String executeMultipartPost(String fullUrl, MultipartEntity requestEntity) {
		try {
	    	String inferredProtocol = fullUrl.substring(0, fullUrl.indexOf(":"));
			if (inferredProtocol.equals("http")) {
				return doPostAsHttp((new URL(fullUrl)), requestEntity);
			} else if (inferredProtocol.equals("https")) {
				return doPostAsHttps((new URL(fullUrl)), requestEntity);
			} else {
				return "Inferred protocol was neither HTTP nor HTTPS.";
			}
		} catch (MalformedURLException e) {
			Log.e(TAG,(e!=null) ? TextUtils.join(" | ", e.getStackTrace()) : NULL_EXC);
			return "Bad URL";
		}
	}
	
	private static String doPostAsHttp(URL url, MultipartEntity entity) {
	    try {
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(requestReadTimeout);
	        conn.setConnectTimeout(requestConnectTimeout);
	        conn.setRequestMethod("POST");
	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setFixedLengthStreamingMode((int) entity.getContentLength());
//	        reqConn.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
	        conn.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
	        OutputStream outputStream = conn.getOutputStream();
	        entity.writeTo(outputStream);
	        outputStream.close();
	        conn.connect();
		    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	            return readResponseStream(conn.getInputStream());
	        }
	    } catch (Exception e) {
	    	Log.e(TAG,(e!=null) ? TextUtils.join(" | ", e.getStackTrace()) : NULL_EXC);
	    }
	    return "The request returned an error.";        
	}
	
	private static String doPostAsHttps(URL url, MultipartEntity entity) {
	    try {
	        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	        conn.setReadTimeout(requestReadTimeout);
	        conn.setConnectTimeout(requestConnectTimeout);
	        conn.setRequestMethod("POST");
	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setFixedLengthStreamingMode((int) entity.getContentLength());
//	        reqConn.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
	        conn.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
	        OutputStream outputStream = conn.getOutputStream();
	        entity.writeTo(outputStream);
	        outputStream.close();
	        conn.connect();
		    if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
	            return readResponseStream(conn.getInputStream());
	        }
	    } catch (Exception e) {
	    	Log.e(TAG,(e!=null) ? TextUtils.join(" | ", e.getStackTrace()) : NULL_EXC);
	    }
	    return "The request returned an error.";        
	}

	private static String readResponseStream(InputStream in) {
	    BufferedReader br = null;
	    StringBuilder sb = new StringBuilder();
	    try {
	        br = new BufferedReader(new InputStreamReader(in));
	        String line = "";
	        while ((line = br.readLine()) != null) {
	            sb.append(line);
	        }
	    } catch (IOException e) {
	    	Log.e(TAG,(e!=null) ? TextUtils.join(" | ", e.getStackTrace()) : NULL_EXC);
	    } finally {
	        if (br != null) {
	            try {
	                br.close();
	            } catch (IOException e) {
	            	Log.e(TAG,(e!=null) ? TextUtils.join(" | ", e.getStackTrace()) : NULL_EXC);
	            }
	        }
	    }
	    return sb.toString();
	} 
	
}
