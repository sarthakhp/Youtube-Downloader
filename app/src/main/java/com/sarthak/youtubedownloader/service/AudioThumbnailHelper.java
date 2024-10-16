package com.sarthak.youtubedownloader.service;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AudioThumbnailHelper {

    // Method to download the image from URL and associate it with the audio file
    public static void addAudioThumbnailFromUrl(Context context, String audioPath, String imageUrl) {
        // Use Glide to download the image and convert to Bitmap
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        // When the image is successfully loaded, associate it with the audio file
                        saveAudioThumbnail(context, audioPath, bitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle cleanup if necessary
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e("AudioThumbnailHelper", "Failed to download image from URL");
                    }
                });
    }

    // Method to associate the Bitmap as a thumbnail for the audio file
    private static void saveAudioThumbnail(Context context, String audioPath, Bitmap thumbnail) {
        try {
            // Insert metadata for the audio file in MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            values.put(MediaStore.Audio.Media.DATA, audioPath); // Path to your audio file
            values.put(MediaStore.Audio.Media.ALBUM, "My Album");
            values.put(MediaStore.Audio.Media.TITLE, "My Audio");
            values.put(MediaStore.Audio.Media.ARTIST, "Artist Name");

            // Adding the thumbnail as album art
//            values.put(MediaStore.Audio.Media.ALBUM_ART, getAlbumArtUri(context, thumbnail));

            // Insert into the MediaStore
            context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to save the album art as a file and return its Uri
    private static String getAlbumArtUri(Context context, Bitmap thumbnail) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Audio Thumbnail");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Insert the image into MediaStore and get the Uri
        return MediaStore.Images.Media.insertImage(context.getContentResolver(), thumbnail, "Audio Thumbnail", null);
    }
}
