package Ponomar;

import java.util.*;

/***************************************************************
 * Matins.java :: MODULE THAT TAKES THE GIVEN MATINS READINGS FOR THE DAY, THAT
 * IS, PENTECOSTARION, MENELOGION, AND FLOATERS AND RETURNS THE APPROPRIATE SET
 * OF READINGS FOR THE DAY AND THEIR ORDER.
 * 
 * Further work will convert this into the programme that will allow the
 * creation of the text for Matins.
 * 
 * Matins.java is part of the Ponomar project. Copyright 2012 Yuri Shardt
 * version 1.0: July 2012 yuri.shardt (at) gmail.com
 * 
 * PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE
 * CODE PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES
 * THEREOF.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **************************************************************/
public class Matins implements DocHandler {

	// private static final String generalFileName="Ponomar/xml/";
	private static final String triodionFileName = "xml/triodion/"; // TRIODION FILE
	private static final String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	private static LinkedHashMap<Object, Object> information; // CONTAINS COMMANDS ABOUT HOW TO CARRY OUT THE ORDERING
																// OF THE READINGS
	private static LanguagePack phrases;// = new LanguagePack();
	private static String[] transferredDays;// = Phrases.obtainValues((String) Phrases.Phrases.get("DayReading"));
	private static String[] error;// = Phrases.obtainValues((String) Phrases.Phrases.get("Errors"));
	private static Helpers findLanguage;// = new Helpers();
	private static final LinkedHashMap<Object, Object> tomorrowRead = new LinkedHashMap<>();
	private static final LinkedHashMap<Object, Object> yesterdayRead = new LinkedHashMap<>();
	private static final StringOp information3 = new StringOp();

