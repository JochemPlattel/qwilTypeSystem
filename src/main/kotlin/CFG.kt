package org.example

sealed interface Expr {
    class Call(val callee: Expr, val arguments: List<Expr>): Expr
    class Is(val test: Expr, val type: Type): Expr
    class Var(val name: String): Expr
    class And(val left: Expr, val right: Expr): Expr
    class Not(val expr: Expr): Expr
}

sealed interface Stmt {
    class If(val condition: Expr, val trueBranch: Stmt, val falseBranch: Stmt): Stmt
    class Block(val stmts: List<Stmt>): Stmt
    class ExprStmt(val expr: Expr): Stmt
}

sealed interface Type {
    data class Base(val name: String): Type
}


sealed interface CFGNode {
    class Assign(val name: String): CFGNode
    class Assume(val expr: Expr): CFGNode
    class Var(val vari: Expr.Var): CFGNode
}

data class CFGEdge(val source: CFGNode, val target: CFGNode)
data class CFGFragment(
    val root: CFGNode,
    val edges: Set<CFGEdge>,
    val falseNodes: Set<CFGNode>,
    val trueNodes: Set<CFGNode>
)

fun stmtToCFG(stmt: Stmt): CFGFragment {
    return when (stmt) {
        is Stmt.Block -> blockToCFG(stmt)
        is Stmt.ExprStmt -> exprToCFGFragment(stmt.expr)
        is Stmt.If -> ifToCFG(stmt)
        else -> TODO()
    }
}

/*
fun exprStmtToCFG(exprStmt: Stmt.ExprStmt): CFGFragment {
    return exprToCFGFragment(exprStmt.expr)
}

 */

fun ifToCFG(ifStmt: Stmt.If): CFGFragment {
    val conditionCfg = exprToCFGFragment(ifStmt.condition)
    val trueCfg = stmtToCFG(ifStmt.trueBranch)
    val falseCfg = stmtToCFG(ifStmt.falseBranch)
    println(prettyPrintCFG(trueCfg))
    println(prettyPrintCFG(falseCfg))
    return mergeCFGFragments(conditionCfg, trueCfg, falseCfg)
}

fun blockToCFG(block: Stmt.Block): CFGFragment {
    check(block.stmts.isNotEmpty())
    var blockCfg = stmtToCFG(block.stmts[0])
    for (i in 1 until block.stmts.size) {
        val stmt = block.stmts[i]
        val stmtCfg = stmtToCFG(stmt)
        blockCfg = cfgMerge(blockCfg, stmtCfg)
    }
    return blockCfg
}

fun exprToCFGFragment(expr: Expr): CFGFragment {
    return when (expr) {
        is Expr.Call -> callToCFGFragment(expr)
        is Expr.Is -> isExprToCFGFragment(expr)
        is Expr.Var -> varToCFG(expr)
        else -> TODO()
    }
}

fun varToCFG(vari: Expr.Var): CFGFragment {
    val node = CFGNode.Var(vari)
    return cfgNodeToFragment(node)
}

fun andToCFG(and: Expr.And): CFGFragment {
    val leftCfg = exprToCFGFragment(and.left)
    val rightCfg = exprToCFGFragment(and.right)
    return trueMergeCFGFragment(leftCfg, rightCfg)
}

fun notToCFG(not: Expr.Not): CFGFragment {
    val cfg = exprToCFGFragment(not.expr)
    return CFGFragment(cfg.root, cfg.edges, cfg.trueNodes, cfg.falseNodes)
}

fun callToCFGFragment(call: Expr.Call): CFGFragment {
    val calleeCfg = exprToCFGFragment(call.callee)
    val argCfgs = call.arguments.map { exprToCFGFragment(it) }
    var callCfg = calleeCfg
    for (argCfg in argCfgs) {
        callCfg = cfgMerge(callCfg, argCfg)
    }
    return callCfg
}

fun isExprToCFGFragment(isExpr: Expr.Is): CFGFragment {
    val builder = CFGBuilder()

    val testCFG = exprToCFGFragment(isExpr.test)
    val trueAssume = CFGNode.Assume(isExpr)
    val falseAssume = CFGNode.Assume(Expr.Not(isExpr))
    builder.addExitNode(trueAssume, true)
    builder.addExitNode(falseAssume, false)

    val trueCfg = cfgNodeToFragment(trueAssume, true)
    val falseCfg = cfgNodeToFragment(falseAssume, false)

    return mergeCFGFragments(testCFG, trueCfg, falseCfg)
}

