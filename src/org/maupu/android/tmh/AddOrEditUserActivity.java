package org.maupu.android.tmh;

import java.util.List;

import org.maupu.android.tmh.database.object.User;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddOrEditUserActivity extends AddOrEditActivity<User> {
	private ImageView imageViewIcon;
	private TextView textViewName;
	private GridView gridView;
	private List<ResolveInfo> mApps;
	private ImageView icon;

	public AddOrEditUserActivity() {
		super("User edition", R.drawable.ic_stat_categories, R.layout.add_or_edit_user, new User());
	}

	@Override
	protected void initResources() {
		imageViewIcon = (ImageView)findViewById(R.id.icon);
		textViewName = (TextView)findViewById(R.id.name);
		icon = (ImageView)findViewById(R.id.icon);
		icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createDialog().show();
			}
		});

		loadApps();
		//gridView = (GridView)findViewById(R.id.grid);
		//gridView.setAdapter(new AppsAdapter());
	}

	private void loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	private AlertDialog createDialog() {
		AlertDialog.Builder builder;
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_user_icon, (ViewGroup) findViewById(R.id.layout_root));



		GridView gridview = (GridView)layout.findViewById(R.id.gridview);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
				Toast.makeText(v.getContext(), "Position is "+position, 3000).show();
			}
		});
		gridview.setAdapter(new AppsAdapter());

		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);

		final AlertDialog dialog = builder.create();

		Button close = (Button)layout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		return dialog;
	}

	@Override
	protected boolean validate() {
		return !"".equals(textViewName.getText().toString().trim());
	}

	@Override
	protected void baseObjectToFields(User obj) {
		if(obj == null) {
			// TODO Display default icon
			textViewName.setText("");
		} else {
			// TODO Set icon
			textViewName.setText(obj.getName());
		}
	}

	@Override
	protected void fieldsToBaseObject(User obj) {
		if(obj != null) {
			// TODO Guess what ? set icon !
			obj.setName(textViewName.getText().toString().trim());
		}
	}

	public class AppsAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(AddOrEditUserActivity.this);

			ResolveInfo info = mApps.get(position % mApps.size());

			i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			final int w = (int) (36 * getResources().getDisplayMetrics().density + 0.5f);
			i.setLayoutParams(new GridView.LayoutParams(w, w));
			return i;
		}


		public final int getCount() {
			return Math.min(32, mApps.size());
		}

		public final Object getItem(int position) {
			return mApps.get(position % mApps.size());
		}

		public final long getItemId(int position) {
			return position;
		}
	}
}
