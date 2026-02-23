# 🚀 COMPLETE STEP-BY-STEP GUIDE - Make Cartoony Work

## ⚠️ IMPORTANT: cartoony.net is a JavaScript SPA

The website loads content dynamically with JavaScript, so we need to use a **different approach**. We'll use the website's **API endpoints** instead of parsing HTML.

---

## PHASE 1: INVESTIGATE THE WEBSITE (30 minutes)

### Step 1.1: Open Developer Tools
1. Go to https://cartoony.net
2. Press **F12** to open Developer Tools
3. Go to the **Network** tab
4. Click the **XHR** filter (shows API calls)
5. Reload the page (F5)

### Step 1.2: Look for API Endpoints
When the page loads, you should see API calls like:
- `https://cartoony.net/api/shows` (get shows/series)
- `https://cartoony.net/api/episodes` (get episodes)
- `https://cartoony.net/api/watch/...` (get video URLs)

**What to do:**
- Click each request
- Go to the **Response** tab
- Copy the JSON response and save it

### Step 1.3: Test an API Endpoint
In your browser console (press F12 → Console):
```javascript
fetch('https://cartoony.net/api/shows')
  .then(r => r.json())
  .then(d => console.log(d))
```

This shows you the data structure.

### Step 1.4: Find Video Player
1. Go to any show/episode page
2. Play the video
3. Go to Network tab
4. Look for `.m3u8` requests
5. Copy the M3U8 URL

**Example what to look for:**
```
https://example.com/stream.m3u8
or
https://cdn.example.com/video.m3u8
```

---

## PHASE 2: EXPLORE API ENDPOINTS (20 minutes)

### Step 2.1: Homepage/Featured Shows
Try this in browser console:
```javascript
// Method 1
fetch('https://cartoony.net/api/shows').then(r => r.json()).then(d => console.log(d))

// Method 2
fetch('https://cartoony.net/api/featured').then(r => r.json()).then(d => console.log(d))

// Method 3
fetch('https://cartoony.net/api/home').then(r => r.json()).then(d => console.log(d))
```

**Save the response** - note the structure:
```json
{
  "shows": [
    {
      "id": "123",
      "title": "Show Name",
      "image": "url",
      "rating": 8.5,
      ...
    }
  ]
}
```

### Step 2.2: Search Endpoint
Try these:
```javascript
fetch('https://cartoony.net/api/search?q=anime-name')
  .then(r => r.json())
  .then(d => console.log(d))

fetch('https://cartoony.net/api/shows?search=anime-name')
  .then(r => r.json())
  .then(d => console.log(d))
```

### Step 2.3: Show Details
Click on a show and find its ID, then try:
```javascript
fetch('https://cartoony.net/api/shows/SHOW_ID')
  .then(r => r.json())
  .then(d => console.log(d))
```

### Step 2.4: Episodes List
```javascript
fetch('https://cartoony.net/api/shows/SHOW_ID/episodes')
  .then(r => r.json())
  .then(d => console.log(d))
```

### Step 2.5: Video Stream URL
```javascript
fetch('https://cartoony.net/api/episodes/EPISODE_ID/stream')
  .then(r => r.json())
  .then(d => console.log(d))

// Or with different endpoint
fetch('https://cartoony.net/api/watch/EPISODE_ID')
  .then(r => r.json())
  .then(d => console.log(d))
```

---

## PHASE 3: DOCUMENT YOUR FINDINGS (10 minutes)

Create a text file with your findings:

```
=== CARTOONY.NET API DOCUMENTATION ===

1. HOMEPAGE/FEATURED:
   - URL: https://cartoony.net/api/???
   - Response structure:
   {
     "shows": [
       {
         "id": "...",
         "title": "...",
         "image": "...",
         "rating": ...
       }
     ]
   }

2. SEARCH:
   - URL: https://cartoony.net/api/????q=QUERY
   - Returns: [Same as above]

3. SHOW DETAILS:
   - URL: https://cartoony.net/api/shows/SHOW_ID
   - Response: {
       "id": "...",
       "title": "...",
       "description": "...",
       "episodes": [...]
     }

4. EPISODES:
   - URL: https://cartoony.net/api/shows/SHOW_ID/episodes
   - Returns: [list of episodes]

5. VIDEO LINK:
   - URL: https://cartoony.net/api/episodes/EPISODE_ID/stream
   - Returns: {
       "url": "https://example.com/video.m3u8"
     }
```

---

## PHASE 4: UPDATE CartoonyProvider.kt (30 minutes)

Now update the provider with the actual API endpoints.

