package org.example

fun main() {
    /*
    val condition = Expr.Is(Expr.Var("x"), Type.Base("T"))
    val trueBranch = Stmt.ExprStmt(Expr.Var("y"))
    val falseBranch = Stmt.ExprStmt(Expr.Var("z"))
    val program = Stmt.If(condition, trueBranch, falseBranch)
    val cfg = stmtToCFG(program)    //println(prettyPrintCFG(cfg))
    val sortedNodes = topoSort(cfg)

     */
    //println(sortedNodes.map { prettyPrintCFGNode(it) })
    val and = and(
        isexpr(vari("x"), type("T")),
        isexpr(vari("y"), type("U"))
    )
    val pro = ifstmt(
        and,
        exprstmt(
            vari("a")
        ),
        exprstmt(
            vari("b")
        )
    )
    val procfg = stmtToCFG(pro)
    for (node in topoSort(procfg)) {
        val context = getOutFlowContext(procfg, node)
        //println(prettyPrintCFGNode(node))
        //println(prettyPrintFlowContext(context))
    }
    //println(prettyPrintCFG(procfg))
    test1()
}

fun test1() {
    val stmt = ifstmt(
        isexpr(vari("x"), type("T")),
        exprstmt(vari("a")),
        exprstmt(vari("b"))
    )
    val cfg = stmtToCFG(stmt)

    for (node in topoSort(cfg)) {
        val context = getOutFlowContext(cfg, node)
        println(prettyPrintCFGNode(node))
        println(prettyPrintFlowContext(context))
    }

    println(prettyPrintCFG(cfg))
}

fun exprstmt(expr: Expr) = Stmt.ExprStmt(expr)
fun vari(name: String) = Expr.Var(name)
fun ifstmt(condition: Expr, trueBranch: Stmt, falseBranch: Stmt) = Stmt.If(condition, trueBranch, falseBranch)
fun and(left: Expr, right: Expr) = Expr.And(left, right)
fun isexpr(test: Expr, type: Type) = Expr.Is(test, type)
fun type(name: String) = Type.Base(name)