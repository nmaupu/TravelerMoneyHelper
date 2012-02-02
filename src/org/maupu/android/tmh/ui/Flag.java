package org.maupu.android.tmh.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
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

	public String toString() {
		return country;
	}

	public static List<Flag> getAllFlags(Context ctx) {
		if(listFlags != null && listFlags.size() != 0)
			return listFlags;

		listFlags = new ArrayList<Flag>();
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

		return rfh.getRawFile();
	}

	public static String[] toStringArray(Context ctx) {
		List<Flag> list = getAllFlags(ctx);
		String[] ret = new String[list.size()];
		Iterator<Flag> it = list.iterator();

		int i=0;
		while (it.hasNext()) {
			Flag currentFlag = (Flag)it.next(); 
			ret[i++] = currentFlag.toString();
		}

		return ret;
	}

	public static Flag getFlagFromCountry(Context ctx, String country) {
		if(country == null || "".equals(country))
			return null;
		
		List<Flag> list = getAllFlags(ctx);

		Iterator<Flag> it = list.iterator();
		while (it.hasNext()) {
			Flag currentFlag = (Flag)it.next();
			if(currentFlag.getCountry().equals(country.trim()))
				return currentFlag;
		}
		
		return null;
	}
}
