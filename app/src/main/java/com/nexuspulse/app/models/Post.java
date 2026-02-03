package com.nexuspulse.app.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    private String postId;
    private String userId;
    private String username;
    private String userProfileImage;
    private boolean userVerified;// <-- Verification badge support
    private String content;
    private String imageUrl;
    private List<String> imageUrls;
    private List<String> hashtags;

    @ServerTimestamp
    private Date createdAt;
    private int likesCount;
    private int retweetsCount;
    private int commentsCount;
    private List<String> likedBy;
    private List<String> retweetedBy;

    // Empty constructor (required for Firebase)
    public Post() {
        this.likedBy = new ArrayList<>();
        this.retweetedBy = new ArrayList<>();
        this.imageUrls = new ArrayList<>();
        this.hashtags = new ArrayList<>();
    }

    // Getters and Setters

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    // --- Verification Badge Getter / Setter ---
    public boolean isUserVerified() {
        return userVerified;
    }

    public void setUserVerified(boolean userVerified) {
        this.userVerified = userVerified;
    }

    // --- End of Verification Badge ---

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    public List<String> getHashtags() {
        if (hashtags == null) {
            hashtags = new ArrayList<>();
        }
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAtMillis() {
        return createdAt != null ? createdAt.getTime() : 0;
    }

    public void setCreatedAtMillis(long millis) {
        this.createdAt = new Date(millis);
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getRetweetsCount() {
        return retweetsCount;
    }

    public void setRetweetsCount(int retweetsCount) {
        this.retweetsCount = retweetsCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public List<String> getLikedBy() {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public List<String> getRetweetedBy() {
        if (retweetedBy == null) {
            retweetedBy = new ArrayList<>();
        }
        return retweetedBy;
    }

    public void setRetweetedBy(List<String> retweetedBy) {
        this.retweetedBy = retweetedBy;
    }

    public boolean isLikedByUser(String userId) {
        return getLikedBy().contains(userId);
    }

    public boolean isRetweetedByUser(String userId) {
        return getRetweetedBy().contains(userId);
    }
}
