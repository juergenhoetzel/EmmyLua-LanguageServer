// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public class LuaIndexExprImpl extends LuaIndexExprMixin implements LuaIndexExpr {

  public LuaIndexExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitIndexExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LuaExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtilKt.getNameIdentifier(this);
  }

  @NotNull
  public PsiElement setName(String name) {
    return LuaPsiImplUtilKt.setName(this, name);
  }

  @Nullable
  public String getName() {
    return LuaPsiImplUtilKt.getName(this);
  }

  public int getTextOffset() {
    return LuaPsiImplUtilKt.getTextOffset(this);
  }

  @Nullable
  public LuaLiteralExpr getIdExpr() {
    return LuaPsiImplUtilKt.getIdExpr(this);
  }

  @NotNull
  public ITy guessParentType(SearchContext context) {
    return LuaPsiImplUtilKt.guessParentType(this, context);
  }

  @NotNull
  public ITy guessValueType(SearchContext context) {
    return LuaPsiImplUtilKt.guessValueType(this, context);
  }

  @Override
  @Nullable
  public PsiElement getDot() {
    return findChildByType(DOT);
  }

  @Override
  @Nullable
  public PsiElement getColon() {
    return findChildByType(COLON);
  }

  @Override
  @Nullable
  public PsiElement getLbrack() {
    return findChildByType(LBRACK);
  }

}
