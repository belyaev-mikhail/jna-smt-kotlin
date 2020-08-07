package ru.spbstu.stp

import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import ru.spbstu.*
import ru.spbstu.common.jna.free
import ru.spbstu.common.jna.getString

internal class STPExprFactory(internal val vc: STPNativeLibrary.VC) : ExprFactory() {
    companion object : STPNativeLibrary by STPNativeLibrary.INSTANCE

    class STPExpr(val native: STPNativeLibrary.Expr): Expr
    sealed class STPSort: Sort {
        internal abstract fun native(vc: STPNativeLibrary.VC): STPNativeLibrary.Type

        data class BitVec(val bitSize: Int): STPSort() {
            override fun native(vc: STPNativeLibrary.VC) = vc.vc_bvType(bitSize)
        }
        object Bool: STPSort() {
            override fun toString(): String = "Bool"
            override fun native(vc: STPNativeLibrary.VC): STPNativeLibrary.Type = vc.vc_boolType()
        }
        data class Array(val index: STPSort, val data: STPSort): STPSort() {
            override fun native(vc: STPNativeLibrary.VC): STPNativeLibrary.Type =
                vc.vc_arrayType(index.native(vc), data.native(vc))
        }
    }
    override val Expr.sort: Sort
        get() {
            check(this is STPExpr)
            return when(getType(this.native)) {
                STPNativeLibrary.STPTypeKinds.BOOLEAN_TYPE -> STPSort.Bool
                STPNativeLibrary.STPTypeKinds.BITVECTOR_TYPE -> {
                    STPSort.BitVec(getBVLength(this.native))
                }
                STPNativeLibrary.STPTypeKinds.ARRAY_TYPE -> {
                    STPSort.Array(
                        STPSort.BitVec(getIWidth(this.native)),
                        STPSort.BitVec(getVWidth(this.native))
                    )
                }
                else -> throw IllegalStateException()
            }
        }

    override fun printExpr(expr: Expr): String {
        check(expr is STPExpr)
        val buf = PointerByReference()
        val len = LongByReference()
        vc.vc_printExprToBuffer(expr.native, buf, len)
        val res = buf.value.getString(0, len.value.toInt())
        buf.free()
        return res
    }

    override fun printSort(sort: Sort): String = "$sort"

    override fun bvSort(bits: Int): Sort =
        STPSort.BitVec(bits)

    override fun boolSort(): Sort =
        STPSort.Bool

    override fun arraySort(idx: Sort, element: Sort): Sort {
        check(idx is STPSort)
        check(element is STPSort)
        return STPSort.Array(idx, element)
    }
    override fun variable(name: String, sort: Sort): Expr {
        check(sort is STPSort)
        return STPExpr(vc.vc_varExpr(name, sort.native(vc)))
    }

    private var freshId = 0
    override fun freshVariable(prefix: String, sort: Sort): Expr {
        check(sort is STPSort)
        return STPExpr(vc.vc_varExpr("$prefix${++freshId}", sort.native(vc)))
    }

    override fun const(value: Long, sort: Sort): Expr {
        check(sort is STPSort.BitVec)
        return STPExpr(vc.vc_bvConstExprFromLL(sort.bitSize, value))
    }

    override fun const(value: Boolean): Expr =
        STPExpr(if(value) vc.vc_trueExpr() else vc.vc_falseExpr())

    override fun Expr.not(): Expr {
        check(this is STPExpr)
        return STPExpr(vc.vc_notExpr(this.native))
    }

    override fun disjunct(vararg elements: Expr): Expr {
        val natives = Array(elements.size) { ix ->
            val elem = elements[ix]
            check(elem is STPExpr)
            elem.native
        }
        return STPExpr(vc.vc_orExprN(natives, natives.size))
    }

    override fun conjunct(vararg elements: Expr): Expr {
        val natives = Array(elements.size) { ix ->
            val elem = elements[ix]
            check(elem is STPExpr)
            elem.native
        }
        return STPExpr(vc.vc_andExprN(natives, natives.size))
    }

    inline fun liftBinary(lhv: Expr, rhv: Expr,
                          body: (STPNativeLibrary.Expr, STPNativeLibrary.Expr) -> STPNativeLibrary.Expr): Expr {
        check(lhv is STPExpr)
        check(rhv is STPExpr)
        return STPExpr(body(lhv.native, rhv.native))
    }

    override fun Expr.implies(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_impliesExpr(a, b) }

    override fun Expr.iff(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_iffExpr(a, b) }

    override fun Expr.xor(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_xorExpr(a, b) }

