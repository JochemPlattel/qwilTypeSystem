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

class FlowContext() {
    val map = mutableMapOf<String, Type>()
}