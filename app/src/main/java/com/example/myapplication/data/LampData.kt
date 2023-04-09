package com.example.myapplication.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

data class Light(
    var state: State? = State(),
    var swupdate: Swupdate? = Swupdate(),
    var type: String? = null,
    var name: String? = null,
    var modelid: String? = null,
    var manufacturername: String? = null,
    var productname: String? = null,
    var capabilities: Capabilities? = Capabilities(),
    var config: Config? = Config(),
    var uniqueid: String? = null,
    var swversion: String? = null,
    var pointsymbol: Map<String, String>? = null
)


data class State(
    var on: Boolean? = false,
    var bri: Int? = 0,
    var hue: Int? = 0,
    var sat: Int? = 0,
    var effect: String? = null,
    var xy: List<Double>? = emptyList(),
    var ct: Int? = 0,
    var alert: String? = null,
    var colormode: String? = null,
    var mode: String? = null,
    var reachable: Boolean? = false
)


data class Swupdate(
    var state: String? = null,
    var lastinstall: String? = null
)

data class Capabilities(
    var certified: Boolean? = false,
    var control: Control? = Control(),
    var streaming: Streaming? = Streaming()
)

data class Control(
    var mindimlevel: Int? = 0,
    var maxlumen: Int? = 0,
    var colorgamuttype: String? = null,
    var colorgamut: List<List<Double>>? = emptyList(),
    var ct: Ct? = Ct()
)

data class Ct(
    var min: Int? = null,
    var max: Int? = null
)

data class Streaming(
    var renderer: Boolean? = false,
    var proxy: Boolean? = false
)

data class Config(
    var archetype: String? = null,
    var function: String? = null,
    var direction: String? = null
)

