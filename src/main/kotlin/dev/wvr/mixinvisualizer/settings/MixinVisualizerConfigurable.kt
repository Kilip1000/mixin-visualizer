package dev.wvr.mixinvisualizer.settings

import com.intellij.openapi.options.Configurable
import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class MixinVisualizerConfigurable : Configurable {
    private var autoCompileCheckbox: JCheckBox? = null

    override fun getDisplayName(): String = "Mixin Visualizer"

    override fun createComponent(): JComponent {
        val panel = JPanel(BorderLayout())
        autoCompileCheckbox = JCheckBox("Auto-compile project and refresh preview on file save")
        panel.add(autoCompileCheckbox!!, BorderLayout.NORTH)
        return panel
    }

    override fun isModified(): Boolean {
        return autoCompileCheckbox?.isSelected != MixinVisualizerSettings.instance.state.autoCompileOnSave
    }

    override fun apply() {
        MixinVisualizerSettings.instance.state.autoCompileOnSave = autoCompileCheckbox?.isSelected ?: false
    }

    override fun reset() {
        autoCompileCheckbox?.isSelected = MixinVisualizerSettings.instance.state.autoCompileOnSave
    }

    override fun disposeUIResources() {
        autoCompileCheckbox = null
    }
}