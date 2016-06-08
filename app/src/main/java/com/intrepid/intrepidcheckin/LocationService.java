package com.intrepid.intrepidcheckin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;
    private Location workLocation;
    public static final int POST = 0;
    public static final int CANCEL = 1;
    public static final int STOP = 2;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
            workLocation = new Location("Work");
            workLocation.setLatitude(42.367053); //Intrepid Lat
            workLocation.setLongitude(-71.080161); //Intrepid Long
        }
        checkLocation(intent.getStringExtra(Constants.NAME));
        return super.onStartCommand(intent, flags, startId);
    }

    public void checkLocation(String name) {
        //Permission check
        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (location != null) {
                if (location.distanceTo(workLocation) <= 1000) {

                    Intent slackIntent = new Intent(getApplicationContext(), SlackMessage.class);
                    slackIntent.putExtra(Constants.NAME, name);

                    android.support.v4.app.NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText(getString(R.string.near_intrepid))
                                    .setAutoCancel(true);

                    slackIntent.putExtra(Constants.FLAG, POST);
                    PendingIntent slackPendingIntent =
                            PendingIntent.getService(getApplicationContext(), POST, slackIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.addAction(R.drawable.post, getString(R.string.post), slackPendingIntent);

                    slackIntent.putExtra(Constants.FLAG, CANCEL);
                    slackPendingIntent =
                            PendingIntent.getService(getApplicationContext(), CANCEL, slackIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.addAction(R.drawable.cancel, getString(R.string.cancel), slackPendingIntent);

                    slackIntent.putExtra(Constants.FLAG, STOP);
                    slackPendingIntent =
                            PendingIntent.getService(getApplicationContext(), STOP, slackIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.addAction(R.drawable.stop, getString(R.string.stop), slackPendingIntent);

                    int notificationId = 001;
                    NotificationManager notifyMgr =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notifyMgr.notify(notificationId, builder.build());

                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

}
