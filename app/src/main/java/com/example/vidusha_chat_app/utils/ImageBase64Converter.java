package com.example.vidusha_chat_app.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageBase64Converter {

    public static String encodeImageToBase64(String imagePath) throws IOException {

        File file = new File(imagePath);
        Log.d("ChatActivity","uploaded img : 4" );
        byte[] fileContent = readFileToByteArray(file);
        Log.d("ChatActivity","uploaded img : 5" );
        return Base64.encodeToString(fileContent, Base64.DEFAULT);
    }

    public static String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private static byte[] readFileToByteArray(File file) throws IOException {
        Log.d("ChatActivity","uploaded img : start" );
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Log.d("ChatActivity","uploaded img : 6" );
            byte[] buffer = new byte[1024];
            Log.d("ChatActivity","uploaded img : 7" );
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            Log.d("ChatActivity","uploaded img : 8" );
            return bos.toByteArray();
        }
    }
}
