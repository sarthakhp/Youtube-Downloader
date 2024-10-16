package com.sarthak.youtubedownloader.dto;

import com.github.kiulian.downloader.model.search.SearchResultItem;
import com.github.kiulian.downloader.model.search.SearchResultItemType;

public class SearchResultDTO implements SearchResultItem {
    @Override
    public SearchResultItemType type() {
        return null;
    }

    @Override
    public String title() {
        return null;
    }
}
