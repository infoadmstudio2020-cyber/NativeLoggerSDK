package com.roni.library.logger;

import com.roni.library.contracts.common.IDisposable;
import com.roni.library.contracts.common.RuntimeState;
import com.roni.library.contracts.logger.ILogger;
import com.roni.library.contracts.logger.LogEntry;
import com.roni.library.contracts.logger.LogLevel;
import com.roni.library.logger.formatter.PatternLogFormatter;
import com.roni.library.logger.sink.FileLogSink;
import com.roni.library.logger.sink.ILogSink;
import com.roni.library.logger.sink.LogcatSink;
import com.roni.library.logger.sink.WebConsoleMirrorSink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NativeLogger - Multi-sink fan-out diagnostic logging engine implementing Layer 0 contracts.
 */
public class NativeLogger implements ILogger, IDisposable {

    private static volatile NativeLogger instance;

    private final AtomicReference<RuntimeState> state = new AtomicReference<>(RuntimeState.UNINITIALIZED);
    private final AtomicReference<LogLevel> minimumLevel = new AtomicReference<>(LogLevel.INFO);
    private final List<ILogSink> sinks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    private NativeLogger() {
    }

    public static NativeLogger getInstance() {
        if (instance == null) {
            synchronized (NativeLogger.class) {
                if (instance == null) {
                    instance = new NativeLogger();
                    // Default configuration: Logcat sink with INFO level
                    instance.addSink(new LogcatSink());
                    instance.state.set(RuntimeState.READY);
                }
            }
        }
        return instance;
    }

    public static synchronized NativeLogger init(LoggerConfig config) {
        NativeLogger logger = getInstance();
        logger.applyConfig(config);
        return logger;
    }

    public static synchronized NativeLogger init(NativeLoggerSDKConfig config) {
        NativeLogger logger = getInstance();
        logger.applySDKConfig(config);
        return logger;
    }

    private void applyConfig(LoggerConfig config) {
        if (config == null) return;
        state.set(RuntimeState.INITIALIZING);
        sinks.clear();

        if (config.getMinimumLevel() != null) {
            this.minimumLevel.set(config.getMinimumLevel());
        }

        PatternLogFormatter formatter = new PatternLogFormatter(config.isPiiMaskingEnabled());
        if (config.getCustomFormatter() != null) {
            sinks.add(new LogcatSink(config.getCustomFormatter()));
        } else {
            sinks.add(new LogcatSink(formatter));
        }

        if (config.isFileLoggingEnabled() && config.getLogDirectory() != null) {
            sinks.add(new FileLogSink(config.getLogDirectory(), 5 * 1024 * 1024L, 5,
                    config.getCustomFormatter() != null ? config.getCustomFormatter() : formatter));
        }

        if (config.isConsoleMirroringEnabled()) {
            sinks.add(new WebConsoleMirrorSink(
                    config.getCustomFormatter() != null ? config.getCustomFormatter() : formatter));
        }

        for (ILogSink sink : config.getAdditionalSinks()) {
            if (sink != null && !sinks.contains(sink)) {
                sinks.add(sink);
            }
        }

        state.set(RuntimeState.READY);
    }

    private void applySDKConfig(NativeLoggerSDKConfig config) {
        if (config == null) return;
        state.set(RuntimeState.INITIALIZING);
        sinks.clear();

        if (config.getMinimumLevel() != null) {
            this.minimumLevel.set(config.getMinimumLevel());
        }

        PatternLogFormatter formatter = new PatternLogFormatter(config.isPiiMaskingEnabled());
        if (config.getCustomFormatter() != null) {
            sinks.add(new LogcatSink(config.getCustomFormatter()));
        } else {
            sinks.add(new LogcatSink(formatter));
        }

        if (config.isFileLoggingEnabled() && config.getLogDirectory() != null) {
            sinks.add(new FileLogSink(config.getLogDirectory(), 5 * 1024 * 1024L, 5,
                    config.getCustomFormatter() != null ? config.getCustomFormatter() : formatter));
        }

        if (config.isConsoleMirroringEnabled()) {
            sinks.add(new WebConsoleMirrorSink(
                    config.getCustomFormatter() != null ? config.getCustomFormatter() : formatter));
        }

        for (ILogSink sink : config.getAdditionalSinks()) {
            if (sink != null && !sinks.contains(sink)) {
                sinks.add(sink);
            }
        }

        state.set(RuntimeState.READY);
    }

    @Override
    public void setMinimumLevel(LogLevel level) {
        if (level != null) {
            this.minimumLevel.set(level);
        }
    }

    @Override
    public LogLevel getMinimumLevel() {
        return minimumLevel.get();
    }

    public RuntimeState getState() {
        return state.get();
    }

    public void addSink(ILogSink sink) {
        if (sink != null && !sinks.contains(sink)) {
            sinks.add(sink);
        }
    }

    public void removeSink(ILogSink sink) {
        if (sink != null) {
            sinks.remove(sink);
        }
    }

    public List<ILogSink> getSinks() {
        return Collections.unmodifiableList(new ArrayList<>(sinks));
    }

    @Override
    public void log(LogEntry entry) {
        if (entry == null || disposed.get() || !state.get().isOperational()) {
            return;
        }

        // Fast-path guard: check minimum log level
        LogLevel currentMin = minimumLevel.get();
        if (currentMin == LogLevel.NONE || entry.getLevel() == null || !entry.getLevel().isLoggable(currentMin)) {
            return;
        }

        // Fan-out to all active sinks
        for (ILogSink sink : sinks) {
            if (sink != null && sink.isEnabled() && !sink.isDisposed()) {
                try {
                    sink.write(entry);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public void v(String tag, String message) {
        v(tag, message, null);
    }

    public void v(String tag, String message, Throwable tr) {
        dispatch(LogLevel.VERBOSE, tag, message, tr);
    }

    public void d(String tag, String message) {
        d(tag, message, null);
    }

    public void d(String tag, String message, Throwable tr) {
        dispatch(LogLevel.DEBUG, tag, message, tr);
    }

    public void i(String tag, String message) {
        i(tag, message, null);
    }

    public void i(String tag, String message, Throwable tr) {
        dispatch(LogLevel.INFO, tag, message, tr);
    }

    public void w(String tag, String message) {
        w(tag, message, null);
    }

    public void w(String tag, String message, Throwable tr) {
        dispatch(LogLevel.WARN, tag, message, tr);
    }

    public void e(String tag, String message) {
        e(tag, message, null);
    }

    public void e(String tag, String message, Throwable tr) {
        dispatch(LogLevel.ERROR, tag, message, tr);
    }

    private void dispatch(LogLevel level, String tag, String message, Throwable tr) {
        LogLevel currentMin = minimumLevel.get();
        if (currentMin == LogLevel.NONE || level == null || !level.isLoggable(currentMin)) {
            return;
        }
        LogEntry entry = new LogEntry(System.currentTimeMillis(), level, tag, message,
                Thread.currentThread().getName(), tr, null);
        log(entry);
    }

    public void flush() {
        for (ILogSink sink : sinks) {
            if (sink != null && !sink.isDisposed()) {
                try {
                    sink.flush();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (disposed.compareAndSet(false, true)) {
            state.set(RuntimeState.TERMINATED);
            for (ILogSink sink : sinks) {
                try {
                    sink.dispose();
                } catch (Throwable ignored) {
                }
            }
            sinks.clear();
            synchronized (NativeLogger.class) {
                instance = null;
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
