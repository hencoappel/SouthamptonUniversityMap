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
import android.util.Log;

/**
 * Stop represents a Bus stopping at a time at a BusStop.
 * 
 * @author Christopher Baines <cbaines8@gmail.com>
 * 
 */
public class Stop {

    // public static final String BUS_FIELD_NAME = "bus";
    // public static final String BUS_STOP_FIELD_NAME = "busStop";
    // public static final String ARIVAL_TIME_FIELD_NAME = "arivalTime";
    // public static final String FETCH_TIME_FIELD_NAME = "timeOfFetch";

    /**
     * The Bus stopping at the stop
     */
    Bus bus;

    /**
     * The busStop that the bus is stopping at
     */
    BusStop busStop;

    /**
     * The time that the bus is estimated to arrive
     */
    Date arivalTime;

    /**
     * The time this data was fetched from the server
     */
    Date timeOfFetch;

    /**
     * Is the time live, or just expected
     */
    boolean live;

    /**
     * Assumed to be the number of seconds since this data was fetched from the ROMANSE system?
     */
    int age;

    /**
     * 
     * @param bus
     * @param busStop
     * @param arivalTime
     * @param timeOfFetch
     */
    public Stop(Bus bus, BusStop busStop, Date arivalTime, Date timeOfFetch, boolean live) {
	this.busStop = busStop;
	this.bus = bus;
	this.arivalTime = arivalTime;
	this.timeOfFetch = timeOfFetch;
	this.live = live;
    }

    /**
     * 
     * @return
     */
    public String getTimeToArival() {
	if (arivalTime.getTime() - System.currentTimeMillis() <= 60000) {
	    return "Due";
	} else {
	    return (String) DateUtils.getRelativeTimeSpanString(arivalTime.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
	}
    }

    public String getShortTimeToArival() {
	if (arivalTime.getTime() - System.currentTimeMillis() <= 60000) {
	    return "Due";
	} else {
	    String time = (String) DateUtils.getRelativeTimeSpanString(arivalTime.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
	    time = time.replace("in ", "");
	    time = time.replace(" minutes", "m");
	    Log.w("Stop", "time " + time);
	    return time;
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((arivalTime == null) ? 0 : arivalTime.hashCode());
	result = prime * result + ((bus == null) ? 0 : bus.hashCode());
	result = prime * result + ((busStop == null) ? 0 : busStop.hashCode());
	return result;
    }

    /**
     * A printout of the stop data for debugging
     */
    @Override
    public String toString() {
	return "Stop [bus=" + bus + ", busStop=" + busStop + ", arivalTime=" + arivalTime + "]";
    }

    @Override
    // TODO: If this is used, the paramaters need to be checked?
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Stop other = (Stop) obj;
	if (arivalTime == null) {
	    if (other.arivalTime != null)
		return false;
	} else if (!arivalTime.equals(other.arivalTime))
	    return false;
	if (bus == null) {
	    if (other.bus != null)
		return false;
	} else if (!bus.equals(other.bus))
	    return false;
	if (busStop == null) {
	    if (other.busStop != null)
		return false;
	} else if (!busStop.equals(other.busStop))
	    return false;
	return true;
    }

}
