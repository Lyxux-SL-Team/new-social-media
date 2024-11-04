package com.example.vidusha_chat_app.models;

public class User {

    private String userId;
    private String name;
    private String profilePicUrl;
    private String status;

    private String password;

    public User() {}

    public User(String userId, String name, String profilePicUrl, String status, String password) {
        this.userId = userId;
        this.name = name;
        this.profilePicUrl = profilePicUrl;
        this.status = status;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
