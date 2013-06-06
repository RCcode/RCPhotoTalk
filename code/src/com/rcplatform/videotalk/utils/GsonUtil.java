package com.rcplatform.videotalk.utils;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;

public class GsonUtil {

    public static String listToJson(List list, Type typeOfSrc) {
        Gson gson = new Gson();
        return gson.toJson(list, typeOfSrc);
    }
}
