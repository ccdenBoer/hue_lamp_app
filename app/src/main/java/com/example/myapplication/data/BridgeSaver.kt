package com.example.myapplication.data

import android.content.Context
import android.util.Log
import com.philips.lighting.annotations.Bridge
import java.io.File
import java.nio.charset.Charset

class BridgeSaver {
    companion object {
        val TAG = "BirdgeSaver"
        fun saveBridge(context: Context, bridge: String) {
            Log.d("Lang.kt", "saving settings")

            val resDir = context?.getDir("HueLight", Context.MODE_PRIVATE)
            File(resDir, "bridge.txt").createNewFile()

            File(resDir, "bridge.txt").printWriter().use { out ->
                out.println(bridge)
            }
        }

        var loaded = false

        fun loadBridge(context: Context): String {
            Log.d("Lang.kt", "Loading settings")

            val resDir = context.getDir("HueLight", Context.MODE_PRIVATE)
            if (File(resDir, "bridge.txt").exists() && !loaded) {
                try {
                    File(resDir, "bridge.txt").reader(Charset.defaultCharset()).use { re ->
                        val lines = re.readLines()
                        loaded = true
                        return lines[0]
                    }
                } catch (e: Exception) {
                    e.message?.let { Log.e(TAG, it) }
                }

            }
            return ""
        }
    }
}