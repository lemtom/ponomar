package Ponomar;

import java.io.BufferedReader;
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
 * (C) 2009 YURI SHARDT. ALL RIGHTS RESERVED.
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

public class Commemoration implements DocHandler {
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
	private LinkedHashMap<Object, Object> serviceInfo;
	private String location1;
	private boolean readService = false;
	private LanguagePack Text;// =new LanguagePack();
	private String[] commNames;// =Text.obtainValues((String)Text.Phrases.get("Commemoration"));
	private final Helpers helper;
	private final StringOp analyse = new StringOp();

	protected Commemoration(String fileName, Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		information = new LinkedHashMap<>();
		readings = new LinkedHashMap<>();
		royalHours = new LinkedHashMap<>();
		information.put("ID", fileName);
		helper = new Helpers(analyse.dayInfo);
		readCommemoration(Location + fileName);
		Text = new LanguagePack(analyse.dayInfo);
		commNames = Text.obtainValues(Text.Phrases.get("Commemoration"));

	}

	protected Commemoration(String fileName, String type) {
		// This allows a more generalised approach to reading, those file that are found
		// not only in the menaion, but also in the triodion, pentecostarion, etc...
		// Codes: M: menaion
		// T: triodion
		// P: pentecostarion
		information = new LinkedHashMap<>();
		readings = new LinkedHashMap<>();
		royalHours = new LinkedHashMap<>();
		information.put("ID", fileName);
		helper = new Helpers(analyse.dayInfo);
		String filePath = "";
		if (type.equals("M")) {
			filePath = Location + fileName;
		}
		if (type.equals("T")) {
			filePath = LocationT + fileName;
		}
		if (type.equals("P")) {
			filePath = LocationP + fileName;
		}
		readCommemoration(filePath);
	}

	protected Commemoration() {
		information = new LinkedHashMap<>();
		readings = new LinkedHashMap<>();
		helper = new Helpers(analyse.dayInfo);
	}

	protected Commemoration(String name, LinkedHashMap<Object, Object> grammar,
			LinkedHashMap<Object, Object> readings) {
		// THIS WILL CREATE A QUASI-COMMEMORATION FILE ONLY GIVEN THE NAME OF THE
		// COMMEMORATION!
		information = new LinkedHashMap<>();
		information.put("Name", name);
		information.put("Rank", -1);
		information.put("Cycle", -1);
		information.put("Grammar", grammar);
		information.put("Scripture", readings);
		information.put("ID", "-1");
		helper = new Helpers(analyse.dayInfo);
	}

	public void readCommemoration(String FileName) // throws IOException
	{

		try {
			BufferedReader frf = new BufferedReader(new InputStreamReader(
					Files.newInputStream(
							Paths.get(helper.langFileFind(analyse.dayInfo.get("LS").toString(), FileName + ".xml"))),
					StandardCharsets.UTF_8));
			QDParser.parse(this, frf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startDocument() {

	}

	public void endDocument() {

	}

	public void startElement(String elem, HashMap<String, String> table) {

		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		// IT WOULD BE VERY RARE IN THIS CASE
		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

			if (!analyse.evalbool(table.get("Cmd"))) {

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
			serviceInfo = new LinkedHashMap<>();
			location1 = "";
			return;
		}
		if (readService && read) {
			location1 += "/" + elem;
			// elemRH=elem;
			value = new LinkedHashMap<>();
            value.putAll(table);
			return;
		}
		// if(elem.equals("ROYALHOURS") && read){
		// readRH=true;
		// RoyalHours=new LinkedHashMap<Object, Object>();
		// }
		if (readRH && read) {
			elemRH = elem;
			value = new LinkedHashMap<>();
            value.putAll(table);
		}
		if (elem.equals("SCRIPTURE") && read) {
			String type = table.get("Type");
			String reading = table.get("Reading");
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
		}
		if (elem.equals("GRAMMAR") && read) {
			// THIS SHOULD ONLY BE READ ONCE PER LANGUAGE AND PASS!
			grammar = new LinkedHashMap<>();
            grammar.putAll(table);
			information.put("Grammar", grammar);
		}
		if (elem.equals("CHURCH") && read) {
			information.put("Rank", table.get("Rank"));
			information.put("Cycle", table.get("Cycle"));
		}
		if (elem.equals("ICON") && read) {
			information.put("Icon", table.get("Id"));
		}
		if (elem.equals("TROPARION") && read) {
			variable = new LinkedHashMap<>();
			variable.put("Tone", table.get("Tone"));
			variable.put("Author", table.get("Author"));
		}
		if (elem.equals("KONTAKION") && read) {
			variable = new LinkedHashMap<>();
			variable.put("Tone", table.get("Tone"));
			variable.put("Author", table.get("Author"));
		}
		if (elem.equals("NAME") && read) {

		}
	}

	public void endElement(String elem) {
		if (elem.equals("LANGUAGE")) {
			read = false;
		}
		if (elem.equals("ROYALHOURS")) {
			readRH = false;
		}
		if (elem.equals("SERVICE") && read) {
			readService = false;
			// System.out.println(ServiceInfo);
			// if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
			// System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
			// }
		}
		if (readService && read) {
			/*
			 * if(elem.equals("VERSE")){ System.out.println(textR); }
			 */
			// System.out.println("In endElement, I saw the following elements: "+elem);
			if (textR != null) {
				value.put("text", textR);
			}
			if (serviceInfo.containsKey(location1)) {
				LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) serviceInfo.get(location1);
				stuff.put(value.get("Type"), value);
				serviceInfo.put(location1, stuff);
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
						location1 = location1.substring(0, location1.lastIndexOf("/"));
						return;
					}
					stuff.put(elemRH, value);
					serviceInfo.put(location1, value);
				}
				serviceInfo.put(location1, stuff);
			}
			value = new LinkedHashMap<>();
			location1 = location1.substring(0, location1.lastIndexOf("/"));

			// return;
		}

		/*
		 * if(elem.equals("TROPARION") && read) { variable.put("Troparion",textR);
		 * Information.put("Troparion",variable); } if(elem.equals("KONTAKION") && read)
		 * { variable.put("Kontakion",textR); Information.put("Kontakion",variable); }
		 */
		if (elem.equals("NAME") && read) {
			information.put("Name", textR);
		}
	}

