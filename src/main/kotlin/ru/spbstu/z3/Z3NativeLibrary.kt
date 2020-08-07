package ru.spbstu.z3

import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import ru.spbstu.common.jna.Out
import ru.spbstu.common.jna.StringByReference

internal interface Z3NativeLibrary: Library {
    class Config: PointerType()
    class Context: PointerType()
    class Solver: PointerType()
    class Sort: PointerType()
    class Ast: PointerType()
    class Params: PointerType()
    class Symbol: PointerType()
    class FuncDecl: PointerType()
    class ParamDescrs: PointerType()
    class Constructor: PointerType()
    class ConstructorList: PointerType()
    class Pattern: PointerType()
    class App: PointerType()
    class Model: PointerType()
    class AstVector: PointerType()
    class FuncInterp: PointerType()
    class FuncEntry: PointerType()
    class Goal: PointerType()
    class Tactic: PointerType()
    class Probe: PointerType()
    class ApplyResult: PointerType()
    class Stats: PointerType()

    companion object {
        object ErrorCode {
            val Z3_OK = 0
            val Z3_SORT_ERROR = 1
            val Z3_IOB = 2
            val Z3_INVALID_ARG = 3
            val Z3_PARSER_ERROR = 4
            val Z3_NO_PARSER = 5
            val Z3_INVALID_PATTERN = 6
            val Z3_MEMOUT_FAIL = 7
            val Z3_FILE_ACCESS_ERROR = 8
            val Z3_INTERNAL_FATAL = 9
            val Z3_INVALID_USAGE = 10
            val Z3_DEC_REF_ERROR = 11
            val Z3_EXCEPTIO = 12
        }

        val INSTANCE by lazy {
            Native.load("z3", Z3NativeLibrary::class.java)!!
        }
    }

    /** @name Global Parameters */

    /*@{*/
    /** @name Global Parameters
     */
    /*@{*/
    /**
     * \brief Set a global (or module) parameter.
     * This setting is shared by all Z3 contexts.
     *
     * When a Z3 module is initialized it will use the value of these parameters
     * when Z3_params objects are not provided.
     *
     * The name of parameter can be composed of characters [a-z][A-Z], digits [0-9], '-' and '_'.
     * The character '.' is a delimiter (more later).
     *
     * The parameter names are case-insensitive. The character '-' should be viewed as an "alias" for '_'.
     * Thus, the following parameter names are considered equivalent: "pp.decimal-precision"  and "PP.DECIMAL_PRECISION".
     *
     * This function can be used to set parameters for a specific Z3 module.
     * This can be done by using <module-name>.<parameter-name>.
     * For example:
     * Z3_global_param_set('pp.decimal', 'true')
     * will set the parameter "decimal" in the module "pp" to true.
     *
     * \sa Z3_global_param_get
     * \sa Z3_global_param_reset_all
     *
     * def_API('Z3_global_param_set', VOID, (_in(STRING), _in(STRING)))
    </parameter-name></module-name> */
    fun Z3_global_param_set(param_id: String, param_value: String)


    /**
     * \brief Restore the value of all global (and module) parameters.
     * This command will not affect already created objects (such as tactics and solvers).
     *
     * \sa Z3_global_param_get
     * \sa Z3_global_param_set
     *
     * def_API('Z3_global_param_reset_all', VOID, ())
     */
    fun Z3_global_param_reset_all()

    /**
     * \brief Get a global (or module) parameter.
     *
     * Returns \c false if the parameter value does not exist.
     *
     * \sa Z3_global_param_reset_all
     * \sa Z3_global_param_set
     *
     * \remark This function cannot be invoked simultaneously from different threads without synchronization.
     * The result string stored in param_value is stored in shared location.
     *
     * def_API('Z3_global_param_get', boolean, (_in(STRING), _out(STRING)))
     */
    fun Z3_global_param_get(param_id: String, param_value: StringByReference): Boolean

    /*@}*/

    /** @name Create configuration */
    /*@{*/

    /*@}*/
    /** @name Create configuration
     */
    /*@{*/
    /**
     * \brief Create a configuration object for the Z3 context object.
     *
     * Configurations are created in order to assign parameters prior to creating
     * contexts for Z3 interaction. For example, if the users wishes to use proof
     * generation, then call:
     *
     * \ccode{Z3_set_param_value(cfg\, "proof"\, "true")}
     *
     * \remark In previous versions of Z3, the \c Z3Config was used to store
     * global and module configurations. Now, we should use \c Z3_global_param_set.
     *
     * The following parameters can be set:
     *
     * - proof  (boolean)           Enable proof generation
     * - debug_ref_count (boolean)  Enable debug support for Z3_ast reference counting
     * - trace  (boolean)           Tracing support for VCC
     * - trace_file_name (String)   Trace out file for VCC traces
     * - timeout (int)         default timeout (in milliseconds) used for solvers
     * - well_sorted_check          type checker
     * - auto_config                use heuristics to automatically select solver and configure it
     * - model                      model generation for solvers, this parameter can be overwritten when creating a solver
     * - model_validate             validate models produced by solvers
     * - unsat_core                 unsat-core generation for solvers, this parameter can be overwritten when creating a solver
     *
     * \sa Z3_set_param_value
     * \sa Z3_del_config
     *
     * def_API('Z3_mk_config', CONFIG, ())
     */
    fun Z3_mk_config(): Config

    /**
     * \brief Delete the given configuration object.
     *
     * \sa Z3_mk_config
     *
     * def_API('Z3_del_config', VOID, (_in(CONFIG),))
     */
    fun Config.Z3_del_config()

    /**
     * \brief Set a configuration parameter.
     *
     * The following parameters can be set for
     *
     * \sa Z3_mk_config
     *
     * def_API('Z3_set_param_value', VOID, (_in(CONFIG), _in(STRING), _in(STRING)))
     */
    fun Config.Z3_set_param_value(param_id: String, param_value: String)

    /*@}*/

    /** @name Context and AST Reference Counting */
    /*@{*/

    /*@}*/
    /** @name Context and AST Reference Counting
     */
    /*@{*/
    /**
     * \brief Create a context using the given configuration.
     *
     * After a context is created, the configuration cannot be changed,
     * although some parameters can be changed using #Z3_update_param_value.
     * All main interaction with Z3 happens in the context of a \c Z3Context.
     *
     * In contrast to #Z3_mk_context_rc, the life time of \c Z3_ast objects
     * are determined by the scope level of #Z3_solver_push and #Z3_solver_pop.
     * In other words, a \c Z3_ast object remains valid until there is a
     * call to #Z3_solver_pop that takes the current scope below the level where
     * the object was created.
     *
     * Note that all other reference counted objects, including \c Z3_model,
     * \c Z3Solver, \c Z3_func_interp have to be managed by the caller.
     * Their reference counts are not handled by the context.
     *
     * Further remarks:
     * - \c Z3_sort, \c Z3_func_decl, \c Z3_app, \c Z3_pattern are \c Z3_ast's.
     * - Z3 uses hash-consing, i.e., when the same \c Z3_ast is created twice,
     * Z3 will return the same pointer twice.
     *
     * \sa Z3_del_context
     *
     * def_API('Z3_mk_context', CONTEXT, (_in(CONFIG),))
     */
    fun Config.Z3_mk_context(): Context

    /**
     * \brief Create a context using the given configuration.
     * This function is similar to #Z3_mk_context. However,
     * in the context returned by this function, the user
     * is responsible for managing \c Z3_ast reference counters.
     * Managing reference counters is a burden and error-prone,
     * but allows the user to use the memory more efficiently.
     * The user must invoke #Z3_inc_ref for any \c Z3_ast returned
     * by Z3, and #Z3_dec_ref whenever the \c Z3_ast is not needed
     * anymore. This idiom is similar to the one used in
     * BDD (binary decision diagrams) packages such as CUDD.
     *
     * Remarks:
     *
     * - \c Z3_sort, \c Z3_func_decl, \c Z3_app, \c Z3_pattern are \c Z3_ast's.
     * - After a context is created, the configuration cannot be changed.
     * - All main interaction with Z3 happens in the context of a \c Z3Context.
     * - Z3 uses hash-consing, i.e., when the same \c Z3_ast is created twice,
     * Z3 will return the same pointer twice.
     *
     * def_API('Z3_mk_context_rc', CONTEXT, (_in(CONFIG),))
     */
    fun Config.Z3_mk_context_rc(): Context

    /**
     * \brief Delete the given logical context.
     *
     * \sa Z3_mk_context
     *
     * def_API('Z3_del_context', VOID, (_in(CONTEXT),))
     */
    fun Context.Z3_del_context()

    /**
     * \brief Increment the reference counter of the given AST.
     * The context \c c should have been created using #Z3_mk_context_rc.
     * This function is a NOOP if \c c was created using #Z3_mk_context.
     *
     * def_API('Z3_inc_ref', VOID, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_inc_ref(a: Ast)

    /**
     * \brief Decrement the reference counter of the given AST.
     * The context \c c should have been created using #Z3_mk_context_rc.
     * This function is a NOOP if \c c was created using #Z3_mk_context.
     *
     * def_API('Z3_dec_ref', VOID, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_dec_ref(a: Ast)

    /**
     * \brief Set a value of a context parameter.
     *
     * \sa Z3_global_param_set
     *
     * def_API('Z3_update_param_value', VOID, (_in(CONTEXT), _in(STRING), _in(STRING)))
     */
    fun Context.Z3_update_param_value(param_id: String, param_value: String)

    /**
     * \brief Interrupt the execution of a Z3 procedure.
     * This procedure can be used to interrupt: solvers, simplifiers and tactics.
     *
     * def_API('Z3_interrupt', VOID, (_in(CONTEXT),))
     */
    fun Context.Z3_interrupt()


    /*@}*/

    /** @name Parameters */
    /*@{*/

    /*@}*/
    /** @name Parameters
     */
    /*@{*/
    /**
     * \brief Create a Z3 (empty) parameter set.
     * Starting at Z3 4.0, parameter sets are used to configure many components such as:
     * simplifiers, tactics, solvers, etc.
     *
     * \remark Reference counting must be used to manage parameter sets, even when the \c Z3Context was
     * created using #Z3_mk_context instead of #Z3_mk_context_rc.
     *
     * def_API('Z3_mk_params', PARAMS, (_in(CONTEXT),))
     */
    fun Context.Z3_mk_params(): Params

    /**
     * \brief Increment the reference counter of the given parameter set.
     *
     * def_API('Z3_params_inc_ref', VOID, (_in(CONTEXT), _in(PARAMS)))
     */
    fun Context.Z3_params_inc_ref(p: Params)

    /**
     * \brief Decrement the reference counter of the given parameter set.
     *
     * def_API('Z3_params_dec_ref', VOID, (_in(CONTEXT), _in(PARAMS)))
     */
    fun Context.Z3_params_dec_ref(p: Params)

    /**
     * \brief Add a boolean parameter \c k with value \c v to the parameter set \c p.
     *
     * def_API('Z3_params_set_boolean', VOID, (_in(CONTEXT), _in(PARAMS), _in(SYMBOL), _in(boolean)))
     */
    fun Context.Z3_params_set_boolean(
        p: Params,
        k: Symbol,
        v: Boolean
    )

    /**
     * \brief Add a int parameter \c k with value \c v to the parameter set \c p.
     *
     * def_API('Z3_params_set_uint', VOID, (_in(CONTEXT), _in(PARAMS), _in(SYMBOL), _in(UINT)))
     */
    fun Context.Z3_params_set_uint(
        p: Params,
        k: Symbol,
        v: Int
    )

    /**
     * \brief Add a double parameter \c k with value \c v to the parameter set \c p.
     *
     * def_API('Z3_params_set_double', VOID, (_in(CONTEXT), _in(PARAMS), _in(SYMBOL), _in(DOUBLE)))
     */
    fun Context.Z3_params_set_double(
        p: Params,
        k: Symbol,
        v: Double
    )

    /**
     * \brief Add a symbol parameter \c k with value \c v to the parameter set \c p.
     *
     * def_API('Z3_params_set_symbol', VOID, (_in(CONTEXT), _in(PARAMS), _in(SYMBOL), _in(SYMBOL)))
     */
    fun Context.Z3_params_set_symbol(
        p: Params,
        k: Symbol,
        v: Symbol
    )

    /**
     * \brief Convert a parameter set into a string. This function is mainly used for printing the
     * contents of a parameter set.
     *
     * def_API('Z3_params_to_string', STRING, (_in(CONTEXT), _in(PARAMS)))
     */
    fun Context.Z3_params_to_string(p: Params): String

    /**
     * \brief Validate the parameter set \c p against the parameter description set \c d.
     *
     * The procedure invokes the error handler if \c p is invalid.
     *
     * def_API('Z3_params_validate', VOID, (_in(CONTEXT), _in(PARAMS), _in(PARAM_DESCRS)))
     */
    fun Context.Z3_params_validate(
        p: Params,
        d: ParamDescrs
    )

    /*@}*/

    /** @name Parameter Descriptions */
    /*@{*/

    /*@}*/
    /** @name Parameter Descriptions
     */
    /*@{*/
    /**
     * \brief Increment the reference counter of the given parameter description set.
     *
     * def_API('Z3_param_descrs_inc_ref', VOID, (_in(CONTEXT), _in(PARAM_DESCRS)))
     */
    fun Context.Z3_param_descrs_inc_ref(p: ParamDescrs)

    /**
     * \brief Decrement the reference counter of the given parameter description set.
     *
     * def_API('Z3_param_descrs_dec_ref', VOID, (_in(CONTEXT), _in(PARAM_DESCRS)))
     */
    fun Context.Z3_param_descrs_dec_ref(p: ParamDescrs)

    /**
     * \brief Return the kind associated with the given parameter name \c n.
     *
     * def_API('Z3_param_descrs_get_kind', UINT, (_in(CONTEXT), _in(PARAM_DESCRS), _in(SYMBOL)))
     */
    fun Context.Z3_param_descrs_get_kind(
        p: ParamDescrs,
        n: Symbol
    ): Int

    /**
     * \brief Return the number of parameters in the given parameter description set.
     *
     * def_API('Z3_param_descrs_size', UINT, (_in(CONTEXT), _in(PARAM_DESCRS)))
     */
    fun Context.Z3_param_descrs_size(p: ParamDescrs): Int

    /**
     * \brief Return the name of the parameter at given index \c i.
     *
     * \pre i < Z3_param_descrs_size(c, p)
     *
     * def_API('Z3_param_descrs_get_name', SYMBOL, (_in(CONTEXT), _in(PARAM_DESCRS), _in(UINT)))
     */
    fun Context.Z3_param_descrs_get_name(p: ParamDescrs, i: Int): Symbol

    /**
     * \brief Retrieve documentation string corresponding to parameter name \c s.
     *
     * def_API('Z3_param_descrs_get_documentation', STRING, (_in(CONTEXT), _in(PARAM_DESCRS), _in(SYMBOL)))
     */
    fun Context.Z3_param_descrs_get_documentation(
        p: ParamDescrs,
        s: Symbol
    ): String

    /**
     * \brief Convert a parameter description set into a string. This function is mainly used for printing the
     * contents of a parameter description set.
     *
     * def_API('Z3_param_descrs_to_string', STRING, (_in(CONTEXT), _in(PARAM_DESCRS)))
     */
    fun Context.Z3_param_descrs_to_string(p: ParamDescrs): String

    /*@}*/

    /** @name Symbols */
    /*@{*/

    /*@}*/
    /** @name Symbols
     */
    /*@{*/
    /**
     * \brief Create a Z3 symbol using an integer.
     *
     * Symbols are used to name several term and type constructors.
     *
     * NB. Not all integers can be passed to this function.
     * The legal range of int integers is 0 to 2^30-1.
     *
     * \sa Z3_get_symbol_int
     * \sa Z3_mk_string_symbol
     *
     * def_API('Z3_mk_int_symbol', SYMBOL, (_in(CONTEXT), _in(INT)))
     */
    fun Context.Z3_mk_int_symbol(i: Int): Symbol

    /**
     * \brief Create a Z3 symbol using a C string.
     *
     * Symbols are used to name several term and type constructors.
     *
     * \sa Z3_get_symbol_string
     * \sa Z3_mk_int_symbol
     *
     * def_API('Z3_mk_string_symbol', SYMBOL, (_in(CONTEXT), _in(STRING)))
     */
    fun Context.Z3_mk_string_symbol(s: String): Symbol

    /*@}*/

    /** @name Sorts */
    /*@{*/

    /*@}*/
    /** @name Sorts
     */
    /*@{*/
    /**
     * \brief Create a free (uninterpreted) type using the given name (symbol).
     *
     * Two free types are considered the same iff the have the same name.
     *
     * def_API('Z3_mk_uninterpreted_sort', SORT, (_in(CONTEXT), _in(SYMBOL)))
     */
    fun Context.Z3_mk_uninterpreted_sort(s: Symbol): Sort

    /**
     * \brief Create the boolean type.
     *
     * This type is used to create propositional variables and predicates.
     *
     * def_API('Z3_mk_bool_sort', SORT, (_in(CONTEXT), ))
     */
    fun Context.Z3_mk_bool_sort(): Sort

    /**
     * \brief Create the integer type.
     *
     * This type is not the int type found in programming languages.
     * A machine integer can be represented using bit-vectors. The function
     * #Z3_mk_bv_sort creates a bit-vector type.
     *
     * \sa Z3_mk_bv_sort
     *
     * def_API('Z3_mk_int_sort', SORT, (_in(CONTEXT), ))
     */
    fun Context.Z3_mk_int_sort(): Sort

    /**
     * \brief Create the real type.
     *
     * Note that this type is not a floating point number.
     *
     * def_API('Z3_mk_real_sort', SORT, (_in(CONTEXT), ))
     */
    fun Context.Z3_mk_real_sort(): Sort

    /**
     * \brief Create a bit-vector type of the given size.
     *
     * This type can also be seen as a machine integer.
     *
     * \remark The size of the bit-vector type must be greater than zero.
     *
     * def_API('Z3_mk_bv_sort', SORT, (_in(CONTEXT), _in(UINT)))
     */
    fun Context.Z3_mk_bv_sort(sz: Int): Sort

    /**
     * \brief Create a named finite domain sort.
     *
     * To create constants that belong to the finite domain,
     * use the APIs for creating numerals and pass a numeric
     * constant together with the sort returned by this call.
     * The numeric constant should be between 0 and the less
     * than the size of the domain.
     *
     * \sa Z3_get_finite_domain_sort_size
     *
     * def_API('Z3_mk_finite_domain_sort', SORT, (_in(CONTEXT), _in(SYMBOL), _in(UINT64)))
     */
    fun Context.Z3_mk_finite_domain_sort(name: Symbol, size: Long): Sort

    /**
     * \brief Create an array type.
     *
     * We usually represent the array type as: \ccode{[domain -> range]}.
     * Arrays are usually used to model the heap/memory in software verification.
     *
     * \sa Z3_mk_select
     * \sa Z3_mk_store
     *
     * def_API('Z3_mk_array_sort', SORT, (_in(CONTEXT), _in(SORT), _in(SORT)))
     */
    fun Context.Z3_mk_array_sort(
        domain: Sort,
        range: Sort
    ): Sort

    /**
     * \brief Create an array type with N arguments
     *
     * \sa Z3_mk_select_n
     * \sa Z3_mk_store_n
     *
     * def_API('Z3_mk_array_sort_n', SORT, (_in(CONTEXT), _in(UINT), _in_array(1, SORT), _in(SORT)))
     */
    fun Context.Z3_mk_array_sort_n(
        n: Int,
        domain: Array<Sort>,
        range: Sort
    ): Sort

    /**
     * \brief Create a tuple type.
     *
     * A tuple with \c n fields has a constructor and \c n projections.
     * This function will also declare the constructor and projection functions.
     *
     * \param c logical context
     * \param mk_tuple_name name of the constructor function associated with the tuple type.
     * \param num_fields number of fields in the tuple type.
     * \param field_names name of the projection functions.
     * \param field_sorts type of the tuple fields.
     * \param mk_tuple_decl output parameter that will contain the constructor declaration.
     * \param proj_decl output parameter that will contain the projection function declarations. This field must be a buffer of size \c num_fields allocated by the user.
     *
     * def_API('Z3_mk_tuple_sort', SORT, (_in(CONTEXT), _in(SYMBOL), _in(UINT), _in_array(2, SYMBOL), _in_array(2, SORT), _out(FUNC_DECL), _out_array(2, FUNC_DECL)))
     */
    fun Context.Z3_mk_tuple_sort(
        mk_tuple_name: Symbol,
        num_fields: Int,
        field_names: Array<Symbol>,
        field_sorts: Array<Sort>,
        mk_tuple_decl: Out<FuncDecl>,
        proj_decl: Array<FuncDecl>
    ): Sort

    /**
     * \brief Create a enumeration sort.
     *
     * An enumeration sort with \c n elements.
     * This function will also declare the functions corresponding to the enumerations.
     *
     * \param c logical context
     * \param name name of the enumeration sort.
     * \param n number of elements in enumeration sort.
     * \param enum_names names of the enumerated elements.
     * \param enum_consts constants corresponding to the enumerated elements.
     * \param enum_testers predicates testing if terms of the enumeration sort correspond to an enumeration.
     *
     * For example, if this function is called with three symbols A, B, C and the name S, then
     * \c s is a sort whose name is S, and the function returns three terms corresponding to A, B, C in
     * \c enum_consts. The array \c enum_testers has three predicates of type \ccode{(s -> boolean)}.
     * The first predicate (corresponding to A) is true when applied to A, and false otherwise.
     * Similarly for the other predicates.
     *
     * def_API('Z3_mk_enumeration_sort', SORT, (_in(CONTEXT), _in(SYMBOL), _in(UINT), _in_array(2, SYMBOL), _out_array(2, FUNC_DECL), _out_array(2, FUNC_DECL)))
     */
    fun Context.Z3_mk_enumeration_sort(
        name: Symbol,
        n: Int,
        enum_names: Array<Symbol>,
        enum_consts: Array<FuncDecl>,
        enum_testers: Array<FuncDecl>
    ): Sort

    /**
     * \brief Create a list sort
     *
     * A list sort over \c elem_sort
     * This function declares the corresponding constructors and testers for lists.
     *
     * \param c logical context
     * \param name name of the list sort.
     * \param elem_sort sort of list elements.
     * \param nil_decl declaration for the empty list.
     * \param is_nil_decl test for the empty list.
     * \param cons_decl declaration for a cons cell.
     * \param is_cons_decl cons cell test.
     * \param head_decl list head.
     * \param tail_decl list tail.
     *
     * def_API('Z3_mk_list_sort', SORT, (_in(CONTEXT), _in(SYMBOL), _in(SORT), _out(FUNC_DECL), _out(FUNC_DECL), _out(FUNC_DECL), _out(FUNC_DECL), _out(FUNC_DECL), _out(FUNC_DECL)))
     */
    fun Context.Z3_mk_list_sort(
        name: Symbol,
        elem_sort: Sort,
        nil_decl: Out<FuncDecl>,
        is_nil_decl: Out<FuncDecl>,
        cons_decl: Out<FuncDecl>,
        is_cons_decl: Out<FuncDecl>,
        head_decl: Out<FuncDecl>,
        tail_decl: Out<FuncDecl>
    ): Sort

