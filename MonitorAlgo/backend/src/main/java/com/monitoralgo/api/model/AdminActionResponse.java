// src/main/java/com/monitoralgo/api/model/AdminActionResponse.java
package com.monitoralgo.api.model;

public class AdminActionResponse {
    private boolean success;
    private String message;

    public AdminActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
