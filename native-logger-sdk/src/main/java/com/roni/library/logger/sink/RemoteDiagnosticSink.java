package com.roni.library.logger.sink;

import com.roni.library.contracts.logger.LogEntry;
import com.roni.library.logger.formatter.ILogFormatter;
import com.roni.library.logger.formatter.JsonLogFormatter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Remote diagnostic sink buffering diagnostic logs for transmission.
 */
public class RemoteDiagnosticSink implements ILogSink {

    private final String name;
    private final String endpointUrl;
    private final int maxQueueSize;
    private final ILogFormatter formatter;
    private final BlockingQueue<LogEntry> queue;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public RemoteDiagnosticSink() {
        this(null, 500, new JsonLogFormatter());
    }

    public RemoteDiagnosticSink(String endpointUrl, int maxQueueSize, ILogFormatter formatter) {
        this.name = "RemoteDiagnosticSink";
        this.endpointUrl = endpointUrl;
        this.maxQueueSize = maxQueueSize > 0 ? maxQueueSize : 500;
        this.formatter = formatter != null ? formatter : new JsonLogFormatter();
        this.queue = new LinkedBlockingQueue<>(this.maxQueueSize);
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
        if (!queue.offer(entry)) {
            // Queue full: evict oldest to make room for new diagnostic telemetry
            queue.poll();
            queue.offer(entry);
        }
    }

    @Override
    public void flush() {
        // In buffer mode, flush maintains buffer state or triggers delivery
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
        queue.clear();
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