    /**
     * \brief Create a constructor.
     *
     * \param c logical context.
     * \param name constructor name.
     * \param recognizer name of recognizer function.
     * \param num_fields number of fields in constructor.
     * \param field_names names of the constructor fields.
     * \param sorts field sorts, 0 if the field sort refers to a recursive sort.
     * \param sort_refs reference to datatype sort that is an argument to the constructor; if the corresponding
     * sort reference is 0, then the value in sort_refs should be an index referring to
     * one of the recursive datatypes that is declared.
     *
     * \sa Z3_del_constructor
     * \sa Z3_mk_constructor_list
     * \sa Z3_query_constructor
     *
     * def_API('Z3_mk_constructor', CONSTRUCTOR, (_in(CONTEXT), _in(SYMBOL), _in(SYMBOL), _in(UINT), _in_array(3, SYMBOL), _in_array(3, SORT), _in_array(3, UINT)))
     */
    fun Context.Z3_mk_constructor(
        name: Symbol,
        recognizer: Symbol,
        num_fields: Int,
        field_names: Array<Symbol>,
        sorts: Array<Sort>,
        sort_refs: IntArray
    ): Constructor

    /**
     * \brief Reclaim memory allocated to constructor.
     *
     * \param c logical context.
     * \param constr constructor.
     *
     * \sa Z3_mk_constructor
     *
     * def_API('Z3_del_constructor', VOID, (_in(CONTEXT), _in(CONSTRUCTOR)))
     */
    fun Context.Z3_del_constructor(constr: Constructor)

    /**
     * \brief Create datatype, such as lists, trees, records, enumerations or unions of records.
     * The datatype may be recursive. Return the datatype sort.
     *
     * \param c logical context.
     * \param name name of datatype.
     * \param num_constructors number of constructors passed in.
     * \param constructors array of constructor containers.
     *
     * \sa Z3_mk_constructor
     * \sa Z3_mk_constructor_list
     * \sa Z3_mk_datatypes
     *
     * def_API('Z3_mk_datatype', SORT, (_in(CONTEXT), _in(SYMBOL), _in(UINT), _inout_array(2, CONSTRUCTOR)))
     */
    fun Context.Z3_mk_datatype(
        name: Symbol,
        num_constructors: Int,
        constructors: Array<Constructor>
    ): Sort

    /**
     * \brief Create list of constructors.
     *
     * \param c logical context.
     * \param num_constructors number of constructors in list.
     * \param constructors list of constructors.
     *
     * \sa Z3_del_constructor_list
     * \sa Z3_mk_constructor
     *
     * def_API('Z3_mk_constructor_list', CONSTRUCTOR_LIST, (_in(CONTEXT), _in(UINT), _in_array(1, CONSTRUCTOR)))
     */
    fun Context.Z3_mk_constructor_list(
        num_constructors: Int,
        constructors: Array<Constructor>
    ): ConstructorList

    /**
     * \brief Reclaim memory allocated for constructor list.
     *
     * Each constructor inside the constructor list must be independently reclaimed using #Z3_del_constructor.
     *
     * \param c logical context.
     * \param clist constructor list container.
     *
     * \sa Z3_mk_constructor_list
     *
     * def_API('Z3_del_constructor_list', VOID, (_in(CONTEXT), _in(CONSTRUCTOR_LIST)))
     */
    fun Context.Z3_del_constructor_list(clist: ConstructorList)

    /**
     * \brief Create mutually recursive datatypes.
     *
     * \param c logical context.
     * \param num_sorts number of datatype sorts.
     * \param sort_names names of datatype sorts.
     * \param sorts array of datatype sorts.
     * \param constructor_lists list of constructors, one list per sort.
     *
     * \sa Z3_mk_constructor
     * \sa Z3_mk_constructor_list
     * \sa Z3_mk_datatype
     *
     * def_API('Z3_mk_datatypes', VOID, (_in(CONTEXT), _in(UINT), _in_array(1, SYMBOL), _out_array(1, SORT), _inout_array(1, CONSTRUCTOR_LIST)))
     */
    fun Context.Z3_mk_datatypes(
        num_sorts: Int,
        sort_names: Array<Symbol>,
        sorts: Array<Sort>,
        constructor_lists: Array<ConstructorList>
    )

    /**
     * \brief Query constructor for declared functions.
     *
     * \param c logical context.
     * \param constr constructor container. The container must have been passed in to a #Z3_mk_datatype call.
     * \param num_fields number of accessor fields in the constructor.
     * \param constructor constructor function declaration, allocated by user.
     * \param tester constructor test function declaration, allocated by user.
     * \param accessors array of accessor function declarations allocated by user. The array must contain num_fields elements.
     *
     * \sa Z3_mk_constructor
     *
     * def_API('Z3_query_constructor', VOID, (_in(CONTEXT), _in(CONSTRUCTOR), _in(UINT), _out(FUNC_DECL), _out(FUNC_DECL), _out_array(2, FUNC_DECL)))
     */
    fun Context.Z3_query_constructor(
        constr: Constructor,
        num_fields: Int,
        constructor: Out<FuncDecl>,
        tester: Out<FuncDecl>,
        accessors: Array<FuncDecl>
    )

    /*@}*/

    /** @name Constants and Applications */
    /*@{*/

    /*@}*/
    /** @name Constants and Applications
     */
    /*@{*/
    /**
     * \brief Declare a constant or function.
     *
     * \param c logical context.
     * \param s name of the constant or function.
     * \param domain_size number of arguments. It is 0 when declaring a constant.
     * \param domain array containing the sort of each argument. The array must contain domain_size elements. It is 0 when declaring a constant.
     * \param range sort of the constant or the return sort of the function.
     *
     * After declaring a constant or function, the function
     * #Z3_mk_app can be used to create a constant or function
     * application.
     *
     * \sa Z3_mk_app
     * \sa Z3_mk_fresh_func_decl
     * \sa Z3_mk_rec_func_decl
     *
     * def_API('Z3_mk_func_decl', FUNC_DECL, (_in(CONTEXT), _in(SYMBOL), _in(UINT), _in_array(2, SORT), _in(SORT)))
     */
    fun Context.Z3_mk_func_decl(
        s: Symbol, domain_size: Int,
        domain: Array<Sort>, range: Sort
    ): FuncDecl


    /**
     * \brief Create a constant or function application.
     *
     * \sa Z3_mk_fresh_func_decl
     * \sa Z3_mk_func_decl
     * \sa Z3_mk_rec_func_decl
     *
     * def_API('Z3_mk_app', AST, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT), _in_array(2, AST)))
     */
    fun Context.Z3_mk_app(
        d: FuncDecl,
        num_args: Int,
        args: Array<Ast>
    ): Ast

    /**
     * \brief Declare and create a constant.
     *
     * This function is a shorthand for:
     * \code
     * Z3_func_decl d = Z3_mk_func_decl(c, s, 0, 0, ty);
     * Z3_ast n            = Z3_mk_app(c, d, 0, 0);
     * \endcode
     *
     * \sa Z3_mk_app
     * \sa Z3_mk_fresh_const
     * \sa Z3_mk_func_decl
     *
     * def_API('Z3_mk_const', AST, (_in(CONTEXT), _in(SYMBOL), _in(SORT)))
     */
    fun Context.Z3_mk_const(s: Symbol, ty: Sort): Ast

    /**
     * \brief Declare a fresh constant or function.
     *
     * Z3 will generate an unique name for this function declaration.
     * If prefix is different from \c NULL, then the name generate by Z3 will start with \c prefix.
     *
     * \remark If \c prefix is \c NULL, then it is assumed to be the empty string.
     *
     * \sa Z3_mk_func_decl
     *
     * def_API('Z3_mk_fresh_func_decl', FUNC_DECL, (_in(CONTEXT), _in(STRING), _in(UINT), _in_array(2, SORT), _in(SORT)))
     */
    fun Context.Z3_mk_fresh_func_decl(
        prefix: String, domain_size: Int,
        domain: Array<Sort>, range: Sort
    ): FuncDecl

    /**
     * \brief Declare and create a fresh constant.
     *
     * This function is a shorthand for:
     * \code Z3_func_decl d = Z3_mk_fresh_func_decl(c, prefix, 0, 0, ty); Z3_ast n = Z3_mk_app(c, d, 0, 0); \endcode
     *
     * \remark If \c prefix is \c NULL, then it is assumed to be the empty string.
     *
     * \sa Z3_mk_app
     * \sa Z3_mk_const
     * \sa Z3_mk_fresh_func_decl
     * \sa Z3_mk_func_decl
     *
     * def_API('Z3_mk_fresh_const', AST, (_in(CONTEXT), _in(STRING), _in(SORT)))
     */
    fun Context.Z3_mk_fresh_const(prefix: String, ty: Sort): Ast


    /**
     * \brief Declare a recursive function
     *
     * \param c logical context.
     * \param s name of the function.
     * \param domain_size number of arguments. It should be greater than 0.
     * \param domain array containing the sort of each argument. The array must contain domain_size elements.
     * \param range sort of the constant or the return sort of the function.
     *
     * After declaring recursive function, it should be associated with a recursive definition #Z3_add_rec_def.
     * The function #Z3_mk_app can be used to create a constant or function
     * application.
     *
     * \sa Z3_add_rec_def
     * \sa Z3_mk_app
     * \sa Z3_mk_func_decl
     *
     * def_API('Z3_mk_rec_func_decl', FUNC_DECL, (_in(CONTEXT), _in(SYMBOL), _in(UINT), _in_array(2, SORT), _in(SORT)))
     */
    fun Context.Z3_mk_rec_func_decl(
        s: Symbol, domain_size: Int,
        domain: Array<Sort>, range: Sort
    ): FuncDecl

    /**
     * \brief Define the body of a recursive function.
     *
     * \param c logical context.
     * \param f function declaration.
     * \param n number of arguments to the function
     * \param args constants that are used as arguments to the recursive function in the definition.
     * \param body body of the recursive function
     *
     * After declaring a recursive function or a collection of  mutually recursive functions, use
     * this function to provide the definition for the recursive function.
     *
     * \sa Z3_mk_rec_func_decl
     *
     * def_API('Z3_add_rec_def', VOID, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT), _in_array(2, AST), _in(AST)))
     */
    fun Context.Z3_add_rec_def(
        f: FuncDecl,
        n: Int,
        args: Array<Ast>,
        body: Ast
    )

    /*@}*/

    /** @name Propositional Logic and Equality */
    /*@{*/
    /*@}*/
    /** @name Propositional Logic and Equality
     */
    /*@{*/
    /**
     * \brief Create an AST node representing \c true.
     *
     * def_API('Z3_mk_true', AST, (_in(CONTEXT), ))
     */
    fun Context.Z3_mk_true(): Ast

    /**
     * \brief Create an AST node representing \c false.
     *
     * def_API('Z3_mk_false', AST, (_in(CONTEXT), ))
     */
    fun Context.Z3_mk_false(): Ast

    /**
     * \brief Create an AST node representing \ccode{l = r}.
     *
     * The nodes \c l and \c r must have the same type.
     *
     * def_API('Z3_mk_eq', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_eq(l: Ast, r: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{distinct(args[0], ..., args[num_args-1])}.
     *
     * The \c distinct construct is used for declaring the arguments pairwise distinct.
     * That is, \ccode{Forall 0 <= i < j < num_args. not args[i] = args[j]}.
     *
     * All arguments must have the same sort.
     *
     * \remark The number of arguments of a distinct construct must be greater than one.
     *
     * def_API('Z3_mk_distinct', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_distinct(
        num_args: Int,
        args: Array<Ast>
    ): Ast

    /**
     * \brief Create an AST node representing \ccode{not(a)}.
     *
     * The node \c a must have boolean sort.
     *
     * def_API('Z3_mk_not', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_not(a: Ast): Ast

    /**
     * \brief Create an AST node representing an if-then-else: \ccode{ite(t1, t2, t3)}.
     *
     * The node \c t1 must have boolean sort, \c t2 and \c t3 must have the same sort.
     * The sort of the new node is equal to the sort of \c t2 and \c t3.
     *
     * def_API('Z3_mk_ite', AST, (_in(CONTEXT), _in(AST), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_ite(
        t1: Ast,
        t2: Ast,
        t3: Ast
    ): Ast

    /**
     * \brief Create an AST node representing \ccode{t1 iff t2}.
     *
     * The nodes \c t1 and \c t2 must have boolean sort.
     *
     * def_API('Z3_mk_iff', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_iff(t1: Ast, t2: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{t1 implies t2}.
     *
     * The nodes \c t1 and \c t2 must have boolean sort.
     *
     * def_API('Z3_mk_implies', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_implies(t1: Ast, t2: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{t1 xor t2}.
     *
     * The nodes \c t1 and \c t2 must have boolean sort.
     *
     * def_API('Z3_mk_xor', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_xor(t1: Ast, t2: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{args[0] and ... and args[num_args-1]}.
     *
     * The array \c args must have \c num_args elements.
     * All arguments must have boolean sort.
     *
     * \remark The number of arguments must be greater than zero.
     *
     * def_API('Z3_mk_and', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_and(num_args: Int, args: Array<Ast>): Ast

    /**
     * \brief Create an AST node representing \ccode{args[0] or ... or args[num_args-1]}.
     *
     * The array \c args must have \c num_args elements.
     * All arguments must have boolean sort.
     *
     * \remark The number of arguments must be greater than zero.
     *
     * def_API('Z3_mk_or', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_or(num_args: Int, args: Array<Ast>): Ast
    /*@}*/

    /** @name Integers and Reals */
    /*@{*/
    /*@}*/
    /** @name Integers and Reals
     */
    /*@{*/
    /**
     * \brief Create an AST node representing \ccode{args[0] + ... + args[num_args-1]}.
     *
     * The array \c args must have \c num_args elements.
     * All arguments must have int or real sort.
     *
     * \remark The number of arguments must be greater than zero.
     *
     * def_API('Z3_mk_add', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_add(num_args: Int, args: Array<Ast>): Ast

    /**
     * \brief Create an AST node representing \ccode{args[0] * ... * args[num_args-1]}.
     *
     * The array \c args must have \c num_args elements.
     * All arguments must have int or real sort.
     *
     * \remark Z3 has limited support for non-linear arithmetic.
     * \remark The number of arguments must be greater than zero.
     *
     * def_API('Z3_mk_mul', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_mul(num_args: Int, args: Array<Ast>?): Ast

    /**
     * \brief Create an AST node representing \ccode{args[0] - ... - args[num_args - 1]}.
     *
     * The array \c args must have \c num_args elements.
     * All arguments must have int or real sort.
     *
     * \remark The number of arguments must be greater than zero.
     *
     * def_API('Z3_mk_sub', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_sub(num_args: Int, args: Array<Ast>?): Ast

    /**
     * \brief Create an AST node representing \ccode{- arg}.
     *
     * The arguments must have int or real type.
     *
     * def_API('Z3_mk_unary_minus', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_unary_minus(arg: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{arg1 div arg2}.
     *
     * The arguments must either both have int type or both have real type.
     * If the arguments have int type, then the result type is an int type, otherwise the
     * the result type is real.
     *
     * def_API('Z3_mk_div', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_div(arg1: Ast, arg2: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{arg1 mod arg2}.
     *
     * The arguments must have int type.
     *
     * def_API('Z3_mk_mod', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_mod(arg1: Ast, arg2: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{arg1 rem arg2}.
     *
     * The arguments must have int type.
     *
     * def_API('Z3_mk_rem', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_rem(arg1: Ast, arg2: Ast): Ast

    /**
     * \brief Create an AST node representing \ccode{arg1 ^ arg2}.
     *
     * The arguments must have int or real type.
     *
     * def_API('Z3_mk_power', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_power(arg1: Ast, arg2: Ast): Ast

    /**
     * \brief Create less than.
     *
     * The nodes \c t1 and \c t2 must have the same sort, and must be int or real.
     *
     * def_API('Z3_mk_lt', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_lt(t1: Ast, t2: Ast): Ast

    /**
     * \brief Create less than or equal to.
     *
     * The nodes \c t1 and \c t2 must have the same sort, and must be int or real.
     *
     * def_API('Z3_mk_le', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_le(t1: Ast, t2: Ast): Ast

    /**
     * \brief Create greater than.
     *
     * The nodes \c t1 and \c t2 must have the same sort, and must be int or real.
     *
     * def_API('Z3_mk_gt', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_gt(t1: Ast, t2: Ast): Ast

    /**
     * \brief Create greater than or equal to.
     *
     * The nodes \c t1 and \c t2 must have the same sort, and must be int or real.
     *
     * def_API('Z3_mk_ge', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_ge(t1: Ast, t2: Ast): Ast

    /**
     * \brief Create division predicate.
     *
     * The nodes \c t1 and \c t2 must be of integer sort.
     * The predicate is true when \c t1 divides \c t2. For the predicate to be part of
     * linear integer arithmetic, the first argument \c t1 must be a non-zero integer.
     *
     * def_API('Z3_mk_divides', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_divides(t1: Ast, t2: Ast): Ast

    /**
     * \brief Coerce an integer to a real.
     *
     * There is also a converse operation exposed.
     * It follows the semantics prescribed by the SMT-LIB standard.
     *
     * You can take the floor of a real by
     * creating an auxiliary integer constant \c k and
     * and asserting \ccode{mk_int2real(k) <= t1 < mk_int2real(k)+1}.
     *
     * The node \c t1 must have sort integer.
     *
     * \sa Z3_mk_real2int
     * \sa Z3_mk_is_int
     *
     * def_API('Z3_mk_int2real', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_int2real(t1: Ast): Ast

    /**
     * \brief Coerce a real to an integer.
     *
     * The semantics of this function follows the SMT-LIB standard
     * for the function to_int
     *
     * \sa Z3_mk_int2real
     * \sa Z3_mk_is_int
     *
     * def_API('Z3_mk_real2int', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_real2int(t1: Ast): Ast

    /**
     * \brief Check if a real number is an integer.
     *
     * \sa Z3_mk_int2real
     * \sa Z3_mk_real2int
     *
     * def_API('Z3_mk_is_int', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Z3_mk_is_int(c: Context, t1: Ast): Ast
    /*@}*/

    /** @name Bit-vectors */
    /*@{*/
    /*@}*/
    /** @name Bit-vectors
     */
    /*@{*/
    /**
     * \brief Bitwise negation.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_bvnot', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_bvnot(t1: Ast): Ast

    /**
     * \brief Take conjunction of bits in vector, return vector of length 1.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_bvredand', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_bvredand(t1: Ast): Ast

    /**
     * \brief Take disjunction of bits in vector, return vector of length 1.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_bvredor', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_bvredor(t1: Ast): Ast

    /**
     * \brief Bitwise and.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvand', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvand(t1: Ast, t2: Ast): Ast

    /**
     * \brief Bitwise or.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvor', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvor(t1: Ast, t2: Ast): Ast

    /**
     * \brief Bitwise exclusive-or.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvxor', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvxor(t1: Ast, t2: Ast): Ast

    /**
     * \brief Bitwise nand.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvnand', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvnand(t1: Ast, t2: Ast): Ast

    /**
     * \brief Bitwise nor.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvnor', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvnor(t1: Ast, t2: Ast): Ast

    /**
     * \brief Bitwise xnor.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvxnor', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvxnor(t1: Ast, t2: Ast): Ast

    /**
     * \brief Standard two's complement unary minus.
     *
     * The node \c t1 must have bit-vector sort.
     *
     * def_API('Z3_mk_bvneg', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_bvneg(t1: Ast): Ast

    /**
     * \brief Standard two's complement addition.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvadd', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvadd(t1: Ast, t2: Ast): Ast

    /**
     * \brief Standard two's complement subtraction.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvsub', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsub(t1: Ast, t2: Ast): Ast

    /**
     * \brief Standard two's complement multiplication.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvmul', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvmul(t1: Ast, t2: Ast): Ast

    /**
     * \brief int division.
     *
     * It is defined as the \c floor of \ccode{t1/t2} if \c t2 is
     * different from zero. If \ccode{t2} is zero, then the result
     * is undefined.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvudiv', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvudiv(t1: Ast, t2: Ast): Ast

    /**
     * \brief Two's complement signed division.
     *
     * It is defined in the following way:
     *
     * - The \c floor of \ccode{t1/t2} if \c t2 is different from zero, and \ccode{t1*t2 >= 0}.
     *
     * - The \c ceiling of \ccode{t1/t2} if \c t2 is different from zero, and \ccode{t1*t2 < 0}.
     *
     * If \ccode{t2} is zero, then the result is undefined.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvsdiv', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsdiv(t1: Ast, t2: Ast): Ast

    /**
     * \brief int remainder.
     *
     * It is defined as \ccode{t1 - (t1 /u t2) * t2}, where \ccode{/u} represents int division.
     *
     * If \ccode{t2} is zero, then the result is undefined.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvurem', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvurem(t1: Ast, t2: Ast): Ast

    /**
     * \brief Two's complement signed remainder (sign follows dividend).
     *
     * It is defined as \ccode{t1 - (t1 /s t2) * t2}, where \ccode{/s} represents signed division.
     * The most significant bit (sign) of the result is equal to the most significant bit of \c t1.
     *
     * If \ccode{t2} is zero, then the result is undefined.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * \sa Z3_mk_bvsmod
     *
     * def_API('Z3_mk_bvsrem', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsrem(t1: Ast, t2: Ast): Ast

    /**
     * \brief Two's complement signed remainder (sign follows divisor).
     *
     * If \ccode{t2} is zero, then the result is undefined.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * \sa Z3_mk_bvsrem
     *
     * def_API('Z3_mk_bvsmod', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsmod(t1: Ast, t2: Ast): Ast

    /**
     * \brief int less than.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvult', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvult(t1: Ast, t2: Ast): Ast

    /**
     * \brief Two's complement signed less than.
     *
     * It abbreviates:
     * \code
     * (or (and (= (extract[|m-1|:|m-1|] t1) bit1)
     * (= (extract[|m-1|:|m-1|] t2) bit0))
     * (and (= (extract[|m-1|:|m-1|] t1) (extract[|m-1|:|m-1|] t2))
     * (bvult t1 t2)))
     * \endcode
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvslt', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvslt(t1: Ast, t2: Ast): Ast

    /**
     * \brief int less than or equal to.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvule', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvule(t1: Ast, t2: Ast): Ast

    /**
     * \brief Two's complement signed less than or equal to.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvsle', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsle(t1: Ast, t2: Ast): Ast

    /**
     * \brief int greater than or equal to.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvuge', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvuge(t1: Ast, t2: Ast): Ast

    /**
     * \brief Two's complement signed greater than or equal to.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvsge', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsge(t1: Ast, t2: Ast): Ast

    /**
     * \brief int greater than.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvugt', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvugt(t1: Ast, t2: Ast): Ast

    /**
     * \brief Two's complement signed greater than.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvsgt', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsgt(t1: Ast, t2: Ast): Ast

    /**
     * \brief Concatenate the given bit-vectors.
     *
     * The nodes \c t1 and \c t2 must have (possibly different) bit-vector sorts
     *
     * The result is a bit-vector of size \ccode{n1+n2}, where \c n1 (\c n2) is the size
     * of \c t1 (\c t2).
     *
     * def_API('Z3_mk_concat', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_concat(t1: Ast, t2: Ast): Ast

    /**
     * \brief Extract the bits \c high down to \c low from a bit-vector of
     * size \c m to yield a new bit-vector of size \c n, where \ccode{n = high - low + 1}.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_extract', AST, (_in(CONTEXT), _in(UINT), _in(UINT), _in(AST)))
     */
    fun Context.Z3_mk_extract(high: Int, low: Int, t1: Ast): Ast

