pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://repo.recloudstream.org/releases") }
        maven { url = uri("https://repo.recloudstream.org/snapshots") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://repo.recloudstream.org/releases")
            metadataSources {
                mavenPom()
                artifact()
            }
            content {
                includeGroup("com.lagradost")
            }
        }
        maven {
            url = uri("https://repo.recloudstream.org/snapshots")
            metadataSources {
                mavenPom()
                artifact()
            }
            content {
                includeGroup("com.lagradost")
            }
        }
    }
}

rootProject.name = "CartoonyProvider"

include(":cartoony")
include(":cloudstream-stubs")
