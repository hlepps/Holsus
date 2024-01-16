package com.pmsdp.holsus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;

import java.util.List;

public class GPSTracker implements LocationListener {
    Context context;
    public IMapController mapController;
    public GPSTracker(Context ctx)
    {
        context = ctx;
    }

    public Location getLocation()
    {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Nie udzielono dostępu do GPS", Toast.LENGTH_SHORT);
        }
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled)
        {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, this);
            //Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            List<String> providers = lm.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location l = lm.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
            Log.d("GPS", "isGPSEnabled:" + isGPSEnabled);
            return bestLocation;

        }
        else {
            Toast.makeText(context, "Włącz GPS", Toast.LENGTH_SHORT);
            Log.e("GPS", "nie dziala lokalizacja jak cos");
        }
        Log.d("GPS", "isGPSEnabled:" + isGPSEnabled);
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
        Log.d("loc", location.toString());

    }
}
