package com.intrepid.intrepidcheckin;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SlackMessage extends IntentService {
    public static final String URL = "https://hooks.slack.com/services/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";

    public SlackMessage() {
        super("SlackMessage");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            JSONObject message = new JSONObject();
            try {
                String name = intent.getStringExtra(Constants.NAME);
                if (name != null) {
                    message.put("username", name);
                }
                message.put("text", "I'm nearby!");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            URL url = null;
            try {
                url = new URL(URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            MediaType json = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json, String.valueOf(message));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
