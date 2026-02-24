# Build Guide - Cartoony CloudStream Provider

## Overview

This guide walks you through building the Cartoony CloudStream3 extension from source.

---

## Prerequisites

Before building, ensure you have:

- ✅ Java Development Kit (JDK) 11 or higher installed
- ✅ JAVA_HOME environment variable set
- ✅ 500MB free disk space
- ✅ Internet connection (for downloading dependencies)

**Not set up?** See [SETUP_ENVIRONMENT.md](SETUP_ENVIRONMENT.md)

---

## Building the Extension

### Quick Start (3 Steps)

```powershell
# 1. Navigate to project directory
cd "C:\Users\MEHDI MARSAMAN\Documents\cartoony"

# 2. Clean previous build (optional but recommended)
.\gradlew.bat clean

# 3. Build the extension
.\gradlew.bat build
```

**First build**: 2-5 minutes (downloads dependencies)  
**Subsequent builds**: 30 seconds - 2 minutes

---

## Understanding the Build Output

### Successful Build Output

You should see output like:

```
Starting gradle daemon with OneDemandDaemonFetcher new daemon.
Downloaded com.lagradost.cloudstream3:cloudstream3:4.11.4 (4.2 MB)
Downloaded org.jsoup:jsoup:1.15.2 (363 KB)

> Task :cartoony:preBuild
> Task :cartoony:preDebugBuild
> Task :cartoony:compileDebugKotlin
> Task :cartoony:compileDebugJavaWithJavac
> Task :cartoony:bundleDebugAar
> Task :cartoony:assembleDebug
> Task :cartoony:preReleaseBuild
> Task :cartoony:compileReleaseKotlin
> Task :cartoony:compileReleaseJavaWithJavac
> Task :cartoony:bundleReleaseAar
> Task :cartoony:assembleRelease
> Task :cartoony:lint
> Task :cartoony:build

BUILD SUCCESSFUL in 2m 15s
```

### Output Files

After successful build, you'll find:

```
cartoony\build\outputs\aar\
├── cartoony-debug.aar       (Debug version)
└── cartoony-release.aar     (Release version) ← USE THIS!
```

**File size**: ~100-200 KB (depending on optimization)

---

## Build Variants

### Debug Build
```powershell
.\gradlew.bat assembleDebug
```
- Output: `cartoony/build/outputs/aar/cartoony-debug.aar`
- Larger file size (~200 KB)
- More logging enabled
- Use for testing and debugging

### Release Build
```powershell
.\gradlew.bat assembleRelease
```
- Output: `cartoony/build/outputs/aar/cartoony-release.aar`
- Smaller file size (~100 KB)
- Optimized with ProGuard
- Use for distribution

### Full Build (both debug + release)
```powershell
.\gradlew.bat build
```
- Builds both debug and release versions
- Runs all tests and linting
- Most thorough option

---

## Gradle Tasks Reference

Common Gradle commands:

```powershell
# Show all available tasks
.\gradlew.bat tasks

# Clean build artifacts
.\gradlew.bat clean

# Compile only (no packaging)
.\gradlew.bat compileDebugKotlin

# Build without tests
.\gradlew.bat build -x test

# Build with verbose output
.\gradlew.bat build --info

# Build with debug output
.\gradlew.bat build --debug

# Run linting only
.\gradlew.bat lint

# Check dependency tree
.\gradlew.bat dependencies
```

---

## Troubleshooting Build Issues

### Issue 1: "JAVA_HOME is not set"

```
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH
```

**Solution**:
1. See [SETUP_ENVIRONMENT.md](SETUP_ENVIRONMENT.md) - Step 1
2. Verify: `java -version`
3. Set environment variable and restart terminal
4. Try building again

### Issue 2: "Unsupported class-file format"

```
error: unsupported class-file format
```

**Solution**: Java version too old
1. Check Java version: `java -version`
2. Need JDK 11 or higher
3. Download from: https://adoptium.net/
4. Install and set JAVA_HOME
5. Try building again

### Issue 3: "Out of memory" during build

```
Java heap space
```

**Solution**:
1. Open or create `gradle.properties` in project root
2. Add: `org.gradle.jvmargs=-Xmx2048m`
3. Save and retry build

### Issue 4: "Module not found"

```
Could not resolve: com.lagradost:cloudstream3:4.11.4
```

**Solution**:
1. Check internet connection
2. Try clean build:
   ```powershell
   .\gradlew.bat clean
   .\gradlew.bat build --refresh-dependencies
   ```

### Issue 5: "Gradle wrapper not found"

```
'gradlew.bat' is not recognized
```

**Solution**:
1. Check you're in correct directory:
   ```powershell
   pwd  # Should show: C:\Users\MEHDI MARSAMAN\Documents\cartoony
   ls   # Should show: gradlew.bat
   ```
2. Use full path if needed:
   ```powershell
   "C:\Users\MEHDI MARSAMAN\Documents\cartoony\gradlew.bat" build
   ```

### Issue 6: "Compilation failed" with Kotlin errors

```
error: unresolved reference: Something
```

**Solutions**:
1. Check dependencies in `cartoony/build.gradle.kts`
2. Try clean build: `.\gradlew.bat clean build`
3. Refresh dependencies: `.\gradlew.bat build --refresh-dependencies`
4. Check CartoonyProvider.kt syntax

