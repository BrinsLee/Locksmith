package com.brins.locksmith.utils

import java.util.concurrent.*

/**
 * @author lipeilin
 * @date 2020/4/29
 */
class JobExecutor private constructor() : Executor {

    companion object {
        private const val TAG = "JobExecutor"
        private const val CORE_THREAD_POOL = 3
        private const val MAX_THREAD_POOL = 5
        private const val MAX_ALIVE_TIME = 60L
        private val MAX_ALIVE_TIME_UNIT =
            TimeUnit.SECONDS
        private var mSingleton: JobExecutor? = null

        fun getExecutor(): JobExecutor {
            if (mSingleton == null) {
                synchronized(JobExecutor::class.java) {
                    if (mSingleton == null) {
                        mSingleton = JobExecutor()
                    }
                }
            }
            return mSingleton!!
        }

    }

    private val mRunnableQueue: BlockingDeque<Runnable> = LinkedBlockingDeque<Runnable>()
    private val mThreadFactory: ThreadFactory = JobThreadFactory()

    private val mThreadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
        CORE_THREAD_POOL, MAX_THREAD_POOL,
        MAX_ALIVE_TIME, MAX_ALIVE_TIME_UNIT, this.mRunnableQueue, this.mThreadFactory
    )


    override fun execute(command: Runnable?) {
        if (command == null) {
            throw IllegalArgumentException("Runnable to execute cannot be null")
        }
        try {
            mThreadPoolExecutor.execute(command)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class JobThreadFactory : ThreadFactory {
        private var mCount = 0
        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, THREAD_PREFIX + mCount++)
        }

        companion object {
            private const val THREAD_PREFIX = "YS_THREAD_"
        }
    }
}