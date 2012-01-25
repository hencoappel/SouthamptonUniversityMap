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

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class BusStopOverlay extends Overlay implements RouteColorConstants {

    private List<BusStop> busStops;

    private final Point mCurScreenCoords = new Point();
    private final Point mTouchScreenPoint = new Point();
    private final Point mItemPoint = new Point();

    private final Rect mRect = new Rect();

    private final Drawable marker;
    private final Drawable favMarker;

    private final Paint paint;

    private static final String TAG = "BusStopOverlay";

    private final Context context;

    private Dao<BusStop, String> busStopDao;

    private float userScale = 1f;

    private HashMap<BusRoute, Boolean> routes = new HashMap<BusRoute, Boolean>();

    public BusStopOverlay(Context context) throws SQLException {
	super(context);
	final long startTime = System.currentTimeMillis();

	this.context = context;

	marker = context.getResources().getDrawable(R.drawable.busstop);
	favMarker = context.getResources().getDrawable(R.drawable.busstop_fav);

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
	busStopDao = helper.getBusStopDao();

	paint = new Paint();
	paint.setStyle(Style.FILL);
	paint.setStrokeWidth(6);

	Log.i(TAG, "Begining to load bus stops in to overlay " + (System.currentTimeMillis() - startTime));
	busStops = busStopDao.queryForAll();
	Log.i(TAG, "Finished loading bus stops in to overlay " + (System.currentTimeMillis() - startTime));
    }

    void setRoutes(BusRoute route, boolean visible) {
	routes.put(route, visible);
    }

    @Override
    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {

	if (shadow) {
	    return;
	}

	float scale = mScale * userScale;

	final Projection pj = mapView.getProjection();

	final int markerWidth = (int) (marker.getIntrinsicWidth() * userScale);
	final int markerHeight = (int) (marker.getIntrinsicHeight() * userScale);

	mRect.set(0, 0, 0 + markerWidth, 0 + markerHeight);
	mRect.offset(-markerWidth / 2, -markerHeight);
	marker.setBounds(mRect);
	favMarker.setBounds(mRect);

	/* Draw in backward cycle, so the items with the least index are on the front. */

	for (int stopNum = 0; stopNum < busStops.size(); stopNum++) {
	    BusStop stop = busStops.get(stopNum);

	    byte routeNum = 0;

	    for (Iterator<BusRoute> busRouteIter = stop.routes.iterator(); busRouteIter.hasNext();) {
		BusRoute route = busRouteIter.next();
		if (routes.get(route)) {
		    break;
		}
		continue;
	    }

	    int yOfsetPerMarker = (int) (10 * scale);
	    int markerYSize = (int) (8 * scale);

	    pj.toMapPixels(stop.point, mCurScreenCoords);

	    if (stop.favourite) {
		Overlay.drawAt(canvas, favMarker, mCurScreenCoords.x, mCurScreenCoords.y, false);
	    } else {
		Overlay.drawAt(canvas, marker, mCurScreenCoords.x, mCurScreenCoords.y, false);
	    }
	    // Log.i(TAG, "Got " + routes.size() + " routes " + routes);

	    int makersPlaced = 0;

	    float rectLeft = mCurScreenCoords.x + (8.8f * scale);
	    float rectRight = rectLeft + markerYSize;

	    if (routeNum == 5) {
		markerYSize = (int) (5 * scale);
		yOfsetPerMarker = (int) (7 * scale);
	    } else if (routeNum == 4) {
		markerYSize = (int) (6.5f * scale);
		yOfsetPerMarker = (int) (8 * scale);
	    }

	    for (BusRoute route : stop.routes) {

		// Log.i(TAG, "Route " + route + " is " + routes.get(route));

		// Log.i(TAG, "Index is " + busRoutes.indexOf(route) + " busRoutes " + busRoutes);

		if (route.code.equals("U1")) {
		    paint.setColor(U1);
		} else if (route.code.equals("U1N")) {
		    paint.setColor(U1N);
		} else if (route.code.equals("U2")) {
		    paint.setColor(U2);
		} else if (route.code.equals("U6")) {
		    paint.setColor(U6);
		} else if (route.code.equals("U9")) {
		    paint.setColor(U9);
		} else {
		    Log.e(TAG, "Unknown route code");
		}

		canvas.drawRect(rectLeft, mCurScreenCoords.y + ((yOfsetPerMarker * makersPlaced) - (45 * scale)), rectRight, mCurScreenCoords.y
			+ (yOfsetPerMarker * makersPlaced) - ((45 * scale) - markerYSize), paint);

		makersPlaced++;
	    }
	}

    }

    @Override
    public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {

	BusStop busStop = getSelectedItem(event, mapView);

	if (busStop == null) {
	    Log.i(TAG, "No busStop pressed");

	    return false;
	} else {
	    Log.i(TAG, "Pressed " + busStop.id);

	    Intent i = new Intent(context, BusStopActivity.class);
	    i.putExtra("busStopID", busStop.id);
	    i.putExtra("busStopName", busStop.description);
	    ((Activity) context).startActivityForResult(i, 0);

	    return true;
	}

    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
	BusStop busStop = getSelectedItem(event, mapView);

	if (busStop == null) {
	    Log.i(TAG, "No busStop pressed");
	    return false;
	} else {
	    Log.i(TAG, "Pressed " + busStop.id);

	    if (busStop.favourite) {
		busStop.favourite = false;

		Toast.makeText(context, busStop.id + " removed from favourites", Toast.LENGTH_SHORT).show();
	    } else {
		Toast.makeText(context, busStop.id + " made a favourite", Toast.LENGTH_SHORT).show();

		busStop.favourite = true;
	    }

	    try {
		busStopDao.update(busStop);
	    } catch (SQLException e) {
		e.printStackTrace();
	    }

	    Collections.sort(busStops, new POIFavouriteComparator());

	    mapView.invalidate();

	    return true;
	}

    }

    public void refresh() {
	try {
	    for (int i = 0; i < busStops.size(); i++) {
		BusStop busStop = busStops.get(i);
		busStopDao.refresh(busStop);
		if (busStop.favourite) {
		    busStops.remove(i);
		    busStops.add(busStop);
		} else {
		    busStops.set(i, busStop);
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Replaces any bus stops that equal the argument in the overlay with the argument
     * 
     * @param busStop
     */
    public void refresh(BusStop busStop) {
	for (int i = 0; i < busStops.size(); i++) {
	    if (busStop.equals(busStops.get(i))) {
		busStops.set(i, busStop);
	    }
	}
    }

    private BusStop getSelectedItem(final MotionEvent event, final MapView mapView) {
	final Projection pj = mapView.getProjection();
	final int eventX = (int) event.getX();
	final int eventY = (int) event.getY();

	/* These objects are created to avoid construct new ones every cycle. */
	pj.fromMapPixels(eventX, eventY, mTouchScreenPoint);

	for (int i = busStops.size() - 1; i > 0; i--) {
	    BusStop busStop = busStops.get(i);

	    pj.toPixels(busStop.point, mItemPoint);

	    if (marker.getBounds().contains(mTouchScreenPoint.x - mItemPoint.x, mTouchScreenPoint.y - mItemPoint.y)) {
		for (Iterator<BusRoute> busRouteIter = busStop.routes.iterator(); busRouteIter.hasNext();) {
		    BusRoute route = busRouteIter.next();
		    if (routes.get(route)) {
			break;
		    }
		    continue;
		}

		return busStop;
	    }
	}
	return null;
    }

}
