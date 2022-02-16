package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.aql.CustomAqlLexer
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.json.JsonElementTypes
import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.EmptyLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiUtilCore

class AqlTextParserDefinition : ParserDefinition {
    override fun createLexer(project: Project): Lexer {
        return CustomAqlLexer()
    }

    override fun createParser(project: Project): PsiParser {
        return  AqlSimpleParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return AqlTextTokenTypes.AQL_FILE
    }

    override fun getWhitespaceTokens(): TokenSet {
        return TokenSet.WHITE_SPACE
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.create(AqlTextTokenTypes.AQL_COMMENT)
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.create(AqlTextTokenTypes.AQL_STRING)
    }

    override fun createElement(node: ASTNode): PsiElement {
        return ASTWrapperPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return PsiAqlTextFileImpl(viewProvider)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): SpaceRequirements {
        return SpaceRequirements.MAY
    }

}