class CFGBuilder {
    val edges = mutableSetOf<CFGEdge>()
    val falseNodes = mutableSetOf<CFGNode>()
    val trueNodes = mutableSetOf<CFGNode>()

    fun connect(source: CFGNode, target: CFGNode) {
        val edge = CFGEdge(source, target)
        edges.add(edge)
    }

    fun addExitNode(node: CFGNode, mark: Boolean? = null) {
        if (mark == null) {
            falseNodes.add(node)
            trueNodes.add(node)
        }
        else if (mark)
            falseNodes.add(node)
        else
            trueNodes.add(node)
    }
}

fun falseMergeCFGFragment(fragment1: CFGFragment, fragment2: CFGFragment): CFGFragment {
    val newEdges = fragment1.edges + fragment2.edges + fragment1.falseNodes.map { CFGEdge(it, fragment2.root) }
    //println(fragment1.falseNodes.map { CFGEdge(it, fragment2.root) })
    return CFGFragment(fragment1.root, newEdges, fragment2.falseNodes, fragment1.trueNodes)
}

fun trueMergeCFGFragment(fragment1: CFGFragment, fragment2: CFGFragment): CFGFragment {
    val newEdges = fragment1.edges + fragment2.edges + fragment1.trueNodes.map { CFGEdge(it, fragment2.root) }
    return CFGFragment(fragment1.root, newEdges, fragment1.falseNodes, fragment2.trueNodes)
}

fun mergeCFGFragments(
    fragment: CFGFragment,
    trueFragment: CFGFragment,
    falseFragment: CFGFragment
): CFGFragment {
    val withTrue = trueMergeCFGFragment(fragment, trueFragment)
    val withFalse = falseMergeCFGFragment(withTrue, falseFragment)
    return withFalse
}

fun cfgNodeToFalseFragment(node: CFGNode): CFGFragment {
    return CFGFragment(node, emptySet(), setOf(node), emptySet())
}

fun cfgNodeToTrueFragment(node: CFGNode): CFGFragment {
    return CFGFragment(node, emptySet(), emptySet(), setOf(node))
}

fun cfgNodeToFragment(node: CFGNode, mark: Boolean? = null): CFGFragment {
    val exit = setOf(node)
    val empty = emptySet<CFGEdge>()
    if (mark == null) {
        return CFGFragment(node, empty, exit, exit)
    }
    if (mark) {
        return CFGFragment(node, empty, emptySet(), exit)
    }
    return CFGFragment(node, empty, exit, emptySet())
}

fun cfgMerge(fragment1: CFGFragment, fragment2: CFGFragment): CFGFragment {
    val result = mergeCFGFragments(fragment1, fragment2, fragment2)
    return result
}

fun prettyPrintCFGNode(node: CFGNode): String {
    return when (node) {
        is CFGNode.Var -> "var(${node.vari.name})"
        is CFGNode.Assume -> "assume(${exprToSource(node.expr)})"
        is CFGNode.Assign -> "assign(${node.name})"
    }
}

fun prettyPrintCFG(cfg: CFGFragment): String {
    val builder = StringBuilder()
    for (edge in cfg.edges) {
        builder.append(prettyPrintCFGNode(edge.source))
        builder.append(" -> ")
        builder.append(prettyPrintCFGNode(edge.target))
        builder.appendLine()
    }
    for (falseNode in cfg.falseNodes) {
        builder.append(prettyPrintCFGNode(falseNode))
        if (falseNode !in cfg.trueNodes)
            builder.append(" -> false")
        else
            builder.append(" -> exit")
        builder.appendLine()
    }
    for (trueNode in cfg.falseNodes) {
        builder.append(prettyPrintCFGNode(trueNode))
        if (trueNode !in cfg.falseNodes)
            builder.append(" -> true")
        builder.appendLine()
    }
    return builder.toString()
}

fun exprToSource(expr: Expr): String {
    return when (expr) {
        is Expr.Var -> expr.name
        is Expr.Call -> exprToSource(expr.callee) + "(" +  expr.arguments.joinToString { exprToSource(it) } + ")"
        is Expr.Is -> exprToSource(expr.test) + " is " + (expr.type as Type.Base).name
        is Expr.Not -> "not (" + exprToSource(expr.expr) + ")"
        else -> TODO()
    }
}