package com.sarthak.youtubedownloader.util;

import com.google.gson.Gson;

public class JsonUtil {
    public static String convertObjToJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static <T> T convertJsonStringToObj(String jsonString, Class<T> tClass) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, tClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle error appropriately
        }
    }
}
