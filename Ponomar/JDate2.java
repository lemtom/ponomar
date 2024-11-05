package Ponomar;

import java.util.Date;
import java.util.LinkedHashMap;

/***********************************************************
 * JDate : a class for performing operations with dates on the Julian calendar
 * 
 * PURPOSE: the purpose of this class is to provide an easy interface to
 * manipulate Julian dates METHODOLOGY: A JDate object is created by specifying
 * the month, day, year. Internally, the mm/dd/yyyy is converted to a Julian
 * date (a long). Operations can be performed easily with this Julian date. In
 * the end, an mm/dd/yyyy can be obtained back from the JDate object
 * 
 * JDate.java is part of the Ponomar program. Copyright 2006, 2007 Aleksandr
 * Andreev. aleksandr.andreev@gmail.com
 * 
 * Ponomar is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * While Ponomar is distributed in the hope that it will be useful, it comes
 * with ABSOLUTELY NO WARRANTY, without even the implied warranties of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for details.
 ************************************************************/

class JDate2 extends JDateGeneric {
	private static final int GREGORIAN = 1;
	private static final int JULIAN = 0;
	private static int calendar; // TO CHANGE ONCE HARMONISED.

	// CONSTRUCTOR: creates a JDate2 object
	// PARAMETERS: month - the month (Jan = 1 - Dec = 12) !
	// day - the day (from 1 to daysInMonth[month - 1]
	// year - the year (AD)
	// RETURNS: none
	// THROWS: IllegalArgumentException if argument is invalid
	// FOR MORE INFORMATION READ:
	// http://en.wikipedia.org/wiki/Julian_date
	protected JDate2(int month, int day, int year, int calendar2) throws IllegalArgumentException {
		if (month < 1 || month > 12) {
			throw (new IllegalArgumentException(Errors[7]));
		}

		if (day < 0) {
			throw (new IllegalArgumentException(Errors[8] + " " + day));
		}

		if (year % 4 == 0) {
			// leap year
			if (day > daysInMonthLeap[month - 1]) {
				throw (new IllegalArgumentException(Errors[8] + " " + day + " " + Errors[9] + " " + month));
			}
		} else {
			if (day > daysInMonth[month - 1]) {
				throw (new IllegalArgumentException(Errors[8] + " " + day + " " + Errors[9] + " " + month));
			}
		}
		if (calendar2 < 0 || calendar2 > 1) {
			throw (new IllegalArgumentException(
					"Nonexistent calendar specified: please use either 0 for Julian or 1 for Gregorian."));
		}

		// construct instruments
		/*
		 * int a = (int)Math.floor((14 - month) / 12); int y = year + 4800 - a; int m =
		 * month + 12 * a - 3;
		 * 
		 * // construct a julian day mn_jday = (long)(day + Math.floor((153 * m + 2) /
		 * 5) + 365 * y + Math.floor(y / 4)) - 32083;
		 */
		if (month < 3) {
			month = month + 12;
			year = year - 1;
		}
		int a = (int) Math.floor(year / 100);
		int b = 0;
		if (((year > 1582 || (year == 1582 && month > 10) || (year == 1582 && month == 10 && day >= 15)))
				&& calendar2 == GREGORIAN) {
			b = 2 - a + (int) Math.floor(a / 4);
		}
		calendar = calendar2;
		mnJday = (long) (Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524);// -1524.5
	}

	// CONSTRUCTOR: CREATES A JDATE OBJECT
	// PARAMETERS: A long WITH A JULIAN DATE
	// RETURNS: NONE
	// THROWS: IllegalArgumentException if the JULIAN DATE < 0
	protected JDate2(long jday, int calendar2) throws IllegalArgumentException {
		if (jday < 0) {
			throw (new IllegalArgumentException(Errors[9]));
		}
		if (calendar2 < 0 || calendar2 > 1) {
			throw (new IllegalArgumentException(
					"Nonexistent calendar specified: please use either 0 for Julian or 1 for Gregorian."));
		}
		calendar = calendar2;
		mnJday = jday;
	}

