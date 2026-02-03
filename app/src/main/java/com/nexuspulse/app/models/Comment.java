package com.nexuspulse.app.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String username;
    private String userProfileImage;
    private String content;
    private int likesCount;

    @ServerTimestamp
    private Date createdAt;

    // Default Constructor
    public Comment() {
        this.likesCount = 0;
    }

    // Constructor with parameters
    public Comment(String commentId, String postId, String userId, String username,
                   String userProfileImage, String content) {
        this();
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.userProfileImage = userProfileImage;
        this.content = content;
    }

    // Getters
    public String getCommentId() { return commentId; }
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getUserProfileImage() { return userProfileImage; }
    public String getContent() { return content; }
    public int getLikesCount() { return likesCount; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public void setContent(String content) { this.content = content; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
