package com.nexuspulse.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.nexuspulse.app.R;
import com.nexuspulse.app.models.Comment;
import com.nexuspulse.app.utils.DateUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.ReplyViewHolder> {
    private final Context context;
    private final List<Comment> replyList;

    public RepliesAdapter(Context context, List<Comment> replyList) {
        this.context = context;
        this.replyList = replyList;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reply, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Comment reply = replyList.get(position);

        String username = reply.getUsername();

        // Check if username looks like a user ID (long random string)
        // If yes, show "Unknown" instead
        if (username == null || username.isEmpty() || username.length() > 18) {
            username = "Unknown";
        }

        holder.tvDisplayName.setText(username);
        holder.tvUsername.setText("@" + username);

        holder.tvReplyContent.setText(reply.getContent() != null ? reply.getContent() : "");

        if (reply.getCreatedAt() != null) {
            holder.tvTime.setText(DateUtil.getTimeAgo(reply.getCreatedAt()));
        } else {
            holder.tvTime.setText("");
        }

        if (reply.getUserProfileImage() != null && !reply.getUserProfileImage().isEmpty()) {
            Glide.with(context)
                    .load(reply.getUserProfileImage())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivProfileImage);
        } else {
            holder.ivProfileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProfileImage;
        TextView tvDisplayName, tvUsername, tvTime, tvReplyContent;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvReplyContent = itemView.findViewById(R.id.tvReplyContent);
        }
    }
}
