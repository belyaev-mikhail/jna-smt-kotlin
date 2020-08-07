package ru.spbstu.z3

import ru.spbstu.*
import ru.spbstu.common.jna.Out
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue

class Z3ExprFactory internal constructor(internal val ctx: Z3NativeLibrary.Context) : LifetimeExprFactory() {

    companion object: Z3NativeLibrary by Z3NativeLibrary.INSTANCE

    private class Z3Sort(val native: Z3NativeLibrary.Sort): Sort
    private class Z3Expr(val native: Z3NativeLibrary.Ast): Expr

    override val Expr.sort: Sort
        get() {
            check(this is Z3Expr)
            return Z3Sort(ctx.Z3_get_sort(this.native))
        }

    override fun printExpr(expr: Expr): String {
        check(expr is Z3Expr)
        return ctx.Z3_ast_to_string(expr.native)
    }

    override fun printSort(sort: Sort): String {
        check(sort is Z3Sort)
        return ctx.Z3_sort_to_string(sort.native)
    }

    override fun bvSort(bits: Int): Sort = Z3Sort(ctx.Z3_mk_bv_sort(bits))
    override fun boolSort(): Sort = Z3Sort(ctx.Z3_mk_bool_sort())
    override fun arraySort(idx: Sort, element: Sort): Sort {
        check(idx is Z3Sort)
        check(element is Z3Sort)
        return Z3Sort(ctx.Z3_mk_array_sort(element.native, idx.native))
    }

    override fun variable(name: String, sort: Sort): Expr {
        check(sort is Z3Sort)
        return Z3Expr(ctx.Z3_mk_const(ctx.Z3_mk_string_symbol(name), sort.native))
    }

    override fun freshVariable(prefix: String, sort: Sort): Expr {
        check(sort is Z3Sort)
        return Z3Expr(ctx.Z3_mk_fresh_const(prefix, sort.native))
    }

    override fun const(value: Long, sort: Sort): Expr {
        check(sort is Z3Sort)
        return Z3Expr(ctx.Z3_mk_int64(value, sort.native))
    }

    override fun const(value: Boolean): Expr {
        return if(value) Z3Expr(ctx.Z3_mk_true()) else Z3Expr(ctx.Z3_mk_false())
    }

    override fun Expr.not(): Expr {
        check(this is Z3Expr)
        return Z3Expr(ctx.Z3_mk_not(this.native))
    }

    override fun disjunct(vararg elements: Expr): Expr {
        val natives = Array(elements.size) { ix ->
            val e = elements[ix]
            check(e is Z3Expr)
            e.native
        }
        return Z3Expr(ctx.Z3_mk_or(natives.size, natives))
    }

    override fun conjunct(vararg elements: Expr): Expr {
        val natives = Array(elements.size) { ix ->
            val e = elements[ix]
            check(e is Z3Expr)
            e.native
        }
        return Z3Expr(ctx.Z3_mk_and(natives.size, natives))
    }

    private inline fun wrapNative(lhv: Expr,
                                  rhv: Expr,
                                  body: (Z3NativeLibrary.Ast, Z3NativeLibrary.Ast) -> Z3NativeLibrary.Ast): Expr {
        check(lhv is Z3Expr)
        check(rhv is Z3Expr)
        return Z3Expr(body(lhv.native, rhv.native))
    }

    override fun Expr.implies(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_implies(l, r) }

    override fun Expr.iff(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_iff(l, r) }

    override fun Expr.xor(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_xor(l, r) }

    override fun Expr.eq(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_eq(l, r) }

    override fun Expr.slt(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvslt(l, r) }

    override fun Expr.ult(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvult(l, r) }

    override fun Expr.concat(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_concat(l, r) }

    override fun Expr.binaryAnd(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvand(l, r) }

    override fun Expr.binaryOr(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvor(l, r) }

    override fun Expr.binaryXor(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvxor(l, r) }

    override fun Expr.plus(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvadd(l, r) }

    override fun Expr.minus(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvsub(l, r) }

    override fun Expr.times(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvmul(l, r) }

    override fun Expr.div(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvsdiv(l, r) }

    override fun Expr.udiv(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvudiv(l, r) }

    override fun Expr.rem(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvsrem(l, r) }

    override fun Expr.mod(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvsmod(l, r) }

    override fun Expr.urem(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvurem(l, r) }

    override fun Expr.ashl(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvshl(l, r) }

    override fun Expr.ashr(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvashr(l, r) }

    override fun Expr.lshr(that: Expr): Expr = wrapNative(this, that) { l, r -> ctx.Z3_mk_bvlshr(l, r) }

    override fun Expr.get(index: Expr): Expr = wrapNative(this, index) { l, r -> ctx.Z3_mk_select(l, r) }

