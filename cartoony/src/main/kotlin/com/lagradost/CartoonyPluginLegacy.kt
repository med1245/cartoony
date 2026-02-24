package com.lagradost

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.BasePlugin

@CloudstreamPlugin
class CartoonyPluginLegacy : BasePlugin() {
    override fun load() {
        registerMainAPI(CartoonyProvider())
    }
}

