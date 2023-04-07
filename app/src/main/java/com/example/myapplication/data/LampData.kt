package com.example.myapplication.data

class Light {
    var modelid: String? = null
    var name: String? = null
    var swversion: String? = null
    var state: LightState? = null
    var type: String? = null
    var pointsymbol: Map<String, String>? = null
    var uniqueid: String? = null

    constructor() // Add default constructor
}

data class LightState(
    var on: Boolean = false,
    var bri: Int = 0,
    var hue: Int = 0,
    var sat: Int = 0,
    var xy: List<Double> = emptyList(),
    var ct: Int = 0,
    var alert: String = "",
    var effect: String = "",
    var colormode: String = "",
    var reachable: Boolean = false
)