	// CONSTRUCTOR: CREATES A JDATE OBJECT INTIALIZED TO THE DAY WHEN THE
	// CONSTRUCTOR WAS CALLED
	// PARAMETERS: NONE
	// RETURNS: NONE
	protected JDate2() {
		Date now = new Date();
		long mils = now.getTime(); // number of miliseconds since Greg. Jan 1 1970, 00:00:00 GMT

		// convert the number of miliseconds to the number of days since Jan 1 1970 Greg
		long days = (long) Math.floor(mils / 86400000);
		// the Julian Day for Jan 1 1970 Gregorian is 2 440 588
		mnJday = days + 2440588;// 2440588; or 2440587.5
		calendar = GREGORIAN;
	}

	// A METHOD FOR OBTAINING THE YEAR BACK FROM A JDATE OBJECT
	// PARAMETERS: none
	// RETURNS: an integer with the year of the JDate object
	protected int getYear() {
		/*
		 * long jbar = mn_jday + 32083; // Compute the number of four-year Julian cycles
		 * that have elapsed since mn_jday // There are 1461 days in each cycle //
		 * Multiply this number by four (years/cycle) int n1 = (int)(jbar / 1461) * 4;
		 * // Compute the number of days since the last four-year cycle // Divide by 365
		 * days in a year int n2 = (int)(jbar % 1461) / 365; int da = (int)(jbar %
		 * 1461); // Add one if we are after December 31, since JDate starts March 1 int
		 * adj = (da % 365) > 306 ? 1 : 0;
		 */
		long a = mnJday;

		if (mnJday >= 2299160 && calendar == GREGORIAN) {
			int alpha = (int) (Math.floor((a - 1867216.25) / 36524.25));
			a = a + 1 + alpha - (int) (Math.floor(alpha / 4));
		}
		long b = a + 1524;
		int c = (int) (Math.floor((b - 122.1) / 365.25));
		int d = (int) (Math.floor(365.25 * c));
		int e = (int) (Math.floor((b - d) / 30.6001));
		int m = e - 1;
		if (m > 12) {
			m = m - 12;
		}
		int y = c - 4715;
		if (m > 2) {
			y = y - 1;
		}
		// System.out.println("Year: " + y);
		return y;
	}

	// A METHOD TO OBTAIN THE MONTH FROM A JDATE OBJECT
	// PARAMETERS: NONE
	// RETURNS: an integer with the month of the JDate object
	protected int getMonth() {
		/*
		 * long jbar = mn_jday + 32083; // Take jbar modulo 1461 to get the number of
		 * days since the last four-year cycle int da = (int)(jbar % 1461); // Take da
		 * modulo 365 to get the number of days since the last 1 March int m = mod(da,
		 * 365); // now, subtract off days for each of the months int j = 2; while (m >
		 * daysInMonthLeap[j]) { m -= daysInMonthLeap[j]; j++; if (j == 12) { j = 0; } }
		 * 
		 * return j + 1;
		 */
		long a = mnJday;
		long z = a;
		if (mnJday > 2299160 && calendar == GREGORIAN) {
			int alpha = (int) (Math.floor((z - 1867216.25) / 36524.25));
			a = z + 1 + alpha - (int) (Math.floor(alpha / 4));
		}
		long b = a + 1524;
		int c = (int) (Math.floor((b - 122.1) / 365.25));
		int d = (int) (Math.floor(365.25 * c));
		int e = (int) (Math.floor((b - d) / 30.6001));
		int m = e - 1;
		if (m > 12) {
			m = e - 13;
		}
		// System.out.println("Month: " + m);
		return m;
	}

