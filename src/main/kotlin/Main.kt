package org.example

fun main() {
    println("--- x is T test ---\n")
    TEST_xIsT()
    println("--- if test ---\n")
    TEST_if()
    println("--- and test ---\n")
    TEST_and()
}
/*
x is T
 */
fun TEST_xIsT() {
    val assertion = Stmt.Assert(isexpr(vari("x"), type("T")))
    val cfg = stmtToCFG(assertion)
    showAnalysis(cfg)
    /*
    println(prettyPrintCFG(cfg))
    for (node in topoSort(cfg)) {
        val context = getOutFlowContext(cfg, node)
        println(prettyPrintCFGNode(node))
        println(prettyPrintFlowContext(context))
    }

     */
}

/*
if x is T and y is U {
    a
} else {
    b
}
 */
fun TEST_and() {
    val and = and(
        isexpr(vari("x"), type("T")),
        isexpr(vari("y"), type("U"))
    )
    val program = ifstmt(
        and,
        exprstmt(
            vari("a")
        ),
        exprstmt(
            vari("b")
        )
    )
    val cfg = stmtToCFG(program)
    showAnalysis(cfg)
    /*
    println(prettyPrintCFG(cfg))

    for (node in topoSort(cfg)) {
        val context = getOutFlowContext(cfg, node)
        println(prettyPrintCFGNode(node))
        println(prettyPrintFlowContext(context))
    }

     */
}

/*
if x is t {
    a
} else {
    b
}
 */
fun TEST_if() {
    val stmt = ifstmt(
        isexpr(vari("x"), type("T")),
        exprstmt(vari("a")),
        exprstmt(vari("b"))
    )
    val cfg = stmtToCFG(stmt)
    showAnalysis(cfg)
    /*
    println(prettyPrintCFG(cfg))

    for (node in topoSort(cfg)) {
        val context = getOutFlowContext(cfg, node)
        println(prettyPrintCFGNode(node))
        println(prettyPrintFlowContext(context))
    }

     */
}

fun showAnalysis(cfg: CFGFragment) {
    val nodes = topoSort(cfg)
    println("---nodes---")
    for (node in nodes) {
        println(prettyPrintCFGNode(node))
    }
    println()
    println("---cfg---")
    println(prettyPrintCFG(cfg))

    println("---analysis---")
    for (node in nodes) {
        val context = getOutFlowContext(cfg, node)
        println(prettyPrintCFGNode(node))
        println(prettyPrintFlowContext(context))
        println()
    }
}

fun exprstmt(expr: Expr) = Stmt.ExprStmt(expr)
fun vari(name: String) = Expr.Var(name)
fun ifstmt(condition: Expr, trueBranch: Stmt, falseBranch: Stmt) = Stmt.If(condition, trueBranch, falseBranch)
fun and(left: Expr, right: Expr) = Expr.And(left, right)
fun isexpr(test: Expr, type: Type) = Expr.Is(test, type)
fun type(name: String) = Type.Base(name)