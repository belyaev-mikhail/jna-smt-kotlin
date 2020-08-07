package ru.spbstu.stp

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import ru.spbstu.common.jna.Out
import ru.spbstu.common.jna.OutArr
import ru.spbstu.common.jna.StringByReference

internal interface STPNativeLibrary: Library {
    companion object {
        val INSTANCE by lazy { Native.load("stp", STPNativeLibrary::class.java)!! }
    }

    class VC: PointerType()
    class Expr: PointerType()
    class Type: PointerType()
    class WholeCounterExample: PointerType()

    //! \brief Deprecated: use process_argument instead!
    //!
    //! Sets flags for the validity checker.
    //! For more information about this look into the documentation of process_argument.
    //!
    //! Parameter num_absrefine has no effect in the current implementation.
    //! It is left for compatibility with existing code.
    //!
    fun vc_setFlags(
        vc: VC, c: Char,
        num_absrefine: Int /* = 0 */
    )

    //! \brief Deprecated: use process_argument instead!
    //!
    //! Sets flags for the validity checker.
    //! For more information about this look into the documentation of process_argument.
    //!
    fun vc_setFlag(vc: VC, c: Char)

    //! Interface-only flags.
    //!
    enum class ifaceflag_t {
        //! Tells the validity checker that it is responsible for resource
        //! deallocation of its allocated expressions.
        //!
        //! This is set to true by default.
        //!
        //! Affected methods are:
        //!  - vc_arrayType
        //!  - vc_boolType
        //!  - vc_bvType
        //!  - vc_bv32Type
        //!  - vc_vcConstExprFromInt
        //!
        //! Changing this flag while STP is running may result in undefined behaviour.
        //!
        //! Use this with great care; otherwise memory leaks are very easily possible!
        //!
        EXPRDELETE,  //! Use the minisat SAT solver.

        //!
        MS,  //! Use a simplifying version of the minisat SAT solver.

        //!
        SMS,  //! Use the crypto minisat version 4 or higher (currently version 5) solver.

        //!
        CMS4,  //! Use the SAT solver Riss.

        //!
        RISS,  //! \brief Deprecated: use `MS` instead!

        //!
        //! This used to be the array version of the minisat SAT solver.
        //!
        //! Currently simply forwards to MS.
        //!
        MSP
    }

    //! \brief Sets the given interface flag for the given validity checker to param_value.
    //!
    //! Use this to set the underlying SAT solver used by STP or to change
    //! the global behaviour for expression ownership semantics via EXPRDELETE.
    //!
    fun vc_setInterfaceFlags(
        vc: VC, f: ifaceflag_t,
        param_value: Int
    )

    //! \brief Deprecated: this functionality is no longer needed!
    //!
    //! Since recent versions of STP division is always total.
    fun make_division_total(vc: VC)

    //! \brief Creates a new instance of an STP validity checker.
    //!
    //! Validity checker is the context for all STP resources like expressions,
    //! type and counter examples that may be generated while running STP.
    //!
    //! It is also the interface for assertions and queries.
    //!
    fun vc_createValidityChecker(): VC

    //! \brief Returns the boolean type for the given validity checker.
    //!
    fun VC.vc_boolType(): Type

    //! \brief Returns an array type with the given index type and data type
    //!        for the given validity checker.
    //!
    //! Note that index type and data type must both be of bitvector (bv) type.
    //!
    fun VC.vc_arrayType(typeIndex: Type, typeData: Type): Type

/////////////////////////////////////////////////////////////////////////////
/// EXPR MANUPULATION METHODS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// EXPR MANUPULATION METHODS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns a variable (symbol) expression with the given name and type.
    //!
    //! The type cannot be a function type. (TODO: Are function type still a thing in STP)
    //!
    //! The variable name must only consist of alphanumerics and underscore
    //! characters, otherwise this may behave in undefined ways, e.g. segfault.
    //!
    fun VC.vc_varExpr(name: String, type: Type): Expr

    //! \brief Similar to vc_varExpr but more bare metal. Do not use this unless
    //!        you really know what you are doing!
    //!
    //! Note: This should be deprecated in favor of the saner vc_varExpr API
    //! and as this API leaks implementation details of STP.
    //!
    //! The variable name must only consist of alphanumerics and underscore
    //! characters, otherwise this may behave in undefined ways, e.g. segfault.
    //!
    fun VC.vc_varExpr1(name: String, indexwidth: Int, valuewidth: Int): Expr

    //! \brief Returns the type of the given expression.
    //!
    fun VC.vc_getType(e: Expr): Type

    //! \brief Returns the bit-width of the given bitvector.
    //!
    fun VC.vc_getBVLength(e: Expr): Int

