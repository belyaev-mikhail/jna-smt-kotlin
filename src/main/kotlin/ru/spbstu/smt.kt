package ru.spbstu

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import kotlin.reflect.KProperty

enum class Result {
    Sat, Unsat, Unknown, Timeout
}

interface Expr
interface Sort
interface Model {
    operator fun get(e: Expr): Expr
}
interface Checker {
    fun assert(e: Expr)
    fun check(): Result
    @Deprecated("Remove asap")
    fun printModel(): String

    fun getModel(): Model?
}
interface ExprFactory {
    val Expr.sort: Sort

    fun printExpr(expr: Expr): String
    fun printSort(sort: Sort): String

    fun bvSort(bits: Int): Sort
    fun boolSort(): Sort
    fun arraySort(idx: Sort, element: Sort): Sort

    fun variable(name: String, sort: Sort): Expr
    fun freshVariable(prefix: String, sort: Sort): Expr

    operator fun Sort.getValue(thisRef: Nothing?, prop: KProperty<*>): Expr =
        variable(prop.name, this)

    fun const(value: Long, sort: Sort): Expr
    fun const(value: Boolean): Expr

    operator fun Sort.invoke(value: Long) = const(value, this)

    operator fun Expr.not(): Expr
    fun disjunct(vararg elements: Expr): Expr
    infix fun Expr.or(that: Expr) =
        disjunct(this, that)
    fun conjunct(vararg elements: Expr): Expr
    infix fun Expr.and(that: Expr) =
        conjunct(this, that)
    infix fun Expr.implies(that: Expr): Expr
    infix fun Expr.iff(that: Expr): Expr
    infix fun Expr.xor(that: Expr): Expr

    infix fun Expr.eq(that: Expr): Expr
    infix fun Expr.ne(that: Expr): Expr =
        !(this eq that)
    infix fun Expr.slt(that: Expr): Expr
    infix fun Expr.sgt(that: Expr): Expr =
        that slt this
    infix fun Expr.sle(that: Expr): Expr =
        !(this sgt that)
    infix fun Expr.sge(that: Expr): Expr =
        that sle this
    infix fun Expr.ult(that: Expr): Expr
    infix fun Expr.ugt(that: Expr): Expr =
        that ult this
    infix fun Expr.ule(that: Expr): Expr =
        !(this ugt that)
    infix fun Expr.uge(that: Expr): Expr =
        that ule this

    infix fun Expr.concat(that: Expr): Expr
    infix fun Expr.binaryAnd(that: Expr): Expr
    infix fun Expr.binaryOr(that: Expr): Expr
    infix fun Expr.binaryXor(that: Expr): Expr

    infix operator fun Expr.plus(that: Expr): Expr
    infix operator fun Expr.minus(that: Expr): Expr
    infix operator fun Expr.times(that: Expr): Expr
    infix operator fun Expr.div(that: Expr): Expr
    infix fun Expr.udiv(that: Expr): Expr
    infix operator fun Expr.rem(that: Expr): Expr
    infix fun Expr.mod(that: Expr): Expr
    infix fun Expr.urem(that: Expr): Expr
    infix fun Expr.ashl(that: Expr): Expr
    infix fun Expr.ashr(that: Expr): Expr
    infix fun Expr.lshr(that: Expr): Expr

    operator fun Expr.get(index: Expr): Expr

    operator fun Expr.unaryMinus(): Expr = sort(0) - this
    fun Expr.binaryNot(): Expr

    fun ite(cond: Expr, tru: Expr, fls: Expr): Expr
    fun Expr.extract(lo: Int, hi: Int): Expr
    fun Expr.sext(size: Int): Expr
    fun Expr.zext(size: Int): Expr

    operator fun Expr.get(assignment: Pair<Expr, Expr>): Expr

    fun makeChecker(): Checker
}

abstract class LifetimeExprFactory: AutoCloseable, ExprFactory {
    private val keeper = mutableSetOf<PhantomReference<*>>()
    private val exprQ = ReferenceQueue<Expr>()
    private val sortQ = ReferenceQueue<Sort>()

    protected abstract fun deleteExpr(expr: PhantomReference<out Expr>)
    protected abstract fun deleteSort(sort: PhantomReference<out Sort>)
    protected open fun makePhantom(expr: Expr, que: ReferenceQueue<Expr>): PhantomReference<Expr> =
        PhantomReference(expr, que)
    protected open fun makePhantom(sort: Sort, que: ReferenceQueue<Sort>): PhantomReference<Sort> =
        PhantomReference(sort, que)

    protected fun register(expr: Expr) {
        keeper += makePhantom(expr, exprQ)
    }
    protected fun register(sort: Sort) {
        keeper += makePhantom(sort, sortQ)
    }

    override fun close() {
        do {
            val ref = exprQ.poll() as? PhantomReference<out Expr> ?: break
            keeper.remove(ref)
            deleteExpr(ref)
        } while(true)
        do {
            val ref = sortQ.poll() as? PhantomReference<out Sort> ?: break
            keeper.remove(ref)
            deleteSort(ref)
        } while(true)
    }
}

interface Context {

}
