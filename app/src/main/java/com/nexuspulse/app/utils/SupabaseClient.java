package com.nexuspulse.app.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {
    private static final String TAG = "SupabaseClient";

    private static final String SUPABASE_URL = "https://bilarohgeuqvcgdtohsz.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJpbGFyb2hnZXVxdmNnZHRvaHN6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA3OTg4NjUsImV4cCI6MjA3NjM3NDg2NX0.79wK8s48PfNWdyiFZi0MUOZkzMQkhWNoXMYbgctI5TE";

    private static final String BUCKET_PROFILE = "profile_images";
    private static final String BUCKET_COVER = "cover_images";
    private static final String BUCKET_POST = "post_images";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static String uploadProfileImage(Context context, Uri imageUri, String userId) {
        return uploadImage(context, imageUri, userId, BUCKET_PROFILE);
    }

    public static String uploadCoverImage(Context context, Uri imageUri, String userId) {
        return uploadImage(context, imageUri, userId, BUCKET_COVER);
    }

    public static String uploadPostImage(Context context, Uri imageUri, String userId) {
        return uploadImage(context, imageUri, userId, BUCKET_POST);
    }

    private static String uploadImage(Context context, Uri imageUri, String userId, String bucketName) {
        try {
            Log.d(TAG, "=== Starting Upload ===");
            Log.d(TAG, "Bucket: " + bucketName);
            Log.d(TAG, "UserId: " + userId);

            // Read image bytes
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "❌ Failed to open input stream");
                return null;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] imageBytes = buffer.toByteArray();
            inputStream.close();

            Log.d(TAG, "Image size: " + imageBytes.length + " bytes");

            // Generate filename
            String fileName = userId + "/" + UUID.randomUUID().toString() + ".jpg";
            String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + bucketName + "/" + fileName;

            Log.d(TAG, "Filename: " + fileName);
            Log.d(TAG, "Upload URL: " + uploadUrl);

            // Create request body
            RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));

            // ⭐ CRITICAL FIX: Add apikey header (required in 2025)
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .addHeader("apikey", SUPABASE_ANON_KEY)  // ← THIS IS THE FIX!
                    .addHeader("Content-Type", "image/jpeg")
                    .post(body)
                    .build();

            Log.d(TAG, "Sending request...");

            // Execute request
            Response response = client.newCall(request).execute();
            String responseBody = response.body() != null ? response.body().string() : "No response";

            Log.d(TAG, "Response code: " + response.code());
            Log.d(TAG, "Response message: " + response.message());

            if (response.isSuccessful()) {
                String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + fileName;
                Log.d(TAG, "✅ UPLOAD SUCCESSFUL!");
                Log.d(TAG, "Public URL: " + publicUrl);
                return publicUrl;
            } else {
                Log.e(TAG, "❌ UPLOAD FAILED!");
                Log.e(TAG, "Status code: " + response.code());
                Log.e(TAG, "Error body: " + responseBody);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during upload: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
