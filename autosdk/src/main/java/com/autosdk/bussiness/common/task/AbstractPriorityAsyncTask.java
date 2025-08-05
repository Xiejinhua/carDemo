/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.autosdk.bussiness.common.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;


import com.autosdk.bussiness.common.task.pool.TaskPriority;
import com.autosdk.bussiness.common.task.pool.TaskPriorityExecutor;
import com.autosdk.bussiness.common.task.pool.TaskPriorityRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p/>
 * 执行顺序:
 * onPreExecute (UI线程)
 * doInBackground(后台线程)
 * onPostExecute(UI线程)
 * <p/>
 * 在doInBackground中调用publishProgress(Progress...)会post到UI线程执行onProgressUpdate(Progress...)
 * <p/>
 * Date: 14-5-23
 * Time: 上午11:25
 */
public abstract class AbstractPriorityAsyncTask<Params, Progress, Result> implements TaskHandler {

    private static TaskPriorityExecutor sDefaultExecutor = new TaskPriorityExecutor();

    private static final int MESSAGE_POST_RESULT = 0x1;
    private static final int MESSAGE_POST_PROGRESS = 0x2;
    private static final InternalHandler S_HANDLER = new InternalHandler();
    private final AbstractWorkerRunnable<Params, Result> mWorker;
    private final FutureTask<Result> mFuture;
    private final AtomicBoolean mCancelled = new AtomicBoolean();
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
    private volatile boolean mExecuteInvoked = false;
    private boolean callbackOnBg = false;
    private TaskPriority priority = TaskPriority.DEFAULT;

