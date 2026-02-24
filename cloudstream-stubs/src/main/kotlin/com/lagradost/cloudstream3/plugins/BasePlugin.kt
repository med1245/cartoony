package com.lagradost.cloudstream3.plugins

import com.lagradost.cloudstream3.MainAPI

open class BasePlugin {
    open fun load() {}
    fun registerMainAPI(api: MainAPI) {}
}

