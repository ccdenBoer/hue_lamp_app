package com.example.myapplication.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

fun colorToHue(color: Color): Triple<Int, Short, Short> {
    val hsb = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsb)
    val hueInt = (hsb[0] / 360 * 65535).toInt() // Convert hue to 16-bit integer
    val saturationShort = (hsb[1] * 255).toInt().toShort() // Convert saturation to 8-bit integer
    val brightnessShort = (hsb[2] * 255).toInt().toShort() // Convert brightness to 8-bit integer
    return Triple(hueInt, saturationShort, brightnessShort)
}

fun lightsJSONToData(json: String): Map<String, Light>{
    val mapper = ObjectMapper()

    return mapper.readValue(
        json,
        object : TypeReference<Map<String, Light>>() {})

}