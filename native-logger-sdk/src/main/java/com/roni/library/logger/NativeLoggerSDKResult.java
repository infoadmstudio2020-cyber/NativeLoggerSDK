package com.roni.library.logger;

import java.io.Serializable;
import java.util.Objects;

/**
 * Standardized result wrapper for NativeLogger operations.
 *
 * @param <T> Result payload type
 */
public final class NativeLoggerSDKResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final int errorCode;

    private NativeLoggerSDKResult(boolean success, T data, String errorMessage, int errorCode) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static <T> NativeLoggerSDKResult<T> success(T data) {
        return new NativeLoggerSDKResult<>(true, data, null, 0);
    }

    public static <T> NativeLoggerSDKResult<T> success() {
        return new NativeLoggerSDKResult<>(true, null, null, 0);
    }

    public static <T> NativeLoggerSDKResult<T> error(String errorMessage, int errorCode) {
        return new NativeLoggerSDKResult<>(false, null, errorMessage, errorCode);
    }

    public static <T> NativeLoggerSDKResult<T> error(String errorMessage) {
        return new NativeLoggerSDKResult<>(false, null, errorMessage, -1);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NativeLoggerSDKResult)) return false;
        NativeLoggerSDKResult<?> that = (NativeLoggerSDKResult<?>) o;
        return success == that.success &&
                errorCode == that.errorCode &&
                Objects.equals(data, that.data) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, data, errorMessage, errorCode);
    }

    @Override
    public String toString() {
        return "NativeLoggerSDKResult{" +
                "success=" + success +
                ", data=" + data +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }
}
