package com.github.markopi.ideafirstplugin.aql

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class AqlAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        println("Annotating $element")
        holder.newAnnotation(HighlightSeverity.WARNING, "Hardcoded warning annotation")
            .range(TextRange(1, 4))
            .create()
    }
}