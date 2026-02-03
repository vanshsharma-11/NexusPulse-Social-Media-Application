package com.nexuspulse.app.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String notificationId;
    private String type; // "like", "retweet", "comment", "follow", "new_post"
    private String fromUserId;
    private String fromUsername;
    private String fromUserImage;
    private String postId; // null for follow notifications
    private String message;
    private boolean isRead;

    @ServerTimestamp
    private Date createdAt;

    // Default Constructor
    public Notification() {
        this.isRead = false;
    }

    // Constructor with parameters
    public Notification(String notificationId, String type, String fromUserId,
                        String fromUsername, String fromUserImage, String message) {
        this();
        this.notificationId = notificationId;
        this.type = type;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.fromUserImage = fromUserImage;
        this.message = message;
    }

    // Getters
    public String getNotificationId() { return notificationId; }
    public String getType() { return type; }
    public String getFromUserId() { return fromUserId; }
    public String getFromUsername() { return fromUsername; }
    public String getFromUserImage() { return fromUserImage; }
    public String getPostId() { return postId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public void setType(String type) { this.type = type; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }
    public void setFromUserImage(String fromUserImage) { this.fromUserImage = fromUserImage; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setMessage(String message) { this.message = message; }
    public void setRead(boolean read) { isRead = read; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
