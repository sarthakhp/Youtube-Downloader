package com.sarthak.youtubedownloader.adapters;

import static com.sarthak.youtubedownloader.util.CommonUtil.convertBytesToMB;
import static java.util.Objects.isNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import com.sarthak.youtubedownloader.R;
import com.sarthak.youtubedownloader.dto.DownloadVideoTaskParams;
import com.sarthak.youtubedownloader.util.CommonUtil;

import java.util.List;

public class DownloadFormatOptionsAdapter extends ArrayAdapter<DownloadVideoTaskParams> {
    public DownloadFormatOptionsAdapter(Context context, List<DownloadVideoTaskParams> entries) {
        super(context, 0, entries);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        DownloadVideoTaskParams downloadVideoTaskParams = getItem(position);

        Format format = CommonUtil.resolve(() -> downloadVideoTaskParams.getFormat(), null);
        String label = getLabel(format);
        String size = convertBytesToMB(CommonUtil.resolve(() -> format.contentLength(), null));

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.download_options_list_item, parent, false);
        }

        // Lookup view for data population
        TextView optionText = (TextView) convertView.findViewById(R.id.format_label_text_id);
        TextView fileSizeText = (TextView) convertView.findViewById(R.id.file_size_text_view);

        // Populate the data into the template view using the data object
        optionText.setText(label);
        fileSizeText.setText(size);

        // Return the completed view to render on screen
        return convertView;
    }

    private static String getLabel(Format format) {
        if (isNull(format)) {
            return null;
        }
        String label = CommonUtil.getFormatLabel(format);
        String formatType = format.type();
        if (Format.VIDEO.equals(formatType)) {
            label += " " + ((VideoFormat) format).qualityLabel();
        }
        else if (Format.AUDIO.equals(formatType)) {
            label += " " + ((AudioFormat) format).audioQuality().toString();
        }
        else if (Format.AUDIO_VIDEO.equals(formatType)) {
            label += " " + ((VideoWithAudioFormat) format).qualityLabel();
        }
        return label;
    }


}
