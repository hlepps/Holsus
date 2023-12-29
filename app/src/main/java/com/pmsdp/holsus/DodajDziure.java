package com.pmsdp.holsus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class DodajDziure extends androidx.fragment.app.DialogFragment {
    // The activity that creates an instance of this dialog fragment must
    // implement this interface to receive event callbacks. Each method passes
    // the DialogFragment in case the host needs to query it.
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    public EditText latitudeText,longitudeText;

    public MapView map;
    public GPSTracker gpsTracker;

    // Use this instance of the interface to deliver action events.
    NoticeDialogListener listener;
    private View dialogView;

    // Override the Fragment.onAttach() method to instantiate the
    // NoticeDialogListener.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface.
        try {
            // Instantiate the NoticeDialogListener so you can send events to
            // the host.
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface. Throw exception.
            throw new ClassCastException("activity must implement NoticeDialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog.Builder alertDialog  = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dodaj_dziure, null);

        alertDialog.setView(dialogView);
        alertDialog.setPositiveButton("dodaj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialog2 =Dialog.class.cast(dialog);
                        latitudeText = dialog2.findViewById((R.id.latitude));
                        longitudeText = dialog2.findViewById((R.id.longitude));
                        float latitude = Float.parseFloat(latitudeText.getText().toString());
                        float longitude = Float.parseFloat(longitudeText.getText().toString());
                        Log.d("dodawane", "lat:" + latitude + " lon:" + longitude);
                        //FILIP
                        //tutaj wyślij do bazy danych

                        HoleManager.addHoleOnMap(map, new GeoPoint(latitude,longitude));
                    }
                })
                .setNegativeButton("anuluj", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        latitudeText = dialogView.findViewById((R.id.latitude));
        longitudeText = dialogView.findViewById((R.id.longitude));
        latitudeText.setText("" + getArguments().getFloat("latitude"));
        longitudeText.setText("" + getArguments().getFloat("longitude"));
        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        /*builder.setView(inflater.inflate(R.layout.dodaj_dziure, null))
                // Add action buttons
                .setPositiveButton("dodaj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialog2 =Dialog.class.cast(dialog);
                        latitudeText = dialog2.findViewById((R.id.latitude));
                        longitudeText = dialog2.findViewById((R.id.longitude));
                        float latitude = Float.parseFloat(latitudeText.getText().toString());
                        float longitude = Float.parseFloat(longitudeText.getText().toString());
                        Log.d("dodawane", "lat:" + latitude + " lon:" + longitude);
                        //FILIP
                        //tutaj wyślij do bazy danych

                        HoleManager.addHoleOnMap(map, new GeoPoint(latitude,longitude));
                    }
                })
                .setNegativeButton("anuluj", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });*/
        /*Location l = gpsTracker.getLocation();
        if(l != null) {
            latitudeText.setText("" + l.getLatitude());
            longitudeText.setText("" + l.getLongitude());

        }*/
        return alertDialog.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //latitudeText = view.findViewById((R.id.latitude));
        //longitudeText = view.findViewById((R.id.longitude));

    }
}
