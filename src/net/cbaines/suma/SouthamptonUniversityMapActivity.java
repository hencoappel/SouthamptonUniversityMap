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
import java.util.Iterator;

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
	OnChildClickListener, OnItemClickListener, OnItemLongClickListener, OnSharedPreferenceChangeListener {

    private boolean useBundledDatabase = true;

    private MapView mapView;
    private MapController mapController;
    private ResourceProxy mResourceProxy;

    private long startTime;

    static final int VIEW_DIALOG_ID = 0;
    static final int FAVOURITE_DIALOG_ID = 1;

    private HashMap<String, Overlay> overlays = new HashMap<String, Overlay>();
    private HashMap<String, Overlay> pastOverlays;

    private ScaleBarOverlay scaleBarOverlay;
    private MyLocationOverlay myLocationOverlay;
    private BuildingNumOverlay residentialBuildingOverlay;
    private BuildingNumOverlay nonResidentialBuildingOverlay;
    private BusStopOverlay busStopOverlay;
    private HashMap<Site, PathOverlay> siteOverlays;
    private HashMap<BusRoute, PathOverlay> routeOverlays;

    private String[] busRoutes;
    private String[] buildingTypes;
    private String[] other;
    private String[] groupHeadings;
    private String[] siteNames;

    private FavouriteDialog favDialog;

    private SouthamptonUniversityMapActivity instance;

    private static final String TAG = "SUM";

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
	startTime = System.currentTimeMillis();

	super.onCreate(savedInstanceState);

	instance = this;

	final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	final SharedPreferences activityPrefs = getPreferences(0);

	if (!sharedPrefs.contains("GPSEnabled")) {
	    sharedPrefs.edit().putBoolean("GPSEnabled", true).commit();
	}
	if (!sharedPrefs.contains("liveBusTimesEnabled")) {
	    sharedPrefs.edit().putBoolean("liveBusTimesEnabled", true).commit();
	}

	Log.i(TAG, "GPS Enabled " + sharedPrefs.getBoolean("GPSEnabled", false));
	Log.i(TAG, "Live Bus Times Enabled " + sharedPrefs.getBoolean("liveBusTimesEnabled", false));

	setContentView(R.layout.main);

	Log.i(TAG, "Finished setting content view " + (System.currentTimeMillis() - startTime));

	busRoutes = getResources().getStringArray(R.array.uniLinkBusRoutes);
	buildingTypes = getResources().getStringArray(R.array.buildingTypes);
	other = getResources().getStringArray(R.array.utilityOverlays);
	groupHeadings = getResources().getStringArray(R.array.preferencesHeadings);

	mapView = (MapView) this.findViewById(R.id.mapview);
	mapView.setTileSource(TileSourceFactory.MAPNIK);
	mapView.setBuiltInZoomControls(true);
	mapView.setMultiTouchControls(true);

	pastOverlays = (HashMap<String, Overlay>) getLastNonConfigurationInstance();

	Log.i(TAG, "Instantiating myLocationOverlay");
	// SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); This code in the following constructor causes problems in
	// some emulators, disable there sensors to fix.
	myLocationOverlay = new MyLocationOverlay(instance, mapView);
	Log.i(TAG, "Finished instantiating myLocationOverlay");

	mapController = mapView.getController();
	mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

	GeoPoint userLocation = myLocationOverlay.getMyLocation();
	if (userLocation == null) {
	    userLocation = new GeoPoint(50935551, -1393488); // ECS
	}

	mapController.setZoom(15);
	mapController.setCenter(userLocation);

	Editor editor = activityPrefs.edit();
	editor.putBoolean("first_run", false);
	editor.commit();

	new Thread(this).start();
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

	    if (activityPrefs.getBoolean("Other:My Location", false) && sharedPrefs.getBoolean("GPSEnabled", false)) {
		myLocationOverlay.enableMyLocation();
	    } else {
		myLocationOverlay.disableMyLocation();
	    }
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
	Log.i(TAG, "Begining loading the map overlay stuff " + (System.currentTimeMillis() - startTime));

	Log.i(TAG, "Begining loading databases " + (System.currentTimeMillis() - startTime));

	DatabaseHelper helper = getHelper();
	Log.i(TAG, "Got the helper");

	boolean dbExist = helper.checkDataBase();

	if (dbExist) {
	    // do nothing - database already exist
	} else {

	    if (useBundledDatabase) {
		try {
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

		    try {
			siteNames = new String[(int) siteDao.countOf()];

			int i = 0;
			for (Site site : siteDao) {
			    siteNames[i] = site.name;
			    i++;
			}
		    } catch (SQLException e) {
			e.printStackTrace();
		    }

		    SharedPreferences mainPrefs = getPreferences(0);
		    if (mainPrefs.getBoolean("first_run", true)) {
			Log.i(TAG, "Changing button in intro");
		    }

		    Log.i(TAG, "Finished loading databases " + (System.currentTimeMillis() - startTime));

		} catch (SQLException e1) {
		    e1.printStackTrace();
		}
	    }

	}

	try {
	    setupActivityPrefs();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	createOverlays();
	Log.i(TAG, "Finished seting in motion the creation of the overlays " + (System.currentTimeMillis() - startTime));

    }

    private void setupActivityPrefs() throws SQLException {
	Log.i(TAG, "Begining setting up preferences");

	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

	int size = (int) getHelper().getSiteDao().countOf();
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

	SharedPreferences activityPrefs = getPreferences(0);
	Editor editor = activityPrefs.edit();

	for (int heading = 0; heading < groupHeadings.length; heading++) {
	    if (heading == 0 || heading == 1) {
		for (int child = 0; child < busRoutes.length; child++) {
		    if (!activityPrefs.contains(groupHeadings[heading] + ":" + busRoutes[child])) {
			editor.putBoolean(groupHeadings[heading] + ":" + busRoutes[child], true);
		    }
		}
	    } else if (heading == 2) {
		for (int child = 0; child < buildingTypes.length; child++) {
		    if (!activityPrefs.contains(groupHeadings[heading] + ":" + buildingTypes[child])) {
			editor.putBoolean(groupHeadings[heading] + ":" + buildingTypes[child], true);
		    }
		}
	    } else if (heading == 3) {
		for (int child = 0; child < sites.size(); child++) {
		    if (!activityPrefs.contains(groupHeadings[heading] + ":" + sites.get(child))) {
			editor.putBoolean(groupHeadings[heading] + ":" + sites.get(child), true);
		    }
		}
	    } else if (heading == 4) {
		for (int child = 0; child < other.length; child++) {
		    if (!activityPrefs.contains(groupHeadings[heading] + ":" + other[child])) {
			editor.putBoolean(groupHeadings[heading] + ":" + other[child], true);
		    }
		}
	    }
	}

	editor.commit();

	activityPrefs.registerOnSharedPreferenceChangeListener(this);
	sharedPrefs.registerOnSharedPreferenceChangeListener(this);

	Log.i(TAG, "Finished setting up preferences");
    }

    private void createOverlays() {
	Log.i(TAG, "Began creating overlays at " + (System.currentTimeMillis() - startTime));

	if (pastOverlays != null) {
	    Log.i(TAG, "Able to recover some/all of the overlays from a previous activity");
	} else {
	    Log.i(TAG, "Unable to recover overlays");
	}

	final OverlayRankComparator comparator = new OverlayRankComparator(getPreferences(0));
	final SharedPreferences activityPrefs = getPreferences(0);

	Thread utilityOverlayCreation = new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining the creation of the utility overlays");

		if (pastOverlays != null) {
		    scaleBarOverlay = (ScaleBarOverlay) pastOverlays.get("Other:Scale Bar");

		    if (scaleBarOverlay != null && myLocationOverlay != null) {
			overlays.put("Other:Scale Bar", scaleBarOverlay);
			Log.i(TAG, "Finished restoring utility overlays " + (System.currentTimeMillis() - startTime));
			return;
		    }
		}

		scaleBarOverlay = new ScaleBarOverlay(instance);
		scaleBarOverlay.setEnabled(activityPrefs.getBoolean("Other:Scale Bar", true));

		overlays.put("Other:Scale Bar", scaleBarOverlay);

		Log.i(TAG, "Finished creating utility overlays " + (System.currentTimeMillis() - startTime));

	    }
	});

	utilityOverlayCreation.start();

	Runnable utilityOverlayApplication = new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining the application of the utility overlays");

		mapView.getOverlays().add(scaleBarOverlay);

		mapView.getOverlays().add(myLocationOverlay);

		Log.v(TAG, "Applyed the utility overlays, now sorting them");

		Collections.sort(mapView.getOverlays(), comparator);

		Log.v(TAG, "Finished sorting the utility overlays them, now applying them");

		mapView.invalidate();

		Log.i(TAG, "Finished loading utility overlays " + (System.currentTimeMillis() - startTime));

	    }
	};

	Thread routeOverlayCreation = new Thread(new Runnable() {
	    public void run() {

		try {
		    Log.i(TAG, "Begining to create the route overlays");

		    SharedPreferences mainPrefs = getPreferences(0);

		    routeOverlays = new HashMap<BusRoute, PathOverlay>(5);

		    Dao<BusRoute, Integer> busRouteDao = getHelper().getBusRouteDao();

		    for (Iterator<BusRoute> routeIter = busRouteDao.iterator(); routeIter.hasNext();) {
			BusRoute route = routeIter.next();

			Log.v(TAG, "Looking at route " + route.code);

			if (pastOverlays != null) {
			    PathOverlay routeOverlay = (PathOverlay) pastOverlays.get("Bus Routes:" + route.code);
			    if (routeOverlay != null) {
				Log.i(TAG, "Restored " + route.code + " route overlay");
				routeOverlays.put(route, routeOverlay);
				overlays.put("Bus Routes:" + route.code, routeOverlay);
				continue;
			    }
			}

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
			    routeOverlayU1E.setEnabled(mainPrefs.getBoolean("Bus Routes:" + route.code, true));

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
			    continue;
			}

			PathOverlay routeOverlay = DataManager.getRoutePath(resource, colour, mResourceProxy);

			Log.i(TAG, "Path overlay has " + routeOverlay.getNumberOfPoints() + " points");

			routeOverlay.getPaint().setAntiAlias(true);
			routeOverlay.getPaint().setAlpha(145);
			routeOverlay.getPaint().setStrokeWidth(12);
			routeOverlay.setEnabled(mainPrefs.getBoolean("Bus Routes:" + route.code, true));

			routeOverlays.put(route, routeOverlay);
			overlays.put("Bus Routes:" + route.code, routeOverlay);

		    }

		    Log.i(TAG, "Finished loading routes " + (System.currentTimeMillis() - startTime));

		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});

	routeOverlayCreation.start();

	Runnable routeOverlayApplication = new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining applying the route overlays, number of route overlays = " + routeOverlays.size());

		for (PathOverlay routeOverlay : routeOverlays.values()) {
		    Log.v(TAG, "Added route overlay");
		    mapView.getOverlays().add(routeOverlay);
		}

		Log.v(TAG, "Added the route overlays, now sorting them");

		Collections.sort(mapView.getOverlays(), comparator);

		Log.v(TAG, "Finished sorting the route overlays them, now applying them");

		mapView.invalidate();

		Log.i(TAG, "Finished loading route overlays " + (System.currentTimeMillis() - startTime));
	    }
	};

	Thread siteOverlayCreation = new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining the creation of the site overlays");

		SharedPreferences mainPrefs = getPreferences(0);

		try {

		    Dao<Site, String> siteDao = getHelper().getSiteDao();
		    siteOverlays = new HashMap<Site, PathOverlay>((int) siteDao.countOf());

		    for (Site site : siteDao) {

			if (pastOverlays != null) {
			    PathOverlay overlay = (PathOverlay) pastOverlays.get("Site Outlines:" + site.name);
			    if (overlay != null) {
				Log.i(TAG, "Restored " + site.name + " site overlay");
				siteOverlays.put(site, overlay);
				overlays.put("Site Outlines:" + site.name, overlay);
				continue;
			    }
			}

			PathOverlay overlay = new PathOverlay(Color.BLUE, instance);
			Paint paint = overlay.getPaint();
			paint.setAntiAlias(true);
			paint.setStrokeWidth(1.5f);
			for (int i = 0; i < site.outline.points.length; i++) {
			    overlay.addPoint(site.outline.points[i]);
			}
			overlay.addPoint(site.outline.points[0]);

			overlay.setEnabled(mainPrefs.getBoolean("Site Outlines:" + site.name, true));

			siteOverlays.put(site, overlay);
			overlays.put("Site Outlines:" + site.name, overlay);
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}

		Log.i(TAG, "Finished creating site overlays " + (System.currentTimeMillis() - startTime));

	    }
	});

	siteOverlayCreation.start();

	Runnable siteOverlayApplication = new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining applying the site overlays, number of site overlays = " + siteOverlays.size());

		for (PathOverlay siteOverlay : siteOverlays.values()) {
		    Log.d(TAG, "Added site overlay");
		    mapView.getOverlays().add(siteOverlay);
		}

		Log.v(TAG, "Added the site overlays, now sorting them");

		Collections.sort(mapView.getOverlays(), comparator);

		Log.v(TAG, "Finished sorting the site overlays them, now applying them");

		mapView.invalidate();

		Log.i(TAG, "Finished loading site overlays " + (System.currentTimeMillis() - startTime));
	    }
	};

	Thread buildingOverlayCreation = new Thread(new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining the creation of the building overlays");
		try {
		    if (pastOverlays != null) {
			residentialBuildingOverlay = (BuildingNumOverlay) pastOverlays.get("Buildings:Residential");
			nonResidentialBuildingOverlay = (BuildingNumOverlay) pastOverlays.get("Buildings:Non-Residential");
			if (residentialBuildingOverlay != null && nonResidentialBuildingOverlay != null) {
			    overlays.put("Buildings:" + buildingTypes[0], residentialBuildingOverlay);
			    overlays.put("Buildings:" + buildingTypes[1], nonResidentialBuildingOverlay);

			    Log.i(TAG, "Restored building overlays");
			    return;
			}
		    }

		    SharedPreferences mainPrefs = getPreferences(0);

		    ArrayList<Building> residentialBuildings = new ArrayList<Building>();
		    ArrayList<Building> nonResidentialBuildings = new ArrayList<Building>();

		    Dao<Building, String> buildingDao = getHelper().getBuildingDao();

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

		    residentialBuildingOverlay.setEnabled(mainPrefs.getBoolean("Buildings:Residential", false));
		    nonResidentialBuildingOverlay.setEnabled(mainPrefs.getBoolean("Buildings:Non-Residential", false));

		    overlays.put("Buildings:" + buildingTypes[0], residentialBuildingOverlay);
		    overlays.put("Buildings:" + buildingTypes[1], nonResidentialBuildingOverlay);

		    Log.i(TAG, "Finished creating building overlays " + (System.currentTimeMillis() - startTime));
		} catch (SQLException e) {
		    e.printStackTrace();
		}

	    }
	});

	buildingOverlayCreation.start();

	Runnable buildingOverlayApplication = new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining applying the building overlays");

		mapView.getOverlays().add(residentialBuildingOverlay);
		mapView.getOverlays().add(nonResidentialBuildingOverlay);

		Log.v(TAG, "Added the building overlays, now sorting them");

		Collections.sort(mapView.getOverlays(), comparator);

		Log.v(TAG, "Finished sorting the building overlays, now applying them");

		mapView.invalidate();

		Log.i(TAG, "Finished loading building overlays " + (System.currentTimeMillis() - startTime));
	    }
	};

	Thread busStopOverlayCreation = new Thread(new Runnable() {

	    public void run() {
		Log.i(TAG, "Begining the creation of the bus stop overlay");

		if (pastOverlays != null) {
		    busStopOverlay = (BusStopOverlay) pastOverlays.get("BusStops");
		    if (busStopOverlay != null) {
			overlays.put("BusStops", busStopOverlay);

			Log.i(TAG, "Restored bus stop overlays");
			return;
		    }
		}

		try {
		    SharedPreferences mainPrefs = getPreferences(0);

		    busStopOverlay = new BusStopOverlay(instance);
		    busStopOverlay.setRoutes(0, mainPrefs.getBoolean("Bus Stops:U1", false));
		    busStopOverlay.setRoutes(1, mainPrefs.getBoolean("Bus Stops:U1N", false));
		    busStopOverlay.setRoutes(2, mainPrefs.getBoolean("Bus Stops:U2", false));
		    busStopOverlay.setRoutes(3, mainPrefs.getBoolean("Bus Stops:U6", false));
		    busStopOverlay.setRoutes(4, mainPrefs.getBoolean("Bus Stops:U9", false));

		    overlays.put("BusStops", busStopOverlay);
		} catch (SQLException e) {
		    e.printStackTrace();
		}

		Log.i(TAG, "Finished creating the bus stops overlay " + (System.currentTimeMillis() - startTime));
	    }
	});

	busStopOverlayCreation.start();

	Runnable busStopOverlayApplication = new Runnable() {
	    public void run() {
		Log.i(TAG, "Begining applying the bus stop overlay");

		mapView.getOverlays().add(busStopOverlay);

		Log.v(TAG, "Added the bus stop overlay, now sorting them");

		Collections.sort(mapView.getOverlays(), comparator);

		Log.v(TAG, "Finished sorting the bus stop overlay, now applying them");

		mapView.invalidate();

		Log.i(TAG, "Finished loading bus stop overlay " + (System.currentTimeMillis() - startTime));
	    }
	};

	while (utilityOverlayCreation != null || routeOverlayCreation != null || siteOverlayCreation != null || buildingOverlayCreation != null
		|| busStopOverlayCreation != null) {
	    if (utilityOverlayCreation != null && !utilityOverlayCreation.isAlive()) {
		mapView.post(utilityOverlayApplication);
		utilityOverlayCreation = null;
	    }

	    if (routeOverlayCreation != null && !routeOverlayCreation.isAlive()) {
		mapView.post(routeOverlayApplication);
		routeOverlayCreation = null;
	    }

	    if (siteOverlayCreation != null && !siteOverlayCreation.isAlive()) {
		mapView.post(siteOverlayApplication);
		siteOverlayCreation = null;
	    }

	    if (buildingOverlayCreation != null && !buildingOverlayCreation.isAlive()) {
		mapView.post(buildingOverlayApplication);
		buildingOverlayCreation = null;
	    }

	    if (busStopOverlayCreation != null && !busStopOverlayCreation.isAlive()) {
		mapView.post(busStopOverlayApplication);
		busStopOverlayCreation = null;
	    }

	    Thread.yield();
	}

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
	    if (favDialog != null) {
		favDialog.refresh();
	    } else {
		Log.e(TAG, "Very wierd, just tried to launch the favourite's dialog, but its null?");
	    }
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

			busStopOverlay.refresh(busStop); // This does not invalidate the map, but it seems to make the changes appear
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}

		if (favDialog != null) {
		    favDialog.refresh();
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
	    favDialog = new FavouriteDialog(instance);
	    favDialog.setOnItemClickListener(this);
	    favDialog.setOnItemLongClickListener(this);
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

		    Intent i = new Intent(this, BusTimeActivity.class);
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
	    } else if (arg0 == busStopOverlay) {
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

	if (key.equals("GPSEnabled")) {
	    final SharedPreferences activityPrefs = getPreferences(0);

	    if (activityPrefs.getBoolean("Other:Compass", false) && prefs.getBoolean("GPSEnabled", false)) {
		myLocationOverlay.enableMyLocation();
	    } else {
		myLocationOverlay.disableMyLocation();
	    }
	} else if (key.equals("liveBusTimesEnabled")) {
	    // Noting to do here atm
	} else if (key.contains("Bus Stops")) {
	    busStopOverlay.setRoutes(0, prefs.getBoolean("Bus Stops:U1", false));
	    busStopOverlay.setRoutes(1, prefs.getBoolean("Bus Stops:U1N", false));
	    busStopOverlay.setRoutes(2, prefs.getBoolean("Bus Stops:U2", false));
	    busStopOverlay.setRoutes(3, prefs.getBoolean("Bus Stops:U6", false));
	    busStopOverlay.setRoutes(4, prefs.getBoolean("Bus Stops:U9", false));
	} else if (key.contains("Bus Routes")) {
	    for (BusRoute route : routeOverlays.keySet()) {
		Log.v(TAG, route.code + " " + key.split(":")[1]);
		if (route.code.equals(key.split(":")[1])) {
		    routeOverlays.get(route).setEnabled(prefs.getBoolean(key, false));
		    if (route.code.equals("U1")) {
			overlays.get("Bus Routes:" + route.code + "E").setEnabled(prefs.getBoolean(key, false));
		    }
		}
	    }
	} else if (key.contains("Buildings")) {
	    if (key.equals("Buildings:Non-Residential")) {
		nonResidentialBuildingOverlay.setEnabled(prefs.getBoolean(key, false));
	    } else if (key.equals("Buildings:Residential")) {
		residentialBuildingOverlay.setEnabled(prefs.getBoolean(key, false));
	    } else {
		Log.e(TAG, "Wierd building preferences key " + key);
	    }
	} else if (key.contains("Site Outlines")) {
	    for (Site site : siteOverlays.keySet()) {
		if (site.name.equals(key.split(":")[1])) {
		    siteOverlays.get(site).setEnabled(prefs.getBoolean(key, false));
		}
	    }
	} else if (key.contains("Other")) {
	    if (key.contains("Scale Bar")) {
		scaleBarOverlay.setEnabled(prefs.getBoolean("Other:Scale Bar", false));
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

		if (groupPosition == 0 || groupPosition == 1) {
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + busRoutes[childPosition], true));
		} else if (groupPosition == 2) {
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + buildingTypes[childPosition], true));
		} else if (groupPosition == 3) {
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + siteNames[childPosition], true));
		} else if (groupPosition == 4) {
		    cb.setChecked(activityPrefs.getBoolean(groupHeadings[groupPosition] + ":" + other[childPosition], true));
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