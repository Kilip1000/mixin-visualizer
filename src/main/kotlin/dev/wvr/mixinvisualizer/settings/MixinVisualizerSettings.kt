package dev.wvr.mixinvisualizer.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "dev.wvr.mixinvisualizer.settings.MixinVisualizerSettings",
    storages = [Storage("MixinVisualizerSettings.xml")]
)
@Service(Service.Level.APP)
class MixinVisualizerSettings : PersistentStateComponent<MixinVisualizerSettings.State> {

    data class State(
        var autoCompileOnSave: Boolean = false
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        val instance: MixinVisualizerSettings
            get() = ApplicationManager.getApplication().getService(MixinVisualizerSettings::class.java)
    }
}