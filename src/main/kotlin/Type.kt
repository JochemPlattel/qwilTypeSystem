package org.example

sealed interface Type {
    class Base(val name: String): Type
    data class Union(val left: Type, val right: Type): Type
    data class Negation(val type: Type): Type
}