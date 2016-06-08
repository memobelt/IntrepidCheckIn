package com.intrepid.intrepidcheckin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static final int FIFTEEN_MINUTES = 15 * 60 * 1000;
    private Calendar cal;
    private Intent locationIntent;
    private PendingIntent locationPendingIntent;
    private AlarmManager alarmManager;
    private EditText etName;
    private Button btnStartTracking;
    private Button btnStopTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cal = Calendar.getInstance();
        locationIntent = new Intent(this, LocationService.class);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        etName = (EditText) findViewById(R.id.etName);
        btnStopTracking = (Button) findViewById(R.id.btnStopTracking);
        btnStartTracking = (Button) findViewById(R.id.btnStartTracking);

    }

    public void startTracking(View v) {
        String name = etName.getText().toString();
        if (!name.isEmpty()) {
            locationIntent.putExtra(Constants.NAME, name);
        }
        locationPendingIntent = PendingIntent.getService(this, 0, locationIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                FIFTEEN_MINUTES, locationPendingIntent);
        setStopButton();
    }

    public void stopTracking(View v) {
        alarmManager.cancel(locationPendingIntent);
        PendingIntent service = PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_NO_CREATE);
        if (service != null) {
            stopService(locationIntent);
            service.cancel();
        }
        setStartButton();
    }

    public void setStopButton() {
        btnStartTracking.setVisibility(View.GONE);
        btnStopTracking.setVisibility(View.VISIBLE);
    }

    public void setStartButton() {
        btnStopTracking.setVisibility(View.GONE);
        btnStartTracking.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_NO_CREATE) != null) {
            setStopButton();
        } else {
            setStartButton();
        }
    }
}
