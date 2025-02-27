package com.tang.vscode.utils

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyFunction
import com.tang.vscode.api.ILuaFile
import com.tang.vscode.api.impl.LuaFile
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import java.util.concurrent.CompletableFuture

fun range(sLine: Int, sChar: Int, eLine: Int, eChar: Int): Range {
    return Range(Position(sLine, sChar), Position(eLine, eChar))
}

fun PsiReference.getRangeInFile(file: ILuaFile): Range {
    var textRange = rangeInElement
    val parentRange = element.textRange
    textRange = textRange.shiftRight(parentRange.startOffset)
    return textRange.toRange(file)
}

val PsiElement.nameRange: TextRange? get() {
    if (this is PsiNameIdentifierOwner) {
        val id = this.nameIdentifier
        return id?.textRange
    }
    return this.textRange
}

fun PsiNamedElement.getSymbol(): SymbolInformation {
    val file = containingFile.virtualFile as LuaFile
    val range = nameRange ?: textRange
    val loc = Location(file.uri.toString(), range.toRange(file))
    var text = name
    if (this is LuaFuncBodyOwner) {
        text = "$text${this.paramSignature}"
    }
    val kind = when (this) {
        is LuaClassMethod -> SymbolKind.Method
        is LuaClassField -> SymbolKind.Field
        is LuaDocTagClass -> SymbolKind.Class
        else -> SymbolKind.Variable
    }
    return SymbolInformation(text, kind, loc)
}

fun getDocumentSymbols(psi: PsiElement, file: ILuaFile): List<DocumentSymbol> {
    val list = mutableListOf<DocumentSymbol>()
    psi.acceptChildren(object : LuaVisitor() {
        override fun visitClassMethodDef(o: LuaClassMethodDef) {
            o.getDocumentSymbol(file)?.let { list.add(it) }
        }

        override fun visitLocalDef(o: LuaLocalDef) {
            o.nameList?.nameDefList?.forEach { def ->
                def.getDocumentSymbol(file)?.let { list.add(it) }
            }
        }

        override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
            o.getDocumentSymbol(file)?.let { list.add(it) }
        }

        override fun visitFuncDef(o: LuaFuncDef) {
            o.getDocumentSymbol(file)?.let { list.add(it) }
        }

        override fun visitClosureExpr(o: LuaClosureExpr) {
            val range = o.textRange.toRange(file)
            val symbol = DocumentSymbol(
                    "function${o.paramSignature}",
                    SymbolKind.Function,
                    range,
                    range
            )
            symbol.children = getDocumentSymbols(o, file)
            list.add(symbol)
        }

        override fun visitBlock(o: LuaBlock) {
            o.acceptChildren(this)
        }

        override fun visitPsiElement(o: LuaPsiElement) {
            o.acceptChildren(this)
        }
    })
    return list
}

private fun PsiNamedElement.getDocumentSymbol(file: ILuaFile): DocumentSymbol? {
    val range = textRange.toRange(file)
    val selectionRange = (nameRange ?: textRange).toRange(file)
    val symbol = when (this) {
        is LuaClassMethodDef -> {
            val fTy = guessType(SearchContext.get(project))
            if (fTy is ITyFunction) {
                DocumentSymbol(
                        "${this.classMethodName.text}${fTy.mainSignature.paramSignature}",
                        SymbolKind.Method,
                        range,
                        selectionRange
                )
            } else null
        }
        is LuaClassField -> {
            DocumentSymbol(
                    this.text,
                    SymbolKind.Field,
                    range,
                    selectionRange
            )
        }
        is LuaNameDef -> {
            DocumentSymbol(
                    "local $name",
                    SymbolKind.Variable,
                    range,
                    selectionRange
            )
        }
        is LuaLocalFuncDef -> {
            DocumentSymbol(
                    "local function $name",
                    SymbolKind.Function,
                    range,
                    selectionRange
            )
        }
        is LuaFuncDef -> {
            DocumentSymbol(
                    "function $name",
                    SymbolKind.Function,
                    range,
                    selectionRange
            )
        }
        else -> null
    }
    if (symbol != null) {
        symbol.children = getDocumentSymbols(this, file)
    }
    return symbol
}

fun TextRange.toRange(file: ILuaFile): Range {
    val lineStart = file.getLine(this.startOffset)
    val lineEnd = file.getLine(this.endOffset)
    return range(lineStart.first, lineStart.second, lineEnd.first, lineEnd.second)
}

fun <R> computeAsync(code :(CancelChecker) -> R) : CompletableFuture<R> {
    return CompletableFutures.computeAsync {
        ProgressManager.setCancelChecker(it)
        code(it)
    }
}