    override fun Expr.get(assignment: Pair<Expr, Expr>): Expr {
        check(this is Z3Expr)
        val (to, v) = assignment
        check(to is Z3Expr)
        check(v is Z3Expr)
        return Z3Expr(ctx.Z3_mk_store(this.native, to.native, v.native))
    }

    override fun Expr.unaryMinus(): Expr {
        check(this is Z3Expr)
        return Z3Expr(ctx.Z3_mk_bvneg(this.native))
    }

    override fun Expr.binaryNot(): Expr {
        check(this is Z3Expr)
        return Z3Expr(ctx.Z3_mk_bvnot(this.native))
    }

    override fun ite(cond: Expr, tru: Expr, fls: Expr): Expr {
        check(cond is Z3Expr)
        check(tru is Z3Expr)
        check(fls is Z3Expr)
        return Z3Expr(ctx.Z3_mk_ite(cond.native, tru.native, fls.native))
    }

    override fun Expr.extract(lo: Int, hi: Int): Expr {
        check(this is Z3Expr)
        return Z3Expr(ctx.Z3_mk_extract(hi, lo, this.native))
    }

    override fun Expr.sext(size: Int): Expr {
        check(this is Z3Expr)
        return Z3Expr(ctx.Z3_mk_sign_ext(size, this.native))
    }

    override fun Expr.zext(size: Int): Expr {
        check(this is Z3Expr)
        return Z3Expr(ctx.Z3_mk_zero_ext(size, this.native))
    }

    internal inner class Z3Model(val native: Z3NativeLibrary.Model): Model {
        override fun get(e: Expr): Expr {
            check(e is Z3Expr)
            val res = Out(Z3NativeLibrary.Ast())
            ctx.Z3_model_eval(native, e.native, /* model_completion = */ true, res)
            return Z3Expr(res.value)
        }
    }

    internal inner class Z3Checker(val native: Z3NativeLibrary.Solver): Checker {
        override fun assert(e: Expr) {
            check(e is Z3Expr)
            ctx.Z3_solver_assert(native, e.native)
        }

        override fun check(): Result {
            val nativeRes = ctx.Z3_solver_check(native)
            return when(nativeRes) {
                -1 -> Result.Unsat
                0 -> Result.Unknown
                1 -> Result.Sat
                else -> Result.Unknown
            }
        }

        override fun printModel(): String {
            val model = ctx.Z3_solver_get_model(native) ?: return "No model available"
            return ctx.Z3_model_to_string(model)
        }

        override fun getModel(): Model? {
            return ctx.Z3_solver_get_model(native)?.let { Z3Model(it) }
        }
    }

    override fun makeChecker(): Checker = Z3Checker(ctx.Z3_mk_solver())


    private class ExprReference(e: Z3Expr, q: ReferenceQueue<Expr>) : PhantomReference<Expr>(e, q) {
        var native = e.native ?: null
        override fun clear() {
            super.clear()
            native = null
        }
    }
    override fun makePhantom(expr: Expr, que: ReferenceQueue<Expr>): PhantomReference<Expr> {
        check(expr is Z3Expr)
        ctx.Z3_inc_ref(expr.native)
        return ExprReference(expr, que)
    }

    private class SortReference(s: Z3Sort, q: ReferenceQueue<Sort>) : PhantomReference<Sort>(s, q) {
        val native = s.native ?: null
    }
    override fun makePhantom(sort: Sort, que: ReferenceQueue<Sort>): PhantomReference<Sort> {
        check(sort is Z3Sort)
        ctx.Z3_inc_ref(ctx.Z3_sort_to_ast(sort.native))
        return SortReference(sort, que)
    }

    override fun deleteExpr(expr: PhantomReference<out Expr>) {
        check(expr is ExprReference)
        val native = expr.native ?: return
        ctx.Z3_dec_ref(native)
        expr.clear()
    }

    override fun deleteSort(sort: PhantomReference<out Sort>) {
        check(sort is SortReference)
        val native = sort.native ?: return
        ctx.Z3_dec_ref(ctx.Z3_sort_to_ast(native))
        sort.clear()
    }
}

fun Z3ExprFactory(): ExprFactory {
    with(Z3NativeLibrary.INSTANCE) {
        val config = Z3_mk_config()
        config.Z3_set_param_value("model", "true")
        val ctx = config.Z3_mk_context()
        ctx.Z3_set_error_handler(object: Z3NativeLibrary.Z3ErrorHandler {
            override fun invoke(ctx: Z3NativeLibrary.Context, error: Int) {
                val message = ctx.Z3_get_error_msg(error)
                throw IllegalStateException("Z3 error: $message")
            }
        })
        return Z3ExprFactory(ctx)
    }
}