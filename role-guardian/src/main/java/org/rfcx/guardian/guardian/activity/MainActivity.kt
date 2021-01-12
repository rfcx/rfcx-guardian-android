package org.rfcx.guardian.guardian.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import org.rfcx.guardian.guardian.R
import org.rfcx.guardian.guardian.RfcxGuardian
import org.rfcx.guardian.guardian.api.methods.download.AssetDownloadJobService
import org.rfcx.guardian.guardian.api.methods.ping.SendApiPingService
import org.rfcx.guardian.guardian.api.methods.register.GuardianCheckApi
import org.rfcx.guardian.guardian.api.methods.register.GuardianCheckCallback
import org.rfcx.guardian.guardian.api.methods.register.RegisterApi
import org.rfcx.guardian.guardian.api.methods.register.RegisterCallback
import org.rfcx.guardian.guardian.entity.RegisterRequest
import org.rfcx.guardian.guardian.manager.PreferenceManager
import org.rfcx.guardian.guardian.manager.getTokenID
import org.rfcx.guardian.guardian.manager.getUserNickname
import org.rfcx.guardian.guardian.manager.isLoginExpired
import org.rfcx.guardian.guardian.utils.AudioSettingUtils
import org.rfcx.guardian.guardian.utils.GuardianUtils
import org.rfcx.guardian.guardian.view.*
import org.rfcx.guardian.utility.rfcx.RfcxLog
import org.rfcx.guardian.utility.rfcx.RfcxPrefs


