package com.example.myapplication

import android.content.Context
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.SystemClock.sleep
import android.renderscript.Script.InvokeID
import android.text.format.Formatter
import android.util.Log
import android.util.MutableBoolean
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.myapplication.HueCommunication.Companion.selectedLight
import com.example.myapplication.data.Light
import com.example.myapplication.data.lightsJSONToData
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class HueCommunication {
    companion object {

        val TAG = "HueCommunication"
        var bridge = ""
        var ip = "10.0.2.2/api/"
        var selectedLight = ""
        var lights: Map<String, Light> = emptyMap()
        var connectionFailed = false

        fun makeBridgeConnection(username: String, callback: (() -> Unit)? = null) {
            val message = "{\"devicetype\":\"hue_lamp_app#c$username\"}"
            val method = "GET"
            makeBridgeRequest(message, method, callback)
            Log.d(TAG, "Making bridge")
        }
        fun setIP(newIP: String) {
            ip = "$newIP/api/"
        }

/*        fun setIP(context: Context) {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            setIP(Formatter.formatIpAddress(wm.connectionInfo.ipAddress))
        }*/

        fun setEmIP(port: Int, emulator: Boolean) {
            if (emulator) {
                ip = "10.0.2.2:$port/api/"
            } else {
                ip = "localhost:$port/api/"
            }

        }

        fun selectLight(selectedLight: String) {
            this.selectedLight = selectedLight
        }

        fun requestLights(callback: (() -> Unit)? = null) {
            Log.d(TAG, "Requesting lights")
            makeRequest("/lights", "GET", "", "request lights", callback)
        }

        fun turnLightOff() {
            Log.d(TAG, "turning light off")
            makeRequest("/lights/$selectedLight/state", "PUT", "{\"on\":false}", "light off")
        }

        fun turnLightOn() {
            Log.d(TAG, "turning light on")
            makeRequest("/lights/$selectedLight/state", "PUT", "{\"on\":true}", "light on")
        }

        fun setLightStatus(on: Boolean, saturation: Int, brightness: Int, hue: Int) {
            Log.d(TAG, "setting light status")
            if (on) {
                makeRequest(
                    "/lights/$selectedLight/state",
                    "PUT",
                    "{\"on\":true, \"sat\":$saturation, \"bri\":$brightness, \"hue\":$hue}",
                    "light status"
                )
            } else {
                makeRequest(
                    "/lights/$selectedLight/state",
                    "PUT",
                    "{\"on\":false, \"sat\":$saturation, \"bri\":$brightness, \"hue\":$hue}",
                    "light status"
                )
            }
        }

        fun setLightStatus(light: Light) {
            Log.d(TAG, "setting light status")
            val objectMapper = ObjectMapper()
            setLightStatus(
                light.state?.on!!,
                light.state!!.sat!!,
                light.state!!.bri!!,
                light.state!!.hue!!
            )
        }

        private fun makeBridgeRequest(
            message: String,
            method: String,
            callback: (() -> Unit)? = null
        ) {
            val networkThread = object : Thread() {
                override fun run() {
                    var data = ""
                    Log.d(TAG, "Staring new request")
                    try {
                        val urlString = "http://$ip"
                        Log.d(TAG, urlString)
                        val url = URL(urlString)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = method
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.doOutput = true
                        val payload = message
                        val writer = OutputStreamWriter(connection.outputStream)
                        writer.write(payload)
                        writer.flush()
                        writer.close()

                        val responseCode = connection.responseCode
                        data = connection.inputStream.bufferedReader().readText()
                        Log.d(TAG, "Response: $data")
                        SystemClock.sleep(100)
                    } catch (error: Exception) {
                        error.message?.let { Log.e(TAG, it) }
                    }

                    if (data != null) {
                        if(data.contains("error")) {
                            Log.d(TAG, "connection failed")
                            // Dismiss the loading screen


                            // Show an error message to the user (optional)

                            return
                        }
                            
                        if (data.contains("username")) {

                            val arr = JSONArray(data)
                            val jObj: JSONObject = arr.getJSONObject(0)
                            bridge = jObj.getJSONObject("success").getString("username")
                            Log.d(TAG, bridge)

                        }
                    }
                    if (callback != null) {
                        callback()
                    }
                }
            }

            networkThread.start()
        }

        private fun makeRequest(
            command: String,
            method: String,
            body: String,
            tag: String,
            callback: (() -> Unit)? = null
        ) {
            if (bridge.isNotEmpty()) {
                val networkThread = object : Thread() {
                    override fun run() {
                        val urlString = "http://$ip$bridge$command"
                        val url = URL(urlString)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = method

                        if (body.isNotEmpty()) {
                            val writer = OutputStreamWriter(connection.outputStream)
                            writer.write(body)
                            writer.flush()
                            writer.close()
                        }

                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val inputStream = connection.inputStream
                            val response = StringBuffer()
                            inputStream.bufferedReader().useLines { lines ->
                                lines.forEach {
                                    response.append(it)
                                }
                            }
                            inputStream.close()
                            Log.d("$TAG : $tag", response.toString())
                            when (tag) {
                                "request lights" -> {
                                    lights = lightsJSONToData(response.toString())
                                    selectLight(lights.keys.first())
                                }
                            }

                        } else {
                            println("GET request failed with response code $responseCode")
                        }
                        if (callback != null) {
                            callback()
                        }
                    }
                }

                networkThread.start()
            } else {
                Log.d(TAG, "No bridge yet!")
            }

        }
        fun disconnect() {
            Log.d(TAG, "Disconnecting from Hue bridge")
            bridge = ""
            ip = "10.0.2.2/api/"
            selectedLight = ""
            lights = emptyMap()

        }
    }
}