    /**
     * \brief Sign-extend of the given bit-vector to the (signed) equivalent bit-vector of
     * size \ccode{m+i}, where \c m is the size of the given
     * bit-vector.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_sign_ext', AST, (_in(CONTEXT), _in(UINT), _in(AST)))
     */
    fun Context.Z3_mk_sign_ext(i: Int, t1: Ast): Ast

    /**
     * \brief Extend the given bit-vector with zeros to the (int) equivalent
     * bit-vector of size \ccode{m+i}, where \c m is the size of the
     * given bit-vector.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_zero_ext', AST, (_in(CONTEXT), _in(UINT), _in(AST)))
     */
    fun Context.Z3_mk_zero_ext(i: Int, t1: Ast): Ast

    /**
     * \brief Repeat the given bit-vector up length \ccode{i}.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_repeat', AST, (_in(CONTEXT), _in(UINT), _in(AST)))
     */
    fun Context.Z3_mk_repeat(i: Int, t1: Ast): Ast

    /**
     * \brief Shift left.
     *
     * It is equivalent to multiplication by \ccode{2^x} where \c x is the value of the
     * third argument.
     *
     * NB. The semantics of shift operations varies between environments. This
     * definition does not necessarily capture directly the semantics of the
     * programming language or assembly architecture you are modeling.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvshl', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvshl(t1: Ast, t2: Ast): Ast

    /**
     * \brief Logical shift right.
     *
     * It is equivalent to int division by \ccode{2^x} where \c x is the
     * value of the third argument.
     *
     * NB. The semantics of shift operations varies between environments. This
     * definition does not necessarily capture directly the semantics of the
     * programming language or assembly architecture you are modeling.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvlshr', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvlshr(t1: Ast, t2: Ast): Ast

    /**
     * \brief Arithmetic shift right.
     *
     * It is like logical shift right except that the most significant
     * bits of the result always copy the most significant bit of the
     * second argument.
     *
     * The semantics of shift operations varies between environments. This
     * definition does not necessarily capture directly the semantics of the
     * programming language or assembly architecture you are modeling.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_bvashr', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvashr(t1: Ast, t2: Ast): Ast

    /**
     * \brief Rotate bits of \c t1 to the left \c i times.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_rotate_left', AST, (_in(CONTEXT), _in(UINT), _in(AST)))
     */
    fun Context.Z3_mk_rotate_left(i: Int, t1: Ast): Ast

    /**
     * \brief Rotate bits of \c t1 to the right \c i times.
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_rotate_right', AST, (_in(CONTEXT), _in(UINT), _in(AST)))
     */
    fun Context.Z3_mk_rotate_right(i: Int, t1: Ast): Ast

    /**
     * \brief Rotate bits of \c t1 to the left \c t2 times.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_ext_rotate_left', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_ext_rotate_left(t1: Ast, t2: Ast): Ast

    /**
     * \brief Rotate bits of \c t1 to the right \c t2 times.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     *
     * def_API('Z3_mk_ext_rotate_right', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_ext_rotate_right(
        t1: Ast,
        t2: Ast
    ): Ast

    /**
     * \brief Create an \c n bit bit-vector from the integer argument \c t1.
     *
     * The resulting bit-vector has \c n bits, where the i'th bit (counting
     * from 0 to \c n-1) is 1 if \c (t1 div 2^i) mod 2 is 1.
     *
     * The node \c t1 must have integer sort.
     *
     * def_API('Z3_mk_int2bv', AST, (_in(CONTEXT), _in(UINT), _in(AST)))
     */
    fun Context.Z3_mk_int2bv(n: Int, t1: Ast): Ast

    /**
     * \brief Create an integer from the bit-vector argument \c t1.
     * If \c is_signed is false, then the bit-vector \c t1 is treated as int.
     * So the result is non-negative
     * and in the range \ccode{[0..2^N-1]}, where N are the number of bits in \c t1.
     * If \c is_signed is true, \c t1 is treated as a signed bit-vector.
     *
     *
     * The node \c t1 must have a bit-vector sort.
     *
     * def_API('Z3_mk_bv2int', AST, (_in(CONTEXT), _in(AST), _in(boolean)))
     */
    fun Context.Z3_mk_bv2int(t1: Ast, is_signed: Boolean): Ast

    /**
     * \brief Create a predicate that checks that the bit-wise addition
     * of \c t1 and \c t2 does not overflow.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvadd_no_overflow', AST, (_in(CONTEXT), _in(AST), _in(AST), _in(boolean)))
     */
    fun Context.Z3_mk_bvadd_no_overflow(
        t1: Ast,
        t2: Ast,
        is_signed: Boolean
    ): Ast

    /**
     * \brief Create a predicate that checks that the bit-wise signed addition
     * of \c t1 and \c t2 does not underflow.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvadd_no_underflow', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvadd_no_underflow(
        t1: Ast,
        t2: Ast
    ): Ast

    /**
     * \brief Create a predicate that checks that the bit-wise signed subtraction
     * of \c t1 and \c t2 does not overflow.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvsub_no_overflow', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsub_no_overflow(
        t1: Ast,
        t2: Ast
    ): Ast

    /**
     * \brief Create a predicate that checks that the bit-wise subtraction
     * of \c t1 and \c t2 does not underflow.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvsub_no_underflow', AST, (_in(CONTEXT), _in(AST), _in(AST), _in(boolean)))
     */
    fun Context.Z3_mk_bvsub_no_underflow(
        t1: Ast,
        t2: Ast,
        is_signed: Boolean
    ): Ast

    /**
     * \brief Create a predicate that checks that the bit-wise signed division
     * of \c t1 and \c t2 does not overflow.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvsdiv_no_overflow', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvsdiv_no_overflow(
        t1: Ast,
        t2: Ast
    ): Ast

    /**
     * \brief Check that bit-wise negation does not overflow when
     * \c t1 is interpreted as a signed bit-vector.
     *
     * The node \c t1 must have bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvneg_no_overflow', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_bvneg_no_overflow(t1: Ast): Ast

    /**
     * \brief Create a predicate that checks that the bit-wise multiplication
     * of \c t1 and \c t2 does not overflow.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvmul_no_overflow', AST, (_in(CONTEXT), _in(AST), _in(AST), _in(boolean)))
     */
    fun Context.Z3_mk_bvmul_no_overflow(
        t1: Ast,
        t2: Ast,
        is_signed: Boolean
    ): Ast

    /**
     * \brief Create a predicate that checks that the bit-wise signed multiplication
     * of \c t1 and \c t2 does not underflow.
     *
     * The nodes \c t1 and \c t2 must have the same bit-vector sort.
     * The returned node is of sort boolean.
     *
     * def_API('Z3_mk_bvmul_no_underflow', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_bvmul_no_underflow(
        t1: Ast,
        t2: Ast
    ): Ast
    /*@}*/

    /** @name Arrays */
    /*@{*/
    /*@}*/
    /** @name Arrays
     */
    /*@{*/
    /**
     * \brief Array read.
     * The argument \c a is the array and \c i is the index of the array that gets read.
     *
     * The node \c a must have an array sort \ccode{[domain -> range]},
     * and \c i must have the sort \c domain.
     * The sort of the result is \c range.
     *
     * \sa Z3_mk_array_sort
     * \sa Z3_mk_store
     *
     * def_API('Z3_mk_select', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_select(a: Ast, i: Ast): Ast


    /**
     * \brief n-ary Array read.
     * The argument \c a is the array and \c idxs are the indices of the array that gets read.
     *
     * def_API('Z3_mk_select_n', AST, (_in(CONTEXT), _in(AST), _in(UINT), _in_array(2, AST)))
     *
     */
    fun Context.Z3_mk_select_n(
        a: Ast,
        n: Int,
        idxs: Array<Ast>
    ): Ast


    /**
     * \brief Array update.
     *
     * The node \c a must have an array sort \ccode{[domain -> range]}, \c i must have sort \c domain,
     * \c v must have sort range. The sort of the result is \ccode{[domain -> range]}.
     * The semantics of this function is given by the theory of arrays described in the SMT-LIB
     * standard. See http://smtlib.org for more details.
     * The result of this function is an array that is equal to \c a (with respect to \c select)
     * on all indices except for \c i, where it maps to \c v (and the \c select of \c a with
     * respect to \c i may be a different value).
     *
     * \sa Z3_mk_array_sort
     * \sa Z3_mk_select
     *
     * def_API('Z3_mk_store', AST, (_in(CONTEXT), _in(AST), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_store(
        a: Ast,
        i: Ast,
        v: Ast
    ): Ast


    /**
     * \brief n-ary Array update.
     *
     * def_API('Z3_mk_store_n', AST, (_in(CONTEXT), _in(AST), _in(UINT), _in_array(2, AST), _in(AST)))
     *
     */
    fun Context.Z3_mk_store_n(
        a: Ast,
        n: Int,
        idxs: Array<Ast>,
        v: Ast
    ): Ast

    /**
     * \brief Create the constant array.
     *
     * The resulting term is an array, such that a \c select on an arbitrary index
     * produces the value \c v.
     *
     * \param c logical context.
     * \param domain domain sort for the array.
     * \param v value that the array maps to.
     *
     * def_API('Z3_mk_const_array', AST, (_in(CONTEXT), _in(SORT), _in(AST)))
     */
    fun Context.Z3_mk_const_array(domain: Sort, v: Ast): Ast

    /**
     * \brief Map f on the argument arrays.
     *
     * The \c n nodes \c args must be of array sorts \ccode{[domain_i -> range_i]}.
     * The function declaration \c f must have type \ccode{ range_1 .. range_n -> range}.
     * \c v must have sort range. The sort of the result is \ccode{[domain_i -> range]}.
     *
     * \sa Z3_mk_array_sort
     * \sa Z3_mk_store
     * \sa Z3_mk_select
     *
     * def_API('Z3_mk_map', AST, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT), _in_array(2, AST)))
     */
    fun Context.Z3_mk_map(
        f: FuncDecl,
        n: Int,
        args: Array<Ast>
    ): Ast

    /**
     * \brief Access the array default value.
     * Produces the default range value, for arrays that can be represented as
     * finite maps with a default range value.
     *
     * \param c logical context.
     * \param array array value whose default range value is accessed.
     *
     * def_API('Z3_mk_array_default', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_array_default(array: Ast): Ast

    /**
     * \brief Create array with the same interpretation as a function.
     * The array satisfies the property (f x) = (select (_ as-array f) x)
     * for every argument x.
     *
     * def_API('Z3_mk_as_array', AST, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_mk_as_array(f: FuncDecl): Ast

    /**
     * \brief Create predicate that holds if boolean array \c set has \c k elements set to true.
     *
     * def_API('Z3_mk_set_has_size', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_set_has_size(set: Ast, k: Ast): Ast

    /*@}*/

    /** @name Sets */
    /*@{*/
    /*@}*/
    /** @name Sets
     */
    /*@{*/
    /**
     * \brief Create Set type.
     *
     * def_API('Z3_mk_set_sort', SORT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_set_sort(ty: Sort): Sort

    /**
     * \brief Create the empty set.
     *
     * def_API('Z3_mk_empty_set', AST, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_empty_set(domain: Sort): Ast

    /**
     * \brief Create the full set.
     *
     * def_API('Z3_mk_full_set', AST, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_full_set(domain: Sort): Ast

    /**
     * \brief Add an element to a set.
     *
     * The first argument must be a set, the second an element.
     *
     * def_API('Z3_mk_set_add', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_set_add(set: Ast, elem: Ast): Ast

    /**
     * \brief Remove an element to a set.
     *
     * The first argument must be a set, the second an element.
     *
     * def_API('Z3_mk_set_del', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_set_del(set: Ast, elem: Ast): Ast

    /**
     * \brief Take the union of a list of sets.
     *
     * def_API('Z3_mk_set_union', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_set_union(
        num_args: Int,
        args: Array<Ast>
    ): Ast

    /**
     * \brief Take the intersection of a list of sets.
     *
     * def_API('Z3_mk_set_intersect', AST, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_set_intersect(
        num_args: Int,
        args: Array<Ast>?
    ): Ast

    /**
     * \brief Take the set difference between two sets.
     *
     * def_API('Z3_mk_set_difference', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_set_difference(
        arg1: Ast,
        arg2: Ast
    ): Ast

    /**
     * \brief Take the complement of a set.
     *
     * def_API('Z3_mk_set_complement', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_set_complement(arg: Ast): Ast

    /**
     * \brief Check for set membership.
     *
     * The first argument should be an element type of the set.
     *
     * def_API('Z3_mk_set_member', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_set_member(elem: Ast, set: Ast): Ast

    /**
     * \brief Check for subsetness of sets.
     *
     * def_API('Z3_mk_set_subset', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_set_subset(arg1: Ast, arg2: Ast): Ast

    /**
     * \brief Create array extensionality index given two arrays with the same sort.
     * The meaning is given by the axiom:
     * (=> (= (select A (array-ext A B)) (select B (array-ext A B))) (= A B))
     *
     * def_API('Z3_mk_array_ext', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_array_ext(arg1: Ast, arg2: Ast): Ast
    /*@}*/

    /** @name Numerals */
    /*@{*/
    /*@}*/
    /** @name Numerals
     */
    /*@{*/
    /**
     * \brief Create a numeral of a given sort.
     *
     * \param c logical context.
     * \param numeral A string representing the numeral value in decimal notation. The string may be of the form `[num]*[.[num]*][E[+|-][num]+]`.
     * If the given sort is a real, then the numeral can be a rational, that is, a string of the form `[num]* / [num]*` .
     * \param ty The sort of the numeral. In the current implementation, the given sort can be an int, real, finite-domain, or bit-vectors of arbitrary size.
     *
     * \sa Z3_mk_int
     * \sa Z3_mk_int_int
     *
     * def_API('Z3_mk_numeral', AST, (_in(CONTEXT), _in(STRING), _in(SORT)))
     */
    fun Context.Z3_mk_numeral(numeral: String, ty: Sort): Ast

    /**
     * \brief Create a real from a fraction.
     *
     * \param c logical context.
     * \param num numerator of rational.
     * \param den denominator of rational.
     *
     * \pre den != 0
     *
     * \sa Z3_mk_numeral
     * \sa Z3_mk_int
     * \sa Z3_mk_int_int
     *
     * def_API('Z3_mk_real', AST, (_in(CONTEXT), _in(INT), _in(INT)))
     */
    fun Context.Z3_mk_real(num: Int, den: Int): Ast

    /**
     * \brief Create a numeral of an int, bit-vector, or finite-domain sort.
     *
     * This function can be used to create numerals that fit in a machine integer.
     * It is slightly faster than #Z3_mk_numeral since it is not necessary to parse a string.
     *
     * \sa Z3_mk_numeral
     *
     * def_API('Z3_mk_int', AST, (_in(CONTEXT), _in(INT), _in(SORT)))
     */
    fun Context.Z3_mk_int(v: Int, ty: Sort): Ast

    /**
     * \brief Create a numeral of a int, bit-vector, or finite-domain sort.
     *
     * This function can be used to create numerals that fit in a machine int integer.
     * It is slightly faster than #Z3_mk_numeral since it is not necessary to parse a string.
     *
     * \sa Z3_mk_numeral
     *
     * def_API('Z3_mk_int_int', AST, (_in(CONTEXT), _in(UINT), _in(SORT)))
     */
    fun Context.Z3_mk_int_int(v: Int, ty: Sort): Ast

    /**
     * \brief Create a numeral of a int, bit-vector, or finite-domain sort.
     *
     * This function can be used to create numerals that fit in a machine \c long integer.
     * It is slightly faster than #Z3_mk_numeral since it is not necessary to parse a string.
     *
     * \sa Z3_mk_numeral
     *
     * def_API('Z3_mk_int64', AST, (_in(CONTEXT), _in(INT64), _in(SORT)))
     */
    fun Context.Z3_mk_int64(v: Long, ty: Sort): Ast

    /**
     * \brief Create a numeral of a int, bit-vector, or finite-domain sort.
     *
     * This function can be used to create numerals that fit in a machine \c long integer.
     * It is slightly faster than #Z3_mk_numeral since it is not necessary to parse a string.
     *
     * \sa Z3_mk_numeral
     *
     * def_API('Z3_mk_int_int64', AST, (_in(CONTEXT), _in(UINT64), _in(SORT)))
     */
    fun Context.Z3_mk_int_int64(v: Long, ty: Sort): Ast

    /**
     * \brief create a bit-vector numeral from a vector of booleans.
     *
     * \sa Z3_mk_numeral
     * def_API('Z3_mk_bv_numeral', AST, (_in(CONTEXT), _in(UINT), _in_array(1, boolean)))
     */
    fun Context.Z3_mk_bv_numeral(sz: Int, bits: BooleanArray): Ast

    /*@}*/

    /** @name Sequences and regular expressions */
    /*@{*/

    /*@}*/
    /** @name Sequences and regular expressions
     */
    /*@{*/
    /**
     * \brief Create a sequence sort out of the sort for the elements.
     *
     * def_API('Z3_mk_seq_sort', SORT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_seq_sort(s: Sort): Sort

    /**
     * \brief Check if \c s is a sequence sort.
     *
     * def_API('Z3_is_seq_sort', boolean, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_is_seq_sort(s: Sort): Boolean

    /**
     * \brief Retrieve basis sort for sequence sort.
     *
     * def_API('Z3_get_seq_sort_basis', SORT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_seq_sort_basis(s: Sort): Sort

    /**
     * \brief Create a regular expression sort out of a sequence sort.
     *
     * def_API('Z3_mk_re_sort', SORT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_re_sort(seq: Sort): Sort

    /**
     * \brief Check if \c s is a regular expression sort.
     *
     * def_API('Z3_is_re_sort', boolean, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_is_re_sort(s: Sort): Boolean

    /**
     * \brief Retrieve basis sort for regex sort.
     *
     * def_API('Z3_get_re_sort_basis', SORT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_re_sort_basis(s: Sort): Sort

    /**
     * \brief Create a sort for 8 bit strings.
     *
     * This function creates a sort for ASCII strings.
     * Each character is 8 bits.
     *
     * def_API('Z3_mk_string_sort', SORT ,(_in(CONTEXT), ))
     */
    fun Context.Z3_mk_string_sort(): Sort

    /**
     * \brief Check if \c s is a string sort.
     *
     * def_API('Z3_is_string_sort', boolean, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_is_string_sort(s: Sort): Boolean

    /**
     * \brief Create a string constant out of the string that is passed in
     * def_API('Z3_mk_string' ,AST ,(_in(CONTEXT), _in(STRING)))
     */
    fun Context.Z3_mk_string(s: String): Ast

    /**
     * \brief Create a string constant out of the string that is passed in
     * It takes the length of the string as well to take into account
     * 0 characters. The string is unescaped.
     *
     * def_API('Z3_mk_lstring' ,AST ,(_in(CONTEXT), _in(UINT), _in(STRING)))
     */
    fun Context.Z3_mk_lstring(len: Int, s: String): Ast

    /**
     * \brief Determine if \c s is a string constant.
     *
     * def_API('Z3_is_string', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_string(s: Ast): Boolean

    /**
     * \brief Retrieve the string constant stored in \c s.
     *
     * \pre  Z3_is_string(c, s)
     *
     * def_API('Z3_get_string' ,STRING ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_string(s: Ast): String

    /**
     * \brief Retrieve the unescaped string constant stored in \c s.
     *
     * \pre  Z3_is_string(c, s)
     *
     * def_API('Z3_get_lstring' ,CHAR_PTR ,(_in(CONTEXT), _in(AST), _out(UINT)))
     */
    fun Context.Z3_get_lstring(s: Ast, length: IntByReference): String

    /**
     * \brief Create an empty sequence of the sequence sort \c seq.
     *
     * \pre s is a sequence sort.
     *
     * def_API('Z3_mk_seq_empty' ,AST ,(_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_seq_empty(seq: Sort): Ast

    /**
     * \brief Create a unit sequence of \c a.
     *
     * def_API('Z3_mk_seq_unit' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_seq_unit(a: Ast): Ast

    /**
     * \brief Concatenate sequences.
     *
     * \pre n > 0
     *
     * def_API('Z3_mk_seq_concat' ,AST ,(_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_seq_concat(n: Int, args: Array<Ast>?): Ast

    /**
     * \brief Check if \c prefix is a prefix of \c s.
     *
     * \pre prefix and s are the same sequence sorts.
     *
     * def_API('Z3_mk_seq_prefix' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_prefix(prefix: Ast, s: Ast): Ast

    /**
     * \brief Check if \c suffix is a suffix of \c s.
     *
     * \pre \c suffix and \c s are the same sequence sorts.
     *
     * def_API('Z3_mk_seq_suffix' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_suffix(suffix: Ast, s: Ast): Ast

    /**
     * \brief Check if \c container contains \c containee.
     *
     * \pre \c container and \c containee are the same sequence sorts.
     *
     * def_API('Z3_mk_seq_contains' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_contains(
        container: Ast,
        containee: Ast
    ): Ast


    /**
     * \brief Check if \c s1 is lexicographically strictly less than \c s2.
     *
     * \pre \c s1 and \c s2 are strings
     *
     * def_API('Z3_mk_str_lt' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_str_lt(prefix: Ast, s: Ast): Ast

    /**
     * \brief Check if \c s1 is equal or lexicographically strictly less than \c s2.
     *
     * \pre \c s1 and \c s2 are strings
     *
     * def_API('Z3_mk_str_le' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_str_le(prefix: Ast, s: Ast): Ast

    /**
     * \brief Extract subsequence starting at \c offset of \c length.
     *
     * def_API('Z3_mk_seq_extract' ,AST ,(_in(CONTEXT), _in(AST), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_extract(
        s: Ast,
        offset: Ast,
        length: Ast
    ): Ast

    /**
     * \brief Replace the first occurrence of \c src with \c dst in \c s.
     *
     * def_API('Z3_mk_seq_replace' ,AST ,(_in(CONTEXT), _in(AST), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_replace(
        s: Ast,
        src: Ast,
        dst: Ast
    ): Ast

    /**
     * \brief Retrieve from \c s the unit sequence positioned at position \c index.
     * The sequence is empty if the index is out of bounds.
     *
     * def_API('Z3_mk_seq_at' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_at(s: Ast, index: Ast): Ast

    /**
     * \brief Retrieve from \c s the element positioned at position \c index.
     * The function is under-specified if the index is out of bounds.
     *
     * def_API('Z3_mk_seq_nth' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_nth(s: Ast, index: Ast): Ast

    /**
     * \brief Return the length of the sequence \c s.
     *
     * def_API('Z3_mk_seq_length' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_seq_length(s: Ast): Ast


    /**
     * \brief Return index of first occurrence of \c substr in \c s starting from offset \c offset.
     * If \c s does not contain \c substr, then the value is -1, if \c offset is the length of \c s, then the value is -1 as well.
     * The value is -1 if \c offset is negative or larger than the length of \c s.
     *
     * def_API('Z3_mk_seq_index' ,AST ,(_in(CONTEXT), _in(AST), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_index(
        s: Ast,
        substr: Ast,
        offset: Ast
    ): Ast

    /**
     * \brief Return the last occurrence of \c substr in \c s.
     * If \c s does not contain \c substr, then the value is -1,
     * def_API('Z3_mk_seq_last_index', AST, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_last_index(
        s: Ast,
        substr: Ast
    ): Ast

    /**
     * \brief Convert string to integer.
     *
     * def_API('Z3_mk_str_to_int' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_str_to_int(s: Ast): Ast


    /**
     * \brief Integer to string conversion.
     *
     * def_API('Z3_mk_int_to_str' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_int_to_str(s: Ast): Ast

    /**
     * \brief Create a regular expression that accepts the sequence \c seq.
     *
     * def_API('Z3_mk_seq_to_re' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_seq_to_re(seq: Ast): Ast

    /**
     * \brief Check if \c seq is in the language generated by the regular expression \c re.
     *
     * def_API('Z3_mk_seq_in_re' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_seq_in_re(
        seq: Ast,
        re: Ast
    ): Ast

    /**
     * \brief Create the regular language \c re+.
     *
     * def_API('Z3_mk_re_plus' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_re_plus(re: Ast): Ast

    /**
     * \brief Create the regular language \c re*.
     *
     * def_API('Z3_mk_re_star' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_re_star(re: Ast): Ast

    /**
     * \brief Create the regular language \c [re].
     *
     * def_API('Z3_mk_re_option' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_re_option(re: Ast): Ast

    /**
     * \brief Create the union of the regular languages.
     *
     * \pre n > 0
     *
     * def_API('Z3_mk_re_union' ,AST ,(_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_re_union(
        n: Int,
        args: Array<Ast>?
    ): Ast

    /**
     * \brief Create the concatenation of the regular languages.
     *
     * \pre n > 0
     *
     * def_API('Z3_mk_re_concat' ,AST ,(_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_re_concat(
        n: Int,
        args: Array<Ast>?
    ): Ast


    /**
     * \brief Create the range regular expression over two sequences of length 1.
     *
     * def_API('Z3_mk_re_range' ,AST ,(_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_mk_re_range(
        lo: Ast,
        hi: Ast
    ): Ast

    /**
     * \brief Create a regular expression loop. The supplied regular expression \c r is repeated
     * between \c lo and \c hi times. The \c lo should be below \c hi with one exception: when
     * supplying the value \c hi as 0, the meaning is to repeat the argument \c r at least
     * \c lo number of times, and with an unbounded upper bound.
     *
     * def_API('Z3_mk_re_loop', AST, (_in(CONTEXT), _in(AST), _in(UINT), _in(UINT)))
     */
    fun Context.Z3_mk_re_loop(
        r: Ast,
        lo: Int,
        hi: Int
    ): Ast

