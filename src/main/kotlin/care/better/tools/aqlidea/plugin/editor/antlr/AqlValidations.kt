package care.better.tools.aqlidea.plugin.editor.antlr

import org.antlr.runtime.CommonTokenStream

object AqlValidations {
    fun validate(aql: String): List<AqlValidationError> {
        val lexer = ErrorReportingAqlLexer(ANTLRUpperCaseStringStream(aql))
        val parser = ErrorReportingAqlParser(CommonTokenStream(lexer))
        parser.queries()

        return lexer.errors + parser.errors
    }
}