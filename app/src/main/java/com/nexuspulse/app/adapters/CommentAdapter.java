package com.nexuspulse.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexuspulse.app.R;
import com.nexuspulse.app.models.Comment;
import com.nexuspulse.app.utils.DateUtil;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private static final String TAG = "CommentAdapter";

    private Context context;
    private List<Comment> commentList;
    private String currentUserId;
    private FirebaseFirestore db;

    public CommentAdapter(Context context, List<Comment> commentList, String currentUserId) {
        this.context = context;
        this.commentList = commentList;
        this.currentUserId = currentUserId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Set comment text
        // Set comment text
        holder.tvCommentText.setText(comment.getContent());


        // Set timestamp
        if (comment.getCreatedAt() != null) {
            holder.tvCommentTime.setText(DateUtil.getTimeAgo(comment.getCreatedAt()));
        }

        // Load user info
        loadUserInfo(holder, comment.getUserId());
    }

    private void loadUserInfo(CommentViewHolder holder, String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String displayName = documentSnapshot.getString("displayName");
                        String username = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        // Set display name
                        holder.tvDisplayName.setText(displayName != null ? displayName : "Unknown User");

                        // Set username
                        holder.tvUsername.setText(username != null ? "@" + username : "@unknown");

                        // Load profile image
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(holder.ivProfileImage);
                        } else {
                            holder.ivProfileImage.setImageResource(R.drawable.ic_profile);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user info", e);
                    holder.tvDisplayName.setText("Unknown User");
                    holder.tvUsername.setText("@unknown");
                });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfileImage;
        TextView tvDisplayName;
        TextView tvUsername;
        TextView tvCommentText;
        TextView tvCommentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
        }
    }
}
