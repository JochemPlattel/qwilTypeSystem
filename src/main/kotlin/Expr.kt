package org.example

sealed interface Expr {
    class Call(val callee: Expr, val arguments: List<Expr>): Expr
    class Is(val test: Expr, val type: Type): Expr
    class Var(val name: String): Expr
    class And(val left: Expr, val right: Expr): Expr
    class Not(val expr: Expr): Expr
}