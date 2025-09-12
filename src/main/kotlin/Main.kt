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

data class Edge(val source: CFGNode, val target: CFGNode)
class CFGFragment(
    val nodes: Set<CFGNode>,
    val root: CFGNode,
    val edges: Set<Edge>,
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
    val nodes = mutableSetOf<CFGNode>()
    val edges = mutableSetOf<Edge>()
    val falseNodes = mutableSetOf<CFGNode>()
    val trueNodes = mutableSetOf<CFGNode>()

    fun addEdge(source: CFGNode, target: CFGNode) {
        nodes.add(source)
        nodes.add(target)
        val edge = Edge(source, target)
        edges.add(edge)
    }

    fun markNode(node: CFGNode, mark: Boolean) {
        nodes.add(node)
        if (mark) {
            falseNodes.add(node)
        } else {
            trueNodes.add(node)
        }
    }
}

fun falseMergeCFGFragment(fragment1: CFGFragment, fragment2: CFGFragment): CFGFragment {
    val newNodes = fragment1.nodes + fragment2.nodes
    val newEdges = (fragment1.edges + fragment2.edges).toMutableSet()
    for (falseNode in fragment1.falseNodes) {
        val edge = Edge(falseNode, fragment2.root)
        newEdges.add(edge)
    }
    return CFGFragment(newNodes, fragment1.root, newEdges, fragment2.falseNodes, fragment2.trueNodes)
}

fun trueMergeCFGFragment(fragment1: CFGFragment, fragment2: CFGFragment): CFGFragment {
    val newNodes = fragment1.nodes + fragment2.nodes
    val newEdges = (fragment1.edges + fragment2.edges).toMutableSet()
    for (trueNode in fragment1.trueNodes) {
        val edge = Edge(trueNode, fragment2.root)
        newEdges.add(edge)
    }
    return CFGFragment(newNodes, fragment1.root, newEdges, fragment2.falseNodes, fragment2.trueNodes)
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
    return CFGFragment(setOf(node), node, emptySet(), setOf(node), emptySet())
}

fun cfgNodeToTrueFragment(node: CFGNode): CFGFragment {
    return CFGFragment(setOf(node), node, emptySet(), emptySet(), setOf(node))
}