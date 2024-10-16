package com.sarthak.youtubedownloader;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestSearchResult;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.field.TypeField;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;

import java.io.File;
import java.util.List;

public class TempMain {
//    public static void main(String[] args) {
//        try {
//            // ex: http://www.youtube.com/watch?v=Nj6PFaDmp6c
//            String url = "https://www.youtube.com/watch?v=S3Dpfyc15qQ&ab_channel=IntroAndOutro";
//            // ex: "/Users/axet/Downloads"
//            String path = "C:/Users/Sarthak Patel/Downloads";
//            VGet v = new VGet(new URL(url), new File(path));
//            System.out.println("Starting");
//            v.download();
//            System.out.println("Done?");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
////        System.out.println("hi");
//    }

    public static void main(String[] args) {

        YoutubeDownloader downloader = new YoutubeDownloader();


        String videoId = "GvXDq-P1NB8"; // for url https://www.youtube.com/watch?v=abc12345 https://www.youtube.com/watch?v=S3Dpfyc15qQ

// sync parsing
        RequestVideoInfo requestVideoInfo = new RequestVideoInfo(videoId);
        Response<VideoInfo> videoInfoResponse = downloader.getVideoInfo(requestVideoInfo);
        VideoInfo video = videoInfoResponse.data();
//        RequestVideoFileDownload requestVideoFileDownload = new RequestVideoFileDownload();

        VideoDetails videoDetails = video.details();
        System.out.println(videoDetails.title());
        System.out.println(videoDetails.viewCount());

        Format format = video.bestVideoWithAudioFormat();
        format = video.bestVideoFormat();
//        format = video.bestAudioFormat();

        // get audio formats
        List<AudioFormat> audioFormats = video.audioFormats();
        audioFormats.forEach(it -> {
            System.out.println(it.audioQuality() + " : " + it.url());
        });



        // get all videos formats (may contain better quality but without audio)
        List<VideoFormat> videoFormats = video.videoFormats();
        videoFormats.forEach(it -> {
            System.out.println(it.videoQuality() + " : " + it.url());
        });

        File videoFileDir = new File("C:/Users/Sarthak Patel/Downloads/my_java_downloads");
//
        // sync downloading
//        RequestVideoFileDownload requestVideoFileDownload = new RequestVideoFileDownload(format)
//                // optional params
//                .saveTo(videoFileDir) // by default "videos" directory
//                .renameTo(format.type() + "-" + videoDetails.title()) // by default file name will be same as video title on youtube
//                .overwriteIfExists(true); // if false and file with such name already exits sufix will be added video(1).mp4
//        Response<File> downloadVideoFileResponse = downloader.downloadVideoFile(requestVideoFileDownload);
//        File data = downloadVideoFileResponse.data();

// async downloading with callback
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .callback(new YoutubeProgressCallback<File>() {
                    @Override
                    public void onDownloading(int progress) {
                        System.out.printf("Downloaded %d%%\n", progress);
                    }

                    @Override
                    public void onFinished(File videoInfo) {
                        System.out.println("Finished file: " + videoInfo);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("Error: " + throwable.getLocalizedMessage());
                    }
                })
                .saveTo(videoFileDir) // by default "videos" directory
                .renameTo(format.type() + "-" + videoDetails.title()) // by default file name will be same as video title on youtube
                .overwriteIfExists(true) // if false and file with such name already exits sufix will be added video(1).mp4
                .async();
        System.out.println("before");
        Response<File> response = downloader.downloadVideoFile(request);
        System.out.println("Here");
//        File data = response.data(); // will block current thread
        System.out.println("Donee");

        RequestSearchResult requestSearchResult = new RequestSearchResult("Teri Baaton Mein Aisa Uljha Jiya")
                // filters
                .type(TypeField.VIDEO);             // Videos only
//                .format(FormatField._3D,
//                        FormatField.HD)                    // 3D HD videos
//                .match(FeatureField.SUBTITLES)         // with subtitles
//                .during(DurationField.OVER_20_MINUTES) // more than 20 minutes videos
//                .uploadedThis(UploadDateField.MONTH)   // uploaded this month
//
//                // other parameters
//                .forceExactQuery(true)                 // avoid auto correction
//                .sortBy(SortField.VIEW_COUNT);         // results sorted by view count


        SearchResult searchResult = downloader.search(requestSearchResult).data();
        searchResult.items().get(0).asVideo().title();
    }
}
