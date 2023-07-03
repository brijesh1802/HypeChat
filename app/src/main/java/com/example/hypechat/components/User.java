package com.example.hypechat.components;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class User {
    String id;
    String name;
    String username;
    String email;
    String bio;
    String profileImageUrl;
    String backgroundImageUrl;
    String onesignalPlayerId;
    boolean isPrivate;
    List<DocumentReference> posts;
    List<DocumentReference> saved;
    List<DocumentReference> following;
    List<DocumentReference> followers;
    List<DocumentReference> blockedAccounts;


    public User() {

    }


    public User(String id, String name, String username, String email, String profileImageUrl, String backgroundImageUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.onesignalPlayerId = "";
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.bio = "";
        this.posts = new ArrayList<>();
        this.saved = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.blockedAccounts = new ArrayList<>();
        this.isPrivate = false;
    }

    public String getOnesignalPlayerId() {
        return onesignalPlayerId;
    }

    public void setOnesignalPlayerId(String onesignalPlayerId) {
        this.onesignalPlayerId = onesignalPlayerId;
    }

    public List<DocumentReference> getFollowing() {
        return following;
    }

    public List<DocumentReference> getFollowers() {
        return followers;
    }

    public List<DocumentReference> getBlockedAccounts() {
        return blockedAccounts;
    }

    public List<DocumentReference> getSaved() {
        return saved;
    }

    public List<DocumentReference> getPosts() {
        return posts;
    }

    public String getId() {
        return id;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public void setPosts(List<DocumentReference> posts) {
        this.posts = posts;
    }

    public void setSaved(List<DocumentReference> saved) {
        this.saved = saved;
    }

    public void setFollowing(List<DocumentReference> following) {
        this.following = following;
    }

    public void setFollowers(List<DocumentReference> followers) {
        this.followers = followers;
    }

    public void setBlockedAccounts(List<DocumentReference> blockedAccounts) {
        this.blockedAccounts = blockedAccounts;
    }

    public String getBio() {
        return bio;
    }
    public String getEmail() {
        return email;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }
}
