package care.better.tools.aqlidea.plugin.settings

import care.better.tools.aqlidea.plugin.toolWindow.servers.AqlServer
import care.better.tools.aqlidea.plugin.toolWindow.servers.AqlServersConfiguration
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection

@State(
    name = "care.better.tools.aqlidea.plugin.settings.AqlServersPersistentState",
    storages = [Storage("AqlServers.xml")]
)
class AqlServersPersistentState : PersistentStateComponent<AqlServersPersistentState> {
    @XCollection(propertyElementName = "Server", style = XCollection.Style.v2)
    private var servers: List<Server> = listOf()

    override fun getState(): AqlServersPersistentState {
        return this
    }

    override fun loadState(state: AqlServersPersistentState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun readState(): AqlServersConfiguration {
        return AqlServersConfiguration(
            servers = servers.mapTo(mutableListOf()) { it.toAqlServer() })
    }

    private fun AqlServer.toServer() = Server(
        id = id,
        name = name,
        serverUrl = serverUrl,
        loginUsername = username,
        loginPassword = password,
        default = default
    )

    private fun Server.toAqlServer() = AqlServer(
        id = id,
        name = name,
        serverUrl = serverUrl,
        username = loginUsername,
        password = loginPassword,
        default = default
    )

    fun writeState(state: AqlServersConfiguration) {
        state.cleanDefaults()
        servers = state.servers.map { it.toServer() }
        AqlPluginHomeDir.cleanup(state)

    }

    private data class Server(
        var id: String = "",
        var name: String = "",
        var serverUrl: String = "",
        var loginUsername: String = "",
        var loginPassword: String = "",
        var default: Boolean = false
    )

    companion object {
        fun getService(): AqlServersPersistentState {
            return ApplicationManager.getApplication().getService(AqlServersPersistentState::class.java)
        }
    }
}