    /**
     * \brief Create the intersection of the regular languages.
     *
     * \pre n > 0
     *
     * def_API('Z3_mk_re_intersect' ,AST ,(_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_re_intersect(
        n: Int,
        args: Array<Ast>
    ): Ast

    /**
     * \brief Create the complement of the regular language \c re.
     *
     * def_API('Z3_mk_re_complement' ,AST ,(_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_mk_re_complement(re: Ast): Ast

    /**
     * \brief Create an empty regular expression of sort \c re.
     *
     * \pre re is a regular expression sort.
     *
     * def_API('Z3_mk_re_empty' ,AST ,(_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_re_empty(re: Sort): Ast


    /**
     * \brief Create an universal regular expression of sort \c re.
     *
     * \pre re is a regular expression sort.
     *
     * def_API('Z3_mk_re_full' ,AST ,(_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_mk_re_full(re: Sort): Ast

    /*@}*/


    /** @name Special relations */
    /*@{*/
    /*@}*/
    /** @name Special relations
     */
    /*@{*/
    /**
     * \brief create a linear ordering relation over signature \c a.
     * The relation is identified by the index \c id.
     *
     * def_API('Z3_mk_linear_order', FUNC_DECL ,(_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_mk_linear_order(a: Sort, id: Int): FuncDecl

    /**
     * \brief create a partial ordering relation over signature \c a and index \c id.
     *
     * def_API('Z3_mk_partial_order', FUNC_DECL ,(_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_mk_partial_order(a: Sort, id: Int): FuncDecl

    /**
     * \brief create a piecewise linear ordering relation over signature \c a and index \c id.
     *
     * def_API('Z3_mk_piecewise_linear_order', FUNC_DECL ,(_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_mk_piecewise_linear_order(
        a: Sort,
        id: Int
    ): FuncDecl

    /**
     * \brief create a tree ordering relation over signature \c a identified using index \c id.
     *
     * def_API('Z3_mk_tree_order', FUNC_DECL, (_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_mk_tree_order(a: Sort, id: Int): FuncDecl

    /**
     * \brief create transitive closure of binary relation.
     *
     * \pre f is a binary relation, such that the two arguments have the same sorts.
     *
     * The resulting relation f+ represents the transitive closure of f.
     *
     * def_API('Z3_mk_transitive_closure', FUNC_DECL ,(_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_mk_transitive_closure(f: FuncDecl): FuncDecl

    /*@}*/

    /** @name Quantifiers */
    /*@{*/
    /*@}*/
    /** @name Quantifiers
     */
    /*@{*/
    /**
     * \brief Create a pattern for quantifier instantiation.
     *
     * Z3 uses pattern matching to instantiate quantifiers. If a
     * pattern is not provided for a quantifier, then Z3 will
     * automatically compute a set of patterns for it. However, for
     * optimal performance, the user should provide the patterns.
     *
     * Patterns comprise a list of terms. The list should be
     * non-empty.  If the list comprises of more than one term, it is
     * a called a multi-pattern.
     *
     * In general, one can pass in a list of (multi-)patterns in the
     * quantifier constructor.
     *
     * \sa Z3_mk_forall
     * \sa Z3_mk_exists
     *
     * def_API('Z3_mk_pattern', PATTERN, (_in(CONTEXT), _in(UINT), _in_array(1, AST)))
     */
    fun Context.Z3_mk_pattern(
        num_patterns: Int,
        terms: Array<Ast>?
    ): Pattern

    /**
     * \brief Create a bound variable.
     *
     * Bound variables are indexed by de-Bruijn indices. It is perhaps easiest to explain
     * the meaning of de-Bruijn indices by indicating the compilation process from
     * non-de-Bruijn formulas to de-Bruijn format.
     *
     * \verbatim
     * abs(forall (x1) phi) = forall (x1) abs1(phi, x1, 0)
     * abs(forall (x1, x2) phi) = abs(forall (x1) abs(forall (x2) phi))
     * abs1(x, x, n) = b_n
     * abs1(y, x, n) = y
     * abs1(f(t1,...,tn), x, n) = f(abs1(t1,x,n), ..., abs1(tn,x,n))
     * abs1(forall (x1) phi, x, n) = forall (x1) (abs1(phi, x, n+1))
     * \endverbatim
     *
     * The last line is significant: the index of a bound variable is different depending
     * on the scope in which it appears. The deeper x appears, the higher is its
     * index.
     *
     * \param c logical context
     * \param index de-Bruijn index
     * \param ty sort of the bound variable
     *
     * \sa Z3_mk_forall
     * \sa Z3_mk_exists
     *
     * def_API('Z3_mk_bound', AST, (_in(CONTEXT), _in(UINT), _in(SORT)))
     */
    fun Context.Z3_mk_bound(index: Int, ty: Sort): Ast

    /**
     * \brief Create a forall formula. It takes an expression \c body that contains bound variables
     * of the same sorts as the sorts listed in the array \c sorts. The bound variables are de-Bruijn indices created
     * using #Z3_mk_bound. The array \c decl_names contains the names that the quantified formula uses for the
     * bound variables. Z3 applies the convention that the last element in the \c decl_names and \c sorts array
     * refers to the variable with index 0, the second to last element of \c decl_names and \c sorts refers
     * to the variable with index 1, etc.
     *
     * \param c logical context.
     * \param weight quantifiers are associated with weights indicating the importance of using the quantifier during instantiation. By default, pass the weight 0.
     * \param num_patterns number of patterns.
     * \param patterns array containing the patterns created using #Z3_mk_pattern.
     * \param num_decls number of variables to be bound.
     * \param sorts the sorts of the bound variables.
     * \param decl_names names of the bound variables
     * \param body the body of the quantifier.
     *
     * \sa Z3_mk_pattern
     * \sa Z3_mk_bound
     * \sa Z3_mk_exists
     *
     * def_API('Z3_mk_forall', AST, (_in(CONTEXT), _in(UINT), _in(UINT), _in_array(2, PATTERN), _in(UINT), _in_array(4, SORT), _in_array(4, SYMBOL), _in(AST)))
     */
    fun Context.Z3_mk_forall(
        weight: Int,
        num_patterns: Int,
        patterns: Array<Pattern>?,
        num_decls: Int,
        sorts: Array<Sort>?,
        decl_names: Array<Symbol>?,
        body: Ast
    ): Ast

    /**
     * \brief Create an exists formula. Similar to #Z3_mk_forall.
     *
     * \sa Z3_mk_pattern
     * \sa Z3_mk_bound
     * \sa Z3_mk_forall
     * \sa Z3_mk_quantifier
     *
     * def_API('Z3_mk_exists', AST, (_in(CONTEXT), _in(UINT), _in(UINT), _in_array(2, PATTERN), _in(UINT), _in_array(4, SORT), _in_array(4, SYMBOL), _in(AST)))
     */
    fun Context.Z3_mk_exists(
        weight: Int,
        num_patterns: Int,
        patterns: Array<Pattern>?,
        num_decls: Int,
        sorts: Array<Sort>?,
        decl_names: Array<Symbol>?,
        body: Ast
    ): Ast

    /**
     * \brief Create a quantifier - universal or existential, with pattern hints.
     * See the documentation for #Z3_mk_forall for an explanation of the parameters.
     *
     * \param c logical context.
     * \param is_forall flag to indicate if this is a universal or existential quantifier.
     * \param weight quantifiers are associated with weights indicating the importance of using the quantifier during instantiation. By default, pass the weight 0.
     * \param num_patterns number of patterns.
     * \param patterns array containing the patterns created using #Z3_mk_pattern.
     * \param num_decls number of variables to be bound.
     * \param sorts array of sorts of the bound variables.
     * \param decl_names names of the bound variables.
     * \param body the body of the quantifier.
     *
     * \sa Z3_mk_pattern
     * \sa Z3_mk_bound
     * \sa Z3_mk_forall
     * \sa Z3_mk_exists
     *
     * def_API('Z3_mk_quantifier', AST, (_in(CONTEXT), _in(boolean), _in(UINT), _in(UINT), _in_array(3, PATTERN), _in(UINT), _in_array(5, SORT), _in_array(5, SYMBOL), _in(AST)))
     */
    fun Context.Z3_mk_quantifier(
        is_forall: Boolean,
        weight: Int,
        num_patterns: Int,
        patterns: Array<Pattern>?,
        num_decls: Int,
        sorts: Array<Sort>?,
        decl_names: Array<Symbol>?,
        body: Ast
    ): Ast


    /**
     * \brief Create a quantifier - universal or existential, with pattern hints, no patterns, and attributes
     *
     * \param c logical context.
     * \param is_forall flag to indicate if this is a universal or existential quantifier.
     * \param quantifier_id identifier to identify quantifier
     * \param skolem_id identifier to identify skolem constants introduced by quantifier.
     * \param weight quantifiers are associated with weights indicating the importance of using the quantifier during instantiation. By default, pass the weight 0.
     * \param num_patterns number of patterns.
     * \param patterns array containing the patterns created using #Z3_mk_pattern.
     * \param num_no_patterns number of no_patterns.
     * \param no_patterns array containing subexpressions to be excluded from inferred patterns.
     * \param num_decls number of variables to be bound.
     * \param sorts array of sorts of the bound variables.
     * \param decl_names names of the bound variables.
     * \param body the body of the quantifier.
     *
     * \sa Z3_mk_pattern
     * \sa Z3_mk_bound
     * \sa Z3_mk_forall
     * \sa Z3_mk_exists
     *
     * def_API('Z3_mk_quantifier_ex', AST, (_in(CONTEXT), _in(boolean), _in(UINT), _in(SYMBOL), _in(SYMBOL), _in(UINT), _in_array(5, PATTERN), _in(UINT), _in_array(7, AST), _in(UINT), _in_array(9, SORT), _in_array(9, SYMBOL), _in(AST)))
     */
    fun Context.Z3_mk_quantifier_ex(
        is_forall: Boolean,
        weight: Int,
        quantifier_id: Symbol,
        skolem_id: Symbol,
        num_patterns: Int,
        patterns: Array<Pattern>?,
        num_no_patterns: Int,
        no_patterns: Array<Ast>?,
        num_decls: Int,
        sorts: Array<Sort>?,
        decl_names: Array<Symbol>?,
        body: Ast
    ): Ast

    /**
     * \brief Create a universal quantifier using a list of constants that
     * will form the set of bound variables.
     *
     * \param c logical context.
     * \param weight quantifiers are associated with weights indicating the importance of using
     * the quantifier during instantiation. By default, pass the weight 0.
     * \param num_bound number of constants to be abstracted into bound variables.
     * \param bound array of constants to be abstracted into bound variables.
     * \param num_patterns number of patterns.
     * \param patterns array containing the patterns created using #Z3_mk_pattern.
     * \param body the body of the quantifier.
     *
     * \sa Z3_mk_pattern
     * \sa Z3_mk_exists_const
     *
     * def_API('Z3_mk_forall_const', AST, (_in(CONTEXT), _in(UINT), _in(UINT), _in_array(2, APP), _in(UINT), _in_array(4, PATTERN), _in(AST)))
     */
    fun Context.Z3_mk_forall_const(
        weight: Int,
        num_bound: Int,
        bound: Array<App>?,
        num_patterns: Int,
        patterns: Array<Pattern>?,
        body: Ast
    ): Ast

    /**
     * \brief Similar to #Z3_mk_forall_const.
     *
     * \brief Create an existential quantifier using a list of constants that
     * will form the set of bound variables.
     *
     * \param c logical context.
     * \param weight quantifiers are associated with weights indicating the importance of using
     * the quantifier during instantiation. By default, pass the weight 0.
     * \param num_bound number of constants to be abstracted into bound variables.
     * \param bound array of constants to be abstracted into bound variables.
     * \param num_patterns number of patterns.
     * \param patterns array containing the patterns created using #Z3_mk_pattern.
     * \param body the body of the quantifier.
     *
     * \sa Z3_mk_pattern
     * \sa Z3_mk_forall_const
     *
     * def_API('Z3_mk_exists_const', AST, (_in(CONTEXT), _in(UINT), _in(UINT), _in_array(2, APP), _in(UINT), _in_array(4, PATTERN), _in(AST)))
     */
    fun Context.Z3_mk_exists_const(
        weight: Int,
        num_bound: Int,
        bound: Array<App>?,
        num_patterns: Int,
        patterns: Array<Pattern>?,
        body: Ast
    ): Ast

    /**
     * \brief Create a universal or existential quantifier using a list of
     * constants that will form the set of bound variables.
     *
     * def_API('Z3_mk_quantifier_const', AST, (_in(CONTEXT), _in(boolean), _in(UINT), _in(UINT), _in_array(3, APP), _in(UINT), _in_array(5, PATTERN), _in(AST)))
     */
    fun Context.Z3_mk_quantifier_const(
        is_forall: Boolean,
        weight: Int,
        num_bound: Int,
        bound: Array<App>?, num_patterns: Int,
        patterns: Array<Pattern>?, body: Ast
    ): Ast

    /**
     * \brief Create a universal or existential quantifier using a list of
     * constants that will form the set of bound variables.
     *
     * def_API('Z3_mk_quantifier_const_ex', AST, (_in(CONTEXT), _in(boolean), _in(UINT), _in(SYMBOL), _in(SYMBOL), _in(UINT), _in_array(5, APP), _in(UINT), _in_array(7, PATTERN), _in(UINT), _in_array(9, AST), _in(AST)))
     */
    fun Context.Z3_mk_quantifier_const_ex(
        is_forall: Boolean,
        weight: Int,
        quantifier_id: Symbol,
        skolem_id: Symbol,
        num_bound: Int,
        bound: Array<App>?, num_patterns: Int,
        patterns: Array<Pattern>?, num_no_patterns: Int,
        no_patterns: Array<Ast>?, body: Ast
    ): Ast

    /**
     * \brief Create a lambda expression. It takes an expression \c body that contains bound variables
     * of the same sorts as the sorts listed in the array \c sorts. The bound variables are de-Bruijn indices created
     * using #Z3_mk_bound. The array \c decl_names contains the names that the quantified formula uses for the
     * bound variables. Z3 applies the convention that the last element in the \c decl_names and \c sorts array
     * refers to the variable with index 0, the second to last element of \c decl_names and \c sorts refers
     * to the variable with index 1, etc.
     * The sort of the resulting expression is \c (Array sorts range) where \c range is the sort of \c body.
     * For example, if the lambda binds two variables of sort \c Int and \c boolean, and the \c body has sort \c Real,
     * the sort of the expression is \c (Array Int boolean Real).
     *
     * \param c logical context
     * \param num_decls number of variables to be bound.
     * \param sorts the sorts of the bound variables.
     * \param decl_names names of the bound variables
     * \param body the body of the lambda expression.
     *
     * \sa Z3_mk_bound
     * \sa Z3_mk_forall
     * \sa Z3_mk_lambda_const
     *
     * def_API('Z3_mk_lambda', AST, (_in(CONTEXT), _in(UINT), _in_array(1, SORT), _in_array(1, SYMBOL), _in(AST)))
     */
    fun Context.Z3_mk_lambda(
        num_decls: Int,
        sorts: Array<Sort>?,
        decl_names: Array<Symbol>?,
        body: Ast
    ): Ast

    /**
     * \brief Create a lambda expression using a list of constants that form the set
     * of bound variables
     *
     * \param c logical context.
     * \param num_bound number of constants to be abstracted into bound variables.
     * \param bound array of constants to be abstracted into bound variables.
     * \param body the body of the lambda expression.
     *
     * \sa Z3_mk_bound
     * \sa Z3_mk_forall
     * \sa Z3_mk_lambda
     *
     * def_API('Z3_mk_lambda_const', AST, (_in(CONTEXT), _in(UINT), _in_array(1, APP), _in(AST)))
     */
    fun Context.Z3_mk_lambda_const(
        num_bound: Int,
        bound: Array<App>?, body: Ast
    ): Ast


    /*@}*/

    /** @name Accessors */
    /*@{*/
    /*@}*/
    /** @name Accessors
     */
    /*@{*/
    /**
     * \brief Return \c Z3_INT_SYMBOL if the symbol was constructed
     * using #Z3_mk_int_symbol, and \c String_SYMBOL if the symbol
     * was constructed using #Z3_mk_string_symbol.
     *
     * def_API('Z3_get_symbol_kind', UINT, (_in(CONTEXT), _in(SYMBOL)))
     */
    fun Context.Z3_get_symbol_kind(s: Symbol): Int

    /**
     * \brief Return the symbol int value.
     *
     * \pre Z3_get_symbol_kind(s) == Z3_INT_SYMBOL
     *
     * \sa Z3_mk_int_symbol
     *
     * def_API('Z3_get_symbol_int', INT, (_in(CONTEXT), _in(SYMBOL)))
     */
    fun Context.Z3_get_symbol_int(s: Symbol): Int

    /**
     * \brief Return the symbol name.
     *
     * \pre Z3_get_symbol_kind(s) == String_SYMBOL
     *
     * \warning The returned buffer is statically allocated by Z3. It will
     * be automatically deallocated when #Z3_del_context is invoked.
     * So, the buffer is invalidated in the next call to \c Z3_get_symbol_string.
     *
     * \sa Z3_mk_string_symbol
     *
     * def_API('Z3_get_symbol_string', STRING, (_in(CONTEXT), _in(SYMBOL)))
     */
    fun Context.Z3_get_symbol_string(s: Symbol): String

    /**
     * \brief Return the sort name as a symbol.
     *
     * def_API('Z3_get_sort_name', SYMBOL, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_sort_name(d: Sort): Symbol

    /**
     * \brief Return a unique identifier for \c s.
     *
     * def_API('Z3_get_sort_id', UINT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_sort_id(s: Sort): Int

    /**
     * \brief Convert a \c Z3_sort into \c Z3_ast. This is just type casting.
     *
     * def_API('Z3_sort_to_ast', AST, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_sort_to_ast(s: Sort): Ast

    /**
     * \brief compare sorts.
     *
     * def_API('Z3_is_eq_sort', boolean, (_in(CONTEXT), _in(SORT), _in(SORT)))
     */
    fun Context.Z3_is_eq_sort(
        s1: Sort,
        s2: Sort
    ): Boolean

    /**
     * \brief Return the sort kind (e.g., array, tuple, int, boolean, etc).
     *
     * \sa Z3_sort_kind
     *
     * def_API('Z3_get_sort_kind', UINT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_sort_kind(t: Sort): Int

    /**
     * \brief Return the size of the given bit-vector sort.
     *
     * \pre Z3_get_sort_kind(c, t) == Z3_BV_SORT
     *
     * \sa Z3_mk_bv_sort
     * \sa Z3_get_sort_kind
     *
     * def_API('Z3_get_bv_sort_size', UINT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_bv_sort_size(t: Sort): Int

    /**
     * \brief Store the size of the sort in \c r. Return \c false if the call failed.
     * That is, Z3_get_sort_kind(s) == Z3_FINITE_DOMAIN_SORT
     *
     * def_API('Z3_get_finite_domain_sort_size', boolean, (_in(CONTEXT), _in(SORT), _out(UINT64)))
     */
    fun Context.Z3_get_finite_domain_sort_size(
        s: Sort,
        r: LongByReference
    ): Boolean

