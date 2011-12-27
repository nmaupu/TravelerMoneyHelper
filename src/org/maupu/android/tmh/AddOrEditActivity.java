package org.maupu.android.tmh;

import org.maupu.android.R;
import org.maupu.android.tmh.database.APersistedData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.CustomTitleBar;
import org.maupu.android.tmh.ui.SimpleDialog;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Class representing an Activity to add or edit objects
 * @author nmaupu
 *
 */
public abstract class AddOrEditActivity<T extends BaseObject> extends Activity implements OnClickListener {
	protected DatabaseHelper dbHelper = new DatabaseHelper(this);
	private Button buttonContinue;
	private Button buttonReset;
	private TextView textInfo;
	private int contentView;
	private T obj;
	private String title;
	private Integer icon;

	protected AddOrEditActivity(String title, Integer icon, int contentView, T obj) {
		this.contentView = contentView;
		this.obj = obj;
		this.title = title;
		this.icon = icon;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CustomTitleBar ctb = new CustomTitleBar(this);
		setContentView(contentView);
		ctb.setName(title);
		ctb.setIcon(icon);
		
		dbHelper.openWritable();

		buttonContinue = (Button)findViewById(R.id.button_continue);
		buttonReset = (Button)findViewById(R.id.button_reset);
		buttonContinue.setOnClickListener(this);
		buttonReset.setOnClickListener(this);
		textInfo = (TextView)findViewById(R.id.text_info);
		textInfo.setText("Please fill this form");
		textInfo.setVisibility(View.VISIBLE);

		initResources();
		fetchAndPreFill();
	}

	/**
	 * Get included object id for editing purpose
	 * @return itemId or null if nothing available
	 */
	protected Integer getItemIdToEdit() {
		try {
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			return bundle.getInt(APersistedData.KEY_ID);
		} catch (NullPointerException npe) {
			return null;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_continue:
			onContinue();
			break;
		case R.id.button_reset:
			baseObjectToFields(null);
			break;
		}
	}

	private void fetchAndPreFill() {
		Integer id = getItemIdToEdit();

		if(obj != null && id != null) {
			// Fetch
			Cursor c = obj.fetch(dbHelper, id);
			obj.toDTO(dbHelper, c);
		}

		// Fill
		baseObjectToFields(obj);
	}

	private void onContinue() {
		if(validate()) {
			fieldsToBaseObject(obj);

			if(obj.getId() != null)
				obj.update(dbHelper);
			else
				obj.insert(dbHelper);

			// Dispose this activity
			super.finish();
		} else {
			SimpleDialog.errorDialog(this, "Error", "Impossible to add object, error in fields !").show();
		}

	}

	/**
	 * Method called just after ui creation to finish initialization
	 */
	protected abstract void initResources();
	/**
	 * Method called to validate all field before updating or adding a baseObject
	 * @return
	 */
	protected abstract boolean validate();
	/**
	 * Method to fill all fields from a given object
	 * If parameter is null, all fields must be reset
	 * @param obj
	 */
	protected abstract void baseObjectToFields(T obj);
	/**
	 * Method to fill a baseObject from fields's content
	 * @param obj
	 */
	protected abstract void fieldsToBaseObject(T obj);
}
