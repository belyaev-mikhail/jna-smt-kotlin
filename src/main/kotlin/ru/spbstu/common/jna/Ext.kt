package ru.spbstu.common.jna

import com.sun.jna.Pointer
import java.nio.charset.Charset

fun Pointer.getString(offset: Long, size: Int, encoding: String): String =
    String(getByteArray(offset, size), Charset.forName(encoding))

fun Pointer.getString(offset: Long, size: Int): String =
    String(getByteArray(offset, size))