    override fun Expr.eq(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_eqExpr(a, b) }

    override fun Expr.slt(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_sbvLtExpr(a, b) }

    override fun Expr.ult(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_bvLtExpr(a, b) }

    override fun Expr.concat(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_bvConcatExpr(a, b) }

    override fun Expr.binaryAnd(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_bvAndExpr(a, b) }

    override fun Expr.binaryOr(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_bvOrExpr(a, b) }

    override fun Expr.binaryXor(that: Expr): Expr = liftBinary(this, that) { a, b -> vc.vc_bvXorExpr(a, b) }

    override fun Expr.plus(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvPlusExpr(sort.bitSize, a, b)
    }

    override fun Expr.minus(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvMinusExpr(sort.bitSize, a, b)
    }

    override fun Expr.times(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvMultExpr(sort.bitSize, a, b)
    }

    override fun Expr.div(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_sbvDivExpr(sort.bitSize, a, b)
    }

    override fun Expr.udiv(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvDivExpr(sort.bitSize, a, b)
    }

    override fun Expr.rem(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_sbvRemExpr(sort.bitSize, a, b)
    }

    override fun Expr.mod(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_sbvModExpr(sort.bitSize, a, b)
    }

    override fun Expr.urem(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvModExpr(sort.bitSize, a, b)
    }

    override fun Expr.ashl(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvLeftShiftExprExpr(sort.bitSize, a, b)
    }

    override fun Expr.ashr(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvSignedRightShiftExprExpr(sort.bitSize, a, b)
    }

    override fun Expr.lshr(that: Expr): Expr = liftBinary(this, that) { a, b ->
        val sort = sort
        check(sort is STPSort.BitVec)
        vc.vc_bvRightShiftExprExpr(sort.bitSize, a, b)
    }

    override fun Expr.get(index: Expr): Expr = liftBinary(this, index) { a, b ->
        vc.vc_readExpr(a, b)
    }

    override fun Expr.get(assignment: Pair<Expr, Expr>): Expr {
        check(this is STPExpr)
        val (i, v) = assignment
        check(i is STPExpr)
        check(v is STPExpr)
        return STPExpr(vc.vc_writeExpr(this.native, i.native, v.native))
    }

    override fun Expr.binaryNot(): Expr {
        check(this is STPExpr)
        return STPExpr(vc.vc_bvNotExpr(this.native))
    }

    override fun ite(cond: Expr, tru: Expr, fls: Expr): Expr {
        check(cond is STPExpr)
        check(tru is STPExpr)
        check(fls is STPExpr)

        return STPExpr(vc.vc_iteExpr(cond.native, tru.native, fls.native))
    }

    override fun Expr.extract(lo: Int, hi: Int): Expr {
        check(this is STPExpr)
        return STPExpr(vc.vc_bvExtract(this.native, hi, lo))
    }

    override fun Expr.sext(size: Int): Expr {
        check(this is STPExpr)
        return STPExpr(vc.vc_bvSignExtend(this.native, size))
    }

    override fun Expr.zext(size: Int): Expr {
        check(this is STPExpr)
        val sort = sort
        check(sort is STPSort.BitVec)
        if(size <= sort.bitSize) return this
        val zeroes = const(0, STPSort.BitVec(size - sort.bitSize))

        return zeroes concat this
    }

    internal inner class STPModel(val native: STPNativeLibrary.WholeCounterExample): Model {
        override fun get(e: Expr): Expr {
            check(e is STPExpr)
            return STPExpr(vc.vc_getCounterExample(e.native))
        }
    }

    inner class STPChecker: Checker {
        override fun assert(e: Expr) {
            check(e is STPExpr)
            vc.vc_assertFormula(e.native)
        }

        override fun check(): Result {
            val q = vc.vc_query(vc.vc_falseExpr())
            return when(q) {
                0 -> Result.Sat
                1 -> Result.Unsat
                2 -> Result.Unknown
                3 -> Result.Timeout
                else -> Result.Unknown
            }
        }

        override fun printModel(): String {
            val buf = PointerByReference()
            val len = LongByReference()
            vc.vc_printCounterExampleToBuffer(buf, len)
            val res = buf.value.getString(0, len.value.toInt())
            buf.free()
            return res
        }

        override fun getModel(): Model? {
            return STPModel(vc.vc_getWholeCounterExample())
        }
    }

    override fun makeChecker(): Checker = STPChecker()

}

fun STPExprFactory(): ExprFactory = STPExprFactory(STPNativeLibrary.INSTANCE.vc_createValidityChecker())
