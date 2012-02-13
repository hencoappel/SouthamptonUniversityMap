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
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class POIView extends LinearLayout {

    private final static String TAG = "POIView";

    private final TextView name;
    private final TextView dist;

    private TextView u1;
    private TextView u1n;
    private TextView u2;
    private TextView u6;
    private TextView u9;

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
	name.setTextSize(16f);
	name.setGravity(Gravity.LEFT);

	dist = new TextView(context);
	dist.setTextSize(16f);
	dist.setGravity(Gravity.RIGHT);

	u1 = new TextView(context);
	u1.setText(R.string.U1);
	u1.setBackgroundResource(R.drawable.u1_back_selected);
	u1n = new TextView(context);
	u1n.setText(R.string.U1N);
	u1n.setBackgroundResource(R.drawable.u1n_back_selected);
	u2 = new TextView(context);
	u2.setText(R.string.U2);
	u2.setBackgroundResource(R.drawable.u2_back_selected);
	u6 = new TextView(context);
	u6.setText(R.string.U6);
	u6.setBackgroundResource(R.drawable.u6_back_selected);
	u9 = new TextView(context);
	u9.setText(R.string.U9);
	u9.setBackgroundResource(R.drawable.u9_back_selected);

	textLayoutParams = new LayoutParams(width - (width / 4), LayoutParams.WRAP_CONTENT);
	LayoutParams distLayoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

	setPOIAndDist(poi, distInM);

	LayoutParams busRouteLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	addView(u1, busRouteLayoutParams);
	addView(u1n, busRouteLayoutParams);
	addView(u2, busRouteLayoutParams);
	addView(u6, busRouteLayoutParams);
	addView(u9, busRouteLayoutParams);

	u1.setVisibility(View.GONE);
	u1n.setVisibility(View.GONE);
	u2.setVisibility(View.GONE);
	u6.setVisibility(View.GONE);
	u9.setVisibility(View.GONE);

	if (poi.type == POI.BUS_STOP) {
	    BusStop busStop = (BusStop) poi;

	    if ((busStop.routes & (1 << 0)) != 0) {
		u1.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 1)) != 0) {
		u1n.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 2)) != 0) {
		u2.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 3)) != 0) {
		u6.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 4)) != 0) {
		u9.setVisibility(View.VISIBLE);
	    }
	}

	addView(name, textLayoutParams);
	addView(dist, distLayoutParams);
    }

    public void setPOI(POI poi) {
	setPOIAndDist(poi, -1);
    }

    public void setPOIAndDist(POI poi, int distInM) {

	// Log.i(TAG, "Looking at poi " + poi.id);

	u1.setVisibility(View.GONE);
	u1n.setVisibility(View.GONE);
	u2.setVisibility(View.GONE);
	u6.setVisibility(View.GONE);
	u9.setVisibility(View.GONE);

	if (poi.type == POI.BUILDING) {
	    Building building = (Building) poi;
	    // Log.i(TAG, "Its a building of name " + building.name);

	    name.setText(building.name + " (" + building.id + ")");
	} else if (poi.type == POI.BUS_STOP) {

	    BusStop busStop = (BusStop) poi;
	    // Log.i(TAG, "Its a bus stop of description " + busStop.description);

	    name.setText(busStop.description + " (" + busStop.id + ")");

	    if ((busStop.routes & (1 << 0)) != 0) {
		u1.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 1)) != 0) {
		u1n.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 2)) != 0) {
		u2.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 3)) != 0) {
		u6.setVisibility(View.VISIBLE);
	    }
	    if ((busStop.routes & (1 << 4)) != 0) {
		u9.setVisibility(View.VISIBLE);
	    }

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