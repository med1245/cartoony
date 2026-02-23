# CloudStream API Reference for Cartoony Provider

## MainAPI Structure

The Cartoony provider extends `MainAPI` which provides:

### Required Properties

```kotlin
override var mainUrl = "https://cartoony.net"
override var name = "Cartoony"
override var lang = "en"
override val hasMainPage = true
override val hasDownloadSupport = true
override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)
```

### Required Methods

#### 1. getMainPage()
**Purpose**: Load featured/popular anime from homepage

**Called**: When user opens the provider in CloudStream3

**Returns**: `HomePageResponse` containing categorized anime lists

```kotlin
override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
    // Fetch main page HTML
    val document = app.get(mainUrl).document
    
    // Extract anime items
    val animeList = mutableListOf<SearchResponse>()
    // ... populate list
    
    // Return categorized results
    return HomePageResponse(listOf(
        HomePageList(
            name = "Featured Anime",
            list = animeList,
            isHorizontalImages = true
        )
    ))
}
```

#### 2. search()
**Purpose**: Search for anime by query

**Called**: When user searches in CloudStream3

**Returns**: `List<SearchResponse>` of matching anime

```kotlin
override suspend fun search(query: String): List<SearchResponse> {
    val document = app.get("$mainUrl/search?q=$query").document
    
    val results = mutableListOf<SearchResponse>()
    // Extract and populate results
    
    return results
}
```

#### 3. load()
**Purpose**: Load full anime details and episode list

**Called**: When user clicks on an anime

**Returns**: `LoadResponse` with all anime data

```kotlin
override suspend fun load(url: String): LoadResponse {
    val document = app.get(url).document
    
    // Extract anime details
    val title = document.selectFirst("h1")?.text() ?: "Unknown"
    val description = document.selectFirst(".description")?.text() ?: ""
    val episodes = mutableListOf<Episode>()
    
    // Extract episodes
    document.select("a.episode").forEach { ep ->
        episodes.add(Episode(
            data = ep.attr("href"),
            name = ep.text(),
            episode = episodes.size + 1
        ))
    }
    
    return newAnimeLoadResponse(title, url, TvType.Anime) {
        this.plot = description
        this.episodes = mapOf(1 to episodes)
    }
}
```

#### 4. loadLinks()
**Purpose**: Extract video links from episode page

**Called**: When user tries to play an episode

**Returns**: `Boolean` indicating if links were found

```kotlin
override suspend fun loadLinks(
    data: String,
    isCasting: Boolean,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
): Boolean {
    val document = app.get(data).document
    var hasLinks = false
    
    // Find video links
    val m3u8Link = extractM3u8Link(document)
    if (m3u8Link.isNotEmpty()) {
        callback(ExtractorLink(
            source = name,
            name = name,
            url = m3u8Link,
            referer = data,
            quality = Qualities.Unknown.value,
            isM3u8 = true
        ))
        hasLinks = true
    }
    
    return hasLinks
}
```

## Key Classes

### SearchResponse
Represents an anime in search/home results

```kotlin
newAnimeSearchResponse(
    name = "Anime Title",
    url = "https://cartoony.net/anime/123",
    type = TvType.Anime
) {
    posterUrl = "https://example.com/poster.jpg"
    rating = 80  // Out of 100
}
```

### Episode
Represents a single episode

```kotlin
Episode(
    data = "https://cartoony.net/episode/123",  // URL to load
    name = "Episode 1",
    season = 1,
    episode = 1
)
```

### LoadResponse
Full anime details

```kotlin
newAnimeLoadResponse(
    name = "Anime Title",
    url = "https://cartoony.net/anime/123",
    type = TvType.Anime
) {
    posterUrl = ""
    rating = 80
    plot = "Description..."
    episodes = mapOf(
        1 to listOf(Episode(...), Episode(...))
    )
}
```

### ExtractorLink
A playable video link

```kotlin
ExtractorLink(
    source = "Cartoony",      // Provider name
    name = "Cartoony",        // Display name
    url = "https://video.com/stream.m3u8",
    referer = "https://cartoony.net/episode/123",
    quality = Qualities.Unknown.value,
    isM3u8 = true            // Is HLS stream
)
```

