package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.aql.LexedAqls
import care.better.tools.aqlidea.plugin.editor.antlr.AqlValidationError
import care.better.tools.aqlidea.plugin.editor.antlr.AqlValidations
import com.google.common.cache.CacheBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.util.elementType
import java.util.concurrent.TimeUnit

class AqlAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val type = element.elementType
        if (type !is IFileElementType || type.language != AqlLanguage) return

        val lexedAqls = LexedAqls.of(element.text)
        for (lexedAqlPart in lexedAqls.parts) {
            val errors = AqlValidations.validate(lexedAqlPart.lexed.aql)
            val aqlLines = splitLines(lexedAqlPart.lexed.aql)

            for (error in errors) {
                var index = error.toIndex(aqlLines)
                    .coerceAtMost(lexedAqlPart.lexed.aql.length - error.length)
                var length = error.length
                if (index < 0) {
                    length += index
                    index = 0
                }

                holder.newAnnotation(HighlightSeverity.ERROR, error.message)
                    .range(TextRange(lexedAqlPart.offset + index, lexedAqlPart.offset + index + length))
                    .create()
            }
        }
    }

    private fun AqlValidationError.toIndex(aqlLines: List<String>): Int {
        val lineStartPos = if (line > 1) {
            aqlLines.subList(0, line - 1).sumBy { it.length }
        } else 0

        return lineStartPos + column;
    }

    private fun splitLines(aql: String): List<String> {
        return aql.split(Regex("""(?<=\n\r?)""", RegexOption.MULTILINE))
    }

    private class ErrorWithOffset(val error: AqlValidationError, val offset: Int)
}