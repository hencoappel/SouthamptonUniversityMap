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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public class BusTimeActivity extends OrmLiteBaseActivity<DatabaseHelper> implements Runnable, OnCheckedChangeListener {

    final static String TAG = "BusTimeActivity";

    private boolean dataChanged;

    private ListView busTimeList;
    private TextView busName;
    private TextView busID;
    private CheckBox busFavourite;
    private TextView busStopMessage;
    private ProgressBar progBar;
    private LinearLayout busTimeContentLayout;

    private TimetableAdapter adapter;

    private String busStopID;
    private String busStopName;

    private Dao<BusStop, String> busStopDao;

    private BusStop busStop;

    private Thread timetableThread;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.bustimes);

	DatabaseHelper helper = getHelper();

	busStopID = getIntent().getExtras().getString("busStopID");
	busStopName = getIntent().getExtras().getString("busStopName");

	TextView U1RouteTextView = (TextView) findViewById(R.id.busStopU1);
	TextView U1NRouteTextView = (TextView) findViewById(R.id.busStopU1N);
	TextView U2RouteTextView = (TextView) findViewById(R.id.busStopU2);
	TextView U6RouteTextView = (TextView) findViewById(R.id.busStopU6);
	TextView U9RouteTextView = (TextView) findViewById(R.id.busStopU9);

	try {
	    Dao<BusRoute, Integer> busRouteDao = helper.getBusRouteDao();
	    Dao<RouteStops, Integer> routeStopsDao = helper.getRouteStopsDao();

	    for (BusRoute route : busRouteDao) {
		QueryBuilder<RouteStops, Integer> queryBuilder = routeStopsDao.queryBuilder();

		queryBuilder.where().eq(RouteStops.ROUTE_ID_FIELD_NAME, route.id).and().eq(RouteStops.STOP_ID_FIELD_NAME, busStopID);
		queryBuilder.setCountOf(true);
		PreparedQuery<RouteStops> preparedQuery = queryBuilder.prepare();

		long count = routeStopsDao.countOf(preparedQuery);

		if (route.code.equals("U1")) {
		    if (count != 0) {
			U1RouteTextView.setVisibility(View.VISIBLE);
		    } else {
			U1RouteTextView.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U1N")) {
		    if (count != 0) {
			U1NRouteTextView.setVisibility(View.VISIBLE);
		    } else {
			U1NRouteTextView.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U2")) {
		    if (count != 0) {
			U2RouteTextView.setVisibility(View.VISIBLE);
		    } else {
			U2RouteTextView.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U6")) {
		    if (count != 0) {
			U6RouteTextView.setVisibility(View.VISIBLE);
		    } else {
			U6RouteTextView.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U9")) {
		    if (count != 0) {
			U9RouteTextView.setVisibility(View.VISIBLE);
		    } else {
			U9RouteTextView.setVisibility(View.GONE);
		    }
		} else {
		    Log.e(TAG, "Error unknown route " + route.code);
		}

	    }

	    busStopDao = helper.getBusStopDao();

	    busStop = busStopDao.queryForId(busStopID);

	    busFavourite = (CheckBox) findViewById(R.id.favouriteCheckBox);
	    busFavourite.setChecked(busStop.favourite);
	    busFavourite.setOnCheckedChangeListener(this);

	} catch (SQLException e) {
	    e.printStackTrace();
	}

	busName = (TextView) findViewById(R.id.busStopName);
	busID = (TextView) findViewById(R.id.busStopID);

	busStopMessage = (TextView) findViewById(R.id.busStopMessage);
	progBar = (ProgressBar) findViewById(R.id.busStopLoadBar);
	busTimeList = (ListView) findViewById(R.id.busStopTimes);
	busTimeContentLayout = (LinearLayout) findViewById(R.id.busTimeContentLayout);

	Log.i(TAG, "Got busstop id " + busStopID);

	busName.setText(busStopName);
	busID.setText(busStopID);

	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	if (sharedPrefs.getBoolean("liveBusTimesEnabled", false)) {
	    timetableThread = new Thread(this);
	    timetableThread.start();
	} else {
	    progBar.setVisibility(View.GONE);
	    busStopMessage.setText("Live bus times disabled");
	    busStopMessage.setVisibility(View.VISIBLE);
	}

    }

    public void finish() {
	Log.i(TAG, "Stopping BusTimeActivity thread");
	if (timetableThread != null) { // Could happen if live bus times are disabled
	    timetableThread.interrupt();
	}

	if (dataChanged) {
	    getIntent().putExtra("busStopChanged", busStopID);
	}

	setResult(RESULT_OK, getIntent());

	super.finish();
    }

    public void run() {
	while (true) {
	    try {
		Timetable timetable = DataManager.getTimetable(this, busStopID, true);

		Log.i(TAG, "Got timetable for " + busStopID);
		if (timetable == null) {
		    Log.i(TAG, "Its null");
		    busTimeList.post(new Runnable() {
			public void run() {
			    progBar.setVisibility(View.GONE);
			    busStopMessage.setText("Error fetching bus times");
			    busStopMessage.setVisibility(View.VISIBLE);
			}
		    });
		} else {
		    Log.i(TAG, "It contains " + timetable.size() + " stops");

		    if (timetable.size() == 0) {
			busTimeList.post(new Runnable() {
			    public void run() {
				progBar.setVisibility(View.GONE);
				busStopMessage.setText("No Busses");
				busStopMessage.setVisibility(View.VISIBLE);
			    }
			});
		    } else {

			adapter = new TimetableAdapter(this, timetable);

			busTimeList.post(new Runnable() {
			    public void run() {
				progBar.setVisibility(View.GONE);
				busStopMessage.setVisibility(View.GONE);
				busTimeList.setAdapter(adapter);
				busTimeContentLayout.setGravity(Gravity.TOP);
			    }
			});
		    }
		}

	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }

	    try {
		Thread.sleep(20000);
	    } catch (InterruptedException e) {
		Log.i(TAG, "Bus stop activity thread stoped");
		break;
	    }
	}
    }

    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
	busStop.favourite = arg1;
	try {
	    busStopDao.update(busStop);
	    dataChanged = true;
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }
}
