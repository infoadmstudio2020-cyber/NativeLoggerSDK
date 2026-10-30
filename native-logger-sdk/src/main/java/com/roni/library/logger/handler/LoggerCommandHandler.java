package com.roni.library.logger.handler;

import com.roni.library.contracts.api.IApiCallback;
import com.roni.library.contracts.api.IApiHandler;
import com.roni.library.contracts.api.NativeApiError;
import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
import com.roni.library.contracts.logger.LogLevel;
import com.roni.library.logger.NativeLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC Command Handler routing logger management actions via NativeRouterSDK.
 */
public class LoggerCommandHandler implements IApiHandler {

    private static final String ROUTE_PREFIX = "logger";
    private static final String PATH = "/logger";
    private static final String ACTION = "*";

    public String getRoutePrefix() {
        return ROUTE_PREFIX;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public NativeApiResponse handleRequest(NativeApiRequest request) {
        if (request == null) {
            return NativeApiResponse.error("unknown", new NativeApiError("INVALID_REQUEST", "Request cannot be null"));
        }
        String requestId = request.getRequestId();
        String action = request.getAction();

        try {
            NativeLogger logger = NativeLogger.getInstance();
            Map<String, Object> data = new HashMap<>();

            if ("log".equalsIgnoreCase(action)) {
                String levelStr = request.getStringParam("level", "INFO");
                String tag = request.getStringParam("tag", "RPC");
                String message = request.getStringParam("message", "");
                LogLevel level = LogLevel.fromString(levelStr, LogLevel.INFO);

                switch (level) {
                    case VERBOSE: logger.v(tag, message); break;
                    case DEBUG: logger.d(tag, message); break;
                    case INFO: logger.i(tag, message); break;
                    case WARN: logger.w(tag, message); break;
                    case ERROR: logger.e(tag, message); break;
                    default: break;
                }
                data.put("status", "logged");
                data.put("tag", tag);
                return NativeApiResponse.success(requestId, data);

            } else if ("setLevel".equalsIgnoreCase(action) || "setMinimumLevel".equalsIgnoreCase(action)) {
                String levelStr = request.getStringParam("level", "INFO");
                LogLevel level = LogLevel.fromString(levelStr, LogLevel.INFO);
                logger.setMinimumLevel(level);
                data.put("currentLevel", level.name());
                return NativeApiResponse.success(requestId, data);

            } else if ("getLevel".equalsIgnoreCase(action) || "getMinimumLevel".equalsIgnoreCase(action)) {
                data.put("currentLevel", logger.getMinimumLevel().name());
                return NativeApiResponse.success(requestId, data);

            } else if ("flush".equalsIgnoreCase(action)) {
                logger.flush();
                data.put("flushed", true);
                return NativeApiResponse.success(requestId, data);

            } else {
                return NativeApiResponse.error(requestId, 404, new NativeApiError("UNKNOWN_ACTION", "Unknown logger action: " + action));
            }
        } catch (Throwable t) {
            return NativeApiResponse.error(requestId, 500, new NativeApiError("EXECUTION_ERROR", t.getMessage()));
        }
    }

    public void handle(NativeApiRequest request, IApiCallback callback) {
        NativeApiResponse response = handleRequest(request);
        if (callback != null) {
            if (response.isSuccess()) {
                callback.onResponse(response);
            } else {
                callback.onError(response.getError());
            }
        }
    }
}
