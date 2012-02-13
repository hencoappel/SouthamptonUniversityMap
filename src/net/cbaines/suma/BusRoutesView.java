package net.cbaines.suma;

import android.R.color;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class BusRoutesView extends LinearLayout {

    private TextView u1;
    private TextView u1n;
    private TextView u2;
    private TextView u6;
    private TextView u9;

    private LinearLayout bottomRow;
    private LinearLayout topRow;

    public BusRoutesView(Context context, final byte routes) {
	super(context);

	u1 = new TextView(context);
	u1.setText(R.string.U1);
	u1.setBackgroundResource(R.drawable.u1_back_selected);
	u1.setPadding(1, 1, 1, 1);

	u1n = new TextView(context);
	u1n.setText(R.string.U1N);
	u1n.setBackgroundResource(R.drawable.u1n_back_selected);
	u1n.setPadding(1, 1, 1, 1);

	u2 = new TextView(context);
	u2.setText(R.string.U2);
	u2.setBackgroundResource(R.drawable.u2_back_selected);
	u2.setPadding(1, 1, 1, 1);

	u6 = new TextView(context);
	u6.setText(R.string.U6);
	u6.setBackgroundResource(R.drawable.u6_back_selected);
	u6.setPadding(1, 1, 1, 1);

	u9 = new TextView(context);
	u9.setText(R.string.U9);
	u9.setBackgroundResource(R.drawable.u9_back_selected);
	u9.setPadding(1, 1, 1, 1);

	this.setOrientation(LinearLayout.VERTICAL);

	topRow = new LinearLayout(context);
	bottomRow = new LinearLayout(context);

	addView(topRow);
	addView(bottomRow);

    }

    void setRoutes(byte routes) {

	topRow.removeView(u1);
	topRow.removeView(u1n);
	topRow.removeView(u2);
	topRow.removeView(u6);
	topRow.removeView(u9);

	bottomRow.removeView(u1);
	bottomRow.removeView(u1n);
	bottomRow.removeView(u2);
	bottomRow.removeView(u6);
	bottomRow.removeView(u9);

	boolean top = true;

	LayoutParams busRouteLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	if ((routes & (1 << 0)) != 0) {
	    if (top) {
		topRow.addView(u1, busRouteLayoutParams);
	    } else {
		bottomRow.addView(u1, busRouteLayoutParams);
	    }
	    u1.setVisibility(View.VISIBLE);
	    top = !top;
	}
	if ((routes & (1 << 1)) != 0) {
	    if (top) {
		topRow.addView(u1n, busRouteLayoutParams);
	    } else {
		bottomRow.addView(u1n, busRouteLayoutParams);
	    }
	    u1n.setVisibility(View.VISIBLE);
	    top = !top;
	}
	if ((routes & (1 << 2)) != 0) {
	    if (top) {
		topRow.addView(u2, busRouteLayoutParams);
	    } else {
		bottomRow.addView(u2, busRouteLayoutParams);
	    }
	    u2.setVisibility(View.VISIBLE);
	    top = !top;
	}
	if ((routes & (1 << 3)) != 0) {
	    if (top) {
		topRow.addView(u6, busRouteLayoutParams);
	    } else {
		bottomRow.addView(u6, busRouteLayoutParams);
	    }
	    u6.setVisibility(View.VISIBLE);
	    top = !top;
	}
	if ((routes & (1 << 4)) != 0) {
	    if (top) {
		topRow.addView(u9, busRouteLayoutParams);
	    } else {
		bottomRow.addView(u9, busRouteLayoutParams);
	    }
	    u9.setVisibility(View.VISIBLE);
	    top = !top;
	}
    }
}
