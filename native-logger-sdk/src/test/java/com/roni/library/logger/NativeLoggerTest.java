package com.roni.library.logger;

import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
import com.roni.library.contracts.common.RuntimeState;
import com.roni.library.contracts.logger.LogEntry;
import com.roni.library.contracts.logger.LogLevel;
import com.roni.library.logger.formatter.JsonLogFormatter;
import com.roni.library.logger.formatter.PatternLogFormatter;
import com.roni.library.logger.handler.LoggerCommandHandler;
import com.roni.library.logger.sink.FileLogSink;
import com.roni.library.logger.sink.ILogSink;
import com.roni.library.logger.sink.LogcatSink;
import com.roni.library.logger.sink.RemoteDiagnosticSink;
import com.roni.library.logger.sink.WebConsoleMirrorSink;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Comprehensive automated JVM unit tests for NativeLoggerSDK.
 */
public class NativeLoggerTest {

    private File tempLogDir;

    @Before
    public void setUp() {
        tempLogDir = new File(System.getProperty("java.io.tmpdir"), "test_logs_" + System.currentTimeMillis());
        tempLogDir.mkdirs();
        NativeLogger.getInstance().dispose();
    }

    @After
    public void tearDown() {
        NativeLogger.getInstance().dispose();
        if (tempLogDir != null && tempLogDir.exists()) {
            File[] files = tempLogDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            tempLogDir.delete();
        }
    }

    @Test
    public void testSingletonLifecycleAndReset() {
        NativeLogger logger1 = NativeLogger.getInstance();
        assertNotNull(logger1);
        NativeLogger logger2 = NativeLogger.getInstance();
        assertSame(logger1, logger2);

        logger1.dispose();
        assertTrue(logger1.isDisposed());

        NativeLogger logger3 = NativeLogger.getInstance();
        assertNotNull(logger3);
        assertNotSame(logger1, logger3);
    }

    @Test
    public void testLogLevelFiltering() {
        NativeLogger logger = NativeLogger.getInstance();
        logger.setMinimumLevel(LogLevel.WARN);
        assertEquals(LogLevel.WARN, logger.getMinimumLevel());

        AtomicInteger callCount = new AtomicInteger(0);
        ILogSink mockSink = new ILogSink() {
            private boolean enabled = true;
            private boolean disposed = false;
            @Override public String getName() { return "MockSink"; }
            @Override public void write(LogEntry entry) { callCount.incrementAndGet(); }
            @Override public void flush() {}
            @Override public boolean isEnabled() { return enabled; }
            @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }
            @Override public void dispose() { disposed = true; }
            @Override public boolean isDisposed() { return disposed; }
        };

        logger.addSink(mockSink);

        logger.v("TEST", "verbose msg");
        logger.d("TEST", "debug msg");
        logger.i("TEST", "info msg");
        assertEquals(0, callCount.get());

        logger.w("TEST", "warn msg");
        assertEquals(1, callCount.get());

