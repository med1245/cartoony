package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cartoony Provider for CloudStream3
 * Scrapes anime from cartoony.net using API endpoints
 * 
 * Features:
 * - Homepage browsing with featured anime
 * - Search functionality
 * - Episode loading
 * - HLS video streaming (.m3u8)
 */
class CartoonyProvider : MainAPI() {
    override var mainUrl = "https://cartoony.net"
    override var name = "Cartoony"
    override val hasMainPage = true
    override var lang = "en"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Anime,
        TvType.AnimeMovie
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val homePageList = mutableListOf<SearchResponse>()
        
        try {
            val response = app.get("$mainUrl/api/shows").text
            val jsonArray = JSONArray(response)
            
            for (i in 0 until minOf(jsonArray.length(), 20)) {
                try {
                    val item = jsonArray.getJSONObject(i)
                    val title = item.optString("title", item.optString("name", "Unknown"))
                    val id = item.optString("id", item.optString("slug", ""))
                    val posterUrl = item.optString("image", item.optString("poster", ""))
                    val rating = item.optDouble("rating", 0.0).toInt()
                    
                    if (id.isEmpty()) continue
                    
                    val anime = newAnimeSearchResponse(
                        name = title,
                        url = "$mainUrl/watch/$id",
                        type = TvType.Anime
                    ) {
                        this.posterUrl = posterUrl
                        this.rating = rating
                    }
                    homePageList.add(anime)
                } catch (e: Exception) {
                    continue
                }
            }
        } catch (e: Exception) {
            logError(e)
        }

        val list = listOf(
            HomePageList(
                name = "Featured Anime",
                list = homePageList,
                isHorizontalImages = true
            )
        )

        return HomePageResponse(list)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val results = mutableListOf<SearchResponse>()
        
        try {
            val searchUrl = "$mainUrl/api/search?q=${query.replace(" ", "+")}"
            val response = app.get(searchUrl).text
            val jsonArray = JSONArray(response)
            
            for (i in 0 until jsonArray.length()) {
                try {
                    val item = jsonArray.getJSONObject(i)
                    val title = item.optString("title", item.optString("name", "Unknown"))
                    val id = item.optString("id", item.optString("slug", ""))
                    val posterUrl = item.optString("image", item.optString("poster", ""))
                    val rating = item.optDouble("rating", 0.0).toInt()
                    
                    if (id.isEmpty()) continue
                    
                    results.add(
                        newAnimeSearchResponse(
                            name = title,
                            url = "$mainUrl/watch/$id",
                            type = TvType.Anime
                        ) {
                            this.posterUrl = posterUrl
                            this.rating = rating
                        }
                    )
                } catch (e: Exception) {
                    logError(e)
                    continue
                }
            }
        } catch (e: Exception) {
            logError(e)
        }

