package com.pmsdp.holsus;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import kotlin.collections.ArrayDeque;

public class HoleManager {

    public static List<GeoPoint> holes = new ArrayList<GeoPoint>();
    public static List<Boolean> holesVisited = new ArrayList<Boolean>();
    public static void addHoleOnMap(MapView map, GeoPoint point)
    {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        holes.add(point);
        holesVisited.add(true);
    }
}
