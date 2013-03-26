package org.maupu.android.tmh.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class DateUtil {
	private static final String SDF_FORMAT = "dd-MM-yyyy HH:mm:ss";
	private static final String SDF_FORMAT_TO_DATE = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Transform a date string from format %d-%M-%y %H:%M:%S to a Date object 
	 * @param date
	 * @return a Date object or throw a ParseException
	 */
	public static Date StringToDate(String date) throws ParseException {
		if(date == null || "".equals(date))
			throw new ParseException("Date "+date+" is impossible to parse", 0);
		
		
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.SDF_FORMAT);
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
		
		
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.SDF_FORMAT_TO_DATE);
		return sdf.parse(date);
	}
	
	public static String dateToStringNoTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return sdf.format(date);
	}
	
	public static String dateToStringOnlyTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(date);
	}
	
	public static String dateToString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.SDF_FORMAT);
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
	
	public static int getNumberOfDaysBetweenDates(Date date1, Date date2) {
		Date d1 = (Date)date1.clone();
		Date d2 = (Date)date2.clone();
		
		d1 = DateUtil.resetDateToBeginingOfDay(d1);
		d2 = DateUtil.resetDateToEndOfDay(d2);
		
		return (int)Math.abs(Math.ceil((double) ( (double)(d1.getTime()-d2.getTime()) / 86400000d)));
	}
	
	public static Date resetDateToBeginingOfDay(Date d) {
		d.setHours(0);
		d.setMinutes(0);
		d.setSeconds(0);
		
		return d;
	}
	
	public static Date resetDateToEndOfDay(Date d) {
		d.setHours(23);
		d.setMinutes(59);
		d.setSeconds(59);
		
		return d;
	}
}
