package com.lauren.multicastlive.ui.settings

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import androidx.preference.*
import com.lauren.multicastlive.R
import io.github.thibaultbee.streampack.streamers.helpers.CameraStreamerConfigurationHelper

class SettingsFragment : PreferenceFragmentCompat() {
    private val serverIpPreference: EditTextPreference by lazy {
        this.findPreference(getString(R.string.srt_server_ip_key))!!
    }

    private val serverPortPreference: EditTextPreference by lazy {
        this.findPreference(getString(R.string.srt_server_port_key))!!
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        loadPreferences()
    }

    private fun loadEndpoint() {
        serverIpPreference.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        serverPortPreference.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.filters = arrayOf(InputFilter.LengthFilter(5))
        }
    }

    private fun loadPreferences() {
        loadEndpoint()
    }
}