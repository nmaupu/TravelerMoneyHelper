package org.maupu.android.tmh.database.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateUtil {
	
	/**
	 * Transform a date string from format %Y-%m-%d %H:%M:%S to a Date object 
	 * @param date
	 * @return a Date object or throw a ParseException
	 */
	public static Date StringToDate(String date) throws ParseException {
		if(date == null || "".equals(date))
			throw new ParseException("Date "+date+" is impossible to parse", 0);
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return sdf.parse(date);
	}
	
	public static String dateToStringNoHour(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return sdf.format(date);
	}
	
	public static String dateToString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return sdf.format(date);
	}
	
	public static Date getFirstDayOfMonth(Date date) {
		int year = date.getYear();
		int month = date.getMonth();
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1);

		return new Date(year, month, 1, 0, 0, 0);
	}
	
	public static Date getLastDayOfMonth(Date date) {
		int year = date.getYear();
		int month = date.getMonth();
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1);
		int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

		return new Date(year, month, maxDay, 23, 59, 59);
	}
}
