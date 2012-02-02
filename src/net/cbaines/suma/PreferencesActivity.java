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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity implements Preferences {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	final Editor editor = sharedPrefs.edit();
	if (!sharedPrefs.contains(GPS_ENABLED)) {
	    editor.putBoolean(GPS_ENABLED, GPS_ENABLED_BY_DEFAULT);
	}
	if (!sharedPrefs.contains(UNI_LINK_BUS_TIMES)) {
	    editor.putBoolean(UNI_LINK_BUS_TIMES, UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT);
	}
	if (!sharedPrefs.contains(NON_UNI_LINK_BUS_TIMES)) {
	    editor.putBoolean(NON_UNI_LINK_BUS_TIMES, NON_UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT);
	}
	if (!sharedPrefs.contains(NON_UNI_LINK_BUS_STOPS)) {
	    editor.putBoolean(NON_UNI_LINK_BUS_STOPS, NON_UNI_LINK_BUS_STOPS_ENABLED_BY_DEFAULT);
	}
	editor.commit();

	addPreferencesFromResource(R.xml.preferences);
    }
}