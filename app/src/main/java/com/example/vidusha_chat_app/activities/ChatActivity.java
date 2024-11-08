package com.example.vidusha_chat_app.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidusha_chat_app.R;
import com.example.vidusha_chat_app.adapters.MessageAdapter;
import com.example.vidusha_chat_app.database.MessageDatabaseHelper;
import com.example.vidusha_chat_app.models.Message;
import com.example.vidusha_chat_app.utils.ImageBase64Converter;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 123;
    private FirebaseFirestore firestore;
    private String userId;
    private String chatId;
    private MessageAdapter messageAdapter;
    private ListenerRegistration listenerRegistration;

    private List<Message> messages;
    private List<Message> filteredMessges= new ArrayList<>();
    private boolean isSearchActive = false;

    private RecyclerView recyclerView;

    private MessageDatabaseHelper dbHelper;

    private Button deleteButton, editButton;

    private SearchView searchTextView;

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
        messageAdapter = new MessageAdapter(messages, chatId, this);
        recyclerView = findViewById(R.id.posts_recycler_view);
        ImageButton btnPickImage = findViewById(R.id.imageButton);
        EditText editTextText = findViewById(R.id.editTextText);
        Button buttonPost = findViewById(R.id.button3);
        deleteButton = findViewById(R.id.delete_button);
        editButton = findViewById(R.id.edit_button);
        searchTextView = findViewById(R.id.search_view);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        listenForMessages();


        deleteButton.setOnClickListener(v -> {
            // Delete selected messages
            List<Message> selectedMessages = messageAdapter.getSelectedMessages();
            deleteMessages(selectedMessages);
        });

        editButton.setOnClickListener(v -> {
            List<Message> selectedMessages = messageAdapter.getSelectedMessages();
            convertToMessageToEdit(selectedMessages);
        });
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = editTextText.getText().toString().trim();
                if (!messageContent.isEmpty()) {
                    if(messageAdapter.getSelectedMessages().isEmpty()){
                        sendMessage(messageContent , false);
                    }else {
                        List<Message> selectedMessages = messageAdapter.getSelectedMessages();
                        updateMessages(selectedMessages);
                    }

                    editTextText.setText(""); // Clear input field
                } else {
                    Toast.makeText(ChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }

        });

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
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

        searchTextView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMessages(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                messageAdapter.filterMessages(newText);
                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");

            if (photo != null) {

                String imageCode = ImageBase64Converter.encodeBitmapToBase64(photo);
                Log.d("ChatActivity", "Captured image code: " + imageCode);
                sendMessage(imageCode, true);
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri imageUri = data.getData();
            Log.d("ChatActivity", "Uploaded img URI: " + imageUri);
            String filePath = getFilePathFromUri(imageUri);

            if (filePath != null) {
                try {

                    String imageCode = ImageBase64Converter.encodeImageToBase64(filePath);
                    Log.d("ChatActivity", "Converted image code: " + imageCode);
                    sendMessage(imageCode, true);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private String getFilePathFromUri(Uri uri) {
        try {
            // Create a temporary file to copy the content from the URI
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);

            File tempFile = File.createTempFile("temp_image", ".jpg", getCacheDir());
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            inputStream.close();

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }



    private void sendMessage(String content, boolean isImage) {
        Message message = new Message();
        message.setSenderId(userId);
        message.setMessageId(userId+chatId+"_"+System.currentTimeMillis());
        message.setContent(content);
        message.setTimestamp(System.currentTimeMillis());
        message.setChatId(chatId);
        message.setIsImage(isImage);

        if(isConnected()){
            // Save the message to Firestore


            firestore.collection("messages").document(message.getMessageId()).set(message)
                    .addOnSuccessListener(documentReference -> {

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
        List<Message> displayList = isSearchActive ? filteredMessges : messages;

        Log.d("ChatActivity","updated display msg list " + displayList.size());
        Collections.sort(displayList, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));


        messageAdapter.updateMessage(displayList);


        messageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(displayList.size() - 1);
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

    public void updateDeleteButtonVisibility() {
        List<Message> selectedMessages = messageAdapter.getSelectedMessages();
        if (selectedMessages.size() > 0) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    public void updateEditButtonVisibility() {
        List<Message> selectedMessages = messageAdapter.getSelectedMessages();
        if (selectedMessages.size() > 0) {
            editButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
        }
    }

    private void deleteMessages(List<Message> selectedMessages) {
        for (Message message : selectedMessages) {
            String messageId = message.getMessageId();
            Log.d("chatActivity", "Attempting to delete message with ID: " + messageId);

            // Check if messageId is valid
            if (messageId != null && !messageId.isEmpty()) {
                firestore.collection("messages")
                        .document(messageId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            messageAdapter.removeMessages(selectedMessages);
                            Toast.makeText(this, "Messages deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("chatActivity", "Error deleting message: " + e.getMessage());
                            Toast.makeText(this, "Error deleting messages", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e("chatActivity", "Message ID is null or empty");
                Toast.makeText(this, "Invalid message ID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void convertToMessageToEdit(List<Message> selectedMessages){
        for (Message message: selectedMessages) {
            String content = message.getContent();
            EditText editTextText = findViewById(R.id.editTextText);

            editTextText.setText(content);
        }
    }
    private void updateMessages(List<Message> selectedMessages) {
        List<Message> updatedMessages = new ArrayList<>();
        EditText editTextText = findViewById(R.id.editTextText);
        String updatedText = editTextText.getText().toString().trim();

        Log.d("ChatActivity", "Updated message: " + updatedText);

        for (Message message : selectedMessages) {
            String messageId = message.getMessageId();
            if (messageId != null && !messageId.isEmpty()) {
                firestore.collection("messages")
                        .document(messageId)
                        .get()  // Check if document exists
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("content", updatedText);

                                firestore.collection("messages")
                                        .document(messageId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            message.setContent(updatedText);
                                            updatedMessages.add(message);

                                            if (updatedMessages.size() == selectedMessages.size()) {
                                                messageAdapter.updateMessage(updatedMessages);
                                                Log.d("ChatActivity", "Messages updated successfully");
                                                Toast.makeText(this, "Messages updated", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("ChatActivity", "Error updating message: " + e.getMessage());
                                            Toast.makeText(this, "Error updating messages", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e("ChatActivity", "Document with ID " + messageId + " does not exist.");
                                Toast.makeText(this, "Message not found in Firestore", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ChatActivity", "Error checking document existence: " + e.getMessage());
                            Toast.makeText(this, "Error finding message in Firestore", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e("ChatActivity", "Invalid message ID");
                Toast.makeText(this, "Invalid message ID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select sourece");
        builder.setItems(new CharSequence[]{"Take a photo", "Choose from gallery"},((dialog, which) -> {
            if(which == 0) {
                openCamera();
            }else {
                openGallery();
            }
        }));
        builder.show();
    }

    private void openCamera(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
    }

    private void filterMessages(String query) {

        if (filteredMessges == null) {
            filteredMessges = new ArrayList<>();
        }
        isSearchActive = !query.trim().isEmpty();
        List<Message> tempFilteredMessages = new ArrayList<>();
        Log.d("ChatActivity", "typed text" + query);

        if(!query.trim().isEmpty()){
            for (Message message : messages) {
                String messageText = message.getContent();


                if (messageText != null && messageText.toLowerCase().contains(query.toLowerCase())) {
                    if(messageText.equals(query)){
                        tempFilteredMessages.add(message);
                        Log.d("ChatActivity", "filtered messages " + message);

                    }
                }
            }
        }
        filteredMessges.clear();
        filteredMessges.addAll(tempFilteredMessages);
        updateRecyclerView();

    }
}