### Step 4.1: Open the File
```
C:\Users\MEHDI MARSAMAN\Documents\cloudstream repo cartoony\cartoony\cartoony\src\main\kotlin\com\lagradost\CartoonyProvider.kt
```

### Step 4.2: Update Import Statements
At the top, add JSON parsing support. Find these lines and make sure they exist:

```kotlin
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import org.jsoup.Jsoup
```

After these imports, add:
```kotlin
import org.json.JSONArray
import org.json.JSONObject
```

### Step 4.3: Replace getMainPage() Function

Find this section (around line 27-50):
```kotlin
override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
```

Replace the entire function with:
```kotlin
override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
    val homePageList = mutableListOf<SearchResponse>()
    
    try {
        // Try to fetch from API
        val response = app.get("$mainUrl/api/shows").text
        val jsonArray = JSONArray(response)
        
        for (i in 0 until minOf(jsonArray.length(), 20)) {
            val item = jsonArray.getJSONObject(i)
            
            val title = item.optString("title", "Unknown")
            val id = item.optString("id", "") 
            val posterUrl = item.optString("image", "")
            val rating = item.optDouble("rating", 0.0).toInt()
            
            if (id.isEmpty()) continue
            
            val anime = newAnimeSearchResponse(
                name = title,
                url = "$mainUrl/watch/$id",  // Adjust URL pattern
                type = TvType.Anime
            ) {
                this.posterUrl = posterUrl
                this.rating = rating
            }
            homePageList.add(anime)
        }
    } catch (e: Exception) {
        logError(e)
        // Fallback to parsing HTML if API fails
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
```

### Step 4.4: Replace search() Function

Find this section (around line 52-75):
```kotlin
override suspend fun search(query: String): List<SearchResponse> {
```

Replace with:
```kotlin
override suspend fun search(query: String): List<SearchResponse> {
    val results = mutableListOf<SearchResponse>()
    
    try {
        // Try search API
        val response = app.get("$mainUrl/api/search?q=${query.replace(" ", "+")}").text
        val jsonArray = JSONArray(response)
        
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            
            val title = item.optString("title", "Unknown")
            val id = item.optString("id", "")
            val posterUrl = item.optString("image", "")
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
        }
    } catch (e: Exception) {
        logError(e)
    }

    return results
}
```

### Step 4.5: Replace load() Function

Find this section (around line 77-110):
```kotlin
override suspend fun load(url: String): LoadResponse {
```

Replace with:
```kotlin
override suspend fun load(url: String): LoadResponse {
    // Extract show ID from URL
    val showId = url.substringAfterLast("/")
    
    var title = "Unknown"
    var posterUrl = ""
    var description = ""
    var rating = 0
    val episodes = mutableListOf<Episode>()
    
    try {
        // Fetch show details from API
        val showResponse = app.get("$mainUrl/api/shows/$showId").text
        val showJson = JSONObject(showResponse)
        
        title = showJson.optString("title", "Unknown")
        posterUrl = showJson.optString("image", "")
        description = showJson.optString("description", "")
        rating = showJson.optDouble("rating", 0.0).toInt()
        
        // Fetch episodes
        val episodesResponse = app.get("$mainUrl/api/shows/$showId/episodes").text
        val episodesJson = JSONArray(episodesResponse)
        
        for (i in 0 until episodesJson.length()) {
            val ep = episodesJson.getJSONObject(i)
            
            val epId = ep.optString("id", "")
            val epTitle = ep.optString("title", "Episode ${i + 1}")
            val epNumber = ep.optInt("episode_number", i + 1)
            
            if (epId.isEmpty()) continue
            
            episodes.add(
                Episode(
                    data = "$mainUrl/api/episodes/$epId/stream",  // API endpoint
                    name = epTitle,
                    season = 1,
                    episode = epNumber
                )
            )
        }
    } catch (e: Exception) {
        logError(e)
    }

    return newAnimeLoadResponse(
        name = title,
        url = url,
        type = TvType.Anime
    ) {
        posterUrl = posterUrl
        this.plot = description
        this.rating = rating
        this.episodes = mapOf(1 to episodes)
    }
}
```

### Step 4.6: Replace loadLinks() Function

Find this section (around line 112-140):
```kotlin
override suspend fun loadLinks(
```

