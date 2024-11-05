package com.example.vidusha_chat_app.database;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.vidusha_chat_app.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ChatApp.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_MESSAGES = "offline_messages";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CHAT_ID = "chatId";
    private static final String COLUMN_SENDER_ID = "senderId";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CHAT_ID + " TEXT, "
                + COLUMN_SENDER_ID + " TEXT, "
                + COLUMN_CONTENT + " TEXT, "
                + COLUMN_TIMESTAMP + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }

    public void saveMessageOffline(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_ID,message.getChatId());
        values.put(COLUMN_SENDER_ID, message.getSenderId());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public List<Message> getOfflineMessage() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MESSAGES, null, null, null, null, null, COLUMN_TIMESTAMP);

        if(cursor.moveToFirst()){
            do {
                Message message = new Message();
                message.setChatId(cursor.getString(cursor.getColumnIndex(COLUMN_CHAT_ID)));
                message.setSenderId(cursor.getString(cursor.getColumnIndex(COLUMN_SENDER_ID)));
                message.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));
                message.setTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
                messages.add(message);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }
    public void deleteOfflineMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, null, null);
        db.close();
    }
}
