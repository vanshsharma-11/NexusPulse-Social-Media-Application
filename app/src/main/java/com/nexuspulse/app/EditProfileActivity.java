package com.nexuspulse.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.nexuspulse.app.models.User;
import com.nexuspulse.app.repository.UserRepository;
import com.nexuspulse.app.utils.Constants;
import com.nexuspulse.app.utils.SupabaseClient;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private MaterialButton btnSave;
    private ImageView ivProfileImage, ivCoverImage;
    private FrameLayout btnEditProfileImage, btnEditCoverImage, loadingOverlay;
    private ProgressBar progressBar;

    // Input Fields (matching User model)
    private TextInputEditText etName, etUsername, etBio;

    // Data
    private Uri selectedProfileImageUri, selectedCoverImageUri;
    private String currentUserId;
    private User currentUser;
    private UserRepository userRepository;

    private static final int REQUEST_PROFILE_IMAGE = 1001;
    private static final int REQUEST_COVER_IMAGE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize
        userRepository = new UserRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initializeViews();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        // Buttons
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        // Images
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivCoverImage = findViewById(R.id.ivCoverImage);
        btnEditProfileImage = findViewById(R.id.btnEditProfileImage);
        btnEditCoverImage = findViewById(R.id.btnEditCoverImage);

        // Input Fields (only the ones that exist in User model)
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);

        // Loading
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadUserData() {
        showLoading(true);

        userRepository.getUserById(currentUserId, new UserRepository.OnUserFetchedListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                populateFields(user);
                showLoading(false);
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(User user) {
        // Set text fields (using correct getter methods)
        etName.setText(user.getDisplayName());  // ✅ FIXED: using getDisplayName()
        etUsername.setText(user.getUsername());
        etBio.setText(user.getBio());

        // Load images with Glide
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivProfileImage);
        }

        if (user.getCoverImageUrl() != null && !user.getCoverImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getCoverImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivCoverImage);
        }
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());

        // Edit profile image
        btnEditProfileImage.setOnClickListener(v -> selectImage(REQUEST_PROFILE_IMAGE));

        // Edit cover image
        btnEditCoverImage.setOnClickListener(v -> selectImage(REQUEST_COVER_IMAGE));
    }

    private void selectImage(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    requestCode);
        } else {
            openImagePicker(requestCode);
        }
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker(requestCode);
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            if (requestCode == REQUEST_PROFILE_IMAGE) {
                selectedProfileImageUri = imageUri;
                Glide.with(this).load(imageUri).into(ivProfileImage);
            } else if (requestCode == REQUEST_COVER_IMAGE) {
                selectedCoverImageUri = imageUri;
                Glide.with(this).load(imageUri).into(ivCoverImage);
            }
        }
    }

    private void saveProfile() {
        // Get input values
        String displayName = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // Validate
        if (displayName.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            return;
        }

        showLoading(true);

        // Upload images first if selected
        uploadImagesAndSave(displayName, username, bio);
    }

    private void uploadImagesAndSave(String displayName, String username, String bio) {
        new Thread(() -> {
            String profileImageUrl = currentUser.getProfileImageUrl();
            String coverImageUrl = currentUser.getCoverImageUrl();

            // Upload profile image if selected
            if (selectedProfileImageUri != null) {
                profileImageUrl = SupabaseClient.uploadProfileImage(
                        this,
                        selectedProfileImageUri,
                        currentUserId
                );
            }

            // Upload cover image if selected
            if (selectedCoverImageUri != null) {
                coverImageUrl = SupabaseClient.uploadCoverImage(
                        this,
                        selectedCoverImageUri,
                        currentUserId
                );
            }

            final String finalProfileUrl = profileImageUrl;
            final String finalCoverUrl = coverImageUrl;

            runOnUiThread(() -> updateUserProfile(displayName, username, bio, finalProfileUrl, finalCoverUrl));
        }).start();
    }

    private void updateUserProfile(String displayName, String username, String bio, String profileUrl, String coverUrl) {
        // Create updates map (using the same approach as your old code)
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        updates.put("username", username);
        updates.put("bio", bio);

        if (profileUrl != null) {
            updates.put("profileImageUrl", profileUrl);
        }

        if (coverUrl != null) {
            updates.put("coverImageUrl", coverUrl);
        }

        userRepository.updateUser(currentUserId, updates, new UserRepository.OnUpdateListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingOverlay.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else {
            loadingOverlay.setVisibility(View.GONE);
            btnSave.setEnabled(true);
        }
    }
}
