package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.plugin.editor.AqlCompletionContributor.AqlCompletionProvider.PositionInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase


internal class AqlCompletionProviderTest : BasePlatformTestCase() {
    val completionProvider = AqlCompletionContributor.AqlCompletionProvider()

    fun testPositionInfoClause() {
        val aql = "select c from ehr e contains composition c where 1=1 limit 10"

        assertEquals("start", PositionInfo(null, null), completionProvider.getPositionInfo(aql, 0))
        assertEquals("mid select", PositionInfo(null, null), completionProvider.getPositionInfo(aql, 4))
        assertEquals("after select", PositionInfo("select", "select"), completionProvider.getPositionInfo(aql, 7))
        assertEquals("after from", PositionInfo("from", "from"), completionProvider.getPositionInfo(aql, 14))
        assertEquals("after contains", PositionInfo("from", "contains"), completionProvider.getPositionInfo(aql, 28))
        assertEquals("at c ", PositionInfo("from", "contains"), completionProvider.getPositionInfo(aql, 41))
        assertEquals("after where", PositionInfo("where", "where"), completionProvider.getPositionInfo(aql, 50))
    }

}
