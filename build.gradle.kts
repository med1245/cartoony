// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.library") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.23" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Utility: compute SHA-256 of the built .cs3 files and update plugins.json
tasks.register("writePluginsJson") {
    dependsOn(":cartoony:makeCs3")
    doLast {
        val cs3Modern = file("cartoony/build/outputs/cs3/Cartoony.cs3")
        val cs3Legacy = file("cartoony/build/outputs/cs3/CartoonyLegacy.cs3")
        require(cs3Modern.exists()) { "Cartoony.cs3 not found at ${cs3Modern.absolutePath}, build it first." }
        fun sha256(f: java.io.File): String {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            f.inputStream().use { ins ->
                val buf = ByteArray(8192)
                var r: Int
                while (ins.read(buf).also { r = it } != -1) {
                    md.update(buf, 0, r)
                }
            }
            return md.digest().joinToString("") { "%02x".format(it) }
        }
        val hashModern = sha256(cs3Modern)
        val hashLegacy = if (cs3Legacy.exists()) sha256(cs3Legacy) else null
        val pluginsJson = file("plugins.json")
        val modernEntry = """
          {
            "name": "Cartoony",
            "version": 1,
            "authors": ["Cartoony"],
            "language": "ar",
            "url": "https://raw.githubusercontent.com/med1245/cartoony/builds/Cartoony.cs3",
            "sha256": "$hashModern",
            "iconUrl": "",
            "tvTypes": ["Anime", "AnimeMovie"],
            "description": "Cartoony.net anime provider"
          }
        """.trimIndent()
        val legacyEntry = hashLegacy?.let {
            """
          {
            "name": "Cartoony (Legacy)",
            "version": 1,
            "authors": ["Cartoony"],
            "language": "ar",
            "url": "https://raw.githubusercontent.com/med1245/cartoony/builds/CartoonyLegacy.cs3",
            "sha256": "$it",
            "iconUrl": "",
            "tvTypes": ["Anime", "AnimeMovie"],
            "description": "Cartoony.net anime provider (legacy)"
          }
            """.trimIndent()
        }
        val listContent = if (legacyEntry != null) {
            "[\n$modernEntry,\n$legacyEntry\n]"
        } else {
            "[\n$modernEntry\n]"
        }
        val content = listContent
        pluginsJson.writeText(content)
        println("plugins.json updated with modern sha256: $hashModern" + (hashLegacy?.let { " and legacy sha256: $it" } ?: ""))
    }
}
