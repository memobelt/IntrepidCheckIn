package com.intrepid.intrepidcheckin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TrackingActivity extends AppCompatActivity {

    public static final int FIFTEEN_MINUTES = 15 * 60 * 1000;
    private Calendar cal;
    private AlarmManager alarmManager;
    private Intent locationIntent;

    @BindView(R.id.et_name)
    EditText etName;

    @BindView(R.id.btn_start_tracking)
    Button btnStartTracking;

    @BindView(R.id.btn_stop_tracking)
    Button btnStopTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        ButterKnife.bind(this);

        cal = Calendar.getInstance();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        locationIntent = new Intent(this, LocationService.class);
    }

    public static void stopBackgroundTracking(Context context){
        Intent locationIntent = new Intent(context, LocationService.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent service = PendingIntent.getService(context, 0, locationIntent, PendingIntent.FLAG_NO_CREATE);
        alarmManager.cancel(service);
        if (service != null) {
            context.stopService(locationIntent);
            service.cancel();
        }
    }

    @OnClick(R.id.btn_start_tracking)
    public void startTracking() {
        String name = etName.getText().toString();
        if (!name.isEmpty()) {
            locationIntent.putExtra(Constants.NAME, name);
        }
        PendingIntent locationPendingIntent = PendingIntent.getService(this, 0, locationIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                FIFTEEN_MINUTES, locationPendingIntent);
        setStopButton();
    }

    @OnClick(R.id.btn_stop_tracking)
    public void stopTracking(View v) {
        stopBackgroundTracking(this);
        setStartButton();
    }

    public void setStopButton() {
        btnStartTracking.setVisibility(View.GONE);
        btnStopTracking.setVisibility(View.VISIBLE);
    }


    public void setStartButton() {
        btnStartTracking.setVisibility(View.VISIBLE);
        btnStopTracking.setVisibility(View.GONE);
    }

    @Subscribe
    public void onEvent(String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setStartButton();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if the app is currently tracking display stop button
        if (PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_NO_CREATE) != null) {
            setStopButton();
        } else {
            setStartButton();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
