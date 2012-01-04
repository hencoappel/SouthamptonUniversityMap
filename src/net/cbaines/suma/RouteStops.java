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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "routestops")
public class RouteStops {
    public final static String STOP_ID_FIELD_NAME = "stop_id";
    public final static String ROUTE_ID_FIELD_NAME = "route_id";
    public final static String SEQUENCE_ID_FIELD_NAME = "sequence";

    /**
     * This id is generated by the database and set on the object when it is passed to the create method. An id is needed in case we need to update or delete
     * this object in the future.
     */
    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField
    int sequence;

    // This is a foreign object which just stores the id from the User object in this table.
    @DatabaseField(foreign = true, columnName = STOP_ID_FIELD_NAME, indexName = "routestops_routestop_idx")
    BusStop stop;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, columnName = ROUTE_ID_FIELD_NAME, indexName = "routestops_routestop_idx")
    BusRoute route;

    RouteStops() {
	// for ormlite
    }

    public RouteStops(BusStop stop, BusRoute route, int sequence) {
	this.stop = stop;
	this.route = route;
	this.sequence = sequence;
    }
}
