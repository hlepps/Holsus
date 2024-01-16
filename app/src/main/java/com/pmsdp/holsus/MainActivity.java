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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;

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

    String bazaname="dziury.db";
    public static SQLiteDatabase twierdza=null;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Sensey.getInstance().init(this,Sensey.SAMPLING_PERIOD_FASTEST);

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

        //SQL
        try
        {
            twierdza = openOrCreateDatabase(bazaname,MODE_PRIVATE,null);
            twierdza.execSQL("CREATE TABLE IF NOT EXISTS Dziury (szerokosc FLOAT, wysokosc FLOAT)");
            Cursor kursor = twierdza.rawQuery("SELECT * FROM Dziury",null);


                    if (kursor != null && kursor.moveToFirst()) {
                        int indeksx = kursor.getColumnIndex("szerokosc");
                        int indeksy = kursor.getColumnIndex("wysokosc");
                        if (indeksx >= 0 && indeksy >= 0) {
                            do {

                                float szeroki = kursor.getFloat(indeksx);
                                float wysoki = kursor.getFloat(indeksy);
                                HoleManager.addHoleOnMap(map,new GeoPoint(szeroki,wysoki));
                            }
                            while (kursor.moveToNext());
                        }
                        kursor.close();
                    } else {}
        }
        catch(SQLiteException e)
        {Log.e(getClass().getSimpleName(), "Nie mozna stworzyc albo otworzyc bazy");}

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

        ShakeDetector.ShakeListener shakeListener=new ShakeDetector.ShakeListener() {
            @Override public void onShakeDetected() {
                Log.d("SHAKE", "szejk");

            }

            @Override public void onShakeStopped() {

                Log.d("SHAKE", "koniec");

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
        };

        Sensey.getInstance().startShakeDetection(1,1,shakeListener);

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