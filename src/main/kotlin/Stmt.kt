package org.example

sealed interface Stmt {
    class If(val condition: Expr, val trueBranch: Stmt, val falseBranch: Stmt): Stmt
    class Block(val stmts: List<Stmt>): Stmt
    class ExprStmt(val expr: Expr): Stmt
    class Assert(val condition: Expr): Stmt
}