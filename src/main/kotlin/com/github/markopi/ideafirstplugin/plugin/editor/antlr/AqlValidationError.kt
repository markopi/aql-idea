package com.github.markopi.ideafirstplugin.plugin.editor.antlr

data class AqlValidationError(
    val line: Int,
    val column: Int,
    val length: Int,
    val message: String,
    val tokenName: String?
) {
    override fun toString(): String {
        return if (line > 0) {
            "($line,$column) $message"
        } else {
            message
        }
    }
}