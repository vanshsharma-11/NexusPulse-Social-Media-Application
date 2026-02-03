package com.nexuspulse.app.utils;

public class Constants {
    // Firebase Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_POSTS = "posts";
    public static final String COLLECTION_COMMENTS = "comments";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_MESSAGES = "messages";

    // SharedPreferences
    public static final String PREF_NAME = "NexusPulsePrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";  // ← ADD THIS!



    // Intent Keys
    public static final String KEY_POST_ID = "postId";
    public static final String KEY_USER_ID_INTENT = "userId";

    // Request Codes
    // Request Codes
    public static final int REQUEST_IMAGE_PICK = 1001;
    public static final int REQUEST_IMAGE_CAMERA = 1002;
    public static final int REQUEST_PROFILE_IMAGE = 1003;  // ← ADD THIS
    public static final int REQUEST_COVER_IMAGE = 1004;    // ← ADD THIS


    // Limits
    public static final int MAX_POST_LENGTH = 280;
    public static final int MAX_BIO_LENGTH = 160;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;
}
