package io.github.tessG.processing.installer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler

class CoreJarInstaller {

    sealed class InstallResult {
        data object Success : InstallResult()
        data class Failure(val message: String) : InstallResult()
    }

    fun install(coreJar: Path, version: String): InstallResult {
        val mvn = findMvn() ?: return InstallResult.Failure(
            "Maven (mvn) was not found on your system.\n\n" +
                    "Please install Maven and make sure it is on your PATH:\n" +
                    "https://maven.apache.org/install.html"
        )
        val cmd = GeneralCommandLine(
            mvn,
            "install:install-file",
            "-Dfile=${coreJar.toAbsolutePath()}",
            "-DgroupId=org.processing",
            "-DartifactId=core",
            "-Dversion=$version",
            "-Dpackaging=jar",
            "-DgeneratePom=true"
        )

        val handler = CapturingProcessHandler(cmd)
        val output = handler.runProcess(60_000)

        return if (output.exitCode == 0) {
            InstallResult.Success
        } else {
            InstallResult.Failure(output.stderr)
        }
    }

    private fun findMvn(): String? {
        val isWindows = System.getProperty("os.name").contains("Win", ignoreCase = true)
        val name = if (isWindows) "mvn.cmd" else "mvn"

        // 1. Tjek PATH
        val pathEnv = System.getenv("PATH") ?: ""
        val separator = if (isWindows) ";" else ":"
        for (dir in pathEnv.split(separator)) {
            val candidate = Paths.get(dir, name)
            if (Files.isExecutable(candidate)) return candidate.toString()
        }

        // 2. Kendte steder på macOS
        val extras = listOf(
            "/usr/local/bin/mvn",
            "/opt/homebrew/bin/mvn",
            "/usr/bin/mvn"
        )
        return extras.map(Paths::get).firstOrNull(Files::isExecutable)?.toString()
    }
    fun isAlreadyInstalled(version: String): Boolean {
        val jar = Paths.get(
            System.getProperty("user.home"),
            ".m2", "repository", "org", "processing", "core", version, "core-$version.jar"
        )
        return Files.isRegularFile(jar)
    }
}