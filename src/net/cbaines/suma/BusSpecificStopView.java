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

import java.sql.SQLException;
import java.text.DateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class BusSpecificStopView extends LinearLayout implements OnClickListener, OnLongClickListener {

    // private final ImageView icon;

    // private static final String TAG = "StopView";

    private final TextView location;
    private final TextView time;
    private String onClickMessage = "";
    private final Context context;

    private Stop stop;

    public BusSpecificStopView(Context context, Stop stop) {
	super(context);

	this.context = context;

	this.setOrientation(HORIZONTAL);

	location = new TextView(context);
	location.setTextSize(22f);

	time = new TextView(context);
	time.setTextSize(22f);
	time.setGravity(Gravity.RIGHT);

	setStop(stop);

	addView(location, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	addView(time, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setStop(Stop stop) {

	// Log.i(TAG, "Time of arival " + stop.arivalTime);

	this.stop = stop;

	location.setText(stop.busStop.description);
	time.setText(stop.getTimeToArival());

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {
	    Dao<Bus, Integer> busDao = helper.getBusDao();

	    busDao.refresh(stop.bus);

	    if (stop.bus.id != null) {
		if (stop.live) {
		    onClickMessage = "Bus " + stop.bus.toString() + " at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		} else {
		    onClickMessage = "Timetabled bus " + stop.bus.toString() + " at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		}
	    } else {
		if (stop.live) {
		    onClickMessage = "Unidentified bus (" + stop.bus.getName() + ") at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		} else {
		    onClickMessage = "Timetabled bus (" + stop.bus.getName() + ") at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	this.setOnClickListener(this);
	this.setOnLongClickListener(this);
    }

    public void onClick(View v) {
	Toast.makeText(context, onClickMessage, Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onLongClick(View v) {
	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {
	    Dao<Bus, Integer> busDao = helper.getBusDao();

	    busDao.refresh(stop.bus);

	    if (stop.bus.id != null) {
		Intent i = new Intent(context, BusActivity.class);
		i.putExtra("busID", stop.bus.id);
		((Activity) context).startActivityForResult(i, 0);
	    } else {
		Toast.makeText(context, "Arival prediction not avalible for timetabled buses", Toast.LENGTH_SHORT).show();
	    }

	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return false;
    }

}