class MainActivity : Activity(),
    RegisterCallback, GuardianCheckCallback {
    private var getInfoThread: Thread? = null
    private lateinit var app: RfcxGuardian

    //Audio settings
    private var sampleRate: String? = null
    private var fileFormat: String? = null
    private var bitRate: String? = null
    private var duration: String? = null

    override fun onResume() {
        super.onResume()

        setConfiguration()
        setVisibilityByPrefs()
        setUIByLoginState()
        setUIByGuidState()
        startServices()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_prefs -> startActivity(Intent(this, PrefsActivity::class.java))
        }
        return true
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        app = application as RfcxGuardian

        initUI()

        sendPingButton.setOnClickListener {
            sendPing()
        }

        clearRegistrationButton.setOnClickListener {
            clearRegistration()
            setVisibilityBeforeRegister()
            // reload or relaunch app
        }

        downloadDevClassifierButton.setOnClickListener {
            app.assetDownloadUtils.createDummyRow();
            app.rfcxServiceHandler.triggerService( AssetDownloadJobService.SERVICE_NAME, false );
        }

        audioCaptureButton.setOnClickListener {
            val isAudioCaptureOn = app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_AUDIO_CAPTURE)
            app.setSharedPref(RfcxPrefs.Pref.ENABLE_AUDIO_CAPTURE, (!isAudioCaptureOn).toString().toLowerCase())
            audioCaptureButton.text = if (!isAudioCaptureOn) "stop" else "record"
            setUIByRecordingState()
        }

        registerButton.setOnClickListener {
            if (!GuardianUtils.isNetworkAvailable(this)) {
                showToast("There is not internet connection. Please turn it on.")
                return@setOnClickListener
            }

            if (this.getTokenID() == null) {
                showToast("Please login before register guardian.")
                return@setOnClickListener
            }

            setVisibilityBeforeRegister()
            val guid = app.rfcxGuardianIdentity.guid

            RegisterApi.registerGuardian(applicationContext, RegisterRequest(guid), this)
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginWebViewActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            PreferenceManager.getInstance(this).clear()
            finish()
            startActivity(intent)
        }
    }

    private fun initUI() {
        setAppVersion()
    }

    private fun setAppVersion() {
        appVersionText.text = "version: ${app.version}"
    }

    private fun setConfiguration() {
        sampleRate = app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_CAPTURE_SAMPLE_RATE)
        fileFormat = app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_STREAM_CODEC)
        bitRate = app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_STREAM_BITRATE)
        duration = app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_CYCLE_DURATION)

        audioSettingButton.setOnClickListener {
            AudioSettingsDialog.build(this, object : OnAudioSettingsSet {
                override fun onSet(settings: AudioSettings) {
                    sampleRate = settings.sampleRate
                    bitRate = settings.bitRate
                    fileFormat = settings.fileFormat
                    app.setSharedPref(RfcxPrefs.Pref.AUDIO_CAPTURE_SAMPLE_RATE, sampleRate)
                    app.setSharedPref(RfcxPrefs.Pref.AUDIO_STREAM_BITRATE, bitRate)
                    app.setSharedPref(RfcxPrefs.Pref.AUDIO_STREAM_CODEC, fileFormat)
                    updateAudioSettingsInfo()
                }
            }).show()
        }

        durationButton.setOnClickListener {
            DurationPickerDialog.build(this, object : OnDurationSet {
                override fun onSet(seconds: Int) {
                    duration = seconds.toString()
                    app.setSharedPref(RfcxPrefs.Pref.AUDIO_CYCLE_DURATION, duration)
                    updateAudioSettingsInfo()
                }
            }).show()
        }
        updateAudioSettingsInfo()
    }

    private fun updateAudioSettingsInfo() {
        val audioSettingUtils = AudioSettingUtils(this)
        audioSettingsInfoText.text =
            "${audioSettingUtils.getSampleRateLabel(sampleRate!!)}, ${fileFormat}, ${audioSettingUtils.getBitRateLabel(
                bitRate!!
            )}"
        durationInfoText.text = "$duration seconds per file"
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun startServices() {
        Handler().postDelayed({
            app.initializeRoleServices()
            setUIByRecordingState()
        }, 1000)
    }

    private fun setVisibilityByPrefs() {
        if (app.rfcxPrefs.getPrefAsString("show_ui") == "true") {
            rootView.visibility = View.VISIBLE
        } else {
            rootView.visibility = View.INVISIBLE
        }
    }

    private fun setUIByRecordingState() {
        if (GuardianUtils.isGuardianRegistered(this)) {
            var deviceIdTxt = app.rfcxGuardianIdentity.guid
            deviceIdText.text = " $deviceIdTxt"
            if (app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_AUDIO_CAPTURE)) {
                recordStatusText.text = " recording"
                recordStatusText.setTextColor(resources.getColor(R.color.primary))
                audioCaptureButton.text = "stop"
            } else {
                recordStatusText.text = " stopped"
                recordStatusText.setTextColor(resources.getColor(R.color.grey_default))
                audioCaptureButton.text = "record"
            }
        }
    }

    private fun sendPing() {
        app.rfcxServiceHandler.triggerService( SendApiPingService.SERVICE_NAME, false);
    }

    private fun clearRegistration() {
        app.clearRegistration();
    }

    private fun setUIByLoginState() {
        if (this.isLoginExpired()) {
            loginInfo.visibility = View.INVISIBLE
            loginButton.visibility = View.VISIBLE
        } else {
            loginInfo.visibility = View.VISIBLE
            loginButton.visibility = View.GONE
            val userNameTxt = this.getUserNickname()
            userName.text = " $userNameTxt"
        }
    }

    private fun setUIByGuidState() {
        if (GuardianUtils.isGuardianRegistered(this)) {
            unregisteredView.visibility = View.INVISIBLE
            registeredView.visibility = View.VISIBLE
            registerProgress.visibility = View.INVISIBLE
            registerInfo.visibility = View.VISIBLE
            loginButton.visibility = View.GONE
            loginInfo.visibility = View.GONE
            val deviceIdTxt = app.rfcxGuardianIdentity.guid
            deviceIdText.text = " $deviceIdTxt"
        } else {
            unregisteredView.visibility = View.VISIBLE
            registeredView.visibility = View.INVISIBLE
            registerInfo.visibility = View.INVISIBLE
        }
    }

    private fun setVisibilityBeforeRegister() {
        registerButton.visibility = View.INVISIBLE
        registerProgress.visibility = View.VISIBLE
    }

    private fun setVisibilityRegisterSuccess() {
        registeredView.visibility = View.VISIBLE
        unregisteredView.visibility = View.INVISIBLE
    }

    private fun setVisibilityRegisterFail() {
        registerButton.visibility = View.VISIBLE
        registerProgress.visibility = View.INVISIBLE
    }

    override fun onRegisterSuccess(t: Throwable?, response: String?) {
        app.saveGuardianRegistration(response)
        Log.i(logTag, "onRegisterSuccess: Successfully Registered")
        showToast("Successfully Registered")
        GuardianCheckApi.exists(applicationContext, app.rfcxGuardianIdentity.guid, this)
    }

    override fun onRegisterFailed(t: Throwable?, message: String?) {
        setVisibilityRegisterFail()
        Log.e(logTag, "onRegisterFailed: $message")
        showToast(message ?: "Registration Failed. Please try again or contact us for support.")
    }

    override fun onGuardianCheckSuccess(t: Throwable?, response: String?) {
        setVisibilityRegisterSuccess()
        app.apiMqttUtils.initializeFailedCheckInThresholds()
        app.apiCheckInJsonUtils.clearPrePackageMetaData()
        app.initializeRoleServices()
        setUIByRecordingState()
        setUIByGuidState()
        val deviceIdTxt = app.rfcxGuardianIdentity.guid
        deviceIdText.text = " $deviceIdTxt"
        Log.i(logTag, "onGuardianCheckSuccess: Successfully Verified Registration")
        showToast("Successfully Verified Registration")
        app.apiPingUtils.sendPing(true, arrayOf<String>())
    }

    override fun onGuardianCheckFailed(t: Throwable?, message: String?) {
        setVisibilityRegisterFail()
        Log.e(logTag, "onGuardianCheckFailed: $message")
        showToast(
            message ?: "Failed to verify registration. Please try again or contact us for support."
        )
    }

    override fun onPause() {
        super.onPause()
        if (getInfoThread != null) {
            getInfoThread?.interrupt()
        }
    }

    companion object {
        private val logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, MainActivity::class.java)
    }

}