    /**
     * \brief Return the domain of the given array sort.
     * In the case of a multi-dimensional array, this function returns the sort of the first dimension.
     *
     * \pre Z3_get_sort_kind(c, t) == Z3_ARRAY_SORT
     *
     * \sa Z3_mk_array_sort
     * \sa Z3_get_sort_kind
     *
     * def_API('Z3_get_array_sort_domain', SORT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_array_sort_domain(t: Sort): Sort

    /**
     * \brief Return the range of the given array sort.
     *
     * \pre Z3_get_sort_kind(c, t) == Z3_ARRAY_SORT
     *
     * \sa Z3_mk_array_sort
     * \sa Z3_get_sort_kind
     *
     * def_API('Z3_get_array_sort_range', SORT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_array_sort_range(t: Sort): Sort

    /**
     * \brief Return the constructor declaration of the given tuple
     * sort.
     *
     * \pre Z3_get_sort_kind(c, t) == Z3_DATATYPE_SORT
     *
     * \sa Z3_mk_tuple_sort
     * \sa Z3_get_sort_kind
     *
     * def_API('Z3_get_tuple_sort_mk_decl', FUNC_DECL, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_tuple_sort_mk_decl(t: Sort): FuncDecl

    /**
     * \brief Return the number of fields of the given tuple sort.
     *
     * \pre Z3_get_sort_kind(c, t) == Z3_DATATYPE_SORT
     *
     * \sa Z3_mk_tuple_sort
     * \sa Z3_get_sort_kind
     *
     * def_API('Z3_get_tuple_sort_num_fields', UINT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_tuple_sort_num_fields(t: Sort): Int

    /**
     * \brief Return the i-th field declaration (i.e., projection function declaration)
     * of the given tuple sort.
     *
     * \pre Z3_get_sort_kind(t) == Z3_DATATYPE_SORT
     * \pre i < Z3_get_tuple_sort_num_fields(c, t)
     *
     * \sa Z3_mk_tuple_sort
     * \sa Z3_get_sort_kind
     *
     * def_API('Z3_get_tuple_sort_field_decl', FUNC_DECL, (_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_get_tuple_sort_field_decl(
        t: Sort,
        i: Int
    ): FuncDecl

    /**
     * \brief Return number of constructors for datatype.
     *
     * \pre Z3_get_sort_kind(t) == Z3_DATATYPE_SORT
     *
     * \sa Z3_get_datatype_sort_constructor
     * \sa Z3_get_datatype_sort_recognizer
     * \sa Z3_get_datatype_sort_constructor_accessor
     *
     * def_API('Z3_get_datatype_sort_num_constructors', UINT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_datatype_sort_num_constructors(
        t: Sort
    ): Int

    /**
     * \brief Return idx'th constructor.
     *
     * \pre Z3_get_sort_kind(t) == Z3_DATATYPE_SORT
     * \pre idx < Z3_get_datatype_sort_num_constructors(c, t)
     *
     * \sa Z3_get_datatype_sort_num_constructors
     * \sa Z3_get_datatype_sort_recognizer
     * \sa Z3_get_datatype_sort_constructor_accessor
     *
     * def_API('Z3_get_datatype_sort_constructor', FUNC_DECL, (_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_get_datatype_sort_constructor(
        t: Sort, idx: Int
    ): FuncDecl

    /**
     * \brief Return idx'th recognizer.
     *
     * \pre Z3_get_sort_kind(t) == Z3_DATATYPE_SORT
     * \pre idx < Z3_get_datatype_sort_num_constructors(c, t)
     *
     * \sa Z3_get_datatype_sort_num_constructors
     * \sa Z3_get_datatype_sort_constructor
     * \sa Z3_get_datatype_sort_constructor_accessor
     *
     * def_API('Z3_get_datatype_sort_recognizer', FUNC_DECL, (_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_get_datatype_sort_recognizer(
        t: Sort, idx: Int
    ): FuncDecl

    /**
     * \brief Return idx_a'th accessor for the idx_c'th constructor.
     *
     * \pre Z3_get_sort_kind(t) == Z3_DATATYPE_SORT
     * \pre idx_c < Z3_get_datatype_sort_num_constructors(c, t)
     * \pre idx_a < Z3_get_domain_size(c, Z3_get_datatype_sort_constructor(c, idx_c))
     *
     * \sa Z3_get_datatype_sort_num_constructors
     * \sa Z3_get_datatype_sort_constructor
     * \sa Z3_get_datatype_sort_recognizer
     *
     * def_API('Z3_get_datatype_sort_constructor_accessor', FUNC_DECL, (_in(CONTEXT), _in(SORT), _in(UINT), _in(UINT)))
     */
    fun Context.Z3_get_datatype_sort_constructor_accessor(
        t: Sort,
        idx_c: Int,
        idx_a: Int
    ): FuncDecl

    /**
     * \brief Update record field with a value.
     *
     * This corresponds to the 'with' construct in OCaml.
     * It has the effect of updating a record field with a given value.
     * The remaining fields are left unchanged. It is the record
     * equivalent of an array store (see \sa Z3_mk_store).
     * If the datatype has more than one constructor, then the update function
     * behaves as identity if there is a mismatch between the accessor and
     * constructor. For example ((_ update-field car) nil 1) is nil,
     * while ((_ update-field car) (cons 2 nil) 1) is (cons 1 nil).
     *
     *
     * \pre Z3_get_sort_kind(Z3_get_sort(c, t)) == Z3_get_domain(c, field_access, 1) == Z3_DATATYPE_SORT
     * \pre Z3_get_sort(c, value) == Z3_get_range(c, field_access)
     *
     *
     * def_API('Z3_datatype_update_field', AST, (_in(CONTEXT), _in(FUNC_DECL), _in(AST), _in(AST)))
     */
    fun Context.Z3_datatype_update_field(
        field_access: FuncDecl, t: Ast,
        value: Ast
    ): Ast

    /**
     * \brief Return arity of relation.
     *
     * \pre Z3_get_sort_kind(s) == Z3_RELATION_SORT
     *
     * \sa Z3_get_relation_column
     *
     * def_API('Z3_get_relation_arity', UINT, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_get_relation_arity(s: Sort): Int

    /**
     * \brief Return sort at i'th column of relation sort.
     *
     * \pre Z3_get_sort_kind(c, s) == Z3_RELATION_SORT
     * \pre col < Z3_get_relation_arity(c, s)
     *
     * \sa Z3_get_relation_arity
     *
     * def_API('Z3_get_relation_column', SORT, (_in(CONTEXT), _in(SORT), _in(UINT)))
     */
    fun Context.Z3_get_relation_column(
        s: Sort,
        col: Int
    ): Sort

    /**
     * \brief Pseudo-boolean relations.
     *
     * Encode p1 + p2 + ... + pn <= k
     *
     * def_API('Z3_mk_atmost', AST, (_in(CONTEXT), _in(UINT), _in_array(1,AST), _in(UINT)))
     */
    fun Context.Z3_mk_atmost(
        num_args: Int, args: Array<Ast>?,
        k: Int
    ): Ast

    /**
     * \brief Pseudo-boolean relations.
     *
     * Encode p1 + p2 + ... + pn >= k
     *
     * def_API('Z3_mk_atleast', AST, (_in(CONTEXT), _in(UINT), _in_array(1,AST), _in(UINT)))
     */
    fun Context.Z3_mk_atleast(
        num_args: Int, args: Array<Ast>?,
        k: Int
    ): Ast

    /**
     * \brief Pseudo-boolean relations.
     *
     * Encode k1*p1 + k2*p2 + ... + kn*pn <= k
     *
     * def_API('Z3_mk_pble', AST, (_in(CONTEXT), _in(UINT), _in_array(1,AST), _in_array(1,INT), _in(INT)))
     */
    fun Context.Z3_mk_pble(
        num_args: Int, args: Array<Ast>?,
        coeffs: IntArray?, k: Int
    ): Ast

    /**
     * \brief Pseudo-boolean relations.
     *
     * Encode k1*p1 + k2*p2 + ... + kn*pn >= k
     *
     * def_API('Z3_mk_pbge', AST, (_in(CONTEXT), _in(UINT), _in_array(1,AST), _in_array(1,INT), _in(INT)))
     */
    fun Context.Z3_mk_pbge(
        num_args: Int, args: Array<Ast>?,
        coeffs: IntArray?, k: Int
    ): Ast

    /**
     * \brief Pseudo-boolean relations.
     *
     * Encode k1*p1 + k2*p2 + ... + kn*pn = k
     *
     * def_API('Z3_mk_pbeq', AST, (_in(CONTEXT), _in(UINT), _in_array(1,AST), _in_array(1,INT), _in(INT)))
     */
    fun Context.Z3_mk_pbeq(
        num_args: Int, args: Array<Ast>?,
        coeffs: IntArray?, k: Int
    ): Ast

    /**
     * \brief Convert a \c Z3_func_decl into \c Z3_ast. This is just type casting.
     *
     * def_API('Z3_func_decl_to_ast', AST, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_func_decl_to_ast(f: FuncDecl): Ast

    /**
     * \brief Compare terms.
     *
     * def_API('Z3_is_eq_func_decl', boolean, (_in(CONTEXT), _in(FUNC_DECL), _in(FUNC_DECL)))
     */
    fun Context.Z3_is_eq_func_decl(
        f1: FuncDecl,
        f2: FuncDecl
    ): Boolean

    /**
     * \brief Return a unique identifier for \c f.
     *
     * def_API('Z3_get_func_decl_id', UINT, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_get_func_decl_id(f: FuncDecl): Int

    /**
     * \brief Return the constant declaration name as a symbol.
     *
     * def_API('Z3_get_decl_name', SYMBOL, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_get_decl_name(d: FuncDecl): Symbol

    /**
     * \brief Return declaration kind corresponding to declaration.
     *
     * def_API('Z3_get_decl_kind', UINT, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_get_decl_kind(d: FuncDecl): Int

    /**
     * \brief Return the number of parameters of the given declaration.
     *
     * \sa Z3_get_arity
     *
     * def_API('Z3_get_domain_size', UINT, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_get_domain_size(d: FuncDecl): Int

    /**
     * \brief Alias for \c Z3_get_domain_size.
     *
     * \sa Z3_get_domain_size
     *
     * def_API('Z3_get_arity', UINT, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_get_arity(d: FuncDecl): Int

    /**
     * \brief Return the sort of the i-th parameter of the given function declaration.
     *
     * \pre i < Z3_get_domain_size(d)
     *
     * \sa Z3_get_domain_size
     *
     * def_API('Z3_get_domain', SORT, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_domain(d: FuncDecl, i: Int): Sort

    /**
     * \brief Return the range of the given declaration.
     *
     * If \c d is a constant (i.e., has zero arguments), then this
     * function returns the sort of the constant.
     *
     * def_API('Z3_get_range', SORT, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_get_range(d: FuncDecl): Sort

    /**
     * \brief Return the number of parameters associated with a declaration.
     *
     * def_API('Z3_get_decl_num_parameters', UINT, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_get_decl_num_parameters(d: FuncDecl): Int

    /**
     * \brief Return the parameter type associated with a declaration.
     *
     * \param c the context
     * \param d the function declaration
     * \param idx is the index of the named parameter it should be between 0 and the number of parameters.
     *
     * def_API('Z3_get_decl_parameter_kind', UINT, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_parameter_kind(
        d: FuncDecl,
        idx: Int
    ): Int

    /**
     * \brief Return the integer value associated with an integer parameter.
     *
     * \pre Z3_get_decl_parameter_kind(c, d, idx) == Z3_PARAMETER_INT
     *
     * def_API('Z3_get_decl_int_parameter', INT, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_int_parameter(
        d: FuncDecl,
        idx: Int
    ): Int

    /**
     * \brief Return the double value associated with an double parameter.
     *
     * \pre Z3_get_decl_parameter_kind(c, d, idx) == Z3_PARAMETER_DOUBLE
     *
     * def_API('Z3_get_decl_double_parameter', DOUBLE, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_double_parameter(
        d: FuncDecl,
        idx: Int
    ): Double

    /**
     * \brief Return the double value associated with an double parameter.
     *
     * \pre Z3_get_decl_parameter_kind(c, d, idx) == Z3_PARAMETER_SYMBOL
     *
     * def_API('Z3_get_decl_symbol_parameter', SYMBOL, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_symbol_parameter(
        d: FuncDecl,
        idx: Int
    ): Symbol

    /**
     * \brief Return the sort value associated with a sort parameter.
     *
     * \pre Z3_get_decl_parameter_kind(c, d, idx) == Z3_PARAMETER_SORT
     *
     * def_API('Z3_get_decl_sort_parameter', SORT, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_sort_parameter(
        d: FuncDecl,
        idx: Int
    ): Sort

    /**
     * \brief Return the expression value associated with an expression parameter.
     *
     * \pre Z3_get_decl_parameter_kind(c, d, idx) == Z3_PARAMETER_AST
     *
     * def_API('Z3_get_decl_ast_parameter', AST, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_ast_parameter(
        d: FuncDecl,
        idx: Int
    ): Ast

    /**
     * \brief Return the expression value associated with an expression parameter.
     *
     * \pre Z3_get_decl_parameter_kind(c, d, idx) == Z3_PARAMETER_FUNC_DECL
     *
     * def_API('Z3_get_decl_func_decl_parameter', FUNC_DECL, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_func_decl_parameter(
        d: FuncDecl,
        idx: Int
    ): FuncDecl

    /**
     * \brief Return the rational value, as a string, associated with a rational parameter.
     *
     * \pre Z3_get_decl_parameter_kind(c, d, idx) == Z3_PARAMETER_RATIONAL
     *
     * def_API('Z3_get_decl_rational_parameter', STRING, (_in(CONTEXT), _in(FUNC_DECL), _in(UINT)))
     */
    fun Context.Z3_get_decl_rational_parameter(
        d: FuncDecl,
        idx: Int
    ): String

    /**
     * \brief Convert a \c Z3_app into \c Z3_ast. This is just type casting.
     *
     * def_API('Z3_app_to_ast', AST, (_in(CONTEXT), _in(APP)))
     */
    fun Context.Z3_app_to_ast(a: App): Ast

    /**
     * \brief Return the declaration of a constant or function application.
     *
     * def_API('Z3_get_app_decl', FUNC_DECL, (_in(CONTEXT), _in(APP)))
     */
    fun Context.Z3_get_app_decl(a: App): FuncDecl

    /**
     * \brief Return the number of argument of an application. If \c t
     * is an constant, then the number of arguments is 0.
     *
     * def_API('Z3_get_app_num_args', UINT, (_in(CONTEXT), _in(APP)))
     */
    fun Context.Z3_get_app_num_args(a: App): Int

    /**
     * \brief Return the i-th argument of the given application.
     *
     * \pre i < Z3_get_app_num_args(c, a)
     *
     * def_API('Z3_get_app_arg', AST, (_in(CONTEXT), _in(APP), _in(UINT)))
     */
    fun Context.Z3_get_app_arg(a: App, i: Int): Ast

    /**
     * \brief Compare terms.
     *
     * def_API('Z3_is_eq_ast', boolean, (_in(CONTEXT), _in(AST), _in(AST)))
     */
    fun Context.Z3_is_eq_ast(
        t1: Ast,
        t2: Ast
    ): Boolean

    /**
     * \brief Return a unique identifier for \c t.
     * The identifier is unique up to structural equality. Thus, two ast nodes
     * created by the same context and having the same children and same function symbols
     * have the same identifiers. Ast nodes created in the same context, but having
     * different children or different functions have different identifiers.
     * Variables and quantifiers are also assigned different identifiers according to
     * their structure.
     *
     * def_API('Z3_get_ast_id', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_ast_id(t: Ast): Int

    /**
     * \brief Return a hash code for the given AST.
     * The hash code is structural. You can use Z3_get_ast_id interchangeably with
     * this function.
     *
     * def_API('Z3_get_ast_hash', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_ast_hash(a: Ast): Int

    /**
     * \brief Return the sort of an AST node.
     *
     * The AST node must be a constant, application, numeral, bound variable, or quantifier.
     *
     * def_API('Z3_get_sort', SORT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_sort(a: Ast): Sort

    /**
     * \brief Return \c true if the given expression \c t is well sorted.
     *
     * def_API('Z3_is_well_sorted', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_well_sorted(t: Ast): Boolean

    /**
     * \brief Return \c Z3_L_TRUE if \c a is true, \c Z3_L_FALSE if it is false, and \c Z3_L_UNDEF otherwise.
     *
     * def_API('Z3_get_boolean_value', INT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_boolean_value(a: Ast): Int

    /**
     * \brief Return the kind of the given AST.
     *
     * def_API('Z3_get_ast_kind', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_ast_kind(a: Ast): Int

    /**
     * def_API('Z3_is_app', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_app(a: Ast): Boolean

    /**
     * def_API('Z3_is_numeral_ast', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_numeral_ast(a: Ast): Boolean

    /**
     * \brief Return \c true if the given AST is a real algebraic number.
     *
     * def_API('Z3_is_algebraic_number', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_algebraic_number(a: Ast): Boolean

    /**
     * \brief Convert an \c ast into an \c APP_AST. This is just type casting.
     *
     * \pre \code Z3_get_ast_kind(c, a) == \c Z3_APP_AST \endcode
     *
     * def_API('Z3_to_app', APP, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_to_app(a: Ast): App

    /**
     * \brief Convert an AST into a FUNC_DECL_AST. This is just type casting.
     *
     * \pre \code Z3_get_ast_kind(c, a) == Z3_FUNC_DECL_AST \endcode
     *
     * def_API('Z3_to_func_decl', FUNC_DECL, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_to_func_decl(a: Ast): FuncDecl

    /**
     * \brief Return numeral value, as a string of a numeric constant term
     *
     * \pre Z3_get_ast_kind(c, a) == Z3_NUMERAL_AST
     *
     * def_API('Z3_get_numeral_string', STRING, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_numeral_string(a: Ast): String

    /**
     * \brief Return numeral as a string in decimal notation.
     * The result has at most \c precision decimal places.
     *
     * \pre Z3_get_ast_kind(c, a) == Z3_NUMERAL_AST || Z3_is_algebraic_number(c, a)
     *
     * def_API('Z3_get_numeral_decimal_string', STRING, (_in(CONTEXT), _in(AST), _in(UINT)))
     */
    fun Context.Z3_get_numeral_decimal_string(
        a: Ast,
        precision: Int
    ): String

    /**
     * \brief Return numeral as a double.
     *
     * \pre Z3_get_ast_kind(c, a) == Z3_NUMERAL_AST || Z3_is_algebraic_number(c, a)
     *
     * def_API('Z3_get_numeral_double', DOUBLE, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_numeral_double(a: Ast): Double

    /**
     * \brief Return the numerator (as a numeral AST) of a numeral AST of sort Real.
     *
     * \pre Z3_get_ast_kind(c, a) == Z3_NUMERAL_AST
     *
     * def_API('Z3_get_numerator', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_numerator(a: Ast): Ast

    /**
     * \brief Return the denominator (as a numeral AST) of a numeral AST of sort Real.
     *
     * \pre Z3_get_ast_kind(c, a) == Z3_NUMERAL_AST
     *
     * def_API('Z3_get_denominator', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_denominator(a: Ast): Ast

    /**
     * \brief Return numeral value, as a pair of 64 bit numbers if the representation fits.
     *
     * \param c logical context.
     * \param a term.
     * \param num numerator.
     * \param den denominator.
     *
     * Return \c true if the numeral value fits in 64 bit numerals, \c false otherwise.
     *
     * \pre Z3_get_ast_kind(a) == Z3_NUMERAL_AST
     *
     * def_API('Z3_get_numeral_small', boolean, (_in(CONTEXT), _in(AST), _out(INT64), _out(INT64)))
     */
    fun Context.Z3_get_numeral_small(
        a: Ast,
        num: LongByReference,
        den: LongByReference
    ): Boolean

    /**
     * \brief Similar to #Z3_get_numeral_string, but only succeeds if
     * the value can fit in a machine int. Return \c true if the call succeeded.
     *
     * \pre Z3_get_ast_kind(c, v) == Z3_NUMERAL_AST
     *
     * \sa Z3_get_numeral_string
     *
     * def_API('Z3_get_numeral_int', boolean, (_in(CONTEXT), _in(AST), _out(INT)))
     */
    fun Context.Z3_get_numeral_int(
        v: Ast,
        i: IntByReference
    ): Boolean

    /**
     * \brief Similar to #Z3_get_numeral_string, but only succeeds if
     * the value can fit in a machine int int. Return \c true if the call succeeded.
     *
     * \pre Z3_get_ast_kind(c, v) == Z3_NUMERAL_AST
     *
     * \sa Z3_get_numeral_string
     *
     * def_API('Z3_get_numeral_uint', boolean, (_in(CONTEXT), _in(AST), _out(UINT)))
     */
    fun Context.Z3_get_numeral_uint(
        v: Ast,
        u: IntByReference
    ): Boolean

    /**
     * \brief Similar to #Z3_get_numeral_string, but only succeeds if
     * the value can fit in a machine \c long int. Return \c true if the call succeeded.
     *
     * \pre Z3_get_ast_kind(c, v) == Z3_NUMERAL_AST
     *
     * \sa Z3_get_numeral_string
     *
     * def_API('Z3_get_numeral_uint64', boolean, (_in(CONTEXT), _in(AST), _out(UINT64)))
     */
    fun Context.Z3_get_numeral_uint64(
        v: Ast,
        u: LongByReference
    ): Boolean

    /**
     * \brief Similar to #Z3_get_numeral_string, but only succeeds if
     * the value can fit in a machine \c long int. Return \c true if the call succeeded.
     *
     * \pre Z3_get_ast_kind(c, v) == Z3_NUMERAL_AST
     *
     * \sa Z3_get_numeral_string
     *
     * def_API('Z3_get_numeral_int64', boolean, (_in(CONTEXT), _in(AST), _out(INT64)))
     */
    fun Context.Z3_get_numeral_int64(
        v: Ast,
        i: LongByReference
    ): Boolean

    /**
     * \brief Similar to #Z3_get_numeral_string, but only succeeds if
     * the value can fit as a rational number as machine \c long int. Return \c true if the call succeeded.
     *
     * \pre Z3_get_ast_kind(c, v) == Z3_NUMERAL_AST
     *
     * \sa Z3_get_numeral_string
     *
     * def_API('Z3_get_numeral_rational_int64', boolean, (_in(CONTEXT), _in(AST), _out(INT64), _out(INT64)))
     */
    fun Context.Z3_get_numeral_rational_int64(
        v: Ast,
        num: LongByReference,
        den: LongByReference
    ): Boolean

    /**
     * \brief Return a lower bound for the given real algebraic number.
     * The interval isolating the number is smaller than 1/10^precision.
     * The result is a numeral AST of sort Real.
     *
     * \pre Z3_is_algebraic_number(c, a)
     *
     * def_API('Z3_get_algebraic_number_lower', AST, (_in(CONTEXT), _in(AST), _in(UINT)))
     */
    fun Context.Z3_get_algebraic_number_lower(
        a: Ast,
        precision: Int
    ): Ast

    /**
     * \brief Return a upper bound for the given real algebraic number.
     * The interval isolating the number is smaller than 1/10^precision.
     * The result is a numeral AST of sort Real.
     *
     * \pre Z3_is_algebraic_number(c, a)
     *
     * def_API('Z3_get_algebraic_number_upper', AST, (_in(CONTEXT), _in(AST), _in(UINT)))
     */
    fun Context.Z3_get_algebraic_number_upper(
        a: Ast,
        precision: Int
    ): Ast

    /**
     * \brief Convert a Z3_pattern into Z3_ast. This is just type casting.
     *
     * def_API('Z3_pattern_to_ast', AST, (_in(CONTEXT), _in(PATTERN)))
     */
    fun Context.Z3_pattern_to_ast(p: Pattern): Ast

    /**
     * \brief Return number of terms in pattern.
     *
     * def_API('Z3_get_pattern_num_terms', UINT, (_in(CONTEXT), _in(PATTERN)))
     */
    fun Context.Z3_get_pattern_num_terms(p: Pattern): Int

    /**
     * \brief Return i'th ast in pattern.
     *
     * def_API('Z3_get_pattern', AST, (_in(CONTEXT), _in(PATTERN), _in(UINT)))
     */
    fun Context.Z3_get_pattern(p: Pattern, idx: Int): Ast

    /**
     * \brief Return index of de-Bruijn bound variable.
     *
     * \pre Z3_get_ast_kind(a) == Z3_VAR_AST
     *
     * def_API('Z3_get_index_value', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_index_value(a: Ast): Int

    /**
     * \brief Determine if an ast is a universal quantifier.
     *
     * def_API('Z3_is_quantifier_forall', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_quantifier_forall(a: Ast): Boolean

    /**
     * \brief Determine if ast is an existential quantifier.
     *
     *
     * def_API('Z3_is_quantifier_exists', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_quantifier_exists(a: Ast): Boolean

    /**
     * \brief Determine if ast is a lambda expression.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_is_lambda', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_lambda(a: Ast): Boolean

    /**
     * \brief Obtain weight of quantifier.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_weight', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_quantifier_weight(a: Ast): Int

    /**
     * \brief Return number of patterns used in quantifier.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_num_patterns', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_quantifier_num_patterns(a: Ast): Int

    /**
     * \brief Return i'th pattern.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_pattern_ast', PATTERN, (_in(CONTEXT), _in(AST), _in(UINT)))
     */
    fun Context.Z3_get_quantifier_pattern_ast(
        a: Ast,
        i: Int
    ): Pattern

