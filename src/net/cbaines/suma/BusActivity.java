package net.cbaines.suma;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class BusActivity extends OrmLiteBaseActivity<DatabaseHelper> implements Preferences {
    final static String TAG = "BusActivity";

    private TextView U1RouteRadioButton;
    private TextView U1NRouteRadioButton;
    private TextView U2RouteRadioButton;
    private TextView U6RouteRadioButton;
    private TextView U9RouteRadioButton;

    private Handler handler;
    private Runnable refreshData;

    private TextView busIDTextView;

    private ProgressBar progBar;
    private TextView busContentMessage;
    private LinearLayout busActivityContentLayout;

    private Bus bus;
    private BusStop busStop;

    protected Timetable timetable;
    private Timetable visibleTimetable;

    private ListView timetableView;

    private HashSet<GetTimetableStopTask> timetableStopTasks;

    private Context instance;

    private List<BusStop> busStops;

    int num = 15;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.bus_activity);
	instance = this;

	String busID = getIntent().getExtras().getString("busID");
	String busStopID = getIntent().getExtras().getString("busStopID");
	final DatabaseHelper helper = getHelper();

	try {
	    List<Bus> buses = helper.getBusDao().queryForEq(Bus.ID_FIELD_NAME, busID);
	    bus = null;
	    if (buses.size() == 0) {
		Log.e(TAG, "Bus " + busID + " not found!");
	    } else if (buses.size() == 1) {
		bus = buses.get(0);
	    } else if (buses.size() > 1) {
		Log.e(TAG, "Found more than one bus? " + busID);
	    }

	    List<BusStop> busStops = helper.getBusStopDao().queryForEq(BusStop.ID_FIELD_NAME, busStopID);
	    busStop = null;
	    if (busStops.size() == 0) {
		Log.e(TAG, "BusStop " + busStopID + " not found!");
	    } else if (busStops.size() == 1) {
		busStop = busStops.get(0);
	    } else if (busStops.size() > 1) {
		Log.e(TAG, "Found more than one busStop? " + busStopID);
	    }

	    U1RouteRadioButton = (TextView) findViewById(R.id.busActivityU1);
	    U1NRouteRadioButton = (TextView) findViewById(R.id.busActivityU1N);
	    U2RouteRadioButton = (TextView) findViewById(R.id.busActivityU2);
	    U6RouteRadioButton = (TextView) findViewById(R.id.busActivityU6);
	    U9RouteRadioButton = (TextView) findViewById(R.id.busActivityU9);

	    busIDTextView = (TextView) findViewById(R.id.busActivityBusID);

	    progBar = (ProgressBar) findViewById(R.id.busActivityLoadBar);
	    busContentMessage = (TextView) findViewById(R.id.busActivityMessage);
	    busActivityContentLayout = (LinearLayout) findViewById(R.id.busActivityContentLayout);
	    timetableView = (ListView) findViewById(R.id.busActivityTimes);

	    if (bus.id != null) {
		Log.i(TAG, "Bus id is not null (" + bus.id + ") setting busIDTextView");
		busIDTextView.setText(bus.id);
	    } else {
		Log.w(TAG, "Bus id is null?");
		// Might not ever happen
		busIDTextView.setText("Unidentified");
	    }

	    if (bus.route.uniLink) {
		Log.i(TAG, "Bus is uniLink");
		if (bus.route.code.equals("U1")) {
		    U1RouteRadioButton.setVisibility(View.VISIBLE);
		    U1NRouteRadioButton.setVisibility(View.GONE);
		    U2RouteRadioButton.setVisibility(View.GONE);
		    U6RouteRadioButton.setVisibility(View.GONE);
		    U9RouteRadioButton.setVisibility(View.GONE);
		} else if (bus.route.code.equals("U1N")) {
		    U1RouteRadioButton.setVisibility(View.GONE);
		    U1NRouteRadioButton.setVisibility(View.VISIBLE);
		    U2RouteRadioButton.setVisibility(View.GONE);
		    U6RouteRadioButton.setVisibility(View.GONE);
		    U9RouteRadioButton.setVisibility(View.GONE);
		} else if (bus.route.code.equals("U2")) {
		    U1RouteRadioButton.setVisibility(View.GONE);
		    U1NRouteRadioButton.setVisibility(View.GONE);
		    U2RouteRadioButton.setVisibility(View.VISIBLE);
		    U6RouteRadioButton.setVisibility(View.GONE);
		    U9RouteRadioButton.setVisibility(View.GONE);
		} else if (bus.route.code.equals("U6")) {
		    U1RouteRadioButton.setVisibility(View.GONE);
		    U1NRouteRadioButton.setVisibility(View.GONE);
		    U2RouteRadioButton.setVisibility(View.GONE);
		    U6RouteRadioButton.setVisibility(View.VISIBLE);
		    U9RouteRadioButton.setVisibility(View.GONE);
		} else if (bus.route.code.equals("U9")) {
		    U1RouteRadioButton.setVisibility(View.GONE);
		    U1NRouteRadioButton.setVisibility(View.GONE);
		    U2RouteRadioButton.setVisibility(View.GONE);
		    U6RouteRadioButton.setVisibility(View.GONE);
		    U9RouteRadioButton.setVisibility(View.VISIBLE);
		} else {
		    Log.e(TAG, "Route not found " + bus.route.code);
		}
	    }

	} catch (NumberFormatException e) {
	    e.printStackTrace();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	busStops = new ArrayList<BusStop>(num);
	busStops.add(busStop);

	BusRoute route = bus.route;

	for (int i = 0; i < num; i++) {
	    BusStop nextStop = route.moveInRoute(instance, busStops.get(i), bus.direction, 1);

	    if (nextStop != null) {
		busStops.add(nextStop);
	    } else {
		Log.e(TAG, "nextStop is null");
	    }
	}

	refreshData = new Runnable() {
	    @Override
	    public void run() {
		for (int i = 0; i < num; i++) {
		    if (timetable.get(i).timeOfFetch == null || (timetable.get(i).timeOfFetch.getTime() - System.currentTimeMillis()) > 20000) {
			GetTimetableStopTask timetableStopTask = new GetTimetableStopTask();
			BusStop[] str = { busStops.get(i) };
			timetableStopTask.execute(str);
			handler.postDelayed(refreshData, 20000);
		    }
		}
	    }
	};
    }

    public void onResume() {
	super.onResume();

	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	if (sharedPrefs.getBoolean(UNI_LINK_BUS_TIMES, UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT)
		|| sharedPrefs.getBoolean(NON_UNI_LINK_BUS_TIMES, NON_UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT)) {
	    Log.i(TAG, "Live Times enabled");
	    timetable = (Timetable) getLastNonConfigurationInstance();

	    handler = new Handler();

	    if (timetable == null) {
		Log.i(TAG, "No Previous timetable");
		timetable = new Timetable();
		for (int i = 0; i < num; i++) {
		    timetable.add(new Stop(bus, busStops.get(i), null, null, false));
		}
	    } else {
		Log.i(TAG, "Displaying previous timetable");
		displayTimetable(timetable);
	    }
	    handler.post(refreshData);

	} else {
	    Log.i(TAG, "Live Times Disabled");
	    progBar.setVisibility(View.GONE);
	    busContentMessage.setText("Live bus times disabled");
	    busContentMessage.setVisibility(View.VISIBLE);
	}

    }

    public void onPause() {
	if (handler != null) { // BusTimes are enabled
	    handler.removeCallbacks(refreshData);
	    if (timetableStopTasks != null) { // Could happen if the handler has not created the timetableTask yet
		for (GetTimetableStopTask task : timetableStopTasks) {
		    if (task != null) {
			task.cancel(true);
		    }
		}
	    }
	    Log.i(TAG, "Stoping refreshing timetable data");
	}

	super.onPause();
    }

    private class GetTimetableStopTask extends AsyncTask<BusStop, Integer, Timetable> {
	String errorMessage;

	protected void onPreExecute() {
	    progBar.setVisibility(View.VISIBLE);
	}

	protected Timetable doInBackground(BusStop... busStop) {
	    Timetable newTimetable = timetable;
	    try {
		Log.i(TAG, "Fetching stop for busStop " + busStops.indexOf(busStop[0]));
		Stop stop = DataManager.getStop(instance, bus, busStop[0]);
		if (stop == null) {
		    stop = new Stop(bus, busStop[0], null, null, false);
		}
		Log.i(TAG, "Finished fetching stop for busStop " + busStops.indexOf(busStop[0]));
		newTimetable.set(busStops.indexOf(busStop[0]), stop);
	    } catch (SQLException e) {
		errorMessage = "Error message regarding SQL?";
		e.printStackTrace();
		newTimetable = null;
	    } catch (ClientProtocolException e) {
		errorMessage = "ClientProtocolException!?!";
		e.printStackTrace();
		newTimetable = null;
	    } catch (IOException e) {
		errorMessage = "Error fetching bus times from server, are you connected to the internet?";
		e.printStackTrace();
		newTimetable = null;
	    } catch (JSONException e) {
		errorMessage = "Error parsing bus times";
		e.printStackTrace();
		newTimetable = null;
	    }
	    return newTimetable;
	}

	protected void onPostExecute(Timetable newTimetable) {
	    // Log.i(TAG, "Got timetable");
	    if (newTimetable == null) {
		Log.i(TAG, "Its null");

		progBar.setVisibility(View.GONE);
		busContentMessage.setText(errorMessage);
		busContentMessage.setVisibility(View.VISIBLE);
	    } else {
		progBar.setVisibility(View.GONE);
		timetable = newTimetable;
		displayTimetable(timetable);
	    }
	}
    }

    private void displayTimetable(Timetable timetable) {
	visibleTimetable = (Timetable) timetable.clone();

	Log.i(TAG, "Displaying timetable, it contains " + visibleTimetable.size() + " stops");

	if (timetable.size() == 0) {
	    busContentMessage.setText("No Busses");
	    busContentMessage.setVisibility(View.VISIBLE);
	    busActivityContentLayout.setGravity(Gravity.CENTER);
	} else {
	    if (visibleTimetable.size() == 0) {
		busActivityContentLayout.setGravity(Gravity.CENTER);
		busContentMessage.setText("No Busses (With the current enabled routes)");
		busContentMessage.setVisibility(View.VISIBLE);
		timetableView.setVisibility(View.GONE);
	    } else {
		timetableView.setVisibility(View.VISIBLE);
		busContentMessage.setVisibility(View.GONE);
		BusSpecificTimetableAdapter adapter;
		if ((adapter = (BusSpecificTimetableAdapter) timetableView.getAdapter()) != null) {
		    adapter.updateTimetable(visibleTimetable);
		} else {
		    adapter = new BusSpecificTimetableAdapter(this, visibleTimetable);
		    timetableView.setAdapter(adapter);
		}
		busActivityContentLayout.setGravity(Gravity.TOP);
	    }
	}
    }
}
