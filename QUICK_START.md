# Quick Start Guide - Cartoony Provider

## 5-Minute Setup

### 1. Prerequisites
- Git
- Android Studio or terminal with Gradle
- JDK 11+

### 2. Clone & Build
```bash
cd cartoony
./gradlew build
```

### 3. Locate Output
```
cartoony/build/outputs/aar/cartoony-release.aar
```

### 4. Install to CloudStream3
- Copy the AAR file to CloudStream3 extensions folder
- Restart CloudStream3
- Provider will appear in sources list

---

## Customization - 15 Minutes

### Step 1: Inspect cartoony.net
1. Open https://cartoony.net
2. Right-click → Inspect (F12)
3. Find these selectors:
   - Anime container: `div.???`
   - Title: `h1 / h2 / .title`
   - Link: `a`
   - Image: `img[src]`

### Step 2: Update CartoonyProvider.kt

Find and update the selectors:

**Homepage (line ~35)**:
```kotlin
// OLD:
document.select("div.anime-container, div.featured-anime, div.anime-item")

// NEW (based on inspection):
document.select("YOUR_SELECTOR_HERE")
```

**Title extraction (line ~95)**:
```kotlin
// OLD:
document.selectFirst("h1, .anime-title, .title")?.text()

// NEW:
document.selectFirst("YOUR_SELECTOR_HERE")?.text()
```

### Step 3: Test & Build
```bash
./gradlew build
```

### Step 4: Load in CloudStream3

---

## Finding Video Links

### Method A: Browser DevTools (Best Way)
1. Open any anime on cartoony.net
2. Press F12 → Network tab
3. Play the video
4. Look for `.m3u8` request
5. Copy that URL

### Method B: View Page Source
1. Right-click → View Page Source
2. Ctrl+F → Search for `.m3u8`
3. Look for HLS/M3U8 URLs

### Method C: Check Script Tags
Look for patterns like:
```javascript
var videoUrl = "https://...";
data: { video: "https://..." }
```

---

## Common Selectors Reference

```kotlin
// Anime Containers
"div.poster"           // Container with anime info
"div.show-item"        // Show item wrapper
"div.anime-card"       // Anime card
"ul > li"              // List items

// Titles
"h1"                   // Main title
"h2"                   // Secondary title
".title, .name"        // Class names
"a.link"               // Link text

// Images
"img[src]"             // Any image with src
"img.poster"           // Image with poster class
".cover img"           // Image in cover div

// Links
"a[href]"              // Any link
"a.anime-link"         // Specific link class
"[href*=anime]"        // Href containing 'anime'

// Video
"video source"         // HTML5 video
"iframe"               // Embedded player
"source[src*=m3u8]"    // M3U8 source
```

---

## Debugging Checklist

- [ ] Selectors match actual HTML
- [ ] Search URL is correct
- [ ] Episodes are being extracted
- [ ] M3U8 link is accessible
- [ ] HTTP headers are correct (if needed)
- [ ] No JavaScript rendering required

---

## File Structure

```
cartoony/
├── build.gradle.kts              ← Module config
├── src/main/
│   ├── AndroidManifest.xml       ← Permissions
│   └── kotlin/com/lagradost/
│       └── CartoonyProvider.kt   ← MAIN FILE TO EDIT
├── proguard-rules.pro            ← Optimization
└── README.md                      ← Documentation
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Build fails | Check JDK version (need 11+), run `./gradlew clean` |
| No anime shown | Update CSS selectors, check browser DevTools |
| No videos play | Look for .m3u8 in Network tab, check regex pattern |
| Crashes on load | Add try-catch blocks, check logcat |
| Episode count wrong | Verify episode selector matches all episodes |

---

## Key Files to Edit

### 1. CartoonyProvider.kt (Main file)
- Change CSS selectors for your site
- Update video extraction logic
- Add custom headers if needed

### 2. build.gradle.kts
- Change version number
- Add dependencies if needed
- Update targetSdk if required

### 3. AndroidManifest.xml
- Add permissions if needed
- Change package name

---

## Testing Steps

1. **Build**: `./gradlew build` ✓
2. **Transfer**: Copy AAR to CloudStream3
3. **Restart**: Close and reopen CloudStream3
4. **Search**: Try searching for anime
5. **Play**: Try playing a video
6. **Check logs**: Look for errors in logcat

---

## Getting Help

**Documentation**:
- `README.md` - Overview
- `IMPLEMENTATION_GUIDE.md` - Detailed setup
- `API_REFERENCE.md` - Code reference

**Resources**:
- [CloudStream3 GitHub](https://github.com/LagradOst/CloudStream-3)
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Jsoup Documentation](https://jsoup.org/)

---

## Next: Advanced Customization

After getting basic functionality working:
- [ ] Add authentication if needed
- [ ] Implement pagination
- [ ] Add subtitle support
- [ ] Optimize performance
- [ ] Handle edge cases

See `IMPLEMENTATION_GUIDE.md` for details.