    //! \brief Create an equality expression. The two children must have the same type.
    //!
    //! Returns a boolean expression.
    //!
    fun VC.vc_eqExpr(child0: Expr, child1: Expr): Expr

/////////////////////////////////////////////////////////////////////////////
/// BOOLEAN EXPRESSIONS
///
/// The following functions create boolean expressions.
/// The children provided as arguments must be of type boolean.
///
/// An exception is the function vc_iteExpr().
/// In the case of vc_iteExpr() the conditional must always be boolean,
/// but the thenExpr (resp. elseExpr) can be bit-vector or boolean type.
/// However, the thenExpr and elseExpr must be both of the same type.
///
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// BOOLEAN EXPRESSIONS
    ///
    /// The following functions create boolean expressions.
    /// The children provided as arguments must be of type boolean.
    ///
    /// An exception is the function vc_iteExpr().
    /// In the case of vc_iteExpr() the conditional must always be boolean,
    /// but the thenExpr (resp. elseExpr) can be bit-vector or boolean type.
    /// However, the thenExpr and elseExpr must be both of the same type.
    ///
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Creates a boolean expression that represents true.
    //!
    fun VC.vc_trueExpr(): Expr

    //! \brief Creates a boolean expression that represents false.
    //!
    fun VC.vc_falseExpr(): Expr

    //! \brief Creates a boolean not expression that logically negates its child.
    //!
    fun VC.vc_notExpr(child: Expr): Expr

    //! \brief Creates a binary and-expression that represents a conjunction
    //!        of the given boolean child expressions.
    //!
    fun VC.vc_andExpr(left: Expr, right: Expr): Expr

    //! \brief Creates an and-expression with multiple child boolean expressions
    //!        that represents the conjunction of all of its child expressions.
    //!
    //! This API is useful since SMTLib2 defines non-binary expressions for logical-and.
    //!
    fun VC.vc_andExprN(children: Array<Expr>, numOfChildNodes: Int): Expr

    //! \brief Creates a binary or-expression that represents a disjunction
    //!        of the given boolean child expressions.
    //!
    fun vc_orExpr(vc: VC, left: Expr, right: Expr): Expr

    //! \brief Creates an or-expression with multiple child boolean expressions
    //!        that represents the disjunction of all of its child expressions.
    //!
    //! This API is useful since SMTLib2 defines non-binary expressions for logical-or.
    //!
    fun VC.vc_orExprN(children: Array<Expr>, numOfChildNodes: Int): Expr

    //! \brief Creates a binary xor-expressions for the given boolean child expressions.
    //!
    fun VC.vc_xorExpr(left: Expr, right: Expr): Expr

    //! \brief Creates an implies-expression for the given hyp (hypothesis) and
    //!        conc (conclusion) boolean expressions.
    //!
    fun VC.vc_impliesExpr(hyp: Expr, conc: Expr): Expr

    //! \brief Creates an if-and-only-if-expression for the given boolean expressions.
    //!
    fun VC.vc_iffExpr(left: Expr, right: Expr): Expr

    //! \brief Creates an if-then-else-expression for the given conditional boolean expression
    //!        and its then and else expressions which must be of the same type.
    //!
    //! The output type of this API may be of boolean or bitvector type.
    //!
    fun VC.vc_iteExpr(conditional: Expr, thenExpr: Expr, elseExpr: Expr): Expr

    //! \brief Returns a bitvector expression from the given boolean expression.
    //!
    //! Returns a constant bitvector expression that represents one (1) if
    //! the given boolean expression was false or returns a bitvector expression
    //! representing zero (0) otherwise.
    //!
    //! Panics if the given expression is not of boolean type.
    //!
    fun VC.vc_boolToBVExpr(form: Expr): Expr

    //! \brief Creates a parameterized boolean expression with the given boolean
    //!        variable expression and the parameter param.
    //!
    fun VC.vc_paramBoolExpr(`var`: Expr, param: Expr): Expr

/////////////////////////////////////////////////////////////////////////////
/// ARRAY EXPRESSIONS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// ARRAY EXPRESSIONS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns an array-read-expression representing the reading of
    //!        the given array's entry of the given index.
    //!
    //! The array parameter must be of type array and index must be of type bitvector.
    //!
    fun VC.vc_readExpr(array: Expr, index: Expr): Expr

    //! \brief Returns an array-write-expressions representing the writing of
    //!        the given new value into the given array at the given entry index.
    //!
    //! The array parameter must be of type array, and index and newValue of type bitvector.
    //!
    fun VC.vc_writeExpr(
        array: Expr,
        index: Expr,
        newValue: Expr
    ): Expr

    //! \brief Parses the expression stored in the file of the given filepath
    //!        and returns it on success.
    //!
    //! TODO: What format is expected SMTLib2?
    //!       Does the user have to deallocate resources for the returned expression
    //!       Why exactly is this "pretty cool!"?
    //!
    fun VC.vc_parseExpr(filepath: String): Expr

    //! \brief Prints the given expression to stdout in the presentation language.
    //!
    fun VC.vc_printExpr(e: Expr)

    //! \brief Prints the given expression to stdout as C code.
    //!
    fun VC.vc_printExprCCode(e: Expr)

    //! \brief Prints the given expression to stdout in the STMLib2 format.
    //!
    fun VC.vc_printSMTLIB(e: Expr): String

