package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.plugin.editor.antlr.AqlValidationError
import care.better.tools.aqlidea.plugin.editor.antlr.AqlValidations
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class AqlAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val aql = element.text
        val errors = AqlValidations.validate(aql)
        val aqlLines = splitLines(aql)

        for (error in errors) {
            var index = error.toIndex(aqlLines)
                .coerceAtMost(aql.length - error.length)
            var length = error.length
            if (index < 0) {
                length += index
                index = 0
            }

            holder.newAnnotation(HighlightSeverity.ERROR, error.message)
                .range(TextRange(index, index + length))
                .create()

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
}