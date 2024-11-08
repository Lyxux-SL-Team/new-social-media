package com.example.vidusha_chat_app.models;

public class Message {
    private String messageId;
    private String senderId;
    private String content;
    private long timestamp;
    private String chatId;
    private String type;

    private boolean isImage;

    public Message() {};

    public Message(String messageId, String senderId, String content, long timestamp, String chatId, boolean isImage){
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.chatId = chatId;
        this.isImage = isImage;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }
}
