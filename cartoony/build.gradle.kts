plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
val cs3OutDir = layout.buildDirectory.dir("outputs/cs3")

tasks.register<Copy>("unpackAarForCs3") {
    dependsOn("assembleDebug")
    from(zipTree(layout.buildDirectory.file("outputs/aar/$aarName")))
    into(unpackDir)
}

tasks.register("makeCs3") {
    dependsOn("unpackAarForCs3")
    doLast {
        val pluginJson = pluginTmp.get().asFile
        pluginJson.parentFile.mkdirs()
        pluginJson.writeText(
            """
            {
              "name": "Cartoony",
              "className": "com.lagradost.CartoonyProvider",
              "description": "Cartoony.net anime provider",
              "version": 1,
              "minApi": 3,
              "targetApi": 3,
              "authors": ["Cartoony"],
              "iconUrl": "",
              "repoUrl": "",
              "lang": "ar",
              "tvTypes": ["Anime","AnimeMovie"]
            }
            """.trimIndent()
        )
        val classesJar = unpackDir.get().file("classes.jar").asFile
        val outDir = cs3OutDir.get().asFile
        outDir.mkdirs()
        val outFile = outDir.resolve("Cartoony.cs3")
        ZipOutputStream(outFile.outputStream()).use { zos ->
            fun addFileToZip(pathInZip: String, file: java.io.File) {
                zos.putNextEntry(ZipEntry(pathInZip))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
            addFileToZip("classes.jar", classesJar)
            addFileToZip("plugin.json", pluginJson)
        }
    }
}
