package net.cbaines.suma;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

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

    private CheckBox U1RouteRadioButton;
    private CheckBox U1NRouteRadioButton;
    private CheckBox U2RouteRadioButton;
    private CheckBox U6RouteRadioButton;
    private CheckBox U9RouteRadioButton;

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

	    U1RouteRadioButton = (CheckBox) findViewById(R.id.radio_u1);
	    U1NRouteRadioButton = (CheckBox) findViewById(R.id.radio_u1n);
	    U2RouteRadioButton = (CheckBox) findViewById(R.id.radio_u2);
	    U6RouteRadioButton = (CheckBox) findViewById(R.id.radio_u6);
	    U9RouteRadioButton = (CheckBox) findViewById(R.id.radio_u9);

	    busIDTextView = (TextView) findViewById(R.id.busActivityBusID);

	    progBar = (ProgressBar) findViewById(R.id.busActivityLoadBar);
	    busContentMessage = (TextView) findViewById(R.id.busActivityMessage);
	    busActivityContentLayout = (LinearLayout) findViewById(R.id.busActivityContentLayout);

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
    }

    public void onResume() {
	super.onResume();

	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	if (sharedPrefs.getBoolean(UNI_LINK_BUS_TIMES, UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT)
		|| sharedPrefs.getBoolean(NON_UNI_LINK_BUS_TIMES, NON_UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT)) {
	    Log.i(TAG, "Live Times enabled");
	    timetable = (Timetable) getLastNonConfigurationInstance();

	    refreshData = new Runnable() {
		@Override
		public void run() {
		    GetTimetableStopTask timetableStopTask = new GetTimetableStopTask();
		    timetableStopTask.execute(busStopID);
		    handler.postDelayed(refreshData, 20000);
		}
	    };

	    handler = new Handler();

	    if (timetable == null) {
		Log.i(TAG, "No Previous timetable");
		handler.post(refreshData);
	    } else {
		Log.i(TAG, "Displaying previous timetable");
		displayTimetable(timetable);
		if (System.currentTimeMillis() - timetable.fetchTime.getTime() > 20000) {
		    handler.post(refreshData);
		}
	    }

	} else {
	    Log.i(TAG, "Live Times Disabled");
	    progBar.setVisibility(View.GONE);
	    busContentMessage.setText("Live bus times disabled");
	    busContentMessage.setVisibility(View.VISIBLE);
	}

    }

    private class GetTimetableStopTask extends AsyncTask<String, Integer, Timetable> {
	String errorMessage;

	protected void onPreExecute() {
	    progBar.setVisibility(View.VISIBLE);
	}

	protected Timetable doInBackground(String... busStopID) {
	    Timetable newTimetable = null;
	    try {
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(instance);

		newTimetable = DataManager.getTimetable(instance, bus, busStop, 10);
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
	    } catch (Exception e) {
		Log.e(TAG, e.getMessage(), e.getCause());
	    }
	    return newTimetable;
	}

	protected void onPostExecute(Timetable newTimetable) {
	    Log.i(TAG, "Got timetable");
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

	Log.i(TAG, "It contains " + visibleTimetable.size() + " stops");

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
