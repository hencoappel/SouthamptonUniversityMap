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


import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class FavouriteDialog extends Dialog {

    private static final String TAG = "FavouriteDialog";
    private ListView listItems;

    private final Context context;

    private final TextView message;

    protected POIArrayAdapter adapter;

    private ArrayList<POI> favouriteItems;

    public FavouriteDialog(Context context) {
	super(context);

	this.context = context;

	setContentView(R.layout.favourite_dialog);
	setTitle("Favourite Items");

	message = (TextView) findViewById(R.id.favouriteDialogMessage);

	favouriteItems = new ArrayList<POI>();

	listItems = (ListView) findViewById(R.id.favouriteListItems);

	refresh();
    }

    public void refresh() {

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {

	    Dao<Building, String> buildingDao = helper.getBuildingDao();
	    Dao<BusStop, String> busStopDao = helper.getBusStopDao();

	    final ArrayList<POI> newFavouriteItems = new ArrayList<POI>();

	    newFavouriteItems.addAll(buildingDao.queryForEq(POI.FAVOURITE_FIELD_NAME, true));
	    newFavouriteItems.addAll(busStopDao.queryForEq(POI.FAVOURITE_FIELD_NAME, true));

	    Log.i(TAG, "There are " + newFavouriteItems.size() + " favourites");
	    if (newFavouriteItems.size() == 0) {
		Log.i(TAG, "Favourite dialog has no favourites, displaying message");
		message.post(new Runnable() {
		    public void run() {
			message.setText(R.string.favourites_dialog_message);
			message.setVisibility(View.VISIBLE);
		    }
		});
		listItems.post(new Runnable() {
		    public void run() {
			listItems.setVisibility(View.GONE);
			adapter = null;
			favouriteItems.clear();
		    }
		});

	    } else {
		message.post(new Runnable() {
		    public void run() {
			message.setVisibility(View.GONE);
		    }
		});

		listItems.post(new Runnable() {
		    public void run() {
			favouriteItems = newFavouriteItems;
			adapter = new POIArrayAdapter(context, favouriteItems);

			listItems.setVisibility(View.VISIBLE);
			listItems.setAdapter(adapter);

		    }
		});

	    }
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    void setOnItemClickListener(OnItemClickListener item) {
	listItems.setOnItemClickListener(item);

    }

    void setOnItemLongClickListener(OnItemLongClickListener item) {
	listItems.setOnItemLongClickListener(item);
    }

}
