package Ponomar;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/***********************************************************************
 * THIS MODULE READS XML FILES THAT CONTAIN THAT ARE OF THE <COMMEMORATION> TYPE
 * AND STORES THE INFORMATION IN A MANNER USUABLE BY OTHER COMPONENTS OF THE
 * PROGRAMME.
 * 
 * (C) 2009, 2015 YURI SHARDT. ALL RIGHTS RESERVED.
 * 
 * 2015 Changes: Updates and simplifications due to changes in the overall
 * standard.
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
 ***********************************************************************/
public class Commemoration1 implements DocHandler {

	private static final String Location = "xml/Services/menaion/"; // THE LOCATION OF THE BASIC SERVICE RULES
	private static final String LocationT = "xml/triodion/";
	private static final String LocationP = "xml/pentecostarion/";
	private static boolean read = false;
	private String filename;
	private int lineNumber;
	// private LanguagePack Text=new LanguagePack();
	// private String[]
	// ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	// private String[]
	// LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private final LinkedHashMap<Object, Object> information;
	private LinkedHashMap<Object, Object> readings;
	private LinkedHashMap<Object, Object> grammar;
	private LinkedHashMap<Object, Object> variable;
	private String textR;
	private boolean readRH = false;
	private LinkedHashMap<Object, Object> royalHours;
	private String elemRH;
	private LinkedHashMap<Object, Object> value;
	private LinkedHashMap<Object, Object> ServiceInfo;
	private String location1;
	private boolean readService = false;
	private LanguagePack Text;// = new LanguagePack();
	private String[] commNames;// = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
	private String errorName;// =(String)Text.Phrases.get("Commemoration3");
	private final Helpers helper;
	private boolean combine = false;
	private boolean skipElement = false;
	private final boolean presentPropers = false;
	private final StringOp analyse = new StringOp();

	protected Commemoration1(String SId, String CId, Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		commNames = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
		errorName = (String) Text.Phrases.get("Commemoration3");
		information = new LinkedHashMap<>();
		readings = new LinkedHashMap<>();
		royalHours = new LinkedHashMap<>();
		grammar = new LinkedHashMap<>();
		information.put("SID", SId);
		information.put("CID", CId);
		helper = new Helpers(analyse.dayInfo);
		ServiceInfo = new LinkedHashMap<>();
		readCommemoration(SId, CId);

	}

	protected Commemoration1() {
		information = new LinkedHashMap<>();
		readings = new LinkedHashMap<>();
		helper = new Helpers(analyse.dayInfo);
	}

	public void readCommemoration(String sId, String cId) // throws IOException
	{
		String fileName = "";
		try {
			combine = true;
			String language = analyse.dayInfo.get("LS").toString();
			String[] pathS = language.split("/");
			int path = pathS.length;
			StringBuilder pathF = new StringBuilder();

			for (int i = -1; i < path; i++) {
				if (i == -1) {
					pathF = new StringBuilder();
				} else {
					pathF.append("/").append(pathS[i]);
				}
				// System.out.println("pathF=" + pathF);

				fileName = "Ponomar/languages/" + pathF + "/xml/lives/" + cId + ".xml";
				File f = new File(fileName);

				if (f.exists()) {

					BufferedReader frf = new BufferedReader(
							new InputStreamReader(Files.newInputStream(Paths.get(fileName)), StandardCharsets.UTF_8));
					QDParser.parse(this, frf);
				} else {
					// The given file does not exist, do nothing, it is not a calamity!
				}
			}
			combine = true;
			// BufferedReader frf = new BufferedReader(new InputStreamReader(new
			// FileInputStream(helper.langFileFind(StringOp.dayInfo.get("LS").toString(),
			// "xml/lives/" + CId + ".xml")), "UTF8"));
			// QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("In file name, " + fileName + " an error occurred of type: ");
			e.printStackTrace();
		}
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, Hashtable table) {

		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		// IT WOULD BE VERY RARE IN THIS CASE
		skipElement = false;
		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			if (!analyse.evalbool(table.get("Cmd").toString())) {

				skipElement = true;

				return;
			}
		}
		// if(elem.equals("LANGUAGE"))
		// {
		read = true;
		// System.out.println(table.get("Cmd").toString());
		// return;
		// }
		if (elem.equals("SERVICE") && read) {
			readService = true;
			if (ServiceInfo == null) {
				ServiceInfo = new LinkedHashMap<>();
			}

			location1 = "";

			if (table.get("Type") != null) {
				information.put("Rank", Integer.parseInt(table.get("Type").toString()));
			}
			return;
		}
		if (readService && read) {
			location1 += "/" + elem;
			// elemRH=elem;
			value = new LinkedHashMap<>();
			for (Enumeration e = table.keys(); e.hasMoreElements();) {
				String type = (String) e.nextElement();
				value.put(type, table.get(type));
			}
			return;
		}

