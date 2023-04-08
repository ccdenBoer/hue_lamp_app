package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.github.skydoves.colorpicker.compose.*
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
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "connectionList") {
        composable("connectionList") {
            HueLampConnections(
                connections = listOf(
                    "lamp 1",
                    "lamp 2",
                    "lamp 3"
                ),
                onConnectionClick = {
                    navController.navigate("settings/$it")
                }
            )
        }
        composable(
            "settings/{connectionName}",
            arguments = listOf(
                navArgument("connectionName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val connectionName =
                backStackEntry.arguments?.getString("connectionName") ?: ""
            SettingsScreen(connectionName)
        }
    }
}

@Composable
fun HueLampConnections(
    connections: List<String>,
    onConnectionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        connections.forEach { connection ->
            ConnectionItem(name = connection, onClick = {
                onConnectionClick(connection)
            })
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
fun SettingsScreen(connectionName: String) {
    var selectedColor by remember { mutableStateOf(Color.White) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Settings for $connectionName",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(16.dp)
        )
        DetailInfoTextField(
            modelId = "123456",
            uniqueId = "ABCDEF",
            swversion = "1.0.0",
            modifier = Modifier.padding(16.dp)
        )
        PowerButton(
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Brightness",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp)
        )
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Text(
            text = "Color",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp)
        )
        ColorPicker(onColorSelected = { color ->
            selectedColor = color
        })
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(16.dp)
                .background(selectedColor)
        )
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
    modifier: Modifier = Modifier
) {
    var isOn by remember { mutableStateOf(false) }

    Button(
        onClick = { isOn = !isOn },
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
    modifier: Modifier = Modifier
) {
    var brightness by remember { mutableStateOf(0f) }

    Slider(
        value = brightness,
        onValueChange = { brightness = it },
        modifier = modifier
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