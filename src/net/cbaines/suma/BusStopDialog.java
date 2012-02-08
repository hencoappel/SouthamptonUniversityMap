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

public class BusStopDialog extends Dialog {

    private static final String TAG = "BusStopDialog";
    private ListView listItems;

    private final Context context;

    protected POIArrayAdapter adapter;

    private ArrayList<BusStop> busStops;

    public BusStopDialog(Context context) {
	super(context);

	this.context = context;

	setContentView(R.layout.bus_stop_dialog);
	setTitle("Favourite Items");

	busStops = new ArrayList<BusStop>();

	listItems = (ListView) findViewById(R.id.favouriteListItems);

	refresh();
    }

    public void refresh() {

	DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

	try {
	    
	    if (busStops.size() == 0) {
		Log.e(TAG, "Error");
	    } else {
		listItems.post(new Runnable() {
		    public void run() {
			adapter = new POIArrayAdapter(context, busStops);

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
