package care.better.tools.aqlidea.plugin.toolWindow

import org.junit.Test
import kotlin.test.assertEquals


class AqlEditDataSourcesDialogPanelTest {
    @Test
    fun extractBaseNameAndIndex() {
        assertEquals("name" to 0, AqlEditDataSourcesDialogPanel.extractBaseNameAndIndex("name"))
        assertEquals("name" to 0, AqlEditDataSourcesDialogPanel.extractBaseNameAndIndex("name "))
        assertEquals("name" to 1, AqlEditDataSourcesDialogPanel.extractBaseNameAndIndex("name (1)"))
    }
}