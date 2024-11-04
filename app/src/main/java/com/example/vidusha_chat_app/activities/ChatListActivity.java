package com.example.vidusha_chat_app.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidusha_chat_app.R;
import com.example.vidusha_chat_app.adapters.UserAdapter;
import com.example.vidusha_chat_app.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        // Initialize Firestore reference
        firestore = FirebaseFirestore.getInstance();
        Log.d("ChatListActivity", "Firestore initialized.");

        // Retrieve user data from Firestore
        firestore.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("ChatListActivity", "Failed to read users from Firestore", error);
                            return;
                        }

                        if (value != null && !value.isEmpty()) {
                            userList.clear(); // Clear previous data to avoid duplicates
                            for (QueryDocumentSnapshot document : value) {
                                User user = document.toObject(User.class);
                                userList.add(user);
                                Log.d("ChatListActivity", "User added: " + user.getName());
                            }
                            userAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("ChatListActivity", "No data found in 'users' collection.");
                        }
                    }
                });
    }
}
