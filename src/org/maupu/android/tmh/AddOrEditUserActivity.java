package org.maupu.android.tmh;

import org.maupu.android.tmh.database.object.User;

import android.widget.ImageView;
import android.widget.TextView;

public class AddOrEditUserActivity extends AddOrEditActivity<User> {
	private ImageView imageViewIcon;
	private TextView textViewName;
	
	public AddOrEditUserActivity() {
		super("User edition", R.drawable.ic_stat_categories, R.layout.add_or_edit_user, new User());
	}

	@Override
	protected void initResources() {
		imageViewIcon = (ImageView)findViewById(R.id.icon);
		textViewName = (TextView)findViewById(R.id.name);
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

}
