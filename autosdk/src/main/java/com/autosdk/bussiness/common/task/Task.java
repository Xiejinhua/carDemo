package com.autosdk.bussiness.common.task;

import com.autosdk.bussiness.common.task.pool.TaskPriority;

import java.util.concurrent.Executor;

/**
 * @date: 2014/11/11
 */
public abstract class Task<ResultType> {

	/*package*/ volatile State state = State.Null;
	/*package*/ TaskProxy taskProxy = null;

	protected abstract ResultType doBackground() throws Exception;

	protected abstract void onFinished(ResultType result);

	protected abstract void onError(Throwable ex, boolean isCallbackError);

	protected void onStart() {
	}

	protected void onUpdate(int flag, Object... args) {
	}

	protected void onCancelled(CancelledException cex) {
	}

	public final void update(int flag, Object... args) {
		if (taskProxy != null) {
			taskProxy.onUpdate(flag, args);
		}
	}

	public final void cancel() {
		this.state = State.Cancelled;
		if (taskProxy != null) {
			taskProxy.cancel();
		}
	}

	public final State getState() {
		return state;
	}

	public final boolean isStopped() {
		return this.state.value() > State.Running.value();
	}

	public TaskPriority getPriority() {
		return null;
	}

	public Executor getExecutor() {
		return null;
	}

	public static class CancelledException extends RuntimeException {
		public CancelledException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static enum State {
		/**
		 * 无
		 */
		Null(0),
		/**
		 *等待
		 */
		Waiting(1),
		/**
		 *运行中
		 */
		Running(2),
		/**
		 *完成
		 */
		Finished(3),
		/**
		 *取消
		 */
		Cancelled(4),
		/**
		 *错误
		 */
		Error(5);
		private final int value;

		private State(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}
}