    //! \brief Prints the given expression into the file with the given file descriptor
    //!        in the presentation language.
    //!
    fun VC.vc_printExprFile(e: Expr, fd: Int)

// //! \brief Prints the state of the given validity checker into
// //!        buffer allocated by STP stores it into the given 'buf' alongside
// //!        its length into 'len'.
// //!
// //! It is the responsibility of the caller to free the buffer.
// //!
// void vc_printStateToBuffer(VC vc, char **buf, unsigned long *len);

    // //! \brief Prints the state of the given validity checker into
    // //!        buffer allocated by STP stores it into the given 'buf' alongside
    // //!        its length into 'len'.
    // //!
    // //! It is the responsibility of the caller to free the buffer.
    // //!
    // void vc_printStateToBuffer(VC vc, char **buf, unsigned long *len);
    //! \brief Prints the given expression into a buffer allocated by STP.
    //!
    //! The buffer is returned via output parameter 'buf' alongside its length 'len'.
    //! It is the responsibility of the caller to free the memory afterwards.
    fun VC.vc_printExprToBuffer(
        e: Expr, buf: PointerByReference, len: LongByReference
    )

    //! \brief Prints the counter example after an invalid query to stdout.
    //!
    //! This method should only be called after a query which returns false.
    //!
    fun VC.vc_printCounterExample()

    //! \brief Prints variable declarations to stdout.
    //!
    fun VC.vc_printVarDecls()

    //! \brief Clears the internal list of variables that are maintained
    //!        for printing purposes via 'vc_printVarDecls'.
    //!
    //! A user may want to do this after finishing printing the variable
    //! declarations to prevent memory leaks.
    //! This is also useful if printing of declarations is never wanted.
    //!
    fun VC.vc_clearDecls()

    //! \brief Prints assertions to stdout.
    //!
    //! The validity checker's flag 'simplify_print' must be set to '1'
    //! to enable simplifications of the asserted formulas during printing.
    //!
    fun VC.vc_printAsserts(simplify_print: Int /* = 0 */)

    //! \brief Prints the state of the query to a buffer allocated by STP
    //!        that is returned via output parameter 'buf' alongside its
    //!        length in 'len'.
    //!
    //! It is the callers responsibility to free the buffer's memory.
    //!
    //! The validity checker's flag 'simplify_print' must be set to '1'
    //! to enable simplifications of the query state during printing.
    //!
    fun VC.vc_printQueryStateToBuffer(
        e: Expr, buf: StringByReference, len: LongByReference,
        simplify_print: Int
    )

    //! \brief Prints the found counter example to a buffer allocated by STP
    //!        that is returned via output parameter 'buf' alongside its
    //!        length in 'len'.
    //!
    //! It is the callers responsibility to free the buffer's memory.
    //!
    //! The validity checker's flag 'simplify_print' must be set to '1'
    //! to enable simplifications of the counter example during printing.
    //!
    fun VC.vc_printCounterExampleToBuffer(
        buf: PointerByReference, len: LongByReference
    )

    //! \brief Prints the query to stdout in presentation language.
    //!
    fun VC.vc_printQuery()

/////////////////////////////////////////////////////////////////////////////
/// CONTEXT RELATED METHODS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// CONTEXT RELATED METHODS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Adds the given expression as assertion to the given validity checker.
    //!
    //! The expression must be of type boolean.
    //!
    fun VC.vc_assertFormula(e: Expr)

    //! \brief Simplifies the given expression with respect to the given validity checker.
    //!
    fun VC.vc_simplify(e: Expr): Expr

    //! \brief Checks the validity of the given expression 'e' in the given context.
    //!
    //! 'timeout_max_conflicts' is represented and expected as the number of conflicts
    //! 'timeout_max_time' is represented and expected in seconds.
    //! The given expression 'e' must be of type boolean.
    //!
    //! Returns ...
    //!   0: if 'e' is INVALID
    //!   1: if 'e' is VALID
    //!   2: if errors occured
    //!   3: if the timeout was reached
    //!
    //! Note: Only the cryptominisat solver supports timeout_max_time
    //!
    fun VC.vc_query_with_timeout(
        e: Expr,
        timeout_max_conflicts: Int,
        timeout_max_time: Int
    ): Int

    //! \brief Checks the validity of the given expression 'e' in the given context
    //!        with an unlimited timeout.
    //!
    //! This simply forwards to 'vc_query_with_timeout'.
    //!
    //! Note: Read the documentation of 'vc_query_with_timeout' for more information
    //!       about subtle details.
    //!
    fun VC.vc_query(e: Expr): Int

    //! \brief Returns the counter example after an invalid query.
    //!
    fun VC.vc_getCounterExample(e: Expr): Expr

    //! \brief Returns an array from a counter example after an invalid query.
    //!
    //! The buffer for the array is allocated by STP and returned via the
    //! non-null expected out parameters 'outIndices' for the indices, 'outValues'
    //! for the values and 'outSize' for the size of the array.
    //!
    //! It is the caller's responsibility to free the memory afterwards.
    //!
    fun VC.vc_getCounterExampleArray(
        e: Expr,
        outIndices: OutArr<Expr>,
        outValues: OutArr<Expr>,
        outSize: IntByReference
    )