    /**
     * \brief Return number of no_patterns used in quantifier.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_num_no_patterns', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_quantifier_num_no_patterns(a: Ast): Int

    /**
     * \brief Return i'th no_pattern.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_no_pattern_ast', AST, (_in(CONTEXT), _in(AST), _in(UINT)))
     */
    fun Context.Z3_get_quantifier_no_pattern_ast(
        a: Ast,
        i: Int
    ): Ast

    /**
     * \brief Return number of bound variables of quantifier.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_num_bound', UINT, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_quantifier_num_bound(a: Ast): Int

    /**
     * \brief Return symbol of the i'th bound variable.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_bound_name', SYMBOL, (_in(CONTEXT), _in(AST), _in(UINT)))
     */
    fun Context.Z3_get_quantifier_bound_name(
        a: Ast,
        i: Int
    ): Symbol

    /**
     * \brief Return sort of the i'th bound variable.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_bound_sort', SORT, (_in(CONTEXT), _in(AST), _in(UINT)))
     */
    fun Context.Z3_get_quantifier_bound_sort(
        a: Ast,
        i: Int
    ): Sort

    /**
     * \brief Return body of quantifier.
     *
     * \pre Z3_get_ast_kind(a) == Z3_QUANTIFIER_AST
     *
     * def_API('Z3_get_quantifier_body', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_quantifier_body(a: Ast): Ast

    /**
     * \brief Interface to simplifier.
     *
     * Provides an interface to the AST simplifier used by Z3.
     * It returns an AST object which is equal to the argument.
     * The returned AST is simplified using algebraic simplification rules,
     * such as constant propagation (propagating true/false over logical connectives).
     *
     * \sa Z3_simplify_ex
     *
     * def_API('Z3_simplify', AST, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_simplify(a: Ast): Ast

    /**
     * \brief Interface to simplifier.
     *
     * Provides an interface to the AST simplifier used by Z3.
     * This procedure is similar to #Z3_simplify, but the behavior of the simplifier
     * can be configured using the given parameter set.
     *
     * \sa Z3_simplify
     * \sa Z3_simplify_get_help
     * \sa Z3_simplify_get_param_descrs
     *
     * def_API('Z3_simplify_ex', AST, (_in(CONTEXT), _in(AST), _in(PARAMS)))
     */
    fun Context.Z3_simplify_ex(
        a: Ast,
        p: Params
    ): Ast

    /**
     * \brief Return a string describing all available parameters.
     *
     * \sa Z3_simplify_ex
     * \sa Z3_simplify_get_param_descrs
     *
     * def_API('Z3_simplify_get_help', STRING, (_in(CONTEXT),))
     */
    fun Z3_simplify_get_help(c: Context): String

    /**
     * \brief Return the parameter description set for the simplify procedure.
     *
     * \sa Z3_simplify_ex
     * \sa Z3_simplify_get_help
     *
     * def_API('Z3_simplify_get_param_descrs', PARAM_DESCRS, (_in(CONTEXT),))
     */
    fun Context.Z3_simplify_get_param_descrs(): ParamDescrs
    /*@}*/

    /** @name Modifiers */
    /*@{*/
    /*@}*/
    /** @name Modifiers
     */
    /*@{*/
    /**
     * \brief Update the arguments of term \c a using the arguments \c args.
     * The number of arguments \c num_args should coincide
     * with the number of arguments to \c a.
     * If \c a is a quantifier, then num_args has to be 1.
     *
     * def_API('Z3_update_term', AST, (_in(CONTEXT), _in(AST), _in(UINT), _in_array(2, AST)))
     */
    fun Context.Z3_update_term(
        a: Ast,
        num_args: Int,
        args: Array<Ast>
    ): Ast

    /**
     * \brief Substitute every occurrence of \ccode{from[i]} in \c a with \ccode{to[i]}, for \c i smaller than \c num_exprs.
     * The result is the new AST. The arrays \c from and \c to must have size \c num_exprs.
     * For every \c i smaller than \c num_exprs, we must have that sort of \ccode{from[i]} must be equal to sort of \ccode{to[i]}.
     *
     * def_API('Z3_substitute', AST, (_in(CONTEXT), _in(AST), _in(UINT), _in_array(2, AST), _in_array(2, AST)))
     */
    fun Context.Z3_substitute(
        a: Ast,
        num_exprs: Int,
        from: Array<Ast>,
        to: Array<Ast>
    ): Ast

    /**
     * \brief Substitute the free variables in \c a with the expressions in \c to.
     * For every \c i smaller than \c num_exprs, the variable with de-Bruijn index \c i is replaced with term \ccode{to[i]}.
     *
     * def_API('Z3_substitute_vars', AST, (_in(CONTEXT), _in(AST), _in(UINT), _in_array(2, AST)))
     */
    fun Context.Z3_substitute_vars(
        a: Ast,
        num_exprs: Int,
        to: Array<Ast>
    ): Ast

    /**
     * \brief Translate/Copy the AST \c a from context \c source to context \c target.
     * AST \c a must have been created using context \c source.
     * \pre source != target
     *
     * def_API('Z3_translate', AST, (_in(CONTEXT), _in(AST), _in(CONTEXT)))
     */
    fun Z3_translate(source: Context, a: Ast, target: Context): Ast
    /*@}*/

    /** @name Models */
    /*@{*/

    /*@}*/
    /** @name Models
     */
    /*@{*/
    /**
     * \brief Create a fresh model object. It has reference count 0.
     *
     * def_API('Z3_mk_model', MODEL, (_in(CONTEXT),))
     */
    fun Context.Z3_mk_model(): Model

    /**
     * \brief Increment the reference counter of the given model.
     *
     * def_API('Z3_model_inc_ref', VOID, (_in(CONTEXT), _in(MODEL)))
     */
    fun Context.Z3_model_inc_ref(m: Model)

    /**
     * \brief Decrement the reference counter of the given model.
     *
     * def_API('Z3_model_dec_ref', VOID, (_in(CONTEXT), _in(MODEL)))
     */
    fun Context.Z3_model_dec_ref(m: Model)

    /**
     * \brief Evaluate the AST node \c t in the given model.
     * Return \c true if succeeded, and store the result in \c v.
     *
     * If \c model_completion is \c true, then Z3 will assign an interpretation for any constant or function that does
     * not have an interpretation in \c m. These constants and functions were essentially don't cares.
     *
     * If \c model_completion is \c false, then Z3 will not assign interpretations to constants for functions that do
     * not have interpretations in \c m. Evaluation behaves as the identify function in this case.
     *
     * The evaluation may fail for the following reasons:
     *
     * - \c t contains a quantifier.
     *
     * - the model \c m is partial, that is, it doesn't have a complete interpretation for uninterpreted functions.
     * That is, the option \ccode{MODEL_PARTIAL=true} was used.
     *
     * - \c t is type incorrect.
     *
     * - \c Z3_interrupt was invoked during evaluation.
     *
     * def_API('Z3_model_eval', boolean, (_in(CONTEXT), _in(MODEL), _in(AST), _in(boolean), _out(AST)))
     */
    fun Context.Z3_model_eval(
        m: Model,
        t: Ast,
        model_completion: Boolean,
        v: Out<Ast>
    ): Boolean

    /**
     * \brief Return the interpretation (i.e., assignment) of constant \c a in the model \c m.
     * Return \c NULL, if the model does not assign an interpretation for \c a.
     * That should be interpreted as: the value of \c a does not matter.
     *
     * \pre Z3_get_arity(c, a) == 0
     *
     * def_API('Z3_model_get_const_interp', AST, (_in(CONTEXT), _in(MODEL), _in(FUNC_DECL)))
     */
    fun Context.Z3_model_get_const_interp(
        m: Model,
        a: FuncDecl
    ): Ast

    /**
     * \brief Test if there exists an interpretation (i.e., assignment) for \c a in the model \c m.
     *
     * def_API('Z3_model_has_interp', boolean, (_in(CONTEXT), _in(MODEL), _in(FUNC_DECL)))
     */
    fun Context.Z3_model_has_interp(
        m: Model,
        a: FuncDecl
    ): Boolean

    /**
     * \brief Return the interpretation of the function \c f in the model \c m.
     * Return \c NULL, if the model does not assign an interpretation for \c f.
     * That should be interpreted as: the \c f does not matter.
     *
     * \pre Z3_get_arity(c, f) > 0
     *
     * \remark Reference counting must be used to manage Z3_func_interp objects, even when the Z3Context was
     * created using #Z3_mk_context instead of #Z3_mk_context_rc.
     *
     * def_API('Z3_model_get_func_interp', FUNC_INTERP, (_in(CONTEXT), _in(MODEL), _in(FUNC_DECL)))
     */
    fun Context.Z3_model_get_func_interp(
        m: Model,
        f: FuncDecl
    ): FuncInterp

    /**
     * \brief Return the number of constants assigned by the given model.
     *
     * \sa Z3_model_get_const_decl
     *
     * def_API('Z3_model_get_num_consts', UINT, (_in(CONTEXT), _in(MODEL)))
     */
    fun Context.Z3_model_get_num_consts(m: Model): Int

    /**
     * \brief Return the i-th constant in the given model.
     *
     * \pre i < Z3_model_get_num_consts(c, m)
     *
     * \sa Z3_model_eval
     *
     * def_API('Z3_model_get_const_decl', FUNC_DECL, (_in(CONTEXT), _in(MODEL), _in(UINT)))
     */
    fun Context.Z3_model_get_const_decl(
        m: Model,
        i: Int
    ): FuncDecl

    /**
     * \brief Return the number of function interpretations in the given model.
     *
     * A function interpretation is represented as a finite map and an 'else' value.
     * Each entry in the finite map represents the value of a function given a set of arguments.
     *
     * def_API('Z3_model_get_num_funcs', UINT, (_in(CONTEXT), _in(MODEL)))
     */
    fun Context.Z3_model_get_num_funcs(m: Model): Int

    /**
     * \brief Return the declaration of the i-th function in the given model.
     *
     * \pre i < Z3_model_get_num_funcs(c, m)
     *
     * \sa Z3_model_get_num_funcs
     *
     * def_API('Z3_model_get_func_decl', FUNC_DECL, (_in(CONTEXT), _in(MODEL), _in(UINT)))
     */
    fun Context.Z3_model_get_func_decl(
        m: Model,
        i: Int
    ): FuncDecl

    /**
     * \brief Return the number of uninterpreted sorts that \c m assigns an interpretation to.
     *
     * Z3 also provides an interpretation for uninterpreted sorts used in a formula.
     * The interpretation for a sort \c s is a finite set of distinct values. We say this finite set is
     * the "universe" of \c s.
     *
     * \sa Z3_model_get_sort
     * \sa Z3_model_get_sort_universe
     *
     * def_API('Z3_model_get_num_sorts', UINT, (_in(CONTEXT), _in(MODEL)))
     */
    fun Context.Z3_model_get_num_sorts(m: Model): Int

    /**
     * \brief Return a uninterpreted sort that \c m assigns an interpretation.
     *
     * \pre i < Z3_model_get_num_sorts(c, m)
     *
     * \sa Z3_model_get_num_sorts
     * \sa Z3_model_get_sort_universe
     *
     * def_API('Z3_model_get_sort', SORT, (_in(CONTEXT), _in(MODEL), _in(UINT)))
     */
    fun Context.Z3_model_get_sort(m: Model, i: Int): Sort

    /**
     * \brief Return the finite set of distinct values that represent the interpretation for sort \c s.
     *
     * \sa Z3_model_get_num_sorts
     * \sa Z3_model_get_sort
     *
     * def_API('Z3_model_get_sort_universe', AST_VECTOR, (_in(CONTEXT), _in(MODEL), _in(SORT)))
     */
    fun Context.Z3_model_get_sort_universe(
        m: Model,
        s: Sort
    ): AstVector

    /**
     * \brief translate model from context \c c to context \c dst.
     *
     * def_API('Z3_model_translate', MODEL, (_in(CONTEXT), _in(MODEL), _in(CONTEXT)))
     */
    fun Context.Z3_model_translate(
        m: Model,
        dst: Context
    ): Model

    /**
     * \brief The \ccode{(_ as-array f)} AST node is a construct for assigning interpretations for arrays in Z3.
     * It is the array such that forall indices \c i we have that \ccode{(select (_ as-array f) i)} is equal to \ccode{(f i)}.
     * This procedure returns \c true if the \c a is an \c as-array AST node.
     *
     * Z3 current solvers have minimal support for \c as_array nodes.
     *
     * \sa Z3_get_as_array_func_decl
     *
     * def_API('Z3_is_as_array', boolean, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_is_as_array(a: Ast): Boolean

    /**
     * \brief Return the function declaration \c f associated with a \ccode{(_ as_array f)} node.
     *
     * \sa Z3_is_as_array
     *
     * def_API('Z3_get_as_array_func_decl', FUNC_DECL, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_get_as_array_func_decl(a: Ast): FuncDecl

    /**
     * \brief Create a fresh func_interp object, add it to a model for a specified function.
     * It has reference count 0.
     *
     * \param c context
     * \param m model
     * \param f function declaration
     * \param default_value default value for function interpretation
     *
     * def_API('Z3_add_func_interp', FUNC_INTERP, (_in(CONTEXT), _in(MODEL), _in(FUNC_DECL), _in(AST)))
     */
    fun Context.Z3_add_func_interp(
        m: Model,
        f: FuncDecl,
        default_value: Ast
    ): FuncInterp

    /**
     * \brief Add a constant interpretation.
     *
     * def_API('Z3_add_const_interp', VOID, (_in(CONTEXT), _in(MODEL), _in(FUNC_DECL), _in(AST)))
     */
    fun Context.Z3_add_const_interp(
        m: Model,
        f: FuncDecl,
        a: Ast
    )

    /**
     * \brief Increment the reference counter of the given Z3_func_interp object.
     *
     * def_API('Z3_func_interp_inc_ref', VOID, (_in(CONTEXT), _in(FUNC_INTERP)))
     */
    fun Context.Z3_func_interp_inc_ref(f: FuncInterp)

    /**
     * \brief Decrement the reference counter of the given Z3_func_interp object.
     *
     * def_API('Z3_func_interp_dec_ref', VOID, (_in(CONTEXT), _in(FUNC_INTERP)))
     */
    fun Context.Z3_func_interp_dec_ref(f: FuncInterp)

    /**
     * \brief Return the number of entries in the given function interpretation.
     *
     * A function interpretation is represented as a finite map and an 'else' value.
     * Each entry in the finite map represents the value of a function given a set of arguments.
     * This procedure return the number of element in the finite map of \c f.
     *
     * def_API('Z3_func_interp_get_num_entries', UINT, (_in(CONTEXT), _in(FUNC_INTERP)))
     */
    fun Context.Z3_func_interp_get_num_entries(f: FuncInterp): Int

    /**
     * \brief Return a "point" of the given function interpretation. It represents the
     * value of \c f in a particular point.
     *
     * \pre i < Z3_func_interp_get_num_entries(c, f)
     *
     * \sa Z3_func_interp_get_num_entries
     *
     * def_API('Z3_func_interp_get_entry', FUNC_ENTRY, (_in(CONTEXT), _in(FUNC_INTERP), _in(UINT)))
     */
    fun Context.Z3_func_interp_get_entry(
        f: FuncInterp,
        i: Int
    ): FuncEntry

    /**
     * \brief Return the 'else' value of the given function interpretation.
     *
     * A function interpretation is represented as a finite map and an 'else' value.
     * This procedure returns the 'else' value.
     *
     * def_API('Z3_func_interp_get_else', AST, (_in(CONTEXT), _in(FUNC_INTERP)))
     */
    fun Context.Z3_func_interp_get_else(f: FuncInterp): Ast

    /**
     * \brief Return the 'else' value of the given function interpretation.
     *
     * A function interpretation is represented as a finite map and an 'else' value.
     * This procedure can be used to update the 'else' value.
     *
     * def_API('Z3_func_interp_set_else', VOID, (_in(CONTEXT), _in(FUNC_INTERP), _in(AST)))
     */
    fun Context.Z3_func_interp_set_else(
        f: FuncInterp,
        else_value: Ast
    )

    /**
     * \brief Return the arity (number of arguments) of the given function interpretation.
     *
     * def_API('Z3_func_interp_get_arity', UINT, (_in(CONTEXT), _in(FUNC_INTERP)))
     */
    fun Context.Z3_func_interp_get_arity(f: FuncInterp): Int

    /**
     * \brief add a function entry to a function interpretation.
     *
     * \param c logical context
     * \param fi a function interpretation to be updated.
     * \param args list of arguments. They should be constant values (such as integers) and be of the same types as the domain of the function.
     * \param value value of the function when the parameters match args.
     *
     * It is assumed that entries added to a function cover disjoint arguments.
     * If an two entries are added with the same arguments, only the second insertion survives and the
     * first inserted entry is removed.
     *
     * def_API('Z3_func_interp_add_entry', VOID, (_in(CONTEXT), _in(FUNC_INTERP), _in(AST_VECTOR), _in(AST)))
     */
    fun Context.Z3_func_interp_add_entry(
        fi: FuncInterp,
        args: AstVector,
        value: Ast
    )

    /**
     * \brief Increment the reference counter of the given \c Z3_func_entry object.
     *
     * def_API('Z3_func_entry_inc_ref', VOID, (_in(CONTEXT), _in(FUNC_ENTRY)))
     */
    fun Context.Z3_func_entry_inc_ref(e: FuncEntry)

    /**
     * \brief Decrement the reference counter of the given \c Z3_func_entry object.
     *
     * def_API('Z3_func_entry_dec_ref', VOID, (_in(CONTEXT), _in(FUNC_ENTRY)))
     */
    fun Context.Z3_func_entry_dec_ref(e: FuncEntry)

    /**
     * \brief Return the value of this point.
     *
     * A \c Z3_func_entry object represents an element in the finite map used to encode
     * a function interpretation.
     *
     * \sa Z3_func_interp_get_entry
     *
     * def_API('Z3_func_entry_get_value', AST, (_in(CONTEXT), _in(FUNC_ENTRY)))
     */
    fun Context.Z3_func_entry_get_value(e: FuncEntry): Ast

    /**
     * \brief Return the number of arguments in a \c Z3_func_entry object.
     *
     * \sa Z3_func_interp_get_entry
     *
     * def_API('Z3_func_entry_get_num_args', UINT, (_in(CONTEXT), _in(FUNC_ENTRY)))
     */
    fun Context.Z3_func_entry_get_num_args(e: FuncEntry): Int

    /**
     * \brief Return an argument of a \c Z3_func_entry object.
     *
     * \pre i < Z3_func_entry_get_num_args(c, e)
     *
     * \sa Z3_func_interp_get_entry
     *
     * def_API('Z3_func_entry_get_arg', AST, (_in(CONTEXT), _in(FUNC_ENTRY), _in(UINT)))
     */
    fun Context.Z3_func_entry_get_arg(
        e: FuncEntry,
        i: Int
    ): Ast
    /*@}*/

    /** @name Interaction logging */
    /*@{*/
    /*@}*/
    /** @name Interaction logging
     */
    /*@{*/
    /**
     * \brief Log interaction to a file.
     *
     * extra_API('Z3_open_log', INT, (_in(STRING),))
     */
    fun Z3_open_log(filename: String): Boolean

    /**
     * \brief Append user-defined string to interaction log.
     *
     * The interaction log is opened using Z3_open_log.
     * It contains the formulas that are checked using Z3.
     * You can use this command to append comments, for instance.
     *
     * extra_API('Z3_append_log', VOID, (_in(STRING),))
     */
    fun Z3_append_log(string: String)

    /**
     * \brief Close interaction log.
     *
     * extra_API('Z3_close_log', VOID, ())
     */
    fun Z3_close_log()

    /**
     * \brief Enable/disable printing warning messages to the console.
     *
     * Warnings are printed after passing \c true, warning messages are
     * suppressed after calling this method with \c false.
     *
     * def_API('Z3_toggle_warning_messages', VOID, (_in(boolean),))
     */
    fun Z3_toggle_warning_messages(enabled: Boolean)
    /*@}*/

    /** @name String conversion */
    /*@{*/
    /*@}*/
    /** @name String conversion
     */
    /*@{*/
    /**
     * \brief Select mode for the format used for pretty-printing AST nodes.
     *
     * The default mode for pretty printing AST nodes is to produce
     * SMT-LIB style output where common subexpressions are printed
     * at each occurrence. The mode is called \c Z3_PRINT_SMTLIB_FULL.
     * To print shared common subexpressions only once,
     * use the \c Z3_PRINT_LOW_LEVEL mode.
     * To print in way that conforms to SMT-LIB standards and uses let
     * expressions to share common sub-expressions use \c Z3_PRINT_SMTLIB2_COMPLIANT.
     *
     * \sa Z3_ast_to_string
     * \sa Z3_pattern_to_string
     * \sa Z3_func_decl_to_string
     *
     * def_API('Z3_set_ast_print_mode', VOID, (_in(CONTEXT), _in(PRINT_MODE)))
     */
    fun Context.Z3_set_ast_print_mode(mode: Int)

    /**
     * \brief Convert the given AST node into a string.
     *
     * \warning The result buffer is statically allocated by Z3. It will
     * be automatically deallocated when #Z3_del_context is invoked.
     * So, the buffer is invalidated in the next call to \c Z3_ast_to_string.
     *
     * \sa Z3_pattern_to_string
     * \sa Z3_sort_to_string
     *
     * def_API('Z3_ast_to_string', STRING, (_in(CONTEXT), _in(AST)))
     */
    fun Context.Z3_ast_to_string(a: Ast): String

    /**
     * def_API('Z3_pattern_to_string', STRING, (_in(CONTEXT), _in(PATTERN)))
     */
    fun Context.Z3_pattern_to_string(p: Pattern): String

    /**
     * def_API('Z3_sort_to_string', STRING, (_in(CONTEXT), _in(SORT)))
     */
    fun Context.Z3_sort_to_string(s: Sort): String

    /**
     * def_API('Z3_func_decl_to_string', STRING, (_in(CONTEXT), _in(FUNC_DECL)))
     */
    fun Context.Z3_func_decl_to_string(d: FuncDecl): String

    /**
     * \brief Convert the given model into a string.
     *
     * \warning The result buffer is statically allocated by Z3. It will
     * be automatically deallocated when #Z3_del_context is invoked.
     * So, the buffer is invalidated in the next call to \c Z3_model_to_string.
     *
     * def_API('Z3_model_to_string', STRING, (_in(CONTEXT), _in(MODEL)))
     */
    fun Context.Z3_model_to_string(m: Model): String

