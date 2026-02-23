plugins {
    id("com.android.library")
    kotlin("android")
}

version = "1.0.0"

dependencies {
    implementation("com.lagradost:cloudstream3:4.11.4")
    implementation("org.jsoup:jsoup:1.15.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
}

android {
    compileSdk = 33
    namespace = "com.lagradost.cartoony"

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
        renderScript = false
        resValues = false
        shaders = false
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("ExtraTranslation")
    }
}
