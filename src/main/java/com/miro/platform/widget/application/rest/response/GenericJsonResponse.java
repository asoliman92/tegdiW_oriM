package com.miro.platform.widget.application.rest.response;

public class GenericJsonResponse {
    private final String message;

    public GenericJsonResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
