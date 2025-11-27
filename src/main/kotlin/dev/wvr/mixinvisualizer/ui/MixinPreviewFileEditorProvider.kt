package dev.wvr.mixinvisualizer.ui

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

class MixinPreviewFileEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        if (file.extension != "java" && file.extension != "kt") return false
        val text = getFileText(file) ?: return false

        return text.contains("@Mixin") || text.contains("org.spongepowered.asm.mixin")
    }

    private fun getFileText(file: VirtualFile): CharSequence? {
        val document = FileDocumentManager.getInstance().getCachedDocument(file)
        if (document != null) {
            return document.charsSequence
        }

        return try {
            VfsUtilCore.loadText(file)
        } catch (_: Exception) {
            null
        }
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return MixinPreviewEditor(project, file)
    }

    override fun getEditorTypeId() = "mixin-vis-editor"
    override fun getPolicy() = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}