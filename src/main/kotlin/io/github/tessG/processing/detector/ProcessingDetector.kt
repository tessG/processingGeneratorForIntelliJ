package io.github.tessG.processing.detector

import java.nio.file.Path
import java.nio.file.Paths

object ProcessingDetector {

    sealed class DetectionResult {
        data object NotFound : DetectionResult()
        data class NotComplete(val home: Path) : DetectionResult()
        data class Found(val coreJar: Path, val version: String) : DetectionResult()
    }

    fun detect(): DetectionResult {
        val home = findProcessingHome() ?: return DetectionResult.NotFound
        val jar = findCoreJar(home) ?: return DetectionResult.NotComplete(home)
        return DetectionResult.Found(jar, readVersion(jar))
    }

    private fun findProcessingHome(): Path? {
        val candidates = when {
            isMac() -> listOf(
                Paths.get("/Applications/Processing.app/Contents/app"),
                Paths.get(System.getProperty("user.home"), "Applications/Processing.app/Contents/app")
            )
            isWindows() -> listOf(
                Paths.get("C:/Program Files/Processing"),
                Paths.get("C:/Program Files/Processing4"),
                Paths.get("C:/Program Files (x86)/Processing"),
                Paths.get(System.getProperty("user.home"), "AppData/Local/Processing"),
                Paths.get("C:/Processing")
            )
            else -> listOf(
                Paths.get("/usr/share/processing"),
                Paths.get("/usr/local/share/processing"),
                Paths.get(System.getProperty("user.home"), "processing"),
                Paths.get(System.getProperty("user.home"), ".local/share/processing"),
                Paths.get("/opt/processing")
            )
        }
        return candidates.firstOrNull { it.toFile().isDirectory }
    }



    private fun isMac() = System.getProperty("os.name").contains("Mac", ignoreCase = true)
    private fun isWindows() = System.getProperty("os.name").contains("Win", ignoreCase = true)
    fun findCoreJar(home: Path): Path? {
        return home.toFile()
            .walk()
            .maxDepth(2)
            .firstOrNull { it.name.startsWith("core-") && it.name.endsWith(".jar") }
            ?.toPath()
    }
    fun readVersion(jar: Path): String {
        val name = jar.fileName.toString()
        val match = Regex("""core-(\d+\.\d+\.\d+)""").find(name)
        return match?.groupValues?.get(1) ?: "4.0"
    }
}