// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.library") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.23" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Utility: compute SHA-256 of the built .cs3 and update plugins.json
tasks.register("writePluginsJson") {
    dependsOn(":cartoony:makeCs3")
    doLast {
        val cs3File = file("cartoony/build/outputs/cs3/Cartoony.cs3")
        require(cs3File.exists()) { "Cartoony.cs3 not found at ${cs3File.absolutePath}, build it first." }
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val hash = cs3File.inputStream().use { ins ->
            val buf = ByteArray(8192)
            var r: Int
            while (ins.read(buf).also { r = it } != -1) {
                md.update(buf, 0, r)
            }
            md.digest().joinToString("") { "%02x".format(it) }
        }
        val pluginsJson = file("plugins.json")
        val content = """
            [
              {
                "name": "Cartoony",
                "version": 1,
                "authors": ["Cartoony"],
                "language": "ar",
                "url": "https://raw.githubusercontent.com/med1245/cartoony/main/cartoony/build/outputs/cs3/Cartoony.cs3",
                "sha256": "$hash",
                "iconUrl": "",
                "tvTypes": ["Anime", "AnimeMovie"],
                "description": "Cartoony.net anime provider"
              }
            ]
        """.trimIndent()
        pluginsJson.writeText(content)
        println("plugins.json updated with sha256: $hash")
    }
}
