package org.maupu.android.tmh;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

public class CategoryChooserActivity extends TmhActivity {
	private ListView list = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setActionBarContentView(R.layout.category_chooser_activity);
		setTitle(R.string.activity_title_category_chooser);
		
		populateWithData();
	}
	
	public void populateWithData() {
		if (list == null)
			list = (ListView)findViewById(R.id.list);
		Category cat = new Category();
		Cursor cursor = cat.fetchAll();
		CheckableCursorAdapter adapter = new CheckableCursorAdapter(this, 
				R.layout.category_item,
				cursor, 
				new String[]{CategoryData.KEY_NAME}, 
				new int[]{R.id.name});
		list.setAdapter(adapter);
	}

	@Override
	public void refreshDisplay() {
		populateWithData();
	}
}
