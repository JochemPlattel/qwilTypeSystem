package org.example

fun getCFGNodes(cfg: CFGFragment): Set<CFGNode> {
    val nodes = mutableSetOf<CFGNode>()
    nodes.addAll(cfg.trueNodes)
    nodes.addAll(cfg.falseNodes)
    nodes.add(cfg.root)
    for (edge in cfg.edges) {
        nodes.add(edge.source)
        nodes.add(edge.target)
    }
    return nodes
}

fun hasIncomingEdge(edges: Set<CFGEdge>, node: CFGNode): Boolean {
    return edges.any { it.target == node }
}

fun getTargets(edges: Set<CFGEdge>, node: CFGNode): Set<CFGNode> {
    return edges.filter { it.source == node }.map { it.target }.toSet()
}

fun topoSort(cfg: CFGFragment): List<CFGNode> {
    val nodes = getCFGNodes(cfg)
    val edges = cfg.edges.toMutableSet()
    val l = mutableListOf<CFGNode>()
    val s = nodes.filter { !hasIncomingEdge(cfg.edges, it) }.toMutableSet()

    while (s.isNotEmpty()) {
        val n = s.first()
        s.remove(n)
        l.add(n)

        for (m in nodes) {
            val edge = edges.firstOrNull { it.source == n && it.target == m }
            if (edge != null) {
                edges.remove(edge)
                if (edges.none { it.target == m }) {
                    s.add(m)
                }
            }
        }
    }

    return l
}

fun getPredecessors(cfg: CFGFragment, node: CFGNode): Set<CFGNode> {
    return cfg.edges.filter { it.target == node }.map { it.source }.toSet()
}
//sortedNodes: List<CFGNode>
fun getInFlowContext(cfg: CFGFragment, node: CFGNode): FlowContext {
    val pred = getPredecessors(cfg, node)
    val predInFlowContexts = pred.map { getOutFlowContext(cfg, it) }
    var inContext = emptyFlowContext()
    for (predInFlowContext in predInFlowContexts) {
        inContext = joinFlowContexts(inContext, predInFlowContext)
    }
    return inContext
}

//joining with empty context not working
fun joinFlowContexts(context1: FlowContext, context2: FlowContext): FlowContext {
    val newMap = context1.map.toMutableMap()
    //require(context1.map.keys == context2.map.keys)
    /*
    val overlappingNames = context1.map.keys.intersect(context2.map.keys)
    for (name in overlappingNames) {
        val type1 = context1.map[name]!!
        val type2 = context2.map[name]!!
        newMap[name] = Type.Union(type1, type2)
    }

     */
    for ((name, type2) in context2.map) {
        if (name in context1.map) {
            val type1 = context1.map[name]!!
            newMap[name] = Type.Union(type1, type2)
        }
        else {
            newMap[name] = type2
        }
    }
    return FlowContext(newMap)
}

fun getOutFlowContext(cfg: CFGFragment, node: CFGNode): FlowContext {
    val inContext = getInFlowContext(cfg, node)

    return when (node) {
        is CFGNode.Assume -> flowContextWithAssumption(inContext, node.assumption)
        is CFGNode.Var -> inContext
        is CFGNode.Assign -> {
            val type = inferType(node.value, inContext)
            val map = inContext.map + (node.name to type)
            FlowContext(map)
        }
        else -> TODO()
    }
}

fun inferType(expr: Expr, flowContext: FlowContext): Type {
    return when (expr) {
        is Expr.Var -> flowContext.map[expr.name]!!
        else -> TODO()
    }
}

fun flowContextWithAssumption(context: FlowContext, assumption: Expr): FlowContext {
    if (assumption is Expr.Is && assumption.test is Expr.Var) {
        val name = assumption.test.name
        val currentType = context.map[name]
        val newType = if (currentType == null) {
            assumption.type
        } else {
            Type.Union(currentType, assumption.type)
        }
        val newMap = context.map + (name to newType)
        return FlowContext(newMap)
    }
    if (assumption is Expr.Not && assumption.expr is Expr.Is && assumption.expr.test is Expr.Var) {
        val name = assumption.expr.test.name
        val currentType = context.map[name]
        val negType = Type.Negation(assumption.expr.type)
        val newType = if (currentType == null) {
            negType
        } else {
            Type.Union(currentType, negType)
        }
        val newMap = context.map + (name to newType)
        return FlowContext(newMap)
    }
    return context
}

fun emptyFlowContext(): FlowContext = FlowContext(emptyMap())

class FlowContext(val map: Map<String, Type>)

fun prettyPrintFlowContext(context: FlowContext): String {
    val inner = context.map.map { (name, type) -> "$name -> ${prettyPrintType(type)}" }.joinToString()
    return "[$inner]"
}