		if (readRH && read) {
			elemRH = elem;
			value = new LinkedHashMap<>();
			for (Enumeration e = table.keys(); e.hasMoreElements();) {
				String type = (String) e.nextElement();
				value.put(type, table.get(type));
			}
		}
		if (elem.equals("SCRIPTURE") && read) {
			String type = (String) table.get("Type");
			String reading = (String) table.get("Reading");
			if (readings.containsKey(type)) {

				// ADD THIS READING TO OTHERS OF THE SAME TYPE
				Vector vect = (Vector) readings.get(type);
				vect.add(reading);
				readings.put(type, vect);
			} else {
				// CREATE A NEW TYPE WITH A COLLECTION INCLUDING THIS READING
				Vector vect = new Vector();
				vect.add(reading);
				readings.put(type, vect);
			}
			information.put("Scripture", readings);
			// Information.put("presentPropers",true);
		}
		if (elem.equals("GRAMMAR") && read) {
			// THIS SHOULD ONLY BE READ ONCE PER LANGUAGE AND PASS!
			if (grammar == null) {
				grammar = new LinkedHashMap<>();
			}
			for (Enumeration e = table.keys(); e.hasMoreElements();) {
				String type = (String) e.nextElement();
				grammar.put(type, table.get(type));
			}
			information.put("Grammar", grammar);
		}
		// if (elem.equals("SERVICE") && read) {

