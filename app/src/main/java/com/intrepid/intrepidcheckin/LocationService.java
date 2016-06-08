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
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location workLocation;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            workLocation = new Location("Work");
            workLocation.setLatitude(42.367053); //Intrepid Lat
            workLocation.setLongitude(-71.080161); //Intrepid Long
        }
        checkLocation(intent.getStringExtra(Constants.NAME));
        return super.onStartCommand(intent, flags, startId);
    }

    public void checkLocation(String name){
        //Permission check
        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if(location != null){
                if(location.distanceTo(workLocation)<=50){

                    Intent slackIntent = new Intent(getApplicationContext(), SlackMessage.class);
                    slackIntent.putExtra(Constants.NAME, name);
                    PendingIntent slackPendingIntent =
                            PendingIntent.getService(getApplicationContext(), 1, slackIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText(getString(R.string.near_intrepid))
                                    .setAutoCancel(true);
                    mBuilder.setContentIntent(slackPendingIntent);

                    int mNotificationId = 001;
                    NotificationManager mNotifyMgr =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

}
