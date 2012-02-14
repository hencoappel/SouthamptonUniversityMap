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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

public class DataManager {

    private final static String TAG = "DataManager";

    private final static String busStopUrl = "http://data.southampton.ac.uk/bus-stop/";

    private static DatabaseHelper helper;
    private static Dao<BusRoute, Integer> busRouteDao;
    private static Dao<Bus, Integer> busDao;
    private static Dao<BusStop, String> busStopDao;

    public static void loadBuildings(Context context) throws SQLException, IOException {
	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
	Dao<Building, String> buildingDao = helper.getBuildingDao();

	TableUtils.clearTable(helper.getConnectionSource(), Building.class);

	Log.i(TAG, "Loading buildings from csv");

	HashMap<String, GeoPoint> buildingPoints = new HashMap<String, GeoPoint>();

	InputStream inputStream = context.getAssets().open("buildings_points.csv");
	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	String strLine;
	try {
	    String def = bufferedReader.readLine();
	    // Log.i(TAG, "Reading the definition " + def);

	    while ((strLine = bufferedReader.readLine()) != null) {
		// Log.i(TAG, "Data: " + strLine);
		String[] dataBits = strLine.split(",");
		GeoPoint point = Util.csLatLongToGeoPoint(dataBits[2], dataBits[1]);
		// Log.i(TAG, "Creating building with id " + dataBits[0] + " and " + point.getLatitudeE6() + " " + point.getLongitudeE6());
		buildingPoints.put(dataBits[0], point);
	    }

	    bufferedReader.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	Log.i(TAG, "Number of building points " + buildingPoints.size());

	/*
	 * inputStream = context.getResources().openRawResource(R.raw.buildings_shapes); bufferedReader = new BufferedReader(new
	 * InputStreamReader(inputStream));
	 * 
	 * try { String def = bufferedReader.readLine(); // Log.i(TAG, "Reading the definition " + def);
	 * 
	 * while ((strLine = bufferedReader.readLine()) != null) { // Log.i(TAG, "Data: " + strLine); String[] dataBits = strLine.split(","); Polygon poly =
	 * Util.csPolygonToPolygon(strLine.split("\"")[1]); // Log.i(TAG, "Creating building with id " + dataBits[0] + " and " + poly);
	 * buildingPolys.put(dataBits[0], poly); }
	 * 
	 * bufferedReader.close(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * Log.i(TAG, "Number of polys points " + buildingPolys.size());
	 */

	inputStream = context.getAssets().open("building_estates.csv");
	bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

	try {
	    String def = bufferedReader.readLine();
	    // Log.i(TAG, "Reading the definition " + def);

	    while ((strLine = bufferedReader.readLine()) != null) {
		// Log.i(TAG, "Data: " + strLine);
		if (strLine.startsWith("\"")) {
		    String[] quoteBits = strLine.split("\"");
		    // Log.i(TAG, "QuoteBits " + quoteBits[0] + " | " + quoteBits[1]);
		    String[] dataBits = quoteBits[2].split(",");
		    // Log.i(TAG, "dataBits " + dataBits[0] + " | " + dataBits[1]);

		    if (buildingPoints.get(dataBits[1]) == null) {
			// Log.w(TAG, "Building " + dataBits[1] + " has a null point");
			continue;
		    }

		    Building bdg = new Building(dataBits[1], buildingPoints.get(dataBits[1]), dataBits[3].equals("R"), quoteBits[0]);
		    /*
		     * Polygon poly = buildingPolys.get(dataBits[1]);
		     * 
		     * if (poly != null) { bdg.outline = poly; // Log.i(TAG, "Adding building " + key + " " + bdg.point.getLatitudeE6() + " " +
		     * bdg.point.getLongitudeE6() + " " + poly); } else { // Log.i(TAG, "Adding building " + key + " " + bdg.point.getLatitudeE6() + " " +
		     * bdg.point.getLongitudeE6()); }
		     */

		    // Log.i(TAG, "Creating building " + bdg.id + " " + bdg.name + " " + bdg.point + " " + bdg.residential + " " + bdg.outline);

		    buildingDao.create(bdg);

		} else {

		    String[] dataBits = strLine.split(",");

		    if (buildingPoints.get(dataBits[1]) == null) {
			// Log.w(TAG, "Building " + dataBits[1] + " has a null point");
			continue;
		    }

		    Building bdg = new Building(dataBits[1], buildingPoints.get(dataBits[1]), dataBits[3].equals("R"), dataBits[0]);
		    /*
		     * Polygon poly = buildingPolys.get(dataBits[1]);
		     * 
		     * if (poly != null) { bdg.outline = poly; // Log.i(TAG, "Adding building " + key + " " + bdg.point.getLatitudeE6() + " " +
		     * bdg.point.getLongitudeE6() + " " + poly); } else { // Log.i(TAG, "Adding building " + key + " " + bdg.point.getLatitudeE6() + " " +
		     * bdg.point.getLongitudeE6()); }
		     */

		    // Log.i(TAG, "Creating building " + bdg.id + " " + bdg.name + " " + bdg.point + " " + bdg.residential + " " + bdg.outline);

		    buildingDao.create(bdg);

		}
	    }

	    bufferedReader.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	/*
	 * for (Iterator<String> iter = buildingPoints.keySet().iterator(); iter.hasNext();) { String key = iter.next();
	 * 
	 * Building bdg = new Building(key, buildingPoints.get(key), false); Polygon poly = buildingPolys.get(key);
	 * 
	 * if (poly != null) { bdg.outline = poly; // Log.i(TAG, "Adding building " + key + " " + bdg.point.getLatitudeE6() + " " + bdg.point.getLongitudeE6() +
	 * " " + poly); } else { // Log.i(TAG, "Adding building " + key + " " + bdg.point.getLatitudeE6() + " " + bdg.point.getLongitudeE6()); }
	 * 
	 * buildingDao.create(bdg); }
	 */

    }

    public static void loadBusData(Context context, boolean onlyUniLink) throws SQLException, IOException {
	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	Dao<BusStop, String> busStopDao = helper.getBusStopDao();
	Dao<BusRoute, Integer> busRouteDao = helper.getBusRouteDao();
	Dao<RouteStops, Integer> routeStopsDao = helper.getRouteStopsDao();

	TableUtils.clearTable(helper.getConnectionSource(), BusStop.class);
	TableUtils.clearTable(helper.getConnectionSource(), BusRoute.class);
	TableUtils.clearTable(helper.getConnectionSource(), RouteStops.class);

	Log.i(TAG, "Loading busstops from csv");

	InputStream inputStream = context.getAssets().open("bus_stops.csv");
	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	String strLine = "";

	try {
	    String def = bufferedReader.readLine();
	    // Log.i(TAG, "Reading the definition " +def );

	    while ((strLine = bufferedReader.readLine()) != null) {
		// Log.i(TAG, "Data: " + strLine);
		String[] dataBits = strLine.split(",");

		String[] quBitsLat = dataBits[3].substring(1, dataBits[3].length() - 1).split(" ");
		String[] quBitsLng = dataBits[4].substring(1, dataBits[4].length() - 1).split(" ");

		// Log.i(TAG, "Whole " + dataBits[3] + " First bit " + quBitsLat[0] + " last bit " + quBitsLat[1]);
		double lat = Double.valueOf(quBitsLat[0]) + Double.valueOf(quBitsLat[1].substring(0, quBitsLat[1].length() - 1)) / 60d; // TODO Much hackage
		// Log.i(TAG, "Whole " + dataBits[4] + " First bit " + quBitsLng[0] + " last bit " + quBitsLng[1]);
		double lng = Double.valueOf(quBitsLng[0]) + Double.valueOf(quBitsLng[1].substring(0, quBitsLng[1].length() - 1)) / 60d; // TODO Much hackage
		GeoPoint point = new GeoPoint((int) (lat * 1e6), (int) (lng * -1e6));
		// Log.i(TAG, "Lat " + point.getLatitudeE6() + " lng " + point.getLongitudeE6());

		busStopDao.create(new BusStop(dataBits[0].replace("\"", ""), dataBits[1].replace("\"", ""), dataBits[2].replace("\"", ""), point));

	    }

	    bufferedReader.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    Log.e(TAG, "Line: " + strLine);
	    e.printStackTrace();
	}

	Log.i(TAG, "Finished loading busstops, now loading routes");

	inputStream = context.getAssets().open("routes.csv");
	bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

	try {
	    String def = bufferedReader.readLine();
	    // Log.i(TAG, "Reading the definition " + def);

	    while ((strLine = bufferedReader.readLine()) != null) {
		// Log.i(TAG, "Data: " + strLine);
		String[] dataBits = strLine.split(",");

		BusRoute route;

		boolean uniLink = false;
		int id = Integer.parseInt(dataBits[0]);
		if (id == 326 || id == 468 || id == 327 || id == 329 || id == 354) {
		    uniLink = true;
		}

		route = new BusRoute(id, dataBits[1], dataBits[2].replace("\"", ""), uniLink);

		if (id == 326) {
		    route.forwardDirection = "C";
		    route.reverseDirection = "A";
		} else if (id == 329) {
		    route.forwardDirection = "C";
		    route.reverseDirection = "B";
		} else if (id == 327) {
		    route.forwardDirection = "H";
		    route.reverseDirection = "C";
		}

		// Log.i(TAG, "Loaded route " + route.id + " " + route.code + " " + route.label);
		busRouteDao.create(route);

	    }

	    bufferedReader.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    Log.e(TAG, "Line: " + strLine);
	    e.printStackTrace();
	}

	Log.i(TAG, "Finished loading routes, now loading routestops");

	inputStream = context.getAssets().open("routestops.csv");
	bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

	try {
	    String def = bufferedReader.readLine();
	    // Log.i(TAG, "Reading the definition " + def);

	    while ((strLine = bufferedReader.readLine()) != null) {
		// Log.i(TAG, "Data: " + strLine);
		String[] dataBits = strLine.split(",");

		BusStop stop = busStopDao.queryForId(dataBits[2]);
		if (stop != null) {
		    // Log.i(TAG, "Found stop " + stop.id);
		} else {
		    Log.w(TAG, "No stop found for " + dataBits[2]);
		    continue;
		}

		BusRoute route = busRouteDao.queryForId(Integer.parseInt(dataBits[0]));
		if (route != null) {
		    // Log.i(TAG, "Found route " + route.id);
		} else {
		    Log.w(TAG, "No route found for " + dataBits[0]);
		    continue;
		}

		int sequence = Integer.parseInt(dataBits[1]);
		Log.i(TAG, "Creating RouteStop " + stop.id + " " + route.code + " " + sequence);

		routeStopsDao.create(new RouteStops(stop, route, sequence));

		if (route.id == 326) { // U1
		    stop.routes = (byte) (stop.routes | 1);
		} else if (route.id == 468) { // U1N
		    stop.routes = (byte) (stop.routes | (1 << 1));
		} else if (route.id == 329) { // U2
		    stop.routes = (byte) (stop.routes | (1 << 2));
		} else if (route.id == 327) { // U6
		    stop.routes = (byte) (stop.routes | (1 << 3));
		} else if (route.id == 354) { // U9
		    stop.routes = (byte) (stop.routes | (1 << 4));
		} else {
		    stop.routes = 0;
		}

		Log.v(TAG, "Stop routes " + stop.routes);
		busStopDao.update(stop);

	    }

	    bufferedReader.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    Log.e(TAG, "Line: " + strLine);
	    e.printStackTrace();
	}

	for (Iterator<BusStop> busStopIter = busStopDao.iterator(); busStopIter.hasNext();) {
	    BusStop stop = busStopIter.next();
	    // Log.i(TAG, "Looking at stop " + stop.id);

	    /*
	     * QueryBuilder<RouteStops, Integer> routeStopsQueryBuilder = routeStopsDao.queryBuilder(); routeStopsQueryBuilder.where().eq(columnName, value)
	     * 
	     * DeleteBuilder<BusStop, String> deleteBuilder = busStopDao.deleteBuilder(); // only delete the rows where password is null
	     * deleteBuilder.where().in(RouteStops.STOP_ID_FIELD_NAME, objects) accountDao.delete(deleteBuilder.prepare());
	     */

	    QueryBuilder<RouteStops, Integer> routeStopsQueryBuilder = routeStopsDao.queryBuilder();
	    routeStopsQueryBuilder.setCountOf(true);
	    routeStopsQueryBuilder.where().eq(RouteStops.STOP_ID_FIELD_NAME, stop);

	    PreparedQuery<RouteStops> routeStopsPreparedQuery = routeStopsQueryBuilder.prepare();
	    long num = routeStopsDao.countOf(routeStopsPreparedQuery);
	    // long num = routeStopsDao.query(routeStopsPreparedQuery).size();
	    // Log.i(TAG, "Number is " + num);
	    if (num == 0) {
		// Log.i(TAG, "Removing " + stop.id);
		stop.uniLink = false;
	    } else {
		stop.uniLink = true;
	    }
	    busStopDao.update(stop);
	}

	Log.i(TAG, "Finished loading bus data");
    }

    public static void loadSiteData(Context context) throws SQLException, IOException {
	Log.i(TAG, "Begining loading site data");

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	TableUtils.clearTable(helper.getConnectionSource(), Site.class);

	Dao<Site, String> siteDao = helper.getSiteDao();

	InputStream inputStream = context.getAssets().open("sites.csv");
	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	String strLine = null;

	try {
	    String def = bufferedReader.readLine();
	    // Log.i(TAG, "Reading the site definition " + def);

	    while ((strLine = bufferedReader.readLine()) != null) {
		// Log.i(TAG, "Site Data: " + strLine);
		String[] dataBits = strLine.split(",");

		GeoPoint point = null;
		if (dataBits[2].length() > 1 && dataBits[3].length() > 1) {
		    point = Util.csLatLongToGeoPoint(dataBits[2], dataBits[3]);
		} else {
		    point = new GeoPoint(0, 0);
		}

		Polygon poly = Util.csPolygonToPolygon(strLine.split("\"")[1]);
		// Log.i(TAG, "Polygon: " + poly);

		siteDao.create(new Site(dataBits[0], dataBits[1], point, poly));
	    }

	    bufferedReader.close();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    Log.e(TAG, "Site Line: " + strLine);
	    e.printStackTrace();
	}

	Log.i(TAG, "Loaded sites from csv");
    }

    private static Stop getStop(Context context, JSONObject stopObj, Set<BusRoute> routes, BusStop busStop) throws SQLException, JSONException {

	if (helper == null)
	    helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
	if (busRouteDao == null)
	    busRouteDao = helper.getBusRouteDao();
	if (busDao == null)
	    busDao = helper.getBusDao();
	if (busStopDao == null)
	    busStopDao = helper.getBusStopDao();

	String time = stopObj.getString("time");

	GregorianCalendar calender = new GregorianCalendar();
	boolean live = true;
	if (!time.equals("Due")) {

	    Log.v(TAG, "Time: " + time + " current time " + calender.getTime());

	    if (time.contains(":")) {
		String[] minAndHour = time.split(":");
		calender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(minAndHour[0]));
		calender.set(Calendar.MINUTE, Integer.parseInt(minAndHour[1]));
		live = false;
	    } else {
		// Log.i(TAG, "Parsing " + time.substring(0, time.length() - 1) + " for min");
		calender.add(Calendar.MINUTE, Integer.parseInt(time.substring(0, time.length() - 1)));
	    }

	    Log.v(TAG, "Date: " + calender.getTime());
	}

	String name = stopObj.getString("name");

	BusRoute route = null;
	String dir = "";

	for (BusRoute tempRoute : routes) {
	    if (name.contains("U")) {
		if (name.equals("U1N")) {
		    if (tempRoute.code.equals(name)) {
			route = tempRoute;
			dir = null;
		    }
		} else {
		    if (tempRoute.code.equals(name.substring(0, 2))) {
			route = tempRoute;
			if (route.forwardDirection.equals(name.substring(2))) {
			    dir = route.forwardDirection;
			} else if (route.reverseDirection.equals(name.substring(2))) {
			    dir = route.reverseDirection;
			} else {
			    Log.e(TAG, "Error detecting direction for " + name);
			    dir = null;
			    return null;
			}
		    }
		}
	    } else {
		if (tempRoute.code.equals(name)) {
		    route = tempRoute;
		    dir = null;
		}
	    }
	}

	if (route == null) {
	    Log.e(TAG, "Route not found (route == null) " + name);
	    return null;
	}

	if (dir != null && dir.equals("")) {
	    Log.e(TAG, "Error detecting direction for " + name);
	    return null;
	}

	String destString = stopObj.getString("dest");
	BusStop destStop = null;

	if (destString.equals("Central Station")) {
	    destStop = busStopDao.queryForId("SNA19709");
	} else if (destString.equals("Civic Centre")) {
	    destStop = busStopDao.queryForId("SN120527");
	} else if (destString.equals("City DG4")) {
	    destStop = busStopDao.queryForId("HAA13579");
	} else if (destString.equals("Central Station")) {
	    destStop = busStopDao.queryForId("SN120520");
	} else if (destString.equals("Airport")) {
	    destStop = busStopDao.queryForId("HA030184");
	} else if (destString.equals("City, Town Quay")) {
	    destStop = busStopDao.queryForId("SNA13766");
	} else if (destString.equals("City Centre")) {
	    destStop = busStopDao.queryForId("SNA13766");
	} else if (destString.equals("Dock Gate 4")) {
	    destStop = busStopDao.queryForId("MG1031");
	} else if (destString.equals("Eastleigh")) {
	    destStop = busStopDao.queryForId("HA030212");
	} else if (destString.equals("Crematorium")) {
	    destStop = busStopDao.queryForId("SN121009");
	} else if (destString.equals("General Hosp")) {
	    destStop = busStopDao.queryForId("SNA19482");
	} else if (destString.equals("Wessex Lane")) {
	    destStop = busStopDao.queryForId("SNA19780");
	} else {
	    Log.e(TAG, "Unknown end dest " + destString + " for route " + route.code);
	}

	Date now = new Date(System.currentTimeMillis());

	String busID = null;
	Stop stop;
	Bus bus;
	if (stopObj.has("vehicle")) {
	    busID = stopObj.getString("vehicle");

	    QueryBuilder<Bus, Integer> busQueryBuilder = busDao.queryBuilder();
	    busQueryBuilder.where().eq(Bus.ID_FIELD_NAME, busID);
	    PreparedQuery<Bus> busPreparedQuery = busQueryBuilder.prepare();

	    bus = busDao.queryForFirst(busPreparedQuery);

	    if (bus == null) {
		bus = new Bus(busID, route, dir);
		bus.destination = destStop;
		busDao.create(bus);
	    } else {
		bus.destination = destStop;
		bus.route = route;
		bus.direction = dir;
		busDao.update(bus);
	    }

	} else {
	    bus = new Bus(null, route, dir);
	    busDao.create(bus);
	}

	stop = new Stop(bus, busStop, calender.getTime(), now, live);

	return stop;
    }

    public static Timetable getTimetable(Context context, String busStop, boolean keepUniLink, boolean keepNonUniLink) throws SQLException,
	    ClientProtocolException, IOException, JSONException {

	if (helper == null)
	    helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
	if (busRouteDao == null)
	    busRouteDao = helper.getBusRouteDao();
	if (busStopDao == null)
	    busStopDao = helper.getBusStopDao();

	Timetable timetable = new Timetable();

	String file = getFileFromServer(busStopUrl + busStop + ".json");

	JSONObject data = new JSONObject(file);
	JSONArray stopsArray = data.getJSONArray("stops");
	JSONObject routesObject = data.getJSONObject("routes");

	HashSet<BusRoute> busRoutes = new HashSet<BusRoute>();
	for (Iterator<String> keyIter = routesObject.keys(); keyIter.hasNext();) {
	    String key = keyIter.next();

	    Log.i(TAG, "Route Key: " + key);

	    BusRoute route = busRouteDao.queryForId(Integer.parseInt(key.substring(key.length() - 3, key.length())));

	    if (route != null) {
		busRoutes.add(route);
	    } else {
		throw new RuntimeException("Route not found " + key.substring(key.length() - 3, key.length()) + " " + key);
	    }
	}

	Log.i(TAG, "Number of entries " + data.length());

	Log.i(TAG, "Stops: " + data.getJSONArray("stops"));

	for (int stopNum = 0; stopNum < stopsArray.length(); stopNum++) {
	    JSONObject stopObj = stopsArray.getJSONObject(stopNum);

	    if (!keepNonUniLink && !stopObj.getString("name").startsWith("U")) {
		continue;
	    }

	    if (!keepUniLink && stopObj.getString("name").startsWith("U")) {
		continue;
	    }

	    BusStop busStopObj = busStopDao.queryForId(busStop);
	    if (busStopObj == null) {
		Log.e(TAG, "BusStopObj == null");
	    }

	    Stop stop = getStop(context, stopObj, busRoutes, busStopObj);

	    if (stop == null) {
		Log.w(TAG, "Null stop, skiping");
		continue;
	    }

	    Log.i(TAG, "Found stop for a unidentified " + stop.bus.toString() + " at " + stop.busStop.id + " at " + stop.arivalTime);

	    timetable.add(stop);
	}

	timetable.fetchTime = new Date(System.currentTimeMillis());
	return timetable;
    }

    public static Timetable getTimetable(Context context, Bus bus, BusStop startStop, int num) throws SQLException, ClientProtocolException, IOException,
	    JSONException {

	if (helper == null)
	    helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
	if (busRouteDao == null)
	    busRouteDao = helper.getBusRouteDao();
	if (busStopDao == null)
	    busStopDao = helper.getBusStopDao();

	Timetable timetable = new Timetable();

	List<BusStop> busStops = new ArrayList<BusStop>(num);
	busStops.add(startStop);

	BusRoute route = bus.route;

	for (int i = 0; i < num; i++) {
	    BusStop nextStop = route.moveInRoute(context, busStops.get(i), bus.direction, 1);

	    if (nextStop != null) {
		busStops.add(nextStop);
	    } else {
		Log.e(TAG, "nextStop is null");
	    }
	}

	for (BusStop busStop : busStops) {

	    String file = getFileFromServer(busStopUrl + busStop + ".json");

	    JSONObject data = new JSONObject(file);
	    JSONArray stopsArray = data.getJSONArray("stops");

	    HashSet<BusRoute> busRoutes = new HashSet<BusRoute>();
	    busRoutes.add(bus.route);

	    Log.i(TAG, "Number of entries " + data.length());

	    Log.i(TAG, "Stops: " + data.getJSONArray("stops"));

	    for (int stopNum = 0; stopNum < stopsArray.length(); stopNum++) {
		JSONObject stopObj = stopsArray.getJSONObject(stopNum);

		if (stopObj.getString("vehicle").equals(bus.id)) {

		    Stop stop = getStop(context, stopObj, busRoutes, busStop);

		    if (stop == null) {
			Log.w(TAG, "Null stop, skiping");
			continue;
		    }

		    Log.i(TAG, "Found stop for a unidentified " + stop.bus.toString() + " at " + stop.busStop.id + " at " + stop.arivalTime);

		    timetable.add(stop);

		}
	    }
	}

	timetable.fetchTime = new Date(System.currentTimeMillis());

	return timetable;
    }

    static PathOverlay getRoutePath(InputStream routeResource, int colour, ResourceProxy resProxy) {
	PathOverlay data = null;

	// sax stuff
	try {
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser sp = spf.newSAXParser();

	    XMLReader xr = sp.getXMLReader();

	    DataHandler dataHandler = new DataHandler(colour, resProxy);
	    xr.setContentHandler(dataHandler);

	    xr.parse(new InputSource(routeResource));

	    data = dataHandler.getData();

	} catch (ParserConfigurationException pce) {
	    Log.e("SAX XML", "sax parse error", pce);
	} catch (SAXException se) {
	    Log.e("SAX XML", "sax error", se);
	} catch (IOException ioe) {
	    Log.e("SAX XML", "sax parse io error", ioe);
	}

	return data;
    }

    public static String getFileFromServer(String request) throws ClientProtocolException, IOException {
	StringBuilder builder = new StringBuilder();
	HttpClient client = new DefaultHttpClient();
	HttpGet httpGet = new HttpGet(request);
	Log.i("Util.getFileFromServer", "Request used: " + request);

	HttpResponse response = client.execute(httpGet);
	StatusLine statusLine = response.getStatusLine();
	int statusCode = statusLine.getStatusCode();
	if (statusCode == 200) {
	    HttpEntity entity = response.getEntity();
	    InputStream content = entity.getContent();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	    String line;
	    while ((line = reader.readLine()) != null) {
		builder.append(line);
	    }
	} else {
	    Log.e("", "Failed to download file");
	}

	return builder.toString();
    }
}
