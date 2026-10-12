package com.monitoralgo.api.model;

public class AlgoRunResponse {
    private boolean success;
    private String message;

    public AlgoRunResponse() {
    }

    public AlgoRunResponse(boolean success, String message) {
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
