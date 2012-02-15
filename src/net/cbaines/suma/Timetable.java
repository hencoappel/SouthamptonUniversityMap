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

import java.util.ArrayList;
import java.util.Date;

public class Timetable extends ArrayList<Stop> {

    /**
     * 
     */
    private static final long serialVersionUID = -9021303378059511643L;

    Date fetchTime;

    public String toString() {
	StringBuilder sb = new StringBuilder();
	for (Stop stop : this) {
	    sb.append(stop + "\n");
	}
	return sb.toString();
    }

    public boolean contains(Stop otherStop, boolean toTheMinute) {
	if (otherStop == null)
	    return false;
	if (toTheMinute) {
	    for (Stop stop : this) {
		if (otherStop.bus != null && stop.bus != null && otherStop.bus.equals(stop.bus)) {
		    if (Math.abs(otherStop.arivalTime.getTime() - stop.arivalTime.getTime()) < 60000) {
			return true;
		    }
		} else if (otherStop.busStop.equals(stop.busStop)) {
		    if (otherStop.arivalTime == null && stop.arivalTime == null) {
			return true;
		    } else {
			if (otherStop.arivalTime == null || stop.arivalTime == null) {
			    return false;
			} else if (Math.abs(otherStop.arivalTime.getTime() - stop.arivalTime.getTime()) < 60000) {
			    return true;
			}
		    }
		}
	    }
	    return false;
	} else {
	    return this.contains(otherStop);
	}
    }

}
