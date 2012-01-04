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

import java.util.Date;

import android.text.format.DateUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stops")
public class Stop {

    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField(canBeNull = false)
    String name;

    @DatabaseField(canBeNull = false, foreign = true)
    BusStop destStop;

    @DatabaseField(canBeNull = true, foreign = true)
    Bus bus;

    @DatabaseField(canBeNull = false, foreign = true)
    BusStop busStop;

    @DatabaseField(canBeNull = false)
    Date arivalTime;

    @DatabaseField(canBeNull = false)
    Date timeOfFetch;

    Stop() {

    }

    Stop(String name, BusStop busStop, BusStop dest, Bus bus, Date arivalTime, Date timeOfFetch) {
	this.name = name;
	this.busStop = busStop;
	this.destStop = dest;
	this.bus = bus;
	this.arivalTime = arivalTime;
	this.timeOfFetch = timeOfFetch;
    }

    Stop(String name, BusStop busStop, BusStop dest, Date arivalTime, Date timeOfFetch) {
	this(name, busStop, dest, null, arivalTime, timeOfFetch);
    }

    public String getTimeToArival() {

	if (arivalTime.getTime() - System.currentTimeMillis() <= 60000) {
	    return "Due";
	} else {
	    return (String) DateUtils.getRelativeTimeSpanString(arivalTime.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
	}
    }
}
