package com.nexuspulse.app.repository;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nexuspulse.app.models.Post;
import com.nexuspulse.app.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostRepository {
    private static final String TAG = "PostRepository";
    private final FirebaseFirestore db;

    public PostRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Create a new post
     */
    public void createPost(Post post, OnPostCreatedListener listener) {
        String postId = db.collection(Constants.COLLECTION_POSTS).document().getId();
        post.setPostId(postId);

        Log.d(TAG, "Creating post with createdAt: " + post.getCreatedAt());

        // Construct a map to explicitly set server timestamp for createdAt
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("userId", post.getUserId());
        postMap.put("username", post.getUsername());
        postMap.put("userProfileImage", post.getUserProfileImage());
        postMap.put("userVerified", post.isUserVerified());
        postMap.put("content", post.getContent());
        postMap.put("imageUrl", post.getImageUrl());
        postMap.put("hashtags", post.getHashtags());
        postMap.put("likesCount", post.getLikesCount());
        postMap.put("retweetsCount", post.getRetweetsCount());
        postMap.put("commentsCount", post.getCommentsCount());
        postMap.put("likedBy", post.getLikedBy());
        postMap.put("retweetedBy", post.getRetweetedBy());
        postMap.put("createdAt", FieldValue.serverTimestamp());  // Set server timestamp here

        db.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .set(postMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post created successfully");
                    // Increment user's post count
                    db.collection(Constants.COLLECTION_USERS)
                            .document(post.getUserId())
                            .update("postsCount", FieldValue.increment(1));
                    listener.onSuccess(postId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get post by ID
     */
    public void getPostById(String postId, OnPostFetchedListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        listener.onSuccess(post);
                    } else {
                        listener.onFailure("Post not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching post", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get all posts (timeline feed)
     */
    public void getAllPosts(OnPostsListFetchedListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Post> posts = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            posts.add(post);
                        }
                    }
                    listener.onSuccess(posts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching posts", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get posts by specific user
     */
    public void getPostsByUserId(String userId, OnPostsListFetchedListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Post> posts = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            posts.add(post);
                        }
                    }
                    listener.onSuccess(posts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user posts", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Like a post
     */
    public void likePost(String postId, String userId, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .update("likedBy", FieldValue.arrayUnion(userId),
                        "likesCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post liked successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error liking post", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Unlike a post
     */
    public void unlikePost(String postId, String userId, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .update("likedBy", FieldValue.arrayRemove(userId),
                        "likesCount", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post unliked successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error unliking post", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Retweet a post
     */
    public void retweetPost(String postId, String userId, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .update("retweetedBy", FieldValue.arrayUnion(userId),
                        "retweetsCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post retweeted successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retweeting post", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Undo retweet
     */
    public void undoRetweet(String postId, String userId, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .update("retweetedBy", FieldValue.arrayRemove(userId),
                        "retweetsCount", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Retweet undone successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error undoing retweet", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Delete a post
     */
    public void deletePost(String postId, String userId, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post deleted successfully");
                    // Decrement user's post count
                    db.collection(Constants.COLLECTION_USERS)
                            .document(userId)
                            .update("postsCount", FieldValue.increment(-1));
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting post", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Callback Interfaces
    public interface OnPostCreatedListener {
        void onSuccess(String postId);
        void onFailure(String error);
    }

    public interface OnPostCreateListener extends OnPostCreatedListener {}

    public interface OnPostFetchedListener {
        void onSuccess(Post post);
        void onFailure(String error);
    }

    public interface OnPostsListFetchedListener {
        void onSuccess(List<Post> posts);
        void onFailure(String error);
    }

    public interface OnUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }
}
