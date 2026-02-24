package com.lagradost.cloudstream3.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun mainPageOf(vararg pairs: Pair<String, String>): Any = pairs.toList()

fun logError(e: Throwable) { /* no-op in stubs */ }

object app {
    fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): Response {
        val empty = ""
        return Response(empty, Jsoup.parse(empty))
    }
}

class Response(val text: String, val document: Document)
