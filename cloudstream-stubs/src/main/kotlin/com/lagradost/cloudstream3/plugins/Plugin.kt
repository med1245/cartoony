package com.lagradost.cloudstream3.plugins

import com.lagradost.cloudstream3.MainAPI

open class Plugin {
    open fun load() {}
    fun registerMainAPI(api: MainAPI) {}
}