    //! \brief Returns the size of the counter example array,
    //!        i.e. the number of variable and array locations
    //!        in the counter example.
    //!
    fun VC.vc_counterexample_size(): Int

    //! \brief Checkpoints the current context and increases the scope level.
    //!
    //! TODO: What effects has this
    //!
    fun VC.vc_push()

    //! \brief Restores the current context to its state at the last checkpoint.
    //!
    //! TODO: What effects has this
    //!
    fun VC.vc_pop()

    //! \brief Returns the associated integer from the given bitvector expression.
    //!
    //! Panics if the given bitvector cannot be represented by an 'int'.
    //!
    fun getBVInt(e: Expr): Int

    //! \brief Returns the associated unsigned integer from the given bitvector expression.
    //!
    //! Panics if the given bitvector cannot be represented by an 'unsigned int'.
    //!
    fun getBVUnsigned(e: Expr): Int

//! Return an unsigned long long int from a constant bitvector expressions

    //! Return an unsigned long long int from a constant bitvector expressions
    //! \brief Returns the associated unsigned long long integer from the given bitvector expression.
    //!
    //! Panics if the given bitvector cannot be represented by an 'unsigned long long int'.
    //!
    fun getBVUnsignedLongLong(e: Expr): Long

/////////////////////////////////////////////////////////////////////////////
/// BITVECTOR OPERATIONS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// BITVECTOR OPERATIONS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns the bitvector type for the given validity checker.
    //!
    fun VC.vc_bvType(no_bits: Int): Type

    //! \brief Returns the bitvector type with a bit-width of 32 for the
    //!        given validity checker.
    //!
    //! This is equal to calling 'vc_bvType(vc, 32)'.
    //!
    //! Note: This is a convenience function that simply forwards its input.
    //!
    fun VC.vc_bv32Type(): Type

//Const expressions for string, int, long-long, etc

    //Const expressions for string, int, long-long, etc
    //! \brief Parses the given string and returns an associated bitvector expression.
    //!
    //! This function expects the input string to be of decimal format.
    //!
    fun VC.vc_bvConstExprFromDecStr(
        width: Int, decimalInput: String
    ): Expr

    //! \brief Parses the given string and returns an associated bitvector expression.
    //!
    //! This function expects the input string to be of binary format.
    //!
    fun VC.vc_bvConstExprFromStr(binaryInput: String): Expr

    //! \brief Returns a bitvector with 'bitWidth' bit-width from the given
    //!        unsigned integer value.
    //!
    //! The 'bitWidth' must be large enough to fully store the given value's bit representation.
    //!
    fun VC.vc_bvConstExprFromInt(bitWidth: Int, value: Int): Expr

    //! \brief Returns a bitvector with 'bitWidth' bit-width from the given
    //!        unsigned long long integer value.
    //!
    //! The 'bitWidth' must be large enough to fully store the given value's bit representation.
    //!
    fun VC.vc_bvConstExprFromLL(bitWidth: Int, value: Long): Expr

    //! \brief Returns a bitvector with a bit-width of 32 from the given
    //!        unsigned integer value.
    //!
    fun VC.vc_bv32ConstExprFromInt(value: Int): Expr

/////////////////////////////////////////////////////////////////////////////
/// BITVECTOR ARITHMETIC OPERATIONS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// BITVECTOR ARITHMETIC OPERATIONS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns a bitvector expression representing the concatenation of the two
    //!        given bitvector expressions.
    //!
    //! This results in a bitvector with the bit-width of the bit-width sum
    //! of its children.
    //!
    //! Example: Given bitvector 'a = 1101' and 'b = 1000' then 'vc_bvConcatExpr(vc, a, b)'
    //!          results in 'c = 11011000'.
    //!
    fun VC.vc_bvConcatExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression representing the addition of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_bvPlusExpr(bitWidth: Int, left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression representing the addition of the N
    //!        given bitvector expressions in the 'children' array.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_bvPlusExprN(
        bitWidth: Int,
        children: Array<Expr>?,
        numOfChildNodes: Int
    ): Expr