Replace with:
```kotlin
override suspend fun loadLinks(
    data: String,
    isCasting: Boolean,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
): Boolean {
    var hasLinks = false
    
    try {
        // data should be the API endpoint URL like: https://cartoony.net/api/episodes/ID/stream
        val response = app.get(data).text
        val json = JSONObject(response)
        
        // Try to get the M3U8 URL from different possible JSON keys
        var m3u8Url = json.optString("url", "")
        if (m3u8Url.isEmpty()) {
            m3u8Url = json.optString("stream", "")
        }
        if (m3u8Url.isEmpty()) {
            m3u8Url = json.optString("video", "")
        }
        if (m3u8Url.isEmpty()) {
            m3u8Url = json.optString("link", "")
        }
        
        if (m3u8Url.isNotEmpty()) {
            // Verify it's an M3U8 link
            if (m3u8Url.contains(".m3u8", ignoreCase = true)) {
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
        }
    } catch (e: Exception) {
        logError(e)
    }

    return hasLinks
}
```

---

## PHASE 5: BUILD THE EXTENSION (10 minutes)

### Step 5.1: Open Terminal
Press **Windows Key + R** and type:
```
cmd
```

### Step 5.2: Navigate to Project
```
cd "C:\Users\MEHDI MARSAMAN\Documents\cloudstream repo cartoony\cartoony"
```

### Step 5.3: Build the Extension
```
gradlew build
```

**Wait for it to finish...** You should see:
```
BUILD SUCCESSFUL
```

### Step 5.4: Check Output
The compiled file should be at:
```
cartoony\build\outputs\aar\cartoony-release.aar
```

If you see errors, share them with me!

---

## PHASE 6: TEST IN CLOUDSTREAM3 (15 minutes)

### Step 6.1: Install CloudStream3 (if not already)
Download from: https://github.com/LagradOst/CloudStream-3/releases

### Step 6.2: Find Extensions Folder
On Android, the folder is usually:
```
/storage/emulated/0/Android/data/com.lagradost.cloudstream3/files/extensions/
```

On PC (with Android emulator):
Find your emulator folder and navigate to the extensions directory.

### Step 6.3: Copy Your Extension
Copy the `.aar` file from:
```
C:\Users\MEHDI MARSAMAN\Documents\cloudstream repo cartoony\cartoony\cartoony\build\outputs\aar\
```

To:
```
CloudStream3/extensions/
```

### Step 6.4: Restart CloudStream3
- Close the app completely
- Reopen it
- The Cartoony provider should appear in your source list

### Step 6.5: Test
1. Search for an anime
2. Click on it
3. Select an episode
4. Try to play

---

## 🐛 TROUBLESHOOTING

### Problem 1: API Endpoints Not Working
**Solution:**
1. Go back to Phase 1
2. Double-check the API URLs in your browser console
3. Make sure you have the correct endpoint URLs

### Problem 2: Build Fails
**Solution:**
1. Open `CartoonyProvider.kt`
2. Check for typos (look for red squiggly lines)
3. Make sure all `import` statements are present
4. Try: `gradlew clean build`

### Problem 3: No Results Show Up
**Solution:**
1. Check if the API response structure matches your code
2. In `getMainPage()`, check if the JSON keys are correct (might be `shows` instead of array)
3. Add `println()` for debugging:
   ```kotlin
   println("DEBUG: Found ${homePageList.size} shows")
   ```

### Problem 4: Video Won't Play
**Solution:**
1. Check if the M3U8 URL is being extracted correctly
2. Verify the URL is accessible (copy it to browser)
3. Check if it requires authentication headers

### Problem 5: Extension Doesn't Appear
**Solution:**
1. Make sure `.aar` file was copied to correct folder
2. Fully close CloudStream3 (not just minimize)
3. Try uninstalling and reinstalling CloudStream3

---

## 📱 QUICK TEST CHECKLIST

- [ ] Opened cartoony.net in browser with DevTools
- [ ] Found API endpoints (noted URLs)
- [ ] Tested API endpoints in console
- [ ] Updated CartoonyProvider.kt with correct endpoints
- [ ] Built extension successfully
- [ ] Copied `.aar` to CloudStream3
- [ ] Restarted CloudStream3
- [ ] Searched for anime
- [ ] Selected episode
- [ ] Video played

---

## 📝 DEBUGGING TIPS

### See Build Logs
```
gradlew build -i
```

### See Runtime Logs
In CloudStream3, open Settings → Developer/Debug section to view logs.

### Add Debugging to Code
Add this to CartoonyProvider.kt:
```kotlin
println("DEBUG: API Response: $response")
```

Then check CloudStream3's logcat output.

---

## 🎯 NEXT STEPS

1. **Complete Phase 1-3** to understand cartoony.net's API
2. **Update CartoonyProvider.kt** with correct API endpoints
3. **Build the extension**
4. **Test in CloudStream3**
5. **Adjust as needed** based on what you find

---

**Need Help?** 
- What errors do you see when building?
- What API endpoints did you find?
- Share the JSON responses from the API!

Let me know where you get stuck! 🚀
