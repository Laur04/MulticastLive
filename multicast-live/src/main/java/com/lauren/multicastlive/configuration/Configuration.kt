package com.lauren.multicastlive.configuration

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.lauren.multicastlive.R

class Configuration(context: Context) {
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val resources = context.resources
    val endpoint = Endpoint(sharedPref, resources)

    class Endpoint(private val sharedPref: SharedPreferences, private val resources: Resources) {
        val srt = SrtConnection(sharedPref, resources)

        class SrtConnection(
            private val sharedPref: SharedPreferences,
            private val resources: Resources
        ) {
            var ip: String = ""
                get() = sharedPref.getString(
                    resources.getString(R.string.srt_server_ip_key),
                    field
                )!!

            var port: Int = 9998
                get() = sharedPref.getString(
                    resources.getString(R.string.srt_server_port_key),
                    field.toString()
                )!!.toInt()
        }
    }
}