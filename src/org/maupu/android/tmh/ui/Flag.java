package org.maupu.android.tmh.ui;

import java.lang.reflect.Field;
import java.util.List;

import org.maupu.android.tmh.R;

import android.content.Context;

public final class Flag {
	private String country;
	private int drawableId;
	private static List<Flag> listFlags;

	public Flag(String country, int drawableId) {
		this.country = country;
		this.drawableId = drawableId;
	}
	
	public String getCountry() {
		return country;
	}

	public int getDrawableId() {
		return drawableId;
	}
	
	public static List<Flag> getAllFlags(Context ctx) {
		if(listFlags != null)
			return listFlags;
		
		RawFileHelper<Flag> rfh = new RawFileHelper<Flag>(ctx, R.raw.flags);
		rfh.setCallback(new ICallback<Flag>() {
			@Override
			public Flag callback(Object item) {
				// Ugly
				try {
					String line = (String) item;
					Field field = R.drawable.class.getField(line);
					field.setAccessible(true);
					return new Flag(line, (Integer)field.get(new Integer(0)));
				} catch (NoSuchFieldException e) {
					return null;
				} catch (IllegalAccessException iae) {
					return null;
				}
			}
		});
		
		listFlags = rfh.getRawFile();
		return listFlags;
	}
}