	// A METHOD FOR OBTAINING THE DAY FROM A JDATE OBJECT
	// PARAMETERS: NONE
	// RETURNS: AN INTEGER WITH THE DAY OF THE JDATE OBJECT
	protected int getDay() {
		/*
		 * long jbar = mn_jday + 32083; // repeat above steps until m int da = mod(jbar,
		 * 1461); int m; if (da == 1461) { m = 29; // FEBRUARY 29 } else { m = mod(da,
		 * 365); // this number is the number of days since the last March 1 // now,
		 * take off days for each month int k = 2; while (m > daysInMonth[k]) { m -=
		 * daysInMonth[k]; k++; if (k == 12) { k = 0; } } } return m; // the number of
		 * days that will remain at the end
		 */
		long a = mnJday;
		long z = a;

		if (mnJday > 2299160 && calendar == GREGORIAN) {
			int alpha = (int) (Math.floor((z - 1867216.25) / 36524.25));
			a = z + 1 + alpha - (int) (Math.floor(alpha / 4));
		}
		long b = a + 1524;
		int c = (int) (Math.floor((b - 122.1) / 365.25));
		int d = (int) (Math.floor(365.25 * c));
		int e = (int) (Math.floor((b - d) / 30.6001));
		double w = (int) b - d - (int) (Math.floor(30.6001 * e));
		// System.out.println("Julian Day: " +mn_jday);
		// System.out.println("Day: " + W);
		return (int) w; // +F is we have a fraction
	}

	// A METHOD TO OBTAIN THE GREGORIAN DATE FROM A JDATE OBJECT
	// PARAMETERS: NONE
	// RETURNS: A java.util.Date OBJECT WITH THE DATE ON THE GREGORIAN CALENDAR
	// IF YEAR < 1583, RETURNS THE DATE ON THE PROLEPTIC GREGORIAN CALENDAR
	protected Date getGregorianDate() {
		double j1;

		if (mnJday >= 2299160) {
			double tmp = Math.floor(((mnJday - 1867216.0) - 0.25) / 36524.25);
			j1 = mnJday + 1 + tmp - Math.floor(0.25 * tmp);
		} else {
			j1 = (double) mnJday;
		}

		double j2 = j1 + 1524.0;
		double j3 = Math.floor(6680.0 + ((j2 - 2439870.0) - 122.1) / 365.25);
		double j4 = Math.floor(j3 * 365.25);
		double j5 = Math.floor((j2 - j4) / 30.6001);

		int d = (int) Math.floor(j2 - j4 - Math.floor(j5 * 30.6001));
		int m = (int) Math.floor(j5 - 1.0);
		if (m > 12) {
			m -= 12;
		}
		int y = (int) Math.floor(j3 - 4715.0);

		if (m > 2) {
			--y;
		}
		if (y <= 0) {
			--y;
		}

		return new Date(y - 1900, m - 1, d); // return the date object
	}

	protected String getGregorianDateS(LinkedHashMap<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Phrases = new LanguagePack(dayInfo);
		dayNames = Phrases.obtainValues(Phrases.Phrases.get("2"));
		civilMonthNames = Phrases.obtainValues(Phrases.Phrases.get("4"));
		monthNames = Phrases.obtainValues(Phrases.Phrases.get("3"));
		civilDayNames = Phrases.obtainValues(Phrases.Phrases.get("5"));
		Errors = Phrases.obtainValues(Phrases.Phrases.get("Errors"));
		// GIVES THE STRING IN THE LOCAL FORMAT.
		double j1;

		if (mnJday >= 2299160) {
			double tmp = Math.floor(((mnJday - 1867216.0) - 0.25) / 36524.25);
			j1 = mnJday + 1 + tmp - Math.floor(0.25 * tmp);
		} else {
			j1 = (double) mnJday;
		}

		double j2 = j1 + 1524.0;
		double j3 = Math.floor(6680.0 + ((j2 - 2439870.0) - 122.1) / 365.25);
		double j4 = Math.floor(j3 * 365.25);
		double j5 = Math.floor((j2 - j4) / 30.6001);

		int d = (int) Math.floor(j2 - j4 - Math.floor(j5 * 30.6001));
		int m = (int) Math.floor(j5 - 1.0);
		if (m > 12) {
			m -= 12;
		}
		int y = (int) Math.floor(j3 - 4715.0);

		if (m > 2) {
			--y;
		}
		if (y <= 0) {
			--y;
		}
		Format = Phrases.Phrases.get("DateFormat");
		int dow = getDayOfWeek();
		int year = y;
		int month = m;
		int day = d;
		if (analyse.dayInfo.get("Ideographic") == null) {
			Format = Format.replace("WW", civilDayNames[dow]);
			Format = Format.replace("DD", String.valueOf(day));
			Format = Format.replace("MM", civilMonthNames[month - 1]);
			Format = Format.replace("YY", String.valueOf(year));
			Format = Character.toUpperCase(Format.charAt(0)) + Format.substring(1);
		} else {
			if (analyse.dayInfo.get("Ideographic").equals("1")) {
				RuleBasedNumber convertN = new RuleBasedNumber(analyse.dayInfo);
				Format = Format.replace("WW", civilDayNames[dow]);
				Format = Format.replace("DD", convertN.getFormattedNumber(Long.parseLong(String.valueOf(day))));
				Format = Format.replace("MM", civilMonthNames[month - 1]);
				Format = Format.replace("YY", convertN.getFormattedNumber(Long.parseLong(String.valueOf(year))));
				Format = Character.toUpperCase(Format.charAt(0)) + Format.substring(1);

			} else {
				Format = Format.replace("WW", civilDayNames[dow]);
				Format = Format.replace("DD", String.valueOf(day));
				Format = Format.replace("MM", civilMonthNames[month - 1]);
				Format = Format.replace("YY", String.valueOf(year));
				Format = Character.toUpperCase(Format.charAt(0)) + Format.substring(1);
			}
		}
		return Format;

		// return new Date(y - 1900, m - 1, d); // return the date object
	}

