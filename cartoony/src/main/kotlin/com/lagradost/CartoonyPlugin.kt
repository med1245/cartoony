package com.lagradost

import com.lagradost.cloudstream3.plugins.Plugin

class CartoonyPlugin : Plugin() {
    override fun load() {
        registerMainAPI(CartoonyProvider())
    }
}

