package org.maupu.android.tmh;

import org.maupu.android.tmh.database.object.Category;

import android.view.View;
import android.widget.TextView;

public class AddOrEditCategoryActivity extends AddOrEditActivity<Category> {
    private static final String TAG = AddOrEditCategoryActivity.class.getName();
    private TextView tvName;

    public AddOrEditCategoryActivity() {
        super(R.string.fragment_title_edition_category,
                R.layout.add_or_edit_category,
                new Category());
    }

    @Override
    public int whatIsMyDrawerIdentifier() {
        return super.DRAWER_ITEM_CATEGORIES;
    }

    @Override
    protected View initResources() {
        tvName = (TextView) findViewById(R.id.name);
        return tvName;
    }

    @Override
    protected void baseObjectToFields(Category obj) {
        if (obj == null) {
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
        return !"".equals(tvName.getText().toString().trim());
    }
}
