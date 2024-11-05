package Ponomar;

import java.util.Map;

public abstract class JDateGeneric implements Comparable<JDateGeneric>, Cloneable {
	protected long mnJday;

	protected static final int[] daysInMonth = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	protected static final int[] daysInMonthLeap = new int[] { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	protected static String[] civilDayNames;// = Phrases.obtainValues((String)Phrases.Phrases.get("5"));

	protected static LanguagePack Phrases;// =new LanguagePack();
	protected static String[] monthNames;// =Phrases.obtainValues((String)Phrases.Phrases.get("3"));
	protected static String[] Errors;// =Phrases.obtainValues((String)Phrases.Phrases.get("Errors"));

	protected static String[] dayNames;// = Phrases.obtainValues((String)Phrases.Phrases.get("2"));
	protected static String[] civilMonthNames;// =Phrases.obtainValues((String)Phrases.Phrases.get("4"));
	protected static String Format;
	protected static final StringOp analyse = new StringOp();

	// A METHOD TO OBTAIN THE DAY OF WEEK FROM A JDATE OBJECT
	// PARAMETERS: NONE
	// RETURNS: AN INTEGER WITH THE DAY OF WEEK, WHERE SUNDAY = 0, MONDAY = 1, ETC.
	protected int getDayOfWeek() {
		int temp = (int) (mnJday % 7) + 1;
		if (temp == 7) {
			temp = 0;
		}

		return temp;
	}

	// A METHOD TO OBTAIN THE JULIAN DATE FROM A JDATE OBJECT
	// PARAMETERS: NONE
	// RETURNS: A long WITH THE JULIAN DATE
	protected long getJulianDay() {
		return mnJday;
	}

	// ADDS A SPECIFIED NUMBER OF DAYS TO A JDATE OBJECT
	// PARAMTERS: AN int WITH THE NUMBER OF DAYS TO BE ADDED
	// RETURNS: NONE
	protected synchronized void addDays(int n) {
		mnJday += n;
	}

	// SUBTRACTS A SPECIFIED NUMBER OF DAYS FROM A JDATE OBJECT
	// PARAMETERS: AN int WITH THE NUMBER OF DAYS TO BE SUBTRACTED
	// RETURNS: NONE
	protected synchronized void subtractDays(int n) {
		mnJday -= n;
	}

	// A METHOD TO OBTAIN A STRING FROM A JDATE OBJECT
	// PARAMETERS: NONE
	// RETURNS: A STRING WITH THE STRING VALUE OF A DATE
	public String toString(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Phrases = new LanguagePack(dayInfo);
		dayNames = Phrases.obtainValues(Phrases.Phrases.get("2"));
		civilMonthNames = Phrases.obtainValues(Phrases.Phrases.get("4"));
		monthNames = Phrases.obtainValues(Phrases.Phrases.get("3"));
		Format = Phrases.Phrases.get("DateFormat");
		int dow = getDayOfWeek();
		int year = getYear();
		int month = getMonth();
		int day = getDay();

		if (analyse.dayInfo.get("Ideographic") == null) {
			Format = Format.replace("WW", dayNames[dow]);
			Format = Format.replace("DD", String.valueOf(day));
			Format = Format.replace("MM", monthNames[month - 1]);
			Format = Format.replace("YY", String.valueOf(year));
			Format = Character.toUpperCase(Format.charAt(0)) + Format.substring(1);
		} else {
			if (analyse.dayInfo.get("Ideographic").equals("1")) {
				RuleBasedNumber convertN = new RuleBasedNumber(dayInfo);
				Format = Format.replace("WW", dayNames[dow]);
				Format = Format.replace("DD", convertN.getFormattedNumber(Long.parseLong(String.valueOf(day))));
				Format = Format.replace("MM", monthNames[month - 1]);
				Format = Format.replace("YY", convertN.getFormattedNumber(Long.parseLong(String.valueOf(year))));
				Format = Character.toUpperCase(Format.charAt(0)) + Format.substring(1);

			} else {
				Format = Format.replace("WW", dayNames[dow]);
				Format = Format.replace("DD", String.valueOf(day));
				Format = Format.replace("MM", monthNames[month - 1]);
				Format = Format.replace("YY", String.valueOf(year));
				Format = Character.toUpperCase(Format.charAt(0)) + Format.substring(1);
			}
		}

		return Format;
	}

	protected abstract int getDay();

	protected abstract int getMonth();

	protected abstract int getYear();

}
