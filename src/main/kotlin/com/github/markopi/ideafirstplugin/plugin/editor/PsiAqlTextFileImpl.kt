package com.github.markopi.ideafirstplugin.plugin.editor

import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry

open class PsiAqlTextFileImpl(viewProvider: FileViewProvider) :
    PsiFileImpl(AqlTextTokenTypes.AQL_FILE, AqlTextTokenTypes.AQL_FILE, viewProvider),
//    PsiFileImpl(viewProvider),
    PsiPlainTextFile, HintedReferenceHost {
    private val myFileType: FileType
    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitPlainTextFile(this)
    }

    override fun toString(): String {
        return "PsiFile(aql text):$name"
    }

    override fun getFileType(): FileType {
        return myFileType
    }

    override fun getReferences(): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this)
    }

    override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this, hints)
    }

    override fun shouldAskParentForReferences(hints: PsiReferenceService.Hints): Boolean {
        return false
    }

    init {
        myFileType =
            if (viewProvider.baseLanguage !== AqlLanguage) AqlFileType.INSTANCE else viewProvider.fileType
    }
}