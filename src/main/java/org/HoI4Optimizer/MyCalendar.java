package org.HoI4Optimizer;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;

/// Wrapper for some calendar utilities, such converting days since 1st of January 1936 into a date-string or vice versa
public class MyCalendar {
    public static String getDate(int day)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(1936,Calendar.JANUARY,1);
        cal.add(Calendar.DAY_OF_YEAR,day);

        if (day<0)
            return "Invalid Date";

        SimpleDateFormat format = new SimpleDateFormat("EEEE, 'the' d 'of' MMMM yyyy");
        //This will have format Wednesday, the 1 of January 1936 (missing the ordinal
        String noOrdinal = format.format(cal.getTime());
        //Get the date, and replace it byt the dateth
        return noOrdinal.replaceFirst("\\b(\\d{1,2})\\b", "$1" + getOrdinalSuffix(cal.get(Calendar.DAY_OF_MONTH)));
    }
    private static String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
    ///Turn a year month day into a day number after the 1st of January 1936, not defined before that day
    public static int getDay(int year,  int month, int day)
    {
        //Game starts in 1936, so no dates before then exist
        if (year<1936)
            return 0;
        else
        {
            Calendar cal = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            start.set(1936,Calendar.JANUARY,1);

            cal.set(year,switch (month%12)
            {
                case 0 ->Calendar.JANUARY;
                case 1 ->Calendar.FEBRUARY;
                case 2 ->Calendar.MARCH;
                case 3 ->Calendar.APRIL;
                case 4 ->Calendar.MAY;
                case 5 ->Calendar.JUNE;
                case 6 ->Calendar.JULY;
                case 7 ->Calendar.AUGUST;
                case 8 ->Calendar.SEPTEMBER;
                case 9 ->Calendar.OCTOBER;
                case 10 ->Calendar.NOVEMBER;
                case 11 ->Calendar.DECEMBER;
                default ->Calendar.JANUARY;//Will never happen, but the compiler gets angry
            },day);

            return (int)Duration.between(cal.toInstant(),start.toInstant()).toDays();
        }
    }
}
