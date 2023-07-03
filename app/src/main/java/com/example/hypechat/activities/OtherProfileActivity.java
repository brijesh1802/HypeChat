package com.example.hypechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hypechat.R;
import com.example.hypechat.components.User;
import com.example.hypechat.utilities.ProfilePageManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherProfileActivity extends AppCompatActivity {
    ProfilePageManager profilePageManager;
    DocumentReference userReference, myReference;
    Button followButton, messageButton;
    boolean amFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        String userid = getIntent().getStringExtra("userid");
        userReference = FirebaseFirestore.getInstance().collection("Users").document(userid);
        myReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid());

        profilePageManager = new ProfilePageManager(this.getWindow().getDecorView());

        followButton = findViewById(R.id.btn_follow);
        messageButton = findViewById(R.id.btn_message);

        if (myReference.equals(userReference)) {
            followButton.setVisibility(View.GONE);
            messageButton.setVisibility(View.GONE);
        }
        messageButton.setOnClickListener(v -> {
            Intent intent = new Intent(OtherProfileActivity.this, ChatActivity.class);
            intent.putExtra("userid", userid);
            startActivity(intent);
        });

        fillUserData();
        readPosts();

        followButton.setOnClickListener(v -> toggleFollow());
    }


    private void toggleFollow() {
        amFollowing = !amFollowing;
        if (amFollowing) {
            profilePageManager.noOfFollowers.setText(String.valueOf(Integer.parseInt(profilePageManager.noOfFollowers.getText().toString()) + 1));

            myReference.update("following", FieldValue.arrayUnion(userReference));
            userReference.update("followers", FieldValue.arrayUnion(myReference)).addOnSuccessListener(unused -> readPosts());
            userReference.get().addOnSuccessListener(userSnapshot -> {
                User user = userSnapshot.toObject(User.class);
                assert user != null;
                List<DocumentReference> posts = user.getPosts();
                for (DocumentReference postReference : posts) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("postReference", postReference);
                    map.put("visited", false);
                    myReference.collection("feed").document(postReference.getId()).set(map);
                }
            });
        } else {
            profilePageManager.noOfFollowers.setText(String.valueOf(Integer.parseInt(profilePageManager.noOfFollowers.getText().toString()) - 1));

            myReference.update("following", FieldValue.arrayRemove(userReference));
            userReference.update("followers", FieldValue.arrayRemove(myReference)).addOnSuccessListener(unused -> readPosts());
            userReference.get().addOnSuccessListener(userSnapshot -> {
                User user = userSnapshot.toObject(User.class);
                assert user != null;
                List<DocumentReference> posts = user.getPosts();
                for (DocumentReference postReference : posts) {
                    myReference.collection("feed").document(postReference.getId()).delete();
                }
            });
        }
        updateFollowButton();
    }


    void updateFollowButton() {
        if (amFollowing) {
            followButton.setText("Following");
            followButton.setBackgroundColor(getResources().getColor(R.color.button_gray));
            messageButton.setEnabled(true);
        } else {
            followButton.setText("Follow");
            followButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
            messageButton.setEnabled(false);
        }
    }

    void fillUserData() {
        userReference.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            assert user != null;

            profilePageManager.fillUserData(user);
            amFollowing = user.getFollowers().contains(myReference);
            updateFollowButton();
        });
    }

    private void readPosts() {
        if (amFollowing) {
            profilePageManager.readPosts(userReference);
            messageButton.setEnabled(true);
        }else{
            profilePageManager.clearPosts();
        }
    }
}


