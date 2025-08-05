package com.autosdk.bussiness.common.task.pool;

import android.text.TextUtils;

/**
 * 任务线程优先级
 */
public class TaskPriorityRunnable implements Runnable {
	public final TaskPriority priority;
	private final Runnable runnable;
	private String name;
	private String oldThreadName;

	public TaskPriorityRunnable(TaskPriority priority, String name, Runnable runnable) {
		this.priority = priority == null ? TaskPriority.DEFAULT : priority;
		this.runnable = runnable;
		this.name = name == null ? null : new StringBuilder().append("Executor#").append(name).toString();
	}

	@Override
	public final void run() {
		// run会进来多次，防止多次设置线程名称引起问题，故添加对name、oldThreadName判空判断
		if (!TextUtils.isEmpty(name) && TextUtils.isEmpty(oldThreadName)){
			oldThreadName = Thread.currentThread().getName();
			Thread.currentThread().setName(name);
		}
		this.runnable.run();
		// oldThreadName不为空时才进入设置线程名称
		if (!TextUtils.isEmpty(oldThreadName)){
			Thread.currentThread().setName(oldThreadName);
			oldThreadName = null;
		}
	}
}
