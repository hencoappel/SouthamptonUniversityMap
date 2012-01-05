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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DonateDialog extends Dialog implements Runnable {

    // private static final String TAG = "DonateDialog";

    private static final String bitcoinAddress = "1LFATViKkmbm6m4u1Ghi9wqrgVy2B6M412";

    private final Context context;

    private final TextView dialogMessage;
    private final ProgressBar progressBar;

    private final LinearLayout errorLayout;
    private final TextView donateDialogErrorMessage;
    private final TextView donateBitcoinAddress;

    public DonateDialog(Context context) {
	super(context);

	this.context = context;

	setContentView(R.layout.donate_dialog);
	setTitle("Donate");

	dialogMessage = (TextView) findViewById(R.id.donateDialogMessage);
	progressBar = (ProgressBar) findViewById(R.id.donateDialogProgress);

	errorLayout = (LinearLayout) findViewById(R.id.donateDialogMessageLayout);
	donateDialogErrorMessage = (TextView) findViewById(R.id.donateDialogErrorMessage);
	donateBitcoinAddress = (TextView) findViewById(R.id.donateBitcoinAddress);

	new Thread(this).start();
    }

    public void run() {
	try {
	    Thread.sleep(3000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	try {

	    Intent donateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + bitcoinAddress
		    + "?label=Southampton%20Uni%20Map%20App&message=Donation%20for%20the%20Southampton%20University%20Map%20App"));
	    context.startActivity(donateIntent);

	} catch (ActivityNotFoundException e) {
	    errorLayout.post(new Runnable() {
		public void run() {
		    dialogMessage.setText(R.string.donate_dialog_error_title);
		    progressBar.setVisibility(View.GONE);
		    errorLayout.setVisibility(View.VISIBLE);
		    donateDialogErrorMessage.setVisibility(View.VISIBLE);
		    donateBitcoinAddress.setText(bitcoinAddress);
		    donateBitcoinAddress.setVisibility(View.VISIBLE);
		}
	    });
	}
    }

}