        logger.e("TEST", "error msg");
        assertEquals(2, callCount.get());
    }

    @Test
    public void testPatternFormatterAndPiiMasking() {
        PatternLogFormatter formatter = new PatternLogFormatter(true);
        assertTrue(formatter.isPiiMaskingEnabled());

        String raw = "User email is test.user@example.com, phone 123-456-7890, card 1234-5678-9012-3456, password: secretPassword123";
        String masked = formatter.maskPII(raw);

        assertFalse(masked.contains("test.user@example.com"));
        assertFalse(masked.contains("123-456-7890"));
        assertFalse(masked.contains("1234-5678-9012-3456"));
        assertFalse(masked.contains("secretPassword123"));
        assertTrue(masked.contains("***@***.***"));
    }

    @Test
    public void testJsonLogFormatter() {
        JsonLogFormatter formatter = new JsonLogFormatter();
        LogEntry entry = new LogEntry(System.currentTimeMillis(), LogLevel.INFO, "TestTag", "Hello \"World\"", "main", null, "trace-101");

        String json = formatter.format(entry);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"tag\":\"TestTag\""));
        assertTrue(json.contains("\"level\":\"INFO\""));
        assertTrue(json.contains("\"correlationId\":\"trace-101\""));
    }

    @Test
    public void testFileLogSinkWriteAndFlush() {
        FileLogSink fileSink = new FileLogSink(tempLogDir, 1024 * 1024L, 3, new PatternLogFormatter());
        assertEquals("FileLogSink", fileSink.getName());
        assertTrue(fileSink.isEnabled());
        assertFalse(fileSink.isDisposed());

        LogEntry entry = new LogEntry(System.currentTimeMillis(), LogLevel.INFO, "FileTest", "Testing file persistence", "main", null, null);
        fileSink.write(entry);
        fileSink.flush();

        File logFile = fileSink.getCurrentLogFile();
        assertNotNull(logFile);
        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);

        fileSink.dispose();
        assertTrue(fileSink.isDisposed());
    }

    @Test
    public void testWebConsoleMirrorSink() {
        WebConsoleMirrorSink mirrorSink = new WebConsoleMirrorSink();
        AtomicInteger listenerCalls = new AtomicInteger(0);

        WebConsoleMirrorSink.WebLogListener listener = (formatted, entry) -> listenerCalls.incrementAndGet();
        mirrorSink.addListener(listener);

        LogEntry entry = new LogEntry(System.currentTimeMillis(), LogLevel.DEBUG, "MirrorTag", "Mirror message", "worker", null, null);
        mirrorSink.write(entry);
        assertEquals(1, listenerCalls.get());

        mirrorSink.removeListener(listener);
        mirrorSink.write(entry);
        assertEquals(1, listenerCalls.get());

        mirrorSink.dispose();
        assertTrue(mirrorSink.isDisposed());
    }

    @Test
    public void testRemoteDiagnosticSink() {
        RemoteDiagnosticSink remoteSink = new RemoteDiagnosticSink("https://example.com/logs", 10, new JsonLogFormatter());
        assertEquals("RemoteDiagnosticSink", remoteSink.getName());
        assertEquals(0, remoteSink.getQueueSize());

        for (int i = 0; i < 5; i++) {
            remoteSink.write(new LogEntry(System.currentTimeMillis(), LogLevel.INFO, "Remote", "Msg " + i, "main", null, null));
        }
        assertEquals(5, remoteSink.getQueueSize());

        remoteSink.dispose();
        assertEquals(0, remoteSink.getQueueSize());
        assertTrue(remoteSink.isDisposed());
    }

    @Test
    public void testLoggerCommandHandler() {
        LoggerCommandHandler handler = new LoggerCommandHandler();
        assertEquals("logger", handler.getRoutePrefix());
        assertEquals("/logger", handler.getPath());

        Map<String, Object> params = new HashMap<>();
        params.put("level", "ERROR");
        params.put("tag", "ApiTag");
        params.put("message", "Api Message");
        NativeApiRequest request = new NativeApiRequest("req-1", "/logger", "log", params, System.currentTimeMillis());

        NativeApiResponse response = handler.handleRequest(request);
        assertNotNull(response);
        assertTrue(response.isSuccess());

        // Test setMinimumLevel action
        Map<String, Object> levelParams = new HashMap<>();
        levelParams.put("level", "DEBUG");
        NativeApiRequest levelReq = new NativeApiRequest("req-2", "/logger", "setLevel", levelParams, System.currentTimeMillis());
        NativeApiResponse levelResp = handler.handleRequest(levelReq);
        assertTrue(levelResp.isSuccess());
        assertEquals(LogLevel.DEBUG, NativeLogger.getInstance().getMinimumLevel());
    }

    @Test
    public void testNativeLoggerSDKResult() {
        NativeLoggerSDKResult<String> res = NativeLoggerSDKResult.success("payload");
        assertTrue(res.isSuccess());
        assertEquals("payload", res.getData());
        assertEquals(0, res.getErrorCode());

        NativeLoggerSDKResult<Void> err = NativeLoggerSDKResult.error("Something went wrong", 404);
        assertFalse(err.isSuccess());
        assertEquals("Something went wrong", err.getErrorMessage());
        assertEquals(404, err.getErrorCode());
    }

    @Test
    public void testNativeLoggerSDKConfigBuilder() {
        NativeLoggerSDKConfig config = new NativeLoggerSDKConfig.Builder()
                .debugEnabled(true)
                .tag("CustomTag")
                .timeout(5000)
                .enableFileLogging(true, tempLogDir)
                .enableConsoleMirroring(true)
                .enablePiiMasking(true)
                .build();

        assertTrue(config.isDebugEnabled());
        assertEquals("CustomTag", config.getTag());
        assertEquals(5000, config.getTimeout());
        assertTrue(config.isFileLoggingEnabled());
        assertEquals(tempLogDir, config.getLogDirectory());
        assertTrue(config.isConsoleMirroringEnabled());
        assertTrue(config.isPiiMaskingEnabled());
    }
}
