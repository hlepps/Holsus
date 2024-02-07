package com.pmsdp.holsus;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.Manifest;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

public class MainActivity extends AppCompatActivity implements DodajDziure.NoticeDialogListener {
    MapView map;
    public IMapController mapController;
    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    GPSTracker gpsTracker;
    OverlayItem navigator;
    MyLocationNewOverlay locationOverlay;
    Button button;

    String bazaname = "dziury.db";
    public static SQLiteDatabase twierdza = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int ALL_PERMISSIONS = 101;

        final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
                Manifest.permission.ACCESS_WIFI_STATE};

        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);


        Sensey.getInstance().init(this, Sensey.SAMPLING_PERIOD_GAME);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        map = (MapView) findViewById(R.id.map);
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
        if (l != null) {
            mapController.setCenter(new GeoPoint(l.getLatitude(), l.getLongitude()));
        }

        //SQL
        try {
            twierdza = openOrCreateDatabase(bazaname, MODE_PRIVATE, null);
            twierdza.execSQL("CREATE TABLE IF NOT EXISTS Dziury (szerokosc FLOAT, wysokosc FLOAT)");
            Cursor kursor = twierdza.rawQuery("SELECT * FROM Dziury", null);


            if (kursor != null && kursor.moveToFirst()) {
                int indeksx = kursor.getColumnIndex("szerokosc");
                int indeksy = kursor.getColumnIndex("wysokosc");
                if (indeksx >= 0 && indeksy >= 0) {
                    do {

                        float szeroki = kursor.getFloat(indeksx);
                        float wysoki = kursor.getFloat(indeksy);
                        Log.d("DZIURA", "sz" + szeroki + " wy" + wysoki);
                        HoleManager.addHoleOnMap(map, new GeoPoint(szeroki, wysoki));
                    }
                    while (kursor.moveToNext());
                }
                kursor.close();
            } else {
            }
        } catch (SQLiteException e) {
            Log.e(getClass().getSimpleName(), "Nie mozna stworzyc albo otworzyc bazy");
        }

        button = (Button) findViewById(R.id.button);
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
                dodajDziure.show(getSupportFragmentManager(), null);
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

        ShakeDetector.ShakeListener shakeListener = new ShakeDetector.ShakeListener() {
            @Override
            public void onShakeDetected() {
                Log.d("SHAKE", "szejk");

            }

            @Override
            public void onShakeStopped() {

                Log.d("SHAKE", "koniec");

                DodajDziure dodajDziure = new DodajDziure();
                dodajDziure.map = map;
                dodajDziure.gpsTracker = gpsTracker;
                Bundle args = new Bundle();
                Location temp = gpsTracker.getLocation();
                args.putFloat("latitude", (float) temp.getLatitude());
                args.putFloat("longitude", (float) temp.getLongitude());
                dodajDziure.setArguments(args);
                dodajDziure.show(getSupportFragmentManager(), null);
                showNotification("test");
            }
        };

        Sensey.getInstance().startShakeDetection(5, 50, shakeListener);


        double min = 0.3;
        // Init
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < HoleManager.holes.size(); i++) {
                    double a = HoleManager.holes.get(i).getLatitude() - gpsTracker.getLocation().getLatitude();
                    double b = HoleManager.holes.get(i).getLongitude() - gpsTracker.getLocation().getLongitude();

                    double magnitute = Math.sqrt(a * a + b * b) * 1000;

                    //Log.d("UWAGA", "[" + i + "] " + magnitute);
                    if (magnitute < min) {
                        if (HoleManager.holesVisited.get(i) == false) {
                            HoleManager.holesVisited.set(i, true);
                            Log.d("UWAGA", "[" + i + "] UWAGA DZIURA");
                            showNotification("Uwaga! Dziura już za " + String.format("%.1f",(magnitute * 10)) + "m!");

                        }
                    } else if (magnitute > min + 0.01) {
                        HoleManager.holesVisited.set(i, false);
                    }
                }
                handler.postDelayed(this, 500);
            }
        };

        //Start
        handler.postDelayed(runnable, 500);


    }


    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming. 
        //if you make changes to the configuration, use 
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        //Configuration.getInstance().save(this, prefs); 
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    int x = 0;


    private void showNotification(String eventtext) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("CHANNEL_ID", "Alarm Time....", NotificationManager.IMPORTANCE_DEFAULT);
        }
        NotificationManager notificationManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = getSystemService(NotificationManager.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "DZIURA");
        builder.setContentTitle("UWAGA DZIURA");
        builder.setContentText(eventtext);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        managerCompat.notify(null, 0, builder.build());
        Log.d("UWAGA", eventtext);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("UWAGA DZIURA")
                .setMessage(eventtext)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Log.d("ok","test");
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


}