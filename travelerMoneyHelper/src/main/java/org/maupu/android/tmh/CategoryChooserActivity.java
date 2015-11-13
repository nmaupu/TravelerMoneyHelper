package org.maupu.android.tmh;

import java.util.HashMap;
import java.util.Map;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

@SuppressLint("UseSparseArrays")
public class CategoryChooserActivity extends TmhActivity {
	private ListView list = null;
	private CheckableCursorAdapter checkableCursorAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setActionBarContentView(R.layout.category_chooser_activity);
		setTitle(R.string.activity_title_category_chooser);
		
		refreshDisplay();
	}
	
	@Override
	protected void onDestroy() {
		closeCheckableCusorAdapterIdNeeded();
		
		super.onDestroy();
	}
	
	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		Category cat = new Category();
		Cursor cursor = cat.fetchAll();
		
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		results.put(0, cursor);
		
		return results;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {
		if (list == null)
			list = (ListView)findViewById(R.id.list);
		
		Cursor c = (Cursor)results.get(0);
		
		closeCheckableCusorAdapterIdNeeded();
		
		checkableCursorAdapter = new CheckableCursorAdapter(this, 
				R.layout.category_item,
				c, 
				new String[]{CategoryData.KEY_NAME}, 
				new int[]{R.id.name});
		list.setAdapter(checkableCursorAdapter);
	}
	
	private void closeCheckableCusorAdapterIdNeeded() {
		try {
			checkableCursorAdapter.getCursor().close();
		} catch(NullPointerException npe) {
			// Nothing to be done
		}
	}
}