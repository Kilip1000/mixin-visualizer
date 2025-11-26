package dev.wvr.mixinvisualizer.logic

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompileStatusNotification
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile
import dev.wvr.mixinvisualizer.settings.MixinVisualizerSettings

class AutoCompileListener : FileDocumentManagerListener {
    override fun beforeDocumentSaving(document: Document) {
        if (!MixinVisualizerSettings.instance.state.autoCompileOnSave) return

        val file = FileDocumentManager.getInstance().getFile(document) ?: return
        if (!isRelevantFile(file)) return

        for (project in ProjectManager.getInstance().openProjects) {
            val module = ModuleUtil.findModuleForFile(file, project)
            if (module != null) {
                ApplicationManager.getApplication().invokeLater {
                    if (!project.isDisposed) {
                        CompilerManager.getInstance(project)
                            .compile(module, CompileStatusNotification { aborted, errors, _, _ ->
                                if (!aborted && errors == 0) {
                                    project.messageBus.syncPublisher(MixinCompilationTopic.TOPIC)
                                        .onCompilationFinished()
                                }
                            })
                    }
                }
                break
            }
        }
    }

    private fun isRelevantFile(file: VirtualFile): Boolean {
        return file.extension == "java" || file.extension == "kt"
    }
}