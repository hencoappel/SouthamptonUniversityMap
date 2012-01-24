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

@DatabaseTable(tableName = "buses")
public class Bus {

    @DatabaseField(id = true)
    public int id;

    @DatabaseField(canBeNull = true, foreign = true)
    Stop lastKnownStop;

    @DatabaseField(canBeNull = true, foreign = true)
    Stop firstKnownStop;

    @DatabaseField(canBeNull = false, foreign = true)
    BusRoute lastKnownRoute;

    Bus() {
    }

    Bus(int id, BusRoute lastKnownRoute, Stop lastKnownStop) {
	this.id = id;
	this.lastKnownRoute = lastKnownRoute;
	this.lastKnownStop = lastKnownStop;
    }

    public Bus(int id, BusRoute lastKnownRoute) {
	this(id, lastKnownRoute, null);
    }

    public String toString() {
	return String.valueOf(id);
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
	Bus other = (Bus) obj;
	if (id != other.id)
	    return false;
	return true;
    }

}
