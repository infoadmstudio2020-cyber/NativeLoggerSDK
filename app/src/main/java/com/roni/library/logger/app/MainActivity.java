package com.roni.library.logger.app;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.roni.library.contracts.logger.LogLevel;
import com.roni.library.logger.NativeLogger;
import com.roni.library.logger.NativeLoggerSDK;
import com.roni.library.logger.NativeLoggerSDKConfig;
import com.roni.library.logger.sink.WebConsoleMirrorSink;

import java.io.File;

/**
 * Main Activity demonstrating NativeLoggerSDK live functionality.
 */
public class MainActivity extends AppCompatActivity {

    private TextView statusView;
    private TextView consoleView;
    private StringBuilder consoleBuffer = new StringBuilder();
    private WebConsoleMirrorSink mirrorSink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = findViewById(R.id.tv_status);
        consoleView = findViewById(R.id.tv_console);
        consoleView.setMovementMethod(new ScrollingMovementMethod());

        File logDir = new File(getFilesDir(), "logs");

        mirrorSink = new WebConsoleMirrorSink();
        mirrorSink.addListener((formattedLog, entry) -> runOnUiThread(() -> {
            consoleBuffer.append(formattedLog).append("\n");
            consoleView.setText(consoleBuffer.toString());
        }));

        NativeLoggerSDKConfig config = new NativeLoggerSDKConfig.Builder()
                .debugEnabled(true)
                .tag("DemoApp")
                .minimumLevel(LogLevel.DEBUG)
                .enableFileLogging(true, logDir)
                .enableConsoleMirroring(true)
                .enablePiiMasking(true)
                .addSink(mirrorSink)
                .build();

        NativeLoggerSDK.getInstance().init(getApplicationContext(), config);

        boolean initialized = NativeLoggerSDK.getInstance().isInitialized();
        statusView.setText("NativeLoggerSDK State: " + NativeLoggerSDK.getInstance().getState() +
                "\nInitialized: " + initialized +
                "\nLog Directory: " + logDir.getAbsolutePath());

        NativeLoggerSDK.i("DemoApp", "Welcome to NativeLoggerSDK Interactive Demo!");

        Button btnInfo = findViewById(R.id.btn_log_info);
        btnInfo.setOnClickListener(v -> NativeLoggerSDK.i("DemoApp", "Informational event triggered at " + System.currentTimeMillis()));

        Button btnWarn = findViewById(R.id.btn_log_warn);
        btnWarn.setOnClickListener(v -> NativeLoggerSDK.w("DemoApp", "Warning: High memory or network latency simulated"));

        Button btnDebug = findViewById(R.id.btn_log_debug);
        btnDebug.setOnClickListener(v -> NativeLoggerSDK.d("DemoApp", "Debug payload: internal state verified OK"));

        Button btnError = findViewById(R.id.btn_log_error);
        btnError.setOnClickListener(v -> NativeLoggerSDK.e("DemoApp", "Simulated error event", new RuntimeException("DemoException")));

        Button btnFlush = findViewById(R.id.btn_flush);
        btnFlush.setOnClickListener(v -> {
            NativeLogger.getInstance().flush();
            NativeLoggerSDK.i("DemoApp", "Sinks flushed successfully.");
        });

        Button btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> {
            consoleBuffer.setLength(0);
            consoleView.setText("");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mirrorSink != null) {
            mirrorSink.dispose();
        }
    }
}
