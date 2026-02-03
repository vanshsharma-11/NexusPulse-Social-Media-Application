package com.nexuspulse.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;  // ✅ ADDED THIS IMPORT
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.nexuspulse.app.models.Post;
import com.nexuspulse.app.models.User;
import com.nexuspulse.app.repository.PostRepository;
import com.nexuspulse.app.repository.UserRepository;
import com.nexuspulse.app.utils.Constants;
import com.nexuspulse.app.utils.SupabaseClient;
import com.nexuspulse.app.utils.ValidationUtil;

import java.util.ArrayList;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etPostContent;
    private TextView tvCharCount;
    private ImageView ivSelectedImage;
    private FloatingActionButton ivRemoveImage;
    private MaterialCardView btnSelectImage;  // ✅ CHANGED FROM ImageButton
    private ImageButton btnClose;             // ✅ SEPARATE LINE
    private MaterialButton btnPost;
    private ProgressBar progressBar;
    private FrameLayout loadingOverlay;
    private CardView imagePreviewContainer;

    private Uri selectedImageUri;
    private String currentUserId;
    private User currentUser;

    private UserRepository userRepository;
    private PostRepository postRepository;

    private static final int REQUEST_IMAGE_PICK = Constants.REQUEST_IMAGE_PICK;
    private static final int MAX_CHAR_COUNT = 280;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_NexusPulse);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize repositories
        userRepository = new UserRepository();
        postRepository = new PostRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        initializeViews();

        // Load current user info
        loadUserInfo();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        btnClose = findViewById(R.id.btnClose);
        btnPost = findViewById(R.id.btnPost);
        etPostContent = findViewById(R.id.etPostContent);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        ivRemoveImage = findViewById(R.id.ivRemoveImage);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvCharCount = findViewById(R.id.tvCharCount);

        // Initially disable post button
        btnPost.setEnabled(false);
    }

    private void loadUserInfo() {
        userRepository.getUserById(currentUserId, new UserRepository.OnUserFetchedListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CreatePostActivity.this, "Error loading user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Close button
        btnClose.setOnClickListener(v -> finish());

        // Post button
        btnPost.setOnClickListener(v -> createPost());

        // Select image button
        btnSelectImage.setOnClickListener(v -> selectImage());

        // Remove image button
        ivRemoveImage.setOnClickListener(v -> removeSelectedImage());

        ivSelectedImage.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                Intent intent = new Intent(CreatePostActivity.this, FullScreenImageActivity.class);
                intent.putExtra("IMAGE_URL", selectedImageUri.toString());
                startActivity(intent);
            }
        });

        // Character counter
        etPostContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharacterCount(s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateCharacterCount(int count) {
        int remaining = MAX_CHAR_COUNT - count;

        // Show count when getting close to limit
        if (count > 260 || count > MAX_CHAR_COUNT) {
            tvCharCount.setText(String.valueOf(remaining));
            tvCharCount.setVisibility(View.VISIBLE);
        } else {
            tvCharCount.setVisibility(View.GONE);
        }

        // Color coding
        if (count > MAX_CHAR_COUNT) {
            tvCharCount.setTextColor(getResources().getColor(R.color.like_pink));
            btnPost.setEnabled(false);
        } else if (count > 0) {
            tvCharCount.setTextColor(getResources().getColor(R.color.twitter_blue));
            btnPost.setEnabled(true);
        } else {
            btnPost.setEnabled(false);
        }
    }

    private void selectImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_IMAGE_PICK);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void removeSelectedImage() {
        selectedImageUri = null;
        imagePreviewContainer.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_PICK) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivSelectedImage);

                imagePreviewContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void createPost() {
        String content = etPostContent.getText().toString().trim();

        // Validate
        if (content.isEmpty()) {
            Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.length() > MAX_CHAR_COUNT) {
            Toast.makeText(this, "Post is too long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "Loading user info...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        showLoading(true);

        // Upload image if selected
        if (selectedImageUri != null) {
            uploadImageAndCreatePost(content);
        } else {
            createPostWithoutImage(content);
        }
    }

    private void uploadImageAndCreatePost(String content) {
        new Thread(() -> {
            String imageUrl = SupabaseClient.uploadPostImage(
                    CreatePostActivity.this,
                    selectedImageUri,
                    currentUserId
            );

            runOnUiThread(() -> {
                if (imageUrl != null) {
                    createPostWithImage(content, imageUrl);
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void createPostWithImage(String content, String imageURL) {
        Post post = createPostObject(content, imageURL);
        savePost(post);
    }

    private void createPostWithoutImage(String content) {
        Post post = createPostObject(content, null);
        savePost(post);
    }

    private Post createPostObject(String content, String imageUrl) {
        Post post = new Post();
        post.setPostId(null);
        post.setUserId(currentUserId);
        post.setUsername(currentUser.getUsername());
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setUserProfileImage(currentUser.getProfileImageUrl());
        post.setLikesCount(0);
        post.setRetweetsCount(0);
        post.setCommentsCount(0);
        post.setLikedBy(new ArrayList<>());
        post.setRetweetedBy(new ArrayList<>());
        return post;
    }

    private void savePost(Post post) {
        postRepository.createPost(post, new PostRepository.OnPostCreateListener() {
            @Override
            public void onSuccess(String postId) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreatePostActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreatePostActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingOverlay.setVisibility(View.VISIBLE);
            btnPost.setEnabled(false);
            btnSelectImage.setEnabled(false);
            btnClose.setEnabled(false);
        } else {
            loadingOverlay.setVisibility(View.GONE);
            btnPost.setEnabled(true);
            btnSelectImage.setEnabled(true);
            btnClose.setEnabled(true);
        }
    }
}
