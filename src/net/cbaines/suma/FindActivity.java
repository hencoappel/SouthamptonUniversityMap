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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public class FindActivity extends OrmLiteBaseActivity<DatabaseHelper> implements Runnable, TextWatcher, OnItemClickListener, LocationListener,
	OnItemLongClickListener {

    final static String TAG = "FindActivity";

    private EditText searchBar;
    private ListView listItems;
    private ProgressBar progBar;
    private LinearLayout findContentLayout;

    private String searchTerm = "";

    private Dao<Building, String> buildingDao;
    private Dao<BusStop, String> busStopDao;
    private Dao<Site, String> siteDao;

    private POIArrayAdapter adapter;
    // private ArrayList<POI> POIsFound = new ArrayList<POI>();

    private boolean dataChanged;

    private GeoPoint userLocation;

    private Thread searchThread;

    ArrayList<POI> getNearestPOIs(int distance) {
	Log.i(TAG, "Getting nearest POI's");
	ArrayList<POI> nearestPOIs = new ArrayList<POI>();
	for (Iterator<Building> buildingDaoIter = buildingDao.iterator(); buildingDaoIter.hasNext();) {
	    POI poi = buildingDaoIter.next();
	    int dist = poi.point.distanceTo(userLocation);
	    if (dist < distance) {
		poi.distTo = dist;
		nearestPOIs.add(poi);
	    }
	}

	for (Iterator<BusStop> busStopDaoIter = busStopDao.iterator(); busStopDaoIter.hasNext();) {
	    POI poi = busStopDaoIter.next();
	    int dist = poi.point.distanceTo(userLocation);
	    if (dist < distance) {
		poi.distTo = dist;
		nearestPOIs.add(poi);
	    }
	}

	Collections.sort(nearestPOIs, new POIDistanceComparator(userLocation, true));

	Log.i(TAG, "Got " + nearestPOIs.size() + " nearest POI's");
	return nearestPOIs;
    }

    // Search thread
    public void run() {
	POIArrayAdapter tempAdaptor;
	GeoPoint thisUserLocation = userLocation;

	Log.i(TAG, "Search thread started");
	String thisSearchTerm = searchTerm;

	ArrayList<POI> foundPOIsArray = null;

	Log.i(TAG, "Search term length " + thisSearchTerm.length() + " userLocation == null " + (thisUserLocation == null));
	if (thisSearchTerm.length() == 0 && thisUserLocation != null) {
	    foundPOIsArray = getNearestPOIs(200);

	}

	if (foundPOIsArray != null && foundPOIsArray.size() != 0) {

	    tempAdaptor = new POIArrayAdapter(this, foundPOIsArray);
	} else {

	    try {

		foundPOIsArray = new ArrayList<POI>();

		if (thisSearchTerm.length() == 0) {
		    for (Building building : buildingDao) {
			foundPOIsArray.add(building);
		    }

		    if (!thisSearchTerm.equals(searchTerm))
			return;

		    for (BusStop busStop : busStopDao) {
			foundPOIsArray.add(busStop);
		    }

		    if (!thisSearchTerm.equals(searchTerm))
			return;

		    for (Site site : siteDao) {
			foundPOIsArray.add(site);
		    }

		} else {

		    QueryBuilder<Building, String> buildingQueryBuilder = buildingDao.queryBuilder();
		    buildingQueryBuilder.where().like(Building.ID_FIELD_NAME, "%" + thisSearchTerm + "%").or()
			    .like(Building.NAME_FIELD_NAME, "%" + thisSearchTerm + "%");
		    PreparedQuery<Building> buildingPreparedQuery = buildingQueryBuilder.prepare();
		    List<Building> buildings = buildingDao.query(buildingPreparedQuery);
		    for (Building building : buildings) {
			foundPOIsArray.add(building);
		    }
		    buildings = null;

		    if (!thisSearchTerm.equals(searchTerm))
			return;

		    if (thisSearchTerm.contains("site")) {
			for (Site site : siteDao) {
			    foundPOIsArray.add(site);
			}
		    } else {
			QueryBuilder<Site, String> siteQueryBuilder = siteDao.queryBuilder();
			siteQueryBuilder.where().like(Site.ID_FIELD_NAME, "%" + thisSearchTerm + "%").or()
				.like(Site.NAME_FIELD_NAME, "%" + thisSearchTerm + "%");
			PreparedQuery<Site> sitePreparedQuery = siteQueryBuilder.prepare();
			List<Site> sites = siteDao.query(sitePreparedQuery);
			for (Site site : sites) {
			    foundPOIsArray.add(site);
			}
			sites = null;
		    }

		    if (!thisSearchTerm.equals(searchTerm))
			return;

		    // if (thisSearchTerm.contains("bus")) {
		    // for (BusStop busStop : busStopDao) {
		    // foundPOIsArray.add(busStop);
		    // }
		    // } else {
		    QueryBuilder<BusStop, String> busStopQueryBuilder = busStopDao.queryBuilder();
		    busStopQueryBuilder.where().like(BusStop.ID_FIELD_NAME, "%" + thisSearchTerm + "%").or()
			    .like(BusStop.DESCRIPTION_FIELD_NAME, "%" + thisSearchTerm + "%");
		    PreparedQuery<BusStop> busStopPreparedQuery = busStopQueryBuilder.prepare();
		    List<BusStop> busStops = busStopDao.query(busStopPreparedQuery);
		    for (BusStop busStop : busStops) {
			foundPOIsArray.add(busStop);
		    }
		    busStops = null;
		    // }

		    if (!thisSearchTerm.equals(searchTerm))
			return;

		    Log.i(TAG, "Found " + foundPOIsArray.size() + " pois");

		    if (thisUserLocation != null) {
			Collections.sort(foundPOIsArray, new POIDistanceComparator(userLocation));
		    } else {
			Collections.sort(foundPOIsArray, new StringDistanceComparator(thisSearchTerm));
		    }

		}

		if (!thisSearchTerm.equals(searchTerm))
		    return;

		tempAdaptor = new POIArrayAdapter(this, foundPOIsArray);

	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	    }
	}

	if (thisSearchTerm.equals(searchTerm)) {
	    Log.i(TAG, "Search terms still equal, starting post");
	    adapter = tempAdaptor;
	    listItems.post(new Runnable() {
		public void run() {
		    listItems.setAdapter(adapter);
		    if (progBar.getVisibility() != View.GONE) {
			progBar.setVisibility(View.GONE);
			findContentLayout.setGravity(Gravity.TOP);
		    }
		}
	    });
	} else {
	    Log.i(TAG, "Search terms no longer equal, exiting");
	}
    }

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.find);

	searchBar = (EditText) findViewById(R.id.searchBar);
	searchBar.addTextChangedListener(this);

	listItems = (ListView) findViewById(R.id.findListItems);
	listItems.setOnItemClickListener(this);
	listItems.setOnItemLongClickListener(this);

	progBar = (ProgressBar) findViewById(R.id.findLoadBar);
	findContentLayout = (LinearLayout) findViewById(R.id.findContentLayout);

	try {
	    buildingDao = getHelper().getBuildingDao();
	    busStopDao = getHelper().getBusStopDao();
	    siteDao = getHelper().getSiteDao();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	// Acquire a reference to the system Location Manager
	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	if (lastKnownLocation != null) {
	    userLocation = Util.locationToGeoPoint(lastKnownLocation);
	} else {
	    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    if (lastKnownLocation != null) {
		userLocation = Util.locationToGeoPoint(lastKnownLocation);

	    }
	}
	// Register the listener with the Location Manager to receive location updates
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, this);

	// SotonBusData.getTimetable("SN120128");

	// Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:1F8fHWmWhqUJGhXUvY57mUof41wMUaeKH7?amount=1X8&label=SUC"));
	// startActivity(browserIntent);
	// 1F8fHWmWhqUJGhXUvY57mUof41wMUaeKH7

	searchThread = new Thread(this);
	searchThread.start();

    }

    public void afterTextChanged(Editable s) {
	searchTerm = s.toString();
	Log.i(TAG, "Text changed " + searchTerm + " starting search thread");
	new Thread(this).start();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	// TODO Auto-generated method stub

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
	// TODO Auto-generated method stub

    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	Log.i(TAG, "OnItemClick pos " + position + " id " + id);

	String poiId = adapter.getItemStringId(position);

	Log.i(TAG, "POI " + poiId + " selected");

	// Intent i = new Intent(FindActivity.this, SouthamptonUniversityMapActivity.class);
	getIntent().putExtra("poi", poiId);
	// startActivity(i);

	setResult(RESULT_OK, getIntent());
	finish();
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

	POI poi = adapter.getPOIItem(position);

	Log.i(TAG, "Long Click Event ID: " + poi.id);

	if (poi.type.equals(POI.BUS_STOP)) {
	    Log.i(TAG, "Its a bus stop");

	    BusStop busStop = (BusStop) poi;

	    Intent i = new Intent(FindActivity.this, BusTimeActivity.class);
	    i.putExtra("busStopID", busStop.id);
	    i.putExtra("busStopName", busStop.description);
	    startActivityForResult(i, 0);
	}

	return false;
    }

    public void finish() {
	getIntent().putExtra("dataChanged", dataChanged);
	// startActivity(i);

	setResult(RESULT_OK, getIntent());

	super.finish();
    }

    public void onLocationChanged(Location location) {
	Log.i(TAG, "Got location update for FindActivity");
	userLocation = Util.locationToGeoPoint(location);
	if (!searchThread.isAlive()) {
	    searchThread = new Thread(this);
	    searchThread.start();
	}
    }

    public void onProviderDisabled(String arg0) {
	// TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
	// TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO Auto-generated method stub

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	Log.i(TAG, "Got activity result");
	if (resultCode == RESULT_OK) {
	    // A contact was picked. Here we will just display it
	    // to the user.

	    boolean dataChangedInBusTimeActivity = false;

	    Bundle bundle = data.getExtras();
	    if (bundle == null) {
		Log.i(TAG, "Bundle is null");
	    } else {
		dataChangedInBusTimeActivity = bundle.getBoolean("dataChanged");
	    }

	    if (dataChangedInBusTimeActivity == true) {
		dataChanged = true;
	    }

	}

    }
}