    /**
     * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
     */
    public AbstractPriorityAsyncTask() {
        mWorker = new AbstractWorkerRunnable<Params, Result>() {
            @Override
            public Result call() throws Exception {
                mTaskInvoked.set(true);
                try{
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                }catch (Exception e){}
                //noinspection unchecked
                return postResult(doInBackground(mParams));
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    Log.w(e.getMessage(), e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(
                            "An error occured while executing doInBackground()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }

    /**
     * Convenience version of {@link #execute(Object...)} for use with
     * a simple Runnable object. See {@link #execute(Object[])} for more
     * information on the order of execution.
     *
     * @see #execute(Object[])
     * @see #executeOnExecutor(Executor, Object[])
     */
    public static void execute(Runnable runnable) {
        execute(runnable, TaskPriority.DEFAULT);
    }

    /**
     * Convenience version of {@link #execute(Object...)} for use with
     * a simple Runnable object. See {@link #execute(Object[])} for more
     * information on the order of execution.
     *
     * @see #execute(Object[])
     * @see #executeOnExecutor(Executor, Object[])
     */
    public static void execute(Runnable runnable, TaskPriority priority) {
        sDefaultExecutor.execute(new TaskPriorityRunnable(priority,null, runnable));
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public void setCallbackOnBg(boolean callbackOnBg) {
        this.callbackOnBg = callbackOnBg;
    }

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    private Result postResult(Result result) {
        @SuppressWarnings("unchecked")
        Message message = S_HANDLER.obtainMessage(MESSAGE_POST_RESULT,
                new AsyncTaskResult<Result>(this, result));
        if (!callbackOnBg) {
            message.sendToTarget();
        } else {
            S_HANDLER.handleMessage(message);
        }
        return result;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPostExecute
     * @see #publishProgress
     */
    protected abstract Result doInBackground(Params... params);

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p/>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param result The result of the operation computed by {@link #doInBackground}.
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    protected void onPostExecute(Result result) {
    }

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     *
     * @param values The values indicating progress.
     * @see #publishProgress
     * @see #doInBackground
     */
    protected void onProgressUpdate(Progress... values) {
    }

    /**
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.</p>
     * <p/>
     * <p>The default implementation simply invokes {@link #onCancelled()} and
     * ignores the result. If you write your own implementation, do not call
     * <code>super.onCancelled(result)</code>.</p>
     *
     * @param result The result, if any, computed in
     *               {@link #doInBackground(Object[])}, can be null
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    protected void onCancelled(Result result) {
        onCancelled();
    }

    /**
     * <p>Applications should preferably override {@link #onCancelled(Object)}.
     * This method is invoked by the default implementation of
     * {@link #onCancelled(Object)}.</p>
     * <p/>
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.</p>
     *
     * @see #onCancelled(Object)
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    protected void onCancelled() {
    }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally. If you are calling {@link #cancel(boolean)} on the task,
     * the value returned by this method should be checked periodically from
     * {@link #doInBackground(Object[])} to trace_end the task as soon as possible.
     *
     * @return <tt>true</tt> if task was cancelled before it completed
     * @see #cancel(boolean)
     */
    @Override
    public final boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete.
     * @return <tt>false</tt> if the task could not be cancelled,
     * typically because it has already completed normally;
     * <tt>true</tt> otherwise
     * @see #isCancelled()
     * @see #onCancelled(Object)
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        return mFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean supportPause() {
        return false;
    }

    @Override
    public boolean supportResume() {
        return false;
    }

    @Override
    public boolean supportCancel() {
        return true;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void cancel() {
        this.cancel(true);
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return The computed result.
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException    If the computation threw an exception.
     * @throws InterruptedException                       If the current thread was interrupted
     *                                                    while waiting.
     */
    public final Result get() throws InterruptedException, ExecutionException {
        return mFuture.get();
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result.
     *
     * @param timeout Time to wait before cancelling the operation.
     * @param unit    The time unit for the timeout.
     * @return The computed result.
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException    If the computation threw an exception.
     * @throws InterruptedException                       If the current thread was interrupted
     *                                                    while waiting.
     * @throws TimeoutException      If the wait timed out.
     */
    public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return mFuture.get(timeout, unit);
    }

    /**
     * @param params The parameters of the task.
     * @return This instance of AsyncTask.
     * @throws IllegalStateException If execute has invoked.
     * @see #executeOnExecutor(Executor, Object[])
     * @see #execute(Runnable)
     */
    public final AbstractPriorityAsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(sDefaultExecutor, params);
    }

    /**
     * @param exec   The executor to use.
     * @param params The parameters of the task.
     * @return This instance of AsyncTask.
     * @throws IllegalStateException If execute has invoked.
     * @see #execute(Object[])
     */
    public final AbstractPriorityAsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
                                                                                       Params... params) {
        if (mExecuteInvoked) {
            throw new IllegalStateException("Cannot execute task: the task is already executed.");
        }

        mExecuteInvoked = true;

        mWorker.mParams = params;
        exec.execute(new TaskPriorityRunnable(priority,null, mFuture));

        return this;
    }

    /**
     * This method can be invoked from {@link #doInBackground} to
     * publish updates on the UI thread while the background computation is
     * still running. Each call to this method will trigger the execution of
     * {@link #onProgressUpdate} on the UI thread.
     * <p/>
     * {@link #onProgressUpdate} will note be called if the task has been
     * canceled.
     *
     * @param values The progress values to update the UI with.
     * @see #onProgressUpdate
     * @see #doInBackground
     */
    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            Message msg = S_HANDLER.obtainMessage(MESSAGE_POST_PROGRESS,
                    new AsyncTaskResult<Progress>(this, values));
            if (!callbackOnBg) {
                msg.sendToTarget();
            } else {
                S_HANDLER.handleMessage(msg);
            }
        }
    }

    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            onPostExecute(result);
        }
    }

    private static class InternalHandler extends Handler {

        private InternalHandler() {
            super(Looper.getMainLooper());
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult<?> result = (AsyncTaskResult<?>) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    // There is only one result
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
                default:
                    break;
            }
        }
    }

    private static abstract class AbstractWorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }

    @SuppressWarnings("rawtypes")
    private static class AsyncTaskResult<Data> {
        final AbstractPriorityAsyncTask mTask;
        final Data[] mData;

        AsyncTaskResult(AbstractPriorityAsyncTask task, Data... data) {
            mTask = task;
            mData = data;
        }
    }
}
