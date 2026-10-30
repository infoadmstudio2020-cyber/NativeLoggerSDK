package com.roni.library.logger;

import android.content.Context;

import com.roni.library.contracts.common.IDisposable;
import com.roni.library.contracts.common.RuntimeState;
import com.roni.library.contracts.logger.LogLevel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Public facade and lifecycle manager for NativeLoggerSDK.
 */
public final class NativeLoggerSDK implements IDisposable {

    private static volatile NativeLoggerSDK instance;

    private final AtomicReference<RuntimeState> state = new AtomicReference<>(RuntimeState.UNINITIALIZED);
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    private volatile Context applicationContext;
    private volatile NativeLoggerSDKConfig currentConfig;

    private NativeLoggerSDK() {
    }

    public static NativeLoggerSDK getInstance() {
        if (instance == null) {
            synchronized (NativeLoggerSDK.class) {
                if (instance == null) {
                    instance = new NativeLoggerSDK();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes NativeLoggerSDK with Context and Config.
     *
     * @param context Android Application Context
     * @param config  Configuration options
     * @return NativeLoggerSDKResult indicating success or failure
     */
    public synchronized NativeLoggerSDKResult<Boolean> init(Context context, NativeLoggerSDKConfig config) {
        if (initialized.get() && !disposed.get()) {
            return NativeLoggerSDKResult.success(true);
        }

        try {
            state.set(RuntimeState.INITIALIZING);
            this.applicationContext = context != null ? context.getApplicationContext() : null;
            this.currentConfig = config != null ? config : new NativeLoggerSDKConfig.Builder().build();

            // Configure underlying NativeLogger engine
            NativeLogger.getInstance().init(this.currentConfig);

            initialized.set(true);
            disposed.set(false);
            state.set(RuntimeState.READY);

            NativeLogger.getInstance().i(currentConfig.getTag(), "NativeLoggerSDK initialized successfully.");
            return NativeLoggerSDKResult.success(true);
        } catch (Throwable t) {
            state.set(RuntimeState.SUSPENDED);
            return NativeLoggerSDKResult.error("Initialization failed: " + t.getMessage(), 500);
        }
    }

    public boolean isInitialized() {
        return initialized.get() && !disposed.get();
    }

    public NativeLoggerSDKConfig getConfig() {
        return currentConfig;
    }

    public RuntimeState getState() {
        return state.get();
    }

    public Context getContext() {
        return applicationContext;
    }

    /**
     * Releases all resources and terminates current instance.
     */
    public synchronized void release() {
        dispose();
    }

    @Override
    public synchronized void dispose() {
        if (disposed.compareAndSet(false, true)) {
            state.set(RuntimeState.TERMINATED);
            initialized.set(false);
            currentConfig = null;
            applicationContext = null;

            NativeLogger.getInstance().dispose();

            synchronized (NativeLoggerSDK.class) {
                instance = null;
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }

    // Static convenience logging methods
    public static void v(String tag, String message) {
        NativeLogger.getInstance().v(tag, message);
    }

    public static void d(String tag, String message) {
        NativeLogger.getInstance().d(tag, message);
    }

    public static void i(String tag, String message) {
        NativeLogger.getInstance().i(tag, message);
    }

    public static void w(String tag, String message) {
        NativeLogger.getInstance().w(tag, message);
    }

    public static void e(String tag, String message) {
        NativeLogger.getInstance().e(tag, message);
    }

    public static void e(String tag, String message, Throwable tr) {
        NativeLogger.getInstance().e(tag, message, tr);
    }
}
