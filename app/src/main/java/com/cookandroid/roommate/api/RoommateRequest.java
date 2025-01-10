package com.cookandroid.roommate.api;

public class RoommateRequest {
    private String requesterName;
    private String receiverName;
    private long requestTime;

    public RoommateRequest(String requesterName, String receiverName) {
        this.requesterName = requesterName;
        this.receiverName = receiverName;
        this.requestTime = System.currentTimeMillis();
    }

    // Getters
    public String getRequesterName() {
        return requesterName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public long getRequestTime() {
        return requestTime;
    }

    // Setters
    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }
}