	protected int getDoy() {
		/*
		 * long jbar = mn_jday + 32083; // repeat above steps until m int da = mod(jbar,
		 * 1461);
		 * 
		 * // this is up to 1461 days since the start of a Julian cycle on March 1, leap
		 * year // if da is 1461, we are at February 29 (doy 366) // otherwise, we mod
		 * by 365 to figure out where we are in the year, remembering to add 59 to get
		 * to Jan 1 return da == 1461 ? 366 : mod(da + 59, 365) - 1; // Jan 1 is doy 0
		 */
		int y = getYear();
		int k = 2;
		if (y % 400 == 0) {
			k = 1;
		} else if (y % 4 == 0 && y % 100 != 0 && calendar == GREGORIAN) {
			k = 1;
		} else if (y % 4 == 0 && calendar == JULIAN) {
			k = 1;
		}
		// System.out.println("Doy: K: " + K);
		int n = (int) Math.floor(275 * getMonth() / 9) - k * (int) Math.floor((getMonth() + 9) / 12) + getDay() - 31;
		if (y == 1582 && (getMonth() > 10 || (getMonth() == 10 && getDay() > 4)) && calendar == GREGORIAN) {
			n = n - 10; // This corrects some of the problems, but introduces a few others. There is no
						// way to solve all without manually coding everything for October to December
						// 1582.
		}
		if (k == 1 && n == 59) {
			n = 366;
		} else if (k == 1 && n > 59) {
			n = n - 1;
		}
		// System.out.println("Doy: " + N);
		return n;
	}

	// A METHOD TO CHECK IF ONE JDATE IS EQUAL TO ANOTHER JDATE
	// PARAMETERS: A JDATE OBJECT m
	// RETURNS: FOR n.equals(m), true iff n.getJulianDay() == m.getJulianDay()
	// false otherwise
	public boolean equals(JDate2 m) {
		return (mnJday == m.getJulianDay());
	}

	// A METHOD TO COMPARE TWO JDATE OBJECTS
	// PARAMETERS: A JDATE OBJECT m
	// RETURNS: FOR n.compareTo(m), 0 iff n == m
	// a number less than 0 iff n < m
	// a number greater than 0 iff m > n
	// THROWS: ClassCastException iff m is not a JDate object
	public int compareTo(JDateGeneric m) throws ClassCastException {
		int temp;
		try {
			temp = (int) (mnJday - m.getJulianDay());
		} catch (ClassCastException cce) {
			throw (new ClassCastException(cce.toString()));
		}
		return temp;
	}

	// A CLONING METHOD
	// PARAMETERS: NONE
	// RETURNS: A CLONE
	public Object clone() {
		return new JDate2(mnJday, calendar);
	}

