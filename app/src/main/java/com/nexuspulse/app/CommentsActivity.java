package com.nexuspulse.app;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nexuspulse.app.adapters.CommentAdapter;
import com.nexuspulse.app.models.Comment;
import com.nexuspulse.app.repository.CommentRepository; // ✅ FIXED: repository (singular)

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private String postId;
    private String currentUserId;

    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText etComment;
    private ImageButton btnSendComment;
    private ProgressBar progressBar;
    private TextView tvNoComments;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CommentRepository commentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Get post ID from intent
        postId = getIntent().getStringExtra("postId");
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        commentRepository = new CommentRepository();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Load comments
        loadComments();

        // Setup send button
        setupSendButton();
    }

    private void initializeViews() {
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        progressBar = findViewById(R.id.progressBar);
        tvNoComments = findViewById(R.id.tvNoComments);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Comments");
        }
    }

    private void setupRecyclerView() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewComments.setLayoutManager(layoutManager);
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void setupSendButton() {
        btnSendComment.setOnClickListener(v -> {
            String commentText = etComment.getText().toString().trim();

            if (commentText.isEmpty()) {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent double-click
            btnSendComment.setEnabled(false);

            // Post comment
            postComment(commentText);
        });
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoComments.setVisibility(View.GONE);

        db.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Error loading comments", error);
                        Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        commentList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Comment comment = document.toObject(Comment.class);
                            comment.setCommentId(document.getId());
                            commentList.add(comment);
                        }

                        commentAdapter.notifyDataSetChanged();

                        // Show/hide empty state
                        if (commentList.isEmpty()) {
                            tvNoComments.setVisibility(View.VISIBLE);
                        } else {
                            tvNoComments.setVisibility(View.GONE);
                            // Scroll to bottom to show latest comment
                            recyclerViewComments.scrollToPosition(commentList.size() - 1);
                        }
                    }
                });
    }

    private void postComment(String commentText) {
        // Create comment object
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(currentUserId);
        comment.setContent(commentText); // ✅ FIXED: setContent() not setText()
        comment.setCreatedAt(new Date());

        // Add to Firestore directly (since CommentRepository might not have addComment method)
        db.collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    // Clear input
                    etComment.setText("");

                    // Re-enable button
                    btnSendComment.setEnabled(true);

                    // Show success message
                    Toast.makeText(CommentsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();

                    // Update comment count in post
                    updatePostCommentCount();
                })
                .addOnFailureListener(e -> {
                    // Re-enable button
                    btnSendComment.setEnabled(true);

                    // Show error
                    Toast.makeText(CommentsActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error posting comment", e);
                });
    }

    private void updatePostCommentCount() {
        db.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int currentCount = documentSnapshot.getLong("commentsCount") != null
                                ? documentSnapshot.getLong("commentsCount").intValue()
                                : 0;

                        db.collection("posts")
                                .document(postId)
                                .update("commentsCount", currentCount + 1)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Comment count updated"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating comment count", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching post", e));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
