package com.lagradost.cloudstream3

import org.jsoup.nodes.Document

open class MainAPI {
    open var mainUrl: String = ""
    open var name: String = ""
    open val hasMainPage: Boolean = false
    open var lang: String = "en"
    open val hasDownloadSupport: Boolean = false
    open val supportedTypes: Set<TvType> = emptySet()
    open val mainPage: Any? = null

    open suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse =
        HomePageResponse(emptyList())

    open suspend fun search(query: String): List<SearchResponse> = emptyList()

    open suspend fun load(url: String): LoadResponse = LoadResponse("", "", TvType.Anime)

    open suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean = false
}

data class MainPageRequest(val data: String, val name: String)

enum class TvType { Anime, AnimeMovie }

class Qualities {
    class Quality(val value: Int)
    companion object {
        val Unknown = Quality(0)
    }
}

open class SearchResponse(
    var name: String,
    var url: String,
    var type: TvType
) {
    var posterUrl: String? = null
    var rating: Int? = null
}

fun newAnimeSearchResponse(
    name: String,
    url: String,
    type: TvType,
    builder: SearchResponse.() -> Unit = {}
): SearchResponse {
    val resp = SearchResponse(name, url, type)
    resp.builder()
    return resp
}

class HomePageList(
    val name: String,
    val list: List<SearchResponse>,
    val isHorizontalImages: Boolean
)

class HomePageResponse(val items: List<HomePageList>)

open class LoadResponse(
    var name: String,
    var url: String,
    var type: TvType
) {
    var posterUrl: String? = null
    var plot: String? = null
    var rating: Int? = null
    var episodes: Map<Int, List<Episode>>? = null
}

fun newAnimeLoadResponse(
    name: String,
    url: String,
    type: TvType,
    builder: LoadResponse.() -> Unit
): LoadResponse {
    val resp = LoadResponse(name, url, type)
    resp.builder()
    return resp
}

data class Episode(
    val data: String,
    val name: String?,
    val season: Int?,
    val episode: Int?
)

data class ExtractorLink(
    val source: String,
    val name: String,
    val url: String,
    val referer: String?,
    val quality: Int,
    val isM3u8: Boolean
)

data class SubtitleFile(
    val lang: String,
    val file: String
)
