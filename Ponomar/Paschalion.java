package Ponomar;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/***************************************************************************
 * Paschalion.java - A CLASS FOR WORKING WITH THE PASCHALION OF THE ORTHODOX
 * CHURCH PURPOSE: The purpose of this class is to provide an interface for
 * various dates of feasts, fasts, and floating observances as well as lunar
 * tables.
 * 
 * Paschalion.java is part of the Ponomar program. Copyright 2006, 2007
 * Aleksandr Andreev. aleksandr.andreev@gmail.com
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
 ****************************************************************************/

final class Paschalion {
	// THE LENGTH OF A LUNAR MONTH
	private static final double lunarMonth = 29.52916667;
	private static final double lengthOfRem = 0.016932411; // SEE COMMENTS IN getLunarPhaseString()
	private static final int calendar = 1; // Set 1 for Gregorian, and 0 for Julian

	// THE FOUNDATION IS THE "AGE OF THE MOON" (NUMBER OF DAYS SINCE NEW MOON)
	// ON 1 MARCH, JULIAN CALENDAR, FOR A PARTICULAR LUNAR YEAR
	// THERE ARE 19 YEARS IN THE METONIC CYCLE
	// IN THIS CALCULATION, IT IS HOURS SINCE MIDNIGHT
	private static final double[] foundation = new double[] { 14.042016807, 25.462184874, 6.084033613, 17.966386555,
			28.336134454, 9.210084034, 20.504201681, 1.420168067, 12.294117647, 23.168067227, 4.546218487, 15.042016807,
			26.294117647, 7.630252101, 18.546218487, 29.420168067, 11.756302521, 26.210084034, 3.042016807 };

	// A mod OPERATOR
	private static int mod(int divisor, int modulo) {
		int temp = divisor % modulo;

		if (temp == 0) {
			temp = modulo;
		}

		return temp;
	}

	// Another mod OPERATOR
	// THE POINT OF THIS IS TO FIND THE REMAINDER FROM DIVISION OF A DECIMAL
	// BY ANOTHER DECIMAL, MUCH IN THE SPIRIT OF MOD
	private static double mod(double divisor, double modulo) {
		double quotient = Math.floor(divisor / modulo);

		double remainder = divisor - quotient * modulo;

		return remainder;
	}

