package org.example

fun main() {
    println("Hello World!")
}

sealed interface Expr {
    data class Call(val callee: Expr, val arguments: List<Expr>): Expr
    data class Is(val test: Expr, val type: Type): Expr
    data class Var(val name: String): Expr
    data class And(val left: Expr, val right: Expr): Expr
    data class Not(val expr: Expr): Expr
}

sealed interface Type {
    data class Base(val name: String): Type
}


sealed interface CFGNode {
    data class Assign(val name: String): CFGNode
    data class Assume(val expr: Expr): CFGNode
}

data class CFGEdge(val source: CFGNode, val target: CFGNode)
class CFGFragment(
    val root: CFGNode,
    val edges: Set<CFGEdge>,
    val falseNodes: Set<CFGNode>,
    val trueNodes: Set<CFGNode>
)

fun exprToCFGFragment(expr: Expr): CFGFragment {
    return when (expr) {
        is Expr.Call -> callToCFGFragment(expr)
        is Expr.Is -> isExprToCFGFragment(expr)
        is Expr.Var ->
        else -> TODO()
    }
}

fun callToCFGFragment(call: Expr.Call): CFGFragment {
    TODO()
}

fun isExprToCFGFragment(isExpr: Expr.Is): CFGFragment {
    val builder = CFGBuilder()

    val testCFG = exprToCFGFragment(isExpr.test)
    val trueAssume = CFGNode.Assume(isExpr)
    val falseAssume = CFGNode.Assume(Expr.Not(isExpr))
    builder.markNode(trueAssume, true)
    builder.markNode(falseAssume, false)

    val fragment = mergeCFGFragments(testCFG)
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
    return CFGFragment(fragment1.root, newEdges, fragment2.falseNodes, fragment2.trueNodes)
}

fun trueMergeCFGFragment(fragment1: CFGFragment, fragment2: CFGFragment): CFGFragment {
    val newEdges = fragment1.edges + fragment2.edges + fragment1.trueNodes.map { CFGEdge(it, fragment2.root) }
    return CFGFragment(fragment1.root, newEdges, fragment2.falseNodes, fragment2.trueNodes)
}

fun mergeCFGFragments(
    fragment: CFGFragment,
    trueFragment: CFGFragment,
    falseFragment: CFGFragment
): CFGFragment {
    val withTrue = trueMergeCFGFragment(fragment, trueFragment)
    return falseMergeCFGFragment(withTrue, falseFragment)
}

fun cfgNodeToFalseFragment(node: CFGNode): CFGFragment {
    return CFGFragment(node, emptySet(), setOf(node), emptySet())
}

fun cfgNodeToTrueFragment(node: CFGNode): CFGFragment {
    return CFGFragment(node, emptySet(), emptySet(), setOf(node))
}

fun cfgNodeToFragment(node: CFGNode): CFGFragment {
    val exit = setOf(node)
    return CFGFragment(node, emptySet(), exit, exit)
}