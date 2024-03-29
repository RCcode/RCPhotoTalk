package com.rcplatform.phototalk.galhttprequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

/**
 * @Title: GalHtppRequest.java
 * @Package galhttprequest
 * @author 林秋明
 * @date 2012-3-22 上午10:06:35
 * @version V1.0
 */
public class GalHttpRequest {

    public static final String CACHE_ROOT = "galhttprequest_cache";

    public static final int BUFFER_SIZE = 4 * 1024;

    // 使用线程池，来重复利用线程，优化内存
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

    private static Handler mHandler = null;

    private final Context mContext;

    private GalHttpRequestListener callBack;

    private GalHttpLoadImageCallBack imageLoadCallBack;

    private GalHttpLoadTextCallBack textLoadCallBack;

    private PhotoChatHttpLoadTextCallBack photoChatHttpLoadTextCallBack;

    private GALURL galurl;

    private ArrayList<Header> headers;

    private boolean cacheEnable = false;

    private boolean writeTocache = false;

    /**
     * @Title: requestWithURL
     * @Description:带参数的url，支持组装参数
     * @param @param context
     * @param @param baseUrl
     * @param @param params
     * @param @return 传入参数名字
     * @return GalHttpRequest 返回类型
     * @date 2012-3-23 上午11:31:54
     * @throw
     */
    public static GalHttpRequest requestWithURL(Context context, String baseUrl, NameValuePair... params) {
        String url = baseUrl + concatParams(params);
        GalHttpRequest request = new GalHttpRequest(context, url);

        return request;
    }

    public static GalHttpRequest requestWithURL(Context context, String url) {
        GalHttpRequest request = new GalHttpRequest(context, url);
        return request;
    }

    public static GalHttpRequest requestWithURL(Context context, String url, Header... headers) {
        GalHttpRequest request = new GalHttpRequest(context, url);
        ArrayList<Header> headList = new ArrayList<Header>();
        for (Header header : headers) {
            headList.add(header);
        }
        request.setHeaders(headList);
        return request;
    }

    static void checkHandler() {
        try {
            if (mHandler == null) {
                mHandler = new Handler();
            }
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
            mHandler = null;
        }
    }

    private static String concatParams(NameValuePair[] params) {
        if (params == null || params.length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            NameValuePair param = params[i];
            if (i == 0) {
                sb.append("?");
                sb.append(URLEncoder.encode(param.getName()) + "=" + URLEncoder.encode(param.getValue()));
            } else {
                sb.append("&");
                sb.append(URLEncoder.encode(param.getName()) + "=" + URLEncoder.encode(param.getValue()));
            }
        }
        return sb.toString();
    }

    public GalHttpRequest(Context context, String url) {
        this.mContext = context;
        initGalURL(url);
    }

    public GalHttpRequest(Context context) {
        this.mContext = context;
    }

    public void setURL(String url) {
        initGalURL(url);
    }

    private void initGalURL(String url) {
        System.out.println("url=" + url);
            galurl = new GALURL();
            galurl.setUrl(url);
    }

    public void setPostValueForKey(String key, String value) {
        if (galurl != null) {
            galurl.getPostData().put(key, value);
        }
    }

    public Map<String, String> getPostData() {
        if (galurl != null) {
            return galurl.getPostData();
        }
        return null;
    }

