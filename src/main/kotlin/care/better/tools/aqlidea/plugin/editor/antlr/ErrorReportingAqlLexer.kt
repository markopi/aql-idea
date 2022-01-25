package care.better.tools.aqlidea.plugin.editor.antlr

import com.marand.thinkehr.aql.antlr.AqlLexer
import org.antlr.runtime.CharStream
import org.antlr.runtime.RecognitionException
import org.antlr.runtime.RecognizerSharedState
import org.antlr.runtime.Token

class ErrorReportingAqlLexer(input: CharStream, state: RecognizerSharedState = RecognizerSharedState()) :
    AqlLexer(input, state) {

    val errors: MutableList<AqlValidationError> = ArrayList()
    private var peekedToken: Token? = null

    override fun displayRecognitionError(tokenNames: Array<String>?, e: RecognitionException) {
        val length = if (e.token != null) e.token.text.length else 1
        val token = if (e.token != null) e.token.text else null
        errors.add(AqlValidationError(e.line, e.charPositionInLine, length, getErrorMessage(e, tokenNames), token))
    }

    fun peekNextToken(): Token {
        peekedToken?.let { return it }
        val t = nextToken()
        peekedToken = t
        return t
    }

    override fun nextToken(): Token {
        if (peekedToken!=null) {
            val t = peekedToken
            peekedToken=null
            return t!!
        }
        return super.nextToken()
    }
}