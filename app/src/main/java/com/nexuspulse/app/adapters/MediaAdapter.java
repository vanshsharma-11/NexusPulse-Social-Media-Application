package com.nexuspulse.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexuspulse.app.FullScreenImageActivity;
import com.nexuspulse.app.R;
import com.nexuspulse.app.models.Post;
import com.nexuspulse.app.models.User;
import com.nexuspulse.app.utils.DateUtil;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private static final String TAG = "MediaAdapter";
    private final Context context;
    private final List<Post> mediaList;
    private final FirebaseFirestore db;

    public MediaAdapter(Context context, List<Post> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
        this.db = FirebaseFirestore.getInstance();
        Log.d(TAG, "MediaAdapter created with " + mediaList.size() + " items");
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Post post = mediaList.get(position);

        Log.d(TAG, "=== onBindViewHolder CALLED ===");
        Log.d(TAG, "Binding media at position " + position);
        Log.d(TAG, "Post ID: " + post.getPostId());
        Log.d(TAG, "Image URL: " + post.getImageUrl());

        // Load post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loading image with Glide: " + post.getImageUrl());

            Glide.with(context)
                    .load(post.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivPostImage);

            // ✅ ADD CLICK LISTENER TO OPEN FULLSCREEN IMAGE
            holder.ivPostImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("IMAGE_URL", post.getImageUrl());
                context.startActivity(intent);
            });
        } else {
            Log.d(TAG, "No image URL, hiding image");
            holder.ivPostImage.setVisibility(View.GONE);
        }

        // Set timestamp
        if (post.getCreatedAt() != null) {
            holder.tvTimestamp.setText(DateUtil.getTimeAgo(post.getCreatedAt()));
            Log.d(TAG, "Timestamp set: " + DateUtil.getTimeAgo(post.getCreatedAt()));
        }

        // Load user data
        loadUserData(post.getUserId(), holder);
    }

    private void loadUserData(String userId, MediaViewHolder holder) {
        Log.d(TAG, "Loading user data for userId: " + userId);
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            holder.tvUsername.setText(user.getUsername());
                            holder.tvUserHandle.setText("@" + user.getUsername());
                            Log.d(TAG, "User data loaded: " + user.getUsername());

                            // Load profile image
                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                Glide.with(context)
                                        .load(user.getProfileImageUrl())
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(holder.ivProfileImage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data", e);
                    holder.tvUsername.setText("Unknown User");
                    holder.tvUserHandle.setText("@unknown");
                });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount called, returning: " + mediaList.size());
        return mediaList.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProfileImage;
        TextView tvUsername;
        TextView tvUserHandle;
        TextView tvTimestamp;
        ImageView ivPostImage;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvUserHandle = itemView.findViewById(R.id.tvUserHandle);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            Log.d("MediaAdapter", "MediaViewHolder created");
        }
    }
}
