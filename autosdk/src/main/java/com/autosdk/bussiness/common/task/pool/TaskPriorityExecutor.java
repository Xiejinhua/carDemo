package com.autosdk.bussiness.common.task.pool;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Date: 14-5-16
 * Time: 上午11:25
 */
public class TaskPriorityExecutor implements Executor {

	private static final int CORE_POOL_SIZE = 3;
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
	private static final int KEEP_ALIVE = 30;

	private static final Comparator<Runnable> RUNNABLE_COMPARATOR = new Comparator<Runnable>() {
		@Override
		public int compare(Runnable lhs, Runnable rhs) {
			if (lhs instanceof TaskPriorityRunnable && rhs instanceof TaskPriorityRunnable) {
				return ((TaskPriorityRunnable) lhs).priority.ordinal() - ((TaskPriorityRunnable) rhs).priority.ordinal();
			} else {
				return 0;
			}
		}
	};

	private final BlockingQueue<Runnable> mPoolWorkQueue =
		new PriorityBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE, RUNNABLE_COMPARATOR);
	private final ThreadPoolExecutor mThreadPoolExecutor;

	public TaskPriorityExecutor() {
        this(CORE_POOL_SIZE, new TaskThreadFactory("Defalut"));
    }

	public TaskPriorityExecutor(int poolSize, ThreadFactory threadFactory) {
		mThreadPoolExecutor = new ThreadPoolExecutor(
			poolSize,
			MAXIMUM_POOL_SIZE,
			KEEP_ALIVE,
			TimeUnit.SECONDS,
			mPoolWorkQueue,
				threadFactory);
	}

	/**
	 * 该构造器会创建单线程池执行任务
	 */
	public TaskPriorityExecutor(ThreadFactory threadFactory) {
		mThreadPoolExecutor = new ThreadPoolExecutor(1, 1,
			0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(),
			threadFactory);
	}

	public int getPoolSize() {
		return mThreadPoolExecutor.getCorePoolSize();
	}

	public void setPoolSize(int poolSize) {
		if (poolSize > 0) {
			mThreadPoolExecutor.setCorePoolSize(poolSize);
		}
//		Executors.newCachedThreadPool()
//		Executors.newFixedThreadPool()
//		Executors.newScheduledThreadPool()
//		Executors.newSingleThreadExecutor()
//		Executors.newSingleThreadScheduledExecutor()
	}

	public ThreadPoolExecutor getThreadPoolExecutor() {
		return mThreadPoolExecutor;
	}

	/**
	 * 线程池忙
	 */
	public boolean isBusy() {
		return mThreadPoolExecutor.getActiveCount() >= mThreadPoolExecutor.getCorePoolSize();
	}

	/**
	 * 线程池超载
	 */
	public boolean isFull() {
		return mThreadPoolExecutor.getActiveCount() >= mThreadPoolExecutor.getCorePoolSize() * 2;
	}

    public boolean isShutdown(){
        return mThreadPoolExecutor.isShutdown();
    }

	@Override
	public void execute(final Runnable r) {
		mThreadPoolExecutor.execute(r);
	}

}
