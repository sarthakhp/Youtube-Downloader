package com.sarthak.youtubedownloader.service;

import static java.util.Objects.isNull;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestSearchResult;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.SearchResultItem;
import com.github.kiulian.downloader.model.search.SearchResultItemType;
import com.github.kiulian.downloader.model.search.SearchResultVideoDetails;
import com.github.kiulian.downloader.model.search.field.TypeField;
import com.sarthak.youtubedownloader.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SearchService extends Service {

    private Context context;

    public SearchService(Context context) {
        this.context = context;
    }

    public List<SearchResultVideoDetails> getVideoTitles(String searchQuery) {

        searchQuery = cleanSearchQueryText(searchQuery, context);

        RequestSearchResult requestSearchResult = new RequestSearchResult(searchQuery)
                // filters
                .type(TypeField.VIDEO)        // Videos only
                .maxRetries(3);
//                .format(FormatField._3D,
//                        FormatField.HD)                    // 3D HD videos
//                .match(FeatureField.SUBTITLES)         // with subtitles
//                .during(DurationField.OVER_20_MINUTES) // more than 20 minutes videos
//                .uploadedThis(UploadDateField.MONTH)   // uploaded this month
//
//                // other parameters
//                .forceExactQuery(true)                 // avoid auto correction
//                .sortBy(SortField.VIEW_COUNT);         // results sorted by view count


        YoutubeDownloader downloader = new YoutubeDownloader();
        SearchResult searchResult = downloader.search(requestSearchResult).data();

        if (isNull(searchResult)) {
            return new ArrayList<>();
        }

        return CommonUtil.resolve(() -> searchResult.items(), new ArrayList<SearchResultItem>()).stream()
                .filter(Objects::nonNull)
                .filter(searchResultItem -> SearchResultItemType.VIDEO.equals(searchResultItem.type()))
                .map(searchResultItem -> searchResultItem.asVideo())
                .filter(Objects::nonNull)
                .filter(searchResultVideoDetails -> !searchResultVideoDetails.isLive())
                .collect(Collectors.toList());

    }

    private String cleanSearchQueryText(String searchQuery, Context context) {
        String cleanedSearchQuery = CommonUtil.extractVideoIdIfUrl(searchQuery);
        if (!Objects.equals(cleanedSearchQuery, searchQuery)) {
            // Handler to show Toast on the main thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Link detected, searching: " + cleanedSearchQuery, Toast.LENGTH_LONG).show();
                }
            });

        }
        return cleanedSearchQuery;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