        return results
    }

    override suspend fun load(url: String): LoadResponse {
        val showId = url.substringAfterLast("/").substringBefore("?")
        
        var title = "Unknown"
        var posterUrl = ""
        var description = ""
        var rating = 0
        var type = TvType.Anime
        val episodes = mutableListOf<Episode>()
        
        try {
            val showResponse = app.get("$mainUrl/api/shows/$showId").text
            val showJson = JSONObject(showResponse)
            
            title = showJson.optString("title", showJson.optString("name", "Unknown"))
            posterUrl = showJson.optString("image", showJson.optString("poster", ""))
            description = showJson.optString("description", showJson.optString("synopsis", ""))
            rating = showJson.optDouble("rating", 0.0).toInt()
            
            val typeStr = showJson.optString("type", "")
            type = if (typeStr.contains("Movie", ignoreCase = true)) {
                TvType.AnimeMovie
            } else {
                TvType.Anime
            }
            
            try {
                val episodesResponse = app.get("$mainUrl/api/shows/$showId/episodes").text
                val episodesJson = JSONArray(episodesResponse)
                
                for (i in 0 until episodesJson.length()) {
                    try {
                        val ep = episodesJson.getJSONObject(i)
                        val epId = ep.optString("id", "")
                        val epTitle = ep.optString("title", ep.optString("name", "Episode ${i + 1}"))
                        val epNumber = ep.optInt("episode_number", ep.optInt("number", i + 1))
                        
                        if (epId.isEmpty()) continue
                        
                        episodes.add(
                            Episode(
                                data = "$mainUrl/api/episodes/$epId/stream",
                                name = epTitle,
                                season = 1,
                                episode = epNumber
                            )
                        )
                    } catch (e: Exception) {
                        logError(e)
                        continue
                    }
                }
            } catch (e: Exception) {
                logError(e)
            }
        } catch (e: Exception) {
            logError(e)
        }

        return newAnimeLoadResponse(
            name = title,
            url = url,
            type = type
        ) {
            posterUrl = posterUrl
            this.plot = description
            this.rating = rating
            this.episodes = mapOf(1 to episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        var hasLinks = false
        
        try {
            val response = app.get(data).text
            val json = JSONObject(response)
            
            var m3u8Url = json.optString("url", "")
            if (m3u8Url.isEmpty()) m3u8Url = json.optString("stream", "")
            if (m3u8Url.isEmpty()) m3u8Url = json.optString("video", "")
            if (m3u8Url.isEmpty()) m3u8Url = json.optString("link", "")
            if (m3u8Url.isEmpty()) m3u8Url = json.optString("media", "")
            
            if (m3u8Url.isNotEmpty() && m3u8Url.contains(".m3u8", ignoreCase = true)) {
                callback(
                    ExtractorLink(
                        source = name,
                        name = name,
                        url = m3u8Url,
                        referer = mainUrl,
                        quality = Qualities.Unknown.value,
                        isM3u8 = true
                    )
                )
                hasLinks = true
            }
        } catch (e: Exception) {
            logError(e)
        }
        
        if (!hasLinks) {
            try {
                val document = app.get(data).document
                
                document.select("iframe").forEach { iframe ->
                    val iframeUrl = iframe.attr("src").ifEmpty { iframe.attr("data-src") }
                    if (iframeUrl.isNotEmpty()) {
                        try {
                            val iframeDoc = app.get(iframeUrl).document
                            val m3u8Link = extractM3u8Link(iframeDoc)
                            if (m3u8Link.isNotEmpty()) {
                                callback(
                                    ExtractorLink(
                                        source = name,
                                        name = name,
                                        url = m3u8Link,
                                        referer = data,
                                        quality = Qualities.Unknown.value,
                                        isM3u8 = true
                                    )
                                )
                                hasLinks = true
                            }
                        } catch (e: Exception) {
                            logError(e)
                        }
                    }
                }
                
                document.select("script").forEach { script ->
                    val scriptContent = script.html()
                    val m3u8Pattern = Regex("(https?://[^\"'\\s]+\\.m3u8[^\"'\\s]*)")
                    val matches = m3u8Pattern.findAll(scriptContent)
                    
                    matches.forEach { match ->
                        val m3u8Link = match.groupValues[1]
                        if (m3u8Link.isNotEmpty()) {
                            callback(
                                ExtractorLink(
                                    source = name,
                                    name = name,
                                    url = m3u8Link,
                                    referer = data,
                                    quality = Qualities.Unknown.value,
                                    isM3u8 = true
                                )
                            )
                            hasLinks = true
                        }
                    }
                }
                
                document.select("video source").forEach { source ->
                    val videoUrl = source.attr("src")
                    if (videoUrl.isNotEmpty() && videoUrl.contains(".m3u8", ignoreCase = true)) {
                        callback(
                            ExtractorLink(
                                source = name,
                                name = name,
                                url = videoUrl,
                                referer = data,
                                quality = Qualities.Unknown.value,
                                isM3u8 = true
                            )
                        )
                        hasLinks = true
                    }
                }
            } catch (e: Exception) {
                logError(e)
            }
        }

        return hasLinks
    }

    private fun extractM3u8Link(document: org.jsoup.nodes.Document): String {
        // Check script tags for m3u8 links
        document.select("script").forEach { script ->
            val scriptContent = script.html()
            val m3u8Pattern = Regex("(https?://[^\"'\\s]+\\.m3u8[^\"'\\s]*)")
            val match = m3u8Pattern.find(scriptContent)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        
        // Check video source elements
        val videoSource = document.selectFirst("video source")
        if (videoSource != null) {
            val src = videoSource.attr("src")
            if (src.contains(".m3u8", ignoreCase = true)) {
                return src
            }
        }

        // Check for direct links in data attributes
        val dataLink = document.selectFirst("[data-video], [data-src], [data-link]")?.attr("data-video")
            ?: document.selectFirst("[data-src]")?.attr("data-src")
            ?: document.selectFirst("[data-link]")?.attr("data-link")
            ?: ""

        if (dataLink.contains(".m3u8", ignoreCase = true)) {
            return dataLink
        }

        return ""
    }
}
