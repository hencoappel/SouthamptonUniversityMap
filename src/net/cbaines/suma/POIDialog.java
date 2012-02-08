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

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class POIDialog extends Dialog {

    private static final String TAG = "POIDialog";
    private ListView listItems;

    private final Context context;

    private final TextView message;

    protected POIArrayAdapter adapter;

    public POIDialog(Context context) {
	super(context);

	this.context = context;

	setContentView(R.layout.poi_dialog);

	message = (TextView) findViewById(R.id.favouriteDialogMessage);
	listItems = (ListView) findViewById(R.id.favouriteListItems);

    }

    void setMessage(final String text) {
	message.post(new Runnable() {
	    public void run() {
		if (text == null || text.length() == 0) {
		    message.setVisibility(View.GONE);
		} else {
		    message.setText(text);
		    message.setVisibility(View.VISIBLE);
		}
	    }
	});
    }

    void setItems(final List<POI> items) {
	listItems.post(new Runnable() {
	    public void run() {
		if (items != null) {
		    adapter = new POIArrayAdapter(context, items);

		    listItems.setVisibility(View.VISIBLE);
		    listItems.setAdapter(adapter);
		} else {
		    listItems.setVisibility(View.GONE);
		}
	    }
	});
    }

    void setOnItemClickListener(OnItemClickListener item) {
	listItems.setOnItemClickListener(item);

    }

    void setOnItemLongClickListener(OnItemLongClickListener item) {
	listItems.setOnItemLongClickListener(item);
    }

}
