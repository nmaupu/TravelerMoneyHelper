package org.maupu.android.tmh;

import org.maupu.android.tmh.database.object.Category;

import android.widget.TextView;

public class AddOrEditCategoryActivity extends AddOrEditActivity<Category> {
	private TextView tvName;
	
	public AddOrEditCategoryActivity() {
		super(R.string.activity_title_edition_category,  
				R.layout.add_or_edit_category, 
				new Category());
	}
	
	@Override
	protected void initResources() {
		tvName = (TextView)findViewById(R.id.name);
	}
	
	@Override
	protected void baseObjectToFields(Category obj) {
		if(obj == null) {
			tvName.setText("");
		} else {
			tvName.setText(obj.getName());
		}
	}

	@Override
	protected void fieldsToBaseObject(Category obj) {
		obj.setName(tvName.getText().toString().trim());
	}

	@Override
	protected boolean validate() {
		return ! "".equals(tvName.getText().toString().trim());
	}
}