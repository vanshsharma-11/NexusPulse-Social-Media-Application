package com.nexuspulse.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexuspulse.app.models.User;
import com.nexuspulse.app.utils.Constants;
import com.nexuspulse.app.utils.ValidationUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignupPrompt, tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_NexusPulse);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupPrompt = findViewById(R.id.tvSignupPrompt);
        //tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());

        tvSignupPrompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        //tvForgotPassword.setOnClickListener(v -> {
         //   Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
        //});
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        String emailError = ValidationUtil.getEmailError(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            etEmail.requestFocus();
            return;
        }

        String passwordError = ValidationUtil.getPasswordError(password);
        if (passwordError != null) {
            etPassword.setError(passwordError);
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveLoginState(user.getUid(), user.getEmail());
                            ensureUserProfileAfterLogin(user);
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveLoginState(String userId, String email) {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        prefs.edit()
                .putBoolean(Constants.KEY_IS_LOGGED_IN, true)
                .putString(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_EMAIL, email)
                .apply();
    }

    // Robust: after login, check/create user profile if needed!
    private void ensureUserProfileAfterLogin(FirebaseUser user) {
        String userId = user.getUid();
        DocumentReference ref = FirebaseFirestore.getInstance().collection("users").document(userId);

        ref.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                String email = user.getEmail();
                // Fill in more fields if you have them
                User userProfile = new User(userId, "", email, "");
                ref.set(userProfile)
                        .addOnSuccessListener(aVoid -> goToHome())
                        .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Failed to create user profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                goToHome();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(LoginActivity.this, "Error loading user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
