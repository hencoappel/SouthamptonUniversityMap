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
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

/**
 * This class represents a bus route (U1, U1N, ..).
 * 
 * @author Christopher Baines <cbaines8@gmail.com>
 * 
 */
@DatabaseTable(tableName = "busroutes")
public class BusRoute {

    final static String ID_FIELD_NAME = "id";
    final static String CODE_FIELD_NAME = "code";
    final static String LABEL_FIELD_NAME = "label";

    @DatabaseField(id = true)
    public int id;

    /**
     * The route code (U1, U1N, ...)
     */
    @DatabaseField
    public String code;

    @DatabaseField(canBeNull = false)
    String label;

    @DatabaseField(canBeNull = true)
    String forwardDirection;

    @DatabaseField(canBeNull = true)
    String reverseDirection;

    BusRoute() {
    }

    public BusRoute(Integer id, String code, String label, String forwardDirection, String reverseDirection) {
	this.id = id.intValue();
	this.code = code;
	this.label = label;
	this.forwardDirection = forwardDirection;
	this.reverseDirection = reverseDirection;
    }

    public BusRoute(Integer id, String code, String label) {
	this(id, code, label, null, null);
    }

    public String toString() {
	return code;
    }

    /**
     * Untested?
     * 
     * @param context
     * @param stop
     * @return
     */
    BusStop getBusStopBefore(Context context, BusStop stop, String dir) {
	return moveInRoute(context, stop, dir, -1);
    }

    /**
     * Untested?
     * 
     * @param context
     * @param stop
     * @return
     */
    BusStop getStopAfter(Context context, BusStop stop, String dir) {
	return moveInRoute(context, stop, dir, 1);
    }

    /**
     * Untested?
     * 
     * @param context
     * @param stop
     * @param moveAmount
     * @return
     */
    BusStop moveInRoute(Context context, BusStop stop, String direction, int moveAmount) {
	if (moveAmount == 0) {
	    return stop;
	}

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	if (forwardDirection != null) {

	    if (direction == null) {
		return null;
	    }

	    if (forwardDirection.equals(direction)) {

	    } else if (reverseDirection.equals(direction)) {
		moveAmount = -moveAmount;
	    } else {
		Log.e("BusRoute", "Direction (" + direction + ") doesnt match either the forward direction (" + forwardDirection + ") or reverse direction ("
			+ reverseDirection + ")");
		return null;
	    }

	}

	try {
	    Dao<RouteStops, Integer> routeStopsDao = helper.getRouteStopsDao();
	    Dao<BusStop, String> busStopDao = helper.getBusStopDao();

	    QueryBuilder<RouteStops, Integer> routeStopsQueryBuilder = routeStopsDao.queryBuilder();
	    routeStopsQueryBuilder.where().eq(RouteStops.ROUTE_ID_FIELD_NAME, this.id);
	    PreparedQuery<RouteStops> routeStopsPreparedQuery = routeStopsQueryBuilder.prepare();

	    List<RouteStops> routeStopsFound = routeStopsDao.query(routeStopsPreparedQuery);
	    Log.v("BusRoute", "Found " + routeStopsFound.size() + " stops");
	    
	    int stopIndex = 0;
	    
	    for (RouteStops routeStop : routeStopsFound) {
		if (routeStop.stop.id.equals(stop.id)) {
		    stopIndex = routeStop.sequence -1;
		}
	    }

	    if (moveAmount > 0) {
		Log.v("BusStop", "stopIndex " + stopIndex);
		int stopWanted = (stopIndex + moveAmount) % (routeStopsFound.size() + 1);
		Log.v("BusStop", "stopWanted " + stopWanted);
		busStopDao.refresh(routeStopsFound.get(stopWanted).stop);

		Log.v("BusRoute",
			"Moving forward in direction " + direction + " " + moveAmount + " stops from " + stop + " to " + routeStopsFound.get(stopWanted).stop
				+ " in route " + this);

		return routeStopsFound.get(stopWanted).stop;
	    } else {
		Log.v("BusStop", "stopIndex " + stopIndex);
		int stopWanted = stopIndex + moveAmount;
		if (stopWanted < 0) {
		    stopWanted = routeStopsFound.size() - (Math.abs(stopWanted) % routeStopsFound.size());
		}
		Log.v("BusStop", "stopWanted " + stopWanted);
		busStopDao.refresh(routeStopsFound.get(stopWanted).stop);

		Log.v("BusRoute",
			"Moving backwards in direction " + direction + " " + moveAmount + " stops from " + stop + " to " + routeStopsFound.get(stopWanted).stop
				+ " in route " + this);

		return routeStopsFound.get(stopWanted).stop;
	    }

	} catch (SQLException e) {
	    e.printStackTrace();
	}
	Log.e("BusRoute", "Error moving in route");
	return null;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + id;
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	BusRoute other = (BusRoute) obj;
	if (id != other.id)
	    return false;
	return true;
    }

}
