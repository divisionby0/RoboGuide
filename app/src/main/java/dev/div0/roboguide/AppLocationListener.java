package dev.div0.roboguide;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

public class AppLocationListener implements LocationListener {

    private ILocationChanged changedCallbackProvider;

    public AppLocationListener(ILocationChanged _changedCallbackProvider){
        changedCallbackProvider = _changedCallbackProvider;
    }

    @Override
    public void onLocationChanged(Location location) {
        changedCallbackProvider.onLocationChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("RoboGuide", "onProviderDisabled");
        //checkEnabled();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("RoboGuide", "onProviderEnabled");
        //checkEnabled();
        //showLocation(locationManager.getLastKnownLocation(provider));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("RoboGuide", "onStatusChanged. provider="+provider);
        /*
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            tvStatusGPS.setText("Status: " + String.valueOf(status));
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            tvStatusNet.setText("Status: " + String.valueOf(status));
        }
        */
    }
}
