package org.rfcx.guardian.admin.comms.swm

import android.content.Context
import android.text.TextUtils
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.rfcx.guardian.admin.RfcxGuardian
import org.rfcx.guardian.admin.device.android.system.DeviceUtils
import org.rfcx.guardian.utility.device.DeviceSmsUtils
import org.rfcx.guardian.utility.misc.ArrayUtils
import org.rfcx.guardian.utility.misc.FileUtils
import org.rfcx.guardian.utility.misc.ShellCommands
import org.rfcx.guardian.utility.rfcx.RfcxComm
import org.rfcx.guardian.utility.rfcx.RfcxLog
import java.lang.Exception
import java.util.*

class SwmUtils(context: Context) {
    var app: RfcxGuardian = context.applicationContext as RfcxGuardian
    var swmCommand: SwmCommand? = null
    private var busyBoxBin: String? = null
    private var ttyPath: String? = null

    @kotlin.jvm.JvmField
    var isInFlight = false

    @kotlin.jvm.JvmField
    var consecutiveDeliveryFailureCount = 0
    fun init(ttyPath: String, busyBoxBin: String) {
        this.ttyPath = ttyPath
        this.busyBoxBin = busyBoxBin
        this.swmCommand = SwmCommand(SwmUartShell(ttyPath, busyBoxBin, baudRate))
    }

    fun setupSwmUtils() {
        app.deviceGpioUtils.runGpioCommand("DOUT", "voltage_refr", true)
        setPower(true)
        setPower(false)
    }

    fun findRunningSerialProcessIds(): IntArray {
        val processIds: MutableList<Int> = ArrayList()
        isInFlight = false
        if (!FileUtils.exists(busyBoxBin)) {
            Log.e(
                logTag,
                "Could not run findRunningSerialProcessIds(). BusyBox binary not found on system."
            )
        } else {
            val processScan =
                ShellCommands.executeCommandAsRoot("$busyBoxBin ps -ef | grep /dev/ttyMT")
            for (scanRtrn in processScan) {
                if (scanRtrn.contains("microcom") || scanRtrn.contains("stty")) {
                    val processId = scanRtrn.substring(0, scanRtrn.indexOf("root"))
                    processIds.add(processId.toInt())
                }
            }
        }
        return ArrayUtils.ListToIntArray(processIds)
    }

    fun setPower(setToOn: Boolean) {
        app.deviceGpioUtils.runGpioCommand("DOUT", "satellite_power", setToOn)
    }

    val isPowerOn: Boolean
        get() = app.deviceGpioUtils.readGpioValue("satellite_power", "DOUT")
    val isNetworkAvailable: Boolean
        get() = app.deviceGpioUtils.readGpioValue("satellite_state", "DOUT")


    fun sendSwmMessage(msgStr: String): Boolean {
        val responses = swmCommand?.transmitData(msgStr)
        if (responses != null) {
            return true
        }
        return false
    }

    private fun updateQueueMessagesFromSwarm(): Boolean {
        val responses = swmCommand?.getUnsentMessages()
        if (responses != null) {
            val guardianMessageIdQueues = app.swmMessageDb.dbSwmQueued.allRows
            val swarmMessageIdQueues = ArrayList<String>()
            for (response in responses) {
                //                            hexdecimal data         message id   timestamp
                // Example message : $MT 68692066726f6d20737761726d,4428826476689,1605639598*55
                swarmMessageIdQueues.add(response.split(",").toTypedArray()[1])
            }
            for (guardianMessage in guardianMessageIdQueues) {
                if (!swarmMessageIdQueues.contains(guardianMessage[4])) {
                    app.swmMessageDb.dbSwmSent.insert(
                        guardianMessage[1].toLong(),
                        guardianMessage[2],
                        guardianMessage[3],
                        guardianMessage[4]
                    )
                    app.swmMessageDb.dbSwmQueued.deleteSingleRowByMessageId(guardianMessage[4])
                }
            }
            return true
        }
        return false
    }

    private fun setSleep(time: Long): Boolean {
        val responses = swmCommand?.sleep(time)
        if (responses != null) {
            return true
        }
        return false
    }

    private fun getRecentSatelliteSignal(): Boolean {
        val responses = swmCommand?.getRecentSignal()
        if (responses != null) {
            return true
        }
        return false
    }

    private fun getDateTime(): Boolean {
        val responses = swmCommand?.getDateTime()
        if (responses != null) {
            return true
        }
        return false
    }

    companion object {
        private val logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "SwmUtils")
        private const val baudRate = 115200
        const val sendCmdTimeout: Long = 70000
        const val prepCmdTimeout: Long = 2500
        const val powerCycleAfterThisManyConsecutiveDeliveryFailures = 5

        // Incoming Message Tools
        @Throws(JSONException::class)
        fun processIncomingSwm(smsObj: JSONObject, context: Context) {
            val app = context.applicationContext as RfcxGuardian

            // In this case, the message arrived from the API SMS address, so we attempt to parse it
            Log.w(logTag, "SWM received from API ''.")
            val segmentPayload = smsObj.getString("body")
            val swmSegmentReceivedResponse = app.resolver.query(
                RfcxComm.getUri(
                    "guardian",
                    "segment_receive_swm",
                    RfcxComm.urlEncode(segmentPayload)
                ),
                RfcxComm.getProjection("guardian", "segment_receive_swm"),
                null, null, null
            )
            swmSegmentReceivedResponse?.close()
        }

        // Scheduling Tools
        @kotlin.jvm.JvmStatic
        fun addScheduledSwmToQueue(
            sendAtOrAfter: Long,
            msgPayload: String?,
            context: Context,
            triggerDispatchService: Boolean
        ): Boolean {
            val isQueued = false
            if (msgPayload != null && !msgPayload.equals("", ignoreCase = true)) {
                val app = context.applicationContext as RfcxGuardian
                val msgId = DeviceSmsUtils.generateMessageId()
                app.swmMessageDb.dbSwmQueued.insert(sendAtOrAfter, "", msgPayload, msgId)
                if (triggerDispatchService) {
                    app.rfcxSvc.triggerService(SwmDispatchService.SERVICE_NAME, false)
                }
            }
            return isQueued
        }

        fun addImmediateSwmToQueue(msgPayload: String?, context: Context): Boolean {
            return addScheduledSwmToQueue(System.currentTimeMillis(), msgPayload, context, true)
        }

        @kotlin.jvm.JvmStatic
        fun getSwmMetaValuesAsJsonArray(context: Context): JSONArray {
            val app = context.applicationContext as RfcxGuardian
            val metaJsonArray = JSONArray()
            try {
                val metaJson = JSONObject()
                metaJson.put("diagnostic", app.swmMetaDb.dbSwmDiagnostic.concatRows)
                metaJsonArray.put(metaJson)
            } catch (e: Exception) {
                RfcxLog.logExc(logTag, e)
            }
            return metaJsonArray
        }

        @kotlin.jvm.JvmStatic
        fun deleteSwmMetaValuesBeforeTimestamp(timeStamp: String, context: Context): Int {
            val app = context.applicationContext as RfcxGuardian
            val clearBefore = Date(timeStamp.toLong())
            app.swmMetaDb.dbSwmDiagnostic.clearRowsBefore(clearBefore)
            return 1
        }
    }
}