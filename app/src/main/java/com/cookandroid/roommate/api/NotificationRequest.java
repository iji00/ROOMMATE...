package com.cookandroid.roommate.api;

public class NotificationRequest {
    private String receiverId;
    private String message;

    public NotificationRequest(String receiverId, String message) {
        this.receiverId = receiverId;
        this.message = message;
    }

    // Getter 및 Setter
    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}