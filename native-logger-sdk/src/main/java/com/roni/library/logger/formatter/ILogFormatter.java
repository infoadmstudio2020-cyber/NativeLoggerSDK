package com.roni.library.logger.formatter;

import com.roni.library.contracts.logger.LogEntry;

/**
 * Formatter interface for transforming LogEntry models into string representations.
 */
public interface ILogFormatter {

    /**
     * Format a log entry into a displayable string.
     *
     * @param entry the log entry
     * @return formatted string
     */
    String format(LogEntry entry);
}
