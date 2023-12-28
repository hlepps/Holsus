package com.pmsdp.holsus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.osmdroid.util.GeoPoint;

public class GPSTracker implements LocationListener {
    Context context;
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
            Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return l;
        }
        else {
            Toast.makeText(context, "Włącz GPS", Toast.LENGTH_SHORT);
        }
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {

    }
}
