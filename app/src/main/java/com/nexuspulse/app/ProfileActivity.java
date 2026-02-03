package com.nexuspulse.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import android.widget.ImageView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nexuspulse.app.adapters.PostAdapter;
import com.nexuspulse.app.adapters.RepliesAdapter;
import com.nexuspulse.app.adapters.MediaAdapter;
import com.nexuspulse.app.models.Comment;
import com.nexuspulse.app.models.Post;
import com.nexuspulse.app.models.User;
import com.nexuspulse.app.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ShapeableImageView ivProfileImage, ivCoverImage;
    private TextView tvUsername, tvProfileName, tvProfileHandle, tvBio;
    private TextView tvFollowingCount, tvFollowersCount;
    private MaterialButton btnEditProfile;
    private MaterialButton btnFollowUnfollow;
    private TabLayout tabLayout;

    // RecyclerViews for different tabs
    private RecyclerView recyclerViewPosts;
    private RecyclerView recyclerViewReplies;
    private RecyclerView recyclerViewMedia;

    // Adapters
    private PostAdapter postAdapter;
    private RepliesAdapter repliesAdapter;
    private MediaAdapter mediaAdapter;

    // Data lists
    private List<Post> postList;
    private List<Comment> replyList;
    private List<Post> mediaList;

    // Firebase & User data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserRepository userRepository;
    private String userId;
    private String currentUserId;
    private User currentUser;
    private boolean isFollowing = false;
    private ImageView ivVerifiedBadgeProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_NexusPulse);
        setContentView(R.layout.activity_profile);
        ivVerifiedBadgeProfile = findViewById(R.id.ivVerifiedBadge);


        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Get userId from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            userId = currentUserId;
        }

        // Setup all components
        initializeViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupRepliesRecyclerView();
        setupMediaRecyclerView();
        setupButtons();

        // Load initial data
        loadUserProfile();
        loadUserPosts();
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivCoverImage = findViewById(R.id.ivCoverImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileHandle = findViewById(R.id.tvProfileHandle);
        tvBio = findViewById(R.id.tvBio);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnFollowUnfollow = findViewById(R.id.btnFollowUnfollow);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewReplies = findViewById(R.id.recyclerViewReplies);
        recyclerViewMedia = findViewById(R.id.recyclerViewMedia);
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Setup tabs for Posts, Replies, and Media
     */
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Posts"));
        tabLayout.addTab(tabLayout.newTab().setText("Replies"));
        tabLayout.addTab(tabLayout.newTab().setText("Media"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Posts tab
                        recyclerViewPosts.setVisibility(View.VISIBLE);
                        recyclerViewReplies.setVisibility(View.GONE);
                        recyclerViewMedia.setVisibility(View.GONE);
                        loadUserPosts();
                        break;

                    case 1: // Replies tab
                        recyclerViewPosts.setVisibility(View.GONE);
                        recyclerViewReplies.setVisibility(View.VISIBLE);
                        recyclerViewMedia.setVisibility(View.GONE);
                        loadUserReplies();
                        break;

                    case 2: // Media tab
                        recyclerViewPosts.setVisibility(View.GONE);
                        recyclerViewReplies.setVisibility(View.GONE);
                        recyclerViewMedia.setVisibility(View.VISIBLE);
                        loadUserMedia();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Setup RecyclerView for Posts tab
     */
    private void setupRecyclerView() {
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, currentUserId);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.setVisibility(View.VISIBLE);
    }

    /**
     * Setup RecyclerView for Replies tab
     */
    private void setupRepliesRecyclerView() {
        replyList = new ArrayList<>();
        repliesAdapter = new RepliesAdapter(this, replyList);
        recyclerViewReplies.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReplies.setAdapter(repliesAdapter);
        recyclerViewReplies.setVisibility(View.GONE);
    }

    /**
     * Setup RecyclerView for Media tab with Grid layout
     */
    private void setupMediaRecyclerView() {
        mediaList = new ArrayList<>();
        mediaAdapter = new MediaAdapter(this, mediaList);
        recyclerViewMedia.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMedia.setAdapter(mediaAdapter);
        recyclerViewMedia.setVisibility(View.GONE);
        Log.d(TAG, "Media RecyclerView setup complete");
    }


    /**
     * Setup Edit Profile and Follow/Unfollow buttons
     */
    private void setupButtons() {
        if (userId.equals(currentUserId)) {
            // Own profile - show Edit button
            btnEditProfile.setVisibility(View.VISIBLE);
            btnFollowUnfollow.setVisibility(View.GONE);
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        } else {
            // Other user's profile - show Follow button
            btnEditProfile.setVisibility(View.GONE);
            btnFollowUnfollow.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Load user profile data from Firestore
     */
    private void loadUserProfile() {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            displayUserProfile(currentUser);
                            if (currentUser.isVerified()) {
                                ivVerifiedBadgeProfile.setVisibility(View.VISIBLE);
                            } else {
                                ivVerifiedBadgeProfile.setVisibility(View.GONE);
                            }


                            if (!userId.equals(currentUserId)) {
                                configureFollowButton();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile", e);
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Display user profile information
     */
    private void displayUserProfile(User user) {
        tvUsername.setText(user.getUsername());
        tvProfileName.setText(user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                ? user.getDisplayName()
                : user.getUsername());
        tvProfileHandle.setText("@" + user.getUsername());

        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setVisibility(View.VISIBLE);
            tvBio.setText(user.getBio());
        } else {
            tvBio.setVisibility(View.GONE);
        }

        tvFollowingCount.setText(String.valueOf(user.getFollowingCount()));
        tvFollowersCount.setText(String.valueOf(user.getFollowersCount()));

        // Load cover image
        Glide.with(this)
                .load(user.getCoverImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivCoverImage);

        // Load profile image
        Glide.with(this)
                .load(user.getProfileImageUrl())
                .placeholder(android.R.drawable.ic_menu_camera)
                .error(android.R.drawable.ic_menu_camera)
                .into(ivProfileImage);
    }

    /**
     * Configure follow/unfollow button
     */
    private void configureFollowButton() {
        if (currentUser == null) return;

        isFollowing = currentUser.getFollowers() != null
                && currentUser.getFollowers().contains(currentUserId);

        updateFollowButtonUI(isFollowing);

        btnFollowUnfollow.setOnClickListener(v -> {
            if (isFollowing) {
                unfollowUser();
            } else {
                followUser();
            }
        });
    }

    /**
     * Follow a user
     */
    private void followUser() {
        btnFollowUnfollow.setEnabled(false);
        userRepository.followUser(currentUserId, userId, new UserRepository.OnUpdateListener() {
            @Override
            public void onSuccess() {
                isFollowing = true;
                if (currentUser != null) {
                    currentUser.setFollowersCount(currentUser.getFollowersCount() + 1);
                    tvFollowersCount.setText(String.valueOf(currentUser.getFollowersCount()));
                }
                updateFollowButtonUI(true);
                btnFollowUnfollow.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Followed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Successfully followed user: " + userId);
            }

            @Override
            public void onFailure(String errorMessage) {
                btnFollowUnfollow.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Failed to follow: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error following user: " + errorMessage);
            }
        });
    }

    /**
     * Unfollow a user
     */
    private void unfollowUser() {
        btnFollowUnfollow.setEnabled(false);
        userRepository.unfollowUser(currentUserId, userId, new UserRepository.OnUpdateListener() {
            @Override
            public void onSuccess() {
                isFollowing = false;
                if (currentUser != null) {
                    currentUser.setFollowersCount(Math.max(0, currentUser.getFollowersCount() - 1));
                    tvFollowersCount.setText(String.valueOf(currentUser.getFollowersCount()));
                }
                updateFollowButtonUI(false);
                btnFollowUnfollow.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Successfully unfollowed user: " + userId);
            }

            @Override
            public void onFailure(String errorMessage) {
                btnFollowUnfollow.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Failed to unfollow: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error unfollowing user: " + errorMessage);
            }
        });
    }

    /**
     * Update follow button UI based on follow status
     */
    private void updateFollowButtonUI(boolean following) {
        if (following) {
            btnFollowUnfollow.setText("Following");
            btnFollowUnfollow.setBackgroundColor(getResources().getColor(R.color.dark_gray, null));
        } else {
            btnFollowUnfollow.setText("Follow");
            btnFollowUnfollow.setBackgroundColor(getResources().getColor(R.color.twitter_blue, null));
        }
    }

    /**
     * Load user posts from Firestore
     */
    private void loadUserPosts() {
        Log.d(TAG, "Loading posts for userId: " + userId);
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());
                        postList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Posts loaded count: " + postList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading posts", e);
                });
    }

    /**
     * Load user replies from Firestore
     */
    private void loadUserReplies() {
        Log.d(TAG, "Loading replies for userId: " + userId);
        db.collection("comments")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    replyList.clear();
                    Log.d(TAG, "Replies query returned documents: " + queryDocumentSnapshots.size());

                    if (queryDocumentSnapshots.isEmpty()) {
                        repliesAdapter.notifyDataSetChanged();
                        Log.d(TAG, "No replies found");
                        return;
                    }

                    // Fetch the user info to get the real username
                    db.collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                String realUsername = "Unknown";
                                String profileImage = null;

                                if (userDoc.exists()) {
                                    User user = userDoc.toObject(User.class);
                                    if (user != null) {
                                        realUsername = user.getUsername();
                                        profileImage = user.getProfileImageUrl();
                                    }
                                }

                                // Populate replies with the correct username
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    Comment reply = doc.toObject(Comment.class);

                                    // Override the username field with the real username
                                    reply.setUsername(realUsername);

                                    // Override profile image if available
                                    if (profileImage != null && !profileImage.isEmpty()) {
                                        reply.setUserProfileImage(profileImage);
                                    }

                                    Log.d(TAG, "Loaded reply: " + reply.getContent() +
                                            " by: " + realUsername);
                                    replyList.add(reply);
                                }

                                repliesAdapter.notifyDataSetChanged();
                                Log.d(TAG, "Replies loaded count: " + replyList.size());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading user for replies", e);
                                // Still show replies even if username fetch fails
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    Comment reply = doc.toObject(Comment.class);
                                    replyList.add(reply);
                                }
                                repliesAdapter.notifyDataSetChanged();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading replies", e);
                });
    }

    /**
     * Load user media (posts with images) from Firestore
     */
    private void loadUserMedia() {
        Log.d(TAG, "Loading media for userId: " + userId);
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mediaList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());

                        // Only add posts that have images
                        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                            mediaList.add(post);
                            Log.d(TAG, "Added media post with URL: " + post.getImageUrl());
                        }
                    }

                    // Notify adapter that data has changed
                    mediaAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Media loaded count: " + mediaList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading media", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadUserPosts();
    }
}
