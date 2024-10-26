package com.sarthak.youtubedownloader.service;

import android.content.Context;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class YoutubeTitleFetcher {

    private final String API_KEY;

    public YoutubeTitleFetcher(Context context) {
        API_KEY = loadApiKey(context);
    }

    private String loadApiKey(Context context) {
        try {
            Properties prop = new Properties();
            InputStream inputStream = context.getAssets().open("youtube.properties");
            prop.load(inputStream);
            return prop.getProperty("YOUTUBE_API_KEY");
        }
        catch (Exception e){
            System.out.println("Error loading YOUTUBE_API_KEY from youtube.properties: " + e);
            return null;
        }
    }

    public String fetchTitleFromYouTube(String videoId) {
        try {
            YouTube youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null
            ).setApplicationName("youtube-title-fetcher").build();

            YouTube.Videos.List request = youtubeService.videos()
                    .list("snippet")
                    .setId(videoId)
                    .setKey(API_KEY);

            VideoListResponse response = request.execute();
            Video video = response.getItems().get(0);
            return video.getSnippet().getTitle();
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("Error fetching Youtube title");
            return null;
        }
    }
}