		// Information.put("Cycle",table.get("Cycle").toString());
		// }
		if (elem.equals("ICON") && read) {
			information.put("Icon", table.get("Id").toString());
		}
		if (elem.equals("TROPARION") && read) {
			variable = new LinkedHashMap<>();
			variable.put("Tone", table.get("Tone").toString());
			if (table.get("Author") != null) {
				variable.put("Author", table.get("Author").toString());
			}
			// Information.put("presentPropers",true);
		}
		if (elem.equals("KONTAKION") && read) {
			variable = new LinkedHashMap<>();
			variable.put("Tone", table.get("Tone").toString());
			if (table.get("Author") != null) {
				variable.put("Author", table.get("Author").toString());
				// Information.put("presentPropers",true);
			}
		}
		if (elem.equals("NAME") && read) {
			// grammar=new LinkedHashMap<Object, Object>();
			// System.out.println("Hello World: This is Name testing!");
			for (Enumeration e = table.keys(); e.hasMoreElements();) {
				String type = (String) e.nextElement();
				grammar.put(type, table.get(type));
			}
			information.put("grammar", grammar);
			// Information.put("Nominative", table.get("Nominative").toString());
			// Information.put("Short", table.get("Short").toString());
			// Information.put("ShortFor", table.get("ShortF").toString());

		}
		if (elem.equals("LIFE") && read) {
			if (table.get("Id") != null) {
				information.put("LifeID", table.get("Id"));
			}
			if (table.get("Copyright") != null) {
				information.put("LifeCopyright", table.get("Copyright"));
			}
		}

	}

	public void endElement(String elem) {
		if (skipElement) {
			skipElement = false;
			return;
		}
		if (elem.equals("LANGUAGE")) {
			read = false;
		}
		if (elem.equals("ROYALHOURS")) {
			readRH = false;
		}
		if (elem.equals("SERVICE") && read) {
			// Information.put("presentPropers",true);
			readService = false;

			// System.out.println(ServiceInfo);
			// if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
			// System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
			// }
		}
		if (readService && read) {
			// System.out.println("Services");
			// System.out.println(textR);
			// System.out.println(Location1);
			// System.out.println(value.get("Type"));

			/*
			 * if(elem.equals("VERSE")){ System.out.println(textR); }
			 */
			// System.out.println("In endElement, I saw the following elements: "+elem);
			if (textR != null) {
				textR = textR.replace("\n", "").replace("\r", "");
				value.put("text", textR);
			}
			if (ServiceInfo.containsKey(location1)) {
				LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) ServiceInfo.get(location1);
				stuff.put(value.get("Type"), value);
				ServiceInfo.put(location1, stuff);
				/*
				 * if(elem.equals("VERSE")){ System.out.println(value.get("Type"));
				 * System.out.println(stuff.get(value.get("Type")));
				 * System.out.println(ServiceInfo.get(Location1)); }
				 */
				// Location1=Location1.substring(0,Location1.lastIndexOf("/"));
			} else {
				// CREATE A NEW LinkedHashMap<Object, Object> TO STORE THE DATA
				LinkedHashMap<Object, Object> stuff = new LinkedHashMap<>();

				if (!(value.get("Type") == null)) {
					// There are instances of this info
					stuff.put(value.get("Type"), value);

				} else {
					// There are no other instances of this info
					if (elemRH == null || value == null) {
						// System.out.println("A null set of values was encountered. Why? At point
						// elemRH = "+elemRH+" and value = "+value+" and location = "+Location1);
						if (location1.lastIndexOf("/") == -1) {
							location1 = "";
							return;

						}
						// System.out.println("****\n" + Location1 + "\n****\n");
						location1 = location1.substring(0, location1.lastIndexOf("/"));
						return;
					}
					stuff.put(elemRH, value);
					ServiceInfo.put(location1, value);
				}
				ServiceInfo.put(location1, stuff);
			}
			value = new LinkedHashMap<>();
			location1 = location1.substring(0, location1.lastIndexOf("/"));

			// return;
		}

		if (elem.equals("LIFE") && read) {
			information.put("LIFE", textR);
		}
		/*
		 * if(elem.equals("NAME") && read) { Information.put("Name",textR); }
		 */
	}

	public void text(String text) {
		textR = text;
	}

	public String getGrammar(String value) {
		if (Integer.parseInt(information.get("CID").toString()) != -1) {
			grammar = (LinkedHashMap<Object, Object>) information.get("grammar");

			if (value.isEmpty()) {
				// System.out.println( Information.get("Name").toString());
				return grammar.get("Nominative").toString();
			}
			try {
				return grammar.get(value).toString();
			} catch (Exception e) {
				if (grammar != null) {
					if (grammar.get("Nominative") != null) {
						return grammar.get("Nominative").toString();
					} else {
						return errorName;
					}
				} else {
					return errorName;
				}
			}
		} else {
			return information.get("Name").toString();
		}
	}

	public int getRank() {

		if (!information.containsKey("Rank")) {
			String CID = information.get("CID").toString();
			int Cidn = Integer.parseInt(CID);
			// System.out.println("CID: "+CID+"; length: "+CID.length());
			if ((Cidn >= 9000 && Cidn < 9900) && CID.length() == 4) {
				information.put("Rank", "-2");
				return -2;
			}
			return 0;
		}
		// System.out.println(Information.get("Rank").toString());
		int rank = Integer.parseInt(information.get("Rank").toString());
		String cID = information.get("CID").toString();
		int cIdn = Integer.parseInt(cID);
		if ((cIdn >= 9000 && cIdn < 9900) && cID.length() == 4) {
			if (rank < 2) {
				information.put("Rank", "-2");
				return -2;
			}
		}
		return rank;
	}

	public String getSId() {
		if (!information.containsKey("SID")) {
			return "";
		}
		return information.get("SID").toString();
	}

	public String getCId() {
		if (!information.containsKey("CID")) {
			return "";
		}
		return information.get("CID").toString();
	}

	public String getName() {
		return getGrammar("Nominative");
		/*
		 * if (!Information.containsKey("Nominative")){ return ""; } return
		 * Information.get("Nominative").toString();
		 */
	}

	public String getIcon() {
		return information.get("Icon").toString();
	}

	public Map<Object, Object> getDisplayIcons() {

		// Ordered List of the Icons
		Vector IconImages = new Vector();
		Vector IconNames = new Vector();

		String Cid = information.get("CID").toString();
		String NameF = getGrammar("Short");
		String[] IconSearch = Text.obtainValues((String) Text.Phrases.get("IconSearch"));

		File fileNew = new File(helper.langFileFind(analyse.dayInfo.get("LS").toString(), "/icons/" + Cid + "/0.jpg"));
		int countSearch = 0;
		String LanguageString = analyse.dayInfo.get("LS").toString();

		while (!(fileNew.exists()) && countSearch < IconSearch.length) {
			LanguageString = IconSearch[countSearch];
			fileNew = new File(helper.langFileFind(IconSearch[countSearch], "/icons/" + Cid + "/0.jpg"));
			countSearch += 1;
		}

		// The above code will add the Greek Icons and this will allow me to do what I
		// wish to do!!!

		int counterI = 0;

		// System.out.println(fileNew.getAbsolutePath());
		while (fileNew.exists()) {

			IconImages.add(fileNew.toString());
			IconNames.add(NameF);
			counterI += 1;
			fileNew = new File(helper.langFileFind(LanguageString, "/icons/" + Cid + "/" + counterI + ".jpg"));
		}
		File file = new File("Ponomar/images/icons/" + Cid + ".jpg");
		if (file.exists()) {
			IconImages.add(file.toString());
			IconNames.add(NameF);
		}

		LinkedHashMap<Object, Object> finalI = new LinkedHashMap<>();
		finalI.put("Images", IconImages);
		finalI.put("Names", IconNames);
		return finalI;
	}

	public String getID() {
		return information.get("ID").toString();
	}

	public String getCycle() {
		return information.get("Cycle").toString();
	}

	public Map<Object, Object> getReadings() {
		// return (LinkedHashMap<Object, Object>) Information.get("Scripture");
		// This is a list of all possible cases:
		// 1stHour,3rdHour,6thHour,9thHour,apostol,gospel,VESPERS,MATINS
		readings = new LinkedHashMap<>();
		Map<Object, Object> readingsT = getServiceNode("/VESPERS/SCRIPTURE");
		if (readingsT != null) {
			readings.put("VESPERS", readingsT);
		}
		readingsT = getServiceNode("/PRIMES/SCRIPTURE");
		if (readingsT != null) {
			readings.put("1st hour", readingsT);
		}
		readingsT = getServiceNode("/TERCE/SCRIPTURE");
		if (readingsT != null) {
			readings.put("3rd hour", readingsT);
		}
		readingsT = getServiceNode("/SEXTE/SCRIPTURE");
		if (readingsT != null) {
			readings.put("6th hour", readingsT);
		}
		readingsT = getServiceNode("/NONE/SCRIPTURE");
		if (readingsT != null) {
			readings.put("9th hour", readingsT);
		}
		readingsT = getServiceNode("/MATINS/SCRIPTURE");
		if (readingsT != null) {
			readings.put("MATINS", readingsT);
		}
		readingsT = getServiceNode("/LITURGY/SCRIPTURE");
		if (readingsT != null) {
			readings.put("LITURGY", readingsT);
		}

		return readings;
	}

	public Map<Object, Object> getServiceNode(String Node) {
		if (ServiceInfo != null) {
			if (ServiceInfo.containsKey(Node)) {
				LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) ServiceInfo.get(Node);

				return stuff;

			}
		}
		// System.out.println(CommNames[2] + Node);
		return null;

	}

	public Map<Object, Object> getService(String Node, String Type) {
		// System.out.println(ServiceInfo);
		// System.out.println("\n\n");
		// System.out.println(Node+"/"+Type);
		// System.out.println(ServiceInfo.get(Node));
		if (ServiceInfo.containsKey(Node)) {
			LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) ServiceInfo.get(Node);

			if (stuff.containsKey(Type)) {
				LinkedHashMap<Object, Object> stuff1 = (LinkedHashMap<Object, Object>) stuff.get(Type);

				return stuff1;
			} else {
				System.out.println(commNames[0] + Node + commNames[1] + Type);
				return null;
			}
		} else {
			System.out.println(commNames[2] + Node);
			return null;
		}
	}

	public Map<Object, Object> getRH(String Node, String Type) {

		if (royalHours.containsKey(Node)) {

			LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) royalHours.get(Node);
			// System.out.println(stuff);
			if (stuff.containsKey(Type)) {
				LinkedHashMap<Object, Object> stuff1 = (LinkedHashMap<Object, Object>) stuff.get(Type);
				return stuff1;
			} else {
				System.out.println(commNames[3]);
				return new LinkedHashMap<>();
			}
		} else {
			System.out.println(commNames[3]);
			return null;
		}

	}

	public boolean checkLife() {
		// Checks whether the given commemoration has an associated life or not

        return information.get("LIFE") != null;
    }

	public boolean checkIcon() {
		// Checks whether the given commemoration has any icons assoicated with it
		Map<Object, Object> checkIcon = getDisplayIcons();
        return !checkIcon.isEmpty();
    }

	public boolean checkPropers() {
		// Checks whether there are any associated propers for the given commemoration
		// that could be display.
		// At present only cares about the tropar and kondak.
		/*
		 * System.out.println(Information.get("CID")); LinkedHashMap<Object, Object>
		 * check1=getService("/LITURGY/TROPARION","1"); System.out.println(check1); if
		 * (check1 != null){ System.out.println("This is CCID: "+
		 * Information.get("CID")+" and check1 form: "+check1); return true; }
		 */
		/*
		 * if (Information.get("presentPropers")!=null){ boolean check=
		 * Boolean.parseBoolean(Information.get("presentPropers").toString()); if
		 * (check){ return true; } }
		 */
		return false;

	}

	public String getLife() {
		// Checks whether the given commemoration has an associated life or not

		if (information.get("LIFE") != null) {
			return information.get("LIFE").toString();
		} else {
			return null;
		}
	}

	public String getLifeCopyright() {
		// Checks whether the given commemoration has an associated life or not

		if (information.get("LifeCopyright") != null) {
			return information.get("LifeCopyright").toString();
		} else {
			return null;
		}
	}

	public static void main(String[] argz) {
		LinkedHashMap<Object, Object> dayInfo = new LinkedHashMap<>();
		dayInfo.put("LS", "en/");
		dayInfo.put("dow", "1");
		// StringOp.dayInfo.put("")
		// Commemoration Paramony = new Commemoration("P_3174"); //Paramony of Christmas
		System.out.println("THIS IS RUNNING ON DEBUG MODE, USING THE FILE FOR the Paramony of Christmas");
		// LinkedHashMap<Object, Object> stuff=Paramony.getRH("Idiomel","11");
		// System.out.println(Paramony.getService("/ROYALHOURS/IDIOMEL","13"));
		// System.out.println(Paramony .ServiceInfo());
		// System.out.println(Paramony.getRH("IDIOMEL","11"));
		// System.out.println(Paramony);
		Commemoration1 Paramony = new Commemoration1("0", "9001", dayInfo); // Forefeast of Christmas
		// System.out.println(Paramony.getService("/MATINS/KONTAKION","1"));
		System.out.println(Paramony.getRank());
		// System.out.println(Paramony.Information.get("LIFE"));
		System.out.println(Paramony.getGrammar("Nominative"));
		System.out.println("Rank = " + Paramony.getRank());
		System.out.println(Paramony.getService("/LITURGY/TROPARION", "1"));
		System.out.println(Paramony.getService("/LITURGY/KONTAKION", "1"));
		System.out.println(Paramony.getService("/VESPERS/SCRIPTURE", "3"));
		System.out.println(Paramony.getReadings().get("VESPERS"));
	}
}
