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
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "buildings")
public class Building extends POI {
    public static final String NAME_FIELD_NAME = "name";
    public static final String RESIDENTIAL_FIELD_NAME = "residential";
    public static final String OUTLINE_FIELD_NAME = "outline";

    @DatabaseField(canBeNull = false)
    public String name;
    @DatabaseField(canBeNull = false)
    public boolean residential;
    @DatabaseField(dataType = DataType.SERIALIZABLE, canBeNull = true)
    Polygon outline;

    Building(String id, GeoPoint point, boolean residential, String name, Polygon outline) {
	super(id, point);
	this.residential = residential;
	this.name = name;
	this.outline = outline;
	this.type = POI.BUILDING;
    }

    public Building(String id, GeoPoint point, boolean residential, String name) {
	this(id, point, residential, name, null);
    }

    Building(String id, GeoPoint point, boolean residential) {
	this(id, point, residential, "");
    }

    Building() {
	this.type = POI.BUILDING;
    }

    public String toString() {
	return name + " (" + id + ")";
    }
}
