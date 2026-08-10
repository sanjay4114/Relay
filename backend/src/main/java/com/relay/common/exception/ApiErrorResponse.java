package com.relay.common.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(
            Instant timestamp,
            int status,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(timestamp, status, code, message, path, fieldErrors);
    }

    public static ApiErrorResponse of(
            Instant timestamp,
            int status,
            String code,
            String message,
            String path
    ) {
        return new ApiErrorResponse(timestamp, status, code, message, path, null);
    }
}
