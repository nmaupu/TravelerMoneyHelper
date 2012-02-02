package org.maupu.android.tmh.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.maupu.android.tmh.R;

import android.content.Context;

public final class Flag {
	private String country;
	private int drawableId;
	private static List<Map<String,?>> listFlags;

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
	
	public static List<Map<String, ?>> getAllFlags(Context ctx) {
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
		
		toMap(rfh.getRawFile());
		return listFlags;
	}
	
	private static void toMap(final List<Flag> flags) {
		listFlags = new ArrayList<Map<String,?>>();
		
		Iterator<Flag> it = flags.iterator();
		while(it.hasNext()) {
			Flag current = it.next();
			Map<String,Object> m = new HashMap<String, Object>();
			m.put("name", current.getCountry());
			m.put("icon", current.getDrawableId());
			listFlags.add(m);
		}
	}
}
