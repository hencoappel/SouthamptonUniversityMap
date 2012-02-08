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
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class SouthamptonUniversityMapActivity extends OrmLiteBaseActivity<DatabaseHelper> implements MapViewConstants, Runnable, RouteColorConstants,
	OnChildClickListener, OnItemClickListener, OnItemLongClickListener, OnSharedPreferenceChangeListener, Preferences {

    private boolean useBundledDatabase = false;

    private MapView mapView;
    private MapController mapController;
    private ResourceProxy mResourceProxy;

    private long startTime;

    static final int VIEW_DIALOG_ID = 0;
    static final int FAVOURITE_DIALOG_ID = 1;

    private HashMap<String, Overlay> overlays = new HashMap<String, Overlay>();
    private HashMap<String, Overlay> pastOverlays;

    // Overlays

    // Scale Bar Overlay
    private static final String SCALE_BAR_OVERLAY = "scaleBarOverlay";
    private ScaleBarOverlay scaleBarOverlay;
    private static final boolean SCALE_BAR_OVERLAY_ENABLED_BY_DEFAULT = true;

    // My Location Overlay
    private static final String MY_LOCATION_OVERLAY = "myLocationOverlay";
    private MyLocationOverlay myLocationOverlay;
    private static final boolean MY_LOCATION_OVERLAY_ENABLED_BY_DEFAULT = true;

    // Residential Building Overlay
    private static final String RESIDENTIAL_BUILDINGS = "residentialBuildingOverlay";
    private BuildingNumOverlay residentialBuildingOverlay;
    private static final boolean RESIDENTIAL_BUILDING_OVERLAY_ENABLED_BY_DEFAULT = true;

    // Non-Residential Building Overlay
    private static final String NON_RESIDENTIAL_BUILDINGS = "nonResidentialBuildingOverlay";
    private BuildingNumOverlay nonResidentialBuildingOverlay;
    private static final boolean NON_RESIDENTIAL_BUILDING_OVERLAY_ENABLED_BY_DEFAULT = true;

    // Uni-Link Bus Stop Overlay
    private static final String UNI_LINK_BUS_STOPS = "uniLinkBusStopOverlay";
    private BusStopOverlay uniLinkBusStopOverlay;
    private static final boolean UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT = true;

    // Uni-Link Bus Stop Overlay
    private static final String NON_UNI_LINK_BUS_STOPS = "nonUniLinkBusStopOverlay";
    private BusStopOverlay nonUniLinkBusStopOverlay;
    private static final boolean NON_UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT = true;

    // Site Overlays
    private HashMap<Site, PathOverlay> siteOverlays = new HashMap<Site, PathOverlay>(21);

    // Route Overlays
    private HashMap<BusRoute, PathOverlay> routeOverlays = new HashMap<BusRoute, PathOverlay>(5);

    // View
    private static final String RESIDENTIAL_BUILDING_OVERLAY = "Buildings:Residential";
    private static final String NON_RESIDENTIAL_BUILDING_OVERLAY = "Buildings:Non-Residential";
    private static final boolean SITE_OVERLAY_ENABLED_BY_DEFAULT = false;
    private static final boolean ROUTE_OVERLAY_ENABLED_BY_DEFAULT = true;

    private static final int UNI_LINK_BUS_STOP_OVERLAY_RANK = 4;

    private POIDialog favDialog;

    private SouthamptonUniversityMapActivity instance;

    private static final String TAG = "SUM";

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
	startTime = System.currentTimeMillis();

	super.onCreate(savedInstanceState);

	instance = this;

	Thread databaseThread = new Thread(this); // Start the database thread
	databaseThread.start();

	setContentView(R.layout.main);

	Log.i(TAG, "Finished setting content view " + (System.currentTimeMillis() - startTime));

	mapView = (MapView) this.findViewById(R.id.mapview);
	mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
	mapView.setBuiltInZoomControls(true);
	mapView.setMultiTouchControls(true);

	pastOverlays = (HashMap<String, Overlay>) getLastNonConfigurationInstance();

	// SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); This code in the following constructor causes problems in
	// some emulators, disable sensors to fix.
	Log.i(TAG, "Starting creating myLocationOverlay");
	myLocationOverlay = new MyLocationOverlay(instance, mapView);
	Log.i(TAG, "Finished creating myLocationOverlay");

	while (databaseThread.isAlive()) {
	    Thread.yield();
	}

	new Thread(new Runnable() {
	    public void run() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
		    showOverlays();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	}).start();

	Log.i(TAG, "Started loading thread " + (System.currentTimeMillis() - startTime));

	mapController = mapView.getController();
	mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

	GeoPoint userLocation = myLocationOverlay.getMyLocation();
	if (userLocation == null) {
	    userLocation = new GeoPoint(50935551, -1393488); // ECS
	}

	mapController.setZoom(15);
	mapController.setCenter(userLocation);

	Log.i(TAG, "Finished onCreate " + (System.currentTimeMillis() - startTime));
    }

    public void onResume() {
	super.onResume();
	Log.i(TAG, "OnResume");
	if (myLocationOverlay != null) {
	    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    final SharedPreferences activityPrefs = getPreferences(0);

	    if (activityPrefs.getBoolean("Other:Compass", false)) {
		myLocationOverlay.enableCompass();
	    } else {
		myLocationOverlay.disableCompass();
	    }

	    if (activityPrefs.getBoolean("Other:My Location", false) && sharedPrefs.getBoolean(GPS_ENABLED, false)) {
		myLocationOverlay.enableMyLocation();
	    } else {
		myLocationOverlay.disableMyLocation();
	    }

	    sharedPrefs.registerOnSharedPreferenceChangeListener(this);
	    activityPrefs.registerOnSharedPreferenceChangeListener(this);
	}
    }

    public void onPause() {
	super.onResume();
	Log.i(TAG, "OnPause");
	if (myLocationOverlay != null) {
	    myLocationOverlay.disableMyLocation();
	    myLocationOverlay.disableCompass();
	}
    }

    public void finish() {
	super.finish();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
	return overlays;
    }

    public void run() {
	Log.i(TAG, "Begining loading database " + (System.currentTimeMillis() - startTime));

	DatabaseHelper helper = getHelper();
	Log.i(TAG, "Got the helper at " + (System.currentTimeMillis() - startTime));

	boolean dbExist = helper.checkDataBase();
	Log.i(TAG, "Finished checking the database at " + (System.currentTimeMillis() - startTime));

	if (dbExist) {
	    // do nothing - database already exist
	} else {

	    if (useBundledDatabase) {
		try {
		    // By calling this method and empty database will be created into the default system path
		    // of your application so we are gonna be able to overwrite that database with our database.
		    Log.i(TAG, "GetReadableDatabase");
		    helper.getWritableDatabase().close();

		    helper.copyDataBase();
		    Log.i(TAG, "Out of copy database");
		} catch (IOException ioe) {
		    throw new Error("Unable to create database");
		}
	    } else {
		Thread buildingThread = null;
		Thread busStopThread = null;
		Thread siteThread = null;

		Log.i(TAG, "Begining loading databases " + (System.currentTimeMillis() - startTime));
		try {
		    Dao<Building, String> buildingDao;

		    buildingDao = helper.getBuildingDao();

		    long buildingCount = buildingDao.countOf();
		    Log.i(TAG, "Building count " + buildingCount);
		    if (buildingCount < 260) {
			buildingThread = new Thread(new Runnable() {
			    public void run() {
				try {
				    DataManager.loadBuildings(instance);
				    Log.i(TAG, "Loaded building database " + (System.currentTimeMillis() - startTime));
				} catch (SQLException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
			    }
			});

			buildingThread.start();
		    }

		    Dao<BusStop, String> busStopDao = helper.getBusStopDao();
		    Dao<BusRoute, Integer> busRouteDao = helper.getBusRouteDao();
		    Dao<RouteStops, Integer> routeStopsDao = helper.getRouteStopsDao();

		    long busStopCount = busStopDao.countOf();
		    long busRouteCount = busRouteDao.countOf();
		    long routeStopsCount = routeStopsDao.countOf();

		    Log.i(TAG, "BusStop count " + busStopCount);
		    Log.i(TAG, "BusRoute count " + busRouteCount);
		    Log.i(TAG, "RouteStops count " + routeStopsCount);
		    if (busStopCount < 217 || busRouteCount < 5 || routeStopsCount < 327) {
			busStopThread = new Thread(new Runnable() {
			    public void run() {
				try {
				    DataManager.loadBusData(instance, true);
				    Log.i(TAG, "Loaded bus stop database " + (System.currentTimeMillis() - startTime));
				} catch (SQLException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
			    }
			});

			busStopThread.start();
		    }

		    Dao<Site, String> siteDao = helper.getSiteDao();

		    long siteCount = siteDao.countOf();
		    Log.i(TAG, "Sites count " + siteCount);
		    if (siteCount < 21) {
			siteThread = new Thread(new Runnable() {
			    public void run() {
				try {
				    DataManager.loadSiteData(instance);
				    Log.i(TAG, "Loaded site database " + (System.currentTimeMillis() - startTime));
				} catch (SQLException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
			    }
			});

			siteThread.start();
		    }

		    while (true) {
			if ((buildingThread == null || !buildingThread.isAlive()) && (busStopThread == null || !busStopThread.isAlive())
				&& (siteThread == null || !siteThread.isAlive()))
			    break;

			Thread.yield();
		    }

		    Log.i(TAG, "Finished loading databases " + (System.currentTimeMillis() - startTime));

		} catch (SQLException e1) {
		    e1.printStackTrace();
		}
	    }

	}

	Log.i(TAG, "Begining setting up the static values " + (System.currentTimeMillis() - startTime));

	Log.i(TAG, "Finished the database thread " + (System.currentTimeMillis() - startTime));
    }

    private void showOverlays() throws SQLException {
	Log.i(TAG, "Began showing overlays at " + (System.currentTimeMillis() - startTime));

	if (pastOverlays != null) {
	    Log.i(TAG, "Able to recover some/all of the overlays from a previous activity");
	} else {
	    Log.i(TAG, "Unable to recover overlays");
	}

	final SharedPreferences activityPrefs = getPreferences(0);

	showUtilityOverlays();

	showBusStopOverlays();

	if (activityPrefs.getBoolean("Buildings:Residential", true) || activityPrefs.getBoolean("Buildings:Non-Residential", true)) {
	    // The argument currently dosent matter for this method.
	    showBuildingOverlay(true);

	}

	Log.i(TAG, "Begining to show the route overlays at " + (System.currentTimeMillis() - startTime));
	for (BusRoute busRoute : getHelper().getBusRouteDao()) {
	    Log.v(TAG, "Looking at showing " + busRoute.code + " route overlay");
	    if (activityPrefs.getBoolean("Bus Routes:" + busRoute.code, ROUTE_OVERLAY_ENABLED_BY_DEFAULT)) {
		showRouteOverlay(busRoute);
	    }
	}
	Log.i(TAG, "Finished loading routes " + (System.currentTimeMillis() - startTime));

	Log.i(TAG, "Begining to show the site overlays at " + (System.currentTimeMillis() - startTime));
	try {
	    for (Site site : getHelper().getSiteDao()) {
		Log.v(TAG, "Looking at  showing " + site.name + " site overlay");
		if (activityPrefs.getBoolean("Sites:" + site.name, SITE_OVERLAY_ENABLED_BY_DEFAULT)) {
		    showSiteOverlay(site);
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	Log.i(TAG, "Finished showing the site overlays " + (System.currentTimeMillis() - startTime));

	Log.i(TAG, "Finished showing all the overlays " + (System.currentTimeMillis() - startTime));
    }

    private void showUtilityOverlays() {
	new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining showing the utility overlays " + (System.currentTimeMillis() - startTime));

		final SharedPreferences activityPrefs = getPreferences(0);
		final OverlayRankComparator comparator = new OverlayRankComparator(getPreferences(0));

		if (scaleBarOverlay != null) {
		    Log.v(TAG, "ScaleBarOverlay is already created");
		} else {
		    if (pastOverlays != null && (scaleBarOverlay = (ScaleBarOverlay) pastOverlays.get("Other:Scale Bar")) != null) {
			Log.i(TAG, "Finished restoring utility overlays " + (System.currentTimeMillis() - startTime));
		    } else {
			scaleBarOverlay = new ScaleBarOverlay(instance);
		    }

		    overlays.put("Other:Scale Bar", scaleBarOverlay);

		    synchronized (mapView.getOverlays()) {
			mapView.getOverlays().add(scaleBarOverlay);
			mapView.getOverlays().add(myLocationOverlay);
			Collections.sort(mapView.getOverlays(), comparator);
		    }

		}

		scaleBarOverlay.setEnabled(activityPrefs.getBoolean("Other:Scale Bar", SCALE_BAR_OVERLAY_ENABLED_BY_DEFAULT));

		mapView.postInvalidate();

		Log.i(TAG, "Finished showing utility overlays " + (System.currentTimeMillis() - startTime));
	    }
	}).start();
    }

    private void showRouteOverlay(final BusRoute route) {
	new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining showing route " + route.code + " overlay at " + (System.currentTimeMillis() - startTime));

		final SharedPreferences activityPrefs = getPreferences(0);
		final OverlayRankComparator comparator = new OverlayRankComparator(getPreferences(0));

		PathOverlay routeOverlay;
		if ((routeOverlay = routeOverlays.get(route)) != null) {
		    Log.v(TAG, route.code + " route overlay already existed");
		} else {
		    if (pastOverlays != null && (routeOverlay = (PathOverlay) pastOverlays.get("Bus Routes:" + route.code)) != null) {
			Log.v(TAG, "Restored " + route.code + " route overlay");
			if (route.code.equals("U1")) {
			    PathOverlay routeOverlayU1E = (PathOverlay) pastOverlays.get("Bus Routes:U1E");
			    overlays.put("Bus Routes:U1E", routeOverlayU1E);
			}
		    } else {
			InputStream resource = null;
			int colour = 0;
			if (route.code.equals("U1")) {
			    resource = getResources().openRawResource(R.raw.u1);
			    colour = U1;

			    // TODO Is this a route like U1N or, something else, this hack works somewhat for now?
			    PathOverlay routeOverlayU1E = DataManager.getRoutePath(getResources().openRawResource(R.raw.u1e), colour, mResourceProxy);
			    routeOverlayU1E.getPaint().setAntiAlias(true);
			    routeOverlayU1E.getPaint().setAlpha(145);
			    routeOverlayU1E.getPaint().setStrokeWidth(12);
			    routeOverlayU1E.getPaint().setPathEffect(new DashPathEffect(new float[] { 20, 16 }, 0));
			    routeOverlayU1E.setEnabled(activityPrefs.getBoolean("Bus Routes:" + route.code, true));

			    routeOverlays.put(new BusRoute(1000, "U1E", "U1e Route Label"), routeOverlayU1E);
			    overlays.put("Bus Routes:" + route.code + "E", routeOverlayU1E);
			} else if (route.code.equals("U1N")) {
			    resource = getResources().openRawResource(R.raw.u1n);
			    colour = U1N;
			} else if (route.code.equals("U2")) {
			    resource = getResources().openRawResource(R.raw.u2);
			    colour = U2;
			} else if (route.code.equals("U6")) {
			    resource = getResources().openRawResource(R.raw.u6);
			    colour = U6;
			} else if (route.code.equals("U9")) {
			    resource = getResources().openRawResource(R.raw.u9);
			    colour = U9;
			} else {
			    Log.w(TAG, "Wierd route " + route);
			}

			routeOverlay = DataManager.getRoutePath(resource, colour, mResourceProxy);

			Log.v(TAG, "Path overlay has " + routeOverlay.getNumberOfPoints() + " points");

			routeOverlay.getPaint().setAntiAlias(true);
			routeOverlay.getPaint().setAlpha(145);
			routeOverlay.getPaint().setStrokeWidth(12);
		    }

		    routeOverlays.put(route, routeOverlay);
		    overlays.put("Bus Routes:" + route.code, routeOverlay);

		    synchronized (mapView.getOverlays()) {
			mapView.getOverlays().add(routeOverlay);
			Collections.sort(mapView.getOverlays(), comparator);
		    }

		}

		routeOverlay.setEnabled(activityPrefs.getBoolean("Bus Routes:" + route.code, ROUTE_OVERLAY_ENABLED_BY_DEFAULT));
		if (route.code.equals("U1")) {
		    overlays.get("Bus Routes:U1E").setEnabled(activityPrefs.getBoolean("Bus Routes:U1", ROUTE_OVERLAY_ENABLED_BY_DEFAULT));
		}

		mapView.postInvalidate();

		Log.i(TAG, "Finished showing route " + route.code + " overlay at " + (System.currentTimeMillis() - startTime));
	    }
	}).start();
    }

    private void showSiteOverlay(final Site site) {

	new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining showing site " + site.name + " overlay at " + (System.currentTimeMillis() - startTime));

		final SharedPreferences activityPrefs = getPreferences(0);
		final OverlayRankComparator comparator = new OverlayRankComparator(getPreferences(0));

		PathOverlay siteOverlay;
		if ((siteOverlay = siteOverlays.get(site)) != null) {

		} else {
		    if (pastOverlays != null && (siteOverlay = (PathOverlay) pastOverlays.get("Site Outlines:" + site.name)) != null) {
			Log.i(TAG, "Restored " + site.name + " site overlay");
		    } else {

			siteOverlay = new PathOverlay(Color.BLUE, instance);
			Paint paint = siteOverlay.getPaint();
			paint.setAntiAlias(true);
			paint.setStrokeWidth(1.5f);
			for (int i = 0; i < site.outline.points.length; i++) {
			    siteOverlay.addPoint(site.outline.points[i]);
			}
			siteOverlay.addPoint(site.outline.points[0]);

		    }

		    siteOverlays.put(site, siteOverlay);
		    overlays.put("Site Outlines:" + site.name, siteOverlay);

		    Log.v(TAG, "Applyed the site overlay, now sorting them");

		    synchronized (mapView.getOverlays()) {
			mapView.getOverlays().add(siteOverlay);
			Collections.sort(mapView.getOverlays(), comparator);
		    }
		}

		siteOverlay.setEnabled(activityPrefs.getBoolean("Site Outlines:" + site.name, SITE_OVERLAY_ENABLED_BY_DEFAULT));

		mapView.postInvalidate();

		Log.i(TAG, "Finished showing site " + site.name + " overlay at " + (System.currentTimeMillis() - startTime));
	    }
	}).start();
    }

    private void showBuildingOverlay(boolean residential) {
	new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining showing building overlays at " + (System.currentTimeMillis() - startTime));

		final SharedPreferences activityPrefs = getPreferences(0);
		final OverlayRankComparator comparator = new OverlayRankComparator(getPreferences(0));

		if (residentialBuildingOverlay != null) {

		} else {
		    if (pastOverlays != null && (residentialBuildingOverlay = (BuildingNumOverlay) pastOverlays.get("Buildings:Residential")) != null) {
			nonResidentialBuildingOverlay = (BuildingNumOverlay) pastOverlays.get("Buildings:Non-Residential");

			Log.i(TAG, "Restored building overlays");
		    } else {
			try {

			    Log.v(TAG, "Begining the creation of the building overlays");

			    ArrayList<Building> residentialBuildings = new ArrayList<Building>();
			    ArrayList<Building> nonResidentialBuildings = new ArrayList<Building>();

			    Dao<Building, String> buildingDao;

			    buildingDao = getHelper().getBuildingDao();

			    for (Building building : buildingDao) {
				// Log.v(TAG, "Looking at building " + building.id);
				if (building.residential == true) {
				    // Log.v(TAG, "Its residential");
				    if (building.favourite) {
					// Log.v(TAG, "Its residential and a favourite");
					residentialBuildings.add(building);
				    } else {
					// Log.v(TAG, "Its residential and not a favourite");
					residentialBuildings.add(0, building);
				    }
				} else {
				    if (building.favourite) {
					// Log.v(TAG, "Its not residential and a favourite");
					nonResidentialBuildings.add(building);
				    } else {
					// Log.v(TAG, "Its not residential and not a favourite");
					nonResidentialBuildings.add(0, building);
				    }
				}
			    }

			    residentialBuildingOverlay = new BuildingNumOverlay(instance, residentialBuildings);
			    nonResidentialBuildingOverlay = new BuildingNumOverlay(instance, nonResidentialBuildings);

			    Log.v(TAG, "Applyed the site overlay, now sorting them");

			} catch (SQLException e) {
			    e.printStackTrace();
			}
		    }

		    overlays.put("Buildings:Residential", residentialBuildingOverlay);
		    overlays.put("Buildings:Non-Residential", nonResidentialBuildingOverlay);

		    synchronized (mapView.getOverlays()) {
			mapView.getOverlays().add(residentialBuildingOverlay);
			mapView.getOverlays().add(nonResidentialBuildingOverlay);
			Collections.sort(mapView.getOverlays(), comparator);
		    }
		}

		residentialBuildingOverlay.setEnabled(activityPrefs.getBoolean("Buildings:Residential", RESIDENTIAL_BUILDING_OVERLAY_ENABLED_BY_DEFAULT));
		nonResidentialBuildingOverlay.setEnabled(activityPrefs.getBoolean("Buildings:Non-Residential",
			NON_RESIDENTIAL_BUILDING_OVERLAY_ENABLED_BY_DEFAULT));

		mapView.postInvalidate();

		Log.i(TAG, "Finished showing building overlays at " + (System.currentTimeMillis() - startTime));
	    }
	}).start();
    }

    private void showBusStopOverlays() {
	new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining showing bus stop overlays at " + (System.currentTimeMillis() - startTime));

		final SharedPreferences activityPrefs = getPreferences(0);
		final OverlayRankComparator comparator = new OverlayRankComparator(getPreferences(0));

		if (uniLinkBusStopOverlay != null) {

		} else {
		    if (pastOverlays != null && (uniLinkBusStopOverlay = (BusStopOverlay) pastOverlays.get("UniLinkBusStops")) != null) {
			Log.i(TAG, "Restored bus stop overlays");
		    } else {
			try {
			    List<BusStop> busStops;
			    Log.v(TAG, "Begin fetching BusStops at " + (System.currentTimeMillis() - startTime));
			    if (activityPrefs.getBoolean(UNI_LINK_BUS_STOPS, UNI_LINK_BUS_STOPS_ENABLED_BY_DEFAULT)) {
				busStops = getHelper().getBusStopDao().queryForAll();
			    } else {
				busStops = getHelper().getBusStopDao().queryForEq(BusStop.UNI_LINK_FIELD_NAME, true);
			    }
			    Log.v(TAG, "Finished fetching BusStops at " + (System.currentTimeMillis() - startTime));

			    uniLinkBusStopOverlay = new BusStopOverlay(instance, busStops);
			} catch (SQLException e) {
			    e.printStackTrace();
			}
		    }

		    overlays.put("BusStops", uniLinkBusStopOverlay);

		    Log.v(TAG, "Applyed the site overlay, now sorting them");

		    synchronized (mapView.getOverlays()) {
			mapView.getOverlays().add(uniLinkBusStopOverlay);
			Collections.sort(mapView.getOverlays(), comparator);
		    }
		}

		uniLinkBusStopOverlay.setRoutes(0, activityPrefs.getBoolean("Bus Stops:U1", UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT));
		uniLinkBusStopOverlay.setRoutes(1, activityPrefs.getBoolean("Bus Stops:U1N", UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT));
		uniLinkBusStopOverlay.setRoutes(2, activityPrefs.getBoolean("Bus Stops:U2", UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT));
		uniLinkBusStopOverlay.setRoutes(3, activityPrefs.getBoolean("Bus Stops:U6", UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT));
		uniLinkBusStopOverlay.setRoutes(4, activityPrefs.getBoolean("Bus Stops:U9", UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT));

		mapView.postInvalidate();

		Log.i(TAG, "Finished showing bus stop overlays at " + (System.currentTimeMillis() - startTime));
	    }
	}).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.map_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle item selection
	switch (item.getItemId()) {
	case R.id.menu_find:
	    Intent i = new Intent(SouthamptonUniversityMapActivity.this, FindActivity.class);
	    startActivityForResult(i, 0);
	    return true;
	case R.id.menu_preferences:
	    Intent settingsActivity = new Intent(getBaseContext(), PreferencesActivity.class);
	    startActivity(settingsActivity);
	    return true;
	case R.id.menu_find_my_location:
	    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    if (sharedPrefs.getBoolean("GPSEnabled", false)) {
		GeoPoint userLocation = myLocationOverlay.getMyLocation();
		if (userLocation != null) {
		    Log.i(TAG, "Found user location, scrolling to " + userLocation);
		    mapController.animateTo(userLocation);
		    myLocationOverlay.enableFollowLocation();
		}
	    } else {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
			    Editor editor = sharedPrefs.edit();
			    editor.putBoolean("GPSEnabled", true);
			    editor.commit();
			    break;

			case DialogInterface.BUTTON_NEGATIVE:
			    // No button clicked
			    break;
			}
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("GPS is not enabled, do you wish to enable it?").setPositiveButton("Yes", dialogClickListener)
			.setNegativeButton("No", dialogClickListener).show();
	    }

	    return true;
	case R.id.menu_view:
	    Log.i(TAG, "Showing view dialog");
	    showDialog(VIEW_DIALOG_ID);
	    return false;
	case R.id.menu_favourites:
	    Log.i(TAG, "Showing favourite dialog");

	    showDialog(FAVOURITE_DIALOG_ID);
	    if (favDialog == null) {
		Log.e(TAG, "Very wierd, just tried to launch the favourite's dialog, but its null?");
		return false;
	    }

	    refreshFavouriteDialog();

	    return false;
	case R.id.menu_about:
	    Intent aboutIntent = new Intent(SouthamptonUniversityMapActivity.this, AboutActivity.class);
	    startActivityForResult(aboutIntent, 0);
	    return true;
	default:
	    Log.e(TAG, "No known menu option selected");
	    return super.onOptionsItemSelected(item);
	}
    }

    private void refreshFavouriteDialog() {
	ArrayList<POI> newFavouriteItems = new ArrayList<POI>();

	try {
	    Dao<Building, String> buildingDao = getHelper().getBuildingDao();
	    Dao<BusStop, String> busStopDao = getHelper().getBusStopDao();

	    newFavouriteItems.addAll(buildingDao.queryForEq(POI.FAVOURITE_FIELD_NAME, true));
	    newFavouriteItems.addAll(busStopDao.queryForEq(POI.FAVOURITE_FIELD_NAME, true));
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	Log.i(TAG, "There are " + newFavouriteItems.size() + " favourites");
	if (newFavouriteItems.size() == 0) {
	    Log.i(TAG, "Favourite dialog has no favourites, displaying message");
	    favDialog.setMessage(getResources().getString(R.string.favourites_dialog_message));
	    favDialog.setItems(null);
	} else {
	    favDialog.setMessage("");
	    favDialog.setItems(newFavouriteItems);
	}
    }

    @Override
    public boolean onSearchRequested() {
	Intent i = new Intent(SouthamptonUniversityMapActivity.this, FindActivity.class);
	startActivityForResult(i, 0);
	return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	Log.i(TAG, "Got activity result");
	if (resultCode == RESULT_OK) {

	    POI poi = null;
	    Bundle bundle = data.getExtras();
	    if (bundle == null) {
		Log.i(TAG, "Bundle is null");
	    } else {
		String poiId = (String) bundle.get("poi");
		if (poiId != null) {
		    Log.i(TAG, "Got id " + poiId);
		    try {
			poi = getHelper().getBuildingDao().queryForId(poiId);
			if (poi == null) {
			    poi = getHelper().getBusStopDao().queryForId(poiId);
			}
		    } catch (SQLException e) {
			e.printStackTrace();
		    }

		    if (poi == null) {
			Log.e(TAG, "Could not find poi " + poiId + " in onActivityResult");
		    } else {
			if (myLocationOverlay != null) {
			    // It could be null if it has not been enabled
			    myLocationOverlay.disableFollowLocation();
			}
			mapController.setZoom(20);
			mapController.setCenter(poi.point);

		    }
		} else {
		    Log.i(TAG, "Got null poi id");

		    // mapController.setZoom(15);
		    // mapController.setCenter(new GeoPoint(50935551, -1393488));
		}

		// This handles the possible change in favourite state caused by the user within the BusTimeActivity
		try {
		    String busStopID = bundle.getString("busStopChanged");
		    if (busStopID != null && busStopID.length() != 0) {
			Log.v(TAG, "Got a busStop id back from the BusTimeActivity " + busStopID);
			BusStop busStop = getHelper().getBusStopDao().queryForId(busStopID);

			uniLinkBusStopOverlay.refresh(busStop); // This does not invalidate the map, but it seems to make the changes appear
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}

		if (favDialog != null) {
		    refreshFavouriteDialog();
		}
	    }
	}

    }

    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

	mapView.post(new Runnable() {
	    public void run() {
		// updateEnabledOverlays(); TODO Fix whatever this did?
		mapView.invalidate();
	    }
	});

	return true;
    }

    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case VIEW_DIALOG_ID:
	    ViewDialog viewDialog = new ViewDialog(instance);
	    viewDialog.setOnItemClickListener(this);
	    return viewDialog;
	case FAVOURITE_DIALOG_ID:
	    favDialog = new POIDialog(instance);
	    favDialog.setOnItemClickListener(this);
	    favDialog.setOnItemLongClickListener(this);
	    favDialog.setTitle(R.string.favourites_dialog_title);
	    return favDialog;
	}
	return null;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	Log.i(TAG, "OnItemClick pos " + position + " id " + id);

	String poiId = favDialog.adapter.getItemStringId(position);

	Log.i(TAG, "POI " + poiId + " selected");

	POI poi = null;

	if (poiId != null) {
	    Log.i(TAG, "Got id " + poiId);
	    try {
		poi = getHelper().getBuildingDao().queryForId(poiId);
		if (poi == null) {
		    poi = getHelper().getBusStopDao().queryForId(poiId);
		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }

	    if (poi == null) {
		Log.e(TAG, "Could not find poi " + poiId + " in onActivityResult");
	    } else {
		if (myLocationOverlay != null) {
		    myLocationOverlay.disableFollowLocation();
		}
		mapController.setZoom(20);
		mapController.setCenter(poi.point);

		favDialog.dismiss();

	    }
	} else {
	    Log.i(TAG, "Got null poi id");

	    // mapController.setZoom(15);
	    // mapController.setCenter(new GeoPoint(50935551, -1393488));
	}

    }

    /**
     * Long click on a item in the favourites menu
     */
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

	Log.i(TAG, "OnItemClick pos " + position + " id " + id);

	String poiId = favDialog.adapter.getItemStringId(position);

	Log.i(TAG, "POI " + poiId + " selected");

	POI poi = null;

	if (poiId != null) {
	    Log.i(TAG, "Got id " + poiId);
	    try {
		poi = getHelper().getBuildingDao().queryForId(poiId);
		if (poi == null) {
		    poi = getHelper().getBusStopDao().queryForId(poiId);
		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }

	    if (poi == null) {
		Log.e(TAG, "Could not find poi " + poiId + " in onActivityResult");
	    } else {
		if (poi.type == POI.BUS_STOP) {
		    BusStop busStop = (BusStop) poi;

		    Log.i(TAG, "Pressed " + busStop.id);

		    Intent i = new Intent(this, BusStopActivity.class);
		    i.putExtra("busStopID", busStop.id);
		    i.putExtra("busStopName", busStop.description);
		    startActivityForResult(i, 0);

		    return true;

		} else {

		    myLocationOverlay.disableFollowLocation();
		    mapController.setZoom(20);
		    mapController.setCenter(poi.point);

		    favDialog.dismiss();
		    favDialog = null;
		}
	    }
	} else {
	    Log.i(TAG, "Got null poi id");

	    // mapController.setZoom(15);
	    // mapController.setCenter(new GeoPoint(50935551, -1393488));
	}

	return true;
    }

    private class OverlayRankComparator implements Comparator<Overlay> {
	private final SharedPreferences prefs;

	OverlayRankComparator(SharedPreferences prefs) {
	    this.prefs = prefs;
	}

	public int compare(Overlay arg0, Overlay arg1) {
	    return getRank(arg1) - getRank(arg0);
	}

	private final int getRank(Overlay arg0) { // TODO: Dont hardcode the rank values
	    if (arg0 == scaleBarOverlay) {
		return prefs.getInt("mScaleBarOverlay", 1);
	    } else if (arg0 == myLocationOverlay) {
		return prefs.getInt("myLocationOverlay", 0);
	    } else if (arg0 == uniLinkBusStopOverlay) {
		return prefs.getInt("busStopOverlay", 2);
	    } else if (arg0 == residentialBuildingOverlay) {
		return prefs.getInt("residentialBuildingOverlay", 4);
	    } else if (arg0 == nonResidentialBuildingOverlay) {
		return prefs.getInt("nonResidentialBuildingOverlay", 3);
	    } else if (siteOverlays != null && siteOverlays.values().contains(arg0)) {
		return prefs.getInt("siteOverlays", 6);
	    } else if (routeOverlays != null && routeOverlays.values().contains(arg0)) {
		return prefs.getInt("routeOverlays", 5);
	    } else {
		return -1;
	    }
	}
    }

    /**
     * Handles all changes in preferences
     */
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	Log.v(TAG, "Got shared prefs changed event for key " + key);

	// Shared Preferences
	if (key.equals(GPS_ENABLED)) {
	    final SharedPreferences activityPrefs = getPreferences(0);

	    if (activityPrefs.getBoolean("Other:Compass", false) && prefs.getBoolean("GPSEnabled", false)) {
		myLocationOverlay.enableMyLocation();
	    } else {
		myLocationOverlay.disableMyLocation();
	    }
	} else if (key.equals(NON_UNI_LINK_BUS_TIMES)) {
	    // Noting to do here atm
	} else if (key.equals(UNI_LINK_BUS_TIMES)) {
	    // Noting to do here atm
	} else if (key.equals(UNI_LINK_BUS_STOPS)) {

	    Log.v(TAG, "Begin fetching BusStops at " + (System.currentTimeMillis() - startTime));
	    try {
		if (prefs.getBoolean(UNI_LINK_BUS_STOPS, UNI_LINK_BUS_STOPS_ENABLED_BY_DEFAULT)) {
		    uniLinkBusStopOverlay.busStops = getHelper().getBusStopDao().queryForAll();
		} else {
		    uniLinkBusStopOverlay.busStops = getHelper().getBusStopDao().queryForEq(BusStop.UNI_LINK_FIELD_NAME, true);
		}

		Log.v(TAG, "Finished fetching BusStops at " + (System.currentTimeMillis() - startTime));

		uniLinkBusStopOverlay.refresh();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	} else if (key.contains("Bus Stops")) { // Activity Preferences
	    showBusStopOverlays();
	} else if (key.contains("Bus Routes")) {
	    try {
		for (BusRoute route : getHelper().getBusRouteDao()) {
		    Log.v(TAG, route.code + " " + key.split(":")[1]);
		    if (route.code.equals(key.split(":")[1])) {
			showRouteOverlay(route);
		    }
		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	} else if (key.equals(NON_RESIDENTIAL_BUILDING_OVERLAY)) {
	    showBuildingOverlay(false);
	} else if (key.equals(RESIDENTIAL_BUILDING_OVERLAY)) {
	    showBuildingOverlay(true);
	} else if (key.contains("Site Outlines")) {
	    try {
		for (Site site : getHelper().getSiteDao()) {
		    if (site.name.equals(key.split(":")[1])) {
			showSiteOverlay(site);
		    }
		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	} else if (key.contains("Other")) {
	    if (key.contains("Scale Bar")) {
		showUtilityOverlays();
	    } else if (key.contains("Compass")) {
		if (prefs.getBoolean("Other:Compass", false)) {
		    myLocationOverlay.enableCompass();
		} else {
		    myLocationOverlay.disableCompass();
		}
	    } else if (key.contains("Other:My Location")) {
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (prefs.getBoolean("Other:Compass", false) && sharedPrefs.getBoolean("GPSEnabled", false)) {
		    myLocationOverlay.enableMyLocation();
		} else {
		    myLocationOverlay.disableMyLocation();
		}
	    } else {
		Log.e(TAG, "Unhandled preference key " + key);
	    }
	} else {
	    Log.e(TAG, "Unhandled preference key " + key);
	}
    }

    class ViewDialog extends Dialog implements OnChildClickListener {

	private final ExpandableListView epView;

	private static final String TAG = "ViewDialog";

	private final MyExpandableListAdapter mAdapter;

	private OnChildClickListener listener;

	private String[] busRoutes;
	private String[] buildingTypes;
	private String[] other;
	private String[] groupHeadings;
	private String[] siteNames;

	public ViewDialog(Context context) {
	    super(context);

	    setContentView(R.layout.view_dialog);
	    setTitle("Select the map elements to display");

	    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(this.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    lp.height = WindowManager.LayoutParams.FILL_PARENT;

	    this.getWindow().setAttributes(lp);

	    epView = (ExpandableListView) findViewById(R.id.view_list);
	    mAdapter = new MyExpandableListAdapter(context);
	    epView.setAdapter(mAdapter);
	    epView.setOnChildClickListener(this);

	    int size;
	    try {
		size = (int) getHelper().getSiteDao().countOf();

		ArrayList<Site> sites = new ArrayList<Site>(size);

		try {
		    sites.addAll(getHelper().getSiteDao().queryForAll());
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		siteNames = new String[size];
		for (int i = 0; i < size; i++) {
		    siteNames[i] = sites.get(i).name;
		}
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }

	    busRoutes = getResources().getStringArray(R.array.uniLinkBusRoutes);
	    buildingTypes = getResources().getStringArray(R.array.buildingTypes);
	    other = getResources().getStringArray(R.array.utilityOverlays);
	    groupHeadings = getResources().getStringArray(R.array.preferencesHeadings);

	}

	public void setOnItemClickListener(OnChildClickListener onChildClickListener) {
	    Log.i(TAG, "Listener set for dialog");
	    listener = onChildClickListener;
	}

	class MyExpandableListAdapter extends BaseExpandableListAdapter {

	    private LayoutInflater inflater;

	    private static final String TAG = "MyExpandableListAdapter";

	    // Bus Stops
	    // |_ U1
	    // |_ U1N
	    // |_ U2
	    // |_ U6
	    // |_ U9
	    // Bus Routes
	    // |_ U1
	    // |_ U1N
	    // |_ U2
	    // |_ U6
	    // |_ U9
	    // Buildings
	    // |_ Residential
	    // |_ Non-Residential
	    // Site Outlines
	    // |_ Highfield Campus
	    // |_ Boldrewood Campus
	    // |_ Avenue Campus
	    // |_ Winchester School of Art
	    // |_ The University of Southampton Science Park
	    // |_ National Oceanography Centre Campus
	    // |_ Boat House
	    // |_ Southampton General Hospital
	    // |_ Royal South Hants Hospital
	    // |_ Belgrave Industrial Site
	    // |_ Highfield Hall
	    // |_ Glen Eyre Hall
	    // |_ South Hill Hall
	    // |_ Chamberlain Hall
	    // |_ Hartley Grove
	    // |_ Bencraft Hall
	    // |_ Connaught Hall
	    // |_ Montefiore Hall
	    // |_ Stoneham Hall
	    // |_ Erasmus Park
	    // Other
	    // |_ Scale Bar
	    // |_ Compass
	    // |_ My Location

	    MyExpandableListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	    }

	    public Object getChild(int groupPosition, int childPosition) {
		if (groupPosition == 0 || groupPosition == 1) {
		    return busRoutes[childPosition];
		} else if (groupPosition == 2) {
		    return buildingTypes[childPosition];
		} else if (groupPosition == 3) {
		    return siteNames[childPosition];
		} else if (groupPosition == 4) {
		    return other[childPosition];
		} else {
		    Log.e(TAG, "Unrecognised groupPosition " + groupPosition);
		    return null;
		}
	    }

	    public long getChildId(int groupPosition, int childPosition) {
		return groupPosition * 50 + childPosition;
	    }

	    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		View v = null;
		if (convertView != null)
		    v = convertView;
		else
		    v = inflater.inflate(R.layout.view_child_row, parent, false);
		String c = (String) getChild(groupPosition, childPosition);
		TextView childName = (TextView) v.findViewById(R.id.childname);
		if (childName != null)
		    childName.setText(c);
		CheckBox cb = (CheckBox) v.findViewById(R.id.check1);
		cb.setClickable(false);
		cb.setFocusable(false);
		SharedPreferences activityPrefs = getPreferences(0);
		if (groupPosition == 0) {
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + busRoutes[childPosition],
			    UNI_LINK_BUS_STOP_OVERLAY_ENABLED_BY_DEFAULT));
		} else if (groupPosition == 1) {
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + busRoutes[childPosition], ROUTE_OVERLAY_ENABLED_BY_DEFAULT));
		} else if (groupPosition == 2) {
		    if (childPosition == 0) {
			cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + buildingTypes[childPosition],
				RESIDENTIAL_BUILDING_OVERLAY_ENABLED_BY_DEFAULT));
		    } else {
			cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + buildingTypes[childPosition],
				NON_RESIDENTIAL_BUILDING_OVERLAY_ENABLED_BY_DEFAULT));
		    }
		} else if (groupPosition == 3) {
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + siteNames[childPosition], SITE_OVERLAY_ENABLED_BY_DEFAULT));
		} else if (groupPosition == 4) {
		    // TODO Default value below is not right
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + other[childPosition], SCALE_BAR_OVERLAY_ENABLED_BY_DEFAULT)); // right
		}
		return v;
	    }

	    public int getChildrenCount(int groupPosition) {
		if (groupPosition == 0 || groupPosition == 1) {
		    return busRoutes.length;
		} else if (groupPosition == 2) {
		    return buildingTypes.length;
		} else if (groupPosition == 3) {
		    return siteNames.length;
		} else if (groupPosition == 4) {
		    return other.length;
		}
		return 0;
	    }

	    public Object getGroup(int groupPosition) {
		return groupHeadings[groupPosition];
	    }

	    public int getGroupCount() {
		return groupHeadings.length;
	    }

	    public long getGroupId(int groupPosition) {
		return groupPosition * 5;
	    }

	    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View v = null;
		if (convertView != null)
		    v = convertView;
		else
		    v = inflater.inflate(R.layout.view_group_row, parent, false);
		String gt = (String) getGroup(groupPosition);
		TextView colorGroup = (TextView) v.findViewById(R.id.childname);
		if (gt != null)
		    colorGroup.setText(gt);
		return v;
	    }

	    public boolean hasStableIds() {
		return true;
	    }

	    public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	    }

	}

	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
	    Log.i(TAG, "Got view dialog click at " + groupPosition + ":" + childPosition);

	    SharedPreferences activityPrefs = getPreferences(0);

	    Editor editor = activityPrefs.edit();

	    CheckBox cb = (CheckBox) v.findViewById(R.id.check1);

	    if (groupPosition == 0 || groupPosition == 1) {
		Log.i(TAG, "Setting value of " + groupHeadings[groupPosition] + ":" + busRoutes[childPosition] + " to " + !cb.isChecked());
		editor.putBoolean(groupHeadings[groupPosition] + ":" + busRoutes[childPosition], !cb.isChecked());

	    } else if (groupPosition == 2) {
		Log.i(TAG, "Setting value of " + groupHeadings[groupPosition] + ":" + buildingTypes[childPosition] + " to " + !cb.isChecked());
		editor.putBoolean(groupHeadings[groupPosition] + ":" + buildingTypes[childPosition], !cb.isChecked());

	    } else if (groupPosition == 3) {
		Log.i(TAG, "Setting value of " + groupHeadings[groupPosition] + ":" + siteNames[childPosition] + " to " + !cb.isChecked());
		editor.putBoolean(groupHeadings[groupPosition] + ":" + siteNames[childPosition], !cb.isChecked());

	    } else if (groupPosition == 4) {
		Log.i(TAG, "Setting value of " + groupHeadings[groupPosition] + ":" + other[childPosition] + " to " + !cb.isChecked());
		editor.putBoolean(groupHeadings[groupPosition] + ":" + other[childPosition], !cb.isChecked());
	    }

	    editor.commit();

	    mAdapter.notifyDataSetInvalidated();

	    listener.onChildClick(parent, v, groupPosition, childPosition, id);

	    return true;
	}

    }
}
