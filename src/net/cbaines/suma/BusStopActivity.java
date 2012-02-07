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
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class BusStopActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnCheckedChangeListener, Preferences {

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
    private Timetable visibleTimetable;

    protected String busStopID;
    private String busStopName;

    private Dao<BusStop, String> busStopDao;

    private BusStop busStop;

    private GetTimetableTask timetableTask;

    private Context instance;

    private Handler mHandler;
    private Runnable refreshData;

    private CheckBox U1RouteRadioButton;
    private CheckBox U1NRouteRadioButton;
    private CheckBox U2RouteRadioButton;
    private CheckBox U6RouteRadioButton;
    private CheckBox U9RouteRadioButton;

    private HashSet<BusRoute> routes = new HashSet<BusRoute>();

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.bustimes);

	final DatabaseHelper helper = getHelper();

	instance = this;

	busStopID = getIntent().getExtras().getString("busStopID");
	busStopName = getIntent().getExtras().getString("busStopName");

	U1RouteRadioButton = (CheckBox) findViewById(R.id.radio_u1);
	U1NRouteRadioButton = (CheckBox) findViewById(R.id.radio_u1n);
	U2RouteRadioButton = (CheckBox) findViewById(R.id.radio_u2);
	U6RouteRadioButton = (CheckBox) findViewById(R.id.radio_u6);
	U9RouteRadioButton = (CheckBox) findViewById(R.id.radio_u9);

	U1RouteRadioButton.setOnCheckedChangeListener(this);
	U1NRouteRadioButton.setOnCheckedChangeListener(this);
	U2RouteRadioButton.setOnCheckedChangeListener(this);
	U6RouteRadioButton.setOnCheckedChangeListener(this);
	U9RouteRadioButton.setOnCheckedChangeListener(this);

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
			U1RouteRadioButton.setVisibility(View.VISIBLE);
			routes.add(route);
		    } else {
			U1RouteRadioButton.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U1N")) {
		    if (count != 0) {
			U1NRouteRadioButton.setVisibility(View.VISIBLE);
			routes.add(route);
		    } else {
			U1NRouteRadioButton.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U2")) {
		    if (count != 0) {
			U2RouteRadioButton.setVisibility(View.VISIBLE);
			routes.add(route);
		    } else {
			U2RouteRadioButton.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U6")) {
		    if (count != 0) {
			U6RouteRadioButton.setVisibility(View.VISIBLE);
			routes.add(route);
		    } else {
			U6RouteRadioButton.setVisibility(View.GONE);
		    }
		} else if (route.code.equals("U9")) {
		    if (count != 0) {
			U9RouteRadioButton.setVisibility(View.VISIBLE);
			routes.add(route);
		    } else {
			U9RouteRadioButton.setVisibility(View.GONE);
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
	if (sharedPrefs.getBoolean(UNI_LINK_BUS_TIMES, false) || sharedPrefs.getBoolean(NON_UNI_LINK_BUS_TIMES, false)) {
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
		if (System.currentTimeMillis() - timetable.fetchTime.getTime() > 20000) {
		    mHandler.post(refreshData);
		}
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
	    if (timetableTask != null) // Could happen if the handler has not created the timetableTask yet
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

    public void onCheckedChanged(CompoundButton button, boolean checked) {
	if (button.equals(busFavourite)) {
	    busStop.favourite = checked;
	    try {
		busStopDao.update(busStop);
		dataChanged = true;
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	} else {

	    Log.i(TAG, "Route radio button made " + checked);

	    displayTimetable(timetable);

	}
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
	return timetable;
    }

    private class GetTimetableTask extends AsyncTask<String, Integer, Timetable> {
	String errorMessage;

	protected void onPreExecute() {
	    progBar.setVisibility(View.VISIBLE);
	}

	protected Timetable doInBackground(String... activity) {
	    Timetable newTimetable = null;
	    try {
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(instance);

		newTimetable = DataManager.getTimetable(instance, busStopID, sharedPrefs.getBoolean(SouthamptonUniversityMapActivity.UNI_LINK_BUS_TIMES,
			SouthamptonUniversityMapActivity.UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT), sharedPrefs.getBoolean(
			SouthamptonUniversityMapActivity.NON_UNI_LINK_BUS_TIMES, SouthamptonUniversityMapActivity.NON_UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT));
	    } catch (SQLException e) {
		errorMessage = "Error message regarding SQL?";
		e.printStackTrace();
	    } catch (ClientProtocolException e) {
		errorMessage = "Insert error message here!";
		e.printStackTrace();
	    } catch (IOException e) {
		errorMessage = "Error fetching bus times from server, are you connected to the internet?";
		e.printStackTrace();
	    } catch (JSONException e) {
		errorMessage = "Error parsing bus times";
		e.printStackTrace();
	    }
	    return newTimetable;
	}

	protected void onPostExecute(Timetable newTimetable) {
	    Log.i(TAG, "Got timetable for " + busStopID);
	    if (newTimetable == null) {
		Log.i(TAG, "Its null");

		progBar.setVisibility(View.GONE);
		busStopMessage.setText(errorMessage);
		busStopMessage.setVisibility(View.VISIBLE);
	    } else {
		progBar.setVisibility(View.GONE);
		timetable = newTimetable;
		displayTimetable(timetable);
	    }
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.stop_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle item selection
	if (false) { // (item.getItemId() == R.id.menu_previous_stop || item.getItemId() == R.id.menu_next_stop) {
	    Log.v(TAG, "Got a request for the stop movement");

	    Log.v(TAG, routes.size() + " routes avalible from this stop");

	    HashSet<BusStop> busStops = new HashSet<BusStop>();

	    for (BusRoute route : routes) {
		try {
		    if (false) { // (item.getItemId() == R.id.menu_next_stop) {
			busStops.add(route.moveInRoute(this, getHelper().getBusStopDao().queryForId(busStopID), null, 1));
		    } else {
			busStops.add(route.moveInRoute(this, getHelper().getBusStopDao().queryForId(busStopID), null, -1));
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }

	    Log.i(TAG, "stops " + busStops);

	    if (busStops.size() == 1) {
		Intent i = new Intent(this, BusStopActivity.class);
		BusStop stop = busStops.iterator().next();
		if (stop == null) {
		    Log.e(TAG, "stop == null");
		}
		if (stop.id == null) {
		    Log.e(TAG, "stop.id == null");
		}
		i.putExtra("busStopID", stop.id);
		i.putExtra("busStopName", stop.description);
		startActivity(i);
	    } else {
		// Show dialog
		Log.i(TAG, "Showing dialog");
	    }

	} else if (item.getItemId() == R.id.menu_refresh_stop) {
	    if (mHandler != null) { // BusTimes are enabled
		mHandler.removeCallbacks(refreshData);
		timetableTask.cancel(true);
		Log.i(TAG, "Stoping refreshing timetable data");

		mHandler.post(refreshData);
	    } else {
		// TODO: Toast here...
	    }
	} else {
	    Log.e(TAG, "No known menu option selected");
	    return super.onOptionsItemSelected(item);
	}
	return true;
    }

    private void displayTimetable(Timetable timetable) {
	visibleTimetable = (Timetable) timetable.clone();

	Log.i(TAG, "It contains " + visibleTimetable.size() + " stops");

	if (timetable.size() == 0) {
	    busStopMessage.setText("No Busses");
	    busStopMessage.setVisibility(View.VISIBLE);
	    busTimeContentLayout.setGravity(Gravity.CENTER);
	} else {

	    for (Iterator<Stop> stopIter = visibleTimetable.iterator(); stopIter.hasNext();) {
		Stop stop = stopIter.next();
		Log.i(TAG, "Begin filtering, looking at " + stop + " with route " + stop.bus.route.code);
		if (stop.bus.route.code.equals("U1")) {
		    if (!U1RouteRadioButton.isChecked()) {
			stopIter.remove();
		    }
		} else if (stop.bus.route.code.equals("U1N")) {
		    if (!U1NRouteRadioButton.isChecked()) {
			stopIter.remove();
		    }
		} else if (stop.bus.route.code.equals("U2")) {
		    if (!U2RouteRadioButton.isChecked()) {
			stopIter.remove();
		    }
		} else if (stop.bus.route.code.equals("U6")) {
		    if (!U6RouteRadioButton.isChecked()) {
			stopIter.remove();
		    }
		} else if (stop.bus.route.code.equals("U9")) {
		    if (!U9RouteRadioButton.isChecked()) {
			stopIter.remove();
		    }
		}
	    }

	    if (visibleTimetable.size() == 0) {
		busTimeContentLayout.setGravity(Gravity.CENTER);
		busStopMessage.setText("No Busses (With the current enabled routes)");
		busStopMessage.setVisibility(View.VISIBLE);
		busTimeList.setVisibility(View.GONE);
	    } else {
		busTimeList.setVisibility(View.VISIBLE);
		busStopMessage.setVisibility(View.GONE);
		TimetableAdapter adapter;
		if ((adapter = (TimetableAdapter) busTimeList.getAdapter()) != null) {
		    adapter.updateTimetable(visibleTimetable);
		} else {
		    adapter = new TimetableAdapter(this, visibleTimetable);
		    busTimeList.setAdapter(adapter);
		}
		busTimeContentLayout.setGravity(Gravity.TOP);
	    }
	}
    }
}
