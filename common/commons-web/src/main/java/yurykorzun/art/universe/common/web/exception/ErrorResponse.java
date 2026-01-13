package yurykorzun.art.universe.common.web.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response format
 */
@Getter
public class ErrorResponse {
    private final String message;
    private final Map<String, String> errors;

    public ErrorResponse(String message) {
        this.message = message;
        this.errors = new HashMap<>();
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors != null ? errors : new HashMap<>();
    }
}
