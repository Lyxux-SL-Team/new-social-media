package com.example.vidusha_chat_app.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidusha_chat_app.R;
import com.example.vidusha_chat_app.activities.ChatActivity;
import com.example.vidusha_chat_app.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages = new ArrayList<>();
    private List<Message> selectedMessages = new ArrayList<>();
    private List<Message> filteredMessages = new ArrayList<>();
    private boolean isSearchActive = false;
    private  String currentUserId ;
//    private Context context;

    private ChatActivity chatActivity;

    // Optional constructor
    public MessageAdapter(List<Message> messages, String currentUserId, ChatActivity chatActivity) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.chatActivity = chatActivity;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        filteredMessages.clear();
        filteredMessages.addAll(messages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = isSearchActive ? filteredMessages.get(position) : messages.get(position);
        holder.bind(message);

        holder.itemView.setOnLongClickListener(v -> {
            // Enable selection mode when long pressed
            if (!selectedMessages.contains(message)) {
                selectedMessages.add(message); // Add message to selection
                notifyDataSetChanged(); // Update the view
                chatActivity.updateDeleteButtonVisibility();
                chatActivity.updateEditButtonVisibility();
            }
            return true; // Indicate that the long click was handled
        });

        if (message.getIsImage()) {
            // Decode the Base64 string to a bitmap and display it in an ImageView
            byte[] decodedString = Base64.decode(message.getContent(), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            holder.messageImage.setImageBitmap(decodedBitmap);
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
        } else {
            // Display text content if not an image
            holder.messageText.setText(message.getContent());
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
        }


        if (message.getChatId().equals(currentUserId)) {

            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(layoutParams);
            holder.messageText.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.blue));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
            params.gravity = Gravity.END;
            holder.itemView.setLayoutParams(params);
        } else {

            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(layoutParams);
            holder.messageText.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.red));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
            params.gravity = Gravity.START;
            holder.itemView.setLayoutParams(params);
        }

        holder.checkBoxSelect.setVisibility(selectedMessages.contains(message) ? View.VISIBLE : View.GONE);

        holder.checkBoxSelect.setChecked(selectedMessages.contains(message));

        holder.checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedMessages.add(message);
            } else {
                selectedMessages.remove(message);
            }

            chatActivity.updateDeleteButtonVisibility();
            chatActivity.updateEditButtonVisibility();
        });
    }

    @Override
    public int getItemCount() {
        return isSearchActive ? filteredMessages.size() : messages.size();
    }

    public List<Message> getSelectedMessages() {
        return selectedMessages;
    }

    public void removeMessages(List<Message> messagesToDelete) {
        messages.removeAll(messagesToDelete);
        notifyDataSetChanged();
    }

    public void updateMessage(List<Message> messagesToUpdate) {
        Log.d("ChatActivity","message adapter"+ messagesToUpdate);
        for (Message updatedMessage : messagesToUpdate) {
            for (int i = 0; i < messages.size(); i++) {
                Message currentMessage = messages.get(i);

                // Check if the message ID matches
                if (currentMessage.getMessageId().equals(updatedMessage.getMessageId())) {
                    // Update the message content or any other fields
                    messages.set(i, updatedMessage);

                    break; // Move to the next updated message
                }
            }
            notifyDataSetChanged();
        }

        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }

    public void filterMessages(String query) {
        if (query.isEmpty()) {
            isSearchActive = false;
        } else {
            isSearchActive = true;
            filteredMessages.clear();
            for (Message message : messages) {
                if (message.getContent() != null && message.getContent().toLowerCase().contains(query.toLowerCase())) {
                    filteredMessages.add(message);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private ImageView messageImage;
        private CheckBox checkBoxSelect;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageImage = itemView.findViewById(R.id.message_image);
            checkBoxSelect = itemView.findViewById(R.id.checkBox_select_message);
        }

        public void bind(Message message) {
            if (message.getIsImage()) {
                // If the message is an image, hide the text view and show the image
                messageText.setVisibility(View.GONE);
                messageImage.setVisibility(View.VISIBLE);

                // Decode Base64 string to Bitmap and display in ImageView
                byte[] decodedString = Base64.decode(message.getContent(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                messageImage.setImageBitmap(decodedBitmap);
            } else {
                // If the message is text, show the text view and hide the image view
                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
                messageText.setText(message.getContent());
            }
        }

        public void setCheckboxVisibility(boolean isVisible) {
            checkBoxSelect.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

}
