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

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;

public class TimetableAdapter extends BaseAdapter {

    private final Context context;
    private Timetable timetable;
    private final Animation a;
    private boolean[] changed;

    private static final String TAG = "TimetableAdapter";

    public TimetableAdapter(Context context, Timetable timetable) {
	this.context = context;
	this.timetable = timetable;
	this.a = AnimationUtils.loadAnimation(context, R.anim.updated_stop_view);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
	Log.i(TAG, "Returning stop " + position + " " + timetable.get(position));

	StopView stopView;
	if (convertView == null) {
	    stopView = new StopView(context, timetable.get(position));
	} else {
	    stopView = (StopView) convertView;
	    stopView.setStop(timetable.get(position));
	}

	if (changed == null || changed[position]) {
	    a.reset();
	    stopView.startAnimation(a);
	    Log.i(TAG, "Animating it");
	}

	return stopView;
    }

    public int getCount() {
	return timetable.size();
    }

    public Object getItem(int position) {
	return position;
    }

    public long getItemId(int position) {
	return position;
    }

    public void updateTimetable(Timetable newTimetable) {
	Log.v(TAG, "Old timetable " + timetable);
	Log.v(TAG, "Adaptor loading new timetable");
	changed = new boolean[newTimetable.size()];
	for (int i = 0; i < newTimetable.size(); i++) {
	    if (!timetable.contains(newTimetable.get(i), true)) {
		changed[i] = true;
		Log.i(TAG, "Old timetable does not contain: " + newTimetable.get(i));
	    } else {
		Log.i(TAG, "Old timetable contains: " + newTimetable.get(i));
		changed[i] = false;
	    }
	}
	timetable = newTimetable;
	this.notifyDataSetChanged();
    }
}
