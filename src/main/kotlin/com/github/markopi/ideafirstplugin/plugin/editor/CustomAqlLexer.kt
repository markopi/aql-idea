package com.github.markopi.ideafirstplugin.plugin.editor

import com.intellij.ide.highlighter.custom.CustomFileTypeLexer
import com.intellij.ide.highlighter.custom.SyntaxTable
import com.intellij.lexer.DelegateLexer

class CustomAqlLexer : DelegateLexer(buildCustomLexer()) {

    companion object {
        fun buildCustomLexer(): CustomFileTypeLexer {
            val syntaxTable = SyntaxTable()
            syntaxTable.numPostfixChars=""
            syntaxTable.isHasBraces = true
            syntaxTable.isHasBraces = true
            syntaxTable.isHasStringEscapes = true
            syntaxTable.isIgnoreCase = true
            syntaxTable.keywords1.addAll(
                setOf(
                    "all_versions", "and", "as", "asc", "ascending", "contains", "distinct", "desc", "descending",
                    "exists", "from", "group", "by", "having", "like", "not", "null", "offset", "limit", "or", "order",
                    "select", "top", "union", "where", "xor"
                )
            )
            syntaxTable.keywords2.addAll(
                setOf(
                    "ehr", "version", "versioned_object",
                    "composition", "observation", "evaluation", "section", "cluster", "admin_entry",
                    "element", "instruction", "action"
                )
            )
            syntaxTable.lineComment = "--"

            return CustomFileTypeLexer(syntaxTable, true)
        }
    }
}