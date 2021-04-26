package com.lifeshare.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateTimeHelper {


    public final static String FILE_NAME_DATETIME_FORMAT = "ddMMyyyy_hhmmss";
    private final static String SERVER_DATE_FORMAT = "yyyy-MM-dd";
    private final static String SERVER_TIME_FORMAT = "HH:mm:ss";
    private final static String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final static String APP_DATE_FORMAT = "dd/MM/yyyy";
    private final static String APP_TIME_FORMAT = "hh:mm a";
    private final static String APP_DATE_TIME_FORMAT = "dd MMM yyyy hh:mm a";
    private final static String SERVER_DATE_YEAR_MONTH_FORMAT = "yyyy-MM";
    private final static String SERVER_DATE_MONTH_YEAR_FORMAT = "MM-yyyy";
    private final static String DATE_PICKER_SEPERATOR = "/";
    private final static String TIME_PICKER_SEPERATOR = ":";
    private final static String DATE_PICKER_FORMAT = "dd/MM/yyyy";
    private final static String TIME_PICKER_FORMAT = "HH:mm";
    private final static String JOINED_DATE_DISPLAY_MONTH_FORMATE = "MMM";
    private final static String JOINED_DATE_DISPLAY_DATE_FORMATE = "dd";
    private final static String YEAR_FORMAT = "yyyy";
    private final static TimeZone APP_TIME_ZONE = TimeZone.getDefault();

    private final static String DISPLAY_WEEKDAY_AND_TIME = "EEE hh:mm a";
    private final static String DISPLAY_FORMAT_DATE = "EEEE, MMMM dd, yyyy";
    private final static String DISPLAY_FORMAT_MONTH = "MMMM yyyy";
    private final static String DISPLAY_FORMAT_YEAR = "yyyy";
    private static final String TAG = "DateTimeHelper";
    private static DateTimeHelper instance;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;


    public String getTimeAgo(String str) {

        SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.ENGLISH);
        input.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        Date outputDate = (new Date(calendar.getTimeInMillis()));
        String todayDate = input.format(outputDate);
        long time = 0;
        long now = 0;
        try {
            time = input.parse(str.trim()).getTime();
            now = input.parse(todayDate.trim()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (time > now || time <= 0) {
            return null;
        }

        long diff = now - time;

        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diff);
            if (diffInDays / 365 > 0) {
                return diffInDays / 365 + " years ago";
            } else if (diffInDays / 30 > 0) {
                return diffInDays / 30 + " months ago";
            } else {
                return diffInDays + " days ago";
            }
        }
    }

    public static DateTimeHelper getInstance() {
        if (instance == null) {
            instance = new DateTimeHelper();
        }
        return instance;
    }

    public static String getJoinedDateMonthFormate(String serverDate) {
        String formatedDate;
        try {

            SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            SimpleDateFormat df = new SimpleDateFormat(JOINED_DATE_DISPLAY_MONTH_FORMATE, Locale.ENGLISH);
            Date date = input.parse(serverDate.trim());
            formatedDate = df.format(date);
        } catch (Exception e) {
            return serverDate;
        }
        return formatedDate;
    }

    public static String getJoinedDateInYearFormate(String serverDate) {
        String formatedDate;
        try {

            SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            SimpleDateFormat df = new SimpleDateFormat(JOINED_DATE_DISPLAY_DATE_FORMATE, Locale.ENGLISH);
            Date date = input.parse(serverDate.trim());
            formatedDate = df.format(date);
        } catch (Exception e) {
            return serverDate;
        }
        return formatedDate;
    }

    public String getWeekDays(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);

        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

