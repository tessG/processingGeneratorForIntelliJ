package io.github.tessG.processing.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.progress.Task
import io.github.tessG.processing.detector.ProcessingDetector
import io.github.tessG.processing.generator.ProjectGenerator
import io.github.tessG.processing.installer.CoreJarInstaller

import io.github.tessG.processing.ui.NewProcessingProjectDialog
import java.nio.file.Paths

class NewProcessingProjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val dialog = NewProcessingProjectDialog()
        if (!dialog.showAndGet()) return

        val processingHome = Paths.get(dialog.processingHomeField.text)
        val coreJar = ProcessingDetector.findCoreJar(processingHome)
            ?: run {
                Messages.showErrorDialog("core.jar ikke fundet i ${processingHome}", "Fejl")
                return
            }
        val version = ProcessingDetector.readVersion(coreJar)
        val installer = CoreJarInstaller()

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            e.project, "Installerer processing:core...", false
        ) {
            override fun run(indicator: ProgressIndicator) {
                if (!installer.isAlreadyInstalled(version)) {
                    indicator.text = "Installing processing:core $version into local Maven repo..."
                    indicator.isIndeterminate = true

                    when (val result = installer.install(coreJar, version)) {
                        is CoreJarInstaller.InstallResult.Success -> { /* fortsæt */ }
                        is CoreJarInstaller.InstallResult.Failure -> {
                            Messages.showErrorDialog(result.message, "Maven fejl")
                            return
                        }
                    }
                }

                indicator.text = "Generating project..."

                val config = ProjectGenerator.ProjectConfig(
                    projectRoot = Paths.get(dialog.locationField.text, dialog.projectNameField.text.trim()),
                    artifactId = dialog.projectNameField.text.trim(),
                    groupId = dialog.groupIdField.text.trim(),
                    mainClassName = dialog.projectNameField.text.trim()
                        .split("-")
                        .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) },
                    processingVersion = version
                )

                ProjectGenerator.generate(config)

                indicator.text = "Opening project..."

                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    val alreadyOpen = com.intellij.openapi.project.ProjectManager.getInstance()
                        .openProjects.any { it.basePath == config.projectRoot.toString() }

                    if (!alreadyOpen) {
                        val project = com.intellij.openapi.project.ProjectManager.getInstance()
                            .loadAndOpenProject(config.projectRoot.toString()) ?: return@invokeLater

                        val sketchFile = config.projectRoot
                            .resolve("src/main/java/${config.groupId.replace('.', '/')}")
                            .resolve("${config.mainClassName}.java")
                            .toFile()

                        val vFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                            .refreshAndFindFileByIoFile(sketchFile)

                        if (vFile != null) {
                            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project!!)
                                .openFile(vFile)
                        }
                    }
                }
            }
        })
    }

}
