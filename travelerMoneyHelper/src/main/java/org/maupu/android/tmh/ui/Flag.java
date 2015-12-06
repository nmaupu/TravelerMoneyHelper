package org.maupu.android.tmh.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.maupu.android.tmh.R;

import android.content.Context;
import android.util.Log;

public final class Flag {
    private String isoCode;
	private String country;
	private static List<Flag> listFlags;

    public static final int ICON_VERY_SMALL_SIZE = 16;
    public static final int ICON_SMALL_SIZE = 16;
    public static final int ICON_NORMAL_SIZE = 32;
    public static final int ICON_LARGE_SIZE = 48;
    public static final int ICON_DEFAULT_SIZE = ICON_NORMAL_SIZE;

	public Flag(String isoCode, String country) {
        this.isoCode = isoCode;
		this.country = country;
	}

    public String getIsoCode() {
        return isoCode;
    }

    public String getCountry() {
		return country;
	}

	public Integer getDrawableId(int size) {
        try {
            Field field = R.drawable.class.getField("flag_" + getIsoCode() + "_" + size);
            field.setAccessible(true);
            return (Integer) field.get(new Integer(0));
        } catch (NoSuchFieldException nsfe) {
            return null;
        } catch (IllegalAccessException iae) {
            return null;
        }
	}

	public String toString() {
		return country;
	}

	public static List<Flag> getAllFlags(Context ctx) {
		if(listFlags != null && listFlags.size() != 0)
			return listFlags;

		listFlags = new ArrayList<Flag>();
		RawFileHelper<Flag> rfh = new RawFileHelper<Flag>(ctx, R.raw.iso_countries);
		rfh.setListener(new ICallback<Flag>() {
            @Override
            public Flag callback(Object item) {
                String line = (String) item;
                StringTokenizer tok = new StringTokenizer(line, RawFileHelper.FIELD_SEPARATOR);
                String isoCode = tok.nextToken();
                String countryName = tok.nextToken();

                Log.d(Flag.class.getName(), "new flag : " + isoCode + " -> " + countryName);

                return new Flag(isoCode, countryName);
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
			Flag currentFlag = it.next();
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
			Flag currentFlag = it.next();
			if(currentFlag.getCountry().equals(country.trim()))
				return currentFlag;
		}
		
		return null;
	}

    public static List<Map<String, ?>> getFlagsForAdapter(Context ctx) {
        List<Flag> list = getAllFlags(ctx);
        List<Map<String, ?>> ret = new ArrayList<Map<String, ?>>();

        Iterator<Flag> it = list.iterator();
        while(it.hasNext()) {
            Flag flag = it.next();
            Map<String, Object> elt = new HashMap<String, Object>();
            elt.put("icon", String.valueOf(flag.getDrawableId(32)));
            elt.put("name", flag.getCountry());
            ret.add(elt);
        }

        return ret;
    }
}
