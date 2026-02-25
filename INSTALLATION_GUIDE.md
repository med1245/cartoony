# Cartoony CloudStream Extension - Installation & Testing Guide

## ✅ Build Status
The extension has been successfully built! The `.cs3` file is located at:
```
cartoony/build/outputs/cs3/Cartoony.cs3
```

## 📦 What Was Fixed

### 1. Plugin Structure
- ✅ Correct `@CloudstreamPlugin` annotation
- ✅ Proper `Plugin()` base class inheritance
- ✅ Correct `load()` method override (no parameters)
- ✅ Proper `registerMainAPI()` call

### 2. Provider Implementation
- ✅ Extends `MainAPI()`
- ✅ Implements homepage browsing
- ✅ Implements search functionality
- ✅ Implements episode loading
- ✅ Implements HLS (.m3u8) video extraction

### 3. Build Configuration
- ✅ Proper plugin.json metadata
- ✅ Correct DEX compilation
- ✅ Proper .cs3 packaging

## 🚀 Installation Steps

### Method 1: ADB Push (Recommended for Testing)
```bash
# From project root
gradlew.bat :cartoony:deployWithAdb
```

This will:
1. Build the .cs3 file
2. Push it to `/sdcard/Download/Cartoony.cs3` on your device

### Method 2: Manual Installation
1. Copy `cartoony/build/outputs/cs3/Cartoony.cs3` to your Android device
2. Open CloudStream app
3. Go to **Settings** → **Extensions**
4. Tap **Install from storage**
5. Navigate to and select `Cartoony.cs3`
6. Restart CloudStream

### Method 3: Repository Hosting
1. Upload `Cartoony.cs3` to a web server or GitHub releases
2. Update `plugins.json` with the correct URL and SHA256
3. Add the repository URL in CloudStream

## 🔍 Troubleshooting

### "No plugin found in extension"

This error typically occurs when:

1. **Wrong API Level**: Check your CloudStream version
   - Current plugin targets: `minApi: 3`, `targetApi: 3`
   - If using older CloudStream, try the legacy version:
     ```bash
     gradlew.bat :cartoony:makeLegacy
     ```

2. **Corrupted Build**: Clean and rebuild
   ```bash
   gradlew.bat clean :cartoony:make
   ```

3. **Missing Permissions**: Grant "All Files Access" permission
   - Settings → Apps → CloudStream → Permissions → Files and media → Allow

4. **Cache Issues**: Clear CloudStream cache
   - Settings → Apps → CloudStream → Storage → Clear cache

### Verification Steps

1. **Check .cs3 file size**: Should be around 16-20 KB
   ```bash
   ls -lh cartoony/build/outputs/cs3/Cartoony.cs3
   ```

2. **Verify plugin.json** (extract .cs3 as ZIP):
   ```json
   {
     "name": "Cartoony",
     "className": "com.lagradost.CartoonyPlugin",
     "version": 1,
     "minApi": 3,
     "targetApi": 3
   }
   ```

3. **Check CloudStream logs**: Use `adb logcat` to see detailed errors
   ```bash
   adb logcat | grep -i "cartoony\|plugin\|extension"
   ```

## 📝 Plugin Features

### Homepage
- Fetches featured anime from cartoony.net
- Displays anime cards with posters and titles

### Search
- Search anime by title
- Returns matching results with metadata

### Video Playback
- Extracts HLS (.m3u8) streams from player pages
- Supports iframe-based video players
- Automatic quality detection

## 🔧 Development Commands

```bash
# Build plugin
gradlew.bat :cartoony:make

# Build legacy version
gradlew.bat :cartoony:makeLegacy

# Clean build
gradlew.bat clean

# Deploy to device via ADB
gradlew.bat :cartoony:deployWithAdb

# Stop Gradle daemon (if issues)
gradlew.bat --stop
```

## 📂 Project Structure

```
cartoony/
├── src/main/
│   ├── kotlin/com/lagradost/
│   │   ├── CartoonyPlugin.kt          # Plugin entry point
│   │   ├── CartoonyPluginLegacy.kt    # Legacy API support
│   │   └── CartoonyProvider.kt        # Main scraper logic
│   └── AndroidManifest.xml
├── build.gradle.kts                    # Build configuration
└── build/outputs/cs3/
    ├── Cartoony.cs3                    # Main plugin (API 3)
    └── CartoonyLegacy.cs3              # Legacy plugin (API 2)
```

## 🌐 Cartoony.net Scraping Strategy

The provider uses multiple fallback strategies:

1. **API Endpoints** (Primary):
   - `/api/tvshows` - List shows
   - `/api/shows/{id}` - Show details
   - `/api/shows/{id}/episodes` - Episode list
   - `/api/episodes/{id}/stream` - Stream URL

2. **HTML Scraping** (Fallback):
   - Homepage parsing for anime cards
   - Search results parsing
   - Episode page iframe extraction
   - M3U8 link regex extraction

## ✨ Next Steps

1. **Test the plugin** in CloudStream
2. **Verify video playback** works
3. **Check search functionality**
4. **Test different anime titles**
5. **Report any issues** for further fixes

## 📞 Support

If you encounter issues:
1. Check CloudStream version compatibility
2. Verify internet connection
3. Check cartoony.net is accessible
4. Review CloudStream logs via ADB
5. Try the legacy version if main version fails

---

**Built with**: Kotlin, Jsoup, CloudStream3 API
**Target**: Android 5.0+ (API 21+)
**Language**: Arabic (ar)
