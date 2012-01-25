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
import java.util.Collection;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
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

    @DatabaseField
    String label;

    @ForeignCollectionField(eager = false)
    Collection<Direction> directions;

    BusRoute() {
    }

    public BusRoute(Integer id, String code, String label) {
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
    BusStop getBusStopBefore(Context context, BusStop stop, Direction dir) {
	return moveInRoute(context, stop, dir, -1);
    }

    /**
     * Untested?
     * 
     * @param context
     * @param stop
     * @return
     */
    BusStop getStopAfter(Context context, BusStop stop, Direction dir) {
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
    BusStop moveInRoute(Context context, BusStop stop, Direction dir, int moveAmount) {
	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {
	    Dao<RouteStops, Integer> routeStopsDao = helper.getRouteStopsDao();
	    Dao<BusStop, String> busStopDao = helper.getBusStopDao();

	    QueryBuilder<RouteStops, Integer> routeStopsQueryBuilder = routeStopsDao.queryBuilder();
	    routeStopsQueryBuilder.where().eq(RouteStops.ROUTE_ID_FIELD_NAME, this.id);
	    PreparedQuery<RouteStops> routeStopsPreparedQuery = routeStopsQueryBuilder.prepare();

	    List<RouteStops> routeStopsFound = routeStopsDao.query(routeStopsPreparedQuery);

	    if (moveAmount > 0) {
		for (int i = 0; i < routeStopsFound.size(); i++) {
		    if (stop.equals(routeStopsFound.get(i))) {
			int stopWanted = (i + moveAmount) % (routeStopsFound.size() + 1);
			busStopDao.refresh(routeStopsFound.get(stopWanted).stop);

			return routeStopsFound.get(stopWanted).stop;
		    }
		}
	    } else {
		int maxSeq = routeStopsFound.size() - 1;
		int stopWanted = maxSeq - (Math.abs(maxSeq) % (routeStopsFound.size() + 1));
		busStopDao.refresh(routeStopsFound.get(stopWanted).stop);

		return routeStopsFound.get(stopWanted).stop;
	    }

	} catch (SQLException e) {
	    e.printStackTrace();
	}
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
