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

import org.osmdroid.util.GeoPoint;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "busstops")
public class BusStop extends POI {
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String BAY_FIELD_NAME = "bay";
    public static final String ROUTES_FIELD_NAME = "bay";

    @DatabaseField(canBeNull = true)
    public String description;
    @DatabaseField(canBeNull = true)
    public String bay;

    // Used to speed up accessing the relevent uni link routes for a bus stop, if == 0, this is not a uni link stop
    @DatabaseField(canBeNull = false)
    public byte routes;

    public BusStop(String location, String description, String bay, GeoPoint point) {
	this.id = location;
	this.description = description;
	this.bay = bay;
	this.point = point;
	this.type = POI.BUS_STOP;
    }

    BusStop() {
	this.type = POI.BUS_STOP;
    }

    public String toString() {
	return description + " (" + id + ")";
    }
}
