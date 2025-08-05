package com.autosdk.bussiness.common.task;

import android.os.Looper;

import com.autosdk.bussiness.common.task.pool.TaskPriorityExecutor;

import java.util.concurrent.TimeUnit;
import timber.log.Timber;

/**
 * @date: 2014/11/12
 */
@Deprecated
public class TaskManager {

	private TaskManager() {
	}

	/**
	 * run task
	 *
	 * @param task
	 * @param <T>
	 * @return
	 */
	public static <T> Task<T> start(Task<T> task) {
		TaskProxy<T> proxy = null;
		if (task instanceof TaskProxy) {
			proxy = (TaskProxy<T>) task;
			Timber.d( "proxy");
		} else {
			proxy = new TaskProxy<T>(task);
			task.taskProxy = proxy;
			Timber.d( "taskProxy");
		}
		try {
			Timber.d( "proxy.doBackground()");
			proxy.doBackground();
		} catch (Throwable ex) {
			Timber.e(ex,"Task error ");
		}
		return proxy;
	}

	/**
	 * run in UI thread
	 *
	 * @param runnable
	 */
	public static void post(Runnable runnable) {
        if(runnable == null){
            return;
        }
		Looper mainLooper = Looper.getMainLooper();
		if (mainLooper != null && mainLooper.getThread() == Thread.currentThread()) {
			runnable.run();
		} else {
			TaskProxy.INTERNAL_HANDLER.post(runnable);
		}
	}

	/**
	 * 在主线程looper队尾插入runnable
	 * */
	public static void postAfterQueue(Runnable runnable){
		if(runnable == null){
			return;
		}
		TaskProxy.INTERNAL_HANDLER.post(runnable);
	}

	/**
	 * run in UI thread
	 *
	 * @param runnable
	 * @param delayMillis
	 */
	public static void postDelayed(Runnable runnable, long delayMillis) {
        if(runnable == null){
            return;
        }
		TaskProxy.INTERNAL_HANDLER.postDelayed(runnable, delayMillis);
	}

	/**
	 *
	 * @param runnable
	 */
	public static void removeCallbacks(Runnable runnable){
		if(runnable == null){
			return;
		}
		TaskProxy.INTERNAL_HANDLER.removeCallbacks(runnable);
	}

	/**
	 * run in background thread
	 *
     * 异常保护：
     * 由于线程池退出时做了shutDown的操作，但是某些销毁逻辑在线程中又开线程，
     * 因此当线程池销毁后再调用，就起新线程来运行
	 * @param runnable
	 */
	public static void run(Runnable runnable) {
        if(runnable == null){
            return;
        }
		if(TaskProxy.sDefaultExecutor != null){
            //若线程池满载，则启动野线程，防止内存泄露
            if(!TaskProxy.sDefaultExecutor.isFull()){
                TaskProxy.sDefaultExecutor.execute(runnable);
                return;
            }else{
            }
		}
        new Thread(runnable).start();
	}
	/**
	 * 执行延迟任务
	 * @param runnable
	 *           任务
	 * @param delay
	 *           延迟时间
	 * @param unit
	 *           延迟时间单位
	 */
	public static void runDelay(Runnable runnable, long delay, TimeUnit unit) {
		if(runnable == null){
			return;
		}
		if(TaskProxy.sScheduledThreadPoolExecutor != null){
			TaskProxy.sScheduledThreadPoolExecutor.schedule(runnable, delay, unit);
		}
	}

	/**
	 * 获取指定类别线程池。
	 *
	 * @param type 线程池类别
	 * @return 返回指定线程池。请注意判空。
	 */
	public static TaskPriorityExecutor getExecute(TaskExector type) {
		return TaskProxy.getExecute(type);
	}

	/**
	 * 顺序线程线程池
	 * @param runnable
	 */
	public synchronized static void runSingleThread(Runnable runnable) {
		if(runnable == null){
			return;
		}
		TaskProxy.getSingleThreadExecutor().execute(runnable);
	}

	/**
	 *  关闭指定的线程池
	 */
	public static void shutDown(TaskExector type){
		TaskProxy.shutDown(type);
	}

	/**
	 *  销毁所有统一控制的线程池
	 */
	public static void onDestory(){
		TaskProxy.onDestory();
	}
}
