package com.sarthak.youtubedownloader.dto;

import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;

public class DownloadVideoTaskParams {
    private VideoInfo videoInfo;
    private Format format;
    private String formatLabel;

    public DownloadVideoTaskParams(VideoInfo videoInfo, Format format) {
        this.videoInfo = videoInfo;
        this.format = format;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }
}
