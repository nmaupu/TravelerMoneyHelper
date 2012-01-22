package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.CustomTitleBar;
import org.maupu.android.tmh.ui.SimpleDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Class representing an Activity to add or edit objects
 * @author nmaupu
 *
 */
public abstract class AddOrEditActivity<T extends BaseObject> extends TmhActivity implements OnClickListener {
	public static final String EXTRA_OBJECT_ID = "base_object";
	private Button buttonContinue;
	private Button buttonReset;
	//private TextView textInfo;
	private int contentView;
	private T obj;
	private int title;

	private Integer icon;

	public AddOrEditActivity(int title, Integer icon, int contentView, T obj) {
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
		super.setTitle(getString(title));
		ctb.setName(getString(title));
		ctb.setIcon(icon);

		buttonContinue = (Button)findViewById(R.id.button_continue);
		buttonReset = (Button)findViewById(R.id.button_reset);
		buttonContinue.setOnClickListener(this);
		buttonReset.setOnClickListener(this);
		/*textInfo = (TextView)findViewById(R.id.text_info);
		if(textInfo != null) {
			textInfo.setText("Please fill this form");
			textInfo.setVisibility(View.VISIBLE);
		}*/

		// Init all widgets
		initResources();
		// Retrieve extra parameter
		retrieveItemFromExtra();
		// Fill form fields
		baseObjectToFields(obj);
	}

	@SuppressWarnings("unchecked")
	private void retrieveItemFromExtra() {
		try {
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			obj = (T) bundle.get(AddOrEditActivity.EXTRA_OBJECT_ID);
		} catch (NullPointerException e) {
			// Here, nothing is allocated, we keep default obj
		} catch (ClassCastException cce) {
			// This exception should not be thrown
			throw cce;
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
			SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_add_object)).show();
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
	
	public void refreshDisplay(DatabaseHelper dbHelper){};
}