## HTML Parsing

The provider uses Jsoup for HTML parsing:

```kotlin
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

// Select single element
val title: Element? = document.selectFirst("h1")
val text: String? = title?.text()

// Select multiple elements
val items: Elements = document.select("div.anime-item")
items.forEach { item ->
    val href = item.attr("href")
    val text = item.text()
}

// CSS selectors
document.select("div.class > span#id")
document.select("a[href*=search]")
document.select(":has(> strong)")
```

## HTTP Requests

Use the app object for HTTP requests:

```kotlin
// GET request
val response = app.get("https://example.com")
val document = response.document
val text = response.text

// POST request
app.post(
    url = "https://example.com",
    data = mapOf("key" to "value"),
    headers = mapOf("Custom-Header" to "value")
)

// With custom headers
app.get(
    url = "https://example.com",
    headers = mapOf(
        "User-Agent" to "Mozilla/5.0...",
        "Referer" to "https://example.com"
    )
)
```

## Regex Patterns

Common patterns for video extraction:

```kotlin
// Find all URLs
val urlPattern = Regex("(https?://[^\\s\"']+)")

// Find m3u8 links
val m3u8Pattern = Regex("(https?://[^\\s\"']+\\.m3u8[^\\s\"']*)")

// Find video URLs from JSON
val videoPattern = Regex("\"video\"\\s*:\\s*\"([^\"]+)")

// Extract from JavaScript variable
val jsPattern = Regex("var\\s+videoUrl\\s*=\\s*\"([^\"]+)")
```

## Qualities

Available quality constants:

```kotlin
Qualities.Unknown.value      // 0
Qualities.SD.value           // 360
Qualities.HD.value           // 720
Qualities.FullHD.value       // 1080
Qualities.FourK.value        // 2160
```

## Logging

Add logging for debugging:

```kotlin
import android.util.Log

// Errors
Log.e("CartoonyProvider", "Error message")

// Info
Log.i("CartoonyProvider", "Info message")

// Using Cloudstream's logging
logError(Exception("Error message"))
```

## Common Patterns

### Pattern 1: Pagination
```kotlin
val results = mutableListOf<SearchResponse>()
var page = 1
while (results.size < 50) {
    val doc = app.get("$url?page=$page").document
    val items = doc.select("div.item")
    if (items.isEmpty()) break
    
    items.forEach { /* process */ }
    page++
}
```

### Pattern 2: Attribute Extraction
```kotlin
val data = element.attr("data-id")
val href = element.attr("href")
val src = element.attr("src")
val title = element.attr("title")
```

### Pattern 3: Multiple Selector Options
```kotlin
val title = element.selectFirst("h1, h2, h3, .title, .name")?.text() ?: "Unknown"
```

### Pattern 4: Try-Catch with Defaults
```kotlin
val rating = try {
    element.selectFirst(".rating")?.text()?.toFloatOrNull()?.toInt() ?: 0
} catch (e: Exception) {
    0
}
```

## CloudStream Callbacks

### Playing Video
```kotlin
// Called when user presses play
override suspend fun loadLinks(data: String, isCasting: Boolean, 
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit): Boolean {
    // callback() = pass video link
    // subtitleCallback() = pass subtitle link
}
```

### Return Values

- `true`: Links were found and provided
- `false`: No links found (show error in UI)

## Error Handling

```kotlin
try {
    val document = app.get(url).document
    // Process...
} catch (e: Exception) {
    logError(e)
    return emptyList()  // or empty response
}
```

## Type System

```kotlin
TvType.Anime           // Series (multiple episodes/seasons)
TvType.AnimeMovie      // Movie (single video)
TvType.OVA             // OVA episodes
TvType.Special         // Special episodes
```

## Next Steps

1. Inspect cartoony.net HTML structure
2. Update CSS selectors in the code
3. Test with sample URLs
4. Build the extension
5. Test in CloudStream3

For more information, check:
- IMPLEMENTATION_GUIDE.md
- CloudStream3 GitHub repository
- Arabico provider examples