    /**
     * @Title: startAsynchronous
     * @Description:异步请求
     * @param 传入参数名字
     * @return void 返回类型
     * @date 2012-3-23 上午10:37:38
     * @throw
     */
    public void startAsynchronous() {
        checkHandler();
        executor.execute(new Runnable() {

            @Override
            public void run() {
                HttpResponse response = requestHttp(true, true);
                if (callBack == null) {
                    return;
                }
                if (response == null) {
                    callBack.loadFailed(response, null);
                    return;
                }
                try {
                    int statusCode = response.getStatusLine().getStatusCode();
                    switch (statusCode) {
                        case 200: {
                            InputStream is = getISFromRespone(response);
                            callBack.loadFinished(is, false);
                            break;
                        }
                        case 304: {
                            InputStream is = getISFromCache();
                            if (is != null) {
                                callBack.loadFinished(is, true);
                            } else {
                                HttpResponse strickResponse = requestHttp(false, false);
                                is = getISFromRespone(strickResponse);
                                callBack.loadFinished(is, false);
                            }
                            break;
                        }
                        default: {
                            if (!cacheEnable) {
                                return;
                            }
                            try {
                                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(galurl.getLocalData()));
                                callBack.loadFailed(response, bis);
                            }
                            catch (Exception e) {
                                callBack.loadFailed(response, null);
                            }
                            break;
                        }
                    }
                }
                catch (Exception e) {
                    LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
                }
            }
        });
    }

    public void startAsynRequestString(GalHttpLoadTextCallBack callBack) {
        checkHandler();
        setTextLoadCallBack(callBack);
        executor.execute(new Runnable() {

            @Override
            public void run() {
                final String content = startSyncRequestString();
                if (textLoadCallBack != null && content != null) {
                    if (mHandler != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                Log.w("MENUE", "rsps=" + content);
                                textLoadCallBack.textLoaded(content);
                            }
                        });
                    } else {
                        textLoadCallBack.textLoaded(content);
                    }
                } else if (textLoadCallBack != null && content == null) {

                    if (mHandler != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                textLoadCallBack.loadFail();
                            }
                        });
                    } else {
                        textLoadCallBack.loadFail();
                    }
                }
            }
        });
    };

    public void startAsynRequestString(PhotoChatHttpLoadTextCallBack callBack, final long time) {
        checkHandler();
        setPhotoChatHttpLoadTextCallBack(callBack);
        executor.execute(new Runnable() {

            @Override
            public void run() {
                final String content = startSyncRequestString();
                final long tmeptime = time;
                if (photoChatHttpLoadTextCallBack != null && content != null) {
                    if (mHandler != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                Log.w("MENUE", "rsps=" + content);
                                photoChatHttpLoadTextCallBack.textLoaded(content, tmeptime);
                            }
                        });
                    } else {
                        photoChatHttpLoadTextCallBack.textLoaded(content, tmeptime);
                    }
                } else if (photoChatHttpLoadTextCallBack != null && content == null) {

                    if (mHandler != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                photoChatHttpLoadTextCallBack.loadFail(tmeptime);
                            }
                        });
                    } else {
                        photoChatHttpLoadTextCallBack.loadFail(tmeptime);
                    }
                }
            }
        });
    };

    public void startAsynUploadImage(PhotoChatHttpLoadTextCallBack callBack, final long time, byte[] data) {
        setPhotoChatHttpLoadTextCallBack(callBack);
        executor.execute(new MyRunable(time, data));
    }

    class MyRunable implements Runnable {

        private final long timeSnap;

        private final byte[] data;

        public MyRunable(long time, byte[] data) {
            this.timeSnap = time;
            this.data = data;
        }

        @Override
        public void run() {
            final String content = uploadImage(this.data);
            if (photoChatHttpLoadTextCallBack != null && content != null) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            photoChatHttpLoadTextCallBack.textLoaded(content, timeSnap);
                        }
                    });
                } else {
                    photoChatHttpLoadTextCallBack.textLoaded(content, timeSnap);
                }
            } else if (photoChatHttpLoadTextCallBack != null && content == null) {

                if (mHandler != null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            photoChatHttpLoadTextCallBack.loadFail(timeSnap);
                        }
                    });
                } else {
                    photoChatHttpLoadTextCallBack.loadFail(timeSnap);
                }
            }
        }

    }

    public void startAsynUploadImage(GalHttpLoadTextCallBack callBack) {
        setTextLoadCallBack(callBack);
        executor.execute(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                final String content = uploadImage();
                if (textLoadCallBack != null && content != null) {
                    if (mHandler != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                textLoadCallBack.textLoaded(content);
                            }
                        });
                    } else {
                        textLoadCallBack.textLoaded(content);
                    }
                } else if (textLoadCallBack != null && content == null) {

                    if (mHandler != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                textLoadCallBack.loadFail();
                            }
                        });
                    } else {
                        textLoadCallBack.loadFail();
                    }
                }
            }

        });
    }

    private String uploadImage(byte[] data) {
        // TODO Auto-generated method stub
        if (galurl == null || GalStringUtil.isEmpty(galurl.getUrl()) || "null".equals(galurl.getUrl())) {
            LogUtil.i("galurl 为空");
            return null;
        }
        HttpResponse response = null;
        try {
            if (galurl.getPostData().size() > 0) {
                HttpPost request = new HttpPost(galurl.getUrl());
                request.addHeader("charset", HTTP.UTF_8);
                HashMap<String, String> params = galurl.getPostData();
                MultipartEntity mulEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                Set<String> keyset = params.keySet();
                for (String key : keyset) {
                    String value = params.get(key);
                    if (key.equals("file") && !TextUtils.isEmpty(value)) {
                        File file = new File(value);
                        InputStreamBody body = null;
                        if (data != null)
                            body = new InputStreamBody(new ByteArrayInputStream(data), file.getName());
                        else {
                            if (value.contains("/")) {
                                body = new InputStreamBody(new FileInputStream(file), file.getName());
                            } else {
                                body = new InputStreamBody(mContext.openFileInput(value), file.getName());
                            }
                        }
                        mulEntity.addPart(key, body);
                        continue;
                    }
                    mulEntity.addPart(key, new StringBody(value, Charset.forName("UTF-8")));
                }

                if (!GalStringUtil.isEmpty(galurl.getLastModified())) {
                    request.addHeader("If-Modified-Since", galurl.getLastModified());
                }
                if (!GalStringUtil.isEmpty(galurl.getEtag())) {
                    request.addHeader("If-None-Match", galurl.getEtag());
                }
                if (headers != null) {
                    for (Header header : headers) {
                        request.addHeader(header);
                    }
                }
                request.setEntity(mulEntity);
                response = MyHttpClient.execute(mContext, request);
            } else {
                HttpGet request = new HttpGet(galurl.getUrl());
                if (!GalStringUtil.isEmpty(galurl.getLastModified())) {
                    request.addHeader("If-Modified-Since", galurl.getLastModified());
                }
                if (!GalStringUtil.isEmpty(galurl.getEtag())) {
                    request.addHeader("If-None-Match", galurl.getEtag());
                }
                response = MyHttpClient.execute(mContext, request);
            }
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        InputStream in = startAsyncChronout(response);
        if (in == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        try {
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            in.close();
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        return baos.toString();
    }

    private String uploadImage() {
        // TODO Auto-generated method stub
        if (galurl == null || GalStringUtil.isEmpty(galurl.getUrl()) || "null".equals(galurl.getUrl())) {
            LogUtil.i("galurl 为空");
            return null;
        }
        HttpResponse response = null;
        try {
            if (galurl.getPostData().size() > 0) {
                HttpPost request = new HttpPost(galurl.getUrl());
                request.addHeader("charset", HTTP.UTF_8);
                HashMap<String, String> params = galurl.getPostData();
                MultipartEntity mulEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                Set<String> keyset = params.keySet();
                for (String key : keyset) {
                    String value = params.get(key);
                    if (key.equals("file") && !TextUtils.isEmpty(value)) {
                        File file = new File(value);
                        InputStreamBody body = new InputStreamBody(new FileInputStream(file), file.getName());
                        mulEntity.addPart(key, body);
                        continue;
                    }
                    mulEntity.addPart(key, new StringBody(value, Charset.forName("UTF-8")));
                }

                if (!GalStringUtil.isEmpty(galurl.getLastModified())) {
                    request.addHeader("If-Modified-Since", galurl.getLastModified());
                }
                if (!GalStringUtil.isEmpty(galurl.getEtag())) {
                    request.addHeader("If-None-Match", galurl.getEtag());
                }
                if (headers != null) {
                    for (Header header : headers) {
                        request.addHeader(header);
                    }
                }
                request.setEntity(mulEntity);
                response = MyHttpClient.execute(mContext, request);
            } else {
                HttpGet request = new HttpGet(galurl.getUrl());
                if (!GalStringUtil.isEmpty(galurl.getLastModified())) {
                    request.addHeader("If-Modified-Since", galurl.getLastModified());
                }
                if (!GalStringUtil.isEmpty(galurl.getEtag())) {
                    request.addHeader("If-None-Match", galurl.getEtag());
                }
                response = MyHttpClient.execute(mContext, request);
            }
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        InputStream in = startAsyncChronout(response);
        if (in == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        try {
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            in.close();
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        return baos.toString();
    }

    private InputStream startAsyncChronout(HttpResponse response) {
        if (response == null) {
            return null;
        }
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            switch (statusCode) {
                case 200: {
                    InputStream is = getISFromRespone(response);
                    return is;
                }
                case 304: {
                    InputStream is = getISFromCache();
                    if (is != null) {
                        return is;
                    } else {
                        response = requestHttp(false, false);
                        is = getISFromRespone(response);
                        return is;
                    }
                }
                default:
                    if (!cacheEnable) {
                        return null;
                    }
                    try {
                        InputStream is = getISFromCache();
                        return is;
                    }
                    catch (Exception e) {
                        return null;
                    }
            }
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        return null;
    }

    public void startAsynRequestBitmap(GalHttpLoadImageCallBack callBack) {
        checkHandler();
        setImageLoadCallBack(callBack);
        executor.execute(new Runnable() {

            @Override
            public void run() {
                final Bitmap bitmap = startSyncRequestBitmap();
                if (imageLoadCallBack != null && bitmap != null) {
                    if (mHandler != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                imageLoadCallBack.imageLoaded(bitmap);
                            }
                        });
                    } else {
                        imageLoadCallBack.imageLoaded(bitmap);
                    }
                }
            }
        });
    };

    /**
     * @Title: startSynchronous
     * @Description:同步请求，返回的是有缓冲的InputStream
     * @param @return 传入参数名字
     * @return InputStream 返回类型
     * @date 2012-3-23 上午10:29:10
     */
    public InputStream startSynchronous() {
        HttpResponse response = requestHttp(true, true);
        if (response == null) {
            return null;
        }
        try {
            int statusCode = response.getStatusLine().getStatusCode();

            Log.d("MENUE", "statusCode=" + statusCode);
            switch (statusCode) {
                case 200: {
                    InputStream is = getISFromRespone(response);
                    return is;
                }
                case 304: {
                    InputStream is = getISFromCache();
                    if (is != null) {
                        return is;
                    } else {
                        response = requestHttp(false, false);
                        is = getISFromRespone(response);
                        return is;
                    }
                }
                default:
                    if (!cacheEnable) {
                        return null;
                    }
                    try {
                        InputStream is = getISFromCache();
                        return is;
                    }
                    catch (Exception e) {
                        return null;
                    }
            }
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        return null;
    }



    /**
     * @Title: startGetStringSynchronous
     * @Description:同步请求String
     * @param @return 传入参数名字
     * @return String 返回类型
     * @date 2012-3-23 下午4:26:31
     * @throw
     */
    public String startSyncRequestString() {
        InputStream is = startSynchronous();
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        try {
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        return baos.toString();
    }

    /**
     * @Title: startGetBitmapSynchronous
     * @Description:同步请求图片
     * @param @return 传入参数名字
     * @return Bitmap 返回类型
     * @date 2012-3-23 下午4:25:43
     * @throw
     */
    public Bitmap startSyncRequestBitmap() {
        Bitmap cache = getBitmapFromCache();
        if (cache != null) {
            return cache;
        }
        InputStream is = startSynchronous();
        if (is == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }

    public GALURL getGalurl() {
        return galurl;
    }

    private HttpResponse requestHttp(boolean haveLastModified, boolean haveEtag) {
        if (galurl == null || GalStringUtil.isEmpty(galurl.getUrl()) || "null".equals(galurl.getUrl())) {
            LogUtil.i("galurl 为空");
            return null;
        }
        HttpResponse response = null;
        try {
            if (galurl.getPostData().size() > 0) {
                HttpPost request = new HttpPost(galurl.getUrl());
                // List<NameValuePair> nameValuePairs = new
                // ArrayList<NameValuePair>();
                HashMap<String, String> params = galurl.getPostData();
                Set<String> keyset = params.keySet();
                JSONObject jObj = new JSONObject();
                for (String key : keyset) {
                    String value = params.get(key);
                    // nameValuePairs.add(new BasicNameValuePair(key, value));
                    jObj.put(key, value);
                }
                String entity = jObj.toString();
                if (!GalStringUtil.isEmpty(galurl.getLastModified()) && haveLastModified) {
                    request.addHeader("If-Modified-Since", galurl.getLastModified());
                }
                if (!GalStringUtil.isEmpty(galurl.getEtag()) && haveEtag) {
                    request.addHeader("If-None-Match", galurl.getEtag());
                }
                if (headers != null) {
                    for (Header header : headers) {
                        request.addHeader(header);
                    }
                }

                request.setEntity(new StringEntity(entity, "UTF-8"));
                Log.v("MENUE", "rstp action=" + request.getURI().getPath());
                Log.v("MENUE", "rstp " + entity.toString());

                response = MyHttpClient.execute(mContext, request);
            } else {
                HttpGet request = new HttpGet(galurl.getUrl());
                if (!GalStringUtil.isEmpty(galurl.getLastModified())) {
                    request.addHeader("If-Modified-Since", galurl.getLastModified());
                }
                if (!GalStringUtil.isEmpty(galurl.getEtag())) {
                    request.addHeader("If-None-Match", galurl.getEtag());
                }
                response = MyHttpClient.execute(mContext, request);
            }
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
        }
        return response;
    }

    private InputStream getISFromRespone(HttpResponse response) {
        try {
            if (writeTocache) {
                String filepath = writeInputSteamToCache(response.getEntity().getContent());
                if (!GalStringUtil.isEmpty(filepath)) {
                    String lastModified = getHeader(response, "Last-Modified");
                    String etag = getHeader(response, "ETag");
                    galurl.setLastModified(lastModified);
                    galurl.setEtag(etag);
                    galurl.setLocalData(filepath);
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(galurl.getLocalData()));
                    return bis;
                }
            } else {
                return new BufferedInputStream(response.getEntity().getContent());
            }
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
            return null;
        }
        return null;
    }

    private Bitmap getBitmapFromCache() {
        if (galurl == null || GalStringUtil.isEmpty(galurl.getLocalData())) {
            return null;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(galurl.getLocalData());
            return bitmap;
        }
        catch (Exception e) {
            return null;
        }
    }

    private InputStream getISFromCache() {
        if (galurl == null || GalStringUtil.isEmpty(galurl.getLocalData())) {
            return null;
        }
        File cache = new File(galurl.getLocalData());
        try {
            return new BufferedInputStream(new FileInputStream(cache));
        }
        catch (Exception e) {
            return null;
        }
    }

    private String writeInputSteamToCache(InputStream is) {
        try {
            File cachedir = mContext.getDir(CACHE_ROOT, 0);
            BufferedInputStream bis = new BufferedInputStream(is);
            final String fileName = MD5.encodeMD5String(galurl.getUrl());
            File file = new File(cachedir, fileName);
            if (file.exists()) {
                file.delete();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int len = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            bis.close();
            return file.getAbsolutePath();
        }
        catch (Exception e) {
            LogUtil.e(new Throwable().getStackTrace()[0].toString() + " Exception ", e);
            return null;
        }
    }

    private String getHeader(HttpResponse responese, String headerName) {
        if (GalStringUtil.isEmpty(headerName) || responese == null) {
            return null;
        }
        Header[] headers = responese.getHeaders(headerName);
        if (headers != null && headers.length > 0) {
            return headers[0].getValue();
        }
        return null;
    }

    public void setGalurl(GALURL galurl) {
        this.galurl = galurl;
    }

    public GalHttpRequestListener getListener() {
        return callBack;
    }

    public void setListener(GalHttpRequestListener callBack) {
        this.callBack = callBack;
    }

    public GalHttpLoadImageCallBack getImageLoadCallBack() {
        return imageLoadCallBack;
    }

    public void setImageLoadCallBack(GalHttpLoadImageCallBack imageLoadCallBack) {
        this.imageLoadCallBack = imageLoadCallBack;
    }

    public GalHttpLoadTextCallBack getTextLoadCallBack() {
        return textLoadCallBack;
    }

    public void setTextLoadCallBack(GalHttpLoadTextCallBack textLoadCallBack) {
        this.textLoadCallBack = textLoadCallBack;
    }

    public ArrayList<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(ArrayList<Header> headers) {
        this.headers = headers;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public boolean isWriteTocache() {
        return writeTocache;
    }

    public void setWriteTocache(boolean writeTocache) {
        this.writeTocache = writeTocache;
    }

    public interface GalHttpRequestListener {

        public void loadFinished(InputStream is, boolean fromcache);

        public void loadFailed(HttpResponse respone, InputStream cacheInputStream);
    }

    public interface GalHttpLoadImageCallBack {

        public void imageLoaded(Bitmap bitmap);
    }

    public interface GalHttpLoadTextCallBack {

        public void textLoaded(String text);

        public void loadFail();

    }

    public PhotoChatHttpLoadTextCallBack getPhotoChatHttpLoadTextCallBack() {
        return photoChatHttpLoadTextCallBack;
    }

    public void setPhotoChatHttpLoadTextCallBack(PhotoChatHttpLoadTextCallBack photoChatHttpLoadTextCallBack) {
        this.photoChatHttpLoadTextCallBack = photoChatHttpLoadTextCallBack;
    }

    public interface PhotoChatHttpLoadTextCallBack {

        public void textLoaded(String text, long time);

        public void loadFail(long time);

    }
}
