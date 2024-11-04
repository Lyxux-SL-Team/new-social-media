package com.example.vidusha_chat_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidusha_chat_app.adapters.MessageAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.vidusha_chat_app.R;

public class ChatActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private String userId;
    private String chatId;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        firestore = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");
        chatId = getIntent().getStringExtra("chatId");

        RecyclerView recyclerView = findViewById(R.id.posts_recycler_view);
        ImageButton btnPickImage = findViewById(R.id.imageButton);
        EditText editTextText = findViewById(R.id.editTextText);
        Button buttonPost = findViewById(R.id.button3);

        messageAdapter = new MessageAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
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



}
