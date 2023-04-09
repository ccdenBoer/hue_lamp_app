package com.example.myapplication

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.BridgeSaver
import com.example.myapplication.data.BridgeSaver.Companion.saveBridge
import com.example.myapplication.data.Light
import com.example.myapplication.data.colorToHue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.Thread.sleep

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DataNetwerkInstrumentedTest {
    val standardBridge = "f8904985e83e4bb47c3b79005b2c246"
    lateinit var instrumentationContext: Context

    @Before
    fun before() {
        instrumentationContext = ApplicationProvider.getApplicationContext<Context>()
        HueCommunication.bridge = ""
        HueCommunication.ip = "10.0.2.2:80/api/"
        HueCommunication.selectedLight = ""
        HueCommunication.lights = emptyMap()
        HueCommunication.testFinished = false
        val resDir = instrumentationContext.getDir("HueLight", Context.MODE_PRIVATE)
        File(resDir, "testfile.txt").delete()
        sleep(100)
    }


    @Test
    fun Happy_ColorConvertion() {
        val color = Color(0xFF4B0082)
        val result = colorToHue(color)
        val expected = Triple(49991, 254.toShort(), 64.toShort())
        assert(result.first == expected.first) { "Expected first value ${expected.first}, but got ${result.first} instead." }
        assert(result.second == expected.second) { "Expected second value ${expected.second}, but got ${result.second} instead." }
        assert(result.third == expected.third) { "Expected third value ${expected.third}, but got ${result.third} instead." }
    }

    @Test
    fun Unhappy_ColorConvertion() {
        val color = Color(0xFFFFFFFFFFFFFF)
        val result = colorToHue(color)
    }


    @Test
    fun Happy_makeBridgeConnection() {
        //link button needs to be pressed
        HueCommunication.setEmIP(80, true)
        HueCommunication.makeBridgeConnection("testUser") {
            assert(HueCommunication.bridge.isNotEmpty())
        }
        while (!HueCommunication.testFinished)
            sleep(100)
    }

    @Test
    fun Unhappy_makeBridgeConnection() {
        //wrong ip
        HueCommunication.setIP("192.1.1.1")
        HueCommunication.makeBridgeConnection("testUser") {
            assert(HueCommunication.bridge.isEmpty())
        }
        while (!HueCommunication.testFinished)
            sleep(100)
    }

    @Test
    fun Happy_requestLights() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = standardBridge
        HueCommunication.requestLights() {
            assert(HueCommunication.lights.isNotEmpty())
        }
        while (!HueCommunication.testFinished)
            sleep(100)
    }

    @Test
    fun Unhappy_requestLights() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = ""
        HueCommunication.requestLights() {
            assert(HueCommunication.lights.isEmpty())
        }
        while (!HueCommunication.testFinished)
            sleep(100)
    }

    @Test
    fun Happy_turnLightOff() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = standardBridge
        HueCommunication.requestLights()
        while (HueCommunication.lights.isEmpty())
            sleep(100)
        HueCommunication.selectLight("1")
        HueCommunication.lights["1"]!!.state!!.on = true
        HueCommunication.turnLightOff() {
            assert(HueCommunication.lights["1"]!!.state!!.on == false)
        }
        while (!HueCommunication.testFinished)
            sleep(100)
    }

    @Test
    fun Unhappy_turnLightOff() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = standardBridge
        HueCommunication.turnLightOff()
        assert(!HueCommunication.testFinished)
    }

    @Test
    fun Happy_turnLightOn() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = standardBridge
        HueCommunication.requestLights()
        while (HueCommunication.lights.isEmpty())
            sleep(100)
        HueCommunication.testFinished = false
        HueCommunication.selectLight("1")
        HueCommunication.lights["1"]!!.state!!.on = false
        HueCommunication.turnLightOn() {
            assert(HueCommunication.lights["1"]!!.state!!.on == true)
        }
        while (!HueCommunication.testFinished)
            sleep(100)
    }

    @Test
    fun Unhappy_turnLightOn() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = standardBridge
        HueCommunication.turnLightOn()
        assert(!HueCommunication.testFinished)
    }

    @Test
    fun Happy_setLightStatus() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = standardBridge
        HueCommunication.requestLights()
        while (HueCommunication.lights.isEmpty())
            sleep(100)
        HueCommunication.testFinished = false
        HueCommunication.selectLight("1")
        HueCommunication.testFinished = false
        HueCommunication.lights["1"]!!.state!!.on = false
        HueCommunication.setLightStatus(false, 100, 100, 10000) {
            assert(HueCommunication.lights["1"]!!.state!!.on == false)
            assert(HueCommunication.lights["1"]!!.state!!.sat == 100)
            assert(HueCommunication.lights["1"]!!.state!!.bri == 100)
            assert(HueCommunication.lights["1"]!!.state!!.hue == 10000)
        }
        while (!HueCommunication.testFinished)
            sleep(100)
        HueCommunication.selectLight("2")
        HueCommunication.testFinished = false
        HueCommunication.lights["2"]!!.state!!.on = true
        HueCommunication.lights["2"]!!.state!!.bri = 200
        HueCommunication.lights["2"]!!.state!!.sat = 200
        HueCommunication.lights["2"]!!.state!!.hue = 20000
        HueCommunication.setLightStatus(HueCommunication.lights["2"]!!)
        while (!HueCommunication.testFinished)
            sleep(100)
        HueCommunication.testFinished = false
        HueCommunication.requestLights()
        while (!HueCommunication.testFinished)
            sleep(100)
        assert(HueCommunication.lights["1"]!!.state!!.on == false)
        assert(HueCommunication.lights["1"]!!.state!!.sat == 100) { "${HueCommunication.lights["1"]!!.state!!.sat} 100" }
        assert(HueCommunication.lights["1"]!!.state!!.bri == 100)
        assert(HueCommunication.lights["1"]!!.state!!.hue == 10000)

        assert(HueCommunication.lights["2"]!!.state!!.on == true)
        assert(HueCommunication.lights["2"]!!.state!!.sat == 200)
        assert(HueCommunication.lights["2"]!!.state!!.bri == 200)
        assert(HueCommunication.lights["2"]!!.state!!.hue == 20000)

    }

    @Test
    fun Unhappy_setLightStatus() {
        HueCommunication.setEmIP(80, true)
        HueCommunication.bridge = standardBridge
        HueCommunication.requestLights()
        while (HueCommunication.lights.isEmpty())
            sleep(100)
        HueCommunication.selectLight("1")
        HueCommunication.lights["1"]!!.state!!.on = false
        HueCommunication.setLightStatus(false, -100, -100, -10000) {
            assert(HueCommunication.lights["1"]!!.state!!.sat != -100)
            assert(HueCommunication.lights["1"]!!.state!!.bri != -100)
            assert(HueCommunication.lights["1"]!!.state!!.hue != -10000)
        }

    }

    @Test
    fun Happy_SaveLoadBridge() {
        val nobridge = BridgeSaver.loadBridge(instrumentationContext, "testfile.txt")
        assert(nobridge == "")
        saveBridge(instrumentationContext, standardBridge, "testfile.txt")
        val yesbridge = BridgeSaver.loadBridge(instrumentationContext, "testfile.txt")
        assert(yesbridge == standardBridge)
    }

    @Test
    fun Unhappy_SaveLoadBridge() {
        val mockContext = InstrumentationRegistry.getInstrumentation().context
        HueCommunication.bridge = standardBridge

        val nobridge = BridgeSaver.loadBridge(mockContext, "testfile.txt")
        assert(nobridge == "")
        saveBridge(mockContext, "", "testfile.txt")
        val yesbridge = BridgeSaver.loadBridge(mockContext, "testfile.txt")
        assert(yesbridge == "")
    }
}