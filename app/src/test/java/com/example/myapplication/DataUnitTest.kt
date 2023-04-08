package com.example.myapplication

import com.example.myapplication.data.Light
import com.example.myapplication.data.State
import com.example.myapplication.data.lightsJSONToData
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DataUnitTest {

    fun makeLights(): Map<String, Light> {
        return mapOf(
            "1" to Light(
                modelid = "LCT001",
                name = "Hue Lamp 1",
                swversion = "65003148",
                state = State(
                    xy = listOf(0.0, 0.0),
                    ct = 0,
                    alert = "none",
                    bri = 200,
                    effect = "none",
                    sat = 200,
                    hue = 10000,
                    colormode = "hs",
                    reachable = true,
                    on = true
                ),
                type = "Extended color light",
                pointsymbol = mapOf(
                    "1" to "none",
                    "2" to "none",
                    "3" to "none",
                    "4" to "none",
                    "5" to "none",
                    "6" to "none",
                    "7" to "none",
                    "8" to "none",),
                uniqueid = "00:17:88:01:00:d4:12:08-0a"),
            "2" to Light(
                modelid = "LCT001",
                name = "Hue Lamp 2",
                swversion = "65003148",
                state = State(
                    xy = listOf(0.0, 0.0),
                    ct = 201,
                    alert = "none",
                    bri = 254,
                    effect = "none",
                    sat = 144,
                    hue = 23536,
                    colormode = "hs",
                    reachable = true,
                    on = true
                ),
                type = "Extended color light",
                pointsymbol = mapOf(
                    "1" to "none",
                    "2" to "none",
                    "3" to "none",
                    "4" to "none",
                    "5" to "none",
                    "6" to "none",
                    "7" to "none",
                    "8" to "none",),
                uniqueid = "00:17:88:01:00:d4:12:08-0b"),
            "3" to Light(
                modelid = "LCT001",
                name = "Hue Lamp 3",
                swversion = "65003148",
                state = State(
                    xy = listOf(0.346, 0.3568),
                    ct = 201,
                    alert = "none",
                    bri = 254,
                    effect = "none",
                    sat = 254,
                    hue = 65136,
                    colormode = "hs",
                    reachable = true,
                    on = true
                ),
                type = "Extended color light",
                pointsymbol = mapOf(
                    "1" to "none",
                    "2" to "none",
                    "3" to "none",
                    "4" to "none",
                    "5" to "none",
                    "6" to "none",
                    "7" to "none",
                    "8" to "none",),
                uniqueid = "00:17:88:01:00:d4:12:08-0c")
        )
    }

    @Test
    fun Happy_JsonConvertion() {
        val json = "{\"1\":{\"modelid\":\"LCT001\",\"name\":\"Hue Lamp 1\",\"swversion\":\"65003148\",\"state\":{\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"sat\":200,\"effect\":\"none\",\"bri\":200,\"hue\":10000,\"colormode\":\"hs\",\"reachable\":true,\"on\":true},\"type\":\"Extended color light\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"},\"uniqueid\":\"00:17:88:01:00:d4:12:08-0a\"},\"2\":{\"modelid\":\"LCT001\",\"name\":\"Hue Lamp 2\",\"swversion\":\"65003148\",\"state\":{\"xy\":[0.346,0.3568],\"ct\":201,\"alert\":\"none\",\"sat\":144,\"effect\":\"none\",\"bri\":254,\"hue\":23536,\"colormode\":\"hs\",\"reachable\":true,\"on\":true},\"type\":\"Extended color light\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"},\"uniqueid\":\"00:17:88:01:00:d4:12:08-0b\"},\"3\":{\"modelid\":\"LCT001\",\"name\":\"Hue Lamp 3\",\"swversion\":\"65003148\",\"state\":{\"xy\":[0.346,0.3568],\"ct\":201,\"alert\":\"none\",\"sat\":254,\"effect\":\"none\",\"bri\":254,\"hue\":65136,\"colormode\":\"hs\",\"reachable\":true,\"on\":true},\"type\":\"Extended color light\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"},\"uniqueid\":\"00:17:88:01:00:d4:12:08-0c\"}}"
        val result = lightsJSONToData(json)
        val expected = makeLights()
        assert(result.size == expected.size)
        assert(result.containsKey("1"))
        assert(result.containsKey("2"))
        assert(result.containsKey("3"))
        assert(result["1"]!!.name == expected["1"]!!.name)
        assert(result["1"]!!.state!!.on == expected["1"]!!.state!!.on)
        assert(result["1"]!!.state!!.bri == expected["1"]!!.state!!.bri)
        assert(result["1"]!!.state!!.hue == expected["1"]!!.state!!.hue)
        assert(result["1"]!!.state!!.sat == expected["1"]!!.state!!.sat)

    }

    @Test
    fun Unhappy_JsonConvertion(){
        var badList = lightsJSONToData("\"1\":{\"badvar\":\"badval\"}")
        assert(badList.isEmpty())
        badList = lightsJSONToData("\"1\":{\"modelid\":\"badval\"}")
        assert(badList.isEmpty())
    }

    @Test
    fun Happy_setIP(){
        HueCommunication.setIP("192.168.0.1")
        assert(HueCommunication.ip == "192.168.0.1/api/")
    }

    @Test
    fun Unhappy_setIP(){
        HueCommunication.setIP("invalid")
        assert(HueCommunication.ip == "10.0.2.2:80/api/")
    }

    @Test
    fun Happy_setEmIP(){
        HueCommunication.setEmIP(80, true)
        assert(HueCommunication.ip == "10.0.2.2:80/api/")
        HueCommunication.setEmIP(80, false)
        assert(HueCommunication.ip == "localhost:80/api/")
    }

    @Test
    fun Unhappy_setEmIP(){
        HueCommunication.setEmIP(100000, true)
        assert(HueCommunication.ip == "10.0.2.2:80/api/")
        HueCommunication.setEmIP(-1, false)
        assert(HueCommunication.ip == "10.0.2.2:80/api/")
    }

    @Test
    fun Happy_selectLight(){
        HueCommunication.lights = makeLights()
        HueCommunication.selectLight("1")
        assert(HueCommunication.selectedLight == "1")
    }

    @Test
    fun Unhappy_selectLight(){
        HueCommunication.lights = makeLights()
        HueCommunication.selectLight("4")
        assert(HueCommunication.selectedLight.isEmpty())
    }
}