package dev.wvr.mixinvisualizer.logic

import com.intellij.util.messages.Topic

interface MixinCompilationTopic {
    companion object {
        val TOPIC = Topic.create("Mixin Compilation Finished", MixinCompilationTopic::class.java)
    }

    fun onCompilationFinished()
}