package care.better.tools.aqlidea.plugin.editor

import com.intellij.codeInsight.completion.PrefixMatcher

class AqlPrefixMatcher(prefix: String): PrefixMatcher(prefix) {
    override fun prefixMatches(name: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun cloneWithPrefix(prefix: String): PrefixMatcher {
        return AqlPrefixMatcher(prefix)
    }
}