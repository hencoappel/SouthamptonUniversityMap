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

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class AboutActivity extends PreferenceActivity {

    static final int DONATE_DIALOG_ID = 0;

    private static final String KEY_ABOUT_LICENSE = "about_license";
    private static final String KEY_ABOUT_PROJECT = "about_project";
    private static final String KEY_ABOUT_DEVELOPER = "about_developer";
    private static final String KEY_ABOUT_DATA = "about_data";
    private static final String KEY_ABOUT_ANDROID_MARKET = "about_android_market";
    private static final String KEY_ABOUT_MAP_DATA = "about_map_data";
    private static final String KEY_ABOUT_MAP_ICONS = "about_map_icons";
    private static final String KEY_ABOUT_OSM_MAP = "about_osm_map";
    private static final String KEY_ABOUT_DATABASE = "about_database";
    private static final String KEY_ABOUT_DONATE = "about_donate";

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	addPreferencesFromResource(R.xml.about);
    }

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference) {
	final String key = preference.getKey();
	if (KEY_ABOUT_LICENSE.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gnu.org/licenses/gpl-2.0.html")));
	} else if (KEY_ABOUT_PROJECT.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cbaines/SouthamptonUniversityMap")));
	} else if (KEY_ABOUT_DEVELOPER.equals(key)) {
	    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "cbaines8@gmail.com" });
	    emailIntent.setType("text/plain");
	    
	    startActivity(Intent.createChooser(emailIntent, "Email the developer"));
	} else if (KEY_ABOUT_DATA.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://data.southampton.ac.uk/bus-routes.html")));
	} else if (KEY_ABOUT_ANDROID_MARKET.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("")));
	} else if (KEY_ABOUT_MAP_DATA.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://data.southampton.ac.uk/places.html"))));
	} else if (KEY_ABOUT_MAP_ICONS.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://mapicons.nicolasmollet.com/")));
	} else if (KEY_ABOUT_OSM_MAP.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.openstreetmap.org/")));
	} else if (KEY_ABOUT_DATABASE.equals(key)) {
	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://ormlite.com/"))));
	} else if (KEY_ABOUT_DONATE.equals(key)) {
	    showDialog(DONATE_DIALOG_ID);
	}

	return false;
    }

    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case DONATE_DIALOG_ID:
	    DonateDialog donateDialog = new DonateDialog(this);
	    return donateDialog;

	}
	return null;
    }
}
