package com.roni.library.logger;

import com.roni.library.contracts.logger.LogLevel;
import com.roni.library.logger.formatter.ILogFormatter;
import com.roni.library.logger.sink.ILogSink;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for NativeLoggerSDK initialization and runtime behavior.
 */
public final class NativeLoggerSDKConfig {

    private final boolean debugEnabled;
    private final String tag;
    private final int timeout;
    private final LogLevel minimumLevel;
    private final boolean fileLoggingEnabled;
    private final File logDirectory;
    private final boolean consoleMirroringEnabled;
    private final boolean piiMaskingEnabled;
    private final ILogFormatter customFormatter;
    private final List<ILogSink> additionalSinks;

    private NativeLoggerSDKConfig(Builder builder) {
        this.debugEnabled = builder.debugEnabled;
        this.tag = builder.tag != null ? builder.tag : "NativeLoggerSDK";
        this.timeout = builder.timeout;
        this.minimumLevel = builder.minimumLevel != null ? builder.minimumLevel : (this.debugEnabled ? LogLevel.DEBUG : LogLevel.INFO);
        this.fileLoggingEnabled = builder.fileLoggingEnabled;
        this.logDirectory = builder.logDirectory;
        this.consoleMirroringEnabled = builder.consoleMirroringEnabled;
        this.piiMaskingEnabled = builder.piiMaskingEnabled;
        this.customFormatter = builder.customFormatter;
        this.additionalSinks = Collections.unmodifiableList(new ArrayList<>(builder.additionalSinks));
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public String getTag() {
        return tag;
    }

    public int getTimeout() {
        return timeout;
    }

    public LogLevel getMinimumLevel() {
        return minimumLevel;
    }

    public boolean isFileLoggingEnabled() {
        return fileLoggingEnabled;
    }

    public File getLogDirectory() {
        return logDirectory;
    }

    public boolean isConsoleMirroringEnabled() {
        return consoleMirroringEnabled;
    }

    public boolean isPiiMaskingEnabled() {
        return piiMaskingEnabled;
    }

    public ILogFormatter getCustomFormatter() {
        return customFormatter;
    }

    public List<ILogSink> getAdditionalSinks() {
        return additionalSinks;
    }

    public static class Builder {
        private boolean debugEnabled = false;
        private String tag = "NativeLoggerSDK";
        private int timeout = 30000;
        private LogLevel minimumLevel = LogLevel.INFO;
        private boolean fileLoggingEnabled = false;
        private File logDirectory = null;
        private boolean consoleMirroringEnabled = false;
        private boolean piiMaskingEnabled = false;
        private ILogFormatter customFormatter = null;
        private final List<ILogSink> additionalSinks = new ArrayList<>();

        public Builder() {
        }

        public Builder debugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            if (debugEnabled) {
                this.minimumLevel = LogLevel.DEBUG;
            }
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder minimumLevel(LogLevel minimumLevel) {
            this.minimumLevel = minimumLevel;
            return this;
        }

        public Builder enableFileLogging(boolean enable, File directory) {
            this.fileLoggingEnabled = enable;
            this.logDirectory = directory;
            return this;
        }

        public Builder enableConsoleMirroring(boolean enable) {
            this.consoleMirroringEnabled = enable;
            return this;
        }

        public Builder enablePiiMasking(boolean enable) {
            this.piiMaskingEnabled = enable;
            return this;
        }

        public Builder customFormatter(ILogFormatter customFormatter) {
            this.customFormatter = customFormatter;
            return this;
        }

        public Builder addSink(ILogSink sink) {
            if (sink != null) {
                this.additionalSinks.add(sink);
            }
            return this;
        }

        public NativeLoggerSDKConfig build() {
            return new NativeLoggerSDKConfig(this);
        }
    }
}
