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

import java.util.Comparator;

public class POIFavouriteComparator implements Comparator<POI> {

    public int compare(POI poi1, POI poi2) {
	if (poi1.favourite) {
	    if (poi2.favourite) {
		return 0;
	    } else {
		return 1;
	    }
	} else if (poi2.favourite) {
	    return -1;
	} else {
	    return 0;
	}
    }

}