// get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        String startDate = String.valueOf(cal.get(Calendar.DATE));
        String startMonth = getMonthForInt(cal.get(Calendar.MONTH));
        String startYear = String.valueOf(cal.get(Calendar.YEAR));
        System.out.println("Start of this week:       " + cal.getTime());
        System.out.println("... in milliseconds:      " + cal.getTimeInMillis());

        cal.add(Calendar.DATE, 6);
        String endDate = String.valueOf(cal.get(Calendar.DATE));
        String endMonth = getMonthForInt(cal.get(Calendar.MONTH));
        String endYear = String.valueOf(cal.get(Calendar.YEAR));

        String weekString = "";
        if (endYear.equals(startYear)) {
            weekString = startMonth + " " + startDate + " - " + endMonth + " " + endDate + ", " + startYear;
        } else {
            weekString = startMonth + " " + startDate + ", " + startYear + " - " + endMonth + " " + endDate + ", " + endYear;

        }

        return weekString;
    }

    public String getLastDayDateOfTheWeek(long timeStamp) {

        Calendar cal = Calendar.getInstance();
        Log.v(TAG, "getLastDayDateOfTheWeek: " + cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Log.v(TAG, "getLastDayDateOfTheWeek1: " + cal.get(Calendar.DATE));

        cal.setTimeInMillis(timeStamp);

        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

// get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        String startDate = String.valueOf(cal.get(Calendar.DATE));
        String startMonth = getMonthForInt(cal.get(Calendar.MONTH));
        String startYear = String.valueOf(cal.get(Calendar.YEAR));
        System.out.println("Start of this week:       " + cal.getTime());
        System.out.println("... in milliseconds:      " + cal.getTimeInMillis());
        cal.add(Calendar.DATE, 6);
        String endDate = String.valueOf(cal.get(Calendar.DATE));
        String endMonth = getMonthForInt(cal.get(Calendar.MONTH));
        String endYear = String.valueOf(cal.get(Calendar.YEAR));
        return getAppDateFormat(cal.getTimeInMillis());

    }

    String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }

    public String getServerDateToAppDate(String serverDate) {
        String formatedDate;
        try {

            SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            SimpleDateFormat df = new SimpleDateFormat(APP_DATE_FORMAT, Locale.ENGLISH);
            Date date = input.parse(serverDate.trim());
            formatedDate = df.format(date);
        } catch (Exception e) {
            return serverDate;
        }
        return formatedDate;
    }

    public long getServerDateTimeToAppDateTimeInLong(String serverDateTime) {
        try {
            SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.ENGLISH);
            return input.parse(serverDateTime.trim()).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getServerDateTimeToAppDateTime(String serverDateTime) {
        String formatedDate;
        try {

            SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.ENGLISH);
            SimpleDateFormat df = new SimpleDateFormat(APP_DATE_TIME_FORMAT, Locale.ENGLISH);
            Date date = input.parse(serverDateTime.trim());
            formatedDate = df.format(date);
        } catch (Exception e) {
            return serverDateTime;
        }
        return formatedDate;
    }

    public String getServerTimeToAppTime(String serverTime) {
        String formatedDate;
        try {

            SimpleDateFormat input = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH);
            SimpleDateFormat df = new SimpleDateFormat(APP_TIME_FORMAT, Locale.ENGLISH);
            Date date = input.parse(serverTime.trim());
            formatedDate = df.format(date);
        } catch (Exception e) {
            return serverTime;
        }
        return formatedDate;
    }

    public String getAppDateToServerDate(String appDate) {
        String formatedDate;
        try {

            SimpleDateFormat input = new SimpleDateFormat(APP_DATE_FORMAT, Locale.ENGLISH);
            SimpleDateFormat df = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            Date date = input.parse(appDate.trim());
            formatedDate = df.format(date);
        } catch (Exception e) {
            return appDate;
        }
        return formatedDate;
    }

    public String getFormattedDate(String format, long date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return simpleDateFormat.format(date);
    }

    public String getDatePickerDate(int year, int month, int dayOfMonth) {
        StringBuilder outputDate = new StringBuilder();

        if (dayOfMonth < 9) {
            outputDate.append("0").append(dayOfMonth);
        } else {
            outputDate.append(dayOfMonth);
        }

        outputDate.append(DATE_PICKER_SEPERATOR);

        if (month + 1 < 9) {
            outputDate.append("0").append(month + 1);
        } else {
            outputDate.append(month + 1);
        }

        outputDate.append(DATE_PICKER_SEPERATOR).append(year);

        return outputDate.toString();
    }

    public String getTimePickerTime(int hourOfDay, int minute) {

        return String.valueOf(hourOfDay) + TIME_PICKER_SEPERATOR + minute;
    }

    public String getAppDateFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(APP_DATE_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getServerDateFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getDisplayDateFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(DISPLAY_FORMAT_DATE, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getDisplayMonthFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(DISPLAY_FORMAT_MONTH, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getDisplayYearFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(DISPLAY_FORMAT_YEAR, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }


    public String getCreatedDateFormat() {
        try {
            DateFormat sdf = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(getCurrentTimeStamp()));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getAppTimeFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(APP_TIME_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getServerTimeFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getAppDateTimeFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(APP_DATE_TIME_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getServerDateTimeFormat(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public long getCurrentTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(APP_TIME_ZONE);
        return calendar.getTimeInMillis();
    }

    public String getDatePickerFormatToAppDateFormat(String date) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat(DATE_PICKER_FORMAT, Locale.ENGLISH);
        datePickerFormat.setTimeZone(APP_TIME_ZONE);

        Date inputDate;

        try {
            inputDate = datePickerFormat.parse(date);
        } catch (ParseException e) {
            return date;
        }

        SimpleDateFormat appFormatter = new SimpleDateFormat(APP_DATE_FORMAT, Locale.ENGLISH);
        appFormatter.setTimeZone(APP_TIME_ZONE);

        return appFormatter.format(inputDate);
    }

    public String getDatePickerFormatToServerDateFormat(String date) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat(DATE_PICKER_FORMAT, Locale.ENGLISH);
        datePickerFormat.setTimeZone(APP_TIME_ZONE);

        Date inputDate;

        try {
            inputDate = datePickerFormat.parse(date);
        } catch (ParseException e) {
            return date;
        }

        SimpleDateFormat appFormatter = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
        appFormatter.setTimeZone(APP_TIME_ZONE);

        return appFormatter.format(inputDate);
    }

    public String getTimePickerFormatToAppTimeFormat(String time) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat(TIME_PICKER_FORMAT, Locale.ENGLISH);
        datePickerFormat.setTimeZone(APP_TIME_ZONE);

        Date inputTime;

        try {
            inputTime = datePickerFormat.parse(time);
        } catch (ParseException e) {
            return time;
        }

        SimpleDateFormat appFormatter = new SimpleDateFormat(APP_TIME_FORMAT, Locale.ENGLISH);
        appFormatter.setTimeZone(APP_TIME_ZONE);

        return appFormatter.format(inputTime);
    }

    public String getTimePickerFormatToServerTimeFormat(String time) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat(TIME_PICKER_FORMAT, Locale.ENGLISH);
        datePickerFormat.setTimeZone(APP_TIME_ZONE);

        Date inputTime;

        try {
            inputTime = datePickerFormat.parse(time);
        } catch (ParseException e) {
            return time;
        }

        SimpleDateFormat appFormatter = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH);
        appFormatter.setTimeZone(APP_TIME_ZONE);

        return appFormatter.format(inputTime);
    }

    public long getTimeInMilliesFromServerDate(String serverDate) {
        try {
            DateFormat formatter = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            Date date = formatter.parse(serverDate);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long getTimeInMilliesFromServerTime(String serverTime) {
        try {
            DateFormat formatter = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH);
            Date date = formatter.parse(serverTime);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getCurrentDateInAppFormate() {
        DateFormat dfDate = new SimpleDateFormat(APP_DATE_FORMAT, Locale.ENGLISH);
        return dfDate.format(Calendar.getInstance().getTime());
    }

    public String getCurrentDateInServerFormate() {
        DateFormat dfDate = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
        return dfDate.format(Calendar.getInstance().getTime());
    }

    public String getCurrentYear() {
        DateFormat dfDate = new SimpleDateFormat(YEAR_FORMAT, Locale.ENGLISH);
        return dfDate.format(Calendar.getInstance().getTime());
    }

    public String getYearMonthFromServerDate(String serverDate) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
        datePickerFormat.setTimeZone(APP_TIME_ZONE);

        Date inputDate;

        try {
            inputDate = datePickerFormat.parse(serverDate);
        } catch (ParseException e) {
            return serverDate;
        }

        SimpleDateFormat appFormatter = new SimpleDateFormat(SERVER_DATE_YEAR_MONTH_FORMAT, Locale.ENGLISH);
        appFormatter.setTimeZone(APP_TIME_ZONE);

        return appFormatter.format(inputDate);
    }

    public String getMonthYearFromServerDate(String serverDate) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
        datePickerFormat.setTimeZone(APP_TIME_ZONE);

        Date inputDate;

        try {
            inputDate = datePickerFormat.parse(serverDate);
        } catch (ParseException e) {
            return serverDate;
        }

        SimpleDateFormat appFormatter = new SimpleDateFormat(SERVER_DATE_MONTH_YEAR_FORMAT, Locale.ENGLISH);
        appFormatter.setTimeZone(APP_TIME_ZONE);

        return appFormatter.format(inputDate);
    }

    public Date getServerDateFromDateAndTime(String serverDate, String serverTime) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
        datePickerFormat.setTimeZone(APP_TIME_ZONE);

        try {
            return datePickerFormat.parse(serverDate + " " + serverTime);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public boolean isTimeBetweenTwoTime(String startTime, String endTime, String selectedTime) {

        try {


            Date startTimeDate = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH).parse(startTime);
            Calendar startTimeCal = Calendar.getInstance();
            startTimeCal.setTime(startTimeDate);
            startTimeCal.add(Calendar.DATE, 1);

            Date endTimeDate = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH).parse(endTime);
            Calendar endTimeCal = Calendar.getInstance();
            endTimeCal.setTime(endTimeDate);
            endTimeCal.add(Calendar.DATE, 1);

            Date selectedTimeDate = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH).parse(selectedTime);
            Calendar selectedTimeCal = Calendar.getInstance();
            selectedTimeCal.setTime(selectedTimeDate);
            selectedTimeCal.add(Calendar.DATE, 1);

            if (startTimeCal.after(endTimeCal)) {
                endTimeCal.add(Calendar.DATE, 1);

                Date dayStartTimeDate = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse("00:00:00");
                Calendar dayStartTimeCal = Calendar.getInstance();
                dayStartTimeCal.setTime(dayStartTimeDate);
                dayStartTimeCal.add(Calendar.DATE, 2);

                if (selectedTimeCal.after(startTimeCal)
                        && selectedTimeCal.before(endTimeCal)) {
                    return true;
                } else if (selectedTimeCal.getTimeInMillis() == startTimeCal.getTimeInMillis()) {
                    return true;
                } else if (selectedTimeCal.before(dayStartTimeCal)
                        && selectedTimeCal.after(startTimeCal)) {
                    return true;
                }

                selectedTimeCal.add(Calendar.DATE, 1);

                if ((selectedTimeCal.after(dayStartTimeCal)
                        && selectedTimeCal.before(endTimeCal))) {
                    return true;
                } else if (selectedTimeCal.getTimeInMillis() == endTimeCal.getTimeInMillis()) {
                    return true;
                } else {
                    return false;
                }

            }

            if (selectedTimeCal.after(startTimeCal)
                    && selectedTimeCal.before(endTimeCal)) {
                return true;
            } else if (selectedTimeCal.getTimeInMillis() == startTimeCal.getTimeInMillis()
                    || selectedTimeCal.getTimeInMillis() == endTimeCal.getTimeInMillis()) {
                return true;
            } else {
                return false;
            }


        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

    }

    public long getTimeDifferenceBetweenTwoTime(String startTime, String endTime) {

        try {
            Date startTimeDate = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH).parse(startTime);
            Calendar startTimeCal = Calendar.getInstance();
            startTimeCal.setTime(startTimeDate);

            Date endTimeDate = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH).parse(endTime);
            Calendar endTimeCal = Calendar.getInstance();
            endTimeCal.setTime(endTimeDate);

            return startTimeCal.getTimeInMillis() - endTimeCal.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getTimeDifferenceBetweenTwoTimeForSameDay(String startTime, String endTime) {

        try {
            Date startTimeDate = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH).parse(startTime);
            Calendar startTimeCal = Calendar.getInstance();
            startTimeCal.setTime(startTimeDate);
            startTimeCal.add(Calendar.DATE, 1);

            Date endTimeDate = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.ENGLISH).parse(endTime);
            Calendar endTimeCal = Calendar.getInstance();
            endTimeCal.setTime(endTimeDate);
            endTimeCal.add(Calendar.DATE, 1);

            return endTimeCal.getTimeInMillis() - startTimeCal.getTimeInMillis();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getNextDayDate(String selectedDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            Date date = input.parse(selectedDate.trim());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            return getServerDateFormat(calendar.getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();
            return selectedDate;
        }
    }

    public long totalDaysBetweenDate(String startDate, String endDate) {

        try {
            SimpleDateFormat input = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            Date date = input.parse(startDate.trim());
            Calendar startDateTime = Calendar.getInstance();
            startDateTime.setTime(date);

            date = input.parse(endDate.trim());
            Calendar endDateTime = Calendar.getInstance();
            endDateTime.setTime(date);

            long diff = endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis();
            return diff / (24 * 60 * 60 * 1000) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }

    // if fromDate is grether than toDate then it return true otherwise false
    public boolean getCheckTwoDates(String fromDate, String toDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH);
            Date date1 = sdf.parse(fromDate);
            Date date2 = sdf.parse(toDate);

            return date1.compareTo(date2) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public long timeDifference(String start, int time) {
        //milliseconds
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT);

        try {
            Date startDate1 = simpleDateFormat.parse(start);
            Date startDate = simpleDateFormat.parse(getCreatedDateFormat());

            Date endDate = simpleDateFormat.parse(getEndDate(time, startDate1));

            long different = endDate.getTime() - startDate.getTime();

            Log.e("startDate ", String.valueOf(startDate));
            Log.e("endDate : ", String.valueOf(endDate));
            Log.e("different ", String.valueOf(different));

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = different / daysInMilli;
//            different = different % daysInMilli;

            long elapsedHours = different / hoursInMilli;
//            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;
//            different = different % minutesInMilli;

            long elapsedSeconds = different / secondsInMilli;

            System.out.printf(
                    "%d days, %d hours, %d minutes, %d seconds%n",
                    elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
            return elapsedSeconds;

        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }

    }

    public String getEndDate(int min, Date startDate) {
        try {
            DateFormat sdf = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(startDate.getTime() + (min * 60000)));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getDefaultDateTimeFromUtcDateTime(String utcDateTimeInServerFormate) {
        String dateTime = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            date = df.parse(utcDateTimeInServerFormate);
            df.setTimeZone(TimeZone.getDefault());
            String formattedDate = df.format(date);
            Log.v(TAG, "getDefaultDateTimeFromUtcDateTime: " + formattedDate);
            Log.v(TAG, "getDefaultDateTimeFromUtcDateTime: " + getServerDateTimeToAppDateTime(formattedDate));
            return getServerDateTimeToAppDateTime(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return utcDateTimeInServerFormate;
        }

    }

    public String getTimeFormateFromSeconds(String timeInSeconds) {
        int totalSecond = Integer.parseInt(timeInSeconds);
        int hours = 0;
        int minutes = 0;
        int remainingSecond = 0;
        if (totalSecond / 3600 > 0) {
            hours = totalSecond / 3600;
            totalSecond = totalSecond - (hours * 3600);
        }
        if (totalSecond / 60 > 0) {
            minutes = totalSecond / 60;
            totalSecond = totalSecond - (minutes * 60);
        }
        remainingSecond = totalSecond;

        String returnHours = "";
        String returnMinute = "";
        String returnSecond = "";
        if (hours < 10) {
            returnHours = "0" + hours;
        } else {
            returnHours = String.valueOf(hours);
        }
        if (minutes < 10) {
            returnMinute = "0" + minutes;
        } else {
            returnMinute = String.valueOf(minutes);
        }
        if (remainingSecond < 10) {
            returnSecond = "0" + remainingSecond;
        } else {
            returnSecond = String.valueOf(remainingSecond);
        }

        return returnHours + ":" + returnMinute + ":" + returnSecond;

    }

    public String getWeekDayAndTimeFromTimeStamp(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat(DISPLAY_WEEKDAY_AND_TIME, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }

    }

    public String getDateAndTimeFromTimeStamp(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat(APP_DATE_TIME_FORMAT, Locale.ENGLISH);
            Date outputDate = (new Date(timeStamp));
            return sdf.format(outputDate);
        } catch (Exception ex) {
            return "";
        }

    }

}
