package ru.spbstu

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import ru.spbstu.common.jna.StringByReference
import ru.spbstu.common.jna.free
import ru.spbstu.stp.STPNativeLibrary
import ru.spbstu.z3.Z3NativeLibrary
import ru.spbstu.common.jna.getString
import ru.spbstu.stp.STPExprFactory
import ru.spbstu.z3.Z3ExprFactory

@OptIn(ExperimentalStdlibApi::class)
fun main(args: Array<String>) {
    with(STPExprFactory()) {
        val bv16 = bvSort(16)
        val x by bv16
        val y by bv16

        val checker = makeChecker()
        val e = (bv16(15) * x + bv16(443) * y + bv16(1000)) eq bv16(0)
        println(printExpr(e))
        checker.assert(e)
        println(checker.check())
        //println(checker.printModel())

        val model = checker.getModel()!!
        println(printExpr(x) + ":" + printExpr(model[x]))
        println(printExpr(y) + ":" + printExpr(model[y]))
    }
//    val z3 = Z3NativeLibrary.INSTANCE
//
//    val stp = STPNativeLibrary.INSTANCE
//
//    //println(tru)
//    with(z3) {
//        val cfg = Z3_mk_config()
//        cfg.Z3_set_param_value("model", "true")
//        val ctx = cfg.Z3_mk_context()
//        with(ctx) {
//            val major = IntByReference()
//            val minor = IntByReference()
//            val buildNumber = IntByReference()
//            val revNumber = IntByReference()
//            Z3_get_version(major, minor, buildNumber, revNumber)
//            println("${major.value}:${minor.value}:${buildNumber.value}:${revNumber.value}")
//
//            val x = Z3_mk_const(Z3_mk_string_symbol("x"), Z3_mk_bv_sort(8))
//            val y = Z3_mk_const(Z3_mk_string_symbol("y"), Z3_mk_bv_sort(8))
//            val z = Z3_mk_const(Z3_mk_string_symbol("z"), Z3_mk_bv_sort(8))
//
//            println(Z3_ast_to_string(x))
//            println(Z3_ast_to_string(y))
//            val sum = Z3_mk_bvadd(x, y)
//            val sub = Z3_mk_bvsub(x, y)
//
//            val solver = Z3_mk_solver()
//            Z3_solver_assert(solver, Z3_mk_bvult(sum, sub))
//
//            val check = Z3_solver_check(solver)
//            val model = Z3_solver_get_model(solver)
//
//            println(Z3_model_to_string(model))
//        }
//    }
//
//    with(stp) {
//        val vc = vc_createValidityChecker()
//        with(vc) {
//            val x = vc_varExpr("x", vc_bvType(16))
//            val cc = vc_notExpr(vc_eqExpr(x, vc_bvConstExprFromInt(16, 240)))
//            val scc = PointerByReference()
//            val sccl = LongByReference()
//            vc_printExprToBuffer(cc, scc, sccl)
//            println(scc.value.getString(0, sccl.value.toInt()))
//            scc.free()
//        }
//
//    }


}

