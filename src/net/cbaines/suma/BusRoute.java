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

@DatabaseTable(tableName = "busroutes")
/**
 * This class represents a bus route (U1, U1N, ..). 
 * 
 * @author Christopher Baines <cbaines8@gmail.com>
 *
 */
public class BusRoute {

    final static String ID_FIELD_NAME = "id";
    final static String CODE_FIELD_NAME = "code";
    final static String LABEL_FIELD_NAME = "label";

    private static final String TAG = "BusRoute";

    @DatabaseField(id = true)
    int id;

    @DatabaseField
    String code;

    @DatabaseField
    String label;

    BusRoute() {
    }

    BusRoute(Integer id, String code, String label) {
	this.id = id.intValue();
	this.code = code;
	this.label = label;
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
    BusStop getBusStopBefore(Context context, Stop stop) {
	return moveInRoute(context, stop, -1);
    }

    /**
     * Untested?
     * 
     * @param context
     * @param stop
     * @return
     */
    BusStop getStopAfter(Context context, Stop stop) {
	return moveInRoute(context, stop, 1);
    }

    /**
     * Untested?
     * 
     * @param context
     * @param stop
     * @param moveAmount
     * @return
     */
    BusStop moveInRoute(Context context, Stop stop, int moveAmount) {
	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {
	    Dao<RouteStops, Integer> routeStopsDao = helper.getRouteStopsDao();
	    // Dao<Stop, Integer> stopDao = helper.getStopDao();
	    Dao<BusStop, String> busStopDao = helper.getBusStopDao();

	    int stopSeq;
	    int beforeStopSeq = -1;

	    QueryBuilder<RouteStops, Integer> routeStopsQueryBuilder = routeStopsDao.queryBuilder();
	    routeStopsQueryBuilder.where().eq(RouteStops.ROUTE_ID_FIELD_NAME, this.id).and().eq(RouteStops.STOP_ID_FIELD_NAME, stop.busStop.id);
	    PreparedQuery<RouteStops> routeStopsPreparedQuery = routeStopsQueryBuilder.prepare();

	    List<RouteStops> stopsFound = routeStopsDao.query(routeStopsPreparedQuery);
	    if (stopsFound.size() != 0) {
		Log.e(TAG, "Wierd, found more than one stop");
		return null;
	    }

	    long maxSeq = 0;

	    routeStopsQueryBuilder = routeStopsDao.queryBuilder();
	    routeStopsQueryBuilder.where().eq(RouteStops.ROUTE_ID_FIELD_NAME, this.id);
	    routeStopsQueryBuilder.setCountOf(true);
	    routeStopsPreparedQuery = routeStopsQueryBuilder.prepare();

	    maxSeq = routeStopsDao.countOf(routeStopsPreparedQuery);

	    if (maxSeq == 0) {
		Log.e(TAG, "Something wierd has gone on, maxSeq equals 0");
		return null;
	    }

	    if (id == 326) { // U1

		if (stop.name.equals("U1C")) {// Seq 0 = End of route

		    stopSeq = routeStopsDao.query(routeStopsPreparedQuery).get(0).sequence;

		    beforeStopSeq = stopSeq + moveAmount;

		} else if (stop.name.equals("U1A")) { // seq 88 == end of route

		    stopSeq = routeStopsDao.query(routeStopsPreparedQuery).get(0).sequence;

		    beforeStopSeq = stopSeq - moveAmount;

		} else {
		    Log.e(TAG, "In route U1 but " + stop.name + " does not match U1A or U1C");
		    return null;
		}
	    } else {
		Log.e(TAG, "Route id not recognised " + id);
		return null;
	    }

	    if (beforeStopSeq == -1) {
		Log.e(TAG, "Something wierd has gone on, beforeStopSeq equals -1");
		return null;
	    }

	    routeStopsQueryBuilder = routeStopsDao.queryBuilder();
	    routeStopsQueryBuilder.where().eq(RouteStops.ROUTE_ID_FIELD_NAME, this.id).and().eq(RouteStops.SEQUENCE_ID_FIELD_NAME, beforeStopSeq);

	    routeStopsPreparedQuery = routeStopsQueryBuilder.prepare();

	    List<RouteStops> beforeStopsFound = routeStopsDao.query(routeStopsPreparedQuery);
	    if (stopsFound.size() != 0) {
		Log.e(TAG, "Wierd, found more than one before stop");
		return null;
	    }

	    busStopDao.refresh(beforeStopsFound.get(0).stop);

	    return beforeStopsFound.get(0).stop;

	} catch (SQLException e) {
	    e.printStackTrace();
	    return null;
	}
    }

}
