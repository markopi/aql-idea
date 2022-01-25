package care.better.tools.aqlidea.plugin.editor.antlr

import com.marand.thinkehr.aql.antlr.AqlParser
import org.antlr.runtime.RecognitionException
import org.antlr.runtime.RecognizerSharedState
import org.antlr.runtime.TokenStream

class ErrorReportingAqlParser(input: TokenStream, state: RecognizerSharedState = RecognizerSharedState()) :
    AqlParser(input, state) {

    val errors: MutableList<AqlValidationError> = ArrayList()

    override fun displayRecognitionError(tokenNames: Array<String>, e: RecognitionException) {
        val length = if (e.token != null) e.token.text.length else 1
        val token = if (e.token != null) e.token.text else null
        errors.add(AqlValidationError(e.line, e.charPositionInLine, length, getErrorMessage(e, tokenNames), token))
    }
}