package org.example

fun main() {
    val condition = Expr.Is(Expr.Var("x"), Type.Base("T"))
    val trueBranch = Stmt.ExprStmt(Expr.Var("y"))
    val falseBranch = Stmt.ExprStmt(Expr.Var("z"))
    val program = Stmt.If(condition, trueBranch, falseBranch)
    val cfg = stmtToCFG(program)    //println(prettyPrintCFG(cfg))
    val sortedNodes = topoSort(cfg)
    println(sortedNodes.map { prettyPrintCFGNode(it) })
}