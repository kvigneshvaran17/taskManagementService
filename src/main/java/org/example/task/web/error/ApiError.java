package org.example.task.web.error;

import java.util.Map;

public class ApiError {
    private final String message;
    private final Map<String, String> details;

    public ApiError(String message, Map<String, String> details) {
        this.message = message;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
