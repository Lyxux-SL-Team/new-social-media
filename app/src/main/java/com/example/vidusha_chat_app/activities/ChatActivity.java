package com.example.vidusha_chat_app.activities;

import android.content.SharedPreferences;
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
import com.example.vidusha_chat_app.models.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.vidusha_chat_app.R;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentChange;


import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private String userId;
    private String chatId;
    private MessageAdapter messageAdapter;
    private ListenerRegistration listenerRegistration;

    private List<Message> messages;
    private RecyclerView recyclerView;

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

        // Save the message to Firestore
        CollectionReference messagesRef = firestore.collection("messages");
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    // Optionally handle success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                });
        listenForMessages();
    }

    private void listenForMessages() {
        messages.clear(); // Clear existing messages to avoid duplication

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
                updateRecyclerView();
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
                updateRecyclerView();
            }
        });

        // Store these listener registrations to remove them later if needed
        listenerRegistration = chatIdListener;
        // Optionally store senderIdListener if you need to remove it later as well
    }

    // Method to update the RecyclerView and scroll to the latest message
    private void updateRecyclerView() {
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

}