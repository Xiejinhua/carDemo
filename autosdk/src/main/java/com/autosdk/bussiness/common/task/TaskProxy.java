package com.autosdk.bussiness.common.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.autosdk.bussiness.common.task.pool.TaskPriority;
import com.autosdk.bussiness.common.task.pool.TaskPriorityExecutor;
import com.autosdk.bussiness.common.task.pool.TaskPriorityRunnable;
import com.autosdk.bussiness.common.task.pool.TaskThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import timber.log.Timber;

/*package*/ final class TaskProxy<ResultType> extends Task<ResultType> {

	private final Task<ResultType> task;
	private Executor executor;

	/*package*/ static final InternalHandler INTERNAL_HANDLER = new InternalHandler();

	static TaskPriorityExecutor sDefaultExecutor = new TaskPriorityExecutor();
	static TaskPriorityExecutor sSearchExecutor = null;
	static TaskPriorityExecutor sNetExecutor = null;
	static TaskPriorityExecutor sAE8EngineExecutor = null;
	static TaskPriorityExecutor sUDiskExecutor = null;
	static TaskPriorityExecutor sTtsInitializeExecutor = null;
	static TaskPriorityExecutor sSyncSdkIOHandleExecutor = null;
	static TaskPriorityExecutor sAdapterExecutor = null;
	static TaskPriorityExecutor sLoggerExecutor = null;
	static TaskPriorityExecutor sActivateLogExecutor = null;
	// 延迟执行线程
	public static ScheduledThreadPoolExecutor sScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
	// 顺序执行线程池
	private static ExecutorService sSingleThreadExecutor = null;

	private ResultType result;
	private Throwable exception;
	private CancelledException cancelledException;

	/*package*/ TaskProxy(Task<ResultType> task) {
		if (task == null) {
			throw new IllegalArgumentException("task must not be null");
		}
		this.task = task;
		this.executor = task.getExecutor();
		if (this.executor == null) {
			this.executor = sDefaultExecutor;
		}
	}

	public static synchronized TaskPriorityExecutor getExecute(TaskExector type){
        if (TaskExector.DEFALUT.equals(type)) {
            if (sDefaultExecutor == null || sDefaultExecutor.isShutdown()){
                sDefaultExecutor = new TaskPriorityExecutor(3, new TaskThreadFactory("Default"));
            }
            return sDefaultExecutor;
        }else if(TaskExector.SEARCH.equals(type)) {
			if (sSearchExecutor == null || sSearchExecutor.isShutdown()){
				sSearchExecutor = new TaskPriorityExecutor(2, new TaskThreadFactory("Search"));
			}
			return sSearchExecutor;
		} else if (TaskExector.NET_WORK.equals(type)){
			if (sNetExecutor == null || sNetExecutor.isShutdown()){
				sNetExecutor = new TaskPriorityExecutor(1, new TaskThreadFactory("Net"));
			}
			return sNetExecutor;
		} else if (TaskExector.AE8_ENGINE.equals(type)){
			if (sAE8EngineExecutor == null || sAE8EngineExecutor.isShutdown()){
				sAE8EngineExecutor = new TaskPriorityExecutor(1, new TaskThreadFactory("AE8_Engine"));
			}
			return sAE8EngineExecutor;
		} else if (TaskExector.UDISK_DOWNLOAD.equals(type)){
			if (sUDiskExecutor == null || sUDiskExecutor.isShutdown()){
				sUDiskExecutor = new TaskPriorityExecutor(1, new TaskThreadFactory("UDISK_DOWNLOAD"));
			}
			return sUDiskExecutor;
		} else if (TaskExector.SYNC_SDK_IO.equals(type)) {
			if (sSyncSdkIOHandleExecutor == null || sSyncSdkIOHandleExecutor.isShutdown()) {
				sSyncSdkIOHandleExecutor = new TaskPriorityExecutor(1, new TaskThreadFactory("SYNC_SDK_IO_HANDLE"));
			}
			return sSyncSdkIOHandleExecutor;
		} else if (TaskExector.USER_BL.equals(type)) {
			if (sSyncSdkIOHandleExecutor == null || sSyncSdkIOHandleExecutor.isShutdown()) {
				sSyncSdkIOHandleExecutor = new TaskPriorityExecutor(1, new TaskThreadFactory("USER_BL"));
			}
			return sSyncSdkIOHandleExecutor;
		}else if (TaskExector.ADAPTER.equals(type)) {
			if (sAdapterExecutor == null || sAdapterExecutor.isShutdown()) {
				sAdapterExecutor = new TaskPriorityExecutor(1, new TaskThreadFactory("ADAPTER"));
			}
			return sAdapterExecutor;
		}
//		else if (TaskExector.Timber.equals(type)) {
//			if (sLoggerExecutor == null || sLoggerExecutor.isShutdown()) {
//				//日志问题出于时序考虑只能单线程模型进行执行
//				sLoggerExecutor = new TaskPriorityExecutor(new TaskThreadFactory("LOGGER"));
//			}
//			return sLoggerExecutor;
//		}
		else if (TaskExector.ACTIVATE_LOG.equals(type)) {
			if (sActivateLogExecutor == null || sActivateLogExecutor.isShutdown()) {
				//日志问题出于时序考虑只能单线程模型进行执行
				sActivateLogExecutor = new TaskPriorityExecutor(1, new TaskThreadFactory("ACTIVATE_LOG"));
			}
			return sActivateLogExecutor;
		}


		return sDefaultExecutor;
	}

	/**
	 * 获取地图引导路线绘制线程池
	 * @return
	 */
	public synchronized static ExecutorService getSingleThreadExecutor() {
		if (sSingleThreadExecutor == null || sSingleThreadExecutor.isShutdown()) {
			sSingleThreadExecutor = Executors.newSingleThreadExecutor();
		}
		return sSingleThreadExecutor;
	}

	/**
	 * 关闭地图引导路线绘制线程池
	 * @return
	 */
	private synchronized static void shutDownSingleThreadExecutor() {
		if (sSingleThreadExecutor != null){
			sSingleThreadExecutor.shutdown();
			sSingleThreadExecutor = null;
		}
	}

	public synchronized static void shutDown(TaskExector type){
        Timber.d( "[TaskProxy] shutDown:= "+ type);
		if (TaskExector.SEARCH.equals(type)) {
			if (sSearchExecutor != null){
				sSearchExecutor.getThreadPoolExecutor().shutdown();
				sSearchExecutor = null;
			}
		} else if (TaskExector.NET_WORK.equals(type)){
			if (sNetExecutor != null){
				sNetExecutor.getThreadPoolExecutor().shutdown();
				sNetExecutor = null;
			}
		} else if (TaskExector.AE8_ENGINE.equals(type)){
			if (sAE8EngineExecutor != null){
				sAE8EngineExecutor.getThreadPoolExecutor().shutdown();
				sAE8EngineExecutor = null;
			}
		} else if (TaskExector.UDISK_DOWNLOAD.equals(type)){
			if (sUDiskExecutor != null){
				sUDiskExecutor.getThreadPoolExecutor().shutdown();
				sUDiskExecutor = null;
			}
		} else if (TaskExector.SYNC_SDK_IO.equals(type)) {
			if (sSyncSdkIOHandleExecutor != null) {
				sSyncSdkIOHandleExecutor.getThreadPoolExecutor().shutdown();
				sSyncSdkIOHandleExecutor = null;
			}
		} else if (TaskExector.USER_BL.equals(type)) {
			if (sSyncSdkIOHandleExecutor != null) {
				sSyncSdkIOHandleExecutor.getThreadPoolExecutor().shutdown();
				sSyncSdkIOHandleExecutor = null;
			}
		}else if (TaskExector.ADAPTER.equals(type)) {
			if (sAdapterExecutor != null) {
				sAdapterExecutor.getThreadPoolExecutor().shutdown();
				sAdapterExecutor = null;
			}
		}else if (TaskExector.ACTIVATE_LOG.equals(type)) {
			if (sActivateLogExecutor != null) {
				sActivateLogExecutor.getThreadPoolExecutor().shutdown();
				sActivateLogExecutor = null;
			}
		}

	}

	public static void onDestory(){
        Timber.d( "[TaskProxy] onDestory");
        if(INTERNAL_HANDLER != null){
            INTERNAL_HANDLER.removeCallbacksAndMessages(null);
        }
		shutDownSingleThreadExecutor();
//		if (sScheduledThreadPoolExecutor != null){
//			sScheduledThreadPoolExecutor.shutdown();
//			sScheduledThreadPoolExecutor = null;
//		}
	}

	@Override
	protected ResultType doBackground() throws Exception {
		this.setState(State.Waiting);
        TaskPriorityRunnable runnable = new TaskPriorityRunnable(
			task.getPriority(),null,
			new Runnable() {
				@Override
				public void run() {
					try {
						Timber.d( "taskProxy 1");
						// trace_start running
						TaskProxy.this.setState(State.Running);
						TaskProxy.this.onStart();

						result = task.doBackground();
						if (TaskProxy.this.state == State.Cancelled) { // 没有在doBackground过程中取消成功
							Timber.d("taskProxy 1 cancelled");
							throw new CancelledException("");
						}
						TaskProxy.this.setState(State.Finished);
						TaskProxy.this.onFinished(result);
					} catch (CancelledException cex) {
						Timber.e(cex, "taskProxy");
						TaskProxy.this.setState(State.Cancelled);
						TaskProxy.this.onCancelled(cex);
					} catch (Throwable ex) {
						Timber.e( ex,"taskProxy");
						TaskProxy.this.setState(State.Error);
						TaskProxy.this.onError(ex, false);
					}
				}
			});
		this.executor.execute(runnable);
		return null;
	}

	@Override
	protected void onFinished(ResultType result) {
		INTERNAL_HANDLER.obtainMessage(MSG_WHAT_ON_FINISH, this).sendToTarget();
	}

	@Override
	protected void onError(Throwable ex, boolean isCallbackError) {
		exception = ex;
		INTERNAL_HANDLER.obtainMessage(MSG_WHAT_ON_ERROR, this).sendToTarget();
	}

	@Override
	protected void onStart() {
		INTERNAL_HANDLER.obtainMessage(MSG_WHAT_ON_START, this).sendToTarget();
	}

	@Override
	protected void onUpdate(int flag, Object... args) {
		INTERNAL_HANDLER.obtainMessage(MSG_WHAT_ON_UPDATE, flag, 0, new ArgsObj(this, args)).sendToTarget();
	}

	@Override
	protected void onCancelled(CancelledException cex) {
		cancelledException = cex;
		INTERNAL_HANDLER.obtainMessage(MSG_WHAT_ON_CANCEL, this).sendToTarget();
	}

	private void setState(State state) {
		this.state = state;
		this.task.state = state;
	}

	@Override
	public TaskPriority getPriority() {
		return task.getPriority();
	}

	@Override
	public Executor getExecutor() {
		return task.getExecutor();
	}

	// ########################### inner type #############################
	private static class ArgsObj {
		TaskProxy taskProxy;
		Object[] args;

		public ArgsObj(TaskProxy taskProxy, Object[] args) {
			this.taskProxy = taskProxy;
			this.args = args;
		}
	}

	private final static int MSG_WHAT_ON_START = 1;
	private final static int MSG_WHAT_ON_FINISH = 2;
	private final static int MSG_WHAT_ON_ERROR = 3;
	private final static int MSG_WHAT_ON_UPDATE = 4;
	private final static int MSG_WHAT_ON_CANCEL = 5;

	/*package*/ final static class InternalHandler extends Handler {

		private InternalHandler() {
			super(Looper.getMainLooper());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj == null) {
				throw new IllegalArgumentException("msg must not be null");
			}
			TaskProxy taskProxy = null;
			Object[] args = null;
			if (msg.obj instanceof TaskProxy) {
				taskProxy = (TaskProxy) msg.obj;
			} else if (msg.obj instanceof ArgsObj) {
				ArgsObj argsObj = (ArgsObj) msg.obj;
				taskProxy = argsObj.taskProxy;
				args = argsObj.args;
			}
			if (taskProxy == null) {
				throw new RuntimeException("msg.obj not instanceof TaskProxy");
			}

			try {
				switch (msg.what) {
					case MSG_WHAT_ON_START: {
						taskProxy.task.onStart();
						break;
					}
					case MSG_WHAT_ON_FINISH: {
						taskProxy.task.onFinished(taskProxy.result);
						break;
					}
					case MSG_WHAT_ON_ERROR: {
						try {
							taskProxy.task.onError(taskProxy.exception, false);
						} catch (Throwable ignored) {
						}
						break;
					}
					case MSG_WHAT_ON_UPDATE: {
						taskProxy.task.onUpdate(msg.arg1, args);
						break;
					}
					case MSG_WHAT_ON_CANCEL: {
						taskProxy.task.onCancelled(taskProxy.cancelledException);
						break;
					}
					default: {
						break;
					}
				}
			} catch (Throwable ex) {
				taskProxy.setState(State.Error);
				if (msg.what != MSG_WHAT_ON_ERROR) {
					taskProxy.task.onError(ex, true);
				} else {
					ex.printStackTrace();
				}
			}
		}
	}
}
