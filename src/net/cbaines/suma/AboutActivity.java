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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener {

    static final int DONATE_DIALOG_ID = 0;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.about_dialog);

	ExpandableListView epView = (ExpandableListView) findViewById(R.id.helpExpandableListView);
	AboutListAdapter mAdapter = new AboutListAdapter(this);
	epView.setAdapter(mAdapter);

	Button donateButton = (Button) findViewById(R.id.donateButton);
	donateButton.setOnClickListener(this);

    }

    class AboutListAdapter extends BaseExpandableListAdapter {
	private String[] groups = { "Map", "Find", "Preferences", "Find my Location", "View", "About", "Favourites", "Licence", "Credits" };
	private LayoutInflater inflater;
	private Context cxt;

	public AboutListAdapter(Context cxt) {
	    inflater = LayoutInflater.from(cxt);
	    this.cxt = cxt;
	}

	public Object getChild(int groupPos, int childPos) {
	    if (groupPos == 0) {
		return cxt.getResources().getString(R.string.map_help_message);
	    } else if (groupPos == 1) {
		return cxt.getResources().getString(R.string.find_help_message);
	    } else if (groupPos == 1) {
		return cxt.getResources().getString(R.string.preferences_help_message);
	    } else if (groupPos == 2) {
		return cxt.getResources().getString(R.string.findmylocation_help_message);
	    } else if (groupPos == 3) {
		return cxt.getResources().getString(R.string.view_help_message);
	    } else if (groupPos == 4) {
		return cxt.getResources().getString(R.string.about_help_message);
	    } else if (groupPos == 5) {
		return cxt.getResources().getString(R.string.favourites_help_message);
	    } else if (groupPos == 6) {
		return cxt.getResources().getString(R.string.favourites_help_message);
	    } else if (groupPos == 7) {
		return cxt.getResources().getString(R.string.licence_help_message);
	    } else if (groupPos == 8) {
		return cxt.getResources().getString(R.string.credits_help_message);
	    }
	    return null;
	}

	public long getChildId(int groupPos, int childPos) {
	    return childPos;
	}

	public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView, ViewGroup parent) {
	    TextView tv = new TextView(cxt);
	    tv.setText(getChild(groupPos, childPos).toString());
	    return tv;
	}

	public int getChildrenCount(int groupPos) {
	    return 1;
	}

	public Object getGroup(int groupPos) {
	    return groups[groupPos];
	}

	public int getGroupCount() {
	    return groups.length;
	}

	public long getGroupId(int groupPos) {
	    return groupPos;
	}

	public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) {
	    View v = null;
	    if (convertView != null)
		v = convertView;
	    else
		v = inflater.inflate(R.layout.view_group_row, parent, false);
	    String gt = (String) getGroup(groupPos);
	    TextView colorGroup = (TextView) v.findViewById(R.id.childname);
	    if (gt != null)
		colorGroup.setText(gt);
	    return v;
	}

	public boolean hasStableIds() {
	    return true;
	}

	public boolean isChildSelectable(int groupPos, int childPos) {
	    return true;
	}

    }

    public void onClick(View arg0) {
	showDialog(DONATE_DIALOG_ID);
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
