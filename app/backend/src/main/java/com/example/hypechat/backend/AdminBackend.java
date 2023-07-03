package com.example.hypechat.backend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class AdminBackend {
    public static void main(String[] args) {
        try {
            FileInputStream serviceAccount = new FileInputStream("F:\\Users\\Hydra\\Downloads\\Kitter-master\\HypeChat\\documents\\hyepechat-9fc6848aefa3.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://hyepechat-default-rtdb.firebaseio.com")
                    .setStorageBucket("hyepechat.appspot.com")
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("App initialized Successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            FirestoreManager.deleteAllPosts();
//            StorageManager.deleteAllPosts();
//            DatabaseManager.deleteAllChat();
//            DatabaseManager.deleteAllComments();
//        } catch (ExecutionException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}