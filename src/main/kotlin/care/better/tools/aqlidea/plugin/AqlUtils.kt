package care.better.tools.aqlidea.plugin

import care.better.tools.aqlidea.plugin.settings.AqlServer
import care.better.tools.aqlidea.plugin.settings.AqlServersConfigurationService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import java.util.*

object AqlUtils {
    const val NOTIFICATION_GROUP = "AQL"
    val KEY_AQL_SERVER_ID = Key<String>(AqlServer::class.java.name + ".id")

    fun notifyError(e: Exception, project: Project?) {
        val n = Notification(
            NOTIFICATION_GROUP,
            NOTIFICATION_GROUP,
            e.toString(),
            NotificationType.ERROR
        )
        Notifications.Bus.notify(n, project)
    }

    fun notifyError(e: Exception, title: String, project: Project?) {
        val n = Notification(
            NOTIFICATION_GROUP,
            title,
            e.toString(),
            NotificationType.ERROR
        )
        Notifications.Bus.notify(n, project)
    }

    fun notify(title: String, text: String, type: NotificationType, project: Project?) {
        val n = Notification(
            NOTIFICATION_GROUP,
            title,
            text,
            type
        )
        Notifications.Bus.notify(n, project)
    }

    private val EXTRA_VALID_CHARS: BitSet = BitSet().setAll(" -=().,#$@")

    private fun BitSet.setAll(characters: String): BitSet {
        return setAll(*characters.toCharArray())
    }

    private fun BitSet.setAll(vararg characters: Char): BitSet {
        for (c in characters) set(c.toInt())
        return this
    }

    private fun isValidFilenameChar(c: Char): Boolean {
        if (Character.isJavaIdentifierPart(c)) return true
        return EXTRA_VALID_CHARS.get(c.toInt())
    }

    fun sanitizeFilename(filename: String): String {
        val result = StringBuilder(filename.length)
        val length = filename.length
        for (i in 0 until length) {
            val c = filename[i]
            if (isValidFilenameChar(c)) {
                result.append(c)
            } else {
                result.append("_")
            }
        }
        return result.toString()
    }


    fun aqlServerForFile(file: VirtualFile): AqlServer? {
        val aqlServers = AqlServersConfigurationService.INSTANCE.load()
        val aqlServerId = file.getUserData(KEY_AQL_SERVER_ID)
        return if (aqlServerId!=null) {
            aqlServers.servers.firstOrNull { it.id==aqlServerId }
        } else {
            aqlServers.defaultServer()
        }
    }

    fun parentPathContainsDir(parent: Path, dir: Path): Boolean {
        val absoluteParent = parent.toAbsolutePath()
        var path: Path? = dir.toAbsolutePath()
        while (path != null) {
            if (path == absoluteParent) return true
            path = path.parent
        }
        return false
    }



}