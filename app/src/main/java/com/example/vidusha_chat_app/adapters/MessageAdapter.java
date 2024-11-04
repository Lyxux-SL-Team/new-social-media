package com.example.vidusha_chat_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidusha_chat_app.R;
import com.example.vidusha_chat_app.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages = new ArrayList<>();
    private  String currentUserId ;

    // Optional constructor
    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.currentUserId = currentUserId;
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
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
        }
    }
}
