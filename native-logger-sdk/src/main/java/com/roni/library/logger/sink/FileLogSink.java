package com.roni.library.logger.sink;

import com.roni.library.contracts.logger.LogEntry;
import com.roni.library.logger.formatter.ILogFormatter;
import com.roni.library.logger.formatter.PatternLogFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Rolling file sink with byte-size caps and automatic log rotation.
 */
public class FileLogSink implements ILogSink {

    private final String name;
    private final File logDirectory;
    private final long maxFileSize;
    private final int maxBackupIndex;
    private final ILogFormatter formatter;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    private File currentLogFile;
    private BufferedWriter writer;
    private long currentFileLength = 0;

    public FileLogSink(File logDirectory) {
        this(logDirectory, 5 * 1024 * 1024L, 5, new PatternLogFormatter());
    }

    public FileLogSink(File logDirectory, long maxFileSize, int maxBackupIndex, ILogFormatter formatter) {
        this.name = "FileLogSink";
        this.logDirectory = logDirectory;
        this.maxFileSize = maxFileSize > 0 ? maxFileSize : 5 * 1024 * 1024L;
        this.maxBackupIndex = Math.max(1, maxBackupIndex);
        this.formatter = formatter != null ? formatter : new PatternLogFormatter();
        initWriter();
    }

    private synchronized void initWriter() {
        if (logDirectory == null) {
            return;
        }
        try {
            if (!logDirectory.exists()) {
                logDirectory.mkdirs();
            }
            if (currentLogFile == null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                String dateStr = sdf.format(new Date());
                currentLogFile = new File(logDirectory, "app-log-" + dateStr + ".log");
            }
            if (currentLogFile.exists()) {
                currentFileLength = currentLogFile.length();
            } else {
                currentFileLength = 0;
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(currentLogFile, true), StandardCharsets.UTF_8));
        } catch (Throwable t) {
            writer = null;
        }
    }

    private synchronized void rotateIfNeeded() {
        if (currentFileLength >= maxFileSize) {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                    writer = null;
                }
                for (int i = maxBackupIndex - 1; i >= 1; i--) {
                    File src = new File(logDirectory, currentLogFile.getName() + "." + i);
                    if (src.exists()) {
                        File dest = new File(logDirectory, currentLogFile.getName() + "." + (i + 1));
                        if (dest.exists()) dest.delete();
                        src.renameTo(dest);
                    }
                }
                File backup1 = new File(logDirectory, currentLogFile.getName() + ".1");
                if (backup1.exists()) backup1.delete();
                currentLogFile.renameTo(backup1);

                initWriter();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized void write(LogEntry entry) {
        if (!enabled.get() || disposed.get() || entry == null) {
            return;
        }
        if (writer == null) {
            initWriter();
            if (writer == null) return;
        }
        try {
            String formatted = formatter.format(entry);
            writer.write(formatted);
            writer.newLine();
            byte[] bytes = formatted.getBytes(StandardCharsets.UTF_8);
            currentFileLength += bytes.length + 1;
            rotateIfNeeded();
        } catch (Throwable t) {
            // silent fail on disk error
        }
    }

    @Override
    public synchronized void flush() {
        if (writer != null) {
            try {
                writer.flush();
            } catch (Throwable ignored) {
            }
        }
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
    public synchronized void dispose() {
        disposed.set(true);
        enabled.set(false);
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (Throwable ignored) {
            }
            writer = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }

    public synchronized File getCurrentLogFile() {
        return currentLogFile;
    }

    public File getLogDirectory() {
        return logDirectory;
    }
}