    /**
     * \brief Convert the given benchmark into SMT-LIB formatted string.
     *
     * \warning The result buffer is statically allocated by Z3. It will
     * be automatically deallocated when #Z3_del_context is invoked.
     * So, the buffer is invalidated in the next call to \c Z3_benchmark_to_smtlib_string.
     *
     * \param c - context.
     * \param name - name of benchmark. The argument is optional.
     * \param logic - the benchmark logic.
     * \param status - the status string (sat, unsat, or unknown)
     * \param attributes - other attributes, such as source, difficulty or category.
     * \param num_assumptions - number of assumptions.
     * \param assumptions - auxiliary assumptions.
     * \param formula - formula to be checked for consistency in conjunction with assumptions.
     *
     * def_API('Z3_benchmark_to_smtlib_string', STRING, (_in(CONTEXT), _in(STRING), _in(STRING), _in(STRING), _in(STRING), _in(UINT), _in_array(5, AST), _in(AST)))
     */
    fun Context.Z3_benchmark_to_smtlib_string(
        name: String,
        logic: String,
        status: String,
        attributes: String,
        num_assumptions: Int,
        assumptions: Array<Ast>?,
        formula: Ast
    ): String

    /*@}*/

    /** @name Parser interface */
    /*@{*/
    /*@}*/
    /** @name Parser interface
     */
    /*@{*/
    /**
     * \brief Parse the given string using the SMT-LIB2 parser.
     *
     * It returns a formula comprising of the conjunction of assertions in the scope
     * (up to push/pop) at the end of the string.
     *
     * def_API('Z3_parse_smtlib2_string', AST_VECTOR, (_in(CONTEXT), _in(STRING), _in(UINT), _in_array(2, SYMBOL), _in_array(2, SORT), _in(UINT), _in_array(5, SYMBOL), _in_array(5, FUNC_DECL)))
     */
    fun Context.Z3_parse_smtlib2_string(
        str: String,
        num_sorts: Int,
        sort_names: Array<Symbol>,
        sorts: Array<Sort>,
        num_decls: Int,
        decl_names: Array<Symbol>,
        decls: Array<FuncDecl>
    ): AstVector

    /**
     * \brief Similar to #Z3_parse_smtlib2_string, but reads the benchmark from a file.
     *
     * def_API('Z3_parse_smtlib2_file', AST_VECTOR, (_in(CONTEXT), _in(STRING), _in(UINT), _in_array(2, SYMBOL), _in_array(2, SORT), _in(UINT), _in_array(5, SYMBOL), _in_array(5, FUNC_DECL)))
     */
    fun Context.Z3_parse_smtlib2_file(
        file_name: String,
        num_sorts: Int,
        sort_names: Array<Symbol>,
        sorts: Array<Sort>,
        num_decls: Int,
        decl_names: Array<Symbol>,
        decls: Array<FuncDecl>
    ): AstVector


    /**
     * \brief Parse and evaluate and SMT-LIB2 command sequence. The state from a previous call is saved so the next
     * evaluation builds on top of the previous call.
     *
     * \returns output generated from processing commands.
     *
     * def_API('Z3_eval_smtlib2_string', STRING, (_in(CONTEXT), _in(STRING),))
     */
    fun Context.Z3_eval_smtlib2_string(str: String): String

    /*@}*/

    /** @name Error Handling */
    /*@{*/
    /*@}*/
    /** @name Error Handling
     */
    /*@{*/
    /**
     * \brief Return the error code for the last API call.
     *
     * A call to a Z3 function may return a non Z3_OK error code,
     * when it is not used correctly.
     *
     * \sa Z3_set_error_handler
     *
     * def_API('Z3_get_error_code', UINT, (_in(CONTEXT), ))
     */
    fun Context.Z3_get_error_code(): Int

    interface Z3ErrorHandler : Callback {
        operator fun invoke(ctx: Context, error: Int)
    }

    /**
     * \brief Register a Z3 error handler.
     *
     * A call to a Z3 function may return a non \c Z3_OK error code, when
     * it is not used correctly.  An error handler can be registered
     * and will be called in this case.  To disable the use of the
     * error handler, simply register with \c h=NULL.
     *
     * \warning Log files, created using #Z3_open_log, may be potentially incomplete/incorrect if error handlers are used.
     *
     * \sa Z3_get_error_code
     */
    fun Context.Z3_set_error_handler(h: Z3ErrorHandler)

    /**
     * \brief Set an error.
     *
     * def_API('Z3_set_error', VOID, (_in(CONTEXT), _in(ERROR_CODE)))
     */
    fun Context.Z3_set_error(e: Int)

    /**
     * \brief Return a string describing the given error code.
     *
     * def_API('Z3_get_error_msg', STRING, (_in(CONTEXT), _in(ERROR_CODE)))
     */
    fun Context.Z3_get_error_msg(err: Int): String

    /*@}*/

    /** @name Miscellaneous */
    /*@{*/

    /*@}*/
    /** @name Miscellaneous
     */
    /*@{*/
    /**
     * \brief Return Z3 version number information.
     *
     * \sa Z3_get_full_version
     *
     * def_API('Z3_get_version', VOID, (_out(UINT), _out(UINT), _out(UINT), _out(UINT)))
     */
    fun Z3_get_version(
        major: IntByReference,
        minor: IntByReference,
        build_number: IntByReference,
        revision_number: IntByReference
    )

    /**
     * \brief Return a string that fully describes the version of Z3 in use.
     *
     * \sa Z3_get_version
     *
     * def_API('Z3_get_full_version', STRING, ())
     */
    fun Z3_get_full_version(): String

    /**
     * \brief Enable tracing messages tagged as \c tag when Z3 is compiled in debug mode.
     * It is a NOOP otherwise
     *
     * \sa Z3_disable_trace
     *
     * def_API('Z3_enable_trace', VOID, (_in(STRING),))
     */
    fun Z3_enable_trace(tag: String)

    /**
     * \brief Disable tracing messages tagged as \c tag when Z3 is compiled in debug mode.
     * It is a NOOP otherwise
     *
     * \sa Z3_enable_trace
     *
     * def_API('Z3_disable_trace', VOID, (_in(STRING),))
     */
    fun Z3_disable_trace(tag: String)

    /**
     * \brief Reset all allocated resources.
     *
     * Use this facility on out-of memory errors.
     * It allows discharging the previous state and resuming afresh.
     * Any pointers previously returned by the API
     * become invalid.
     *
     * def_API('Z3_reset_memory', VOID, ())
     */
    fun Z3_reset_memory()

    /**
     * \brief Destroy all allocated resources.
     *
     * Any pointers previously returned by the API become invalid.
     * Can be used for memory leak detection.
     *
     * def_API('Z3_finalize_memory', VOID, ())
     */
    fun Z3_finalize_memory()
    /*@}*/

    /** @name Goals */
    /*@{*/
    /*@}*/
    /** @name Goals
     */
    /*@{*/
    /**
     * \brief Create a goal (aka problem). A goal is essentially a set
     * of formulas, that can be solved and/or transformed using
     * tactics and solvers.
     *
     * If \c models is \c true, then model generation is enabled for the new goal.
     *
     * If \c unsat_cores is \c true, then unsat core generation is enabled for the new goal.
     *
     * If \c proofs is \c true, then proof generation is enabled for the new goal. Remark, the
     * Z3 context \c c must have been created with proof generation support.
     *
     * \remark Reference counting must be used to manage goals, even when the \c Z3Context was
     * created using #Z3_mk_context instead of #Z3_mk_context_rc.
     *
     * def_API('Z3_mk_goal', GOAL, (_in(CONTEXT), _in(boolean), _in(boolean), _in(boolean)))
     */
    fun Context.Z3_mk_goal(
        models: Boolean,
        unsat_cores: Boolean,
        proofs: Boolean
    ): Goal

    /**
     * \brief Increment the reference counter of the given goal.
     *
     * def_API('Z3_goal_inc_ref', VOID, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_inc_ref(g: Goal)

    /**
     * \brief Decrement the reference counter of the given goal.
     *
     * def_API('Z3_goal_dec_ref', VOID, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_dec_ref(g: Goal)

    /**
     * \brief Return the "precision" of the given goal. Goals can be transformed using over and under approximations.
     * A under approximation is applied when the objective is to find a model for a given goal.
     * An over approximation is applied when the objective is to find a proof for a given goal.
     *
     * def_API('Z3_goal_precision', UINT, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_precision(g: Goal): Int

    /**
     * \brief Add a new formula \c a to the given goal.
     * The formula is split according to the following procedure that is applied
     * until a fixed-point:
     * Conjunctions are split into separate formulas.
     * Negations are distributed over disjunctions, resulting in separate formulas.
     * If the goal is \c false, adding new formulas is a no-op.
     * If the formula \c a is \c true, then nothing is added.
     * If the formula \c a is \c false, then the entire goal is replaced by the formula \c false.
     *
     * def_API('Z3_goal_assert', VOID, (_in(CONTEXT), _in(GOAL), _in(AST)))
     */
    fun Context.Z3_goal_assert(g: Goal, a: Ast)

    /**
     * \brief Return \c true if the given goal contains the formula \c false.
     *
     * def_API('Z3_goal_inconsistent', boolean, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_inconsistent(g: Goal): Boolean

    /**
     * \brief Return the depth of the given goal. It tracks how many transformations were applied to it.
     *
     * def_API('Z3_goal_depth', UINT, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_depth(g: Goal): Int

    /**
     * \brief Erase all formulas from the given goal.
     *
     * def_API('Z3_goal_reset', VOID, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_reset(g: Goal)

    /**
     * \brief Return the number of formulas in the given goal.
     *
     * def_API('Z3_goal_size', UINT, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_size(g: Goal): Int

    /**
     * \brief Return a formula from the given goal.
     *
     * \pre idx < Z3_goal_size(c, g)
     *
     * def_API('Z3_goal_formula', AST, (_in(CONTEXT), _in(GOAL), _in(UINT)))
     */
    fun Context.Z3_goal_formula(g: Goal, idx: Int): Ast

    /**
     * \brief Return the number of formulas, subformulas and terms in the given goal.
     *
     * def_API('Z3_goal_num_exprs', UINT, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_num_exprs(g: Goal): Int

    /**
     * \brief Return \c true if the goal is empty, and it is precise or the product of a under approximation.
     *
     * def_API('Z3_goal_is_decided_sat', boolean, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_is_decided_sat(g: Goal): Boolean

    /**
     * \brief Return \c true if the goal contains false, and it is precise or the product of an over approximation.
     *
     * def_API('Z3_goal_is_decided_unsat', boolean, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_is_decided_unsat(g: Goal): Boolean

    /**
     * \brief Copy a goal \c g from the context \c source to the context \c target.
     *
     * def_API('Z3_goal_translate', GOAL, (_in(CONTEXT), _in(GOAL), _in(CONTEXT)))
     */
    fun Z3_goal_translate(source: Context, g: Goal, target: Context): Goal

    /**
     * \brief Convert a model of the formulas of a goal to a model of an original goal.
     * The model may be null, in which case the returned model is valid if the goal was
     * established satisfiable.
     *
     * def_API('Z3_goal_convert_model', MODEL, (_in(CONTEXT), _in(GOAL), _in(MODEL)))
     */
    fun Context.Z3_goal_convert_model(
        g: Goal,
        m: Model
    ): Model

    /**
     * \brief Convert a goal into a string.
     *
     * def_API('Z3_goal_to_string', STRING, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_to_string(g: Goal): String

    /**
     * \brief Convert a goal into a DIMACS formatted string.
     * The goal must be in CNF. You can convert a goal to CNF
     * by applying the tseitin-cnf tactic. Bit-vectors are not automatically
     * converted to booleans either, so if the caller intends to
     * preserve satisfiability, it should apply bit-blasting tactics.
     * Quantifiers and theory atoms will not be encoded.
     *
     * def_API('Z3_goal_to_dimacs_string', STRING, (_in(CONTEXT), _in(GOAL)))
     */
    fun Context.Z3_goal_to_dimacs_string(g: Goal): String

    /*@}*/

    /** @name Tactics and Probes */
    /*@{*/
    /*@}*/
    /** @name Tactics and Probes
     */
    /*@{*/
    /**
     * \brief Return a tactic associated with the given name.
     * The complete list of tactics may be obtained using the procedures #Z3_get_num_tactics and #Z3_get_tactic_name.
     * It may also be obtained using the command \ccode{(help-tactic)} in the SMT 2.0 front-end.
     *
     * Tactics are the basic building block for creating custom solvers for specific problem domains.
     *
     * def_API('Z3_mk_tactic', TACTIC, (_in(CONTEXT), _in(STRING)))
     */
    fun Context.Z3_mk_tactic(name: String): Tactic

    /**
     * \brief Increment the reference counter of the given tactic.
     *
     * def_API('Z3_tactic_inc_ref', VOID, (_in(CONTEXT), _in(TACTIC)))
     */
    fun Context.Z3_tactic_inc_ref(t: Tactic)

    /**
     * \brief Decrement the reference counter of the given tactic.
     *
     * def_API('Z3_tactic_dec_ref', VOID, (_in(CONTEXT), _in(TACTIC)))
     */
    fun Context.Z3_tactic_dec_ref(g: Tactic)

    /**
     * \brief Return a probe associated with the given name.
     * The complete list of probes may be obtained using the procedures #Z3_get_num_probes and #Z3_get_probe_name.
     * It may also be obtained using the command \ccode{(help-tactic)} in the SMT 2.0 front-end.
     *
     * Probes are used to inspect a goal (aka problem) and collect information that may be used to decide
     * which solver and/or preprocessing step will be used.
     *
     * def_API('Z3_mk_probe', PROBE, (_in(CONTEXT), _in(STRING)))
     */
    fun Context.Z3_mk_probe(name: String): Probe

    /**
     * \brief Increment the reference counter of the given probe.
     *
     * def_API('Z3_probe_inc_ref', VOID, (_in(CONTEXT), _in(PROBE)))
     */
    fun Context.Z3_probe_inc_ref(p: Probe)

    /**
     * \brief Decrement the reference counter of the given probe.
     *
     * def_API('Z3_probe_dec_ref', VOID, (_in(CONTEXT), _in(PROBE)))
     */
    fun Context.Z3_probe_dec_ref(p: Probe)

    /**
     * \brief Return a tactic that applies \c t1 to a given goal and \c t2
     * to every subgoal produced by \c t1.
     *
     * def_API('Z3_tactic_and_then', TACTIC, (_in(CONTEXT), _in(TACTIC), _in(TACTIC)))
     */
    fun Context.Z3_tactic_and_then(
        t1: Tactic,
        t2: Tactic
    ): Tactic

    /**
     * \brief Return a tactic that first applies \c t1 to a given goal,
     * if it fails then returns the result of \c t2 applied to the given goal.
     *
     * def_API('Z3_tactic_or_else', TACTIC, (_in(CONTEXT), _in(TACTIC), _in(TACTIC)))
     */
    fun Context.Z3_tactic_or_else(
        t1: Tactic,
        t2: Tactic
    ): Tactic

    /**
     * \brief Return a tactic that applies the given tactics in parallel.
     *
     * def_API('Z3_tactic_par_or', TACTIC, (_in(CONTEXT), _in(UINT), _in_array(1, TACTIC)))
     */
    fun Context.Z3_tactic_par_or(num: Int, ts: Array<Tactic>?): Tactic

    /**
     * \brief Return a tactic that applies \c t1 to a given goal and then \c t2
     * to every subgoal produced by \c t1. The subgoals are processed in parallel.
     *
     * def_API('Z3_tactic_par_and_then', TACTIC, (_in(CONTEXT), _in(TACTIC), _in(TACTIC)))
     */
    fun Context.Z3_tactic_par_and_then(
        t1: Tactic,
        t2: Tactic
    ): Tactic

    /**
     * \brief Return a tactic that applies \c t to a given goal for \c ms milliseconds.
     * If \c t does not terminate in \c ms milliseconds, then it fails.
     *
     * def_API('Z3_tactic_try_for', TACTIC, (_in(CONTEXT), _in(TACTIC), _in(UINT)))
     */
    fun Context.Z3_tactic_try_for(t: Tactic, ms: Int): Tactic

    /**
     * \brief Return a tactic that applies \c t to a given goal is the probe \c p evaluates to true.
     * If \c p evaluates to false, then the new tactic behaves like the skip tactic.
     *
     * def_API('Z3_tactic_when', TACTIC, (_in(CONTEXT), _in(PROBE), _in(TACTIC)))
     */
    fun Context.Z3_tactic_when(p: Probe, t: Tactic): Tactic

    /**
     * \brief Return a tactic that applies \c t1 to a given goal if the probe \c p evaluates to true,
     * and \c t2 if \c p evaluates to false.
     *
     * def_API('Z3_tactic_cond', TACTIC, (_in(CONTEXT), _in(PROBE), _in(TACTIC), _in(TACTIC)))
     */
    fun Context.Z3_tactic_cond(
        p: Probe,
        t1: Tactic,
        t2: Tactic
    ): Tactic

    /**
     * \brief Return a tactic that keeps applying \c t until the goal is not modified anymore or the maximum
     * number of iterations \c max is reached.
     *
     * def_API('Z3_tactic_repeat', TACTIC, (_in(CONTEXT), _in(TACTIC), _in(UINT)))
     */
    fun Context.Z3_tactic_repeat(t: Tactic, max: Int): Tactic

    /**
     * \brief Return a tactic that just return the given goal.
     *
     * def_API('Z3_tactic_skip', TACTIC, (_in(CONTEXT),))
     */
    fun Context.Z3_tactic_skip(): Tactic

    /**
     * \brief Return a tactic that always fails.
     *
     * def_API('Z3_tactic_fail', TACTIC, (_in(CONTEXT),))
     */
    fun Context.Z3_tactic_fail(): Tactic

    /**
     * \brief Return a tactic that fails if the probe \c p evaluates to false.
     *
     * def_API('Z3_tactic_fail_if', TACTIC, (_in(CONTEXT), _in(PROBE)))
     */
    fun Context.Z3_tactic_fail_if(p: Probe): Tactic

    /**
     * \brief Return a tactic that fails if the goal is not trivially satisfiable (i.e., empty) or
     * trivially unsatisfiable (i.e., contains false).
     *
     * def_API('Z3_tactic_fail_if_not_decided', TACTIC, (_in(CONTEXT),))
     */
    fun Context.Z3_tactic_fail_if_not_decided(): Tactic

    /**
     * \brief Return a tactic that applies \c t using the given set of parameters.
     *
     * def_API('Z3_tactic_using_params', TACTIC, (_in(CONTEXT), _in(TACTIC), _in(PARAMS)))
     */
    fun Context.Z3_tactic_using_params(
        t: Tactic,
        p: Params
    ): Tactic

    /**
     * \brief Return a probe that always evaluates to val.
     *
     * def_API('Z3_probe_const', PROBE, (_in(CONTEXT), _in(DOUBLE)))
     */
    fun Context.Z3_probe_const(`val`: Double): Probe

    /**
     * \brief Return a probe that evaluates to "true" when the value returned by \c p1 is less than the value returned by \c p2.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_lt', PROBE, (_in(CONTEXT), _in(PROBE), _in(PROBE)))
     */
    fun Context.Z3_probe_lt(p1: Probe, p2: Probe): Probe

    /**
     * \brief Return a probe that evaluates to "true" when the value returned by \c p1 is greater than the value returned by \c p2.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_gt', PROBE, (_in(CONTEXT), _in(PROBE), _in(PROBE)))
     */
    fun Context.Z3_probe_gt(p1: Probe, p2: Probe): Probe

    /**
     * \brief Return a probe that evaluates to "true" when the value returned by \c p1 is less than or equal to the value returned by \c p2.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_le', PROBE, (_in(CONTEXT), _in(PROBE), _in(PROBE)))
     */
    fun Context.Z3_probe_le(p1: Probe, p2: Probe): Probe

    /**
     * \brief Return a probe that evaluates to "true" when the value returned by \c p1 is greater than or equal to the value returned by \c p2.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_ge', PROBE, (_in(CONTEXT), _in(PROBE), _in(PROBE)))
     */
    fun Context.Z3_probe_ge(p1: Probe, p2: Probe): Probe

    /**
     * \brief Return a probe that evaluates to "true" when the value returned by \c p1 is equal to the value returned by \c p2.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_eq', PROBE, (_in(CONTEXT), _in(PROBE), _in(PROBE)))
     */
    fun Context.Z3_probe_eq(p1: Probe, p2: Probe): Probe

    /**
     * \brief Return a probe that evaluates to "true" when \c p1 and \c p2 evaluates to true.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_and', PROBE, (_in(CONTEXT), _in(PROBE), _in(PROBE)))
     */
    fun Context.Z3_probe_and(p1: Probe, p2: Probe): Probe

    /**
     * \brief Return a probe that evaluates to "true" when \c p1 or \c p2 evaluates to true.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_or', PROBE, (_in(CONTEXT), _in(PROBE), _in(PROBE)))
     */
    fun Context.Z3_probe_or(p1: Probe, p2: Probe): Probe

    /**
     * \brief Return a probe that evaluates to "true" when \c p does not evaluate to true.
     *
     * \remark For probes, "true" is any value different from 0.0.
     *
     * def_API('Z3_probe_not', PROBE, (_in(CONTEXT), _in(PROBE)))
     */
    fun Context.Z3_probe_not(p: Probe): Probe

    /**
     * \brief Return the number of builtin tactics available in Z3.
     *
     * def_API('Z3_get_num_tactics', UINT, (_in(CONTEXT),))
     */
    fun Context.Z3_get_num_tactics(): Int

    /**
     * \brief Return the name of the idx tactic.
     *
     * \pre i < Z3_get_num_tactics(c)
     *
     * def_API('Z3_get_tactic_name', STRING, (_in(CONTEXT), _in(UINT)))
     */
    fun Context.Z3_get_tactic_name(i: Int): String

    /**
     * \brief Return the number of builtin probes available in Z3.
     *
     * def_API('Z3_get_num_probes', UINT, (_in(CONTEXT),))
     */
    fun Context.Z3_get_num_probes(): Int

    /**
     * \brief Return the name of the \c i probe.
     *
     * \pre i < Z3_get_num_probes(c)
     *
     * def_API('Z3_get_probe_name', STRING, (_in(CONTEXT), _in(UINT)))
     */
    fun Context.Z3_get_probe_name(i: Int): String

    /**
     * \brief Return a string containing a description of parameters accepted by the given tactic.
     *
     * def_API('Z3_tactic_get_help', STRING, (_in(CONTEXT), _in(TACTIC)))
     */
    fun Context.Z3_tactic_get_help(t: Tactic): String

    /**
     * \brief Return the parameter description set for the given tactic object.
     *
     * def_API('Z3_tactic_get_param_descrs', PARAM_DESCRS, (_in(CONTEXT), _in(TACTIC)))
     */
    fun Context.Z3_tactic_get_param_descrs(t: Tactic): ParamDescrs

    /**
     * \brief Return a string containing a description of the tactic with the given name.
     *
     * def_API('Z3_tactic_get_descr', STRING, (_in(CONTEXT), _in(STRING)))
     */
    fun Context.Z3_tactic_get_descr(name: String): String

    /**
     * \brief Return a string containing a description of the probe with the given name.
     *
     * def_API('Z3_probe_get_descr', STRING, (_in(CONTEXT), _in(STRING)))
     */
    fun Context.Z3_probe_get_descr(name: String): String

    /**
     * \brief Execute the probe over the goal. The probe always produce a double value.
     * "boolean" probes return 0.0 for false, and a value different from 0.0 for true.
     *
     * def_API('Z3_probe_apply', DOUBLE, (_in(CONTEXT), _in(PROBE), _in(GOAL)))
     */
    fun Context.Z3_probe_apply(p: Probe, g: Goal): Double

