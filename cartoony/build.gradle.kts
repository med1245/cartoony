plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.Properties

version = "1.0.0"

dependencies {
    compileOnly(project(":cloudstream-stubs"))
    implementation("org.jsoup:jsoup:1.15.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
}

android {
    compileSdk = 33
    namespace = "com.lagradost.cartoony"
    buildToolsVersion = "34.0.0"

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        aidl = false
        resValues = false
        shaders = false
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("ExtraTranslation")
    }
}

// --- Packaging: Build .cs3 from AAR (stub mode) ---
val aarName = "cartoony-debug.aar"
val unpackDir = layout.buildDirectory.dir("tmp/cs3/unpack")
val pluginTmp = layout.buildDirectory.file("tmp/cs3/plugin.json")
val dexOutDir = layout.buildDirectory.dir("tmp/cs3/dex")
val cs3OutDir = layout.buildDirectory.dir("outputs/cs3")

tasks.register<Copy>("unpackAarForCs3") {
    dependsOn("assembleDebug")
    from(zipTree(layout.buildDirectory.file("outputs/aar/$aarName")))
    into(unpackDir)
}

fun getSdkDir(): File {
    // Resolve Android SDK directory from local.properties or env
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        val props = Properties()
        propsFile.inputStream().use { props.load(it) }
        val sdk = props.getProperty("sdk.dir") ?: props.getProperty("android.sdk.path")
        if (!sdk.isNullOrBlank()) return File(sdk)
    }
    val env = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
    if (!env.isNullOrBlank()) return File(env)
    throw GradleException("Android SDK not found. Set sdk.dir in local.properties or ANDROID_SDK_ROOT env var.")
}

// Convert classes.jar -> classes.dex using D8
tasks.register<Exec>("dexForCs3") {
    dependsOn("unpackAarForCs3")
    doFirst {
        dexOutDir.get().asFile.mkdirs()
    }
    val sdkDir = getSdkDir()
    val isWin = System.getProperty("os.name")?.lowercase()?.contains("win") == true
    val d8Name = if (isWin) "d8.bat" else "d8"
    val d8 = File(File(sdkDir, "build-tools/${android.buildToolsVersion}"), d8Name)
    val classesJar = unpackDir.get().file("classes.jar").asFile
    if (!d8.exists()) {
        throw GradleException("D8 not found at ${d8.absolutePath}. Check buildToolsVersion or SDK setup.")
    }
    commandLine(d8.absolutePath, classesJar.absolutePath, "--output", dexOutDir.get().asFile.absolutePath)
}

tasks.register("makeCs3") {
    dependsOn("dexForCs3")
    doLast {
        val pluginJson = pluginTmp.get().asFile
        pluginJson.parentFile.mkdirs()
        pluginJson.writeText(
            """
            {
              "name": "Cartoony",
              "className": "com.lagradost.CartoonyPlugin",
              "description": "Cartoony.net anime provider",
              "version": 1,
              "minApi": 3,
              "targetApi": 3,
              "authors": ["Cartoony"],
              "iconUrl": "",
              "repoUrl": "",
              "language": "ar",
              "lang": "ar",
              "tvTypes": ["Anime","AnimeMovie"]
            }
            """.trimIndent()
        )
        val outDir = cs3OutDir.get().asFile
        outDir.mkdirs()
        val outFile = outDir.resolve("Cartoony.cs3")
        ZipOutputStream(outFile.outputStream()).use { zos ->
            fun addFileToZip(pathInZip: String, file: java.io.File) {
                zos.putNextEntry(ZipEntry(pathInZip))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
            val classesDex = dexOutDir.get().file("classes.dex").asFile
            addFileToZip("classes.dex", classesDex)
            addFileToZip("plugin.json", pluginJson)
        }
    }
}

// CloudStream-like convenience tasks
tasks.register("make") {
    group = "cloudstream"
    description = "Builds Cartoony.cs3 (CloudStream plugin bundle)"
    dependsOn("makeCs3")
}

tasks.register("deployWithAdb") {
    group = "cloudstream"
    description = "Builds and pushes Cartoony.cs3 to /sdcard/Download via ADB"
    dependsOn("makeCs3")
    doLast {
        val cs3 = cs3OutDir.get().file("Cartoony.cs3").asFile
        val adbCmd = System.getenv("ADB") ?: "adb"
        try {
            exec {
                isIgnoreExitValue = true
                commandLine(adbCmd, "push", cs3.absolutePath, "/sdcard/Download/Cartoony.cs3")
            }
            println("Pushed to /sdcard/Download/Cartoony.cs3 (if a device was connected).")
        } catch (_: Exception) {
            println("ADB not available. Manually copy ${cs3.absolutePath} to your device.")
        }
        println("Install in CloudStream: Settings → Extensions → Install from storage → select Cartoony.cs3")
    }
}
