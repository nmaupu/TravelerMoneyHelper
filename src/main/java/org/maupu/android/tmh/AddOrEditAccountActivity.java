package org.maupu.android.tmh;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.Flag;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.widget.SpinnerManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddOrEditAccountActivity extends AddOrEditActivity<Account> {
	private ImageView imageViewIcon;
	private TextView textViewName;
	private SpinnerManager spinnerCurrencyManager;
	private List<ResolveInfo> mApps;
	private ImageView icon;
	private String[] popupMenuIconNames;
	private static final int MENU_ITEM_APPS = 0;
	private static final int MENU_ITEM_URL = 1;
	private static final int MENU_ITEM_CAMERA = 2;
	private static final int MENU_ITEM_FLAGS = 3;
	private static final int MENU_ITEM_DEFAULT = 4;

	public AddOrEditAccountActivity() {
		super(R.string.activity_title_edition_account, R.layout.add_or_edit_account, new Account());
	}

	@Override
	protected void initResources() {
		imageViewIcon = (ImageView)findViewById(R.id.icon);
		textViewName = (TextView)findViewById(R.id.name);
		Spinner spinnerCurrency = (Spinner)findViewById(R.id.currency);
		//spinnerCurrencyManager = new SpinnerManager(spinnerCurrency);
		spinnerCurrencyManager = new SpinnerManager(this, spinnerCurrency);
		icon = (ImageView)findViewById(R.id.icon);
		icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createDialogMenu();
			}
		});
		
		// Setting spinner currency values
		Currency dummyCurrency = new Currency();
		Cursor c = dummyCurrency.fetchAll();
		//spinnerCurrency.setAdapter(SpinnerManager.createSpinnerCursorAdapter(this, c, CurrencyData.KEY_LONG_NAME));
		spinnerCurrencyManager.setAdapter(c, CurrencyData.KEY_LONG_NAME);
		
		popupMenuIconNames = new String[]{
				getString(R.string.popup_app_icon),
				getString(R.string.popup_url), 
				getString(R.string.popup_camera),
				getString(R.string.popup_flag_icon),
				getString(R.string.popup_default_icon)};

		loadApps();
	}

	private void loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	private void createDialogMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle(R.string.account_icon_edit_dialog_title);
        
        builder.setItems(popupMenuIconNames, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int item) {
            	Dialog d =  createDialogIconChooser(item);
            	
            	dialogInterface.dismiss();
            	
            	if(d != null)
            		d.show();
            }
        });
        
        builder.create().show();
	}
	
	private AlertDialog createDialogIconChooser(int dialogType) {
		Toast toast = Toast.makeText(this, getString(R.string.not_implemented), Toast.LENGTH_SHORT);
		switch(dialogType) {
		case MENU_ITEM_APPS:
			return createDialogFromApps();
		case MENU_ITEM_URL:
			toast.show();
			return null;
		case MENU_ITEM_CAMERA:
			toast.show();
			return null;
		case MENU_ITEM_FLAGS:
			return createDialogFromFlags();
		case MENU_ITEM_DEFAULT:
			imageViewIcon.setImageResource(R.drawable.icon_default);
			imageViewIcon.setScaleType(ScaleType.FIT_CENTER);
			return null;
		default:
			return null;
		}
	}
	
	private AlertDialog createDialogFromApps() {
		AlertDialog.Builder builder;
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_account_app_icon, (ViewGroup) findViewById(R.id.layout_root));
		
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);

		final AlertDialog dialog = builder.create();
		
		GridView gridview = (GridView)layout.findViewById(R.id.gridview);
		gridview.setAdapter(new AppsAdapter());
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
				ResolveInfo info = mApps.get(position % mApps.size());
				imageViewIcon.setImageDrawable(info.activityInfo.loadIcon(getPackageManager())); 
				dialog.dismiss();
			}
		});

		Button close = (Button)layout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		return dialog;
	}
	
	private AlertDialog createDialogFromFlags() {
		AlertDialog.Builder builder;
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_flags_icon, (ViewGroup) findViewById(R.id.layout_root));
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);

		final AlertDialog dialog = builder.create();
		
		//ListView list = (ListView)layout.findViewById(R.id.list);
		//FlagAdapter adapter = new FlagAdapter(this, Flag.getAllFlags(this), R.layout.icon_name_item_no_checkbox, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon});
		//list.setAdapter(adapter);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, Flag.toStringArray(this));
        final AutoCompleteTextView textView = (AutoCompleteTextView)layout.findViewById(R.id.edit);
        textView.setAdapter(adapter);

		Button cancel = (Button)layout.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		Button validate = (Button) layout.findViewById(R.id.validate);
		validate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Flag f = Flag.getFlagFromCountry(v.getContext(), textView.getText().toString());
				if(f != null) {
					// Resize flag
					Bitmap bmp = BitmapFactory.decodeResource(getResources(), f.getDrawableId());
					final int w = (int) (65 * getResources().getDisplayMetrics().density + 0.5f);
				    Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, w, w, true);
					
				    // Set it
				    imageViewIcon.setImageBitmap(resizedbitmap);
				    imageViewIcon.setScaleType(ScaleType.FIT_CENTER);
				    
					dialog.dismiss();
				} else {
					SimpleDialog.errorDialog(v.getContext(), getString(R.string.error), getString(R.string.country_not_found)).show();
				}
			}
		});

		return dialog;
	}

	@Override
	protected boolean validate() {
		return !"".equals(textViewName.getText().toString().trim());
	}

	@Override
	protected void baseObjectToFields(Account obj) {
		if(obj == null) {
			textViewName.setText("");
			imageViewIcon.setImageResource(R.drawable.icon_default);
		} else {
			textViewName.setText(obj.getName());
			
			// Loading icon
			String filename = obj.getIcon();
			ImageViewHelper.setIcon(this, imageViewIcon, filename);
			
			if(obj.getCurrency() !=null)
				spinnerCurrencyManager.setSpinnerPositionCursor(obj.getCurrency().getLongName(), new Currency());
		}
	}

	@Override
	protected void fieldsToBaseObject(Account obj) {
		if(obj != null) {
			String account = textViewName.getText().toString().trim();
			Bitmap b = ((BitmapDrawable)imageViewIcon.getDrawable()).getBitmap();
			String filename = account+".png";
			
			try {
				FileOutputStream fOut = openFileOutput(filename, Context.MODE_PRIVATE);
				
				b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
				fOut.close();
				
				obj.setIcon(filename);
			} catch (IOException ioe) {}
			
			obj.setName(account);
			
			Cursor c = spinnerCurrencyManager.getSelectedItem();
			Currency cur = new Currency();
			cur.toDTO(c);
			obj.setCurrency(cur);
		}
	}

	class AppsAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(AddOrEditAccountActivity.this);

			ResolveInfo info = mApps.get(position % mApps.size());

			i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			final int w = (int) (36 * getResources().getDisplayMetrics().density + 0.5f);
			i.setLayoutParams(new GridView.LayoutParams(w, w));
			return i;
		}


		public final int getCount() {
			return mApps.size();
		}

		public final Object getItem(int position) {
			return mApps.get(position % mApps.size());
		}

		public final long getItemId(int position) {
			return position;
		}
	}
}