### Issue 7: Build hangs/freezes

**Solution**:
1. First build downloads lots of data (2-5 min is normal)
2. Wait 5-10 minutes before interrupting
3. If still stuck, press Ctrl+C to cancel
4. Check disk space: `dir` → ensure you have 500MB+ free
5. Try again

---

## Build Performance Tips

### Speed Up Builds

```powershell
# Skip tests and linting
.\gradlew.bat build -x test -x lint

# Build only debug version (faster)
.\gradlew.bat assembleDebug

# Use daemon (faster subsequent builds)
.\gradlew.bat --daemon build

# Disable daemon if problematic
.\gradlew.bat --no-daemon build
```

### First-Time Build Checklist

- [ ] Java 11+ installed
- [ ] JAVA_HOME set
- [ ] Internet connection active
- [ ] 500MB+ free disk space
- [ ] No antivirus blocking downloads
- [ ] Using correct directory
- [ ] Terminal restarted after environment changes

---

## After Successful Build

### Step 1: Verify AAR File

```powershell
# Check file exists and size
Get-ChildItem "C:\Users\MEHDI MARSAMAN\Documents\cartoony\cartoony\build\outputs\aar\cartoony-release.aar"
```

Expected output:
```
LastWriteTime : [current date/time]
Length        : ~100000-200000 bytes
Name          : cartoony-release.aar
```

### Step 2: Extract and Inspect (Optional)

```powershell
# AAR files are ZIP archives
# You can open with 7-Zip or extract with PowerShell

# Extract to inspect contents
Expand-Archive "cartoony/build/outputs/aar/cartoony-release.aar" -DestinationPath "cartoony-inspect"

# View structure
tree cartoony-inspect
```

Expected structure:
```
cartoony-inspect/
├── AndroidManifest.xml
├── classes.jar           ← Contains compiled code
├── R.txt
└── resources.pb
```

### Step 3: Deploy to CloudStream3

See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

---

## Build Information

### Dependencies Used

```kotlin
com.lagradost:cloudstream3:4.11.4
org.jsoup:jsoup:1.15.2
org.jetbrains.kotlin:kotlin-stdlib:1.9.23
```

### Compile Settings

```kotlin
sourceCompatibility = JavaVersion.VERSION_11
targetCompatibility = JavaVersion.VERSION_11
compileSdk = 33
minSdk = 21
targetSdk = 33
```

### Build Tools

```
Gradle: 7.6.2
Android Gradle Plugin: 8.4.1
Kotlin: 1.9.23
```

---

## Continuous Building (Watch Mode)

While developing, rebuild on every file change:

```powershell
# Build on file changes (Linux/Mac with fswatch)
# For Windows, use Gradle's daemon:

.\gradlew.bat --daemon build

# Subsequent builds will be faster thanks to daemon
```

---

## Advanced: Custom Build Configuration

### Modify Version

Edit `cartoony/build.gradle.kts`:

```kotlin
version = "1.0.0"  ← Change this
```

Then rebuild:

```powershell
.\gradlew.bat build
```

### Add Custom JVM Arguments

Edit `gradle.properties`:

```properties
# Memory settings
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m

# Faster builds
org.gradle.parallel=true
org.gradle.caching=true
```

### ProGuard Optimization

Rules in `cartoony/proguard-rules.pro` control code obfuscation.

**Default**: Enabled for release builds
**To disable**: Remove obfuscation from `release` block in `build.gradle.kts`

---

## CI/CD Integration

### GitHub Actions Example

If you want automatic builds on push:

```yaml
name: Build
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - run: ./gradlew build
      - uses: actions/upload-artifact@v2
        with:
          name: aar
          path: cartoony/build/outputs/aar/cartoony-release.aar
```

---

## Gradle Wrapper Info

This project uses **Gradle Wrapper** (7.6.2):

- ✅ No need to install Gradle separately
- ✅ Uses version defined in `gradle/wrapper/gradle-wrapper.properties`
- ✅ Works on Windows, Mac, Linux
- ✅ First run downloads Gradle (~200MB)

---

## Environment Variables

For reference, these are used during build:

```powershell
$env:JAVA_HOME              # Java installation path
$env:ANDROID_HOME           # Android SDK path (if using Android Studio)
$env:GRADLE_USER_HOME       # Gradle cache location
$env:ORG_GRADLE_PROJECT_*   # Custom properties
```

---

## Next Steps

1. ✅ Set up environment (SETUP_ENVIRONMENT.md)
2. ✅ Build successfully
3. ⏭️ Test selectors on cartoony.net
4. ⏭️ Deploy to CloudStream3 (DEPLOYMENT_GUIDE.md)
5. ⏭️ Integration testing

---

## Useful Resources

- **Gradle Guide**: https://gradle.org/
- **Android Build System**: https://developer.android.com/studio/build
- **Kotlin Compiler**: https://kotlinlang.org/docs/compiler-reference.html
- **CloudStream3 Build**: https://github.com/LagradOst/CloudStream-3/wiki

---

**Having build issues?** 
- Check SETUP_ENVIRONMENT.md
- Run with `--info` or `--debug` flags for more details
- Check for error messages above
- See PROJECT_STATUS.md for known issues

