package com.example.vidusha_chat_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
        Message message = messages.get(position);
        holder.bind(message);


        if (message.getChatId().equals(currentUserId)) {

            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(layoutParams);
            holder.messageText.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.blue));
            holder.messageText.setGravity(View.TEXT_ALIGNMENT_TEXT_END);
        } else {

            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(layoutParams);
            holder.messageText.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.red));
            holder.messageText.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
        }

        holder.checkBoxSelect.setVisibility(View.VISIBLE);

        holder.checkBoxSelect.setChecked(selectedMessages.contains(message));

        holder.checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedMessages.add(message);
            } else {
                selectedMessages.remove(message);
            }

            chatActivity.updateDeleteButtonVisibility();
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public List<Message> getSelectedMessages() {
        return selectedMessages;
    }

    public void removeMessages(List<Message> messagesToDelete) {
        messages.removeAll(messagesToDelete);
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        CheckBox checkBoxSelect;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            checkBoxSelect = itemView.findViewById(R.id.checkBox_select_message);
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
        }
    }
}
