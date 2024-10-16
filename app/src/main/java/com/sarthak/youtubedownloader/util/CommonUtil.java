package com.sarthak.youtubedownloader.util;

import static java.util.Objects.isNull;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.kiulian.downloader.model.videos.formats.Format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class CommonUtil {

    public static final String BLANK = "";

    public static void toastOnMainThread(AppCompatActivity appCompatActivity, String msg, int duration) {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(appCompatActivity, msg, duration).show();
            }
        });
    }

    public static <T> T resolve(Supplier<T> resolver, T defaultValue) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result).orElse(defaultValue);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public static void resolve(Supplier resolver) {
        try {
            resolver.get();
        }
        catch (Exception e) {
        }
    }

    public static String removeExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(0, fileName.indexOf("."));
        }
        return fileName; // Return the original string if no dot is present
    }

    public static String getExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.indexOf("."));
        }
        return fileName; // Return the original string if no dot is present
    }

    public static String convertBytesToMB(Long contentLength) {
        if (isNull(contentLength)) {
            return BLANK;
        }
        double fileSizeInMB = (double) contentLength / (1024 * 1024); // Convert to MB
        BigDecimal bigDecimal = new BigDecimal(fileSizeInMB);
        bigDecimal = bigDecimal.setScale(2, RoundingMode.UP);
        return bigDecimal + " MB"; // Format to 2 decimal places
    }

    public static String getFormatLabel(Format format) {
        return "[" + format.type().toUpperCase() + "]";
    }

    public static boolean isEmpty(Collection c) {
        return isNull(c) || c.size() <= 0;
    }

    public static <T> Collection<T> nullSafeList(Collection<T> c) {
        if (isNull(c)) {
            return new ArrayList<>();
        }
        return c;
    }

    public static String convertSecondsToReadableTime(long seconds) {
        // Calculate hours, minutes, and seconds
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        // Build the readable string
        StringBuilder readableTime = new StringBuilder();

        if (hours > 0) {
            readableTime.append(hours).append(" hour");
            if (hours > 1) {
                readableTime.append("s");
            }
            readableTime.append(", ");
        }

        if (minutes > 0) {
            readableTime.append(minutes).append(" minute");
            if (minutes > 1) {
                readableTime.append("s");
            }
            readableTime.append(", ");
        }

        readableTime.append(remainingSeconds).append(" second");
        if (remainingSeconds > 1 || remainingSeconds == 0) {
            readableTime.append("s");
        }

        return readableTime.toString();
    }

    public static String extractVideoIdIfUrl(String url) {
        String videoId = url;
//        String pattern = "^(?:https?:\\/\\/)?(?:www\\.)?(?:youtube\\.com\\/.*v=|youtu\\.be\\/)([^&\\n?#]+)";
        String pattern = "^(?:https?:\\/\\/)?(?:www\\.)?(?:youtube\\.com\\/.*(?:v=|shorts/)|youtu\\.be\\/)([^&\\n?#]+)";
        // Using regex to extract the video ID
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find()) {
            videoId = matcher.group(1);  // Group 1 contains the video ID
        }

        return videoId;
    }

    public static void main(String[] args) {
        System.out.println((extractVideoIdIfUrl("https://youtube.com/shorts/_6xrLhp1sSQ?si=P7aTD7Q-L34Iwstj")));
    }
}
