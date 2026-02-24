# Environment Setup Guide - Cartoony CloudStream Provider

## Prerequisites

Before you can build this CloudStream3 extension, you need to set up your development environment.

### Required Software

1. **Java Development Kit (JDK) 11 or higher**
2. **Android SDK (if building with Android Studio)**
3. **Git** (for version control)

---

## Step 1: Install Java (JDK)

### Option A: Using Windows (Recommended - Adoptium OpenJDK)

1. **Download JDK 21 (Long-Term Support)**
   - Visit: https://adoptium.net/
   - Click "Download LTS" (Latest JDK 21)
   - Select Windows x64 version
   - Choose ".msi installer" format

2. **Install JDK**
   - Run the `.msi` installer
   - Follow installation wizard
   - **Important**: When asked to "Set JAVA_HOME variable" → **CHECK THIS OPTION**
   - Complete installation

3. **Verify Installation**
   ```powershell
   java -version
   javac -version
   ```
   
   Should show output like:
   ```
   openjdk version "21.0.1" 2023-10-17
   OpenJDK Runtime Environment (build 21.0.1+12-39)
   ```

### Option B: Manual JAVA_HOME Setup (if auto-setup didn't work)

If Java is installed but JAVA_HOME isn't set:

1. **Find your Java installation**
   ```powershell
   # Default location for Adoptium OpenJDK
   C:\Program Files\Eclipse Adoptium\jdk-21.0.1+12
   ```

2. **Set JAVA_HOME Environment Variable**
   - Press `Win + X` → "System"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Click "New..." (under System variables)
   - **Variable name**: `JAVA_HOME`
   - **Variable value**: `C:\Program Files\Eclipse Adoptium\jdk-21.0.1+12` (adjust version if different)
   - Click OK, then OK again
   - **Restart PowerShell/Command Prompt**

3. **Verify**
   ```powershell
   $env:JAVA_HOME
   java -version
   ```

---

## Step 2: Verify Android SDK (Optional but Recommended)

If you want to use Android Studio for building:

### Install Android Studio

1. Download from: https://developer.android.com/studio
2. Run installer and follow wizard
3. During setup, install:
   - Android SDK
   - SDK Platform Tools
   - Build Tools 33.0.0 or higher

### Set ANDROID_HOME

1. **Find Android SDK location**
   - Usually: `C:\Users\{YourUsername}\AppData\Local\Android\Sdk`

2. **Set Environment Variable**
   - Press `Win + X` → "System"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Click "New..." (under System variables)
   - **Variable name**: `ANDROID_HOME`
   - **Variable value**: `C:\Users\{YourUsername}\AppData\Local\Android\Sdk`
   - Click OK, OK, OK
   - **Restart PowerShell/Command Prompt**

3. **Create local.properties**
   ```bash
   cd C:\Users\MEHDI MARSAMAN\Documents\cartoony
   echo "sdk.dir=C:\Users\{YourUsername}\AppData\Local\Android\Sdk" > local.properties
   ```

---

## Step 3: Install Git (Optional but Recommended)

1. Download from: https://git-scm.com/download/win
2. Run installer and follow wizard
3. Use default settings recommended
4. Restart terminal after install

Verify:
```powershell
git --version
```

---

## Step 4: Test Your Setup

### Quick Test

```powershell
# Navigate to project
cd "C:\Users\MEHDI MARSAMAN\Documents\cartoony"

# Test Gradle without building
.\gradlew.bat --version
```

Should show:
```
Gradle 7.6.2
```

### Full Build Test

```powershell
# Clean and build
cd "C:\Users\MEHDI MARSAMAN\Documents\cartoony"
.\gradlew.bat clean build
```

**First build will take 2-5 minutes** (downloads dependencies)

---

## Troubleshooting

### Issue: "JAVA_HOME is not set"

**Solution 1**: Install Java with "Set JAVA_HOME" option checked
**Solution 2**: Manually set JAVA_HOME environment variable

```powershell
# Temporary workaround (this session only)
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.1+12"
java -version
```

### Issue: "Failed to find SDK Build Tools"

**Solution**: Update Android SDK Build Tools

1. Open Android Studio
2. Go to: SDK Manager (Tools → SDK Manager)
3. Click "SDK Tools" tab
4. Check "Android SDK Build-Tools 33.0.0" or higher
5. Click "Apply" and "OK"

### Issue: "Gradle not found"

**Solution**: Ensure you're using the correct path:

```powershell
# Windows (use .bat file)
.\gradlew.bat build

# NOT: ./gradlew build (that's for Linux/Mac)
```

### Issue: "Out of memory" during build

**Solution**: Increase Gradle heap memory

Edit or create `gradle.properties` in project root:
```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m
```

### Issue: Build fails with "Unsupported class-file format"

**Solution**: JDK version mismatch

```powershell
# Check version
java -version

# Should be 11 or higher
# If lower, download JDK 21 from https://adoptium.net/
```

---

## Complete Setup Verification Checklist

Run these commands and verify output:

```powershell
# 1. Java Version (should be 11+)
java -version
# Expected: openjdk version "21.0.1" or similar

# 2. Gradle Version
.\gradlew.bat --version
# Expected: Gradle 7.6.2

# 3. Navigate to project
cd "C:\Users\MEHDI MARSAMAN\Documents\cartoony"

# 4. Check project structure
dir src\main\kotlin\com\lagradost\
# Expected: CartoonyProvider.kt file

# 5. List Gradle tasks
.\gradlew.bat tasks
# Expected: Long list of available tasks
```

---

## Ready to Build!

Once all checks pass, you can build the extension:

```powershell
cd "C:\Users\MEHDI MARSAMAN\Documents\cartoony"
.\gradlew.bat build
```

**Output location**: `cartoony\build\outputs\aar\cartoony-release.aar`

---

## Next Steps

1. ✅ Set up Java
2. ✅ Verify Gradle works
3. ⏭️ Run `./gradlew build`
4. ⏭️ See BUILD_GUIDE.md for next steps

---

## Additional Resources

- **JDK Download**: https://adoptium.net/
- **Android SDK**: https://developer.android.com/studio
- **Gradle Documentation**: https://gradle.org/guides/getting-started/
- **CloudStream3**: https://github.com/LagradOst/CloudStream-3

---

**Questions?** Check PROJECT_STATUS.md or IMPLEMENTATION_GUIDE.md

