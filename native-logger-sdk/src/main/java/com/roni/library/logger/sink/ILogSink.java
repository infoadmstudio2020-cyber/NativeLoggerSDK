package com.roni.library.logger.sink;

import com.roni.library.contracts.common.IDisposable;
import com.roni.library.contracts.logger.LogEntry;

/**
 * Universal destination sink interface for log events.
 */
public interface ILogSink extends IDisposable {

    /**
     * Unique identifier for this sink.
     *
     * @return sink name
     */
    String getName();

    /**
     * Dispatch a log entry to this sink.
     *
     * @param entry log entry to write
     */
    void write(LogEntry entry);

    /**
     * Flush buffered writes.
     */
    void flush();

    /**
     * Check if this sink is currently active.
     *
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Toggle sink active status.
     *
     * @param enabled true to enable, false to disable
     */
    void setEnabled(boolean enabled);
}
