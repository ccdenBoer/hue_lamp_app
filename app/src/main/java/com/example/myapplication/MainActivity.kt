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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.myapplication.data.BridgeSaver
import com.example.myapplication.data.colorToHue
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueParsingError
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    var hasBridge: MutableState<Boolean> = mutableStateOf(false)
    var hasLights: MutableState<Boolean> = mutableStateOf(false)
    var isLoading: MutableState<Boolean> = mutableStateOf(false)
    var standardIpAddress = "10.0.2.2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HueCommunication.bridge = BridgeSaver.loadBridge(this, "bridge.txt")
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
    }


    @Composable
    fun MyApp() {
        val navController = rememberNavController()

        NavHost(navController, startDestination = "connect") {
            composable("connect") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Hue Connect",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    var ipAddress by remember { mutableStateOf(standardIpAddress) }
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            HueCommunication.setIP(ipAddress)
                            isLoading.value = true
                            HueCommunication.makeBridgeConnection("newuser") {
                                val handler = Handler(Looper.getMainLooper())
                                if (HueCommunication.bridge.isNotEmpty()) {
                                    handler.post {
                                        hasBridge.value = true
                                    }
                                    HueCommunication.requestLights() {
                                        handler.post {
                                            if (HueCommunication.lights.isNotEmpty()) {
                                                hasLights.value = true
                                                navController.navigate("connectionList")
                                            } else {
                                                Log.d(TAG, "No lights")
                                            }
                                            isLoading.value = false
                                        }
                                    }
                                }
                            }

                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBDB246)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Connect")
                    }
                    if (isLoading.value) {
                        if (!hasBridge.value && !hasLights.value)
                            loadingScreen(message = "Creating bridge, make sure it's on linking mode ")
                        if (hasBridge.value && !hasLights.value)
                            loadingScreen(message = "Created bridge, searching for lights")
                        if (hasBridge.value && hasLights.value)
                            loadingScreen(message = "Created bridge, found lights")


                    }

//                HueLampConnect(
//                    onClick = {
//                        HueCommunication.makeBridgeConnection("newuser") {
//                            val handler = Handler(Looper.getMainLooper())
//                            if(HueCommunication.bridge.isNotEmpty()){
//                                handler.post{
//                                    hasBridge.value = true
//                                }
//                                HueCommunication.requestLights() {
//                                    handler.post {
//                                        if (HueCommunication.lights.isNotEmpty()) {
//                                            hasLights.value = true
//                                            navController.navigate("connectionList")
//                                        } else {
//                                            Log.d(TAG, "No lights")
//                                        }
//                                    }
//                                }
//                            }
//
//
//
//                        }
//                    }
//                )
                }
            }
            composable("connectionList") {
                val extendedLights = mutableMapOf<String, Light>()
                HueCommunication.lights.forEach { (key, value) ->
                    extendedLights[key] = value
                    extendedLights["$key-copy1"] = value
                    extendedLights["$key-copy2"] = value
                }
                HueLampConnections(
                    connections = extendedLights,
                    onConnectionClick = {
                        navController.navigate("settings/${it.name}")
                    },
                    onDisconnectClick = {
                        HueCommunication.disconnect()
                        navController.navigate("connect")
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
                    if (hasBridge.value && hasLights.value) {
                        Text(
                            text = "Made bridge with lights",
                            style = MaterialTheme.typography.h6.copy(color = Color(0xFF462521))
                        )
                    }
                    if (!hasBridge.value && !hasLights.value) {
                        Text(
                            text = "No bridge, press link button",
                            style = MaterialTheme.typography.h6.copy(color = Color(0xFF462521))
                        )
                    }
                    if (hasBridge.value && !hasLights.value) {
                        Text(
                            text = "Made bridge, searching for lights",
                            style = MaterialTheme.typography.h6.copy(color = Color(0xFF462521))
                        )
                    }

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
    }
    @Composable
    fun loadingScreen(message: String) {
        AlertDialog(
            onDismissRequest = { },
            buttons = {
                Button(
                    onClick = { isLoading.value = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFCA2E55)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancel")
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Loading...",
                        color = Color(0xFF462521),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = message,
                        color = Color(0xFF462521),
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            },
            text = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF462521),
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            backgroundColor = Color(0xFFFFE0B5),
            contentColor = Color.White
        )
    }


    @Composable
    fun HueLampConnections(
        connections: Map<String, Light>,
        onConnectionClick: (Light) -> Unit,
        onDisconnectClick: () -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    connections.forEach { connection ->
                        Log.d(TAG, "Found 1 connection: ${connection.value.name}")
                        connection.value.name?.let {
                            ConnectionItem(
                                name = it,
                                onClick = {
                                    HueCommunication.selectLight(connection.key)
                                    onConnectionClick(connection.value)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onDisconnectClick,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFCA2E55)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Disconnect")
                    }
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Settings for $connectionName",
                style = MaterialTheme.typography.h5,
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
                light = light
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
                    brightness.value = newColors.third / 255f
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

    var brightness: MutableState<Float> = mutableStateOf(0f)

    @Composable
    fun BrightnessSlider(
        modifier: Modifier = Modifier,
        light: Light
    ) {
        brightness.value = light.state!!.bri!!.toFloat()

        Slider(
            value = brightness.value,
            onValueChange = {
                brightness.value = it
                light.state!!.bri = brightness.value.toInt()
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
}



