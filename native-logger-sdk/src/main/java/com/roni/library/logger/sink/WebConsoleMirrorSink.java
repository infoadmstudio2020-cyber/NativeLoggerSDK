package com.roni.library.logger.sink;

import com.roni.library.contracts.logger.LogEntry;
import com.roni.library.logger.formatter.ILogFormatter;
import com.roni.library.logger.formatter.PatternLogFormatter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Real-time web console mirroring sink for DevTools and WebBridge streaming.
 */
public class WebConsoleMirrorSink implements ILogSink {

    public interface WebLogListener {
        void onLog(String formattedLog, LogEntry entry);
    }

    private final String name;
    private final ILogFormatter formatter;
    private final List<WebLogListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public WebConsoleMirrorSink() {
        this(new PatternLogFormatter());
    }

    public WebConsoleMirrorSink(ILogFormatter formatter) {
        this.name = "WebConsoleMirrorSink";
        this.formatter = formatter != null ? formatter : new PatternLogFormatter();
    }

    public void addListener(WebLogListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(WebLogListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void write(LogEntry entry) {
        if (!enabled.get() || disposed.get() || entry == null || listeners.isEmpty()) {
            return;
        }
        String formatted = formatter.format(entry);
        for (WebLogListener listener : listeners) {
            try {
                listener.onLog(formatted, entry);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void flush() {
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
        listeners.clear();
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