    /**
     * \brief Apply tactic \c t to the goal \c g.
     *
     * def_API('Z3_tactic_apply', APPLY_RESULT, (_in(CONTEXT), _in(TACTIC), _in(GOAL)))
     */
    fun Context.Z3_tactic_apply(t: Tactic, g: Goal): ApplyResult

    /**
     * \brief Apply tactic \c t to the goal \c g using the parameter set \c p.
     *
     * def_API('Z3_tactic_apply_ex', APPLY_RESULT, (_in(CONTEXT), _in(TACTIC), _in(GOAL), _in(PARAMS)))
     */
    fun Context.Z3_tactic_apply_ex(
        t: Tactic,
        g: Goal,
        p: Params
    ): ApplyResult

    /**
     * \brief Increment the reference counter of the given \c Z3_apply_result object.
     *
     * def_API('Z3_apply_result_inc_ref', VOID, (_in(CONTEXT), _in(APPLY_RESULT)))
     */
    fun Context.Z3_apply_result_inc_ref(r: ApplyResult)

    /**
     * \brief Decrement the reference counter of the given \c Z3_apply_result object.
     *
     * def_API('Z3_apply_result_dec_ref', VOID, (_in(CONTEXT), _in(APPLY_RESULT)))
     */
    fun Context.Z3_apply_result_dec_ref(r: ApplyResult)

    /**
     * \brief Convert the \c Z3_apply_result object returned by #Z3_tactic_apply into a string.
     *
     * def_API('Z3_apply_result_to_string', STRING, (_in(CONTEXT), _in(APPLY_RESULT)))
     */
    fun Context.Z3_apply_result_to_string(r: ApplyResult): String

    /**
     * \brief Return the number of subgoals in the \c Z3_apply_result object returned by #Z3_tactic_apply.
     *
     * def_API('Z3_apply_result_get_num_subgoals', UINT, (_in(CONTEXT), _in(APPLY_RESULT)))
     */
    fun Context.Z3_apply_result_get_num_subgoals(r: ApplyResult): Int

    /**
     * \brief Return one of the subgoals in the \c Z3_apply_result object returned by #Z3_tactic_apply.
     *
     * \pre i < Z3_apply_result_get_num_subgoals(c, r)
     *
     * def_API('Z3_apply_result_get_subgoal', GOAL, (_in(CONTEXT), _in(APPLY_RESULT), _in(UINT)))
     */
    fun Context.Z3_apply_result_get_subgoal(r: ApplyResult, i: Int): Goal

    /*@}*/

    /** @name Solvers*/
    /*@{*/
    /*@}*/
    /** @name Solvers
     */
    /*@{*/
    /**
     * \brief Create a new solver. This solver is a "combined solver" (see
     * combined_solver module) that internally uses a non-incremental (solver1) and an
     * incremental solver (solver2). This combined solver changes its behaviour based
     * on how it is used and how its parameters are set.
     *
     * If the solver is used in a non incremental way (i.e. no calls to
     * #Z3_solver_push() or #Z3_solver_pop(), and no calls to
     * #Z3_solver_assert() or #Z3_solver_assert_and_track() after checking
     * satisfiability without an intervening #Z3_solver_reset()) then solver1
     * will be used. This solver will apply Z3's "default" tactic.
     *
     * The "default" tactic will attempt to probe the logic used by the
     * assertions and will apply a specialized tactic if one is supported.
     * Otherwise the general `(and-then simplify smt)` tactic will be used.
     *
     * If the solver is used in an incremental way then the combined solver
     * will switch to using solver2 (which behaves similarly to the general
     * "smt" tactic).
     *
     * Note however it is possible to set the `solver2_timeout`,
     * `solver2_unknown`, and `ignore_solver1` parameters of the combined
     * solver to change its behaviour.
     *
     * The function #Z3_solver_get_model retrieves a model if the
     * assertions is satisfiable (i.e., the result is \c
     * Z3_L_TRUE) and model construction is enabled.
     * The function #Z3_solver_get_model can also be used even
     * if the result is \c Z3_L_UNDEF, but the returned model
     * is not guaranteed to satisfy quantified assertions.
     *
     * \remark User must use #Z3_solver_inc_ref and #Z3_solver_dec_ref to manage solver objects.
     * Even if the context was created using #Z3_mk_context instead of #Z3_mk_context_rc.
     *
     * def_API('Z3_mk_solver', SOLVER, (_in(CONTEXT),))
     */
    fun Context.Z3_mk_solver(): Solver

    /**
     * \brief Create a new incremental solver.
     *
     * This is equivalent to applying the "smt" tactic.
     *
     * Unlike #Z3_mk_solver() this solver
     * - Does not attempt to apply any logic specific tactics.
     * - Does not change its behaviour based on whether it used
     * incrementally/non-incrementally.
     *
     * Note that these differences can result in very different performance
     * compared to #Z3_mk_solver().
     *
     * The function #Z3_solver_get_model retrieves a model if the
     * assertions is satisfiable (i.e., the result is \c
     * Z3_L_TRUE) and model construction is enabled.
     * The function #Z3_solver_get_model can also be used even
     * if the result is \c Z3_L_UNDEF, but the returned model
     * is not guaranteed to satisfy quantified assertions.
     *
     * \remark User must use #Z3_solver_inc_ref and #Z3_solver_dec_ref to manage solver objects.
     * Even if the context was created using #Z3_mk_context instead of #Z3_mk_context_rc.
     *
     * def_API('Z3_mk_simple_solver', SOLVER, (_in(CONTEXT),))
     */
    fun Context.Z3_mk_simple_solver(): Solver

    /**
     * \brief Create a new solver customized for the given logic.
     * It behaves like #Z3_mk_solver if the logic is unknown or unsupported.
     *
     * \remark User must use #Z3_solver_inc_ref and #Z3_solver_dec_ref to manage solver objects.
     * Even if the context was created using #Z3_mk_context instead of #Z3_mk_context_rc.
     *
     * def_API('Z3_mk_solver_for_logic', SOLVER, (_in(CONTEXT), _in(SYMBOL)))
     */
    fun Context.Z3_mk_solver_for_logic(logic: Symbol): Solver

    /**
     * \brief Create a new solver that is implemented using the given tactic.
     * The solver supports the commands #Z3_solver_push and #Z3_solver_pop, but it
     * will always solve each #Z3_solver_check from scratch.
     *
     * \remark User must use #Z3_solver_inc_ref and #Z3_solver_dec_ref to manage solver objects.
     * Even if the context was created using #Z3_mk_context instead of #Z3_mk_context_rc.
     *
     * def_API('Z3_mk_solver_from_tactic', SOLVER, (_in(CONTEXT), _in(TACTIC)))
     */
    fun Context.Z3_mk_solver_from_tactic(t: Tactic): Solver

    /**
     * \brief Copy a solver \c s from the context \c source to the context \c target.
     *
     * def_API('Z3_solver_translate', SOLVER, (_in(CONTEXT), _in(SOLVER), _in(CONTEXT)))
     */
    fun Z3_solver_translate(source: Context, s: Solver, target: Context): Solver

    /**
     * \brief Ad-hoc method for importing model conversion from solver.
     *
     * This method is used for scenarios where \c src has been used to solve a set
     * of formulas and was interrupted. The \c dst solver may be a strengthening of \c src
     * obtained from cubing (assigning a subset of literals or adding constraints over the
     * assertions available in \c src). If \c dst ends up being satisfiable, the model for \c dst
     * may not correspond to a model of the original formula due to inprocessing in \c src.
     * This method is used to take the side-effect of inprocessing into account when returning
     * a model for \c dst.
     *
     * def_API('Z3_solver_import_model_converter', VOID, (_in(CONTEXT), _in(SOLVER), _in(SOLVER)))
     */
    fun Context.Z3_solver_import_model_converter(
        src: Solver,
        dst: Solver
    )

    /**
     * \brief Return a string describing all solver available parameters.
     *
     * \sa Z3_solver_get_param_descrs
     * \sa Z3_solver_set_params
     *
     * def_API('Z3_solver_get_help', STRING, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_help(s: Solver): String

    /**
     * \brief Return the parameter description set for the given solver object.
     *
     * \sa Z3_solver_get_help
     * \sa Z3_solver_set_params
     *
     * def_API('Z3_solver_get_param_descrs', PARAM_DESCRS, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Z3_solver_get_param_descrs(c: Context, s: Solver): ParamDescrs

    /**
     * \brief Set the given solver using the given parameters.
     *
     * \sa Z3_solver_get_help
     * \sa Z3_solver_get_param_descrs
     *
     * def_API('Z3_solver_set_params', VOID, (_in(CONTEXT), _in(SOLVER), _in(PARAMS)))
     */
    fun Context.Z3_solver_set_params(
        s: Solver,
        p: Params
    )

    /**
     * \brief Increment the reference counter of the given solver.
     *
     * def_API('Z3_solver_inc_ref', VOID, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_inc_ref(s: Solver)

    /**
     * \brief Decrement the reference counter of the given solver.
     *
     * def_API('Z3_solver_dec_ref', VOID, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_dec_ref(s: Solver)

    /**
     * \brief Solver local interrupt.
     * Normally you should use Z3_interrupt to cancel solvers because only
     * one solver is enabled concurrently per context.
     * However, per GitHub issue #1006, there are use cases where
     * it is more convenient to cancel a specific solver. Solvers
     * that are not selected for interrupts are left alone.
     *
     * def_API('Z3_solver_interrupt', VOID, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_interrupt(s: Solver)

    /**
     * \brief Create a backtracking point.
     *
     * The solver contains a stack of assertions.
     *
     * \sa Z3_solver_get_num_scopes
     * \sa Z3_solver_pop
     *
     * def_API('Z3_solver_push', VOID, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_push(s: Solver)

    /**
     * \brief Backtrack \c n backtracking points.
     *
     * \sa Z3_solver_get_num_scopes
     * \sa Z3_solver_push
     *
     * \pre n <= Z3_solver_get_num_scopes(c, s)
     *
     * def_API('Z3_solver_pop', VOID, (_in(CONTEXT), _in(SOLVER), _in(UINT)))
     */
    fun Context.Z3_solver_pop(s: Solver, n: Int)

    /**
     * \brief Remove all assertions from the solver.
     *
     * \sa Z3_solver_assert
     * \sa Z3_solver_assert_and_track
     *
     * def_API('Z3_solver_reset', VOID, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_reset(s: Solver)

    /**
     * \brief Return the number of backtracking points.
     *
     * \sa Z3_solver_push
     * \sa Z3_solver_pop
     *
     * def_API('Z3_solver_get_num_scopes', UINT, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_num_scopes(s: Solver): Int

    /**
     * \brief Assert a constraint into the solver.
     *
     * The functions #Z3_solver_check and #Z3_solver_check_assumptions should be
     * used to check whether the logical context is consistent or not.
     *
     * \sa Z3_solver_assert_and_track
     * \sa Z3_solver_reset
     *
     * def_API('Z3_solver_assert', VOID, (_in(CONTEXT), _in(SOLVER), _in(AST)))
     */
    fun Context.Z3_solver_assert(s: Solver, a: Ast)

    /**
     * \brief Assert a constraint \c a into the solver, and track it (in the unsat) core using
     * the boolean constant \c p.
     *
     * This API is an alternative to #Z3_solver_check_assumptions for extracting unsat cores.
     * Both APIs can be used in the same solver. The unsat core will contain a combination
     * of the boolean variables provided using Z3_solver_assert_and_track and the boolean literals
     * provided using #Z3_solver_check_assumptions.
     *
     * \pre \c a must be a boolean expression
     * \pre \c p must be a boolean constant (aka variable).
     *
     * \sa Z3_solver_assert
     * \sa Z3_solver_reset
     *
     * def_API('Z3_solver_assert_and_track', VOID, (_in(CONTEXT), _in(SOLVER), _in(AST), _in(AST)))
     */
    fun Context.Z3_solver_assert_and_track(
        s: Solver,
        a: Ast,
        p: Ast
    )

    /**
     * \brief load solver assertions from a file.
     *
     * \sa Z3_solver_from_string
     * \sa Z3_solver_to_string
     *
     * def_API('Z3_solver_from_file', VOID, (_in(CONTEXT), _in(SOLVER), _in(STRING)))
     */
    fun Context.Z3_solver_from_file(s: Solver, file_name: String)

    /**
     * \brief load solver assertions from a string.
     *
     * \sa Z3_solver_from_file
     * \sa Z3_solver_to_string
     *
     * def_API('Z3_solver_from_string', VOID, (_in(CONTEXT), _in(SOLVER), _in(STRING)))
     */
    fun Context.Z3_solver_from_string(s: Solver, file_name: String)

    /**
     * \brief Return the set of asserted formulas on the solver.
     *
     * def_API('Z3_solver_get_assertions', AST_VECTOR, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_assertions(s: Solver): AstVector

    /**
     * \brief Return the set of units modulo model conversion.
     *
     * def_API('Z3_solver_get_units', AST_VECTOR, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_units(s: Solver): AstVector

    /**
     * \brief Return the trail modulo model conversion, in order of decision level
     * The decision level can be retrieved using \c Z3_solver_get_level based on the trail.
     *
     * def_API('Z3_solver_get_trail', AST_VECTOR, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_trail(s: Solver): AstVector

    /**
     * \brief Return the set of non units in the solver state.
     *
     * def_API('Z3_solver_get_non_units', AST_VECTOR, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_non_units(s: Solver): AstVector

    /**
     * \brief retrieve the decision depth of boolean literals (variables or their negations).
     * Assumes a check-sat call and no other calls (to extract models) have been invoked.
     *
     * def_API('Z3_solver_get_levels', VOID, (_in(CONTEXT), _in(SOLVER), _in(AST_VECTOR), _in(UINT), _in_array(3, UINT)))
     */
    fun Context.Z3_solver_get_levels(
        s: Solver,
        literals: AstVector,
        sz: Int,
        levels: IntArray?
    )

    /**
     * \brief Check whether the assertions in a given solver are consistent or not.
     *
     * The function #Z3_solver_get_model retrieves a model if the
     * assertions is satisfiable (i.e., the result is \c
     * Z3_L_TRUE) and model construction is enabled.
     * Note that if the call returns \c Z3_L_UNDEF, Z3 does not
     * ensure that calls to #Z3_solver_get_model succeed and any models
     * produced in this case are not guaranteed to satisfy the assertions.
     *
     * The function #Z3_solver_get_proof retrieves a proof if proof
     * generation was enabled when the context was created, and the
     * assertions are unsatisfiable (i.e., the result is \c Z3_L_FALSE).
     *
     * \sa Z3_solver_check_assumptions
     *
     * def_API('Z3_solver_check', INT, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_check(s: Solver): Int

    /**
     * \brief Check whether the assertions in the given solver and
     * optional assumptions are consistent or not.
     *
     * The function #Z3_solver_get_unsat_core retrieves the subset of the
     * assumptions used in the unsatisfiability proof produced by Z3.
     *
     * \sa Z3_solver_check
     *
     * def_API('Z3_solver_check_assumptions', INT, (_in(CONTEXT), _in(SOLVER), _in(UINT), _in_array(2, AST)))
     */
    fun Context.Z3_solver_check_assumptions(
        s: Solver, num_assumptions: Int,
        assumptions: Array<Ast>
    ): Int

    /**
     * \brief Retrieve congruence class representatives for terms.
     *
     * The function can be used for relying on Z3 to identify equal terms under the current
     * set of assumptions. The array of terms and array of class identifiers should have
     * the same length. The class identifiers are numerals that are assigned to the same
     * value for their corresponding terms if the current context forces the terms to be
     * equal. You cannot deduce that terms corresponding to different numerals must be all different,
     * (especially when using non-convex theories).
     * All implied equalities are returned by this call.
     * This means that two terms map to the same class identifier if and only if
     * the current context implies that they are equal.
     *
     * A side-effect of the function is a satisfiability check on the assertions on the solver that is passed in.
     * The function return \c Z3_L_FALSE if the current assertions are not satisfiable.
     *
     * def_API('Z3_get_implied_equalities', INT, (_in(CONTEXT), _in(SOLVER), _in(UINT), _in_array(2, AST), _out_array(2, UINT)))
     */
    fun Context.Z3_get_implied_equalities(
        s: Solver,
        num_terms: Int,
        terms: Array<Ast>,
        class_ids: IntArray
    ): Int

    /**
     * \brief retrieve consequences from solver that determine values of the supplied function symbols.
     *
     * def_API('Z3_solver_get_consequences', INT, (_in(CONTEXT), _in(SOLVER), _in(AST_VECTOR), _in(AST_VECTOR), _in(AST_VECTOR)))
     */
    fun Context.Z3_solver_get_consequences(
        s: Solver,
        assumptions: AstVector,
        variables: AstVector,
        consequences: AstVector
    ): Int


    /**
     * \brief extract a next cube for a solver. The last cube is the constant \c true or \c false.
     * The number of (non-constant) cubes is by default 1. For the sat solver cubing is controlled
     * using parameters sat.lookahead.cube.cutoff and sat.lookahead.cube.fraction.
     *
     * The third argument is a vector of variables that may be used for cubing.
     * The contents of the vector is only used in the first call. The initial list of variables
     * is used in subsequent calls until it returns the unsatisfiable cube.
     * The vector is modified to contain a set of Autarky variables that occur in clauses that
     * are affected by the (last literal in the) cube. These variables could be used by a different
     * cuber (on a different solver object) for further recursive cubing.
     *
     * The last argument is a backtracking level. It instructs the cube process to backtrack below
     * the indicated level for the next cube.
     *
     * def_API('Z3_solver_cube', AST_VECTOR, (_in(CONTEXT), _in(SOLVER), _in(AST_VECTOR), _in(UINT)))
     */
    fun Context.Z3_solver_cube(
        s: Solver,
        vars: AstVector,
        backtrack_level: Int
    ): AstVector

    /**
     * \brief Retrieve the model for the last #Z3_solver_check or #Z3_solver_check_assumptions
     *
     * The error handler is invoked if a model is not available because
     * the commands above were not invoked for the given solver, or if the result was \c Z3_L_FALSE.
     *
     * def_API('Z3_solver_get_model', MODEL, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_model(s: Solver): Model?

    /**
     * \brief Retrieve the proof for the last #Z3_solver_check or #Z3_solver_check_assumptions
     *
     * The error handler is invoked if proof generation is not enabled,
     * or if the commands above were not invoked for the given solver,
     * or if the result was different from \c Z3_L_FALSE.
     *
     * def_API('Z3_solver_get_proof', AST, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_proof(s: Solver): Ast

    /**
     * \brief Retrieve the unsat core for the last #Z3_solver_check_assumptions
     * The unsat core is a subset of the assumptions \c a.
     *
     * By default, the unsat core will not be minimized. Generation of a minimized
     * unsat core can be enabled via the `"sat.core.minimize"` and `"smt.core.minimize"`
     * settings for SAT and SMT cores respectively. Generation of minimized unsat cores
     * will be more expensive.
     *
     * def_API('Z3_solver_get_unsat_core', AST_VECTOR, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_unsat_core(s: Solver): AstVector

    /**
     * \brief Return a brief justification for an "unknown" result (i.e., \c Z3_L_UNDEF) for
     * the commands #Z3_solver_check and #Z3_solver_check_assumptions
     *
     * def_API('Z3_solver_get_reason_unknown', STRING, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_reason_unknown(s: Solver): String

    /**
     * \brief Return statistics for the given solver.
     *
     * \remark User must use #Z3_stats_inc_ref and #Z3_stats_dec_ref to manage Z3_stats objects.
     *
     * def_API('Z3_solver_get_statistics', STATS, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_get_statistics(s: Solver): Stats

    /**
     * \brief Convert a solver into a string.
     *
     * \sa Z3_solver_from_file
     * \sa Z3_solver_from_string
     *
     * def_API('Z3_solver_to_string', STRING, (_in(CONTEXT), _in(SOLVER)))
     */
    fun Context.Z3_solver_to_string(s: Solver): String

    /**
     * \brief Convert a solver into a DIMACS formatted string.
     * \sa Z3_goal_to_diamcs_string for requirements.
     *
     * def_API('Z3_solver_to_dimacs_string', STRING, (_in(CONTEXT), _in(SOLVER), _in(boolean)))
     */
    fun Context.Z3_solver_to_dimacs_string(
        s: Solver,
        include_names: Boolean
    ): String

    /*@}*/

    /** @name Statistics */
    /*@{*/

    /*@}*/
    /** @name Statistics
     */
    /*@{*/
    /**
     * \brief Convert a statistics into a string.
     *
     * def_API('Z3_stats_to_string', STRING, (_in(CONTEXT), _in(STATS)))
     */
    fun Context.Z3_stats_to_string(s: Stats): String

    /**
     * \brief Increment the reference counter of the given statistics object.
     *
     * def_API('Z3_stats_inc_ref', VOID, (_in(CONTEXT), _in(STATS)))
     */
    fun Context.Z3_stats_inc_ref(s: Stats)

    /**
     * \brief Decrement the reference counter of the given statistics object.
     *
     * def_API('Z3_stats_dec_ref', VOID, (_in(CONTEXT), _in(STATS)))
     */
    fun Context.Z3_stats_dec_ref(s: Stats)

    /**
     * \brief Return the number of statistical data in \c s.
     *
     * def_API('Z3_stats_size', UINT, (_in(CONTEXT), _in(STATS)))
     */
    fun Context.Z3_stats_size(s: Stats): Int

    /**
     * \brief Return the key (a string) for a particular statistical data.
     *
     * \pre idx < Z3_stats_size(c, s)
     *
     * def_API('Z3_stats_get_key', STRING, (_in(CONTEXT), _in(STATS), _in(UINT)))
     */
    fun Context.Z3_stats_get_key(s: Stats, idx: Int): String

    /**
     * \brief Return \c true if the given statistical data is a int integer.
     *
     * \pre idx < Z3_stats_size(c, s)
     *
     * def_API('Z3_stats_is_uint', boolean, (_in(CONTEXT), _in(STATS), _in(UINT)))
     */
    fun Context.Z3_stats_is_uint(s: Stats, idx: Int): Boolean

    /**
     * \brief Return \c true if the given statistical data is a double.
     *
     * \pre idx < Z3_stats_size(c, s)
     *
     * def_API('Z3_stats_is_double', boolean, (_in(CONTEXT), _in(STATS), _in(UINT)))
     */
    fun Context.Z3_stats_is_double(s: Stats, idx: Int): Boolean

    /**
     * \brief Return the int value of the given statistical data.
     *
     * \pre idx < Z3_stats_size(c, s) && Z3_stats_is_uint(c, s)
     *
     * def_API('Z3_stats_get_uint_value', UINT, (_in(CONTEXT), _in(STATS), _in(UINT)))
     */
    fun Context.Z3_stats_get_uint_value(s: Stats, idx: Int): Int

    /**
     * \brief Return the double value of the given statistical data.
     *
     * \pre idx < Z3_stats_size(c, s) && Z3_stats_is_double(c, s)
     *
     * def_API('Z3_stats_get_double_value', DOUBLE, (_in(CONTEXT), _in(STATS), _in(UINT)))
     */
    fun Context.Z3_stats_get_double_value(s: Stats, idx: Int): Double

    /**
     * \brief Return the estimated allocated memory in bytes.Z3_params
     *
     * def_API('Z3_get_estimated_alloc_size', UINT64, ())
     */
    fun Z3_get_estimated_alloc_size(): Long
}
