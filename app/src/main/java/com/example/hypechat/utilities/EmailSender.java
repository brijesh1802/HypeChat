package com.example.hypechat.utilities;

import androidx.annotation.NonNull;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class EmailSender {
    private static final String SENDINBLUE_API_KEY = "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";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void sendEmail(String recipientEmail, String subject, String content) {
        OkHttpClient client = new OkHttpClient();

        // Create the JSON request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("sender", new JSONObject()
                    .put("name", "HypeChat")
                    .put("email", "hypechat.org@gmail.com"));
            requestBody.put("to", new JSONArray()
                    .put(new JSONObject()
                            .put("email", recipientEmail)));
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", content);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Create the HTTP request
        Request request = new Request.Builder()
                .url("https://api.sendinblue.com/v3/smtp/email")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("api-key", SENDINBLUE_API_KEY)
                .post(RequestBody.create(JSON, requestBody.toString()))
                .build();

        // Send the HTTP request asynchronously
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle request failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                // Handle request success
                if (response.isSuccessful()) {
                    // Email sent successfully
                    System.out.println("Email sent successfully");
                } else {
                    // Error occurred while sending email
                    System.out.println("Error sending email: " + response.code() + " " + response.message());
                }
            }
        });
    }
}

