package com.nexuspulse.app.repository;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nexuspulse.app.models.Notification;
import com.nexuspulse.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Create a new notification
     */
    public void createNotification(String targetUserId, Notification notification, OnNotificationCreatedListener listener) {
        String notificationId = db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(targetUserId)
                .collection("userNotifications")
                .document().getId();

        notification.setNotificationId(notificationId);

        db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(targetUserId)
                .collection("userNotifications")
                .document(notificationId)
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification created successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating notification", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get notifications for a user
     */
    public void getUserNotifications(String userId, OnNotificationsListFetchedListener listener) {
        db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(userId)
                .collection("userNotifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notifications.add(notification);
                        }
                    }
                    listener.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching notifications", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String userId, String notificationId, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(userId)
                .collection("userNotifications")
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification marked as read");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking notification as read", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Callback Interfaces
    public interface OnNotificationCreatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnNotificationsListFetchedListener {
        void onSuccess(List<Notification> notifications);
        void onFailure(String error);
    }

    public interface OnUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }
}
