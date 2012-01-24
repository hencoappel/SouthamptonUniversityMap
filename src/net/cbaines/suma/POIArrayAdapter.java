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

import java.util.List;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class POIArrayAdapter extends BaseAdapter {

    private final Context context;
    private final List<POI> POIs;

    public POIArrayAdapter(Context context, List<POI> pois) {
	this.context = context;
	this.POIs = pois;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
	POIView poiView;
	if (convertView == null) {
	    if (POIs.get(position).distTo == -1) {
		poiView = new POIView(context, POIs.get(position));
	    } else {
		poiView = new POIView(context, POIs.get(position), POIs.get(position).distTo);
	    }
	} else {
	    poiView = (POIView) convertView;
	    if (POIs.get(position).distTo == -1) {
		poiView = new POIView(context, POIs.get(position));
	    } else {
		poiView = new POIView(context, POIs.get(position), POIs.get(position).distTo);
	    }
	}

	return poiView;
    }

    public int getCount() {
	return POIs.size();
    }

    public Object getItem(int position) {
	return position;
    }

    public POI getPOIItem(int position) {
	return POIs.get(position);
    }

    public long getItemId(int position) {
	return position;
    }

    public String getItemStringId(int position) {
	return POIs.get(position).id;
    }
}