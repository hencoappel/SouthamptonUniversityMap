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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

public class BuildingNumOverlay extends Overlay {

    private ArrayList<Building> buildings;

    private final Point mCurScreenCoords = new Point();
    private final Point mTouchScreenPoint = new Point();
    private final Point mItemPoint = new Point();

    private final Rect mRect = new Rect();

    private final Drawable marker;
    private final Drawable favMarker;

    private final Paint paint;

    private static final String TAG = "BuildingNumOverlay";

    private final Context context;

    private Dao<Building, String> buildingDao;

    private float userScale = 1f;

    public BuildingNumOverlay(Context context, List<Building> buildings) throws SQLException {
	super(context);

	this.context = context;

	marker = context.getResources().getDrawable(R.drawable.building);
	favMarker = context.getResources().getDrawable(R.drawable.building_fav);

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
	buildingDao = helper.getBuildingDao();

	paint = new Paint();
	paint.setColor(Color.BLACK);
	paint.setAntiAlias(true);
	paint.setStyle(Style.FILL);
	paint.setAlpha(120);
	paint.setStrokeWidth(6);
	paint.setTextAlign(Paint.Align.CENTER);

	this.buildings = (ArrayList<Building>) buildings;
    }

    /**
     * Draw a marker on each of our items. populate() must have been called first.<br/>
     * <br/>
     * The marker will be drawn twice for each Item in the Overlay--once in the shadow phase, skewed and darkened, then again in the non-shadow phase. The
     * bottom-center of the marker will be aligned with the geographical coordinates of the Item.<br/>
     * <br/>
     * The order of drawing may be changed by overriding the getIndexToDraw(int) method. An item may provide an alternate marker via its
     * OverlayItem.getMarker(int) method. If that method returns null, the default marker is used.<br/>
     * <br/>
     * The focused item is always drawn last, which puts it visually on top of the other items.<br/>
     * 
     * @param canvas
     *            the Canvas upon which to draw. Note that this may already have a transformation applied, so be sure to leave it the way you found it
     * @param mapView
     *            the MapView that requested the draw. Use MapView.getProjection() to convert between on-screen pixels and latitude/longitude pairs
     * @param shadow
     *            if true, draw the shadow layer. If false, draw the overlay contents.
     */
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
	for (Iterator<Building> buildingIter = buildings.iterator(); buildingIter.hasNext();) {
	    final Building building = buildingIter.next();

	    // Log.i(TAG, "Looking at drawing stop " + stop.id);

	    pj.toMapPixels(building.point, mCurScreenCoords);

	    // draw it
	    if (building.favourite) {
		Overlay.drawAt(canvas, favMarker, mCurScreenCoords.x, mCurScreenCoords.y, false);
	    } else {
		Overlay.drawAt(canvas, marker, mCurScreenCoords.x, mCurScreenCoords.y, false);
	    }

	    String idString = String.valueOf(building.id);

	    int yOfset = 10;
	    switch (idString.length()) {
	    case 1:
		paint.setTextSize(25 * scale);
		yOfset = 18;
		break;
	    case 2:
		paint.setTextSize(24 * scale);
		yOfset = 18;
		break;
	    case 3:
		paint.setTextSize(17 * scale);
		yOfset = 20;
		break;
	    case 4:
		paint.setTextSize(14 * scale);
		yOfset = 23;
		break;
	    case 5:
		paint.setTextSize(10 * scale);
		yOfset = 20;
		break;
	    case 6:
		paint.setTextSize(9 * scale);
		yOfset = 24;
		break;
	    default:
		Log.w(TAG, "Reverting to default text size for length " + idString.length());
		paint.setTextSize(15 * scale);
		break;
	    }
	    canvas.drawText(idString, mCurScreenCoords.x, mCurScreenCoords.y - (yOfset * scale), paint);
	}
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
	if (!this.isEnabled())
	    return false;

	final Building building = getSelectedItem(event, mapView);

	if (building == null) {
	    Log.i(TAG, "No building pressed");
	    return false;
	} else {
	    Log.i(TAG, "building Pressed " + building.id);

	    Toast.makeText(context, building.name + " (" + building.id + ")", Toast.LENGTH_SHORT).show();
	    return true;
	}

    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
	if (!this.isEnabled())
	    return false;

	final Building building = getSelectedItem(event, mapView);

	if (building == null) {
	    Log.i(TAG, "No building pressed");
	    return false;
	} else {
	    Log.i(TAG, "building Pressed " + building.id);

	    if (building.favourite) {
		building.favourite = false;

		Toast.makeText(context, building.id + " removed from favourites", Toast.LENGTH_SHORT).show();
	    } else {
		Toast.makeText(context, building.id + " made a favourite", Toast.LENGTH_SHORT).show();

		building.favourite = true;
	    }

	    try {
		buildingDao.update(building);
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    Collections.sort(buildings, new POIFavouriteComparator());

	    mapView.invalidate();

	    return true;
	}

    }

    private Building getSelectedItem(final MotionEvent event, final MapView mapView) {
	final Projection pj = mapView.getProjection();
	final int eventX = (int) event.getX();
	final int eventY = (int) event.getY();

	/* These objects are created to avoid construct new ones every cycle. */
	pj.fromMapPixels(eventX, eventY, mTouchScreenPoint);

	// Iterate back through the array to properly deal with overlap
	for (int i = buildings.size() - 1; i > 0; i--) {
	    final Building building = buildings.get(i);

	    pj.toPixels(building.point, mItemPoint);

	    if (marker.getBounds().contains(mTouchScreenPoint.x - mItemPoint.x, mTouchScreenPoint.y - mItemPoint.y)) {
		return building;
	    }
	}
	return null;
    }

    public void refresh() {
	for (int i = 0; i < buildings.size(); i++) {
	    refresh(buildings.get(i));
	}
    }

    public void refresh(Building building) {
	if (building.favourite) {
	    buildings.remove(building);
	    buildings.add(building);
	}
    }

}
