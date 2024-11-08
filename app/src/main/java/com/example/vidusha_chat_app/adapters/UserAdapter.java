package com.example.vidusha_chat_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.vidusha_chat_app.R;
import com.example.vidusha_chat_app.activities.ChatActivity;
import com.example.vidusha_chat_app.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_show_one_chat, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());

        // Load profile image using Glide
//        Glide.with(context)
//                .load(user.getProfilePicUrl())
//                .placeholder(R.drawable.img)
//                .into(holder.profileIcon);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("selectedUserId", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIcon;
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIcon = itemView.findViewById(R.id.profile_icon);
            userName = itemView.findViewById(R.id.user_name);
        }

    }


}
