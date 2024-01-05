package de.spaceai.twitchbot.command.script.request;

import java.util.Map;

import de.spaceai.twitchbot.util.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestHandler {

    public String get(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            Logger.from("HTTP").log(e.getMessage());
        }
        return null;
    }

    public String post(String url, String data, String mediaType, Map<String, String> headers) {
        if(mediaType == null)
            mediaType = "text/html; charset=utf-8";
        if(data == null)
            data = "";
        RequestBody body = RequestBody.create(MediaType.get(mediaType), data);
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);

        if(headers != null) {
            headers.forEach((key, value) -> {
                builder.addHeader(key, value);
            });
        }

        Request request = builder.build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            Logger.from("HTTP").log(e.getMessage());
        }
        return null;
    }

}
