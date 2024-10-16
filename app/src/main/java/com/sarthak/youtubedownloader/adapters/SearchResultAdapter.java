package com.sarthak.youtubedownloader.adapters;

import static java.util.Objects.nonNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.kiulian.downloader.model.search.SearchResultVideoDetails;
import com.sarthak.youtubedownloader.R;
import com.sarthak.youtubedownloader.util.CommonUtil;

import java.util.List;

public class SearchResultAdapter extends ArrayAdapter<SearchResultVideoDetails> {

    private DataChangeListener listener;

    public SearchResultAdapter(Context context, List<SearchResultVideoDetails> items) {
        super(context, 0, items);
    }

    public void setDataChangeListener(DataChangeListener listener) {
        this.listener = listener;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data searchResultVideoDetails for this position
        SearchResultVideoDetails searchResultVideoDetails = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result_list_item, parent, false);
        }

        // Lookup view for data population
        TextView titleTextView = (TextView) convertView.findViewById(R.id.search_result_title_text_id);
        ImageView searchResultThumbnailView = (ImageView) convertView.findViewById(R.id.search_result_image_id);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.search_result_time_text_id);

        // Populate the data into the template view using the data object
        titleTextView.setText(searchResultVideoDetails.title());
        Glide.with(getContext()).load(searchResultVideoDetails.thumbnails().get(0)).into(searchResultThumbnailView);
        timeTextView.setText(getTimeText(searchResultVideoDetails));


        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (nonNull(listener)) {
            listener.onDataChanged(this.getCount());
        }
    }

    private static String getTimeText(SearchResultVideoDetails searchResultVideoDetails) {
        return CommonUtil.convertSecondsToReadableTime(searchResultVideoDetails.lengthSeconds());
    }

    public interface DataChangeListener {
        void onDataChanged(int listItemCount);
    }

}
