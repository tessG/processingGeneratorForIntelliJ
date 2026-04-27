package io.github.tessG.processing.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import io.github.tessG.processing.detector.ProcessingDetector
import java.nio.file.Paths
import javax.swing.JComponent

class NewProcessingProjectDialog : DialogWrapper(true) {
    val processingHomeField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            "Select Processing Installation", null, null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
    }
    val projectNameField = JBTextField("my-sketch")
    val groupIdField = JBTextField("io.github.tessG")
    val locationField = JBTextField(
        Paths.get(System.getProperty("user.home"), "IdeaProjects").toString()
    )



    init {
        title = "New Processing Project"
        when (val result = ProcessingDetector.detect()) {
            is ProcessingDetector.DetectionResult.Found ->
                processingHomeField.text = result.coreJar.parent.toString()
            is ProcessingDetector.DetectionResult.NotComplete ->
                processingHomeField.text = result.home.toString()
            is ProcessingDetector.DetectionResult.NotFound ->
                processingHomeField.text = ""
        }
        init()
        //        ProcessingDetector.detect() og sæt processingHomeField.text hvis resultatet er Found eller NotComplete.


    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Project name:", projectNameField)
            .addLabeledComponent("Location:", locationField)
            .addLabeledComponent("Group ID:", groupIdField)
            .addLabeledComponent("Processing home:", processingHomeField)
            .panel
    }

    override fun doValidate(): ValidationInfo? {
        if (projectNameField.text.isBlank())
            return ValidationInfo("Project name is required", projectNameField)

        if (groupIdField.text.isBlank() || !groupIdField.text.contains('.'))
            return ValidationInfo("Group ID must be a valid Java package (e.g. io.github.tessG)", groupIdField)

        if (locationField.text.isBlank() || !Paths.get(locationField.text).toFile().isDirectory)
            return ValidationInfo("Location must be a valid directory", locationField)

        if (processingHomeField.text.isBlank())
            return ValidationInfo("Processing home is required", processingHomeField)

        if (ProcessingDetector.findCoreJar(Paths.get(processingHomeField.text)) == null)
            return ValidationInfo("core.jar not found in this Processing installation", processingHomeField)

        return null
    }
}