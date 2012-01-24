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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public abstract class POI {
    public static final String BUS_STOP = "busstop";
    public static final String BUILDING = "building";
    public static final String WAYPOINT = "waypoint";
    public static final String SITE = "site";

    public static final String ID_FIELD_NAME = "id";
    public static final String POINT_FIELD_NAME = "point";
    public static final String FAVOURITE_FIELD_NAME = "favourite";

    POI() {
    }

    public POI(String id, GeoPoint point) {
	this.id = id;
	this.point = point;
    }

    @DatabaseField(dataType = DataType.SERIALIZABLE, canBeNull = false)
    public GeoPoint point;

    @DatabaseField(id = true)
    public String id;

    @DatabaseField(canBeNull = false)
    public boolean favourite; // This field is not assessed by equals

    public int distTo = -1; // Used by the comparator to store distances, then later by the gui to display them.

    public String type;

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	result = prime * result + ((point == null) ? 0 : point.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
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
	POI other = (POI) obj;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	if (point == null) {
	    if (other.point != null)
		return false;
	} else if (!point.equals(other.point))
	    return false;
	if (type == null) {
	    if (other.type != null)
		return false;
	} else if (!type.equals(other.type))
	    return false;
	return true;
    }
}
