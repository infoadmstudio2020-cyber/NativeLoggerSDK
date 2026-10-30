package com.roni.library.logger.formatter;

import com.roni.library.contracts.logger.LogEntry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Standard pattern formatter with optional PII masking support.
 */
public class PatternLogFormatter implements ILogFormatter {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}(?!\\d)");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d{4}[- ]?){3}\\d{4}\\b");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|secret|token|apiKey|passwd)\\s*[:=]\\s*([\"']?[^\"'\\s,;]+[\"']?)");

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private boolean piiMaskingEnabled;

    public PatternLogFormatter() {
        this(false);
    }

    public PatternLogFormatter(boolean piiMaskingEnabled) {
        this.piiMaskingEnabled = piiMaskingEnabled;
    }

    public void setPiiMaskingEnabled(boolean enabled) {
        this.piiMaskingEnabled = enabled;
    }

    public boolean isPiiMaskingEnabled() {
        return piiMaskingEnabled;
    }

    @Override
    public String format(LogEntry entry) {
        if (entry == null) {
            return "";
        }
        String timestampStr;
        synchronized (dateFormat) {
            timestampStr = dateFormat.format(new Date(entry.getTimestamp()));
        }

        String msg = entry.getMessage() != null ? entry.getMessage() : "";
        if (piiMaskingEnabled) {
            msg = maskPII(msg);
        }

        StringBuilder sb = new StringBuilder(128);
        sb.append("[").append(timestampStr).append("] ");
        sb.append("[").append(entry.getLevel() != null ? entry.getLevel().getShortName() : "I").append("] ");
        sb.append("[").append(entry.getTag() != null ? entry.getTag() : "App").append("] ");
        if (entry.getThreadName() != null && !entry.getThreadName().isEmpty()) {
            sb.append("[").append(entry.getThreadName()).append("] ");
        }
        if (entry.getCorrelationId() != null && !entry.getCorrelationId().isEmpty()) {
            sb.append("<").append(entry.getCorrelationId()).append("> ");
        }
        sb.append(msg);

        if (entry.getThrowable() != null) {
            sb.append("\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            entry.getThrowable().printStackTrace(pw);
            pw.flush();
            sb.append(sw.toString());
        }

        return sb.toString();
    }

    public String maskPII(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String masked = EMAIL_PATTERN.matcher(text).replaceAll("***@***.***");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("***-***-****");
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll("****-****-****-****");
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1=********");
        return masked;
    }
}
