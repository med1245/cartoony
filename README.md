# Cartoony Provider for CloudStream3

A CloudStream3 extension for streaming anime from **cartoony.net**.

**Status**: 70% Complete - Ready for Testing & Refinement  
**Last Updated**: February 24, 2026

## 🚀 Quick Links

| Document | Purpose |
|----------|---------|
| **[COMPLETION_CHECKLIST.md](COMPLETION_CHECKLIST.md)** | ⭐ **START HERE** - Step-by-step checklist |
| [SETUP_ENVIRONMENT.md](SETUP_ENVIRONMENT.md) | Install Java, Android SDK, Git |
| [BUILD_GUIDE.md](BUILD_GUIDE.md) | Build the extension |
| [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) | Deploy to CloudStream3 |
| [QUICK_START.md](QUICK_START.md) | 5-15 minute setup |
| [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) | Detailed customization |
| [PROJECT_STATUS.md](PROJECT_STATUS.md) | Project overview & status |

## Features

- **Homepage Browsing**: Browse featured and popular anime from the homepage
- **Search**: Search for anime by title
- **Episode Loading**: View and load episodes
- **HLS Video Streaming**: Extract and stream HLS (.m3u8) video links
- **Multiple Player Support**: Supports various embedded players

## Installation

### Prerequisites

- ✅ Java Development Kit (JDK) 11 or higher
- ✅ Gradle (included via wrapper)
- ✅ Android SDK (optional, for Android Studio users)

**Not installed?** See [SETUP_ENVIRONMENT.md](SETUP_ENVIRONMENT.md)

### Quick Build (3 Steps)

1. **Open terminal/PowerShell** and navigate to project:
```bash
cd "C:\Users\MEHDI MARSAMAN\Documents\cartoony"
```

2. **Build the extension**:
```bash
.\gradlew.bat build
```

3. **Find the compiled extension**:
```
cartoony/build/outputs/aar/cartoony-release.aar
```

### First Time Users

1. [Install Java](SETUP_ENVIRONMENT.md) (if needed)
2. [Build the extension](BUILD_GUIDE.md)
3. [Deploy to CloudStream3](DEPLOYMENT_GUIDE.md)
4. [Test features](COMPLETION_CHECKLIST.md)

## Usage

### In CloudStream3

1. Copy the compiled `.jar` file to your CloudStream3 extensions folder
2. Restart CloudStream3
3. The Cartoony provider should appear in your sources list

## API Implementation

This extension implements the CloudStream3 MainAPI with the following methods:

- `getMainPage()`: Fetches anime from the homepage
- `search()`: Searches for anime by query
- `load()`: Loads full anime details and episode list
- `loadLinks()`: Extracts video links from player pages

## Video Extraction

The provider handles multiple video link extraction methods:

- Iframe embedded players
- Direct M3U8 links in script tags
- Video source elements
- Data attributes

## Structure

```
cartoony/
├── build.gradle.kts              # Module build configuration
├── proguard-rules.pro             # ProGuard rules
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml    # Android manifest
│   │   └── kotlin/
│   │       └── com/lagradost/
│   │           └── CartoonyProvider.kt  # Main provider class
```

## Customization

### Modifying Selectors

The CSS selectors in the provider can be adjusted to match the actual HTML structure of cartoony.net:

```kotlin
// In getMainPage()
document.select("div.anime-container, div.featured-anime, div.anime-item")

// In load()
document.selectFirst("h1, .anime-title, .title")
```

### Adding Custom Headers

Modify the `loadLinks()` method to add custom HTTP headers if needed:

```kotlin
val headers = mapOf(
    "User-Agent" to "CustomUserAgent",
    "Referer" to data
)
app.get(url, headers = headers)
```

## Troubleshooting

### No Videos Found

1. Check that the CSS selectors match the actual HTML structure
2. Verify that HLS links are being served properly
3. Check network requests in browser DevTools

### Build Errors

1. Ensure JDK version is 11 or higher
2. Check Gradle version compatibility
3. Verify all dependencies are accessible

## Legal Disclaimer

This extension is provided for educational purposes only. Users are responsible for ensuring they comply with all applicable laws and terms of service when using this extension.

## Contributing

Improvements and bug fixes are welcome. Please submit pull requests with clear descriptions of changes.

## License

This project follows the same license as CloudStream3.
