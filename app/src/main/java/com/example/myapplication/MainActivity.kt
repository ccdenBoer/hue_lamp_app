package com.example.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock.sleep
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.philips.lighting.hue.sdk.*
import com.philips.lighting.hue.sdk.exception.*
import com.philips.lighting.hue.sdk.utilities.*
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueParsingError
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    var bridge = ""
    val ip = "10.0.2.2:100/api/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //lastAccessPoint.setIpAddress("10.0.2.2:800") // Enter the IP Address and Port your Emulator is running on here.
        //lastAccessPoint.setUsername("coen1") // newdeveloper is loaded by the emulator and set on the WhiteList.

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold { innerpadding ->
                        ConnectButton()
                        SetButton()
                        DataCard()
                    }
                }
            }
        }


        makeBridgeConnection()

        Log.d(TAG, "waiting for bridge")

        while (bridge.equals(""))
            sleep(10)

        makeBridgeConnection()

        Log.d(TAG, "go bridge")
        makeRequest(ip, "/lights/1/state", "PUT", "{\"on\":false}")

    }

    private fun makeBridgeConnection() {
        val username = "coen1" // Replace with your Hue bridge username
        val bridgeIp = "10.0.2.2:100/api" // Replace with your Hue bridge IP address
        val lightId = "1" // Replace with the ID of the light you want to control
        val message = "{\"devicetype\":\"my_hue_app#coen1\"}"
        val method = "GET"
        HueBridgeTask().execute(bridgeIp, username, lightId, message, method)
        Log.d(TAG, "Making bridge")

    }

    private fun makeRequest(adress: String, command: String, method: String, body: String){
        HueTask().execute(adress, command, method, body)
    }

    inner class HueBridgeTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            var data =  "error"
            Log.d(TAG, "Staring new request")
            while(data.contains("error")){
                val urlString = "http://" + params[0]
                Log.d(TAG, urlString)
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = params[4]
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                val payload = params[3]
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(payload)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                data = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Response: " + data)
                sleep(100)
            }
            val arr = JSONArray(data)
            val jObj: JSONObject = arr.getJSONObject(0)
            bridge = jObj.getJSONObject("success").getString("username")
            return data
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d(TAG, "onpostexecute")
            if (result != null) {
                Log.d(TAG, result)
            }

            if (result != null) {
                if(result.contains("username")){
                    val arr = JSONArray(result)
                    val jObj: JSONObject = arr.getJSONObject(0)
                    bridge = jObj.getJSONObject("success").getString("username")
                    Log.d(TAG, bridge)
                }
            }
        }
    }
    inner class HueTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            var data =  "error"
            Log.d(TAG, "starting new general request")
            while(data.contains("error")){
                val addres = params[0]
                val command = params[1]
                val method = params[2]
                val body = params[3]

                val urlString = "http://" + addres + bridge + command
                Log.d(TAG, urlString)
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = method
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                val payload = body
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(payload)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                data = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Response: " + data)
                sleep(100)
            }

            return data
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)



        }
    }
}

fun bridgeReceieved(bridge: String){

}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}

@Composable
fun ConnectButton(){
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = {
                //connect
            },
            modifier = Modifier
                .padding(top = 20.dp, start = 30.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )

        ) {
            Text(
                text = "Connect",
                color = MaterialTheme.colors.onPrimary
            )
        }

    }
}

@Composable
fun SetButton(){
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Button(
            onClick = {
                //send data
            },
            modifier = Modifier
                .padding(top = 20.dp, start = 30.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )

        ) {
            Text(
                text = "set",
                color = MaterialTheme.colors.onPrimary
            )
        }

    }
}

@Composable
fun DataCard(){
    LazyColumn {

    }

}

class PHListener : PHSDKListener {
    val TAG = "PHSDKListener"
    override fun onAccessPointsFound(accessPoints: List<PHAccessPoint>) {
        Log.d(TAG, "accespoints found")
        // Handle access points found
    }

    override fun onCacheUpdated(flags: List<Int>, bridge: PHBridge) {
        Log.d(TAG, "cache updated found")
        // Handle cache updated
    }

    override fun onBridgeConnected(bridge: PHBridge, username: String) {
        Log.d(TAG, "bridge connected found: $username")
        // Handle bridge connected
    }

    override fun onAuthenticationRequired(accessPoint: PHAccessPoint) {
        Log.d(TAG, "authentication required")
        // Handle authentication required
    }

    override fun onConnectionResumed(bridge: PHBridge) {
        Log.d(TAG, "connection resumed")
        // Handle connection resumed
    }

    override fun onConnectionLost(accessPoint: PHAccessPoint) {
        Log.d(TAG, "connection lost")
        // Handle connection lost
    }

    override fun onError(code: Int, message: String) {
        Log.e(TAG, "error: code: ${code} message: $message")
        // Handle error
    }

    override fun onParsingErrors(parsingErrors: List<PHHueParsingError>) {
        Log.e(TAG, "parsing errors ${parsingErrors.forEach({it.message})}")
        // Handle parsing errors
    }
}

