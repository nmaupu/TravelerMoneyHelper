package org.maupu.android;

import org.maupu.android.database.CategoryData;
import org.maupu.android.database.DatabaseHelper;
import org.maupu.android.database.ExpenseData;
import org.maupu.android.database.object.Category;
import org.maupu.android.ui.CustomTitleBar;
import org.maupu.android.ui.SimpleDialog;
import org.maupu.android.ui.SimpleEditDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class ManageCategoryActivity extends Activity implements OnClickListener {
	private Spinner spinnerCategories;
	private DatabaseHelper dbHelper = new DatabaseHelper(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.manage_category);
		
		customTB.setName("Categories");
		customTB.setIcon(R.drawable.ic_stat_categories);

		ImageButton buttonEdit = (ImageButton)this.findViewById(R.id.button_edit);
		buttonEdit.setOnClickListener(this);
		ImageButton buttonDelete = (ImageButton)this.findViewById(R.id.button_delete);
		buttonDelete.setOnClickListener(this);
		ImageButton buttonValidate = (ImageButton)this.findViewById(R.id.button_validate);
		buttonValidate.setOnClickListener(this);
		spinnerCategories = (Spinner)findViewById(R.id.spinner_category);

		dbHelper.openWritable();
		fillSpinnerCategories();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_edit:
			editCategory();
			break;

		case R.id.button_delete:
			deleteCategory();
			break;

		case R.id.button_validate:
			addCategory();
			break;
		}
	}

	private void fillSpinnerCategories() {
		Cursor c = new Category().fetchAll(dbHelper);
		startManagingCursor(c);

		String[] from = new String[] {CategoryData.KEY_NAME};
		int[] to = new int[] {android.R.id.text1};

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, from, to);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerCategories.setAdapter(adapter);
	}
	
	private void addCategory() {
		EditText editTextNewName = (EditText)findViewById(R.id.edit_name);
		Category catToAdd = new Category();
		catToAdd.setName(editTextNewName.getText().toString().trim());
		
		if(catToAdd.insert(dbHelper)) {
			editTextNewName.setText("");
			fillSpinnerCategories();
		} else {
			SimpleDialog.errorDialog(this, "Error", "Category is not valid !").show();
		}
	}
	
	private void deleteCategory() {
		Cursor c = (Cursor)spinnerCategories.getSelectedItem();
		final Category catToDelete = new Category();
		catToDelete.toDTO(dbHelper, c);

		// Verify for category linked to expenses
		Cursor tmpC = dbHelper.getDb().query(ExpenseData.TABLE_NAME, 
				new String[]{ExpenseData.KEY_ID}, 
				ExpenseData.KEY_ID_CATEGORY+"="+catToDelete.getId(), null, null, null, null);
		if(tmpC.getCount() > 0) {
			SimpleDialog.errorDialog(this, "Error", "One or more expenses use this category ["+catToDelete.getName()+"] !").show();
		} else {
			SimpleDialog.confirmDialog(this, "Are you sure you want to delete "+catToDelete.getName()+" ?", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					catToDelete.delete(dbHelper);
					fillSpinnerCategories();
					dialog.dismiss();
				}
			}).show();
		}
	}
	
	public void editCategory() {
		Cursor c = (Cursor)spinnerCategories.getSelectedItem();
		final Category cat = new Category();
		cat.toDTO(dbHelper, c);

		final SimpleEditDialog dialog = new SimpleEditDialog(this, 
				"Edit category", "Enter new name :", 
				cat.getName(), (ViewGroup)findViewById(R.layout.manage_category));
		dialog.show();
		
		final Context ctx = this;
		
		dialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int buttonOk = dialog.getButtonOkId();
				int buttonCancel = dialog.getButtonCancelId();

				if(v.getId() == buttonOk) {
					cat.setName(dialog.getText().trim());
					if(cat.validate()) {
						cat.update(dbHelper);
						fillSpinnerCategories();
						dialog.dismiss();
					} else {
						SimpleDialog.errorDialog(ctx, "Error", "Cannot update this category").show();
					}
				} else if(v.getId() == buttonCancel){
					dialog.cancel();
				}
			}
		});
	}
}
