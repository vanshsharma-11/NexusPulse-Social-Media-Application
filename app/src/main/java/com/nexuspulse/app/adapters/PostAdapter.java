package com.nexuspulse.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.nexuspulse.app.ProfileActivity;
import android.util.Log;    // For Log.d()
import java.util.Date;      // For Date class
import com.nexuspulse.app.utils.DateUtil;  // For your DateUtil.getTimeAgo(Date)



import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.nexuspulse.app.CommentsActivity;
import com.nexuspulse.app.FullScreenImageActivity;
import com.nexuspulse.app.R;
import com.nexuspulse.app.models.Post;
import com.nexuspulse.app.repository.PostRepository;
import com.nexuspulse.app.utils.DateUtil;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private String currentUserId;
    private PostRepository postRepository;

    public PostAdapter(Context context, List<Post> postList, String currentUserId) {
        this.context = context;
        this.postList = postList;
        this.currentUserId = currentUserId;
        this.postRepository = new PostRepository();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Set text data
        holder.usernameText.setText(post.getUsername());
        // Add this for the badge:
        if (post.isUserVerified()) {
            holder.ivVerifiedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.ivVerifiedBadge.setVisibility(View.GONE);
        }
        holder.handleText.setText("@" + post.getUsername());
        holder.tweetContent.setText(post.getContent());
        holder.timeText.setText(" · " + DateUtil.getTimeAgo(post.getCreatedAt()));
        // Debug: log createdAt value for diagnosis
        Log.d("PostTimeDebug", "PostId: " + post.getPostId() + ", createdAt: " + post.getCreatedAt());

        Date createdAt = post.getCreatedAt();
        if (createdAt == null || createdAt.getTime() < 100000) {
            holder.timeText.setText(" · Just now");
        } else {
            holder.timeText.setText(" · " + DateUtil.getTimeAgo(createdAt));
        }




        // Show/hide like count
        int likesCount = post.getLikesCount();
        if (likesCount > 0) {
            holder.likeCount.setVisibility(View.VISIBLE);
            holder.likeCount.setText(String.valueOf(likesCount));
        } else {
            holder.likeCount.setVisibility(View.GONE);
        }

        // Show/hide retweet count
        int retweetsCount = post.getRetweetsCount();
        if (retweetsCount > 0) {
            holder.retweetCount.setVisibility(View.VISIBLE);
            holder.retweetCount.setText(String.valueOf(retweetsCount));
        } else {
            holder.retweetCount.setVisibility(View.GONE);
        }

        // Show/hide reply count
        int commentsCount = post.getCommentsCount();
        if (commentsCount > 0) {
            holder.replyCount.setVisibility(View.VISIBLE);
            holder.replyCount.setText(String.valueOf(commentsCount));
        } else {
            holder.replyCount.setVisibility(View.GONE);
        }

        // Load profile image
        Glide.with(context)
                .load(post.getUserProfileImage())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.userProfileImage);

        // ✅ NEW CODE: Navigate to user profile on avatar click
        View.OnClickListener profileClickListener = v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", post.getUserId());
            intent.putExtra("username", post.getUsername());
            context.startActivity(intent);
        };

        // Set click listeners on profile image, username, and handle
        holder.userProfileImage.setOnClickListener(profileClickListener);
        holder.usernameText.setOnClickListener(profileClickListener);
        holder.handleText.setOnClickListener(profileClickListener);

        // Load post image if exists
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.imageContainer.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.postImage);
            holder.postImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("IMAGE_URL", post.getImageUrl());
                context.startActivity(intent);
            });
        } else {
            holder.imageContainer.setVisibility(View.GONE);
        }

        // Set initial UI states for likes and retweets from post
        updateLikeUI(holder, post.isLikedByUser(currentUserId));
        updateRetweetUI(holder, post.isRetweetedByUser(currentUserId));

        // Like button click
        holder.likeButton.setOnClickListener(v -> {
            boolean currentlyLiked = post.isLikedByUser(currentUserId);
            if (currentlyLiked) {
                unlikePost(post, holder);
            } else {
                likePost(post, holder);
            }
        });

        // Retweet button click
        holder.retweetButton.setOnClickListener(v -> {
            boolean currentlyRetweeted = post.isRetweetedByUser(currentUserId);
            if (currentlyRetweeted) {
                undoRetweet(post, holder);
            } else {
                retweetPost(post, holder);
            }
        });

        // Reply/Comments button click
        holder.replyButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("postId", post.getPostId());
            context.startActivity(intent);
        });

        // Show Delete button only for author
        if (post.getUserId().equals(currentUserId)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Delete button click
        holder.deleteButton.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Post?")
                    .setMessage("Are you sure you want to delete this post? This can't be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            postRepository.deletePost(post.getPostId(), post.getUserId(), new PostRepository.OnUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    postList.remove(adapterPosition);
                                    notifyItemRemoved(adapterPosition);
                                    Toast.makeText(context, "Post deleted.", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(context, "Failed to delete post: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void updateLikeUI(PostViewHolder holder, boolean isLiked) {
        if (isLiked) {
            holder.likeIcon.setImageResource(R.drawable.ic_like_filled);
            holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.like_pink));
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_like);
            holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.gray_text));
        }
    }

    private void updateRetweetUI(PostViewHolder holder, boolean isRetweeted) {
        if (isRetweeted) {
            holder.retweetIcon.setColorFilter(context.getResources().getColor(R.color.retweet_green));
        } else {
            holder.retweetIcon.setColorFilter(context.getResources().getColor(R.color.gray_text));
        }
    }

    private void likePost(Post post, PostViewHolder holder) {
        holder.likeIcon.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(150)
                .withEndAction(() -> postRepository.likePost(post.getPostId(), currentUserId, new PostRepository.OnUpdateListener() {
                    @Override
                    public void onSuccess() {
                        post.getLikedBy().add(currentUserId);
                        post.setLikesCount(post.getLikesCount() + 1);
                        updateLikeUI(holder, true);
                        holder.likeCount.setText(String.valueOf(post.getLikesCount()));
                        holder.likeCount.setVisibility(View.VISIBLE);
                        holder.likeIcon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                        holder.likeIcon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();
                    }
                }))
                .start();
    }

    private void unlikePost(Post post, PostViewHolder holder) {
        holder.likeIcon.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(150)
                .withEndAction(() -> postRepository.unlikePost(post.getPostId(), currentUserId, new PostRepository.OnUpdateListener() {
                    @Override
                    public void onSuccess() {
                        post.getLikedBy().remove(currentUserId);
                        post.setLikesCount(post.getLikesCount() - 1);
                        updateLikeUI(holder, false);
                        if (post.getLikesCount() > 0) {
                            holder.likeCount.setText(String.valueOf(post.getLikesCount()));
                        } else {
                            holder.likeCount.setVisibility(View.GONE);
                        }
                        holder.likeIcon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                        holder.likeIcon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();
                    }
                }))
                .start();
    }

    private void retweetPost(Post post, PostViewHolder holder) {
        postRepository.retweetPost(post.getPostId(), currentUserId, new PostRepository.OnUpdateListener() {
            @Override
            public void onSuccess() {
                post.getRetweetedBy().add(currentUserId);
                post.setRetweetsCount(post.getRetweetsCount() + 1);
                updateRetweetUI(holder, true);
                holder.retweetCount.setText(String.valueOf(post.getRetweetsCount()));
                holder.retweetCount.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Retweeted!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void undoRetweet(Post post, PostViewHolder holder) {
        postRepository.undoRetweet(post.getPostId(), currentUserId, new PostRepository.OnUpdateListener() {
            @Override
            public void onSuccess() {
                post.getRetweetedBy().remove(currentUserId);
                post.setRetweetsCount(post.getRetweetsCount() - 1);
                updateRetweetUI(holder, false);
                if (post.getRetweetsCount() > 0) {
                    holder.retweetCount.setText(String.valueOf(post.getRetweetsCount()));
                } else {
                    holder.retweetCount.setVisibility(View.GONE);
                }
                Toast.makeText(context, "Retweet removed", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView userProfileImage;
        TextView usernameText, handleText, tweetContent, timeText;
        TextView likeCount, retweetCount, replyCount;
        LinearLayout likeButton, retweetButton, replyButton;
        ImageView likeIcon, retweetIcon;
        ImageButton deleteButton;
        CardView imageContainer;
        ImageView postImage;
        ImageView ivVerifiedBadge;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            ivVerifiedBadge = itemView.findViewById(R.id.ivVerifiedBadge);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            usernameText = itemView.findViewById(R.id.usernameText);
            handleText = itemView.findViewById(R.id.handleText);
            tweetContent = itemView.findViewById(R.id.tweetContent);
            timeText = itemView.findViewById(R.id.timeText);

            likeCount = itemView.findViewById(R.id.likeCount);
            retweetCount = itemView.findViewById(R.id.retweetCount);
            replyCount = itemView.findViewById(R.id.replyCount);

            likeButton = itemView.findViewById(R.id.likeButton);
            retweetButton = itemView.findViewById(R.id.retweetButton);
            replyButton = itemView.findViewById(R.id.replyButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            likeIcon = itemView.findViewById(R.id.likeIcon);
            retweetIcon = itemView.findViewById(R.id.retweetIcon);

            imageContainer = itemView.findViewById(R.id.imageContainer);
            postImage = itemView.findViewById(R.id.postImage);
        }
    }

    public void updatePosts(List<Post> newPosts) {
        this.postList = newPosts;
        notifyDataSetChanged();
    }
}
