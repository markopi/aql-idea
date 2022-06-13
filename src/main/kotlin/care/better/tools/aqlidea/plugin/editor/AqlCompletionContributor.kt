package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.aql.LexedAql
import care.better.tools.aqlidea.aql.LexedAqls
import care.better.tools.aqlidea.aql.autocomplete.AqlAutocompletion
import care.better.tools.aqlidea.aql.autocomplete.AqlKeywordAutocompletionProvider
import care.better.tools.aqlidea.aql.autocomplete.AqlServerAutocompletionProvider
import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.service.ThinkEhrClientService
import care.better.tools.aqlidea.plugin.settings.AqlServer
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ApplicationManager
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
            val aqlServer = AqlUtils.aqlServerForFile(parameters.originalFile.virtualFile)

            val lexedAqls = LexedAqls.of(parameters.originalFile.text)
            val lexedAql = lexedAqls.parts.lastOrNull { it.offset <= originalPosition.startOffset } ?: return

            val aql = lexedAql.lexed.aql
            val aqlStartOffset = lexedAql.offset
            val offset = parameters.offset - aqlStartOffset
            val autocompletions = getAutocompletions(aqlServer, aql, offset)
            if (autocompletions.isEmpty()) return
            val originalText = parameters.editor.document.text

            for (ac in autocompletions) {
                when (ac) {
                    is AqlAutocompletion.Keyword -> {
                        resultSet.addElement(
                            LookupElementBuilder.create(ac.completion)
//                                .withInsertHandler(AqlInsertHandler(originalText, aqlStartOffset, ac))
                        )
                    }
                    is AqlAutocompletion.Archetype -> {
                        resultSet.addElement(
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

                        resultSet.addElement(e)
                    }
                }.run { }
            }

        }

        fun getAutocompletions(aqlServer: AqlServer?, aql: String, cursorOffset: Int): List<AqlAutocompletion> {
            val lexedAql = LexedAql.of(aql, whitespaceAware = false)
            val result = mutableListOf<AqlAutocompletion>()
            val thinkEhr = ApplicationManager.getApplication().getService(ThinkEhrClientService::class.java)
            if (aqlServer!=null) {
                val target = thinkEhr.toThinkEhrTarget(aqlServer)
                result += AqlServerAutocompletionProvider(thinkEhr.client)
                    .getAutocompletions(lexedAql, cursorOffset, target)
            }
            if (result.isEmpty()) {
                result += AqlKeywordAutocompletionProvider.getAutocompletions(lexedAql, cursorOffset)
            }

            return result
        }

    }

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