	// A METHOD TO OBTAIN THE DATE OF THE JULIAN PASCHA
	// USES THE GAUSSIAN FORMULAE TO OBTAIN PASCHA
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A JDate WITH THE DATE OF PASCHA FOR THAT YEAR (JULIAN CALENDAR)
	// THROWS: IllegalArgumentException IF year < 33
	static JDate getPascha(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}
		if (year > 1582 && calendar == 1) {
			int a = year % 19;
			int b = (int) Math.floor(year / 100);
			int c = year % 100;
			int d = (int) Math.floor(b / 4);
			int e = b % 4;
			int f = (int) Math.floor((b + 8) / 25);
			int g = (int) Math.floor((b - f + 1) / 3);
			int h = (19 * a + b - d - g + 15) % 30;
			int i = (int) Math.floor(c / 4);
			int k = c % 4;
			int l = (32 + 2 * e + 2 * i - h - k) % 7;
			int m = (int) Math.floor((a + 11 * h + 22 * l) / 451);
			int n = (int) Math.floor((h + l - 7 * m + 114) / 31);
			int p = (h + l - 7 * m + 114) % 31;
			return new JDate(n, p + 1, year);
		} else {
			int a = year % 4;
			int b = year % 7;
			int c = year % 19;
			int d = (19 * c + 15) % 30;
			int e = (2 * a + 4 * b - d + 34) % 7;
			int f = (int) Math.floor((d + e + 114) / 31); // Month of pascha e.g. march=3
			int g = ((d + e + 114) % 31) + 1; // Day of pascha in the month
			// System.out.println("Easter on the Julian calendar is: "+year+"/"+f+"/"+g);
			// Create a JDate object
			return new JDate(f, g, year);
		}

	}

	// A METHOD TO OBTAIN THE DATE OF JULIAN PENTECOST
	// USES THE ABOVE ALGORITHM AND ADDS 49 DAYS
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A JDate WITH THE DATE OF PENTECOST (JULIAN CALENDAR)
	// THROWS: IllegalArgumentException IF year < 33
	static JDate getPentecost(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		date.addDays(49);
		return date;
	}

	// A METHOD TO OBTAIN THE DATE JULIAN LENT STARTS (48 DAYS BEFORE PASCHA)
	// USES THE ABOVE ALOGORITHM AND SUBTRACTS 48 DAYS
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A JDate WITH THE DATE LENT STARTS (DATE OF CLEAN MONDAY)
	// THROWS: IllegalArgumentException IF year < 33
	private static JDate getLentStart(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		date.subtractDays(48);
		return date;
	}

	// A METHOD TO OBTAIN THE DATE APOSTLES' FAST STARTS
	// USES THE ABOVE ALGORITHM AND ADDS 57 DAYS
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURS: A JDate WITH THE DATE APOSTLES' FAST STARTS (MONDAY AFTER SUNDAY OF
	// ALL SAINTS)
	// THROWS: IllegalArgumentException IF year < 33
	private static JDate getApostlesFastStart(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		date.addDays(57);
		return date;
	}

	// A METHOD TO OBTAIN THE LENGTH OF APOSTLES' FAST
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: AN int WITH THE LENGTH OF APOSTLES' FAST
	// THROWS: IllegalArgumentException IF year < 33
	private static int getApostlesFastLength(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		JDate start = getApostlesFastStart(year);

		JDate end = new JDate(6, 29, year);
		return (int) JDate.difference(end, start);
	}

	// METHODS FOR DEALING WITH THE VISUAL PASCHALION
	// FOR ADVANCED USERS ONLY
	// A METHOD TO OBTAIN THE kluch granits (key of boundaries)
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: AN int REPRESENTING ONE OF THE LETTERS OF THE KEY, WHERE Az = 1
	// THROWS: ditto
	private static int getKeyOfBoundaries(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		int f = date.getMonth();
		int g = date.getDay();

		if (f == 3) {
			// PASCHA IS IN MARCH
			g -= 21;
		} else {
			// PASCHA IS IN APRIL
			g += 10;
		}

		return g;
	}

	// A METHOD TO OBTAIN THE INDICTION
	// PARAMETERS: ditto
	// RETURNS: AN INTEGER WITH THE INDICTION
	// THROWS: ditto
	private static int getIndiction(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		return mod(year - 312, 15);
	}

	// A METHOD TO OBTAIN THE SOLAR CYCLE
	// PARAMETERS: ditto
	// RETURNS: AN int WITH THE SOLAR CYCLE FOR THIS YEAR
	// THROWS: ditto
	private static int getSolarCycle(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		return mod(year + 5508, 28);
	}

	// A METHOD FOR OBTAINING THE LUNAR CYCLE
	// PARAMETERS: ditto
	// RETURNS: AN int WITH THE LUNAR CYCLE FOR THIS YEAR
	// THROWS: ditto
	private static int getLunarCycle(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		int temp = (year + 1) % 19 - 3;
		if (temp <= 0) {
			temp += 19;
		}

		return temp;
	}

	// METHODS FOR PERFORMING CALCULATIONS WITH PHASES OF THE MOON
	// ALL LUNAR PHASE INFO IS COMPUTED **NOT ASTRONOMICALLY** BUT ON THE METONIC
	// CYCLE
	// EXPERIMENTAL ------------ NOT FOR FURTHER USE AS OF THIS VERSION
	// ----------------
	// NO GUARANTEES OF ANY KIND ARE MADE ABOUT THE RESULTS OF THESE (EXPERIMENTAL)
	// ALGORITHMS

	// A METHOD TO OBTAIN THE PHASE OF THE MOON FOR SOME DATE
	// PARAMETERS: A JDate object WITH THE DATE DESIRED
	// RETURNS: A double WITH THE PHASE OF THE MOON FOR THAT DAY
	// WHERE NEW MOON = 0, FULL MOON = 0.5
	// THROWS: IllegalArgumentException IF THE YEAR IS < 33 AD
	private static double getLunarPhase(JDate date) throws IllegalArgumentException {
		int year = date.getYear();
		int cycle;
		try {
			cycle = getLunarCycle(year);
		} catch (IllegalArgumentException iae) {
			throw (iae);
		}

		// FIND THE DIFFERENCE (IN DAYS) BETWEEN NOW AND MAR 1
		long diff = JDate.difference(date, new JDate(3, 1, year));
		if (diff < 0) {
			// WE ARE BEFORE MAR 1, SO USE THE PREVIOUS YEAR
			diff = JDate.difference(date, new JDate(3, 1, year - 1));
		}

		// TAKE THE DIFFERENCE MODULO THE LENGTH OF A LUNAR MONTH
		// TO FIND THE REMAINDER OF THE MOON VIS-A-VIS MAR 1
		double remainder = mod((double) diff, lunarMonth);
		// ADD THIS REMAINDER TO THE AGE OF THE MOON ON MAR 1
		remainder += foundation[cycle - 1];
		while (remainder >= lunarMonth) {
			remainder -= lunarMonth;
		}

		// SCALE THE AGE OF THE MOON TO A MORE MANAGEABLE QUANTITY
		return (remainder / lunarMonth);
	}

	// CONVERT ABOVE TO A STRING WITH THE PHASE OF THE MOON
	private static String getLunarPhaseString(JDate date, LinkedHashMap<Object, Object> dayInfo)
			throws IllegalArgumentException {
		double raw;

		try {
			raw = getLunarPhase(date);
		} catch (IllegalArgumentException e) {
			throw (new IllegalArgumentException(e.toString()));
		}

		String ret = "";
		// LET ME EXPLAIN THIS WITH AN EXAMPLE. THE PHASE TODAY IS "FULL MOON"
		// IFF THE TIME OF FULL MOON OCCURS +/- ONE-HALF LUNAR DAY (lengthOfRem)
		// FROM THE TIME WE ARE CONSIDERING, SINCE THE TIME WE ARE CONSIDERING IS NOON
		// ALL OTHER PHASES ARE ANALAGOUS
		LanguagePack Text = new LanguagePack(dayInfo);
		String[] phases = Text.obtainValues((String) Text.Phrases.get("Phases"));
		if (raw < lengthOfRem || raw > 1 - lengthOfRem) {
			ret = phases[0];
		} else if (raw < 0.25 - lengthOfRem) {
			ret = phases[1];
		} else if (raw >= 0.25 - lengthOfRem && raw <= 0.25 + lengthOfRem) {
			ret = phases[2];
		} else if (raw < 0.5 - lengthOfRem) {
			ret = phases[3];
		} else if (raw >= 0.5 - lengthOfRem && raw <= 0.5 + lengthOfRem) {
			ret = phases[4];
		} else if (raw < 0.75 - lengthOfRem) {
			ret = phases[5];
		} else if (raw >= 0.75 - lengthOfRem && raw <= 0.75 + lengthOfRem) {
			ret = phases[6];
		} else if (raw >= 0.75 + lengthOfRem) {
			ret = phases[7];
		}

		return ret;
	}

	// A METHOD TO OBTAIN THE DATE OF THE NEXT NEW MOON
	// PARAMETERS: A JDate WITH THE DATE DESIRED
	// RETURNS: A JDate WITH THE DATE OF THE NEXT NEW MOON, ROUNDED DOWN
	// THROWS: IllegalArgumentException IF YEAR < 33
	private static JDate getNextNewMoon(JDate date) throws IllegalArgumentException {
		// GET THE LUNAR PHASE FOR THIS DATE
		double phase;
		try {
			phase = getLunarPhase(date);
		} catch (IllegalArgumentException iae) {
			throw (iae);
		}

		// OBTAIN THE AGE OF THE MOON (AGAIN)
		phase *= lunarMonth;

		// CALCULATE HOW MANY DAYS REMAIN UNTIL PHASE == LUNARMONTH
		int diff = (int) Math.floor(lunarMonth - phase);

		// ADD THAT MANY DAYS TO THE CURRENT DATE
		date.addDays(diff);
		return date;
	}

	// A METHOD TO OBTAIN THE DATE OF THE NEXT FULL MOON
	// PARAMETERS: A JDate WITH THE DATE DESIRED
	// RETURNS: A JDate WITH THE DATE OF THE NEXT FULL MOON, ROUNDED DOWN
	// THROWS: IllegalArgumentException IF YEAR < 33
	private static JDate getNextFullMoon(JDate date) throws IllegalArgumentException {
		// GET THE LUNAR PHASE FOR THIS DATE
		double phase;
		try {
			phase = getLunarPhase(date);
		} catch (IllegalArgumentException iae) {
			throw (iae);
		}

		// OBTAIN THE AGE OF THE MOON
		phase *= lunarMonth;

		// FIND OUT HOW MANY REMAIN UNTIL PHASE == LUNARMONTH / 2
		int diff = (int) Math.floor(lunarMonth / 2 - phase);
		if (diff < 0) {
			diff = (int) Math.floor(lunarMonth / 2 - phase + lunarMonth);
		}

		// ADD THIS MANY DAYS TO THE CURRENT DATE
		date.addDays(diff);
		return date;
	}

	// A METHOD FOR FIGURING OUT THE FAST DAYS FOR A PARTICULAR YEAR
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: AN array WITH THE FASTING REGULATIONS FOR EVERY DAY OF THAT YEAR
	// 0 - FAST FREE
	// 1 - FAST DAY
	// 2 - CHEESEFARE
	// ONLY FOR USE WITH THE CALENDAR CONTROL; FOR SPECIFIC DAYS, MORE CONVOLUTED
	// CALCULATIONS ARE MADE
	// THROWS: ditto
	static int[] getFasts(int year) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		// A HASHTABLE WITH MANDATORY FAST DAYS IN THE YEAR
		Hashtable mustFast = new Hashtable();

		mustFast.put(new JDate(1, 5, year).getJulianDay(), "Eve of Theophany");
		mustFast.put(new JDate(8, 29, year).getJulianDay(), "Beheading");
		mustFast.put(new JDate(9, 14, year).getJulianDay(), "Exaltation");

		// A HASHTABLE WITH MANDATORY FAST-FREE DAYS
		Hashtable cantFast = new Hashtable();

		cantFast.put(new JDate(1, 6, year).getJulianDay(), "Theophany");

		// PASCHA
		JDate pascha = getPascha(year);

		// OTHER FASTING REGULATIONS
		JDate sviatkiStart = new JDate(12, 25, year);
		JDate sviatkiEnd = new JDate(1, 4, year);
		JDate pubPharStart = new JDate(pascha.getJulianDay() - 70);
		JDate pubPharEnd = new JDate(pascha.getJulianDay() - 63);
		JDate cheeseStart = new JDate(pascha.getJulianDay() - 55);
		JDate cheeseEnd = new JDate(pascha.getJulianDay() - 49);
		JDate lentStart = new JDate(pascha.getJulianDay() - 48);
		JDate lentEnd = new JDate(pascha.getJulianDay() - 1);
		JDate brightStart = new JDate(pascha.getJulianDay());
		JDate brightEnd = new JDate(pascha.getJulianDay() + 6);
		JDate pentStart = new JDate(pascha.getJulianDay() + 49);
		JDate pentEnd = new JDate(pascha.getJulianDay() + 56);
		JDate apostlesStart = new JDate(pascha.getJulianDay() + 57);
		JDate apostlesEnd = new JDate(6, 28, year);
		JDate dormStart = new JDate(8, 1, year);
		JDate dormEnd = new JDate(8, 14, year);
		JDate adventStart = new JDate(11, 15, year);
		JDate adventEnd = new JDate(12, 24, year);

		JDate dummy = new JDate(1, 1, year);
		int numdays = (year % 4 == 0) ? 366 : 365;

		int[] retval = new int[numdays];
		int i = 0;

		do {
			// figure out if this day is a fast day
			int fast = 0;

			if (mustFast.containsKey(dummy.getJulianDay())) {
				// mandatory fast
				fast = 1;
			} else if (cantFast.containsKey(dummy.getJulianDay())) {
				// not a fast day
			} else if (dummy.compareTo(sviatkiStart) >= 0) {
				// not a fast day
			} else if (dummy.compareTo(sviatkiEnd) <= 0) {
				// not a fast day
			} else if (dummy.compareTo(pubPharStart) >= 0 && dummy.compareTo(pubPharEnd) <= 0) {
				// not a fast day
			} else if (dummy.compareTo(cheeseStart) >= 0 && dummy.compareTo(cheeseEnd) <= 0) {
				// CHEESEFARE WEEK
				fast = 2;
			} else if (dummy.compareTo(lentStart) >= 0 && dummy.compareTo(lentEnd) <= 0) {
				// LENT
				fast = 1;
			} else if (dummy.compareTo(brightStart) >= 0 && dummy.compareTo(brightEnd) <= 0) {
				// BRIGHT WEEK
				fast = 0;
			} else if (dummy.compareTo(pentStart) >= 0 && dummy.compareTo(pentEnd) <= 0) {
				// PENTECOST WEEK
				fast = 0;
			} else if (dummy.compareTo(apostlesStart) >= 0 && dummy.compareTo(apostlesEnd) <= 0) {
				// APOSTLES' FAST
				fast = 1;
			} else if (dummy.compareTo(dormStart) >= 0 && dummy.compareTo(dormEnd) <= 0) {
				// DORMITION FAST
				fast = 1;
			} else if (dummy.compareTo(adventStart) >= 0 && dummy.compareTo(adventEnd) <= 0) {
				// ADVENT
				fast = 1;
			} else {
				// IS THIS A WEDNESDAY OR FRIDAY?
				fast = (dummy.getDayOfWeek() == 3 || dummy.getDayOfWeek() == 5) ? 1 : 0;
			}

			retval[i] = fast;
			i++;
			dummy.addDays(1);
		} while (i < numdays);

		return retval;

	}

	// A METHOD TO OBTAIN MAJOR FEAST DAYS FOR A PARTICULAR YEAR
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A Hashtable OBJECT WITH THE FEASTS FOR THAT YEAR
	// FIRST ENTRY: THE julian date of a feast
	// SECOND ENTRY: A STRING DESCRIBING THAT FEAST
	// THROWS: ditto
	static Hashtable getFeasts(int year, Map<Object, Object> dayInfo) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		Hashtable feasts = new Hashtable();
		LanguagePack Text = new LanguagePack(dayInfo);
		String[] FeastNames = Text.obtainValues((String) Text.Phrases.get("Feasts"));
		// ADD ALL THE FIXED FEASTS TO OUR HASHTABLE
		feasts.put(new JDate(1, 1, year).getJulianDay(), FeastNames[0]);
		feasts.put(new JDate(1, 6, year).getJulianDay(), FeastNames[1]);
		feasts.put(new JDate(6, 24, year).getJulianDay(), FeastNames[2]);
		feasts.put(new JDate(6, 29, year).getJulianDay(), FeastNames[3]);
		feasts.put(new JDate(8, 6, year).getJulianDay(), FeastNames[4]);
		feasts.put(new JDate(8, 15, year).getJulianDay(), FeastNames[5]);
		feasts.put(new JDate(8, 29, year).getJulianDay(), FeastNames[6]);
		feasts.put(new JDate(9, 8, year).getJulianDay(), FeastNames[7]);
		feasts.put(new JDate(9, 14, year).getJulianDay(), FeastNames[8]);
		feasts.put(new JDate(10, 1, year).getJulianDay(), FeastNames[9]);
		feasts.put(new JDate(11, 21, year).getJulianDay(), FeastNames[10]);
		feasts.put(new JDate(12, 25, year).getJulianDay(), FeastNames[11]);

		// NOW ADD THE MOVEABLE FEASTS TO OUR HASHTABLE
		JDate pascha = getPascha(year);
		// DOUBLE CHECK THAT PASCHA IS NOT ON ANNUNCIATION:
		if (pascha.equals(new JDate(3, 25, year))) {
			feasts.put(pascha.getJulianDay(), FeastNames[12]);
		} else {
			feasts.put(pascha.getJulianDay(), FeastNames[13]);
			feasts.put(new JDate(3, 25, year).getJulianDay(), FeastNames[14]);
		}

		feasts.put(new JDate(pascha.getJulianDay() + 49).getJulianDay(), FeastNames[15]);
		feasts.put(new JDate(pascha.getJulianDay() + 39).getJulianDay(), FeastNames[16]);
		feasts.put(new JDate(pascha.getJulianDay() - 7).getJulianDay(), FeastNames[17]);

		// CHECK THAT MEETING OF THE LORD DOES NOT OCCUR ON THE FIRST MONDAY OF LENT
		JDate meeting = new JDate(2, 2, year);

		if (JDate.difference(pascha, meeting) == 48) {
			// MEETING OF THE LORD TRANSFERRED TO FORGIVENESS SUNDAY
			meeting.subtractDays(1);
		}

		feasts.put(meeting.getJulianDay(), FeastNames[18]);

		return feasts;
	}

	private static int[] getFasts(int year, int calendar) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		// A HASHTABLE WITH MANDATORY FAST DAYS IN THE YEAR
		Hashtable mustFast = new Hashtable();

		mustFast.put(new JDate2(1, 5, year, calendar).getJulianDay(), "Eve of Theophany");
		mustFast.put(new JDate2(8, 29, year, calendar).getJulianDay(), "Beheading");
		mustFast.put(new JDate2(9, 14, year, calendar).getJulianDay(), "Exaltation");

		// A HASHTABLE WITH MANDATORY FAST-FREE DAYS
		Hashtable cantFast = new Hashtable();

		cantFast.put(new JDate2(1, 6, year, calendar).getJulianDay(), "Theophany");

		// PASCHA
		JDate2 pascha = getPascha(year, calendar);

		// OTHER FASTING REGULATIONS
		JDate2 sviatkaStart = new JDate2(12, 25, year, calendar);
		JDate2 sviatkaEnd = new JDate2(1, 4, year, calendar);
		JDate2 pubPharStart = new JDate2(pascha.getJulianDay() - 70, calendar);
		JDate2 pubPharEnd = new JDate2(pascha.getJulianDay() - 63, calendar);
		JDate2 cheeseStart = new JDate2(pascha.getJulianDay() - 55, calendar);
		JDate2 cheeseEnd = new JDate2(pascha.getJulianDay() - 49, calendar);
		JDate2 lentStart = new JDate2(pascha.getJulianDay() - 48, calendar);
		JDate2 lentEnd = new JDate2(pascha.getJulianDay() - 1, calendar);
		JDate2 brightStart = new JDate2(pascha.getJulianDay(), calendar);
		JDate2 brightEnd = new JDate2(pascha.getJulianDay() + 6, calendar);
		JDate2 pentStart = new JDate2(pascha.getJulianDay() + 49, calendar);
		JDate2 pentEnd = new JDate2(pascha.getJulianDay() + 56, calendar);
		JDate2 apostolesStart = new JDate2(pascha.getJulianDay() + 57, calendar);
		JDate2 apostlesEnd = new JDate2(6, 28, year, calendar);
		JDate2 dormStart = new JDate2(8, 1, year, calendar);
		JDate2 dormEnd = new JDate2(8, 14, year, calendar);
		JDate2 adventStart = new JDate2(11, 15, year, calendar);
		JDate2 adventEnd = new JDate2(12, 24, year, calendar);

		JDate2 dummy = new JDate2(1, 1, year, calendar);

		boolean leapYear = false;
		int y = year;
		if (y % 400 == 0) {
			leapYear = true;
		} else if (y % 4 == 0 && y % 100 != 0 && calendar == 1) {
			leapYear = true;
		} else if (y % 4 == 0) {
			leapYear = true;
		}
		int numdays = leapYear ? 366 : 365;

		int[] retval = new int[numdays];

		int i = 0;

		do {
			// figure out if this day is a fast day
			int fast = 0;

			if (mustFast.containsKey(dummy.getJulianDay())) {
				// mandatory fast
				fast = 1;
			} else if (cantFast.containsKey(dummy.getJulianDay())) {
				// not a fast day
			} else if (dummy.compareTo(sviatkaStart) >= 0) {
				// not a fast day
			} else if (dummy.compareTo(sviatkaEnd) <= 0) {
				// not a fast day
			} else if (dummy.compareTo(pubPharStart) >= 0 && dummy.compareTo(pubPharEnd) <= 0) {
				// not a fast day
			} else if (dummy.compareTo(cheeseStart) >= 0 && dummy.compareTo(cheeseEnd) <= 0) {
				// CHEESEFARE WEEK
				fast = 2;
			} else if (dummy.compareTo(lentStart) >= 0 && dummy.compareTo(lentEnd) <= 0) {
				// LENT
				fast = 1;
			} else if (dummy.compareTo(brightStart) >= 0 && dummy.compareTo(brightEnd) <= 0) {
				// BRIGHT WEEK
				fast = 0;
			} else if (dummy.compareTo(pentStart) >= 0 && dummy.compareTo(pentEnd) <= 0) {
				// PENTECOST WEEK
				fast = 0;
			} else if (dummy.compareTo(apostolesStart) >= 0 && dummy.compareTo(apostlesEnd) <= 0) {
				// APOSTLES' FAST
				fast = 1;
			} else if (dummy.compareTo(dormStart) >= 0 && dummy.compareTo(dormEnd) <= 0) {
				// DORMITION FAST
				fast = 1;
			} else if (dummy.compareTo(adventStart) >= 0 && dummy.compareTo(adventEnd) <= 0) {
				// ADVENT
				fast = 1;
			} else {
				// IS THIS A WEDNESDAY OR FRIDAY?
				fast = (dummy.getDayOfWeek() == 3 || dummy.getDayOfWeek() == 5) ? 1 : 0;
			}

			retval[i] = fast;
			i++;
			dummy.addDays(1);
		} while (i < numdays);

		return retval;

	}

	// A METHOD TO OBTAIN MAJOR FEAST DAYS FOR A PARTICULAR YEAR
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A Hashtable OBJECT WITH THE FEASTS FOR THAT YEAR
	// FIRST ENTRY: THE julian date of a feast
	// SECOND ENTRY: A STRING DESCRIBING THAT FEAST
	// THROWS: ditto
	private static Hashtable<Long, String> getFeasts(int year, LinkedHashMap<Object, Object> dayInfo, int calendar)
			throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		Hashtable<Long, String> feasts = new Hashtable<>();
		LanguagePack Text = new LanguagePack(dayInfo);
		String[] feastNames = Text.obtainValues((String) Text.Phrases.get("Feasts"));
		// ADD ALL THE FIXED FEASTS TO OUR HASHTABLE
		feasts.put(new JDate2(1, 1, year, calendar).getJulianDay(), feastNames[0]);
		feasts.put(new JDate2(1, 6, year, calendar).getJulianDay(), feastNames[1]);
		feasts.put(new JDate2(6, 24, year, calendar).getJulianDay(), feastNames[2]);
		feasts.put(new JDate2(6, 29, year, calendar).getJulianDay(), feastNames[3]);
		feasts.put(new JDate2(8, 6, year, calendar).getJulianDay(), feastNames[4]);
		feasts.put(new JDate2(8, 15, year, calendar).getJulianDay(), feastNames[5]);
		feasts.put(new JDate2(8, 29, year, calendar).getJulianDay(), feastNames[6]);
		feasts.put(new JDate2(9, 8, year, calendar).getJulianDay(), feastNames[7]);
		feasts.put(new JDate2(9, 14, year, calendar).getJulianDay(), feastNames[8]);
		feasts.put(new JDate2(10, 1, year, calendar).getJulianDay(), feastNames[9]);
		feasts.put(new JDate2(11, 21, year, calendar).getJulianDay(), feastNames[10]);
		feasts.put(new JDate2(12, 25, year, calendar).getJulianDay(), feastNames[11]);

		// NOW ADD THE MOVEABLE FEASTS TO OUR HASHTABLE
		JDate2 pascha = getPascha(year, calendar);
		// DOUBLE CHECK THAT PASCHA IS NOT ON ANNUNCIATION:
		if (pascha.equals(new JDate2(3, 25, year, calendar))) {
			feasts.put(pascha.getJulianDay(), feastNames[12]);
		} else {
			feasts.put(pascha.getJulianDay(), feastNames[13]);
			feasts.put(new JDate2(3, 25, year, calendar).getJulianDay(), feastNames[14]);
		}

		feasts.put(new JDate2(pascha.getJulianDay() + 49, calendar).getJulianDay(), feastNames[15]);
		feasts.put(new JDate2(pascha.getJulianDay() + 39, calendar).getJulianDay(), feastNames[16]);
		feasts.put(new JDate2(pascha.getJulianDay() - 7, calendar).getJulianDay(), feastNames[17]);

		// CHECK THAT MEETING OF THE LORD DOES NOT OCCUR ON THE FIRST MONDAY OF LENT
		JDate2 meeting = new JDate2(2, 2, year, calendar);

		if (JDate2.difference(pascha, meeting) == 48) {
			// MEETING OF THE LORD TRANSFERRED TO FORGIVENESS SUNDAY
			meeting.subtractDays(1);
		}

		feasts.put(meeting.getJulianDay(), feastNames[18]);

		return feasts;
	}

	static JDate2 getPascha(int year, int calendar) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}
		if (year > 1582 && calendar == 1) {
			int a = year % 19;
			int b = (int) Math.floor(year / 100);
			int c = year % 100;
			int d = (int) Math.floor(b / 4);
			int e = b % 4;
			int f = (int) Math.floor((b + 8) / 25);
			int g = (int) Math.floor((b - f + 1) / 3);
			int h = (19 * a + b - d - g + 15) % 30;
			int i = (int) Math.floor(c / 4);
			int k = c % 4;
			int l = (32 + 2 * e + 2 * i - h - k) % 7;
			int m = (int) Math.floor((a + 11 * h + 22 * l) / 451);
			int n = (int) Math.floor((h + l - 7 * m + 114) / 31);
			int p = (h + l - 7 * m + 114) % 31;
			return new JDate2(n, p + 1, year, 1);
		} else {
			int a = year % 4;
			int b = year % 7;
			int c = year % 19;
			int d = (19 * c + 15) % 30;
			int e = (2 * a + 4 * b - d + 34) % 7;
			int f = (int) Math.floor((d + e + 114) / 31); // Month of pascha e.g. march=3
			int g = ((d + e + 114) % 31) + 1; // Day of pascha in the month
			// System.out.println("Easter on the Julian calendar is: "+year+"/"+f+"/"+g);
			// Create a JDate object
			return new JDate2(f, g, year, 0);
		}

	}

	static JDate2 getPentecost(int year, int calendar) throws IllegalArgumentException {
		if (year < 33) {
			throw (new IllegalArgumentException("Invalid year"));
		}

		JDate2 date = getPascha(year, calendar);

		date.addDays(49);
		return date;
	}

}
