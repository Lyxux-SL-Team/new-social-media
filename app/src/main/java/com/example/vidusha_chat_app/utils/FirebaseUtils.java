package com.example.vidusha_chat_app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseUtils {
    private static FirebaseAuth auth = FirebaseAuth.getInstance();
    private static DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private static FirebaseStorage storage = FirebaseStorage.getInstance();

    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public static DatabaseReference getMessagesRef(String chatRoomId) {
        return database.child("messages").child(chatRoomId);
    }

    public static DatabaseReference getUsersRef() {
        return database.child("users");
    }

    public static FirebaseStorage getStorage() {
        return storage;
    }

    // Additional methods for Firebase authentication, uploading images, etc.
}