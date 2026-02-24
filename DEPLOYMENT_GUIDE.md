# Deployment Guide - Cartoony CloudStream Provider

## Overview

This guide explains how to deploy the compiled Cartoony extension to CloudStream3 and test it.

---

## Prerequisites

- ✅ Successful build (see BUILD_GUIDE.md)
- ✅ CloudStream3 APK installed
- ✅ Android device or emulator
- ✅ File: `cartoony/build/outputs/aar/cartoony-release.aar`

---

## Deployment Methods

### Method 1: Using Android Device (Physical Phone)

#### Step 1: Enable Developer Mode

On your Android device:
1. Go to Settings → About Phone
2. Find "Build Number" (usually at bottom)
3. Tap "Build Number" 7 times until it says "You are now a developer"
4. Go back to Settings
5. Go to Settings → Developer Options (now visible)
6. Enable "USB Debugging"

#### Step 2: Connect Device

1. Connect phone to computer via USB cable
2. A dialog may appear on phone asking to allow USB debugging
3. Check "Always allow from this computer" and tap "Allow"

#### Step 3: Find CloudStream3 Extensions Folder

The extension folder location varies by device:

**Option A: Find manually**
1. On device, open CloudStream3 app
2. Go to Settings → About
3. Look for "Extensions Dir" path (usually shown)
4. Common path: `/sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/`

**Option B: Using file explorer**
1. Install ES File Explorer or similar
2. Navigate to: `Android/data/com.lagradost.cloudstream3/files/extensions/`
3. Remember this path

#### Step 4: Deploy AAR File

**Using ADB (Android Debug Bridge)**

```powershell
# First, install ADB
# Download from: https://developer.android.com/tools/adb

# List connected devices
adb devices
# Should show: device-name        device

# Copy AAR to device
adb push "cartoony/build/outputs/aar/cartoony-release.aar" "/sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/Cartoony.aar"

# Verify
adb shell ls -la /sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/
```

**Alternative: Using File Manager**

