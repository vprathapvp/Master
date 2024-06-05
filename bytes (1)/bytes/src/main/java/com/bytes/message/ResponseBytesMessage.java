package com.bytes.message;

import java.util.List;

public class ResponseBytesMessage {
    private String message;
    private List<String> frames;

    public ResponseBytesMessage(String message) {
        this.message = message;
    }

    public ResponseBytesMessage(String message, List<String> frames) {
        this.message = message;
        this.frames = frames;
    }

    // Getters and setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getFrames() {
        return frames;
    }

    public void setFrames(List<String> frames) {
        this.frames = frames;
    }
}

