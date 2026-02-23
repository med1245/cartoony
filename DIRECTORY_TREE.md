# Project Directory Structure

## Complete Cartoony Provider File Tree

```
cartoony/
│
├── 📄 Root Configuration Files
│   ├── build.gradle.kts                    [Gradle build config]
│   ├── settings.gradle.kts                 [Gradle settings - multi-module]
│   ├── gradle.properties                   [Gradle properties]
│   └── .gitignore                          [Git ignore patterns]
│
├── 📚 Documentation Files (Start Here!)
│   ├── README.md                           [📖 Overview & features - READ FIRST]
│   ├── QUICK_START.md                      [⚡ 5-15 min setup - READ SECOND]
│   ├── IMPLEMENTATION_GUIDE.md             [🔧 Customization guide - READ THIRD]
│   ├── API_REFERENCE.md                    [📚 CloudStream API reference]
│   ├── PROJECT_SUMMARY.md                  [📋 Complete project overview]
│   └── DIRECTORY_TREE.md                   [🗂️ This file]
│
└── 📁 cartoony/ (Main Module)
    │
    ├── 📄 Build & Config
    │   ├── build.gradle.kts                [Build configuration - may need version update]
    │   ├── proguard-rules.pro              [Code optimization rules]
    │   └── src/main/AndroidManifest.xml    [Android manifest]
    │
    └── 📁 src/main/kotlin/
        └── com/lagradost/
            └── CartoonyProvider.kt          [⭐ MAIN FILE - Edit this!]
                ├── MainAPI Implementation
                ├── getMainPage()            [Browse featured anime]
                ├── search()                 [Search anime by title]
                ├── load()                   [Load anime details]
                ├── loadLinks()              [Extract video links]
                └── extractM3u8Link()        [Helper to find M3U8]
```

## File Statistics

| Category | Count | Size |
|----------|-------|------|
| Gradle Files | 3 | ~5 KB |
| Documentation | 6 | ~50 KB |
| Source Code | 1 | ~15 KB |
| Config Files | 3 | ~2 KB |
| **TOTAL** | **13** | **~72 KB** |

## Documentation Hierarchy

```
START HERE
    ↓
README.md (Overview)
    ↓
QUICK_START.md (Fast Setup)
    ↓
CartoonyProvider.kt (Main Code)
    ↓
IMPLEMENTATION_GUIDE.md (Customization)
    ↓
API_REFERENCE.md (Deep Dive)
```

## What Each File Does

### 🎯 Essential Files (Must Edit/Know)

```
CartoonyProvider.kt
├── CSS Selectors → Update to match cartoony.net HTML
├── Video Extraction → Update to find .m3u8 links
└── Search URL → Update to match site's search endpoint
```

### 📖 Essential Documentation (Must Read)

```
README.md
└── Understand what this extension does

QUICK_START.md
└── Get it working in 15 minutes

IMPLEMENTATION_GUIDE.md
└── Adapt it to cartoony.net

API_REFERENCE.md
└── Understanding CloudStream API
```

### ⚙️ Configuration Files (Rarely Edit)

```
build.gradle.kts (root)
└── Build plugins, repositories

cartoony/build.gradle.kts
└── Version number, dependencies

settings.gradle.kts
└── Module structure

gradle.properties
└── Gradle settings
```

### 🔐 Support Files (Auto-Generated)

```
AndroidManifest.xml
└── Android app configuration

proguard-rules.pro
└── Code optimization

.gitignore
└── Git exclusion patterns
```

## Customization Flow

```
1. Read: README.md
   ↓
2. Read: QUICK_START.md
   ↓
3. Build: ./gradlew build
   ↓
4. Inspect: cartoony.net with browser DevTools
   ↓
5. Edit: CartoonyProvider.kt
   └── Update selectors
   └── Update video extraction
   └── Update search URL
   ↓
6. Build: ./gradlew build
   ↓
7. Test: In CloudStream3
   ↓
8. Reference: API_REFERENCE.md (if needed)
```

## Build Output Locations

After running `./gradlew build`:

```
cartoony/build/
├── outputs/
│   ├── aar/
│   │   ├── cartoony-debug.aar           [Debug version - for testing]
│   │   └── cartoony-release.aar         [Release version - for distribution]
│   └── ...
├── intermediates/
│   └── ... [Temporary build files]
└── ...
```

## Total Lines of Code

| File | Lines | Type |
|------|-------|------|
| CartoonyProvider.kt | ~280 | Kotlin |
| build.gradle.kts | ~30 | Gradle |
| AndroidManifest.xml | ~10 | XML |
| proguard-rules.pro | ~25 | Config |
| Documentation | ~1000 | Markdown |

## Memory Usage

| Component | Size |
|-----------|------|
| Source Code | ~15 KB |
| Documentation | ~50 KB |
| Gradle Cache | ~500 MB (first build) |
| Build Output (.aar) | ~100 KB |

## Dependencies

```
Direct Dependencies:
├── com.lagradost:cloudstream3:4.11.4
├── org.jsoup:jsoup:1.15.2
└── org.jetbrains.kotlin:kotlin-stdlib:1.8.10

Gradle Plugins:
├── kotlin("jvm") v1.8.10
└── com.android.library v7.4.2
```

## Important Paths

```
Project Root:
C:\Users\MEHDI MARSAMAN\Documents\cloudstream repo cartoony\cartoony\

Source Code:
cartoony\src\main\kotlin\com\lagradost\CartoonyProvider.kt

Build Output:
cartoony\build\outputs\aar\

Config Files:
├── build.gradle.kts (root)
├── cartoony\build.gradle.kts
├── settings.gradle.kts
└── gradle.properties

Documentation:
├── README.md
├── QUICK_START.md
├── IMPLEMENTATION_GUIDE.md
├── API_REFERENCE.md
└── PROJECT_SUMMARY.md
```

## Git Structure

```
.gitignore
├── build/                  [Build output - ignored]
├── .gradle/               [Gradle cache - ignored]
├── *.class               [Compiled files - ignored]
└── Allows:
    ├── src/
    ├── *.gradle.kts
    ├── *.md
    └── AndroidManifest.xml
```

## Next Steps Indicator

```
✅ Created           Project Structure
✅ Created           Documentation (5 files)
✅ Created           Build Configuration
✅ Created           Main Provider Code
⏳ TODO              Update CSS Selectors
⏳ TODO              Test with cartoony.net
⏳ TODO              Extract Video Links
✅ Ready             For Customization
```

## Quick Reference

| Need | File |
|------|------|
| Overview | README.md |
| Fast Setup | QUICK_START.md |
| CSS Selectors | IMPLEMENTATION_GUIDE.md |
| API Docs | API_REFERENCE.md |
| Project Map | PROJECT_SUMMARY.md |
| File Tree | DIRECTORY_TREE.md (this file) |
| Main Code | CartoonyProvider.kt |
| Build Config | build.gradle.kts |

## Maintenance Files

```
Directory Tree: DIRECTORY_TREE.md (this file)
Last Updated: 2025-02-23
Version: 1.0.0
Gradle Version: 7.4.2+
Kotlin Version: 1.8.10
Min SDK: 21
Target SDK: 33
```

---

**Total Setup Time: 15-60 minutes**
- 5 min: Read docs
- 5 min: Build
- 45 min: Customize & test

**All files ready! 🚀**
