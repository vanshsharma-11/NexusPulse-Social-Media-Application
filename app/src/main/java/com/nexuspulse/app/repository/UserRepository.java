package com.nexuspulse.app.repository;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexuspulse.app.models.User;
import com.nexuspulse.app.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Create new user in Firestore
     */
    public void createUser(User user, OnUserCreatedListener listener) {
        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get user by ID
     */
    public void getUserById(String userId, OnUserFetchedListener listener) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get user by username
     */
    public void getUserByUsername(String username, OnUserFetchedListener listener) {
        db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        User user = querySnapshot.getDocuments().get(0).toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user by username", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Update user profile
     */
    public void updateUser(String userId, Map<String, Object> updates, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Follow a user
     */
    public void followUser(String currentUserId, String targetUserId, OnUpdateListener listener) {
        // Update current user's following list
        db.collection(Constants.COLLECTION_USERS)
                .document(currentUserId)
                .update("following", FieldValue.arrayUnion(targetUserId),
                        "followingCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {

                    // Update target user's followers list
                    db.collection(Constants.COLLECTION_USERS)
                            .document(targetUserId)
                            .update("followers", FieldValue.arrayUnion(currentUserId),
                                    "followersCount", FieldValue.increment(1))
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "User followed successfully");
                                listener.onSuccess();
                            })

                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating followers", e);
                                listener.onFailure(e.getMessage());
                            });
                })

                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating following", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Unfollow a user
     */
    public void unfollowUser(String currentUserId, String targetUserId, OnUpdateListener listener) {
        // Update current user's following list
        db.collection(Constants.COLLECTION_USERS)
                .document(currentUserId)
                .update("following", FieldValue.arrayRemove(targetUserId),
                        "followingCount", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    // Update target user's followers list
                    db.collection(Constants.COLLECTION_USERS)
                            .document(targetUserId)
                            .update("followers", FieldValue.arrayRemove(currentUserId),
                                    "followersCount", FieldValue.increment(-1))
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "User unfollowed successfully");
                                listener.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating followers", e);
                                listener.onFailure(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating following", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Check if username exists
     */
    public void checkUsernameExists(String username, OnUsernameCheckListener listener) {
        db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listener.onResult(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking username", e);
                    listener.onResult(false);
                });
    }

    /**
     * Search users by username
     */
    public void searchUsers(String query, OnUsersListFetchedListener listener) {
        db.collection(Constants.COLLECTION_USERS)
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<User> users = new java.util.ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    listener.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching users", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Callback Interfaces
    public interface OnUserCreatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserFetchedListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUsernameCheckListener {
        void onResult(boolean exists);
    }

    public interface OnUsersListFetchedListener {
        void onSuccess(java.util.List<User> users);
        void onFailure(String error);
    }
}