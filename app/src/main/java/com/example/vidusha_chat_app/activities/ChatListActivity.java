package com.example.vidusha_chat_app.activities;

import static android.Manifest.permission.READ_CONTACTS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidusha_chat_app.R;
import com.example.vidusha_chat_app.adapters.UserAdapter;
import com.example.vidusha_chat_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    private static final int REQUEST_SELECT_CONTACT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

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
        mAuth = FirebaseAuth.getInstance();
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

        Button addButton = findViewById(R.id.button5);
        Button logoutButton = findViewById(R.id.logout_button);
        addButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_PERMISSIONS);
            } else {
                openContactPicker();
            }
        });

        logoutButton.setOnClickListener(view -> {
            mAuth.signOut();

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ChatListActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        }
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();

            // Retrieve the contact’s phone number
            String phoneNumber = getPhoneNumber(contactUri);

            if (phoneNumber != null) {
                openSMSApp(phoneNumber, "Join with us");
            }
        }
    }

    private String getPhoneNumber(Uri contactUri) {
        String phoneNumber = null;
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int hasPhoneNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

            String contactId = cursor.getString(idIndex);
            String hasPhoneNumber = cursor.getString(hasPhoneNumberIndex);

            if (hasPhoneNumber.equals("1")) {
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    int phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    phoneNumber = phoneCursor.getString(phoneIndex);
                    phoneCursor.close();
                }
            }
            ((Cursor) cursor).close();
        }
        return phoneNumber;
    }

    private void openSMSApp(String phoneNumber, String message) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null));
        smsIntent.putExtra("sms_body", message);
        startActivity(smsIntent);
    }



}
