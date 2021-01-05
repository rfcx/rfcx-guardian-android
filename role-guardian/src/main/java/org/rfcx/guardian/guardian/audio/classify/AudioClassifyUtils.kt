package org.rfcx.guardian.guardian.audio.classify

import android.content.Context
import android.util.Log
import org.rfcx.guardian.guardian.RfcxGuardian
import org.rfcx.guardian.guardian.audio.classify.AudioConverter.sliceTo
import org.rfcx.guardian.utility.rfcx.RfcxLog
import java.io.File

class AudioClassifyUtils(context: Context) {

    private val logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "AudioClassifyUtils")

    private val app: RfcxGuardian = context.applicationContext as RfcxGuardian

    private val predictor = MLPredictor()

    fun classifyAudio(file: File) {
        val path = file.absolutePath
        classifyAudio(path)
    }

    fun classifyAudio(path: String) {
//        val step = app.rfcxPrefs.getPrefAsInt("prediction_step_size")
        predictor.also {
            it.load()
            AudioConverter.readAudioSimple(path).sliceTo(0).forEach { audioChunk ->
                if (audioChunk.size == 11700) {
                    val output = it.run(audioChunk)
                    Log.d(logTag, output)
                }
            }
        }
    }
}