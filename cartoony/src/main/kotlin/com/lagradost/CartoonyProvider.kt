package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

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
    override var lang = "ar"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Anime,
        TvType.AnimeMovie
    )

    private val apiHeaders by lazy {
        mapOf(
            "Accept" to "application/json, text/javascript, */*; q=0.01",
            "X-Requested-With" to "XMLHttpRequest",
            "Referer" to "$mainUrl/",
            "Origin" to mainUrl,
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept-Language" to "en-US,en;q=0.9"
        )
    }

    private suspend fun fetchJsonArray(urls: List<String>): JSONArray? {
        for (url in urls) {
            try {
                val text = app.get(url, headers = apiHeaders).text
                if (text.contains("Access denied", ignoreCase = true)) continue
                return JSONArray(text)
            } catch (_: Exception) {
            }
        }
        return null
    }

    private suspend fun fetchJsonObject(urls: List<String>): JSONObject? {
        for (url in urls) {
            try {
                val text = app.get(url, headers = apiHeaders).text
                if (text.contains("Access denied", ignoreCase = true)) continue
                return JSONObject(text)
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun parseCardsFromHtml(document: Document): List<SearchResponse> {
        val candidates = mutableListOf<org.jsoup.nodes.Element>()
        val containerSelectors = listOf(
            ".items .item a",
            ".grid .card a",
            "article a",
            ".post a",
            ".entry a",
            ".media a",
            ".list a",
            "a.card",
            ".item a",
            "a[href*=/watch/]"
        )
        for (sel in containerSelectors) {
            val found = document.select(sel)
            if (found.isNotEmpty()) {
                candidates.addAll(found)
            }
        }
        val cards = candidates.ifEmpty { document.select("a[href*=/watch/]") }
        val seen = hashSetOf<String>()
        return cards.mapNotNull { elementToSearchResponse(it, seen) }
    }

    private fun elementToSearchResponse(a: Element, seen: MutableSet<String>): SearchResponse? {
        val href = a.absUrl("href").ifEmpty { a.attr("href") }
        if (!href.contains("/watch/")) return null
        val id = href.substringAfter("/watch/").substringBefore("/")
        if (id.isBlank() || !seen.add(id)) return null
        val title = run {
            val direct = a.attr("title")
            if (direct.isNotBlank()) direct else
                a.selectFirst(".title, .name, .card-title, .entry-title")?.text().orEmpty()
                    .ifEmpty { a.text() }
                    .ifEmpty { "Unknown" }
        }
        val img = extractImageUrl(a).ifEmpty { extractImageUrl(a.parent()) }
        return newAnimeSearchResponse(
            name = title,
            url = "$mainUrl/watch/$id",
            type = TvType.Anime
        ) {
            this.posterUrl = img
        }
    }

    override val mainPage = mainPageOf(
        Pair("home", "الرئيسية")
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val homePageList = mutableListOf<SearchResponse>()
        
        try {
            val url = when (request.data) {
                "home" -> mainUrl
                else -> mainUrl
            }
            val doc = app.get(url, headers = apiHeaders).document
            homePageList.addAll(parseCardsFromHtml(doc).take(20))
            if (homePageList.isEmpty()) {
                val jsonArray = fetchJsonArray(
                    listOf(
                        "$mainUrl/api/tvshows",
                        "$mainUrl/api/shows"
                    )
                )
                if (jsonArray != null) {
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
                        } catch (_: Exception) {
                        }
                    }
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
            val htmlSearchUrls = listOf(
                "$mainUrl/search?q=${query.replace(" ", "+")}",
                "$mainUrl/?s=${query.replace(" ", "+")}"
            )
            for (u in htmlSearchUrls) {
                try {
                    val doc = app.get(u, headers = apiHeaders).document
                    val parsed = parseCardsFromHtml(doc)
                    if (parsed.isNotEmpty()) {
                        results.addAll(parsed)
                        break
                    }
                } catch (_: Exception) {
                }
            }
            if (results.isEmpty()) {
                val jsonArray = fetchJsonArray(
                    listOf(
                        "$mainUrl/api/search?q=${query.replace(" ", "+")}",
                        "$mainUrl/api/search/${query.replace(" ", "+")}"
                    )
                )
                if (jsonArray != null) {
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
                        }
                    }
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
            val showJson = fetchJsonObject(
                listOf(
                    "$mainUrl/api/tvshows/$showId",
                    "$mainUrl/api/shows/$showId"
                )
            )
            
            if (showJson != null) {
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
                    val episodesJson = fetchJsonArray(
                        listOf(
                            "$mainUrl/api/tvshows/$showId/episodes",
                            "$mainUrl/api/shows/$showId/episodes"
                        )
                    )
                    
                    if (episodesJson != null) {
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
                            }
                        }
                    }
                } catch (e: Exception) {
                    logError(e)
                }
            } else {
                try {
                    val doc = app.get(url, headers = apiHeaders).document
                    title = doc.selectFirst("h1, .title, .name")?.text().orEmpty().ifEmpty { title }
                    posterUrl = (doc.selectFirst(".poster img, .cover img, img[alt*=poster]")?.let {
                        it.absUrl("data-original")
                            .ifEmpty { it.absUrl("data-src") }
                            .ifEmpty { it.absUrl("srcset").substringBefore(" ").ifEmpty { it.absUrl("src") } }
                    } ?: run { extractImageUrl(doc.selectFirst(".poster, .cover") ?: doc.body()) }).orEmpty()
                    description = doc.selectFirst(".description, .plot, p")?.text().orEmpty().ifEmpty { description }
                    val eps = doc.select(
                        "a[href*=/episode/], a[data-episode], .episodes a, .episode a, li a[href*=/episode/]"
                    )
                    var epNum = 1
                    eps.forEach { a ->
                        val href = a.absUrl("href").ifEmpty { a.attr("href") }
                        val name = a.attr("title").ifEmpty { a.text() }.ifEmpty { "Episode $epNum" }
                        if (href.isNotBlank()) {
                            episodes.add(
                                Episode(
                                    data = href,
                                    name = name,
                                    season = 1,
                                    episode = epNum++
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    logError(e)
                }
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
        val episodeDoc = app.get(data, headers = apiHeaders).document
        val iframe = episodeDoc.selectFirst("iframe") ?: return false
        val rawSrc = iframe.absUrl("src").ifEmpty { iframe.attr("src") }.ifEmpty { iframe.attr("data-src") }
        val iframeUrl = when {
            rawSrc.startsWith("http", true) -> rawSrc
            rawSrc.startsWith("/") -> "$mainUrl$rawSrc"
            rawSrc.isNotEmpty() -> "$mainUrl/$rawSrc"
            else -> return false
        }
        val ua = apiHeaders["User-Agent"] ?: "Mozilla/5.0"
        val iframeResponse = app.get(iframeUrl, headers = mapOf("Referer" to data, "User-Agent" to ua))
        val html = iframeResponse.text
        val m3u8Regex = Regex("""https?:\/\/[^\s'"]+\.m3u8""", RegexOption.IGNORE_CASE)
        val match = m3u8Regex.find(html)
        if (match != null) {
            val streamUrl = match.value
            callback(
                ExtractorLink(
                    source = name,
                    name = "$name HLS",
                    url = streamUrl,
                    referer = iframeUrl,
                    quality = Qualities.Unknown.value,
                    isM3u8 = true
                )
            )
            return true
        }
        return false
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

    private fun extractImageUrl(node: org.jsoup.nodes.Element?): String {
        if (node == null) return ""
        val img = node.selectFirst("img")
        if (img != null) {
            val candidates = listOf(
                img.absUrl("data-original"),
                img.absUrl("data-src"),
                img.absUrl("srcset").substringBefore(" "),
                img.absUrl("src")
            ).firstOrNull { it.isNotBlank() }
            if (candidates != null) return candidates
        }
        // Background-image style
        val style = node.attr("style")
        if (style.contains("background-image")) {
            val regex = Regex("url\\(['\"]?([^'\")]+)['\"]?\\)")
            val m = regex.find(style)
            if (m != null) return m.groupValues[1]
        }
        return ""
    }
}
