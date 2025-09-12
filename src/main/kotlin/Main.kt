package org.example

fun main() {
    println("Hello World!")
    val program = Expr.Call(Expr.Var("myfunction"), listOf(Expr.Var("myvariable")))
    val cfg = exprToCFGFragment(program)
    println(cfg)
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
    data class Var(val vari: Expr.Var): CFGNode
}

data class CFGEdge(val source: CFGNode, val target: CFGNode)
data class CFGFragment(
    val root: CFGNode,
    val edges: Set<CFGEdge>,
    val falseNodes: Set<CFGNode>,
    val trueNodes: Set<CFGNode>
)

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

    TODO()
    //val fragment = mergeCFGFragments(testCFG)
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
    println(fragment1.falseNodes.map { CFGEdge(it, fragment2.root) })
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

fun cfgNodeToFragment(node: CFGNode): CFGFragment {
    val exit = setOf(node)
    return CFGFragment(node, emptySet(), exit, exit)
}

fun cfgMerge(fragment1: CFGFragment, fragment2: CFGFragment): CFGFragment {
    val result = mergeCFGFragments(fragment1, fragment2, fragment2)
    return result
}