	public void text(String text) {
		textR = text;
		// System.out.println(textR);
	}

	public String getGrammar(String value) {
		if (Integer.parseInt(information.get("ID").toString()) != -1) {
			grammar = (LinkedHashMap<Object, Object>) information.get(grammar);

			if (value.isEmpty()) {
				// System.out.println( Information.get("Name").toString());
				return information.get("Name").toString();
			}
			try {
				return grammar.get(value).toString();
			} catch (Exception e) {
				return information.get("Name").toString();
			}
		} else {
			return information.get("Name").toString();
		}
	}

	public String getRank() {
		return information.get("Rank").toString();
	}

	public String getIcon() {
		return information.get("Icon").toString();
	}

	public String getID() {
		return information.get("ID").toString();
	}

	public String getCycle() {
		return information.get("Cycle").toString();
	}

	public Map<Object, Object> getService(String node, String type) {
		// System.out.println(ServiceInfo);
		// System.out.println("\n\n");
		// System.out.println(Node+"/"+Type);
		if (serviceInfo.containsKey(node)) {
			LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) serviceInfo.get(node);

			if (stuff.containsKey(type)) {
				LinkedHashMap<Object, Object> stuff1 = (LinkedHashMap<Object, Object>) stuff.get(type);

				return stuff1;
			} else {
				System.out.println(commNames[0] + node + commNames[1] + type);
				return new LinkedHashMap<>();
			}
		} else {
			System.out.println(commNames[2] + node);
			return new LinkedHashMap<>();
		}
	}

	public Map<Object, Object> getRH(String mode, String type) {

		if (royalHours.containsKey(mode)) {

			LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) royalHours.get(mode);
			// System.out.println(stuff);
			if (stuff.containsKey(type)) {
				LinkedHashMap<Object, Object> stuff1 = (LinkedHashMap<Object, Object>) stuff.get(type);
				return stuff1;
			} else {
				System.out.println(commNames[3]);
				return new LinkedHashMap<>();
			}
		} else {
			System.out.println(commNames[3]);
			return new LinkedHashMap<>();
		}

	}

	public static void main(String[] argz) {
		LinkedHashMap<Object, Object> dayInfo = new LinkedHashMap<>();
		dayInfo.put("LS", "2");
		dayInfo.put("dow", "1");
		// Commemoration Paramony = new Commemoration("P_3174"); //Paramony of Christmas
		System.out.println("THIS IS RUNNING ON DEBUG MODE, USING THE FILE FOR the Paramony of Christmas");
		// LinkedHashMap<Object, Object> stuff=Paramony.getRH("Idiomel","11");
		// System.out.println(Paramony.getService("/ROYALHOURS/IDIOMEL","13"));
		// System.out.println(Paramony .ServiceInfo());
		// System.out.println(Paramony.getRH("IDIOMEL","11"));
		// System.out.println(Paramony);
		Commemoration Paramony = new Commemoration("01", dayInfo); // Forefeast of Christmas
		System.out.println(Paramony.getService("/MATINS/KONTAKION", "1"));
	}

}