package com.nexuspulse.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nexuspulse.app.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_splash);

            Log.d(TAG, "SplashActivity started");

            // Check if user is already logged in after 2 seconds
            new Handler().postDelayed(() -> {
                try {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
                    boolean isLoggedIn = prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);

                    Intent intent;
                    if (currentUser != null && isLoggedIn) {
                        Log.d(TAG, "User logged in, going to Home");
                        intent = new Intent(SplashActivity.this, HomeActivity.class);
                    } else {
                        Log.d(TAG, "User not logged in, going to Login");
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error in delayed task", e);
                    // Fallback to login if error
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000); // 2 second delay

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            // Emergency fallback
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
