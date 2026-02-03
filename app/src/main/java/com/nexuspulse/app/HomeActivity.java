package com.nexuspulse.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nexuspulse.app.adapters.PostAdapter;
import com.nexuspulse.app.models.Post;

import java.util.ArrayList;
import java.util.List;




public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabTweet;
    private ShapeableImageView profileAvatar;

    private List<String> followingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_NexusPulse);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        followingList = new ArrayList<>();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupTabs();
        setupSwipeRefresh();
        setupFAB();
        setupProfileAvatar();

        loadCurrentUserProfile();
        loadFollowingList();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        tabLayout = findViewById(R.id.tabLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabTweet = findViewById(R.id.fabTweet);
        profileAvatar = findViewById(R.id.profileAvatar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewPosts.setLayoutManager(layoutManager);
        recyclerViewPosts.setAdapter(postAdapter);

        recyclerViewPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fabTweet.hide();
                } else if (dy < 0) {
                    fabTweet.show();
                }
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("For You"));
        tabLayout.addTab(tabLayout.newTab().setText("Following"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadPosts();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                recyclerViewPosts.smoothScrollToPosition(0);
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(
                R.color.twitter_blue,
                R.color.twitter_blue_dark
        );
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.black);
        swipeRefresh.setOnRefreshListener(this::loadFollowingList);
    }

    private void setupFAB() {
        fabTweet.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
            startActivity(new Intent(HomeActivity.this, CreatePostActivity.class));
        });
    }

    private void setupProfileAvatar() {
        profileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        });
    }

    private void loadCurrentUserProfile() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.w(TAG, "Current user ID is null or empty");
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(HomeActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(profileAvatar);
                            Log.d(TAG, "Profile image loaded successfully");
                        } else {
                            Log.d(TAG, "No profile image URL found for user");
                        }
                    } else {
                        Log.w(TAG, "User document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile image", e);
                });
    }

    private void loadFollowingList() {
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        if (following != null) {
                            followingList = following;
                            Log.d(TAG, "Following list loaded: " + followingList.size() + " users");
                        } else {
                            followingList = new ArrayList<>();
                            Log.d(TAG, "No following list found");
                        }
                    }
                    loadPosts();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading following list", e);
                    followingList = new ArrayList<>();
                    loadPosts();
                });
    }

    /**
     * UPDATED: Load posts based on selected tab and set user verification
     */
    private void loadPosts() {
        swipeRefresh.setRefreshing(true);

        int selectedTab = tabLayout.getSelectedTabPosition();

        Query query = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50);

        // If "Following" tab is selected and user follows someone
        if (selectedTab == 1 && followingList != null && !followingList.isEmpty()) {
            query = db.collection("posts")
                    .whereIn("userId", followingList)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50);
            Log.d(TAG, "Loading Following feed from " + followingList.size() + " users");
        } else if (selectedTab == 1) {
            postList.clear();
            postAdapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(false);
            Toast.makeText(this, "Follow users to see their posts here", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Log.d(TAG, "Loading For You feed (all posts)");
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());
                        postList.add(post);

                        // Fetch verified status from users collection
                        db.collection("users").document(post.getUserId()).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        boolean isVerified = userDoc.contains("verified")
                                                && userDoc.getBoolean("verified") != null
                                                && userDoc.getBoolean("verified");
                                        post.setUserVerified(isVerified);
                                        postAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user verification status", e));
                    }
                    postAdapter.notifyDataSetChanged();
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                    Log.d(TAG, "Loaded " + postList.size() + " posts");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading posts", e);
                    Toast.makeText(this, "Error loading posts", Toast.LENGTH_SHORT).show();
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFollowingList();
        loadCurrentUserProfile();
    }
}
