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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_PATH = "/data/data/net.cbaines.suma/databases/";
    private static final String DATABASE_NAME = "data.db";

    private static final int DATABASE_VERSION = 36;

    private static final String TAG = "DatabaseHelper";

    // the DAO object we use to access the SimpleData table
    private Dao<Building, String> buildingDao = null;
    private Dao<BusStop, String> busStopDao = null;
    private Dao<BusRoute, Integer> busRouteDao = null;
    private Dao<RouteStops, Integer> routeStopsDao = null;
    private Dao<Site, String> siteDao = null;
    private Dao<Bus, Integer> busDao = null;
    private Dao<Stop, Integer> stopDao = null;

    private Context context;

    public DatabaseHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
	Log.i(TAG, "Database Helper created");
	this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
	try {
	    Log.i(DatabaseHelper.class.getName(), "onCreate");
	    TableUtils.createTable(connectionSource, Building.class);
	    TableUtils.createTable(connectionSource, BusStop.class);
	    TableUtils.createTable(connectionSource, BusRoute.class);
	    TableUtils.createTable(connectionSource, RouteStops.class);
	    TableUtils.createTable(connectionSource, Site.class);
	    TableUtils.createTable(connectionSource, Bus.class);
	    TableUtils.createTable(connectionSource, Stop.class);
	} catch (SQLException e) {
	    Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
	try {
	    Log.i(DatabaseHelper.class.getName(), "onUpgrade");
	    TableUtils.dropTable(connectionSource, Building.class, true);
	    TableUtils.dropTable(connectionSource, BusStop.class, true);
	    TableUtils.dropTable(connectionSource, BusRoute.class, true);
	    TableUtils.dropTable(connectionSource, RouteStops.class, true);
	    TableUtils.dropTable(connectionSource, Site.class, true);
	    TableUtils.dropTable(connectionSource, Bus.class, true);
	    TableUtils.dropTable(connectionSource, Stop.class, true);
	    // after we drop the old databases, we create the new ones
	    onCreate(database, connectionSource);
	} catch (SQLException e) {
	    Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
	    throw new RuntimeException(e);
	}

    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached value.
     */
    public Dao<Building, String> getBuildingDao() throws SQLException {
	if (buildingDao == null) {
	    buildingDao = getDao(Building.class);
	}
	return buildingDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached value.
     */
    public Dao<BusStop, String> getBusStopDao() throws SQLException {
	if (busStopDao == null) {
	    busStopDao = getDao(BusStop.class);
	}
	return busStopDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached value.
     */
    public Dao<BusRoute, Integer> getBusRouteDao() throws SQLException {
	if (busRouteDao == null) {
	    busRouteDao = getDao(BusRoute.class);
	}
	return busRouteDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached value.
     */
    public Dao<RouteStops, Integer> getRouteStopsDao() throws SQLException {
	if (routeStopsDao == null) {
	    routeStopsDao = getDao(RouteStops.class);
	}
	return routeStopsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached value.
     */
    public Dao<Site, String> getSiteDao() throws SQLException {
	if (siteDao == null) {
	    siteDao = getDao(Site.class);
	}
	return siteDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached value.
     */
    public Dao<Bus, Integer> getBusDao() throws SQLException {
	if (busDao == null) {
	    busDao = getDao(Bus.class);
	}
	return busDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached value.
     */
    public Dao<Stop, Integer> getStopDao() throws SQLException {
	if (stopDao == null) {
	    stopDao = getDao(Stop.class);
	}
	return stopDao;
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * 
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase() {
	Log.i(TAG, "Check database");

	/*
	 * SQLiteDatabase checkDB = null;
	 * 
	 * try { String myPath = DATABASE_PATH + DATABASE_NAME; checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY); } catch
	 * (SQLiteException e) {
	 * 
	 * // database does't exist yet.
	 * 
	 * }
	 * 
	 * if (checkDB != null) {
	 * 
	 * checkDB.close();
	 * 
	 * }
	 * 
	 * Log.i(TAG, "Finished checking database"); return checkDB != null ? true : false;
	 */

	File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
	return dbFile.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    public void copyDataBase() throws IOException {
	Log.i(TAG, "Begining copy database");

	// By calling this method and empty database will be created into the default system path
	// of your application so we are gonna be able to overwrite that database with our database.
	Log.i(TAG, "GetReadableDatabase");
	this.getWritableDatabase().close();

	InputStream myInput = context.getAssets().open(DATABASE_NAME);

	// Path to the just created empty db
	String outFileName = DATABASE_PATH + DATABASE_NAME;

	File database = new File(outFileName);
	if (database.exists()) {
	    database.delete();
	}

	// Open the empty db as the output stream
	OutputStream myOutput = new FileOutputStream(outFileName);

	// transfer bytes from the inputfile to the outputfile
	byte[] buffer = new byte[1024];
	int length;
	while ((length = myInput.read(buffer)) > 0) {
	    myOutput.write(buffer, 0, length);
	}

	// Close the streams
	myOutput.flush();
	myOutput.close();
	myInput.close();

	// getWritableDatabase().close();

	Log.i(TAG, "Finished copying db");

    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

	boolean dbExist = checkDataBase();

	if (dbExist) {
	    // do nothing - database already exist
	} else {

	    try {
		Log.i(TAG, "Copy database");
		copyDataBase();
	    } catch (IOException e) {
		throw new Error("Error copying database");
	    }
	}

    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
	super.close();
	buildingDao = null;
	busStopDao = null;
	busRouteDao = null;
	routeStopsDao = null;
	siteDao = null;
	busDao = null;
	stopDao = null;
    }
}