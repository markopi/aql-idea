package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.aql.LexedAql
import care.better.tools.aqlidea.aql.LexedAqls
import care.better.tools.aqlidea.aql.autocomplete.AqlAutocompletion
import care.better.tools.aqlidea.aql.autocomplete.AqlKeywordAutocompletionProvider
import care.better.tools.aqlidea.aql.autocomplete.AqlServerAutocompletionProvider
import care.better.tools.aqlidea.plugin.service.ThinkEhrClientService
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.ProcessingContext


class AqlCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(),
            AqlCompletionProvider
        )
    }

    object AqlCompletionProvider : CompletionProvider<CompletionParameters>() {


        public override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            if (parameters.originalFile.language != AqlLanguage) return
            val originalPosition = parameters.originalPosition ?: return

            val lexedAqls = LexedAqls.of(parameters.originalFile.text)
            val lexedAql = lexedAqls.parts.lastOrNull { it.offset <= originalPosition.startOffset } ?: return

            val aql = lexedAql.lexed.aql
            val aqlStartOffset = lexedAql.offset
            val offset = parameters.offset - aqlStartOffset
            val autocompletions = getAutocompletions(aql, offset, parameters.editor.project)
            if (autocompletions.isEmpty()) return
            val first = autocompletions.first()
            val prefix = parameters.editor.document.getText(TextRange(aqlStartOffset+first.start, aqlStartOffset+first.end))
            val originalText = parameters.editor.document.text

            val rs = resultSet.withPrefixMatcher(resultSet.prefixMatcher.cloneWithPrefix(prefix))

            for (ac in autocompletions) {
                when (ac) {
                    is AqlAutocompletion.Keyword -> {
                        rs.addElement(
                            LookupElementBuilder.create(ac.completion)
                                .withInsertHandler(AqlInsertHandler(originalText, aqlStartOffset, ac))
                        )
                    }
                    is AqlAutocompletion.Archetype -> {
                        rs.addElement(
                            LookupElementBuilder.create(ac.completion)
                                .withPresentableText(ac.archetypeId)
                                .withTailText(ac.name)
                                .withInsertHandler(AqlInsertHandler(originalText, aqlStartOffset, ac))
                        )
                    }
                    is AqlAutocompletion.Path -> {
                        var e =  LookupElementBuilder.create(ac.completion)
                            .withPresentableText(ac.path)
                            .withTypeText(ac.type)
                            .withTailText(ac.name)
                            .withLookupString(ac.path + " " + (ac.name ?: ""))
                            .withInsertHandler(AqlInsertHandler(originalText, aqlStartOffset, ac))

                        rs.addElement(e)
                    }
                }.run { }
            }


        }


        fun getAutocompletions(aql: String, cursorOffset: Int, project: Project?): List<AqlAutocompletion> {
            val lexedAql = LexedAql.of(aql, whitespaceAware = false)
            val result = mutableListOf<AqlAutocompletion>()
            val thinkEhr = ApplicationManager.getApplication().getService(ThinkEhrClientService::class.java)
            val server = project?.let { thinkEhr.getTarget(it) }
            if (server != null) {
                result += AqlServerAutocompletionProvider(thinkEhr.client)
                    .getAutocompletions(lexedAql, cursorOffset, server)
            }
            if (result.isEmpty()) {
                result += AqlKeywordAutocompletionProvider.getAutocompletions(lexedAql, cursorOffset)
            }

            return result
        }

    }

    // todo custom completion
    private class AqlInsertHandler(val originalText: String, val anchorOffset: Int, val ac: AqlAutocompletion) : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            // remove the text already inserted by idea
            context.document.replaceString(0, context.document.textLength, originalText)
//            context.document.deleteString(context.startOffset, context.tailOffset)

            context.document.replaceString(anchorOffset + ac.start, anchorOffset + ac.end, ac.completion)

            context.editor.caretModel.moveToOffset(anchorOffset + ac.start + ac.completion.length)
        }
    }
}