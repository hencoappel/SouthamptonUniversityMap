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

import java.io.InputStream;
import java.util.HashSet;

import android.content.Context;
import android.os.Bundle;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class BuildingActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    final static String TAG = "BusTimeActivity";

    private boolean dataChanged;

    private Context instance;

    private HashSet<BusRoute> routes = new HashSet<BusRoute>();

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.bustimes);

	final DatabaseHelper helper = getHelper();

	// create an empty model
	// Model model = ModelFactory.createDefaultModel();

	// use the FileManager to find the input file
	InputStream in = getResources().openRawResource(R.raw.u9);
	if (in == null) {
	    throw new IllegalArgumentException("File not found");
	}

	// read the RDF/XML file
	// model.read(in, null);

	instance = this;

    }

    public void onResume() {
	super.onResume();

    }

    public void onPause() {

	super.onPause();
    }

    public void finish() {
	setResult(RESULT_OK, getIntent());

	super.finish();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
	return null;
    }

}
