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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

public class BusStopActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnCheckedChangeListener {

    final static String TAG = "BusTimeActivity";

    private boolean dataChanged;

    private ListView busTimeList;
    private TextView busName;
    private TextView busID;
    private CheckBox busFavourite;
    private TextView busStopMessage;
    private ProgressBar progBar;
    private LinearLayout busTimeContentLayout;

    protected Timetable timetable;

    protected String busStopID;
    private String busStopName;

    private Dao<BusStop, String> busStopDao;

    private BusStop busStop;

    private GetTimetableTask timetableTask;

    private Context instance;

    private Handler mHandler;
    private Runnable refreshData;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.bustimes);

	final DatabaseHelper helper = getHelper();

	instance = this;

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
    }

    public void onResume() {
	super.onResume();

	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	if (sharedPrefs.getBoolean("liveBusTimesEnabled", false)) {
	    Log.i(TAG, "Live Times enabled");
	    timetable = (Timetable) getLastNonConfigurationInstance();

	    refreshData = new Runnable() {
		@Override
		public void run() {
		    timetableTask = new GetTimetableTask();
		    timetableTask.execute(busStopID);
		    mHandler.postDelayed(refreshData, 20000);
		}
	    };

	    mHandler = new Handler();

	    if (timetable == null) {
		Log.i(TAG, "No Previous timetable");
		mHandler.post(refreshData);
	    } else {
		Log.i(TAG, "Displaying previous timetable");
		displayTimetable(timetable);
	    }

	} else {
	    Log.i(TAG, "Live Times Disabled");
	    progBar.setVisibility(View.GONE);
	    busStopMessage.setText("Live bus times disabled");
	    busStopMessage.setVisibility(View.VISIBLE);
	}

    }

    public void onPause() {
	if (mHandler != null) { // BusTimes are enabled
	    mHandler.removeCallbacks(refreshData);
	    timetableTask.cancel(true);
	    Log.i(TAG, "Stoping refreshing timetable data");
	}

	super.onPause();
    }

    public void finish() {
	if (dataChanged) {
	    getIntent().putExtra("busStopChanged", busStopID);
	}

	setResult(RESULT_OK, getIntent());

	super.finish();
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

    @Override
    public Object onRetainNonConfigurationInstance() {
	return timetable;
    }

    private class GetTimetableTask extends AsyncTask<String, Integer, Timetable> {
	protected Timetable doInBackground(String... activity) {
	    Timetable newTimetable = null;
	    try {
		newTimetable = DataManager.getTimetable(instance, busStopID, true);
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	    return newTimetable;
	}

	protected void onPostExecute(Timetable newTimetable) {
	    Log.i(TAG, "Got timetable for " + busStopID);
	    if (newTimetable == null) {
		Log.i(TAG, "Its null");

		progBar.setVisibility(View.GONE);
		busStopMessage.setText("Error fetching bus times");
		busStopMessage.setVisibility(View.VISIBLE);
	    } else {
		timetable = newTimetable;
		displayTimetable(timetable);
	    }
	}
    }

    private void displayTimetable(Timetable timetable) {
	Log.i(TAG, "It contains " + timetable.size() + " stops");

	if (timetable.size() == 0) {
	    progBar.setVisibility(View.GONE);
	    busStopMessage.setText("No Busses");
	    busStopMessage.setVisibility(View.VISIBLE);
	} else {
	    progBar.setVisibility(View.GONE);
	    busStopMessage.setVisibility(View.GONE);
	    TimetableAdapter adapter;
	    if ((adapter = (TimetableAdapter) busTimeList.getAdapter()) != null) {
		adapter.updateTimetable(timetable);
	    } else {
		adapter = new TimetableAdapter(this, timetable);
		busTimeList.setAdapter(adapter);
	    }
	    busTimeContentLayout.setGravity(Gravity.TOP);
	}
    }

}
