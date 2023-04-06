package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Settings for $connectionName",
            style = MaterialTheme.typography.h5
        )
        DetailInfoTextField()
        PowerSwitch()
        BrightnessSlider()
    }
}

@Composable
fun DetailInfoTextField() {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        value = "",
        onValueChange = { }
    )
}

@Composable
fun PowerSwitch() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Power:")
        Button(
            onClick = { /* Handle power switch click */ },
            modifier = Modifier.padding(start = 8.dp),
            content = {

            }
        )
    }
}

@Composable
fun BrightnessSlider() {
    Slider(
        value = 0f,
        onValueChange = { /* Handle brightness change */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}



@Preview
@Composable
fun SettingsScreenPreview() {
    Surface {
        SettingsScreen(connectionName = "lamp 1")
    }
}