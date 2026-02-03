package com.nexuspulse.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.nexuspulse.app.adapters.UserAdapter;
import com.nexuspulse.app.models.User;
import com.nexuspulse.app.repository.UserRepository;
import com.nexuspulse.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvUsers;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private UserAdapter userAdapter;
    private List<User> userList;
    private UserRepository userRepository;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Material Components theme
        setTheme(R.style.Theme_NexusPulse);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize
        userRepository = new UserRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        etSearch = findViewById(R.id.etSearch);
        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        // Setup RecyclerView
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, userId -> {
            // User clicked - go to their profile
            Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
            intent.putExtra(Constants.KEY_USER_ID_INTENT, userId);
            startActivity(intent);
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);

        // Show empty state initially
        showEmptyState(true);

        // Search text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 0) {
                    searchUsers(query);
                } else {
                    userList.clear();
                    userAdapter.notifyDataSetChanged();
                    showEmptyState(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Auto-focus search bar
        etSearch.requestFocus();
    }

    private void searchUsers(String query) {
        progressBar.setVisibility(View.VISIBLE);
        showEmptyState(false);
        rvUsers.setVisibility(View.GONE);

        userRepository.searchUsers(query, new UserRepository.OnUsersListFetchedListener() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userList.clear();

                // Remove current user from results
                for (User user : users) {
                    if (!user.getUserId().equals(currentUserId)) {
                        userList.add(user);
                    }
                }

                userAdapter.notifyDataSetChanged();

                if (userList.isEmpty()) {
                    showEmptyState(true);
                } else {
                    rvUsers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void showEmptyState(boolean show) {
        if (show) {
            emptyState.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
        }
    }
}
