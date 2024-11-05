package Ponomar;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

/***********************************************************************
 * Main.java :: MAIN MODULE FOR THE PONOMAR PROGRAM. THIS MODULE CONSTITUTES THE
 * PRIMARY PONOMAR GUI AND CENTRE OF THE PROGRAM. TO START THE PROGRAM, INVOKE
 * main(String[]) OF THIS CLASS. OUTPUTS RELEVANT INFORMATION FOR EACH DAY, WITH
 * LINKS TO DETAILED INFO.
 * 
 * Main.java is part of the Ponomar program. Copyright 2006, 2007, 2008, 2009,
 * 2010, 2012 Aleksandr Andreev and Yuri Shardt. Corresponding e-mail
 * aleksandr.andreev@gmail.com
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
 ***********************************************************************/
public class FindReadingList {
	// First, some relevant constants

	private static final String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
	// private static final String generalFileName="Ponomar/xml/";
	private static final String triodionFileName = "xml/triodion/"; // TRIODION FILE
	private static final String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	private static final String newline = "\n";
	// Elements of the interface
	final JDate2 today; // "TODAY" (I.E. THE DATE WE'RE WORKING WITH
	private final JDate2 pascha; // THIS YEAR'S PASCHA
	private JDate2 pentecost; // THIS YEAR'S PENTECOST
	private Stack fastInfo; // CONTAINS A VECTOR OF THE FASTING INFORMATION FOR TODAY, WHICH IS LATER PASSED
							// TO CONVOLVE()
	private Map<Object, Object> readings; // CONTAINS TODAY'S SCRIPTURE READING
	private String output; // TODAY'S CALENDAR OUTPUT
	private final Boolean inited = false; // PREVENTS MULTIPLE READING OF XML FILES ON LAUNCH
	private GospelSelector GospelLocation; // THE GOSPEL SELECTOR OBJECT
	private String gLocation; // STORES THE PATH (FOLDER) TO THE APPROPRIATE GOSPEL READING LOCATION FILES
	private final LanguageSelector languageLocation;
	// private String LLocation;
	// MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/19 n.s. YURI
	// SHARDT
	private LinkedHashMap<Object, Object> pentecostarionS; // CONTAINS THE PENTECOSTARION READINGS (SEQUENTIAL
															// (rjadovoje) READINGS!)
	private LinkedHashMap<Object, Object> menalogionS; // CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
	private LinkedHashMap<Object, Object> floaterS; // CONTAINS THE FLOATER READINGS.
	private LinkedHashMap<Object, Object>[] readScriptures;
	private final LanguagePack phrases;
	private static final boolean read = false; // DETERMINES WHICH LANGUAGE WILL BE READ
	private String rSep = "";
	private String cSep = "";
	private String colon = "";
	private String ideographic = "";
	private final StringOp analyse = new StringOp();
	private int religiousCal = 0;
	// private GospelSelector Selector;
	final Helpers findLanguage;

