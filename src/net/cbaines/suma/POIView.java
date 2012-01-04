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

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class POIView extends LinearLayout {

    private final static String TAG = "POIView";

    private final TextView name;
    private final TextView dist;

    private LayoutParams textLayoutParams;

    final int width;

    public POIView(Context context, POI poi) {
	this(context, poi, -1);
    }

    public POIView(Context context, POI poi, int distInM) {
	super(context);

	Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	width = display.getWidth();
	// int height = display.getHeight();

	this.setOrientation(HORIZONTAL);

	name = new TextView(context);
	name.setTextSize(22f);
	name.setGravity(Gravity.LEFT);

	dist = new TextView(context);
	dist.setTextSize(22f);
	dist.setGravity(Gravity.RIGHT);

	textLayoutParams = new LayoutParams(width - (width / 4), LayoutParams.WRAP_CONTENT);
	LayoutParams distLayoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

	setPOIAndDist(poi, distInM);

	addView(name, textLayoutParams);
	addView(dist, distLayoutParams);
    }

    public void setPOI(POI poi) {
	setPOIAndDist(poi, -1);
    }

    public void setPOIAndDist(POI poi, int distInM) {

	// Log.i(TAG, "Looking at poi " + poi.id);

	if (poi.type == POI.BUILDING) {
	    Building building = (Building) poi;
	    // Log.i(TAG, "Its a building of name " + building.name);

	    name.setText(building.name + " (" + building.id + ")");
	} else if (poi.type == POI.BUS_STOP) {

	    BusStop busStop = (BusStop) poi;
	    // Log.i(TAG, "Its a bus stop of description " + busStop.description);

	    name.setText(busStop.description + " (" + busStop.id + ")");
	} else if (poi.type == POI.SITE) {

	    Site site = (Site) poi;
	    // Log.i(TAG, "Its a site of name " + site.name);

	    name.setText(site.name + " (" + site.id + ")");
	} else {
	    Log.w(TAG, "Cant identify " + poi.type);

	    name.setText(poi.id);
	}

	textLayoutParams = new LayoutParams(width - (width / 4), LayoutParams.WRAP_CONTENT);

	if (distInM != -1) {
	    textLayoutParams.width = width - (width / 4);
	    name.requestLayout();
	    dist.setText(String.valueOf(distInM) + "m");
	} else {
	    textLayoutParams.width = LayoutParams.FILL_PARENT;
	    name.requestLayout();
	    dist.setText("");
	    // Log.w("POIView", "No dist avalible for S" + poi.id);
	}
    }

}