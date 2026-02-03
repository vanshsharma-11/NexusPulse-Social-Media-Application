package com.nexuspulse.app.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {
    private String userId;
    private String username;
    private String email;
    private String displayName;
    private String bio;
    private String profileImageUrl;
    private String coverImageUrl;
    private List<String> followers;
    private List<String> following;
    private int followersCount;
    private int followingCount;
    private int postsCount;
    private boolean isVerified;
    private boolean isAdmin;

    @ServerTimestamp
    private Date createdAt;

    // Default Constructor (Required for Firebase)
    public User() {
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followersCount = 0;
        this.followingCount = 0;
        this.postsCount = 0;
        this.isVerified = false;
        this.isAdmin = false;
        this.bio = "";
        this.profileImageUrl = "";
        this.coverImageUrl = "";
    }

    // Constructor with parameters
    public User(String userId, String username, String email, String displayName) {
        this();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getBio() { return bio; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public List<String> getFollowers() { return followers; }
    public List<String> getFollowing() { return following; }
    public int getFollowersCount() { return followersCount; }
    public int getFollowingCount() { return followingCount; }
    public int getPostsCount() { return postsCount; }
    public boolean isVerified() { return isVerified; }
    public boolean isAdmin() { return isAdmin; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setBio(String bio) { this.bio = bio; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public void setFollowers(List<String> followers) { this.followers = followers; }
    public void setFollowing(List<String> following) { this.following = following; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }
    public void setPostsCount(int postsCount) { this.postsCount = postsCount; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
