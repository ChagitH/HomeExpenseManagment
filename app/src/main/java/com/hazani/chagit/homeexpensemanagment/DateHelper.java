package com.hazani.chagit.homeexpensemanagment;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by chagithazani on 1/28/16.
 */
public class DateHelper {
    /*
    will return the first of the month when added number of months to the sent date
     */
    //static private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
    //static private Calendar cal = new GregorianCalendar();
    //static private Calendar cal = Calendar.getInstance(Locale.getISOCountries());
    static private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    /*
    8 am the 1st of the month.
     */
    public static Date getFirstOfMonth(Date originalDate, int monthsToAdd){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        cal.setTime(originalDate);

//        cal.clear(Calendar.MINUTE);
//        cal.clear(Calendar.SECOND);
//        cal.clear(Calendar.HOUR_OF_DAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH,monthsToAdd);
        return cal.getTime();
    }

    public static Date getFirstOfMonthForFetchMethods(Date originalDate, int monthsToAdd){
        Date date = getFirstOfMonth(originalDate, monthsToAdd);
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        return cal.getTime();
    }


    public static Date addMonthSameDay(Date originalDate, int monthsToAdd){
        cal.setTime(originalDate);
//        cal.set(Calendar.HOUR, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 1);
        //cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH,monthsToAdd);
        return cal.getTime();
    }
    public static Date geDateAddMonth(Date originalDate, int monthsToAdd){
        cal.setTime(originalDate);
        cal.add(Calendar.MONTH, monthsToAdd);
        return cal.getTime();
    }

//    public static Date stringToDate(String dateStr){
//        cal.
//        cal.setTime(originalDate);
//        cal.add(Calendar.MONTH, 1);
//        return cal.getTime();
//    }

    private static DateFormat formatter;
    private static DateFormat getFormater(){
        if(formatter == null){
            formatter = new DateFormat();
        }
        return formatter;
    }
    private static String dateFormate(){
        return "mm-dd-yyyy";
    }

    public static Date getToday() {
        return new Date();
    }

    public static String dateToFormatedStringDDMMYYYY(Date date){
        return String.valueOf(DateFormat.format("dd-MM-yyyy", date));
    }

    public static String dateToFormatedStringMMYYYY(Date date){
        return String.valueOf(DateFormat.format("MM-yyyy", date));
    }

    public static boolean didMonthYearChangeSinceDate(Date lastUpdated) {
//        Date today = new Date();
//        cal.setTime(today);
//        int monthToday = cal.get(Calendar.MONTH);
//        int yearToday = cal.get(Calendar.YEAR);
//        cal.setTime(lastUpdated);
//        int monthLastUpdate = cal.get(Calendar.MONTH);
//        int yearLastUpdate = cal.get(Calendar.YEAR);
//        Log.i("%%%%%%%%%%%%%%%%%%%%%%", "monthToday = " + monthToday + " monthLastUpdate = " + monthLastUpdate);
//        return monthToday == monthLastUpdate && yearToday == yearLastUpdate ? false : true;
        return ! isSameMonthAndYear(new Date(), lastUpdated);
    }

    public static boolean isSameMonthAndYear(Date today, Date sinceWhenNotUpdated) {
        int todayMonth, lastUpdatedMonth, todayYear, lastUpdatedYear;
        cal.setTime(today);
        todayMonth = cal.get(Calendar.MONTH);
        todayYear = cal.get(Calendar.YEAR);
        cal.setTime(sinceWhenNotUpdated);
        lastUpdatedMonth = cal.get(Calendar.MONTH);
        lastUpdatedYear = cal.get(Calendar.YEAR);
        return todayMonth == lastUpdatedMonth && todayYear == lastUpdatedYear ? true : false;
    }
}
