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

/**
 * Represents a bus
 * 
 * @author Christopher Baines <cbaines8@gmail.com>
 * 
 */
@DatabaseTable(tableName = "buses")
public class Bus {

    final static String ID_FIELD_NAME = "id";
    final static String ROUTE_FIELD_NAME = "route";
    final static String DIRECTION_FIELD_NAME = "direction";

    @DatabaseField(generatedId = true)
    int gid;

    /**
     * The identification number of the bus.
     */
    @DatabaseField(canBeNull = true)
    String id;

    /**
     * The route the bus is travelling.
     */
    @DatabaseField(canBeNull = false, foreign = true)
    BusRoute route;

    /**
     * The direction which the bus is travelling.
     */
    @DatabaseField(canBeNull = false)
    String direction;

    /**
     * The destination the bus is travelling towards.
     */
    @DatabaseField(canBeNull = false, foreign = true)
    BusStop destination;

    Bus() {
    }

    /**
     * Create a bus.
     * 
     * @param id
     *            The identification number of the bus.
     * @param route
     *            The route the bus is travelling.
     * @param dir
     *            The direction which the bus is travelling.
     */
    Bus(String id, BusRoute route, String direction) {
	this.id = id;
	this.route = route;
	this.direction = direction;
    }

    /**
     * Create a bus.
     * 
     * @param id
     *            The identification number of the bus.
     * @param route
     *            The route the bus is travelling.
     */
    Bus(String id, BusRoute route) {
	this(id, route, null);
    }

    public String toString() {
	return String.valueOf(id + " (" + route.code + direction + ")");
    }

    String getName() {
	if (direction != null) {
	    return route.code + direction;
	} else {
	    return route.code;
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + gid;
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
	Bus other = (Bus) obj;
	if (id != other.id)
	    return false;
	return true;
    }

}
