package com.roni.library.logger.formatter;

import com.roni.library.contracts.logger.LogEntry;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * JSON log formatter generating structured log representation.
 */
public class JsonLogFormatter implements ILogFormatter {

    @Override
    public String format(LogEntry entry) {
        if (entry == null) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append("{");
        sb.append("\"timestamp\":").append(entry.getTimestamp()).append(",");
        sb.append("\"level\":\"").append(escapeJson(entry.getLevel() != null ? entry.getLevel().name() : "INFO")).append("\",");
        sb.append("\"tag\":\"").append(escapeJson(entry.getTag() != null ? entry.getTag() : "")).append("\",");
        sb.append("\"message\":\"").append(escapeJson(entry.getMessage() != null ? entry.getMessage() : "")).append("\",");
        sb.append("\"threadName\":\"").append(escapeJson(entry.getThreadName() != null ? entry.getThreadName() : "")).append("\"");

        if (entry.getCorrelationId() != null && !entry.getCorrelationId().isEmpty()) {
            sb.append(",\"correlationId\":\"").append(escapeJson(entry.getCorrelationId())).append("\"");
        }

        if (entry.getThrowable() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            entry.getThrowable().printStackTrace(pw);
            pw.flush();
            sb.append(",\"stackTrace\":\"").append(escapeJson(sw.toString())).append("\"");
        }

        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        StringBuilder out = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 32) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }
}
