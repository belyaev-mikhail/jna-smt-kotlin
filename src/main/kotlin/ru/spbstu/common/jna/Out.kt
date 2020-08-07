package ru.spbstu.common.jna

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class Out<T: PointerType>(val base: T?): PointerByReference() {
    constructor(): this(null)

    val value: T get() {
        base?.pointer = super.getValue()
        return base!!
    }
}

class OutArr<T: PointerType>(val clazz: Class<T>): PointerByReference() {
    operator fun get(idx: Int): T = clazz.newInstance().also {
        it.pointer = super.getValue().getPointer(idx.toLong() * Native.POINTER_SIZE)
    }
}

class StringByReference: PointerByReference() {
    val value: String get() = super.getValue().getString(0)
}

fun PointerByReference.free() = Native.free(Pointer.nativeValue(value))
