# Implementation Guide for Cartoony Provider

## Overview

This guide explains how to adapt the Cartoony extension to work with the actual cartoony.net website.

## Step 1: Inspect the Website

Before using this provider, you need to inspect the actual HTML structure of cartoony.net:

1. Open https://cartoony.net in your browser
2. Right-click and select "Inspect" or press F12
3. Check the Elements/Inspector tab to understand the HTML structure

## Step 2: Update CSS Selectors

The provider uses CSS selectors to extract data. You need to customize these to match cartoony.net's actual HTML:

### Homepage Selectors (in `getMainPage()`)

Look for anime containers on the homepage:
```kotlin
// Current selector - NEEDS ADJUSTMENT
document.select("div.anime-container, div.featured-anime, div.anime-item")
```

Check the actual cartoony.net HTML and find:
- The container div for each anime (e.g., `.anime-card`, `.show-item`, `.poster-item`)
- Title element (usually `h2`, `h3`, or `.title`)
- Link element (usually `a`)
- Image element (usually `img` with `src` attribute)
- Rating element (if available)

### Search Selectors (in `search()`)

Update the search URL and result selectors:
```kotlin
// Update this URL to match cartoony.net's search functionality
val searchUrl = "$mainUrl/search?q=${query.replace(" ", "+")}"
```

Then adjust the selectors:
```kotlin
// Update these selectors based on actual HTML
document.select("div.anime-item, div.search-result, a.anime-link")
```

### Details Selectors (in `load()`)

Update selectors for anime detail page:
```kotlin
// Title - find the main heading
document.selectFirst("h1, .anime-title, .title")

// Poster image
document.selectFirst("img.poster, img.anime-poster, .poster img")

// Description/Synopsis
document.selectFirst(".synopsis, .description, .about, p")

// Rating/Score
document.selectFirst(".rating, .score, .rate")

// Episodes list
document.select("a.episode, .episode-link, div.episode")
```

## Step 3: Update Video Link Extraction

The provider looks for HLS (.m3u8) links in multiple ways:

### Method 1: Iframe Players

If the player is in an iframe:
```kotlin
document.select("iframe").forEach { iframe ->
    val iframeUrl = iframe.attr("src")
    // The provider fetches this iframe and looks for m3u8 links
}
```

### Method 2: Direct Script Content

If the m3u8 link is in a `<script>` tag:
```javascript
// Example: 
<script>
    var videoUrl = "https://example.com/video.m3u8";
</script>
```

The provider uses regex to find it.

### Method 3: Video Source Elements

If using HTML5 video:
```html
<video>
    <source src="https://example.com/video.m3u8" type="application/x-mpegURL">
</video>
```

## Debugging Steps

### Step 1: Enable Logging

Add this to your provider to debug selector issues:

```kotlin
override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
    val document = app.get(mainUrl).document
    
    // Print the document for debugging
    println("DEBUG: Full HTML length: ${document.html().length}")
    
    // Print found elements
    document.select("div.anime-container").forEach {
        println("DEBUG: Found element: ${it.html().take(200)}")
    }
    
    // ... rest of code
}
```

### Step 2: Check Network Requests

1. Open cartoony.net in browser
2. Open DevTools (F12)
3. Go to Network tab
4. Reload the page
5. Look for:
   - API endpoints for loading data
   - Player iframe sources
   - M3U8 video links

### Step 3: Test with Specific URLs

Modify the provider to test specific pages:

```kotlin
override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
    // Temporarily hardcode a URL for testing
    val document = app.get("$mainUrl/specific-anime-page").document
    // ... test extraction
}
```

## Common Issues and Solutions

### Issue: No anime titles found

**Solution**: 
1. Check if the HTML structure matches your selectors
2. Try more generic selectors: `document.select("a")` to see all links
3. Use browser DevTools to find the actual selector

### Issue: No video links extracted

**Solution**:
1. Check if the player is in an iframe (look in Network tab)
2. Search for `.m3u8` in page source (Ctrl+F)
3. Verify the video URL isn't behind JavaScript or requires authentication

### Issue: Wrong episodes loaded

**Solution**:
1. Verify the episode selector captures all episodes
2. Check if pagination is needed for multiple episodes
3. Look for hidden elements or lazy-loaded content

## Advanced Customization

### Adding Custom Headers

If cartoony.net requires specific headers:

```kotlin
override suspend fun loadLinks(
    data: String,
    isCasting: Boolean,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
): Boolean {
    val headers = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Referer" to mainUrl,
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    )
    
    val document = app.get(data, headers = headers).document
    // ... rest of code
}
```

### Handling JavaScript-Rendered Content

If content is loaded via JavaScript:

```kotlin
// This requires a headless browser (more complex)
// Consider using: https://github.com/aormis/Cloudstream3-Extensions
// Or checking if there's an API endpoint
```

### Adding Subtitles

If subtitles are available:

```kotlin
override suspend fun loadLinks(...): Boolean {
    // Find subtitle links
    document.select("a[href*='.vtt'], a[href*='.srt']").forEach { sub ->
        val url = sub.attr("href")
        subtitleCallback(SubtitleFile("English", url))
    }
}
```

## Building and Testing

### Build the extension

```bash
./gradlew build
```

The output will be in: `cartoony/build/outputs/aar/cartoony-release.aar`

### Testing Locally

1. Connect Android device or use emulator
2. Install CloudStream3
3. Copy the compiled AAR to CloudStream3's extension folder
4. Restart CloudStream3
5. Test the provider

## Performance Optimization

### Lazy Loading

For large result sets, consider implementing pagination:

```kotlin
override suspend fun search(query: String): List<SearchResponse> {
    val results = mutableListOf<SearchResponse>()
    var page = 1
    
    while (results.size < 50 && page < 10) {  // Limit to reasonable amount
        val searchUrl = "$mainUrl/search?q=$query&page=$page"
        val document = app.get(searchUrl).document
        
        val pageResults = document.select("div.anime-item").mapNotNull { item ->
            // Extract result
            null
        }
        
        if (pageResults.isEmpty()) break
        results.addAll(pageResults)
        page++
    }
    
    return results
}
```

### Caching

If needed, implement caching for frequently accessed data:

```kotlin
private val cache = mutableMapOf<String, LoadResponse>()

override suspend fun load(url: String): LoadResponse {
    cache[url]?.let { return it }
    
    // ... load from network
    
    return result.also { cache[url] = it }
}
```

## Browser DevTools Tips

### Finding the Video URL

1. Open video player
2. Go to Network tab
3. Filter for `.m3u8` requests
4. The .m3u8 URL is what you need

### Finding API Endpoints

1. Open Network tab
2. Set to XHR filter
3. Look for JSON responses that contain anime/episode data
4. These might be easier to parse than HTML

## Need Help?

Refer to the CloudStream3 documentation:
- Official Docs: https://github.com/LagradOst/CloudStream-3
- Extension Examples: Look at other providers in the main repository

## Legal Notes

Ensure that:
1. cartoony.net allows scraping in their robots.txt
2. You respect their Terms of Service
3. You use appropriate rate limiting to avoid overloading their servers
4. You respect copyright of the content served by the site