	// ADDS A SEPCIFIED NUMBER OF MONTHS TO A JDATE OBJECT
	// PARAMETERS: AN int WITH THE NUMBER OF MONTHS TO BE ADDED
	// RETURNS: NONE
	protected synchronized void addMonths() {
		// create a new version
		boolean leapYear = this.isLeapYear();
		mnJday += (leapYear) ? daysInMonthLeap[this.getMonth() - 1] : daysInMonth[this.getMonth() - 1];
	}

	// SUBTRACTS A SPECIFIED NUMBER OF MONTHS FROM A JDATE OBJECT
	// PARAMETERS: AN int WITH THE NUMBER OF MONTHS TO BE SUBTRACTED
	// RETURNS: NONE
	protected synchronized void subtactMonths() {
		int month = this.getMonth() - 1; // ADJUSTED TO BE THE ARRAY INDEX (STARTS WITH 0)
		month--; // PREVIOUS MONTH

		if (month < 0) {
			month += 11;
		}
		// create a new version
		boolean leapYear = this.isLeapYear();
		int y = getYear();

		mnJday -= (leapYear) ? daysInMonthLeap[month] : daysInMonth[month];
	}

	// FINDS THE DIFFERENCE BETWEEN TWO DATES
	// PARAMETERS: TWO JDATE OBJECTS
	// RETURNS: A LONG WITH THE NUMBER OF DAYS BETWEEN THE TWO OBJECTS,
	// NON-first-INCLUSIVE
	// i.e. saturday - sunday = 6
	protected static long difference(JDate2 former, JDate2 latter) {
		return former.getJulianDay() - latter.getJulianDay();
	}

	// RETURNS THE MAXIMUM NUMBER OF DAYS IN A MONTH
	// PARAMETER: AN int WITH THE MONTH AND AN int WITH THE YEAR
	// RETURNS: THE MAXIMUM NUMBER OF DAYS IN THAT MONTH, GIVEN THAT YEAR
	protected int getMaxDaysInMonth(int month, int year) throws IllegalArgumentException {
		if (month < 1 || month > 12) {
			throw (new IllegalArgumentException(Errors[7]));
		}
		// create a new version
		boolean leapYear = isLeapYear(year);

		return (leapYear) ? daysInMonthLeap[month - 1] : daysInMonth[month - 1];
	}

	protected static void setCalendar(int calNew) {
		if (calNew < 0 || calNew > 1) {
			System.out.print("No such calendar exists: 0 == Julian and 1 == Gregorian");
		} else {
			calendar = calNew;
		}
	}

	protected JDate toJDate() {
		System.out.println("Warning: coverting JDate2 to JDate. Perhaps correct this later?");
		return new JDate(this.mnJday);
	}

	protected boolean isLeapYear() {
		boolean leapYear = false;
		int y = this.getYear();
		if (y % 400 == 0) {
			leapYear = true;
		} else if (y % 4 == 0 && y % 100 != 0 && calendar == GREGORIAN) {
			leapYear = true;
		} else if (y % 4 == 0 && calendar == JULIAN) {
			leapYear = true;
		}
		return leapYear;
	}

	protected boolean isLeapYear(int year) {
		boolean leapYear = false;
		int y = year;
		if (y % 400 == 0) {
			leapYear = true;
		} else if (y % 4 == 0 && y % 100 != 0 && calendar == GREGORIAN) {
			leapYear = true;
		} else if (y % 4 == 0 && calendar == JULIAN) {
			leapYear = true;
		}
		return leapYear;
	}

	protected static String getCalendar() {
		if (calendar == JULIAN) {
			return "Julian";
		} else {
			return "Gregorian";
		}

	}

	protected static int getCalendar2() {
		return calendar;

	}

	protected int getAM() {
		// returns the corresponding anno mundi given the date.
		double difference = 0;
		int y = this.getYear();
		int m = this.getMonth();
		int d = this.getMonth();
		int AM = 5782 + (y - 2022);

		if (m > 8) {
			AM = AM + 1;
		}

		return AM;
	}
}
