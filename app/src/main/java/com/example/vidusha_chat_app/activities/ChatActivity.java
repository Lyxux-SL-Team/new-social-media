package com.example.vidusha_chat_app.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidusha_chat_app.adapters.MessageAdapter;
import com.example.vidusha_chat_app.database.MessageDatabaseHelper;
import com.example.vidusha_chat_app.models.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.vidusha_chat_app.R;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentChange;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private String userId;
    private String chatId;
    private MessageAdapter messageAdapter;
    private ListenerRegistration listenerRegistration;

    private List<Message> messages;
    private RecyclerView recyclerView;

    private MessageDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        firestore = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        chatId = sharedPreferences.getString("userId", null);
        if (userId != null) {
            Log.d("ChatActivity", "Logged-in User ID: " + userId);
        } else {
            Log.d("ChatActivity", "User ID not found.");
        }

        Log.d("ChatActivity", "Logged-in chat ID: " + chatId);
        Log.d("ChatActivity", "Logged-in User ID: " + userId);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, chatId);
        recyclerView = findViewById(R.id.posts_recycler_view);
        ImageButton btnPickImage = findViewById(R.id.imageButton);
        EditText editTextText = findViewById(R.id.editTextText);
        Button buttonPost = findViewById(R.id.button3);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        listenForMessages();
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = editTextText.getText().toString().trim();
                if (!messageContent.isEmpty()) {
                    sendMessage(messageContent);
                    editTextText.setText(""); // Clear input field
                } else {
                    Toast.makeText(ChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }

        });
        editTextText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    editTextText.setHint("");
                }else{
                    editTextText.setHint("Text here..");
                }
            }
        });
    }

    private void sendMessage(String content) {
        Message message = new Message();
        message.setSenderId(userId);
        message.setContent(content);
        message.setTimestamp(System.currentTimeMillis());
        message.setChatId(chatId);

        if(isConnected()){
            // Save the message to Firestore
            CollectionReference messagesRef = firestore.collection("messages");
            messagesRef.add(message)
                    .addOnSuccessListener(documentReference -> {
                        // Optionally handle success
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                    });
//            listenForMessages();
        } else {
            MessageDatabaseHelper dbHelper = new MessageDatabaseHelper(this);
            dbHelper.saveMessageOffline(message);
            Toast.makeText(ChatActivity.this, "Message saved offline", Toast.LENGTH_SHORT).show();
            dbHelper.getOfflineMessage();
            listenForMessages();

        }

    }

    private void listenForMessages() {
        messages.clear(); // Clear existing messages to avoid duplication

        // Fetch offline messages first and add them to the list
        MessageDatabaseHelper dbHelper = new MessageDatabaseHelper(this);
        List<Message> offlineMessages = dbHelper.getOfflineMessage();
        messages.addAll(offlineMessages);
//        updateRecyclerView();

        // Query for messages where chatId matches userId
        Query queryByChatId = firestore.collection("messages")
                .whereEqualTo("chatId", userId)
                .orderBy("timestamp");

        // Query for messages where senderId matches userId
        Query queryBySenderId = firestore.collection("messages")
                .whereEqualTo("senderId", userId)
                .orderBy("timestamp");

        // Listener for chatId query
        ListenerRegistration chatIdListener = queryByChatId.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("ChatActivity", "Error fetching messages by chatId", e);
                return;
            }

            if (snapshots != null) {
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Message message = dc.getDocument().toObject(Message.class);
                        if (!messages.contains(message)) {
                            messages.add(message);
                        }
                    }
                }
                updateRecyclerView(); // Update the RecyclerView after adding Firestore messages
            }
        });

        // Listener for senderId query
        ListenerRegistration senderIdListener = queryBySenderId.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("ChatActivity", "Error fetching messages by senderId", e);
                return;
            }

            if (snapshots != null) {
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Message message = dc.getDocument().toObject(Message.class);
                        if (!messages.contains(message)) {
                            messages.add(message);
                        }
                    }
                }
                updateRecyclerView(); // Update the RecyclerView after adding Firestore messages
            }
        });

        // Store these listener registrations to remove them later if needed
        listenerRegistration = chatIdListener;


        // Update the RecyclerView with offline messages initially
        updateRecyclerView();
    }

    // Method to update the RecyclerView and scroll to the latest message
    private void updateRecyclerView() {
        // Sort messages by timestamp to ensure correct order
        Collections.sort(messages, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));

        messageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove(); // Remove listener on destroy
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

}