package com.sarthak.youtubedownloader.service;

import static java.util.Objects.isNull;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {

    public static boolean isNotConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return isNull(netInfo) || !Boolean.TRUE.equals(netInfo.isConnectedOrConnecting());
    }

}
