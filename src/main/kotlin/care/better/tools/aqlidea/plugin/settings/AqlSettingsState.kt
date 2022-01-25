package care.better.tools.aqlidea.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


@State(
    name = "care.better.tools.aqlidea.plugin.settings.AqlSettingsState",
    storages = [Storage("AqlPlugin.xml")]
)
class AqlSettingsState : PersistentStateComponent<AqlSettingsState> {
    var serverUrl: String = ""
    var loginUsername: String = ""
    var loginPassword: String = ""


    override fun getState(): AqlSettingsState {
        return this
    }

    override fun loadState(state: AqlSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val INSTANCE: AqlSettingsState
            get() {
                return ApplicationManager.getApplication().getService(AqlSettingsState::class.java)
            }

    }
}