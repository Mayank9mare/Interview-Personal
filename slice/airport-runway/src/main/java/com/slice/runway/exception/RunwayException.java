package com.slice.runway.exception;

public class RunwayException extends RuntimeException {

    private final String runwayId;

    public RunwayException(String message, String runwayId) {
        super(message + (runwayId != null ? " [runwayId=" + runwayId + "]" : ""));
        this.runwayId = runwayId;
    }

    public RunwayException(String message) {
        this(message, null);
    }

    public String getRunwayId() { return runwayId; }
}
