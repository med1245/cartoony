// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    kotlin("jvm") version "1.8.10" apply false
    id("com.android.library") version "7.4.2" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
