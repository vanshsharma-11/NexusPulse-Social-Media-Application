package com.nexuspulse.app.repository;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nexuspulse.app.models.Comment;
import com.nexuspulse.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository {
    private static final String TAG = "CommentRepository";
    private final FirebaseFirestore db;

    public CommentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Create a new comment
     */
    public void createComment(Comment comment, OnCommentCreatedListener listener) {
        String commentId = db.collection(Constants.COLLECTION_COMMENTS).document().getId();
        comment.setCommentId(commentId);

        db.collection(Constants.COLLECTION_COMMENTS)
                .document(commentId)
                .set(comment)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment created successfully");
                    // Increment post's comment count
                    db.collection(Constants.COLLECTION_POSTS)
                            .document(comment.getPostId())
                            .update("commentsCount", FieldValue.increment(1));
                    listener.onSuccess(commentId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating comment", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get comments for a specific post
     */
    public void getCommentsByPostId(String postId, OnCommentsListFetchedListener listener) {
        db.collection(Constants.COLLECTION_COMMENTS)
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comments.add(comment);
                        }
                    }
                    listener.onSuccess(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching comments", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Delete a comment
     */
    public void deleteComment(String commentId, String postId, OnUpdateListener listener) {
        db.collection(Constants.COLLECTION_COMMENTS)
                .document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment deleted successfully");
                    // Decrement post's comment count
                    db.collection(Constants.COLLECTION_POSTS)
                            .document(postId)
                            .update("commentsCount", FieldValue.increment(-1));
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting comment", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Callback Interfaces
    public interface OnCommentCreatedListener {
        void onSuccess(String commentId);
        void onFailure(String error);
    }

    public interface OnCommentsListFetchedListener {
        void onSuccess(List<Comment> comments);
        void onFailure(String error);
    }

    public interface OnUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }
}
