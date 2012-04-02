package org.maupu.android.tmh.database.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class DateUtil {
	
	/**
	 * Transform a date string from format %d-%M-%y %H:%M:%S to a Date object 
	 * @param date
	 * @return a Date object or throw a ParseException
	 */
	public static Date StringToDate(String date) throws ParseException {
		if(date == null || "".equals(date))
			throw new ParseException("Date "+date+" is impossible to parse", 0);
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return sdf.parse(date);
	}
	
	/**
	 * Transform a SQL date string from format %y-%M-%d %H:%M:%S to a Date object 
	 * @param date
	 * @return a Date object or throw a ParseException
	 */
	public static Date StringSQLToDate(String date) throws ParseException {
		if(date == null || "".equals(date))
			throw new ParseException("Date "+date+" is impossible to parse", 0);
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);

		return new GregorianCalendar(year, month, 1, 0, 0, 0).getTime();
	}
	
	public static Date getLastDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

		return new GregorianCalendar(year, month, maxDay, 23, 59, 59).getTime();
	}
}
