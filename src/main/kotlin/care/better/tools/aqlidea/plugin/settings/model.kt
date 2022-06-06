package care.better.tools.aqlidea.plugin.settings

import java.time.Instant

data class AqlServersConfiguration(val servers: MutableList<AqlServer>) {

    fun cleanDefaults() {
        val default = defaultServer()
        for (server in servers) {
            server.default = server === default
        }
    }
    fun defaultServer(): AqlServer? {
        return servers.firstOrNull { it.default } ?: servers.firstOrNull()
    }
}

data class AqlServer(
    var id: String,
    var name: String,
    var serverUrl: String,
    var username: String,
    var password: String,
    var default: Boolean
)

data class AqlServerConsoleHistory(val history: ArrayDeque<Item>) {
    data class Item(val timestamp: Long, val aql: String)
}