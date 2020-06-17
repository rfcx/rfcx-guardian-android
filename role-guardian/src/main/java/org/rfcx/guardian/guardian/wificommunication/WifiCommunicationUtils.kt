package org.rfcx.guardian.guardian.wificommunication

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.rfcx.guardian.guardian.RfcxGuardian
import org.rfcx.guardian.guardian.audio.capture.AudioCaptureUtils
import org.rfcx.guardian.utility.rfcx.RfcxLog

class WifiCommunicationUtils(context: Context) {

    private val app = context.applicationContext as RfcxGuardian
    private val logTag = RfcxLog.generateLogTag(
        RfcxGuardian.APP_ROLE,
        "WifiCommunicationUtils"
    )

    fun getCurrentConfigurationAsJson(): JSONArray {
        val configurationJsonArray = JSONArray()
        try {
            val configurationJson = JSONObject()
            val simpleRate = app.rfcxPrefs.getPrefAsInt("audio_sample_rate")
            val bitrate = app.rfcxPrefs.getPrefAsInt("audio_encode_bitrate")
            val fileFormat = app.rfcxPrefs.getPrefAsString("audio_encode_codec")
            val duration = app.rfcxPrefs.getPrefAsInt("audio_cycle_duration")

            configurationJson.let {
                it.put("sample_rate", simpleRate)
                it.put("bitrate", bitrate)
                it.put("file_format", fileFormat)
                it.put("duration", duration)
            }

            configurationJsonArray.put(configurationJson)
        } catch (e: Exception) {
            RfcxLog.logExc(logTag, e)
        } finally {
            return configurationJsonArray
        }
    }
}
