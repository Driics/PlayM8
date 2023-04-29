package ru.driics.playm8.utils.cache

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import java.util.concurrent.CountDownLatch

class DispatchQueue @JvmOverloads constructor(threadName: String?, start: Boolean = true) :
    Thread() {
    @Volatile
    var handler: Handler? = null
        private set
    private val syncLatch = CountDownLatch(1)
    var lastTaskTime: Long = 0
        private set
    val index = indexPointer++

    init {
        name = threadName
        if (start) {
            start()
        }
    }

    fun sendMessage(msg: Message?, delay: Int) {
        try {
            syncLatch.await()
            if (delay <= 0) {
                handler!!.sendMessage(msg!!)
            } else {
                handler!!.sendMessageDelayed(msg!!, delay.toLong())
            }
        } catch (ignore: Exception) {
        }
    }

    fun cancelRunnable(runnable: Runnable?) {
        try {
            syncLatch.await()
            handler!!.removeCallbacks(runnable!!)
        } catch (ignored: Exception) {
        }
    }

    fun cancelRunnables(runnables: Array<Runnable?>) {
        try {
            syncLatch.await()
            for (runnable in runnables) {
                handler!!.removeCallbacks(runnable!!)
            }
        } catch (ignored: Exception) {
        }
    }

    fun postRunnable(runnable: Runnable?): Boolean {
        lastTaskTime = SystemClock.elapsedRealtime()
        return postRunnable(runnable, 0)
    }

    fun postRunnable(runnable: Runnable?, delay: Long): Boolean {
        try {
            syncLatch.await()
        } catch (ignored: Exception) {
        }
        return if (delay <= 0) {
            handler!!.post(runnable!!)
        } else {
            handler!!.postDelayed(runnable!!, delay)
        }
    }

    fun cleanupQueue() {
        try {
            syncLatch.await()
            handler!!.removeCallbacksAndMessages(null)
        } catch (_: Exception) {
        }
    }

    fun handleMessage(inputMessage: Message?) {}

    fun recycle() {
        handler!!.looper.quit()
    }

    override fun run() {
        Looper.prepare()
        handler = Handler(Looper.myLooper()!!) { msg: Message? ->
            handleMessage(msg)
            true
        }
        syncLatch.countDown()
        Looper.loop()
    }

    val isReady: Boolean
        get() = syncLatch.count == 0L

    companion object {
        private var indexPointer = 0
    }
}