package com.example.myapplication

import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.Light
import com.example.myapplication.data.State
import com.example.myapplication.data.colorToHue
import com.example.myapplication.data.lightsJSONToData
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
/*    @Test
    fun Happy_JsonConvertion() {
        val json = "{\"1\":{\"modelid\":\"LCT001\",\"name\":\"Hue Lamp 1\",\"swversion\":\"65003148\",\"state\":{\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"sat\":200,\"effect\":\"none\",\"bri\":200,\"hue\":10000,\"colormode\":\"hs\",\"reachable\":true,\"on\":true},\"type\":\"Extended color light\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"},\"uniqueid\":\"00:17:88:01:00:d4:12:08-0a\"},\"2\":{\"modelid\":\"LCT001\",\"name\":\"Hue Lamp 2\",\"swversion\":\"65003148\",\"state\":{\"xy\":[0.346,0.3568],\"ct\":201,\"alert\":\"none\",\"sat\":144,\"effect\":\"none\",\"bri\":254,\"hue\":23536,\"colormode\":\"hs\",\"reachable\":true,\"on\":true},\"type\":\"Extended color light\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"},\"uniqueid\":\"00:17:88:01:00:d4:12:08-0b\"},\"3\":{\"modelid\":\"LCT001\",\"name\":\"Hue Lamp 3\",\"swversion\":\"65003148\",\"state\":{\"xy\":[0.346,0.3568],\"ct\":201,\"alert\":\"none\",\"sat\":254,\"effect\":\"none\",\"bri\":254,\"hue\":65136,\"colormode\":\"hs\",\"reachable\":true,\"on\":true},\"type\":\"Extended color light\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"},\"uniqueid\":\"00:17:88:01:00:d4:12:08-0c\"}}"
        val result = lightsJSONToData(json)
        val expected = mapOf(
            "1" to Light(
                modelid = "LCT001",
                name = "Hue Lamp 1",
                swversion = "65003148",
                state = State(
                    xy = listOf(0.0, 0.0),
                    ct = 0,
                    "none",
                    200,
                    "none",
                    200,
                    10000,
                    "hs",
                    true,
                    true
                ),
                "Extended color light",
                mapOf(
                    "1" to "none",
                    "2" to "none",
                    "3" to "none",
                    "4" to "

    }*/
    @Test
    fun Happy_ColorConvertion() {
        val color = Color(0x00FF00)
        val result = colorToHue(color)
        val expected = Triple(21845, 255.toShort(), 0.toShort())
        assert(result == expected)
    }
}