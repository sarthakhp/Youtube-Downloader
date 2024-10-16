package com.sarthak.youtubedownloader.activity;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.client.Client;
import com.github.kiulian.downloader.downloader.client.Clients;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.search.SearchResultVideoDetails;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.google.android.material.snackbar.Snackbar;
import com.sarthak.youtubedownloader.adapters.DownloadFormatOptionsAdapter;
import com.sarthak.youtubedownloader.R;
import com.sarthak.youtubedownloader.constants.IntentKeys;
import com.sarthak.youtubedownloader.dto.DownloadVideoTaskParams;
import com.sarthak.youtubedownloader.service.FFmpegHelper;
import com.sarthak.youtubedownloader.util.CommonUtil;
import com.sarthak.youtubedownloader.util.JsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadFileActivity extends BaseActivity {

    private YoutubeDownloader downloader;
    private ProgressBar downloadProgressBar;
    private SearchResultVideoDetails searchResultVideoDetails;
    private TextView titleTextView;
    private Button downloadButton;
    private ImageView fileImageView;
    private ListView downloadOptionsListView;
    private DownloadFormatOptionsAdapter downloadFormatOptionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);

        setupToolbar("Youtube Downloader");

        downloadButton = findViewById(R.id.download_button);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);
        titleTextView = findViewById(R.id.file_title);
        TextView fileTimeTextView = findViewById(R.id.file_time_text_id);
        fileImageView = findViewById(R.id.file_image_view);
        downloader = new YoutubeDownloader();
        downloadFormatOptionsAdapter = new DownloadFormatOptionsAdapter(this, new ArrayList<>());

        downloadOptionsListView = findViewById(R.id.download_options_list_view_id);
        downloadOptionsListView.setAdapter(downloadFormatOptionsAdapter);

        downloadOptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DownloadVideoTaskParams downloadVideoTaskParams = (DownloadVideoTaskParams) parent.getItemAtPosition(position);
                new DownloadVideoTask().execute(downloadVideoTaskParams);
            }
        });


        String searchResultItemJsonString = getIntent().getStringExtra(IntentKeys.SEARCH_RESULT_ITEM_EXTRA);
        searchResultVideoDetails = JsonUtil.convertJsonStringToObj(searchResultItemJsonString, SearchResultVideoDetails.class);
        if (nonNull(searchResultVideoDetails)) {

            titleTextView.setText(searchResultVideoDetails.title());
            fileTimeTextView.setText(CommonUtil.convertSecondsToReadableTime(searchResultVideoDetails.lengthSeconds()));

            Glide.with(this).load(searchResultVideoDetails.thumbnails().get(0)).into(fileImageView);

            new VideoInfoTask().execute(searchResultVideoDetails);
        }
        else {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show();
        }

        downloadProgressBar.setVisibility(View.GONE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // This is the ID for the Up button
                onBackPressed(); // Go back to the previous activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class VideoInfoTask extends AsyncTask<SearchResultVideoDetails, Void, Void> {

        @Override
        protected Void doInBackground(SearchResultVideoDetails... searchResultVideoDetailsParams) {
            updateDownloadOptions(searchResultVideoDetailsParams[0]);
            return null;
        }
    }

    private void updateDownloadOptions(SearchResultVideoDetails searchResultVideoDetails) {

        System.out.println("Fetching Video Info");

        VideoInfo videoInfo = fetchVideoInfo(searchResultVideoDetails.videoId());

        if (isNull(videoInfo)) {
            CommonUtil.toastOnMainThread(DownloadFileActivity.this, "Could not find Video Details", Toast.LENGTH_LONG);
            return;
        }

        addVideoFormatDownloadOptions(videoInfo);

    }

    private void addVideoFormatDownloadOptions(@NonNull VideoInfo videoInfo) {

        if (CommonUtil.isEmpty(videoInfo.formats())) {
            CommonUtil.toastOnMainThread(DownloadFileActivity.this, "No Formats found!", Toast.LENGTH_LONG);
            System.out.println("No Formats found for videoInfo: " + JsonUtil.convertObjToJson(videoInfo));
            return;
        }

        List<DownloadVideoTaskParams> videoTaskParamsList = new ArrayList<>();
        videoTaskParamsList.add(new DownloadVideoTaskParams(videoInfo, videoInfo.bestVideoWithAudioFormat()));
        videoTaskParamsList.add(new DownloadVideoTaskParams(videoInfo, videoInfo.bestAudioFormat()));
        videoTaskParamsList.add(new DownloadVideoTaskParams(videoInfo, videoInfo.bestVideoFormat()));

        for (Format format: videoInfo.formats()) {
            videoTaskParamsList.add(new DownloadVideoTaskParams(videoInfo, format));
        }

        DownloadFileActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadFormatOptionsAdapter.clear();
                downloadFormatOptionsAdapter.addAll(videoTaskParamsList);
                downloadFormatOptionsAdapter.notifyDataSetChanged();
            }
        });
    }

    private VideoInfo fetchVideoInfo(String videoId) {
        System.out.println("Video ID: " + videoId);

        // sync parsing

        VideoInfo videoInfo = null;
        for (Client client: Clients.defaultClients()) {
            System.out.println("Client: " + client.getType().getName());
            RequestVideoInfo requestVideoInfo = new RequestVideoInfo(videoId);
            requestVideoInfo.clientType(client.getType());
            Response<VideoInfo> videoInfoResponse = downloader.getVideoInfo(requestVideoInfo);
            videoInfo = videoInfoResponse.data();
            if (isNull(videoInfo)) {
                System.out.println("videoInfoResponse error: " + videoInfoResponse.error());
            }
            else {
                break;
            }
        }
        return videoInfo;
    }

    private class DownloadVideoTask extends AsyncTask<DownloadVideoTaskParams, Void, Void> {

        @Override
        protected Void doInBackground(DownloadVideoTaskParams... downloadVideoTaskParams) {
            updateLoadingBarVisibility(View.VISIBLE);
            File downloadedFile = downloadVideo(downloadVideoTaskParams[0]);

            if (isNull(downloadedFile)) {
                CommonUtil.toastOnMainThread(DownloadFileActivity.this, "Downloading Failed!", Toast.LENGTH_LONG);
                return null;
            }

            showSnackBar(downloadedFile);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
        }
    }

    private void updateLoadingBarVisibility(int visible) {
        DownloadFileActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (View.GONE == visible) {
                    downloadProgressBar.setProgress(0);
                }
                downloadProgressBar.setVisibility(visible);
            }
        });
    }

    private void updateLoadingBar(int progress) {
        updateLoadingBarVisibility(View.VISIBLE);

        DownloadFileActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadProgressBar.setIndeterminate(false);
                downloadProgressBar.setProgress(progress);
            }
        });
    }

    private void updateLoadingBarIndeterminate(boolean isIndeterminate) {
        updateLoadingBarVisibility(View.VISIBLE);

        DownloadFileActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadProgressBar.setIndeterminate(isIndeterminate);
            }
        });
    }

    private void removeLoadingBar() {
        updateLoadingBarVisibility(View.GONE);
    }

    public File downloadVideo(DownloadVideoTaskParams downloadVideoTaskParams) {

        System.out.println("Starting Main file download");

        VideoDetails videoDetails = downloadVideoTaskParams.getVideoInfo().details();
        Format format = downloadVideoTaskParams.getFormat();

        File finalFile = null;

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fileDir = new File(storageDir.getAbsolutePath());

        if (Format.AUDIO_VIDEO.equals(format.type()) || Format.AUDIO.equals(format.type())) {
            finalFile = getSingleFile(videoDetails, format, fileDir);
        }
        else if (Format.VIDEO.equals(format.type())) {
            finalFile = getMergedAudioVideoFile(downloadVideoTaskParams);
        }

        return finalFile;
    }

    private File getSingleFile(VideoDetails videoDetails, Format format, File fileDir) {

        updateLoadingBarVisibility(View.VISIBLE);
        String finalFileName = getFinalFileName(videoDetails, format);
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .callback(new YoutubeProgressCallback<File>() {

                    @Override
                    public void onDownloading(int progress) {
                        updateLoadingBar(progress);
                    }


                    @Override
                    public void onFinished(File videoFile) {

                        removeLoadingBar();
                        System.out.println("Finished downloading file");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        removeLoadingBar();
                        System.out.println("Error: " + throwable.getLocalizedMessage());
                    }
                })
                .saveTo(fileDir) // by default "videos" directory
                .renameTo(finalFileName) // by default file name will be same as videoInfo title on youtube
                .overwriteIfExists(true) // if false and file with such name already exits sufix will be added videoInfo(1).mp4
//                .maxRetries(3)
                .async();
        Response<File> response = downloader.downloadVideoFile(request);
        File downloadedFile = response.data();

        if (isNull(downloadedFile)) {
            System.out.println("Main file is NULL");
        }
        else {
            System.out.println("Main file NON NULL");
        }
        return downloadedFile;
    }

    @NonNull
    private static String getFinalFileName(VideoDetails videoDetails, Format format) {
        return CommonUtil.getFormatLabel(format) + " " + videoDetails.title();
    }

    private File getMergedAudioVideoFile(DownloadVideoTaskParams downloadVideoTaskParams) {

        System.out.println("Starting Temp Files download");

        VideoDetails videoDetails = downloadVideoTaskParams.getVideoInfo().details();
        AudioFormat audioFormat = downloadVideoTaskParams.getVideoInfo().bestAudioFormat();
        Format videoFormat = downloadVideoTaskParams.getFormat();

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fileDir = new File(storageDir.getAbsolutePath());

        File audioFile = getSingleFile(videoDetails, audioFormat, fileDir);
        if (isNull(audioFile)) {
            System.out.println("Audio file is NULL");
            return null;
        }
        File videoFile = getSingleFile(videoDetails, videoFormat, fileDir);
        if (isNull(videoFile)) {
            System.out.println("Video file is NULL");
            return null;
        }

        FFmpegHelper fFmpegHelper = new FFmpegHelper();
        updateLoadingBarVisibility(View.VISIBLE);
        updateLoadingBarIndeterminate(true);
        File mergedAudioVideoFile = fFmpegHelper.mergeVideoAndAudio(videoFile.getAbsolutePath(), audioFile.getAbsolutePath());
        updateLoadingBarIndeterminate(false);
        updateLoadingBarVisibility(View.GONE);
        if (isNull(mergedAudioVideoFile)) {
            System.out.println("Error merging files");
        }
        return mergedAudioVideoFile;

    }

    private void showSnackBar(File videoFile) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, "File downloaded", 5000)
                .setAction("Show File", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle the click action here
                        // For example, open the file

                        if (nonNull(videoFile) && videoFile.exists()) {
                            Uri videoUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", videoFile);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(videoUri, "video/*");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant permission to read URI
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(Intent.createChooser(intent, "Open Video"));
                        } else {
                            // Handle the case when the file doesn't exist
                            System.out.println("Video file does not exist.");
                        }
                    }
                });

        // Show the Snackbar
        snackbar.show();
    }

}
