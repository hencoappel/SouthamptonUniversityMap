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

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
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

    private static final String TAG = "BusSpecificStopView";

    private Handler handler;

    private Runnable refreshData;

    // private static final String TAG = "StopView";

    private final TextView location;
    private final TextView time;
    private String onClickMessage = "";
    private final Context context;

    private GetTimetableStopTask task;

    private Stop stop;

    public BusSpecificStopView(Context context, Stop newStop, Handler newHandler) {
	super(context);

	this.context = context;
	this.handler = newHandler;

	this.setOrientation(HORIZONTAL);

	location = new TextView(context);
	location.setTextSize(22f);

	time = new TextView(context);
	time.setTextSize(22f);
	time.setGravity(Gravity.RIGHT);

	setStop(stop);

	addView(location, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	addView(time, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

	refreshData = new Runnable() {
	    @Override
	    public void run() {
		if (stop.timeOfFetch == null || (stop.timeOfFetch.getTime() - System.currentTimeMillis()) > 20000) {
		    if (task == null) {
			task = new GetTimetableStopTask();
			BusStop[] str = { stop.busStop };
			task.execute(str);
			handler.postDelayed(refreshData, 20000);
		    }
		}

	    }
	};

	handler.post(refreshData);
    }

    private class GetTimetableStopTask extends AsyncTask<BusStop, Integer, Stop> {
	private String errorMessage;

	private BusStop busStop;

	private int position;

	protected void onPreExecute() {
	    // progBar.setVisibility(View.VISIBLE);
	}

	protected Stop doInBackground(BusStop... busStopArray) {
	    busStop = busStopArray[0];
	    Stop stop = null;

	    try {
		Log.i(TAG, "Fetching stop for busStop " + position);
		stop = DataManager.getStop(context, stop.bus, busStop);
		if (stop == null) {
		    stop = new Stop(stop.bus, busStop, null, null, false);
		}
		Log.i(TAG, "Finished fetching stop for busStop " + position);
	    } catch (SQLException e) {
		errorMessage = "Error message regarding SQL?";
		e.printStackTrace();
	    } catch (ClientProtocolException e) {
		errorMessage = "ClientProtocolException!?!";
		e.printStackTrace();
	    } catch (IOException e) {
		errorMessage = "Error fetching bus times from server, are you connected to the internet?";
		e.printStackTrace();
	    } catch (JSONException e) {
		errorMessage = "Error parsing bus times";
		e.printStackTrace();
	    }
	    return stop;
	}

	protected void onPostExecute(Stop stop) {
	    // Log.i(TAG, "Got timetable");
	    if (stop == null) {

		// progBar.setVisibility(View.GONE);
		// busContentMessage.setText(errorMessage);
		// busContentMessage.setVisibility(View.VISIBLE);
	    } else {
		// progBar.setVisibility(View.GONE);

	    }
	}
    }

    public void setStop(Stop stop) {

	// Log.i(TAG, "Time of arival " + stop.arivalTime);

	this.stop = stop;

	if (stop.busStop.description.length() > 20) {
	    location.setText(stop.busStop.description.substring(0, 20)); // TODO
	} else {
	    location.setText(stop.busStop.description); // TODO
	}
	if (stop.arivalTime != null) {
	    time.setText(stop.getShortTimeToArival());
	} else {
	    time.setText("");
	}

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {
	    Dao<Bus, Integer> busDao = helper.getBusDao();

	    busDao.refresh(stop.bus);

	    if (stop.arivalTime != null) {

		if (stop.bus.id != null) {
		    if (stop.live) {
			onClickMessage = "Bus " + stop.bus.toString() + " at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		    } else {
			onClickMessage = "Timetabled bus " + stop.bus.toString() + " at "
				+ DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		    }
		} else {
		    if (stop.live) {
			onClickMessage = "Unidentified bus (" + stop.bus.getName() + ") at "
				+ DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		    } else {
			onClickMessage = "Timetabled bus (" + stop.bus.getName() + ") at "
				+ DateFormat.getTimeInstance(DateFormat.SHORT).format(stop.arivalTime);
		    }
		}
	    } else {
		if (stop.bus.id != null) {
		    if (stop.live) {
			onClickMessage = "Bus " + stop.bus.toString();
		    } else {
			onClickMessage = "Timetabled bus " + stop.bus.toString();
		    }
		} else {
		    if (stop.live) {
			onClickMessage = "Unidentified bus (" + stop.bus.getName() + ")";
		    } else {
			onClickMessage = "Timetabled bus (" + stop.bus.getName() + ")";
		    }
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
    public boolean onLongClick(View v) { // TODO
	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {
	    Dao<Bus, Integer> busDao = helper.getBusDao();

	    busDao.refresh(stop.bus);

	    if (stop.bus.id != null) {
		Intent i = new Intent(context, SouthamptonUniversityMapActivity.class);
		i.putExtra("poiPoint", stop.busStop.point.toDoubleString());
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
