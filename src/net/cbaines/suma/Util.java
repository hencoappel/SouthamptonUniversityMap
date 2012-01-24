/*
 * Southampton University Map App
 * Copyright (C) 2011  Christopher Baines
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.cbaines.suma;

import java.util.ArrayList;


import org.osmdroid.util.GeoPoint;

import android.location.Location;
import android.util.Log;

public class Util {

    private static final String TAG = "Util";

    public static GeoPoint csLatLongToGeoPoint(String lat, String lng) {
	try {
	    double dLat = Double.valueOf(lat).doubleValue();
	    double dLng = Double.valueOf(lng).doubleValue();
	    int iLat = (int) (dLat * 1e6);
	    int iLng = (int) (dLng * 1e6);
	    return new GeoPoint(iLat, iLng);
	} catch (NumberFormatException e) {
	    Log.e(TAG, "Error formating " + lat + " " + lng, e);
	    throw e;
	}
    }

    public static GeoPoint ssLatLongToGeoPoint(String lat, String lng) {
	try {

	    double dLat = Double.valueOf(lat).doubleValue();
	    double dLng = Double.valueOf(lng).doubleValue();
	    int iLat = (int) (dLat * 1e6);
	    int iLng = (int) (dLng * 1e6);
	    return new GeoPoint(iLat, iLng);
	} catch (NumberFormatException e) {
	    Log.e(TAG, "Error formating " + lat + " " + lng, e);
	    throw e;
	}
    }

    public static Polygon csPolygonToPolygon(String str) {
	// Log.i(TAG, "Getting poly from " + str);
	ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

	String[] latLongPoints = str.split(",");
	for (int point = 0; point < latLongPoints.length; point++) {
	    // Log.i(TAG, "LatLong point " + point + " " + latLongPoints[point]);

	    String[] latLongs = latLongPoints[point].split(" ");
	    GeoPoint geoPoint = ssLatLongToGeoPoint(latLongs[1], latLongs[0]);
	    geoPoints.add(geoPoint);
	}

	return new Polygon(geoPoints.toArray(new GeoPoint[0]));
    }

    public static int doubleToIntE6(double dub) {
	return (int) (dub * 1e6);
    }

    public static double E6IntToDouble(int integer) {
	return (double) (integer / 1e6);
    }

    public static GeoPoint locationToGeoPoint(Location loc) {
	return new GeoPoint(doubleToIntE6(loc.getLatitude()), doubleToIntE6(loc.getLongitude()));
    }

    public static Location geoPointToLocation(GeoPoint point) {
	Location loc = new Location("");
	loc.setLatitude(E6IntToDouble(point.getLatitudeE6()));
	loc.setLongitude(E6IntToDouble(point.getLongitudeE6()));
	return loc;
    }

}
