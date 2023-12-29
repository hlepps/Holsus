package com.pmsdp.holsus;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  implements DodajDziure.NoticeDialogListener {
    MapView map;
    public IMapController mapController;
    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    GPSTracker gpsTracker;
    OverlayItem navigator;
    MyLocationNewOverlay locationOverlay;

    Button button;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        map = (MapView)findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mapController = map.getController();
        mapController.setZoom(19);
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

        gpsTracker.mapController = mapController;
        Location l = gpsTracker.getLocation();
        if(l != null) {
            mapController.setCenter(new GeoPoint(l.getLatitude(), l.getLongitude()));
        }


        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DodajDziure dodajDziure = new DodajDziure();
                dodajDziure.map = map;
                dodajDziure.gpsTracker = gpsTracker;
                Bundle args = new Bundle();
                Location temp = gpsTracker.getLocation();
                args.putFloat("latitude", (float) temp.getLatitude());
                args.putFloat("longitude", (float) temp.getLongitude());
                dodajDziure.setArguments(args);
                dodajDziure.show(getSupportFragmentManager(),null);
            }
        });

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        /*locationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapController.animateTo(locationOverlay
                        .getMyLocation());
            }
        });*/
        map.getOverlays().add(locationOverlay);

    }


    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming. 
        //if you make changes to the configuration, use 
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        //Configuration.getInstance().save(this, prefs); 
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Log.d("ok","test");
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


}