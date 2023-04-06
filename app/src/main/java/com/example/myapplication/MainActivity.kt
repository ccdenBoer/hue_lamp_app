package com.example.myapplication

import android.os.Bundle
import android.text.style.BackgroundColorSpan
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val lastAccessPoint = PHAccessPoint()
//        lastAccessPoint.setIpAddress("192.168.1.1:8000") // Enter the IP Address and Port your Emulator is running on here.
//
//        lastAccessPoint.setUsername("newdeveloper") // newdeveloper is loaded by the emulator and set on the WhiteList.
//
//        phHueSDK.connect(lastAccessPoint)

        setContent {
            MyApplicationTheme {
                Surface(color = Color(0xFFFFE0B5)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .background(Color(0xFF462521))
                        ) {
                            Text(
                                text = "Hue Connect",
                                color = Color(0xFF8A6552),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                )
                                    .align(Alignment.CenterStart)
                            )
                        }
                        ConnectionList(connections = listOf("lamp 1", "lamp 2", "lamp 3"))
                    }
                }
            }
        }
    }
}
@Composable
fun ConnectionList(connections: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        connections.forEach { connection ->
            ConnectionItem(name = connection, onClick = { /* Handle connection click */ })
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
        horizontalArrangement = Arrangement.Start
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

        }

    }
}

@Composable
fun DataCard(){
    LazyColumn {

    }

}