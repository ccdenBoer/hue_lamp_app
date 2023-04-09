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
        var ip = "10.0.2.2:80/api/"
        var selectedLight = ""
        var lights: Map<String, Light> = emptyMap()
        var testFinished = false

        fun makeBridgeConnection(username: String, callback: (() -> Unit)? = null) {
            if(bridge.isEmpty()) {
                val message = "{\"devicetype\":\"hue_lamp_app#c$username\"}"
                val method = "GET"
                makeBridgeRequest(message, method, callback)
                Log.d(TAG, "Making bridge")
            } else{
                Log.d(TAG, "Already have bridge $bridge")
                if (callback != null) {
                    callback()
                }
            }


        }

        fun setIP(newIP: String) {
            val regex = Regex("""^localhost:\d+|\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$""")
            if (regex.matches(newIP))
                ip = "$newIP/api/"
        }

        fun setEmIP(port: Int, emulator: Boolean) {
            if (port in 1..65535) {
                if (emulator) {
                    setIP("10.0.2.2:$port")
                } else {
                    setIP("localhost:$port")
                }
            }
        }

        fun selectLight(selectedLight: String) {
            if (lights.containsKey(selectedLight)) {
                this.selectedLight = selectedLight
            }
        }

        fun requestLights(callback: (() -> Unit)? = null) {
            Log.d(TAG, "Requesting lights")
            makeRequest("/lights", "GET", "", "request lights", callback)
        }

        fun turnLightOff(callback: (() -> Unit)? = null) {
            if (lights.isNotEmpty()) {
                Log.d(TAG, "turning light off")
                lights[selectedLight]!!.state!!.on = false
                makeRequest(
                    "/lights/$selectedLight/state",
                    "PUT",
                    "{\"on\":false}",
                    "light off",
                    callback
                )
            }
        }

        fun turnLightOn(callback: (() -> Unit)? = null) {
            Log.d(TAG, "turning light on")
            if(lights.isNotEmpty()){
                lights[selectedLight]!!.state!!.on = true
                makeRequest(
                    "/lights/$selectedLight/state",
                    "PUT",
                    "{\"on\":true}",
                    "light on",
                    callback
                )
            }

        }

        fun setLightStatus(
            on: Boolean,
            saturation: Int,
            brightness: Int,
            hue: Int,
            callback: (() -> Unit)? = null
        ) {
            Log.d(TAG, "setting light status sat: $saturation, bri: $brightness, hue: $hue")
            if(saturation !in 1..254 || brightness !in 1..254 || hue !in 0..65535){
                return
            }
            val off = lights[selectedLight]!!.state!!.on
            lights[selectedLight]!!.state!!.on = on
            lights[selectedLight]!!.state!!.bri = brightness
            lights[selectedLight]!!.state!!.sat = saturation
            lights[selectedLight]!!.state!!.hue = hue
            if(!off!!){
                turnLightOn(){
                    sleep(100)
                    if (on) {
                        makeRequest(
                            "/lights/$selectedLight/state",
                            "PUT",
                            "{\"on\":true, \"sat\":$saturation, \"bri\":$brightness, \"hue\":$hue}",
                            "light status",
                            callback = callback
                        )
                    } else {
                        makeRequest(
                            "/lights/$selectedLight/state",
                            "PUT",
                            "{\"on\":true, \"sat\":$saturation, \"bri\":$brightness, \"hue\":$hue}",
                            "light status",
                            callback = callback
                        )
                        turnLightOff()
                    }
                }
            } else {
                if (on) {
                    makeRequest(
                        "/lights/$selectedLight/state",
                        "PUT",
                        "{\"on\":true, \"sat\":$saturation, \"bri\":$brightness, \"hue\":$hue}",
                        "light status",
                        callback = callback
                    )
                } else {
                    makeRequest(
                        "/lights/$selectedLight/state",
                        "PUT",
                        "{\"on\":true, \"sat\":$saturation, \"bri\":$brightness, \"hue\":$hue}",
                        "light status",
                        callback = callback
                    )
                    turnLightOff()
                }
            }
        }

        fun setLightStatus(light: Light, callback: (() -> Unit)? = null) {
            Log.d(TAG, "setting light status")
            setLightStatus(
                light.state?.on!!,
                light.state!!.sat!!,
                light.state!!.bri!!,
                light.state!!.hue!!,
                callback
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
                        connection.connectTimeout = 5000 // milliseconds
                        connection.readTimeout = 5000 // milliseconds
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
                    } catch (error: Exception) {
                        error.message?.let { Log.e(TAG, it) }
                    }

                    if (data != null) {
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
                    testFinished = true
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
                        Log.d(
                            TAG,
                            "url: $urlString ip: $ip, bridge: $bridge, command: $command, method: $method, body: $body, tag: $tag"
                        )
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connectTimeout = 5000 // milliseconds
                        connection.readTimeout = 5000 // milliseconds
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
                            if(response.toString().contains("unauthorized user")){
                                bridge = ""
                                val message = "{\"devicetype\":\"hue_lamp_app#cnewuser\"}"
                                val method = "GET"
                                makeBridgeRequest(message, method, callback)
                                Log.d(TAG, "Making bridge")
                                return
                            }


                            when (tag) {
                                "request lights" -> {
                                    lights = lightsJSONToData(response.toString())
                                    selectLight(lights.keys.first())
                                }
                            }

                        } else {
                            println("request failed with response code $responseCode")
                        }
                        if (callback != null) {
                            callback()
                        }
                        testFinished = true
                    }
                }

                networkThread.start()
            } else {
                Log.d(TAG, "No bridge yet!")
                if (callback != null) {
                    callback()
                }
                testFinished = true
            }

        }
    }
}


