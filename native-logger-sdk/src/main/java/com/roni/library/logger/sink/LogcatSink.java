package com.roni.library.logger.sink;

import com.roni.library.contracts.logger.LogEntry;
import com.roni.library.contracts.logger.LogLevel;
import com.roni.library.logger.formatter.ILogFormatter;
import com.roni.library.logger.formatter.PatternLogFormatter;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Logcat sink routing diagnostic output to android.util.Log with JVM fallback.
 */
public class LogcatSink implements ILogSink {

    private final String name;
    private final ILogFormatter formatter;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    private static final Method ANDROID_LOG_V;
    private static final Method ANDROID_LOG_D;
    private static final Method ANDROID_LOG_I;
    private static final Method ANDROID_LOG_W;
    private static final Method ANDROID_LOG_E;
    private static final boolean IS_ANDROID;

    static {
        Method v = null, d = null, i = null, w = null, e = null;
        boolean androidAvailable = false;
        try {
            Class<?> logClass = Class.forName("android.util.Log");
            v = logClass.getMethod("v", String.class, String.class);
            d = logClass.getMethod("d", String.class, String.class);
            i = logClass.getMethod("i", String.class, String.class);
            w = logClass.getMethod("w", String.class, String.class);
            e = logClass.getMethod("e", String.class, String.class);
            androidAvailable = true;
        } catch (Throwable ignored) {
            androidAvailable = false;
        }
        ANDROID_LOG_V = v;
        ANDROID_LOG_D = d;
        ANDROID_LOG_I = i;
        ANDROID_LOG_W = w;
        ANDROID_LOG_E = e;
        IS_ANDROID = androidAvailable;
    }

    public LogcatSink() {
        this(new PatternLogFormatter());
    }

    public LogcatSink(ILogFormatter formatter) {
        this.name = "LogcatSink";
        this.formatter = formatter != null ? formatter : new PatternLogFormatter();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void write(LogEntry entry) {
        if (!enabled.get() || disposed.get() || entry == null) {
            return;
        }

        String tag = entry.getTag() != null ? entry.getTag() : "NativeLogger";
        String formatted = formatter.format(entry);

        if (IS_ANDROID) {
            try {
                LogLevel level = entry.getLevel() != null ? entry.getLevel() : LogLevel.INFO;
                switch (level) {
                    case VERBOSE:
                        if (ANDROID_LOG_V != null) ANDROID_LOG_V.invoke(null, tag, formatted);
                        break;
                    case DEBUG:
                        if (ANDROID_LOG_D != null) ANDROID_LOG_D.invoke(null, tag, formatted);
                        break;
                    case INFO:
                        if (ANDROID_LOG_I != null) ANDROID_LOG_I.invoke(null, tag, formatted);
                        break;
                    case WARN:
                        if (ANDROID_LOG_W != null) ANDROID_LOG_W.invoke(null, tag, formatted);
                        break;
                    case ERROR:
                        if (ANDROID_LOG_E != null) ANDROID_LOG_E.invoke(null, tag, formatted);
                        break;
                    case NONE:
                    default:
                        break;
                }
                return;
            } catch (Throwable ignored) {
                // fallback to standard output
            }
        }

        if (entry.getLevel() == LogLevel.ERROR) {
            System.err.println(formatted);
        } else {
            System.out.println(formatted);
        }
    }

    @Override
    public void flush() {
        System.out.flush();
        System.err.flush();
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public void dispose() {
        disposed.set(true);
        enabled.set(false);
        flush();
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
