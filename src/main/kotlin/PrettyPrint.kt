package org.example

fun prettyPrintType(type: Type): String {
    return when (type) {
        is Type.Base -> type.name
        is Type.Union -> {
            val left = prettyPrintType(type.left)
            val right = prettyPrintType(type.right)
            "($left) | ($right)"
        }
        is Type.Negation -> {
            val negType = prettyPrintType(type.type)
            "!($negType)"
        }
    }
}

fun prettyPrintCFGNode(node: CFGNode): String {
    return when (node) {
        is CFGNode.Var -> "var(${node.vari.name})"
        is CFGNode.Assume -> "assume(${exprToSource(node.assumption)})"
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
        builder.append(" -> false")
        builder.appendLine()
    }
    for (trueNode in cfg.trueNodes) {
        builder.append(prettyPrintCFGNode(trueNode))
        builder.append(" -> true")
        builder.appendLine()
    }
    return builder.toString()
}

fun exprToSource(expr: Expr): String {
    return when (expr) {
        is Expr.Var -> expr.name
        is Expr.Call -> exprToSource(expr.callee) + "(" +  expr.arguments.joinToString { exprToSource(it) } + ")"
        is Expr.Is -> exprToSource(expr.test) + " is " + prettyPrintType(expr.type)
        is Expr.Not -> "not (" + exprToSource(expr.expr) + ")"
        else -> TODO()
    }
}