    //! \brief Returns a bitvector expression with a bit-width of 32
    //!        representing the addition of the two given bitvector expressions.
    //!
    //! The given bitvector expressions must have a bit-width of 32.
    //!
    fun VC.vc_bv32PlusExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the subtraction '(left - right)' of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_bvMinusExpr(bitWidth: Int, left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression with a bit-width of 32
    //!        representing the subtraction '(left - right)' of the given
    //!        bitvector expressions.
    //!
    //! The given bitvector expressions must have a bit-width of 32.
    //!
    fun VC.vc_bv32MinusExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the multiplication '(left * right)' of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_bvMultExpr(bitWidth: Int, left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression with a bit-width of 32
    //!        representing the multiplication '(left * right)' of the given
    //!        bitvector expressions.
    //!
    //! The given bitvector expressions must have a bit-width of 32.
    //!
    fun VC.vc_bv32MultExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the division '(dividend / divisor)' of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_bvDivExpr(bitWidth: Int, dividend: Expr, divisor: Expr): Expr

    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the modulo '(dividend % divisor)' of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_bvModExpr(bitWidth: Int, dividend: Expr, divisor: Expr): Expr

    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the modulo '(dividend % divisor)' of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_bvRemExpr(bitWidth: Int, dividend: Expr, divisor: Expr): Expr

    //! \brief Returns a (signed) bitvector expression with a bit-width of 'bitWidth'
    //!        representing the signed division '(dividend / divisor)' of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_sbvDivExpr(bitWidth: Int, dividend: Expr, divisor: Expr): Expr

    //! \brief Returns a (signed) bitvector expression with a bit-width of 'bitWidth'
    //!        representing the signed modulo '(dividend % divisor)' of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_sbvModExpr(bitWidth: Int, dividend: Expr, divisor: Expr): Expr

    //! \brief Returns a (signed) bitvector expression with a bit-width of 'bitWidth'
    //!        representing the signed remainder of the two
    //!        given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width as 'bitWidth'
    //!
    fun VC.vc_sbvRemExpr(bitWidth: Int, dividend: Expr, divisor: Expr): Expr

/////////////////////////////////////////////////////////////////////////////
/// BITVECTOR COMPARISON OPERATIONS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// BITVECTOR COMPARISON OPERATIONS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns a boolean expression representing the less-than
    //!        operation '(left < right)' of the given bitvector expressions.
    //!
    fun VC.vc_bvLtExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a boolean expression representing the less-equals
    //!        operation '(left <= right)' of the given bitvector expressions.
    //!
    fun VC.vc_bvLeExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a boolean expression representing the greater-than
    //!        operation '(left > right)' of the given bitvector expressions.
    //!
    fun VC.vc_bvGtExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a boolean expression representing the greater-equals
    //!        operation '(left >= right)' of the given bitvector expressions.
    //!
    fun VC.vc_bvGeExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a boolean expression representing the signed less-than
    //!        operation '(left < right)' of the given signed bitvector expressions.
    //!
    fun VC.vc_sbvLtExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a boolean expression representing the signed less-equals
    //!        operation '(left <= right)' of the given signed bitvector expressions.
    //!
    fun VC.vc_sbvLeExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a boolean expression representing the signed greater-than
    //!        operation '(left > right)' of the given signed bitvector expressions.
    //!
    fun VC.vc_sbvGtExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a boolean expression representing the signed greater-equals
    //!        operation '(left >= right)' of the given signed bitvector expressions.
    //!
    fun VC.vc_sbvGeExpr(left: Expr, right: Expr): Expr

/////////////////////////////////////////////////////////////////////////////
/// BITVECTOR BITWISE OPERATIONS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// BITVECTOR BITWISE OPERATIONS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns a bitvector expression representing the arithmetic
    //!        negation '(-a)' (unary minus) of the given child bitvector expression.
    //!
    fun VC.vc_bvUMinusExpr(child: Expr): Expr

    //! \brief Returns a bitvector expression representing the bitwise-and
    //!        operation '(a & b)' for the given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width.
    //!
    fun VC.vc_bvAndExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression representing the bitwise-or
    //!        operation '(a | b)' for the given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width.
    //!
    fun VC.vc_bvOrExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression representing the bitwise-xor
    //!        operation '(a ^ b)' for the given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width.
    //!
    fun VC.vc_bvXorExpr(left: Expr, right: Expr): Expr

    //! \brief Returns a bitvector expression representing the bitwise-not
    //!        operation '~a' for the given bitvector expressions.
    //!
    //! The given bitvector expressions must have the same bit-width.
    //!
    fun VC.vc_bvNotExpr(child: Expr): Expr

/////////////////////////////////////////////////////////////////////////////
/// BITVECTOR SHIFT OPERATIONS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// BITVECTOR SHIFT OPERATIONS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the left-shift operation '(left >> right)' of the
    //!        given bitvector expressions.
    //!
    //! Note: This is the new API for this kind of operation!
    //!
    fun VC.vc_bvLeftShiftExprExpr(
        bitWidth: Int, left: Expr, right: Expr
    ): Expr

    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the right-shift operation '(left << right)' of the
    //!        given bitvector expressions.
    //!
    //! Note: This is the new API for this kind of operation!
    //!
    fun VC.vc_bvRightShiftExprExpr(
        bitWidth: Int, left: Expr, right: Expr
    ): Expr

    //! \brief Returns a bitvector expression with a bit-width of 'bitWidth'
    //!        representing the signed right-shift operation '(left >> right)' of the
    //!        given bitvector expressions.
    //!
    //! Note: This is the new API for this kind of operation!
    //!
    fun VC.vc_bvSignedRightShiftExprExpr(
        bitWidth: Int, left: Expr, right: Expr
    ): Expr

    //! \brief Deprecated: Use the new API instead!
    //!
    //! Returns an expression representing the left-shift operation '(child << sh_amt)'
    //! for the given child bitvector expression.
    //!
    //! Note: Use 'vc_bvLeftShiftExprExpr' instead!
    //!
    fun VC.vc_bvLeftShiftExpr(sh_amt: Int, child: Expr): Expr

    //! \brief Deprecated: Use the new API instead!
    //!
    //! Returns an expression representing the right-shift operation '(child >> sh_amt)'
    //! for the given child bitvector expression.
    //!
    //! Note: Use 'vc_bvRightShiftExprExpr' instead!
    //!
    fun VC.vc_bvRightShiftExpr(sh_amt: Int, child: Expr): Expr

    //! \brief Deprecated: Use the new API instead!
    //!
    //! Returns a bitvector expression with a bit-width of 32
    //! representing the left-shift operation '(child << sh_amt)'
    //! for the given child bitvector expression.
    //!
    //! Note: Use 'vc_bvLeftShiftExprExpr' instead!
    //!
    fun VC.vc_bv32LeftShiftExpr(sh_amt: Int, child: Expr): Expr

    //! \brief Deprecated: Use the new API instead!
    //!
    //! Returns a bitvector expression with a bit-width of 32
    //! representing the right-shift operation '(child >> sh_amt)'
    //! for the given child bitvector expression.
    //!
    //! Note: Use 'vc_bvRightShiftExprExpr' instead!
    //!
    fun VC.vc_bv32RightShiftExpr(sh_amt: Int, child: Expr): Expr

    //! \brief Deprecated: Use the new API instead!
    //!
    //! Returns a bitvector expression with a bit-width of 32
    //! representing the left-shift operation '(child << sh_amt)'
    //! for the given child bitvector expression.
    //!
    //! Note: Use 'vc_bvLeftShiftExprExpr' instead!
    //!
    fun VC.vc_bvVar32LeftShiftExpr(sh_amt: Expr, child: Expr): Expr

    //! \brief Deprecated: Use the new API instead!
    //!
    //! Returns a bitvector expression with a bit-width of 32
    //! representing the right-shift operation '(child >> sh_amt)'
    //! for the given child bitvector expression.
    //!
    //! Note: Use 'vc_bvRightShiftExprExpr' instead!
    //!
    fun VC.vc_bvVar32RightShiftExpr(sh_amt: Expr, child: Expr): Expr

    //! \brief Deprecated: Use the new API instead!
    //!
    //! Returns a bitvector expression representing the division
    //! operation of the power of two '(child / 2^rhs)' for the given
    //! bitvector expressions.
    //!
    //! Note: Use 'vc_bvSignedRightShiftExprExpr' instead!
    //!
    fun VC.vc_bvVar32DivByPowOfTwoExpr(child: Expr, rhs: Expr): Expr

/////////////////////////////////////////////////////////////////////////////
/// BITVECTOR EXTRACTION & EXTENSION
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// BITVECTOR EXTRACTION & EXTENSION
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns a bitvector expression representing the extraction
    //!        of the bits within the range of 'low_bit_no' and 'high_bit_no'.
    //!
    //! Note: The resulting bitvector expression has a bit-width of 'high_bit_no - low_bit_no'.
    //!
    fun VC.vc_bvExtract(
        child: Expr, high_bit_no: Int, low_bit_no: Int
    ): Expr

    //! \brief Superseeded: Use 'vc_bvBoolExtract_Zero' or 'vc_bvBoolExtract_One' instead.
    //!
    //! Returns a boolean expression that accepts a bitvector expression 'x'
    //! and represents the following equation: '(x[bit_no:bit_no] == 0)'.
    //!
    //! Note: This is equal to calling 'vc_bvBoolExtract_Zero'.
    //!
    fun VC.vc_bvBoolExtract(x: Expr, bit_no: Int): Expr

    //! \brief Returns a boolean expression that accepts a bitvector expression 'x'
    //!        and represents the following equation: '(x[bit_no:bit_no] == 0)'.
    //!
    fun VC.vc_bvBoolExtract_Zero(x: Expr, bit_no: Int): Expr

    //! \brief Returns a boolean expression that accepts a bitvector expression 'x'
    //!        and represents the following equation: '(x[bit_no:bit_no] == 1)'.
    //!
    fun VC.vc_bvBoolExtract_One(x: Expr, bit_no: Int): Expr

    //! \brief Returns a bitvector expression representing the extension of the given
    //!        to the amount of bits given by 'newWidth'.
    //!
    //! Note: This operation retains the signedness of the bitvector is existant.
    //!
    fun VC.vc_bvSignExtend(child: Expr, newWidth: Int): Expr

/////////////////////////////////////////////////////////////////////////////
/// CONVENIENCE FUNCTIONS FOR ARRAYS
/////////////////////////////////////////////////////////////////////////////

    /*C pointer support:  C interface to support C memory arrays in CVCL */

    /////////////////////////////////////////////////////////////////////////////
    /// CONVENIENCE FUNCTIONS FOR ARRAYS
    /////////////////////////////////////////////////////////////////////////////
    /*C pointer support:  C interface to support C memory arrays in CVCL */ //! \brief Convenience function to create a named array expression with
    //!        an index bit-width of 32 and a value bit-width of 8.
    //!
    fun VC.vc_bvCreateMemoryArray(arrayName: String): Expr

    //! \brief Convenience function to read a bitvector with byte-width 'numOfBytes' of an
    //!        array expression created by 'vc_bvCreateMemoryArray' and return it.
    //!
    //! Note: This returns a bitvector expression with a bit-width of 'numOfBytes'.
    //!
    fun VC.vc_bvReadMemoryArray(
        array: Expr, byteIndex: Expr, numOfBytes: Int
    ): Expr

    //! \brief Convenience function to write a bitvector 'element' with byte-width 'numOfBytes'
    //!        into the given array expression at offset 'byteIndex'.
    //!
    fun VC.vc_bvWriteToMemoryArray(
        array: Expr, byteIndex: Expr, element: Expr,
        numOfBytes: Int
    ): Expr

/////////////////////////////////////////////////////////////////////////////
/// GENERAL EXPRESSION OPERATIONS
/////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    /// GENERAL EXPRESSION OPERATIONS
    /////////////////////////////////////////////////////////////////////////////
    //! \brief Returns a string representation of the given expression.
    //!
    //! Note:
    //!     The caller is responsible for deallocating the string afterwards.
    //!     The buffer that stores the string is allocated by STP.
    //!
    fun exprString(e: Expr): String

    //! \brief Returns a string representation of the given type.
    //!
    //! Note:
    //!     The caller is responsible for deallocating the string afterwards.
    //!     The buffer that stores the string is allocated by STP.
    //!
    fun typeString(t: Type): String

    //! \brief Returns the n-th child of the given expression.
    //!
    fun getChild(e: Expr, n: Int): Expr

    //! \brief Misleading name!
    //!
    //! Returns '1' if the given boolean expression evaluates to 'true',
    //! returns '0' if the given boolean expression evaluates to 'false',
    //! or returns '-1' otherwise, i.e. if the given expression was not a
    //! boolean expression.
    //!
    fun vc_isBool(e: Expr): Int

    //! \brief Registers the given error handler function to be called for each
    //!        fatal error that occures while running STP.
    //!
    fun vc_registerErrorHandler(errHandlr: Pointer)

    //! \brief Returns the hash of the given query state.
    //!
    fun VC.vc_getHashQueryStateToBuffer(query: Expr): Int

    //! \brief Destroy the given validity checker.
    //!
    //! Removes all associated expressions with it if 'EXPRDELETE' was set to 'true'
    //! via 'vc_setInterfaceFlags' during the process.
    //!
    fun VC.vc_Destroy()

    //! \brief Destroy the given expression, freeing its associated memory.
    //!
    fun Expr.vc_DeleteExpr()

    //! \brief Returns the whole counterexample from the given validity checker.
    //!
    fun VC.vc_getWholeCounterExample(): WholeCounterExample

    //! \brief Returns the value of the given term expression from the given whole counter example.
    //!
    fun VC.vc_getTermFromCounterExample(
        e: Expr, c: WholeCounterExample
    ): Expr

    //! \brief Destroys the given whole counter example, freeing all of its associated memory.
    //!
    fun vc_deleteWholeCounterExample(cc: WholeCounterExample)

    //! Covers all kinds of expressions that exist in STP.
    //!
    enum class exprkind_t {
        UNDEFINED,  //!< An undefined expression.
        SYMBOL,  //!< Named expression (or variable), i.e. created via 'vc_varExpr'.
        BVCONST,  //!< Bitvector constant expression, i.e. created via 'vc_bvConstExprFromInt'.
        BVNOT,  //!< Bitvector bitwise-not
        BVCONCAT,  //!< Bitvector concatenation
        BVOR,  //!< Bitvector bitwise-or
        BVAND,  //!< Bitvector bitwise-and
        BVXOR,  //!< Bitvector bitwise-xor
        BVNAND,  //!< Bitvector bitwise not-and; OR nand (TODO: does this still exist)
        BVNOR,  //!< Bitvector bitwise not-or; OR nor (TODO: does this still exist)
        BVXNOR,  //!< Bitvector bitwise not-xor; OR xnor (TODO: does this still exist)
        BVEXTRACT,  //!< Bitvector extraction, i.e. via 'vc_bvExtract'.
        BVLEFTSHIFT,  //!< Bitvector left-shift
        BVRIGHTSHIFT,  //!< Bitvector right-right
        BVSRSHIFT,  //!< Bitvector signed right-shift
        BVPLUS,  //!< Bitvector addition
        BVSUB,  //!< Bitvector subtraction
        BVUMINUS,  //!< Bitvector unary minus; OR negate expression
        BVMULT,  //!< Bitvector multiplication
        BVDIV,  //!< Bitvector division
        BVMOD,  //!< Bitvector modulo operation
        SBVDIV,  //!< Signed bitvector division
        SBVREM,  //!< Signed bitvector remainder
        SBVMOD,  //!< Signed bitvector modulo operation
        BVSX,  //!< Bitvector signed extend
        BVZX,  //!< Bitvector zero extend
        ITE,  //!< If-then-else
        BOOLEXTRACT,  //!< Bitvector boolean extraction
        BVLT,  //!< Bitvector less-than
        BVLE,  //!< Bitvector less-equals
        BVGT,  //!< Bitvector greater-than
        BVGE,  //!< Bitvector greater-equals
        BVSLT,  //!< Signed bitvector less-than
        BVSLE,  //!< Signed bitvector less-equals
        BVSGT,  //!< Signed bitvector greater-than
        BVSGE,  //!< Signed bitvector greater-equals
        EQ,  //!< Equality comparator
        FALSE,  //!< Constant false boolean expression
        TRUE,  //!< Constant true boolean expression
        NOT,  //!< Logical-not boolean expression
        AND,  //!< Logical-and boolean expression
        OR,  //!< Logical-or boolean expression
        NAND,  //!< Logical-not-and boolean expression (TODO: Does this still exist)
        NOR,  //!< Logical-not-or boolean expression (TODO: Does this still exist)
        XOR,  //!< Logical-xor (either-or) boolean expression
        IFF,  //!< If-and-only-if boolean expression
        IMPLIES,  //!< Implication boolean expression
        PARAMBOOL,  //!< Parameterized boolean expression
        READ,  //!< Array read expression
        WRITE,  //!< Array write expression
        ARRAY,  //!< Array creation expression
        BITVECTOR,  //!< Bitvector creation expression
        BOOLEAN //!< Boolean creation expression
    }

    //! \brief Returns the expression-kind of the given expression.
    //!
    fun getExprKind(e: Expr): exprkind_t

    //! \brief Returns the number of child expressions of the given expression.
    //!
    fun getDegree(e: Expr): Int

    //! \brief Returns the bit-width of the given bitvector expression.
    //!
    fun getBVLength(e: Expr): Int

    //! Covers all kinds of types that exist in STP.
    //!
    object STPTypeKinds {
        const val BOOLEAN_TYPE = 0;
        const val BITVECTOR_TYPE = BOOLEAN_TYPE + 1;
        const val ARRAY_TYPE = BITVECTOR_TYPE + 1;
        const val UNKNOWN_TYPE = ARRAY_TYPE + 1;
    }

    //! \brief Returns the type-kind of the given expression.
    //!
    fun getType(e: Expr): Int

// get value bit width

    // get value bit width
    //! \brief Returns the value bit-width of the given expression.
    //!
    //! This is mainly useful for array expression.
    //!
    fun getVWidth(e: Expr): Int

    //! \brief Returns the index bit-width of the given expression.
    //!
    //! This is mainly useful for array expression.
    //!
    fun getIWidth(e: Expr): Int

    //! \brief Prints the given counter example to the file that is
    //!        associated with the given open file descriptor.
    //!
    fun vc_printCounterExampleFile(vc: VC, fd: Int)

    //! \brief Returns the name of the given variable expression.
    //!
    fun exprName(e: Expr): String

    //! \brief Returns the internal node ID of the given expression.
    //!
    fun getExprID(ex: Expr): Int

    //! \brief Parses the given string in CVC or SMTLib1.0 format and extracts
    //!        query and assertion information into the 'outQuery' and 'outAsserts'
    //!        buffers respectively.
    //!
    //! It is the caller's responsibility to free the buffer's memory afterwards.
    //!
    //! Note: The user can controle the parsed format via 'process_argument'.
    //!
    //! Returns '1' if parsing was successful.
    //!
    fun vc_parseMemExpr(vc: VC, s: String, outQuery: Out<Expr>?, outAsserts: Out<Expr>?): Int

    //! \brief Checks if STP was compiled with support for minisat
    //!
    //!  Note: always returns true (future support for minisat being the
    //!  non-default)
    //!
    fun vc_supportsMinisat(vc: VC): Boolean

    //! \brief Sets underlying SAT solver to minisat
    //!
    fun vc_useMinisat(vc: VC): Boolean

    //! \brief Checks if underlying SAT solver is minisat
    //!
    fun vc_isUsingMinisat(vc: VC): Boolean

    //! \brief Checks if STP was compiled with support for simplifying minisat
    //!
    //!  Note: always returns true (future support for simplifying minisat being
    //!  the non-default)
    //!
    fun vc_supportsSimplifyingMinisat(vc: VC): Boolean

    //! \brief Sets underlying SAT solver to simplifying minisat
    //!
    fun vc_useSimplifyingMinisat(vc: VC): Boolean

    //! \brief Checks if underlying SAT solver is simplifying minisat
    //!
    fun vc_isUsingSimplifyingMinisat(vc: VC): Boolean

    //! \brief Checks if STP was compiled with support for cryptominisat
    //!
    fun vc_supportsCryptominisat(vc: VC): Boolean

    //! \brief Sets underlying SAT solver to cryptominisat
    //!
    fun vc_useCryptominisat(vc: VC): Boolean

    //! \brief Checks if underlying SAT solver is cryptominisat
    //!
    fun vc_isUsingCryptominisat(vc: VC): Boolean

    //! \brief Checks if STP was compiled with support for riss
    //!
    fun vc_supportsRiss(vc: VC): Boolean

    //! \brief Sets underlying SAT solver to riss
    //!
    fun vc_useRiss(vc: VC): Boolean

    //! \brief Checks if underlying SAT solver is riss
    //!
    fun vc_isUsingRiss(vc: VC): Boolean

}