1. On computer, enable Media Transfer Protocol (MTP) for device
2. Device appears in File Explorer as storage device
3. Navigate to: `Android/data/com.lagradost.cloudstream3/files/extensions/`
4. Copy-paste `cartoony-release.aar` file
5. Rename if needed (doesn't matter)

#### Step 5: Restart CloudStream3

1. Close CloudStream3 app completely
2. Wait 5 seconds
3. Reopen CloudStream3
4. Provider should appear in Sources list

### Method 2: Using Android Emulator (Android Studio)

#### Step 1: Create Virtual Device

1. Open Android Studio
2. Go to: Tools → AVD Manager
3. Click "Create Virtual Device"
4. Select "Pixel 4" (recommended)
5. Download system image (API 33)
6. Complete setup and start emulator

#### Step 2: Wait for Emulator Boot

- First boot takes 2-5 minutes
- Wait until home screen appears
- Android emulator icon appears in taskbar

#### Step 3: Copy File to Emulator

```powershell
# List running emulators
adb devices
# Should show: emulator-5554    device

# Push AAR file
adb push "cartoony/build/outputs/aar/cartoony-release.aar" "/sdcard/Download/cartoony.aar"

# Verify
adb shell ls -la /sdcard/Download/
```

#### Step 4: Move to CloudStream3 Extensions

```powershell
# SSH into emulator
adb shell

# Create extensions folder if needed
mkdir -p /sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/

# Copy file
cp /sdcard/Download/cartoony.aar /sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/Cartoony.aar

# Exit shell
exit
```

#### Step 5: Install & Test CloudStream3

1. In emulator, open Google Play Store
2. Search for "CloudStream3"
3. Install official CloudStream3 app
4. Open app and restart
5. Provider should appear in Sources

---

## Testing the Extension

### Step 1: Verify Provider Appears

1. Open CloudStream3
2. Go to "Browse" or "Sources"
3. Look for "Cartoony" in the list
4. Should see provider name and icon (if configured)

**Not appearing?** 
- Check file is in extensions folder
- Verify filename ends in `.aar`
- Try restarting CloudStream3
- Check logcat for errors: `adb logcat | grep -i cartoony`

### Step 2: Test Homepage

1. Click on "Cartoony" provider
2. Homepage should load with anime list
3. Should see anime titles, posters, and ratings

**Issues?**
- If blank: CSS selectors don't match site
- If crashes: Check logcat for exception
- If slow: Network issue or large dataset

### Step 3: Test Search

1. Tap search icon
2. Type anime name (e.g., "Death Note")
3. Results should appear

**Issues?**
- If no results: Search endpoint might be wrong
- If crashes: Selector parsing issue
- If slow: Too many results or slow network

### Step 4: Test Anime Details

1. Click on any anime from search or homepage
2. Details page should load showing:
   - Title
   - Poster image
   - Description/synopsis
   - Episode list
   - Episode count

**Issues?**
- Missing details: Selector needs adjustment
- Episodes not showing: Episode selector wrong
- Poster not loading: Image URL issue

### Step 5: Test Video Playback

1. Click on an episode
2. Video player should open
3. M3U8 stream should start loading
4. Video should play

**Issues?**
- Player opens but no video: M3U8 URL not found
- Black screen: Referer header issue
- Crashes: Video format unsupported

---

## Debugging Guide

### View Logs

While testing, monitor logs for errors:

```powershell
# Show all logs
adb logcat

# Filter for Cartoony provider
adb logcat | grep -i cartoony

# Filter for errors
adb logcat | grep -i error

# Show last 100 lines
adb logcat | tail -100
```

### Common Log Errors

```
E/CartoonyProvider: No anime found
→ CSS selectors don't match HTML

E/CartoonyProvider: Failed to extract m3u8
→ Video extraction logic needs adjustment

E/CloudStream3: Provider crashed
→ Check for null pointer exceptions

E/OkHttp: Failed to connect
→ Network issue or blocked URL
```

### Enable Debug Logging

Add to CartoonyProvider.kt:

```kotlin
println("DEBUG: Starting getMainPage()")
println("DEBUG: Found ${animeList.size} anime")
```

Then rebuild and redeploy:

```powershell
.\gradlew.bat build
adb push cartoony/build/outputs/aar/cartoony-release.aar ...
```

---

## Testing Checklist

Complete this checklist after deployment:

### Functionality Tests
- [ ] Provider appears in CloudStream3
- [ ] Tapping provider loads homepage
- [ ] Homepage shows anime list
- [ ] Anime have poster images
- [ ] Search functionality works
- [ ] Search returns results
- [ ] Clicking anime shows details page
- [ ] Details page has title, poster, description
- [ ] Episodes list displays
- [ ] Episode count is correct
- [ ] Clicking episode opens player
- [ ] Video player loads
- [ ] M3U8 stream starts playing
- [ ] Video quality is good
- [ ] No persistent lag or stuttering

### Error Handling Tests
- [ ] Searching with empty query handles gracefully
- [ ] Searching for non-existent anime doesn't crash
- [ ] Network timeout doesn't crash app
- [ ] Missing images show placeholder
- [ ] Missing description shows "N/A"
- [ ] No episodes show "No episodes" message

### Performance Tests
- [ ] Homepage loads in < 3 seconds
- [ ] Search results appear in < 2 seconds
- [ ] Details page loads in < 2 seconds
- [ ] No excessive network requests
- [ ] Scrolling is smooth
- [ ] App doesn't consume excessive memory

### Compatibility Tests
- [ ] Works on different Android versions
- [ ] Works on different screen sizes
- [ ] Landscape and portrait orientations work
- [ ] Low-end devices don't crash
- [ ] High-end devices have good performance

---

## Undeployment / Removal

### Remove Provider

```powershell
# Method 1: ADB
adb shell rm /sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/Cartoony.aar

# Method 2: Manual
# On device, use file manager to navigate to:
# Android/data/com.lagradost.cloudstream3/files/extensions/
# Delete Cartoony.aar file

# Restart CloudStream3
```

---

## Continuous Testing

### Rapid Iteration Workflow

```powershell
# Make code changes to CartoonyProvider.kt
# Edit: C:\Users\MEHDI MARSAMAN\Documents\cartoony\cartoony\src\main\kotlin\com\lagradost\CartoonyProvider.kt

# Rebuild
.\gradlew.bat build

# Redeploy
adb push cartoony/build/outputs/aar/cartoony-release.aar /sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/Cartoony.aar

# Restart CloudStream3
# Test changes
```

### Quick Deploy Script

Create `deploy.ps1`:

```powershell
param(
    [string]$Build = "release"
)

Write-Host "Building..."
.\gradlew.bat build

$aarPath = "cartoony/build/outputs/aar/cartoony-$Build.aar"
if (-Not (Test-Path $aarPath)) {
    Write-Error "AAR file not found: $aarPath"
    exit 1
}

Write-Host "Pushing to device..."
adb push $aarPath "/sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/Cartoony.aar"

Write-Host "Done! Restart CloudStream3 to test."
```

Usage:
```powershell
.\deploy.ps1 -Build "release"
```

---

## Troubleshooting Deployment Issues

### Issue 1: "Device not found"

```
error: no devices found
```

**Solution**:
```powershell
# Check USB connection
adb devices

# Authorize device
# Tap "Allow" on device screen when USB Debugging dialog appears

# Or specify device explicitly
adb -s <device-id> push ...
```

### Issue 2: "Permission denied"

```
adb: error: remote couldn't create file: Permission denied
```

**Solution**:
1. On device, go to Settings → Apps → CloudStream3
2. Grant "Storage" permission
3. Or push to /sdcard/Download first, then move:
   ```powershell
   adb push cartoony-release.aar /sdcard/Download/
   adb shell mv /sdcard/Download/cartoony-release.aar /sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/
   ```

### Issue 3: "Provider doesn't appear"

**Solutions**:
1. Check file is in correct location:
   ```powershell
   adb shell ls -la /sdcard/Android/data/com.lagradost.cloudstream3/files/extensions/
   ```
2. Verify filename ends in `.aar`
3. Try different filename: `Cartoony.aar` vs `cartoony.aar`
4. Restart CloudStream3 completely (force stop, then reopen)
5. Check CloudStream3 settings for extension folder location

### Issue 4: "Provider crashes on load"

**Solutions**:
1. Check logcat for exceptions:
   ```powershell
   adb logcat | grep Exception
   ```
2. Look for null pointer exceptions
3. Check CSS selector validity
4. Verify API endpoints exist

### Issue 5: "Can't connect to device"

```
error: device offline
```

**Solution**:
1. Unplug USB cable
2. Remove device from `adb devices`
   ```powershell
   adb disconnect <device>
   ```
3. Replug USB cable
4. Tap "Allow" on device screen
5. Verify: `adb devices` shows `device` not `offline`

---

## Distribution

### Share Provider

Once tested and working:

1. **Rename file**:
   ```powershell
   Copy-Item "cartoony/build/outputs/aar/cartoony-release.aar" "Cartoony.aar"
   ```

2. **Share via**:
   - Discord
   - GitHub Releases
   - Online storage
   - QR code

3. **Users install**:
   - Download `.aar` file
   - Copy to CloudStream3 extensions folder
   - Restart CloudStream3

---

## Next Steps

1. ✅ Deploy to CloudStream3
2. ✅ Test all features
3. ⏭️ Fix any issues found
4. ⏭️ Optimize performance
5. ⏭️ Share with community

---

## Additional Resources

- **ADB Install**: https://developer.android.com/tools/adb
- **CloudStream3**: https://github.com/LagradOst/CloudStream-3
- **Android Debug**: https://developer.android.com/studio/debug/logcat
- **Emulator Setup**: https://developer.android.com/studio/run/emulator

---

**Having deployment issues?**
- Check the Troubleshooting section above
- Review PROJECT_STATUS.md
- Check IMPLEMENTATION_GUIDE.md
- View logcat output for specific errors