	// CONSTRUCTOR
	public FindReadingList(int year, int gs) {
		// super("Ponomar");

		ConfigurationFiles.Defaults = new LinkedHashMap<>();
		ConfigurationFiles.ReadFile();
		// DisplayCal=Integer.parseInt(ConfigurationFiles.Defaults.get("DisplayCalendar").toString());
		religiousCal = gs;// Integer.parseInt(ConfigurationFiles.Defaults.get("ReligiousCalendar").toString());
		languageLocation = new LanguageSelector(analyse.dayInfo);

		analyse.dayInfo.put("LS", languageLocation.getLValue());
		phrases = new LanguagePack(analyse.dayInfo);
		// Changing language storage format
		findLanguage = new Helpers(analyse.dayInfo);

		rSep = " ";
		cSep = phrases.Phrases.get("CommSep");
		colon = phrases.Phrases.get("Colon");
		analyse.dayInfo.put("ReadSep", rSep);
		analyse.dayInfo.put("Colon", colon);
		ideographic = phrases.Phrases.get("Ideographic");
		analyse.dayInfo.put("Ideographic", ideographic);
		// GospelLocation = new GospelSelector(analyse.dayInfo);

		pascha = Paschalion.getPascha(year, religiousCal);
		pascha.addDays(134);
		JDate2 start = pascha;

		today = new JDate2(start.getMonth(), start.getDay(), start.getYear(), religiousCal);
		int nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(), religiousCal));
		int ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1, religiousCal));
		StringBuilder outputE = new StringBuilder("Year: " + today.getYear());
		outputE.append("\n");
		StringBuilder outputG = new StringBuilder(outputE.toString());

		while (nday >= 134 || nday < -70) {
			outputE.append(nday).append(" M").append(today.getMonth()).append(".").append(today.getDay()).append(" ");
			outputG.append(nday).append(" M").append(today.getMonth()).append(".").append(today.getDay()).append(" ");
			int dow = today.getDayOfWeek();
			int doy = today.getDoy();
			nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(), religiousCal));
			ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1, religiousCal));
			// REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
			int ndayF = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() + 1, religiousCal));

			analyse.dayInfo.put("dow", dow); // THE DAY'S DAY OF WEEK
			analyse.dayInfo.put("doy", doy); // THE DAY'S DOY (see JDate.java for specification)
			analyse.dayInfo.put("nday", nday); // THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
			analyse.dayInfo.put("ndayP", ndayP); // THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
			analyse.dayInfo.put("ndayF", ndayF); // THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
			analyse.dayInfo.put("GS", 1);

			// INTERFACE LANGUAGE
			analyse.dayInfo.put("LS", languageLocation.getLValue());
			analyse.dayInfo.put("Year", today.getYear());
			analyse.dayInfo.put("dRank", 0); // The default rank for a day is 0. Y.S. 2010/02/01 n.s.
			analyse.dayInfo.put("Ideographic", ideographic);
			analyse.dayInfo.put("isLeapYear", today.isLeapYear(today.getYear()) ? 1 : 0);

			readings = new LinkedHashMap<>();
			// MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/24 n.s. YURI
			// SHARDT
			/*
			 * ReadScriptures = new LinkedHashMap<Object, Object>[3]; //CONTAINS A SORTED
			 * ARRAY OF ALL THE READINGS ReadScriptures[0] = new LinkedHashMap<Object,
			 * Object>(); //STORES THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje)
			 * READINGS!) ReadScriptures[1] = new LinkedHashMap<Object, Object>();
			 * //CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS ReadScriptures[2]
			 * = new LinkedHashMap<Object, Object>(); //CONTAINS THE FLOATER READINGS.
			 */
			// TESTING THE LANGUAGE PACKS
			String rough = phrases.Phrases.get("1");
			String[] final1 = rough.split(",");
			// System.out.println(output);
			String filename = "";
			int lineNumber = 0;

			if (nday >= -70 && nday < 0) {
				filename = triodionFileName;
				lineNumber = Math.abs(nday);
			} else if (nday < -70) {
				// WE HAVE NOT YET REACHED THE LENTEN TRIODION
				filename = pentecostarionFileName;
				JDate2 lastPascha = Paschalion.getPascha(today.getYear() - 1, religiousCal);
				lineNumber = (int) JDate2.difference(today, lastPascha) + 1;
			} else {
				// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
				filename = pentecostarionFileName;
				lineNumber = nday + 1;
			}

			filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
			// System.out.println("++++++++++++++++++++++\n"+filename+"\n+++++++++++++++++++\n");
			// System.out.println("File name in Main: " +
			// analyse.dayInfo.get("LS").toString());
			Day paschalCycle = new Day(filename, analyse.dayInfo);

			// READ THE PENTECOSTARION / TRIODION INFORMATION

			/*
			 * for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); ) {
			 * String type = (String)e.nextElement(); Vector vect =
			 * (Vector)readings.get(type);
			 * 
			 * ReadScriptures[0].put(type, vect); }
			 * 
			 * 
			 * readings.clear();
			 */

			// GET THE MENAION DATA, THESE MAY BE INDEPENDENT OF THE GOSPEL READING
			// IMPLEMENTATION, BUT WILL NOT BE SO IMPLEMENTED
			int m = today.getMonth();
			int d = today.getDay();

			filename = "xml/";
			filename += m < 10 ? "0" + m : "" + m; // CLEANED UP
			filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
			// filename += ".xml";
			Day solarCycle = new Day(filename, analyse.dayInfo);
			analyse.dayInfo.put("dRank", Math.max(solarCycle.getDayRank(), paschalCycle.getDayRank()));
			output = "";

			LinkedHashMap<String, Object>[] paschalReadings = paschalCycle.getReadings();
			// System.out.println("Length of Ordinary Readings="+PaschalReadings.length);

			LinkedHashMap<String, Object>[] menaionReadings = solarCycle.getReadings();
			Bible shortForm = new Bible(analyse.dayInfo);
			// System.out.println("First Paschal Reading is
			// :"+PaschalReadings[0].get("Readings"));
			// System.out.println("First Menologion Reading is
			// :"+MenaionReadings[0].get("Readings"));
			LinkedHashMap<String, Object> combinedReadings = new LinkedHashMap<>();
			// for(int j=0;j<7;j++){
			for (LinkedHashMap<String, Object> menaionReading : menaionReadings) {
				LinkedHashMap<String, Object> reading = (LinkedHashMap<String, Object>) menaionReading.get("Readings");
				LinkedHashMap<String, Object> readings = (LinkedHashMap<String, Object>) reading.get("Readings");
				for (Map.Entry<String, Object> entry : readings.entrySet()) {
                    String element1 = entry.getKey();
                    if (combinedReadings.get(element1) != null) {
						// Type of Reading already exists combine them
						LinkedHashMap<Object, Object> temp = (LinkedHashMap<Object, Object>) combinedReadings
								.get(element1);
						Vector readings2 = (Vector) temp.get("Readings");
						Vector rank = (Vector) temp.get("Rank");
						Vector tag = (Vector) temp.get("Tag");
						readings2.add(entry.getValue());
						rank.add(reading.get("Rank"));
						tag.add(reading.get("Name"));
						temp.put("Readings", readings2);
						temp.put("Rank", rank);
						temp.put("Tag", tag);
						combinedReadings.put(element1, temp);
					} else {
						// Reading does not exist
						Vector readings2 = new Vector();
						Vector rank = new Vector();
						Vector tag = new Vector();
						readings2.add(entry.getValue());
						rank.add(reading.get("Rank"));
						tag.add(reading.get("Name"));
						LinkedHashMap<Object, Object> temp = new LinkedHashMap<>();
						temp.put("Readings", readings2);
						temp.put("Rank", rank);
						temp.put("Tag", tag);
						combinedReadings.put(element1, temp);
					}
				}
			}
			for (LinkedHashMap<String, Object> paschalReading : paschalReadings) {
				LinkedHashMap<String, Object> reading = (LinkedHashMap<String, Object>) paschalReading.get("Readings");
				LinkedHashMap<String, Object> readings = (LinkedHashMap<String, Object>) reading.get("Readings");
				for (Map.Entry<String, Object> entry : readings.entrySet()) {
                    String element1 = entry.getKey();
                    if (combinedReadings.get(element1) != null) {
						// Type of Reading already exists combine them
						LinkedHashMap<Object, Object> temp = (LinkedHashMap<Object, Object>) combinedReadings
								.get(element1);
						Vector readings2 = (Vector) temp.get("Readings");
						Vector rank = (Vector) temp.get("Rank");
						Vector tag = (Vector) temp.get("Tag");
						readings2.add(entry.getValue());
						rank.add(reading.get("Rank"));
						tag.add(reading.get("Name"));
						temp.put("Readings", readings2);
						temp.put("Rank", rank);
						temp.put("Tag", tag);
						combinedReadings.put(element1, temp);
					} else {
						// Reading does not exist
						Vector readings2 = new Vector();
						Vector rank = new Vector();
						Vector tag = new Vector();
						readings2.add(entry.getValue());
						rank.add(reading.get("Rank"));

						tag.add(reading.get("Name"));
						LinkedHashMap<Object, Object> temp = new LinkedHashMap<>();
						temp.put("Readings", readings2);
						temp.put("Rank", rank);
						temp.put("Tag", tag);
						combinedReadings.put(element1, temp);
					}
				}
			}
			// }
			boolean firstTime = true;
			for (Map.Entry<String, Object> entry : combinedReadings.entrySet()) {
				// Temperary solution
				LinkedHashMap<Object, Object> temp = (LinkedHashMap<Object, Object>) entry.getValue();
				Vector Readings = (Vector) temp.get("Readings");
				Vector Rank = (Vector) temp.get("Rank");
				Vector Tag = (Vector) temp.get("Tag");
				if (entry.getKey().equals("LITURGY")) {
					if (firstTime) {
						firstTime = false;
					} else {
						output += rSep;
					}
					// Special case and consider it differently
					Vector epistle = new Vector();

					Vector gospel = new Vector();

					for (Object reading : Readings) {
						LinkedHashMap<Object, Object> liturgy = (LinkedHashMap<Object, Object>) reading;
						LinkedHashMap<Object, Object> stepE = (LinkedHashMap<Object, Object>) liturgy.get("apostol");
						LinkedHashMap<Object, Object> stepG = (LinkedHashMap<Object, Object>) liturgy.get("gospel");

						if (stepE != null) {
							epistle.add(stepE.get("Reading").toString());
						} else {
							epistle.add("");
						}
						if (stepG != null) {
							gospel.add(stepG.get("Reading").toString());
						} else {
							gospel.add("");
						}

					}
					LinkedHashMap<Object, Object> readingsA = new LinkedHashMap<>();

					if (!epistle.get(0).equals("")) {
						readingsA.put("Readings", epistle);
						readingsA.put("Rank", Rank);
						readingsA.put("Tag", Tag);
						// System.out.println(Tag);
						// System.out.println("Hello World");
						DivineLiturgy1 trial1 = new DivineLiturgy1(analyse.dayInfo);
						String type1 = phrases.Phrases.get("apostol");
						outputE.append(trial1.Readings(readingsA, "apostol", today));
						outputE.append(" \n");
					}
					if (!gospel.get(0).equals("")) {
						readingsA.put("Readings", gospel);
						readingsA.put("Rank", Rank);
						readingsA.put("Tag", Tag);
						String type1 = phrases.Phrases.get("gospel");
						DivineLiturgy1 trial1 = new DivineLiturgy1(analyse.dayInfo);
						outputG.append(trial1.Readings(readingsA, "gospel", today)).append(" \n");
					}

					/*
					 * for (int j=0; j<Readings.size();j++){ if (j!=0){ output+=RSep; } String
					 * BibleText=epistle.get(j).toString();
					 * 
					 * output+=ShortForm.getHyperlink(BibleText);
					 * 
					 * if (Readings.size()>1){ output+= Tag.get(j).toString(); } }
					 */

					/*
					 * for (int j=0; j<Readings.size();j++){ if (j!=0){ output+=RSep; } String
					 * BibleText=gospel.get(j).toString();
					 * output+=ShortForm.getHyperlink(BibleText);
					 * 
					 * if (Readings.size()>1){ output+= Tag.get(j).toString(); } }
					 */

				}

			} // output += RSep;
			today.addDays(1);
		}

