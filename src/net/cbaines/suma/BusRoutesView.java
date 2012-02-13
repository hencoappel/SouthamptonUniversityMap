package net.cbaines.suma;

import android.R.color;
import android.content.Context;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

public class BusRoutesView extends TableLayout {

    private TextView u1;
    private TextView u1n;
    private TextView u2;
    private TextView u6;
    private TextView u9;

    public BusRoutesView(Context context, final byte routes) {
	super(context);

	u1 = new TextView(context);
	u1.setText(R.string.U1);
	u1.setBackgroundResource(R.drawable.u1_back_selected);
	// /u1.setTextColor(color.white);

	u1n = new TextView(context);
	u1n.setText(R.string.U1N);
	u1n.setBackgroundResource(R.drawable.u1n_back_selected);
	// u1n.setTextColor(color.white);

	u2 = new TextView(context);
	u2.setText(R.string.U2);
	u2.setBackgroundResource(R.drawable.u2_back_selected);
	// u2.setTextColor(color.white);

	u6 = new TextView(context);
	u6.setText(R.string.U6);
	u6.setBackgroundResource(R.drawable.u6_back_selected);
	// u6.setTextColor(color.white);

	u9 = new TextView(context);
	u9.setText(R.string.U9);
	u9.setBackgroundResource(R.drawable.u9_back_selected);
	// u9.setTextColor(color.white);

    }

    void setRoutes(byte routes) {

	removeView(u1);
	removeView(u1n);
	removeView(u2);
	removeView(u6);
	removeView(u9);

	LayoutParams busRouteLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	if ((routes & (1 << 0)) != 0) {
	    addView(u1, busRouteLayoutParams);
	    u1.setVisibility(View.VISIBLE);
	}
	if ((routes & (1 << 1)) != 0) {
	    u1n.setVisibility(View.VISIBLE);
	    addView(u1n, busRouteLayoutParams);
	}
	if ((routes & (1 << 2)) != 0) {
	    u2.setVisibility(View.VISIBLE);
	    addView(u2, busRouteLayoutParams);
	}
	if ((routes & (1 << 3)) != 0) {
	    u6.setVisibility(View.VISIBLE);
	    addView(u6, busRouteLayoutParams);
	}
	if ((routes & (1 << 4)) != 0) {
	    u9.setVisibility(View.VISIBLE);
	    addView(u9, busRouteLayoutParams);
	}
    }
}
