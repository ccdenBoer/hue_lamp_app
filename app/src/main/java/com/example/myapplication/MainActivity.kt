package com.example.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.Light
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.philips.lighting.hue.sdk.*
import com.philips.lighting.hue.sdk.exception.*
import com.philips.lighting.hue.sdk.utilities.*
import androidx.core.graphics.ColorUtils
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueParsingError
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    color = Color(0xFFFFE0B5),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    color = Color(0xFF462521)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hue Connect",
                                color = Color(0xFF8A6552),
                                style = MaterialTheme.typography.h5
                            )
                        }
                        MyApp()
                    }
                }
            }
        }

        //test()

    }


    fun test() {


        while (HueCommunication.bridge.equals(""))
            sleep(10)


        HueCommunication.requestLights()

        Log.d(TAG, "requesting light info")
        HueCommunication.requestLights()
        while (HueCommunication.lights.isEmpty())
            sleep(10)
        Log.d(TAG, "Lights: ${HueCommunication.lights.size}")
        HueCommunication.lights.forEach { light ->
            Log.d(TAG, "Name: ${light.value.name} cm: ${light.value.state?.hue}")
        }
        sleep(1000)
        Log.d(TAG, "setting status")
        HueCommunication.setLightStatus(true, 254, 254, 43690)
        sleep(1000)

        Log.d(TAG, "turning light on")
        HueCommunication.turnLightOn()
        sleep(1000)
        Log.d(TAG, "turning light off")
        HueCommunication.turnLightOff()
        sleep(1000)
        Log.d(TAG, "turning light on")
        HueCommunication.turnLightOn()
        sleep(1000)
        HueCommunication.lights.get("1")?.state?.hue = 21845
        HueCommunication.lights.get("1")?.let { HueCommunication.setLightStatus(it) }
    }
}


@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "connect") {
        composable("connect") {
            HueLampConnect(
                onClick = {
                    HueCommunication.makeBridgeConnection("newuser") {
                        HueCommunication.requestLights() {
                            val handler = Handler(Looper.getMainLooper())

                            handler.post {
                                if (HueCommunication.lights.isNotEmpty()) {
                                    navController.navigate("connectionList")
                                } else {
                                    Log.d(TAG, "No lights")
                                }
                            }
                        }


                    }
                }
            )
        }
        composable("connectionList") {
            HueLampConnections(
                connections = HueCommunication.lights,
                onConnectionClick = {
                    navController.navigate("settings/${it.name}")
                }
            )
        }
        composable(
            "settings/{connectionName}",
            arguments = listOf(navArgument("connectionName") { type = NavType.StringType })
        ) { backStackEntry ->
            val connectionName = backStackEntry.arguments?.getString("connectionName") ?: ""
            SettingsScreen(connectionName, onClick = {
                navController.navigateUp()
            })
        }
    }
}

@Composable
fun HueLampConnect(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF8A6552))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBDB246)),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                Text(
                    text = "Connect on ${HueCommunication.ip}",
                    style = MaterialTheme.typography.button.copy(color = Color(0xFF462521))
                )
            }
        }
    }
}

@Composable
fun HueLampConnections(
    connections: Map<String, Light>,
    onConnectionClick: (Light) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        connections.forEach { connection ->
            Log.d(TAG, "Found 1 connection: ${connection.value.name}")
            connection.value.name?.let {
                ConnectionItem(name = it, onClick = {
                    HueCommunication.selectLight(connection.key)
                    onConnectionClick(connection.value)
                })
            }
        }
    }
}


@Composable
fun ConnectionItem(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF8A6552))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.h6.copy(color = Color(0xFF462521))
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBDB246)),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                Text(
                    text = "Connect",
                    style = MaterialTheme.typography.button.copy(color = Color(0xFF462521))
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    connectionName: String,
    onClick: () -> Unit,
) {
    var selectedColor by remember { mutableStateOf(Color.White) }
    var light = HueCommunication.lights[HueCommunication.selectedLight]
    var brightness by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Settings for $connectionName",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(4.dp)
        )
        DetailInfoTextField(
            modelId = light?.modelid!!,
            uniqueId = light?.uniqueid!!,
            swversion = light?.swversion!!,
            modifier = Modifier?.padding(4.dp)
        )
        Text(
            text = "Power",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(4.dp)
        )
        PowerButton(
            modifier = Modifier.padding(4.dp),
            light = light
        )

        Text(
            text = "Brightness",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(4.dp)
        )
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            light = light,
            brightness = brightness
        )
        Text(
            text = "Color",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(4.dp)
        )
        ColorPicker(
            onColorSelected = { color ->
                selectedColor = color
                val newColors = colorToHue(color)
                light.state!!.hue = newColors.first
                light.state!!.sat = newColors.second.toInt()
                light.state!!.bri = newColors.third.toInt()
                brightness = newColors.third/255f
            }
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp)
                .background(selectedColor)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(
                onClick = { HueCommunication.setLightStatus(light) },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBDB246)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Apply")
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFCA2E55)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Disconnect")
            }
        }
    }
}

@Composable
fun DetailInfoTextField(
    modelId: String,
    uniqueId: String,
    swversion: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row {
            Text(
                text = "model id = ",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = modelId,
                style = MaterialTheme.typography.body2
            )
        }
        Row {
            Text(
                text = "unique id = ",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = uniqueId,
                style = MaterialTheme.typography.body2
            )
        }
        Row {
            Text(
                text = "swversion = ",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = swversion,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
fun PowerButton(
    modifier: Modifier = Modifier,
    light: Light
) {
    var isOn by remember { mutableStateOf(light!!.state!!.on!!) }

    Button(
        onClick = {
            isOn = !isOn
            if (isOn) {
                light.state!!.on = true
                HueCommunication.turnLightOn()
            } else {
                light.state!!.on = false
                HueCommunication.turnLightOff()
            }
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isOn) Color(0xFFBDB246) else Color(0xFFCA2E55)
        ),
        modifier = modifier
    ) {
        Text(
            text = if (isOn) "On" else "Off",
            style = MaterialTheme.typography.button
        )
    }
}

@Composable
fun BrightnessSlider(
    modifier: Modifier = Modifier,
    light: Light,
    brightness: Float
) {


    Slider(
        value = brightness,
        onValueChange = {
            light.state!!.bri = brightness.toInt()
        },
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF462521),
            activeTrackColor = Color(0xFF8A6552)
        ),
        modifier = modifier,
        valueRange = 1f..254f
    )
}

@Composable
fun ColorPicker(
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFFFF0000), // red
        Color(0xFFFFA500), // orange
        Color(0xFFFFFF00), // yellow
        Color(0xFF008000), // green
        Color(0xFF0000FF), // blue
        Color(0xFF4B0082), // indigo
        Color(0xFF9400D3)  // violet
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors.size) { index ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors[index])
                    .clickable {
                        onColorSelected(colors[index])
                    }
            )
        }
    }
}



fun colorToHue(color: Color): Triple<Int, Short, Short> {
    val hsb = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsb)
    val hueInt = (hsb[0] / 360 * 65535).toInt() // Convert hue to 16-bit integer
    val saturationShort = (hsb[1] * 255).toInt().toShort() // Convert saturation to 8-bit integer
    val brightnessShort = (hsb[2] * 255).toInt().toShort() // Convert brightness to 8-bit integer
    return Triple(hueInt, saturationShort, brightnessShort)
}

