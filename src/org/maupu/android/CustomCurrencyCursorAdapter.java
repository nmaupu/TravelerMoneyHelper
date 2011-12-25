package org.maupu.android;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;

public class CustomCurrencyCursorAdapter extends SimpleCursorAdapter implements OnCheckedChangeListener {
	private Context context;
	private int numberChecked = 0;

	public CustomCurrencyCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		
		CheckBox cb = (CheckBox)row.findViewById(R.id.checkbox);
		if(cb.getTag() == null) {
			cb.setOnCheckedChangeListener(this);
			cb.setTag("position="+position);
		}

		return row;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Log.d(CustomCurrencyCursorAdapter.class.getCanonicalName(), "Checked changed, id="+buttonView.getId()+", checked="+isChecked);

		//LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(isChecked)
			numberChecked++;
		else
			numberChecked--;

		//View currentPushUpView = (View)((Activity)context).findViewById(R.id.push_up_menu_root_layout);
		//View buttonDelete = (View)((Activity)context).findViewById(R.id.button_delete);
		Button buttonDelete = (Button)((Activity)context).findViewById(R.id.button_delete);
		Button buttonEdit = (Button)((Activity)context).findViewById(R.id.button_edit);
		//LinearLayout layout = (LinearLayout)((Activity)context).findViewById(R.id.currency_root_layout);

		if(numberChecked == 0) {
			//buttonDelete.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pushdown));
			//buttonDelete.setVisibility(View.GONE);
			buttonDelete.setEnabled(false);
			buttonEdit.setEnabled(false);
			//layout.removeView(buttonDelete);
		} else {
			// If one selected, edit enable
			// If > 1, disable edit
			if(numberChecked == 1) {
				buttonEdit.setEnabled(true);
			} else {
				buttonEdit.setEnabled(false);
			}
			
			// Adding a push up menu if not there
			//View v = inflater.inflate(R.layout.currency_push_up_menu, null);
			//layout.addView(v);
			//buttonDelete.setVisibility(View.VISIBLE);
			//buttonDelete.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pushup));
			if(! buttonDelete.isEnabled())
				buttonDelete.setEnabled(true);
		}

		/*
		View currentPushUpView = (View)((Activity)context).findViewById(R.id.push_up_menu_root_layout);
		LinearLayout layout = (LinearLayout)((Activity)context).findViewById(R.id.currency_root_layout);

		if(numberChecked == 0) {
			currentPushUpView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pushdown));
			currentPushUpView.setVisibility(View.GONE);
			layout.removeView(currentPushUpView);
		} else if(currentPushUpView == null) {
			// Adding a push up menu if not there
			View v = inflater.inflate(R.layout.currency_push_up_menu, null);
			layout.addView(v);
			v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pushup));
		}
		 */
	}
}