//Closed the while loop above
		String epistle = "Epistle." + year + "." + religiousCal + ".csv";
		String gospel = "Gospel." + year + "." + religiousCal + ".csv";

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/Regression/" + epistle)), StandardCharsets.UTF_8));
			out.write(outputE.toString());
			out.close();
			out = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/Regression/" + gospel)), StandardCharsets.UTF_8));
			out.write(outputG.toString());
			out.close();
		} catch (IOException e) {
			// CANNOT BE MULTILINGUAL
			System.out.println("There was a problem:" + e);
		}

	}

	public static void main(String[] argz) {
		int[] yearsJulian = new int[] { 2487, 2288, 2209, 2053, 2211, 2128, 2056, 2214, 2131, 2132, 2486, 2134, 2135,
				2136, 2149, 2139, 2152, 2153, 2507, 2157, 2063, 2150, 2151, 2154, 2155, 2509, 2253, 2159, 2160, 2077,
				2078, 2527, 2176, 2177, 2083, 2254, 2087, 2525, 2100, 2174, 2175, 2004, 2179, 2001, 2192, 2193, 2099,
				2196, 2197, 2103, 2094, 2117, 2023, 2205, 2279, 2282, 2021, 2202, 2203, 2025, 2122, 2123, 2118, 2383,
				2222, 2060, 2084, 2535, 2091, 2456, 2095, 2457, 2024, 2027, 2028, 2031, 2035, 2064, 2085, 2104, 2105,
				2351, 2142, 2143, 2230, 2407, 2162, 2250, 2294, 2223, 2242, 2243, 2247, 2347, 2021, 2453, 2199, 2302,
				2303, 2322, 2271, 2283, 2287, 2291, 2488, 2267, 2275, 2307, 2338, 2295, 2315, 2331, 2335, 2439, 2423,
				2443, 2467, 2427, 2447, 2491, 2511, 2515, 2163, 2089, 2065, 2119, 2095 };
		int[] yearsGregorian = new int[] { 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011,
				2012, 2014, 2015, 2017, 2018, 2019, 2021, 2022, 2023, 2026, 2027, 2030, 2031, 2032, 2034, 2035, 2036,
				2037, 2038, 2039, 2041, 2042, 2043, 2046, 2047, 2048, 2049, 2050, 2051, 2054, 2055, 2056, 2057, 2058,
				2059, 2061, 2062, 2063, 2065, 2066, 2067, 2070, 2071, 2074, 2075, 2076, 2078, 2079, 2080, 2082, 2083,
				2089, 2091, 2092, 2095, 2098, 2099, 2112, 2115, 2119, 2120, 2123, 2132, 2136, 2139, 2140, 2143, 2147,
				2244, 2265, 2284, 2285, 2319, 2390, 2391, 2478, 2487, 2494, 2498, 2599, 2691, 2699, 2863, 2867, 2883,
				2887, 2890, 2894, 2971, 2982, 2990, 2991, 2999, 3134, 3263, 3275, 3279, 3283, 3290, 3783, 3791, 3891,
				4074, 4183, 4271, 4287, 4291, 4463, 4819, 4839, 4863, 5279, 5671, 5783, 6395, 6483, 7504, 7599, 8587,
				8739, 16567, 22267, 23255, 30095, 31083, 35795, 35947 };

		// for (int j=0;j<yearsJulian.length;j++)
		// {
		// System.out.println("Years Julian: "+yearsJulian[j]+"\n");
		// new FindReadingList(yearsJulian[j],0);
		// }

		for (int i : yearsGregorian) {
			System.out.println("Years Gregorian: " + i + "\n");
			new FindReadingList(i, 1);
		}
	}
}
