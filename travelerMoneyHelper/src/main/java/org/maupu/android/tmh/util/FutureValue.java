package org.maupu.android.tmh.util;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementation of {@link Future}, allowing waiting for value to be set (from another thread).
 * Use {@link #set(Object)} to set value, {@link #get()} or {@link #get(long, TimeUnit)} to retrieve
 * value.
 * TODO: tests
 *
 * @param <T> type of awaited value
 */
public class FutureValue<T> implements Future<T> {
    private static final String TAG = FutureValue.class.getName();
    private static final long NANOS_IN_MILLI = TimeUnit.MILLISECONDS.toNanos(1);

    private volatile T value;
    private volatile boolean isDone = false;
    private volatile boolean isCanceled = false;

    /**
     * Sets value awaited by this future.
     *
     * @param value value
     */
    public synchronized void set(T value) {
        this.value = value;
        isDone = true;
        notifyAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        isCanceled = true;
        notifyAll();
        return !isDone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return isCanceled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        return isDone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized T get() {
        while (!isDone) {
            if (isCanceled) {
                return value;
            }
            try {
                wait();
            } catch (InterruptedException ignored) {
                Log.w(TAG, "We're just gonna ignore this exception: " + ignored, ignored);
            }
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized T get(long timeout, @NonNull TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        final long targetTime = System.nanoTime() + unit.toNanos(timeout);

        while (!isDone && !isCanceled) {
            try {
                final long waitTimeNanos = targetTime - System.nanoTime();
                if (waitTimeNanos <= 0) {
                    throw new TimeoutException();
                }
                wait(waitTimeNanos / NANOS_IN_MILLI, (int) (waitTimeNanos % NANOS_IN_MILLI));
            } catch (InterruptedException ignored) {
                Log.w(TAG, "We're just gonna ignore this exception: " + ignored, ignored);
            }
        }

        return value;
    }

}
