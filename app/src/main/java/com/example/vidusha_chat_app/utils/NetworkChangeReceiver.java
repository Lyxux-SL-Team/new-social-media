package com.example.vidusha_chat_app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.vidusha_chat_app.database.MessageDatabaseHelper;
import com.example.vidusha_chat_app.models.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isConnected(context)) {
            // Device is online, sync offline messages
            syncOfflineMessages(context);
        }
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void syncOfflineMessages(Context context) {
        MessageDatabaseHelper dbHelper = new MessageDatabaseHelper(context);
        List<Message> offlineMessages = dbHelper.getOfflineMessage();

        if (!offlineMessages.isEmpty()) {
            CollectionReference messagesRef = FirebaseFirestore.getInstance().collection("messages");
            for (Message message : offlineMessages) {
                messagesRef.add(message)
                        .addOnSuccessListener(documentReference -> Log.d("NetworkChangeReceiver", "Message sent successfully"))
                        .addOnFailureListener(e -> Log.e("NetworkChangeReceiver", "Error sending message", e));
            }

            // Clear offline messages once synced
            dbHelper.deleteOfflineMessages();
        }
    }
}
