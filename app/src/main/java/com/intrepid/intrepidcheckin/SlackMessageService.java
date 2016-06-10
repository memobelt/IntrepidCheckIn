package com.intrepid.intrepidcheckin;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SlackMessageService extends IntentService {
    public static final String URL = "https://hooks.slack.com/services/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";

    public SlackMessageService() {
        super("SlackMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int i = intent.getIntExtra(Constants.FLAG, -1);

        //Dismiss notification on click
        NotificationManager notifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.cancelAll();

        if (i == LocationService.POST) {
            postCheckIn(intent);
        } else if (i == LocationService.STOP) {
            stopTracking();
            EventBus.getDefault().post("");
        }
    }


    public void postCheckIn(Intent intent) {
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
        assert url != null;
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

    public void stopTracking() {
        TrackingActivity.stopBackgroundTracking(getApplicationContext());
    }
}
