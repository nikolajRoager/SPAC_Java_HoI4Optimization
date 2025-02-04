package org.HoI4Optimizer;

public class Calender {
    public static String getDate(int day)
    {
        if (day<0)
            return "Invalid Date";

        //The game is not going to run to 2100, so I just use 1 leap year at the start of EVERY 4-year cycle
        int four_year = day/1461;
        int year = four_year*4+1936;//This year started the 4-year cycle
        //The assignment is INTENTIONAL, regardless of the check result the day is now from the start of the 4-year cycle
        boolean isLeapYear=(day%=1461)<366/*Within 366 first days, remember day starts at 0 not 1!*/;

        String[] Monthnames = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        //1936 started on a wednesday
        String[] Weekday = {"Wednesday","Thursday","Friday","Saturday","Sunday","Monday","Tuesday"};
        //Augustus and Julius were great Caesars, therefore their months must have 31 days even though they are next to each other
        int[] Monthdays      = {31,28,31,30,31,30,31,30,31,31,30,31};

        String y;

        if (!isLeapYear)
        {
            day-=366;//Subtract the first leap year
            //Then get which of the 3 365 years we are in since the start of the
            year += 1+day/365;
            day%=365;
        }
        else
        {
            Monthdays[1]=29;
        }

        //Ok now deal with the month  and days

        for (int month=0; month<Monthnames.length; month++)
        {

            if (day< Monthdays[month] )
            {
                //A bunch of if statements to deal with the weirdness of numbers in the English language
                if (day==0)
                    return Weekday[day%7]+" the 1st of "+Monthnames[month]+" "+year;
                else if (day==1)
                    return Weekday[day%7]+" the "+"2nd of "+Monthnames[month]+" "+year;
                else if (day==2)
                    return Weekday[day%7]+" the "+"3rd of "+Monthnames[month]+" "+year;
                else if (day==20)
                    return Weekday[day%7]+" the "+"21st of "+Monthnames[month]+" "+year;
                else if (day==21)
                    return Weekday[day%7]+" the "+"22nd of "+Monthnames[month]+" "+year;
                else if (day==22)
                    return Weekday[day%7]+" the "+"23rd of "+Monthnames[month]+" "+year;
                else if (day==30)
                    return Weekday[day%7]+" the "+"31st of "+Monthnames[month]+" "+year;
                else
                    return Weekday[day%7]+" the "+(day+1)+"th of "+Monthnames[month]+" "+year;
            }
            else
            {
                day -= Monthdays[month];
            }
        }
        //Should never happen, we know day is < 365 or 366, this just makes the compiler shut up
        return "Invalid Date";
    }

    ///Turn a year month day into a day number after the 1st of January 1936, not defined before that day
    public static int getDay(int year, int month, int day)
    {
        //Game starts in 1936, so no dates before then exist
        if (year<1936)
            return 0;
        else
        {
            //Traditionally month and days start at 1 not 0, but 0 is easier to calculate, so subtract 1
            month--;
            day--;
            int days=0;
            //Loop through years betwixt 1936 and this year and add days
            for  (int _year=1936;_year<year;_year++)
            {
                //Realistically, the simulation is not going to run to 2100
                boolean isLeapYear = (_year % 4 == 0 && _year % 100 != 0) || _year % 400 == 0;
                if (isLeapYear) days+=366;
                else days+=365;
            }

            for  (int _month=0;_month<month;_month++)
            {
                boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
                //January,March,May,July,August ,October,December
                if (_month==0 || _month == 2 || _month == 4 || _month == 6 || _month == 7 /*(Augustus is a Great Caesar like Julius, therefore they both have 31 days)*/| _month == 9 || _month == 11)
                    days+=31;
                //April,June,September,November
                else if(_month==3 || _month == 5 || _month == 8 || _month == 10)
                    days+=30;
                //February
                else if (isLeapYear)
                    days+=29;
                else
                    days+=28;
            }

            return days+day;
        }
    }
}