	public Matins(Map<Object, Object> dayInfo) {
		information3.dayInfo = dayInfo;
		phrases = new LanguagePack(dayInfo);
		transferredDays = phrases.obtainValues((String) phrases.Phrases.get("DayReading"));
		error = phrases.obtainValues((String) phrases.Phrases.get("Errors"));
		findLanguage = new Helpers(information3.dayInfo);
	}

//THESE ARE THE SAME FUNCTION AS IN MAIN, BUT TRIMMED FOR THE CURRENT NEEDS
	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, Hashtable table) {
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

			if (!information3.evalbool(table.get("Cmd").toString())) {
				return;
			}
		}

		if (elem.equals("COMMAND")) {
			// THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE
			// RESULTS TO BE DETEMINED.
			String name = (String) table.get("Name");
			String value = (String) table.get("Value");
			// IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS
			// VALUES.
			if (information.containsKey(name)) {
				Vector<String> previous = (Vector) information.get(name);
				previous.add(value);
				information.put(name, previous);
			} else {
				Vector<String> vect = new Vector<>();
				vect.add(value);
				information.put(name, vect);
			}

		}
		// ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
	}

	public void endElement(String elem) {
	}

	public void text(String text) {
	}

	public String Readings(Map<Object, Object> readingsIn, JDate2 today) {
		/********************************************************
		 * SINCE I HAVE CORRECTED THE SCRIPTURE READINGS IN THE MAIN FILE, I CAN NOW
		 * PRECEDE WITH A BETTER VERSION OF THIS PROGRAMME!
		 ********************************************************/
		// PROCESS THE READINGS INTO THE DESIRED FORMS:
		classifyReadings orderedReadings = new classifyReadings(readingsIn);
		/*
		 * Information3.dayInfo.put("doy","12"); Information3.dayInfo.put("dow","1");
		 * Information3.dayInfo.put("nday","2");
		 * System.out.println("Testing the new StringOp formulation is " +
		 * Information3.evalbool("doy == 12"));
		 */

		information = new LinkedHashMap<>();
		int dow = Integer.parseInt(information3.dayInfo.get("dow").toString());

		// DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
		/*
		 * try { FileReader frf = new
		 * FileReader(findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(),
		 * "xml/Commands/Matins.xml")); Matins a1 = new Matins(); QDParser.parse(a1,
		 * frf); } catch (Exception e) { e.printStackTrace(); }
		 */
		// For the time being I will hard code the rules, as it is a simple one.
		// Suppress Sequential readings on Sunday if dRank > 6; otherwise suppress the
		// menaion readings.

		ArrayList<Object> dailyVf = new ArrayList<>();
		ArrayList<Object> dailyRf = new ArrayList<>();
		ArrayList<Object> dailyTf = new ArrayList<>();

		for (int i = 0; i < orderedReadings.dailyV.size(); i++) {
			dailyVf.add(orderedReadings.dailyV.get(i));
			dailyRf.add(orderedReadings.dailyR.get(i));
			dailyTf.add(dow);

		}
		int rankD = -2;
		for (int i = 0; i < orderedReadings.menaionV.size(); i++) {
			int rankCur = (int) orderedReadings.menaionR.get(i);
			if (rankCur > rankD) {
				dailyVf.add(orderedReadings.menaionV.get(i));
				dailyRf.add(orderedReadings.menaionR.get(i));
				dailyTf.add(orderedReadings.menaionT.get(i));
				rankD = rankCur;
			}
		}
		// System.out.println("VF: "+dailyVf+" Rf: " + dailyRf +" Tf: " +dailyTf);
		return format(dailyVf, dailyRf, dailyTf);
	}

	protected String Display(String a, String b, String c) {
		// THIS FUNCTION TAKES THE POSSIBLE 3 READINGS AND COMBINES THEM AS APPROPRIATE,
		// SO THAT NO SPACES OR OTHER UNDESIRED STUFF IS DISPLAYED!
		String output = "";
		if (!a.isEmpty()) {
			output += a;
		}
		if (!b.isEmpty()) {
			if (!output.isEmpty()) {
				output += information3.dayInfo.get("ReadSep") + " ";
			}
			output += b;
		}
		if (!c.isEmpty()) {
			if (!output.isEmpty()) {
				output += information3.dayInfo.get("ReadSep") + " ";
			}
			output += c;

		}

		// TECHNICALLY, IF THERE ARE 3 OR MORE READINGS, THEN SOME SHOULD BE TAKEN "FROM
		// THE BEGINNING" (nod zachalo).
		return output;
	}

	public String format(List<Object> vectV, List<Object> vectR, List<Object> vectT) {
		StringBuilder output = new StringBuilder();

		Bible shortForm = new Bible(information3.dayInfo);
		try {
			for (int k = 0; k < vectV.size(); k++) {
				String reading = (String) vectV.get(k);
				output.append(shortForm.getHyperlinkLoc(reading));

				if ((Integer) vectR.get(k) == -2) {
					if (vectV.size() > 1) {
						output.append(" (").append(Week(vectT.get(k).toString())).append(")");
					}
				} else if ((Integer) vectR.get(k) == -99) {

				} else {
					output.append(vectT.get(k));
				}

				if (k < vectV.size() - 1) {
					output.append(information3.dayInfo.get("ReadSep")); // IF THERE ARE MORE READINGS OF THE SAME TYPE
																		// APPEND A SEMICOLON!
				}
			}
		} catch (Exception a) {

			System.out.println(a);
			StackTraceElement[] trial = a.getStackTrace();
			System.out.println(trial[0].toString());

		}
		return output.toString();
	}

	private String Week(String dow) {
		// CONVERTS THE DOW STRING INTO A NAME. THIS SHOULD BE IN THE ACCUSATIVE CASE
		try {
			return transferredDays[Integer.parseInt(dow)];
		} catch (Exception a) {
			return dow; // A DAY OF THE WEEK WAS NOT SENT
		}
	}

	public static void main(String[] argz) {
	}

	class classifyReadings implements DocHandler {

		private LinkedHashMap<Object, Object> information2; // CONTAINS COMMANDS ABOUT HOW TO CARRY OUT THE ORDERING OF
															// THE READINGS
		public Vector dailyV = new Vector();
		public Vector dailyR = new Vector();
		public Vector dailyT = new Vector();
		public final Vector menaionV = new Vector();
		public final Vector menaionR = new Vector();
		public final Vector menaionT = new Vector();
		public final Vector suppressedV = new Vector();
		public final Vector suppressedR = new Vector();
		public final Vector suppressedT = new Vector();
		private final StringOp parameterValues = new StringOp();

		public classifyReadings() {
		}

		public classifyReadings(Map<Object, Object> readingsInA) {
			new StringOp();
			parameterValues.dayInfo = information3.dayInfo;
			classify(readingsInA);
		}

		public classifyReadings(Map<Object, Object> readingsInA, StringOp parameterValues) {
			classify(readingsInA);

		}

		private void classify(Map<Object, Object> readingsIn) {
			// Initialise Information.
			information2 = new LinkedHashMap<>();
			/*
			 * try { FileReader frf = new
			 * FileReader(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").
			 * toString(), "xml/Commands/Matins.xml"));
			 * //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get(
			 * "LS").toString(), "xml/Commands/DivineLiturgy.xml")); //DivineLiturgy a1 =
			 * new classifyReadin(); QDParser.parse(this, frf); } catch (Exception e) {
			 * e.printStackTrace(); }
			 */

			Vector paschalV = (Vector) readingsIn.get("Readings");
			Vector paschalR = (Vector) readingsIn.get("Rank");
			Vector paschalT = (Vector) readingsIn.get("Tag");

			dailyV = new Vector();
			dailyR = new Vector();
			dailyT = new Vector();

			if (paschalV == null) {
				return;
			}

			for (int k = 0; k < paschalV.size(); k++) {
				// System.out.println("Matins/k="+k+"\nRank is now: "+paschalR.get(0));
				if ((Integer) paschalR.get(0) == -2) {
					// THIS IS A DAILY READING THAT CAN BE SKIPPED, EXCEPT MAYBE ON SUNDAYS.
					dailyV.add(paschalV.get(k));
					if (k == paschalV.size() - 1) {
						dailyR.add(paschalR.get(0));
						dailyT.add(paschalT.get(0));
					} else {
						dailyR.add(-99);
						dailyT.add(-99);
					}
				} else {
					menaionV.add(paschalV.get(k));
					menaionR.add(paschalR.get(k));
					menaionT.add(paschalT.get(k));
				}

			}

			Suppress();
			// LeapReadings();

		}

		public void startDocument() {
		}

		public void endDocument() {
		}

		public void startElement(String elem, Hashtable table) {
			// THE TAG COULD CONTAIN A COMMAND Cmd
			// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
			// TODAY'S INFORMATION IN dayInfo.
			if (table.get("Cmd") != null) {
				// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

				if (!parameterValues.evalbool(table.get("Cmd").toString())) {
					return;
				}
			}

			if (elem.equals("COMMAND")) {
				// THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE
				// RESULTS TO BE DETEMINED.
				String name = (String) table.get("Name");
				String value = (String) table.get("Value");
				// IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS
				// VALUES.
				// System.out.println("==============================\nTesting
				// Information\n++++++++++++++++++++");
				if (information2.containsKey(name)) {
					Vector previous = (Vector) information2.get(name);
					previous.add(value);
					information2.put(name, previous);
				} else {
					Vector vect = new Vector();
					vect.add(value);
					information2.put(name, vect);
				}

			}
			// ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
		}

		public void endElement(String elem) {
		}

		public void text(String text) {
		}

		private void Suppress() {
			int dow = Integer.parseInt(parameterValues.dayInfo.get("dow").toString());
			int nday = Integer.parseInt(parameterValues.dayInfo.get("nday").toString());
			int dRank = Integer.parseInt(parameterValues.dayInfo.get("dRank").toString());
			// LeapReadings(); //THIS ALLOWS APPROPRIATE SKIPPING OF READINGS OVER THE
			// NATIVITY SEASON!

			if (dow == 0 && dRank > 6 && (nday < -49 || nday > 0)) {
				for (int k = 0; k < dailyV.size(); k++) {
					suppressedV.add(dailyV.get(k));
					suppressedR.add(dailyR.get(k));
					suppressedT.add(dailyT.get(k));
				}
				dailyV.clear();
				dailyR.clear();
				dailyT.clear();

				return; // There is no need for any other readings to be considered!
			}

			if (dow == 0 && dRank <= 6) {
				for (Object o : menaionV) {
					suppressedV.add(o);
					suppressedR.add(o);
					suppressedT.add(o);
				}
				menaionV.clear();
				menaionV.clear();
				menaionV.clear();
            }

		}

		protected void LeapReadings() {
			// USING THE NEWER VERSION OF STORED VALUES
			// EACH OF THE STORED COMMANDS ARE EVALUATED IF ANY ARE TRUE THEN THE READINGS
			// ARE SKIPPED IF THERE ARE ANY FURTHER READINGS ON THAT DAY.
			int available = menaionV.size();

			if (available > 0) {
				Vector vect = (Vector) information2.get("Suppress");
				if (vect != null) {
					for (Enumeration e2 = vect.elements(); e2.hasMoreElements();) {
						String command = (String) e2.nextElement();
						if (parameterValues.evalbool(command)) {
							// THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SKIPPED
							dailyV.clear();
							dailyR.clear();
							dailyT.clear();
							suppressedV.clear();
							suppressedR.clear();
							suppressedT.clear();
							return;
						}

					}
				}
			}

		}
	}

}
