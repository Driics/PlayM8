package ru.driics.playm8.utils.cache

import java.io.IOException
import java.io.OutputStream
import java.util.Arrays

class ImmutableByteArrayOutputStream @JvmOverloads constructor(size: Int = 32) : OutputStream() {
    @JvmField
    var buf: ByteArray
    var count = 0
    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity - buf.size > 0) {
            grow(minCapacity)
        }
    }

    init {
        buf = ByteArray(size)
    }

    private fun grow(minCapacity: Int) {
        val oldCapacity = buf.size
        var newCapacity = oldCapacity shl 1
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity
        if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity)
        buf = Arrays.copyOf(buf, newCapacity)
    }

    @Synchronized
    override fun write(b: Int) {
        ensureCapacity(count + 1)
        buf[count] = b.toByte()
        count += 1
    }

    fun writeInt(value: Int) {
        ensureCapacity(count + 4)
        buf[count] = (value ushr 24).toByte()
        buf[count + 1] = (value ushr 16).toByte()
        buf[count + 2] = (value ushr 8).toByte()
        buf[count + 3] = value.toByte()
        count += 4
    }

    fun writeLong(value: Long) {
        ensureCapacity(count + 8)
        buf[count] = (value ushr 56).toByte()
        buf[count + 1] = (value ushr 48).toByte()
        buf[count + 2] = (value ushr 40).toByte()
        buf[count + 3] = (value ushr 32).toByte()
        buf[count + 4] = (value ushr 24).toByte()
        buf[count + 5] = (value ushr 16).toByte()
        buf[count + 6] = (value ushr 8).toByte()
        buf[count + 7] = value.toByte()
        count += 8
    }

    @Synchronized
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || off > b.size || len < 0 || off + len - b.size > 0) {
            throw IndexOutOfBoundsException()
        }
        ensureCapacity(count + len)
        System.arraycopy(b, off, buf, count, len)
        count += len
    }

    @Synchronized
    @Throws(IOException::class)
    fun writeTo(out: OutputStream) {
        out.write(buf, 0, count)
    }

    @Synchronized
    fun reset() {
        count = 0
    }

    fun count(): Int {
        return count
    }

    companion object {
        private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8
        private fun hugeCapacity(minCapacity: Int): Int {
            if (minCapacity < 0) throw OutOfMemoryError()
            return if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
        }
    }
}