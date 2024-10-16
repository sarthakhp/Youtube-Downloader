package com.sarthak.youtubedownloader.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.SessionState;
import com.sarthak.youtubedownloader.util.CommonUtil;

import java.io.File;

public class FFmpegHelper {

    public File mergeVideoAndAudio(String videoPath, String audioPath) {

        File originalVideoFile = new File(videoPath);
        File originalAudioFile = new File(audioPath);

        if (isNull(originalVideoFile) || !originalVideoFile.exists() || isNull(originalAudioFile) || !originalAudioFile.exists()) {
            System.out.println("Files do not exist! : " + originalVideoFile + " " + originalAudioFile);
            return null;
        }

        // Temp output path (as you cannot overwrite the input file directly)
        String tempOutputPath = CommonUtil.removeExtension(videoPath) + "_temp" + ".mp4";
        System.out.println("Output path: " + tempOutputPath);

        String[] ffmpegCommandArray = {
                "-i", "\"" + videoPath + "\"",  // Input video file
                "-i", "\"" + audioPath + "\"",  // Input audio file
                "-c:v", "copy",   // Copy video without re-encoding
                "-c:a", "aac",    // Encode audio to AAC
                "-strict", "experimental",
                "\"" + tempOutputPath + "\""    // Temporary output file
        };

        String finalFFmpegCommand = String.join(" ", ffmpegCommandArray);
        System.out.println(finalFFmpegCommand);

        Session session = FFmpegKit.execute(finalFFmpegCommand);

        SessionState state = session.getState();
        ReturnCode returnCode = session.getReturnCode();

        System.out.println("Merging was successful!");

        // Replace the original video file with the merged output
        replaceOriginalVideoFile(videoPath, tempOutputPath);

        // Delete the audio file
        deleteFileV2(audioPath);

        // CALLED WHEN SESSION IS EXECUTED
        System.out.println(String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));

        File mergedAudioVideoFile = new File(videoPath);
        if (nonNull(mergedAudioVideoFile) && mergedAudioVideoFile.exists()) {
            return mergedAudioVideoFile;
        }
        return null;

    }

    // Method to replace the original video file with the merged video
    private void replaceOriginalVideoFile(String originalVideoPath, String tempOutputPath) {
        File originalVideoFile = new File(originalVideoPath);
        File tempOutputFile = new File(tempOutputPath);

        boolean fileReplacementSuccessStatus = false;
        if (tempOutputFile.exists()) {
            // Delete the original video file
            if (originalVideoFile.delete()) {
                // Rename the temp file to the original file name
                try {
                    fileReplacementSuccessStatus = tempOutputFile.renameTo(originalVideoFile);
                }
                catch (Exception e) {
                    fileReplacementSuccessStatus = false;
                    Log.e("FFmpegHelper", "Rename the temp file to the original file name failed", e);
                }
            }
        }
        if (fileReplacementSuccessStatus) {
            System.out.println("Original video file replaced successfully.");
        }
        else {
            System.out.println("Failed to delete original video file.");
        }
    }

    // Method to delete a file
    private void deleteFileV2(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.delete()) {
            System.out.println("Audio file deleted successfully.");
        } else {
            System.out.println("Failed to delete audio file.");
        }
    }

}
