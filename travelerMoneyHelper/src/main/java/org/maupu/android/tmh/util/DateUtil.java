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
        int minDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, minDay);

        return resetDateToBeginingOfDay(cal.getTime());
	}
	
	public static Date getLastDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, maxDay);

        return resetDateToEndOfDay(cal.getTime());
	}

    public static Date addDays(Date date, int nbDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, nbDays);
        return cal.getTime();
    }
	
	public static int getNumberOfDaysBetweenDates(Date date1, Date date2) {
		Date d1 = (Date)date1.clone();
		Date d2 = (Date)date2.clone();
		
		d1 = DateUtil.resetDateToBeginingOfDay(d1);
		d2 = DateUtil.resetDateToEndOfDay(d2);
		
		return (int)Math.ceil(Math.abs(((double)(d1.getTime()-d2.getTime()) / 86400000d)));
	}
	
	public static Date resetDateToBeginingOfDay(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        int minH = cal.getActualMinimum(Calendar.HOUR);
        int minM = cal.getActualMinimum(Calendar.MINUTE);
        int minS = cal.getActualMinimum(Calendar.SECOND);

        cal.set(Calendar.HOUR, minH);
        cal.set(Calendar.MINUTE, minM);
        cal.set(Calendar.SECOND, minS);

        return cal.getTime();
	}
	
	public static Date resetDateToEndOfDay(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        int maxH = cal.getActualMaximum(Calendar.HOUR);
        int maxM = cal.getActualMaximum(Calendar.MINUTE);
        int maxS = cal.getActualMaximum(Calendar.SECOND);

        cal.set(Calendar.HOUR, maxH);
        cal.set(Calendar.MINUTE, maxM);
        cal.set(Calendar.SECOND, maxS);

        return cal.getTime();
	}

    public static Date getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new GregorianCalendar().getTime());
        return cal.getTime();
    }
}
