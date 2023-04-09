package com.example.myapplication.data

import android.content.Context
import android.util.Log
import com.philips.lighting.annotations.Bridge
import java.io.File
import java.nio.charset.Charset

class BridgeSaver {
    companion object {
        val TAG = "BirdgeSaver"
        fun saveBridge(context: Context, bridge: String, filename: String) {
            Log.d(TAG, "saving settings")

            try {
                val resDir = context?.getDir("HueLight", Context.MODE_PRIVATE)
                Log.d(TAG, resDir.absolutePath)
                File(resDir, filename).createNewFile()

                File(resDir, filename).printWriter().use { out ->
                    out.println(bridge)
                }
            } catch (error : Exception){
                Log.d(TAG, error.message.toString())
            }
        }

        var loaded = false

        fun loadBridge(context: Context,filename: String): String {
            Log.d(TAG, "Loading settings")

            val resDir = context.getDir("HueLight", Context.MODE_PRIVATE)
            if (File(resDir, filename).exists() && !loaded) {
                try {
                    File(resDir, filename).reader(Charset.defaultCharset()).use { re ->
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