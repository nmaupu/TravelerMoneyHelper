package org.maupu.android.tmh.ui;

import org.maupu.android.R;

import android.app.Activity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomTitleBar {
	private Activity activity;
	private TextView tvRight;
	private ImageView iconView;
	private boolean initialized = false;
	
	public CustomTitleBar(Activity activity) {
		this.activity = activity;
		activity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	}
	
	public void setName(String name) {
		initialize();
		if(tvRight == null)
			tvRight = (TextView)activity.findViewById(R.id.title_bar_tv_right);
		
		tvRight.setText(name);
	}
	
	public void setIcon(int icon) {
		initialize();
		if(iconView == null)
			iconView = (ImageView)activity.findViewById(R.id.title_bar_icon);
		
		iconView.setImageResource(icon);
	}
	
	private void initialize() {
		if(! initialized) {
			this.activity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);
			initialized = true;
		}
	}
}
