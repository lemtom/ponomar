package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/***********************************************************************
 * THIS MODULE CREATES THE TEXT FOR THE ORTHODOX SERVICE OF THE FIRST HOUR
 * (PRIME) THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.
 * 
 * (C) 2007, 2008, 2024 YURI SHARDT. ALL RIGHTS RESERVED. Updated some parts to
 * make it compatible with the changes in Ponomar, especially the language
 * issues!
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
public class Primes implements DocHandler, ActionListener, ItemListener, PropertyChangeListener {
	private static final String octoecheosFileName = "xml/Services/Octoecheos/"; // THE LOCATION OF THE BASIC SERVICE
																					// RULES
	private static final String ServicesFileName = "xml/Services/"; // THE LOCATION FOR ANY EXTRA INFORMATION
	private static String text;
	private static boolean read = false;
	private static String type;
	private String kontakion1;
	private static final String triodionFileName = "xml/triodion/"; // TRIODION FILE
	private static final String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	private String filename;
	private int lineNumber;
	private final LanguagePack Text;// =new LanguagePack();
	private final String[] primesNames;// =Text.obtainValues((String)Text.Phrases.get("Primes"));
	private final String[] languageNames;// =Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private String lentenK; // ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th
							// KATHISMA.
	private JFrame frames;
	private final String[] fileNames;// =Text.obtainValues((String)Text.Phrases.get("File"));
	private final String[] helpNames;// =Text.obtainValues((String)Text.Phrases.get("Help"));
	final String newline = "\n";
	private String strOut;
	private final JDate2 today;
	private final Helpers helper;
	private PrintableTextPane output;
	private final StringOp analyse = new StringOp();
	private String versionControl = "";
	private Day menologion;

	public Primes(JDate2 date, Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		primesNames = Text.obtainValues((String) Text.Phrases.get("Primes"));
		languageNames = Text.obtainValues((String) Text.Phrases.get("LanguageMenu"));
		fileNames = Text.obtainValues((String) Text.Phrases.get("File"));
		helpNames = Text.obtainValues((String) Text.Phrases.get("Help"));
		new PrimeSelector(dayInfo);

		// analyse.dayInfo = new Hashtable();
		// analyse.dayInfo.put("dow", Weekday); //DETERMINE THE DAY OF THE WEEK.

		// CREATING THE SERVICE
		today = date;
		helper = new Helpers(analyse.dayInfo);
		try {
			String strOut = createPrimes();
			if (strOut.equals("No Service Today")) {
				Object[] options = { languageNames[3] };
				JOptionPane.showOptionDialog(null, primesNames[0],
						(String) Text.Phrases.get("0") + Text.Phrases.get("Colon") + primesNames[1],
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			} else {
				primesWindow(strOut);
			}
		} catch (IOException j) {
		}

	}

	private void primesWindow(String textOut) {
		frames = new JFrame(Text.Phrases.get("0") + (String) Text.Phrases.get("Colon") + primesNames[1]);
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		textOut = textOut.replace("</br>", "<BR>");
		textOut = textOut.replace("<br>", "<BR>");
		strOut = textOut;
		// System.out.println(textOut);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		output = new PrintableTextPane();
		output.setEditable(false);
		output.setSize(800, 700);
		output.setContentType("text/html; charset=UTF-8");
		output.setText(textOut);
		output.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(output);
		JMenuBar menuBar = new JMenuBar();
		MenuFiles demo = new MenuFiles(analyse.dayInfo);
		PrimeSelector trial = new PrimeSelector(analyse.dayInfo);
		menuBar.add(demo.createFileMenu(this));
		menuBar.add(trial.createPrimeMenu());
		menuBar.add(demo.createHelpMenu(this));
		frames.setJMenuBar(menuBar);
		trial.addPropertyChangeListener(this);

		contentPane.add(scrollPane, BorderLayout.CENTER);
		frames.setContentPane(contentPane);
		frames.pack();
		frames.setSize(800, 700);
		frames.setVisible(true);

		Helpers orient = new Helpers(analyse.dayInfo);
		orient.applyOrientation(frames, (ComponentOrientation) analyse.dayInfo.get("Orient"));

		// scrollPane.top();
	}

	private String createPrimes() throws IOException {
		// OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		analyse.dayInfo.put("PS", PrimeSelector.getWhoValue());
		int typeP = PrimeSelector.getTypeValue();
		Service readPrime = new Service(analyse.dayInfo);
		// FIRST READ THE TONE FILES:
		int weekday = Integer.parseInt(analyse.dayInfo.get("dow").toString());
		// System.out.println(Weekday);
		int tone = Integer.parseInt(analyse.dayInfo.get("Tone").toString());
		if (tone == 8) {
			tone = 0;
		}
		// System.out.println(Tone);
		if (tone != -1) {
			String fileName = octoecheosFileName + "Tone " + tone;
			if (weekday == 1) {
				fileName = fileName + "/Monday.xml";
			} else if (weekday == 2) {
				fileName = fileName + "/Tuesday.xml";
			} else if (weekday == 3) {
				fileName = fileName + "/Wednesday.xml";
			} else if (weekday == 4) {
				fileName = fileName + "/Thursday.xml";
			} else if (weekday == 5) {
				fileName = fileName + "/Friday.xml";
			} else if (weekday == 6) {
				fileName = fileName + "/Saturday.xml";
			} else {
				fileName = fileName + "/Sunday.xml";
			}

			try {
				BufferedReader frf = new BufferedReader(new InputStreamReader(
						Files.newInputStream(
								Paths.get(helper.langFileFind(analyse.dayInfo.get("LS").toString(), fileName))),
						StandardCharsets.UTF_8));
				QDParser.parse(this, frf);

			} catch (Exception primes) {
				primes.printStackTrace();
			}
		}

		// READ THE PENTECOSTARION!

		// Integer.parseInt(dayInfo.get(expression).toString())
		int nday = Integer.parseInt(analyse.dayInfo.get("nday").toString());

		if (nday >= -70 && nday < 0) {
			filename = triodionFileName;
			lineNumber = Math.abs(nday);
		} else if (nday < -70) {
			// WE HAVE NOT YET REACHED THE LENTEN TRIODION
			filename = pentecostarionFileName;
			lineNumber = Integer.parseInt(analyse.dayInfo.get("ndayP").toString()) + 1;
		} else {
			// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
			filename = pentecostarionFileName;
			lineNumber = nday + 1;
		}

		filename += lineNumber >= 10 ? lineNumber + ".xml" : "0" + lineNumber + ".xml"; // CLEANED UP
		// READ THE PENTECOSTARION / TRIODION INFORMATION
		// IF THERE ARE SPECIAL TROPARION1's FROM THIS FILE THEY CAN OVERRIDE THE SET
		// PIECES

		try {
			BufferedReader frf = new BufferedReader(new InputStreamReader(
					Files.newInputStream(
							Paths.get(helper.langFileFind(analyse.dayInfo.get("LS").toString(), filename))),
					StandardCharsets.UTF_8));
			QDParser.parse(this, frf);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String filenameS = analyse.dayInfo.get("SolarPath").toString();
		menologion = new Day(filenameS, analyse.dayInfo);

		// CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
		// POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED
		// EXCEPT FOR ANY OVERRIDES
		ServiceInfo servicePrimes = new ServiceInfo("PRIME", analyse.dayInfo);
		Map<Object, Object> primesTrial = servicePrimes.ServiceRules();

		/*
		 * ServiceInfo ServicePrimesM=new
		 * ServiceInfo("PRIME",analyse.dayInfo.get("SolarPath").toString()+".xml",
		 * analyse.dayInfo); LinkedHashMap<Object, Object> PrimesTrialM =
		 * ServicePrimesM.ServiceRules();
		 * System.out.println("Information Found: "+PrimesTrialM);
		 * System.out.println("Override Type: "+PrimesTrialM.get("Type").toString());
		 * System.out.println("Override Troparion: "+PrimesTrialM.get("Troparion").
		 * toString());
		 */
		/*
		 * System.out.println(PrimesTrial.get("PickT"));
		 * System.out.println(PrimesTrial.get("Troparion"));
		 * System.out.println(Troparion1);
		 */
		type = primesTrial.get("Type").toString();
		lentenK = (String) primesTrial.get("LENTENK");
		versionControl = type + ".";

		if (type.equals("None")) {
			// THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
			return "No Service Today";
		} else if (type.equals("Paschal")) {

			return readPrime.startService(ServicesFileName + "PaschalHours.xml") + "\r\n" + versionControl;
		}

		// I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
		// DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE

		String strOut = "";
		analyse.dayInfo.put("PFlag1", typeP);
		analyse.dayInfo.put("PFlag2", 0);
		// NOTE PFlag2 == 3 for Holy Week Services!
		if (type.equals("Lenten")) {
			analyse.dayInfo.put("PFlag2", 1);

			if (lentenK != null) {
				analyse.dayInfo.put("PFlag2", 2);
				// CREATE THE KATHISMA PART
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKath.xml")),
								StandardCharsets.UTF_8));
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n<GET File=\"Kathisma" + lentenK
						+ "\" Null=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
			}
		} else {
			// CREATE THE FIRST TROPAR (BEFORE THE Glory...) PART, IF ANY
			// CREATE THE SECOND TROPAR (NORMAL)
			// APPROPRIATE TROPAR STILL NEEDS TO BE DETERMINED!!
			String options = primesTrial.get("Troparion").toString();
			String amount = primesTrial.get("PickT").toString();
			Map<Object, Object> troparia = troparia(options, amount);
			int amountI = Integer.parseInt(amount);
			int amountA = troparia.size(); // How many troparia did we actually find, we may find fewer than allowed.

			// When properly done, this will give me the maximum allowed. But what if there
			// are two allowed, but not available!?!
			if (Math.min(amountA, amountI) == 2) {
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop1.xml")),
								StandardCharsets.UTF_8));
				GetID dataID = (GetID) troparia.get(1);
				dataID.Header = "1";
				dataID.ToneA = "1";
				dataID.RedFirst = "1";
				dataID.NewLine = "1";
				dataID.Who = "R";
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n" + dataID.getHTML() + "\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
				versionControl += "T{" + dataID.GetFullID();
				dataID = (GetID) troparia.get(2);
				dataID.Header = "1";
				dataID.ToneA = "1";
				dataID.RedFirst = "1";
				dataID.NewLine = "1";
				dataID.Who = "R";
				data = "<SERVICES>\r\n<LANGUAGE>\r\n" + dataID.getHTML() + "\r\n</LANGUAGE>\r\n</SERVICES>";

				out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop2.xml")),
								StandardCharsets.UTF_8));
				out.write(data);
				out.close();
				versionControl += "," + dataID.GetFullID() + "}.";
			} else if (Math.min(amountA, amountI) == 1) {
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop2.xml")),
								StandardCharsets.UTF_8));
				GetID dataID = (GetID) troparia.get(1);
				// System.out.println(DataID);
				dataID.Header = "1";
				dataID.ToneA = "1";
				dataID.RedFirst = "1";
				dataID.NewLine = "1";
				dataID.Who = "R";
				dataID.Times = "1";
				versionControl += "T{" + dataID.GetFullID() + "}.";
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n" + dataID.getHTML() + "\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
				// System.out.println("TESTING COMPLETED");
				// Clearing the other Troparion file.
				out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop1.xml")),
								StandardCharsets.UTF_8));
				out.write("<SERVICE/>");
				out.close();
			} else {
				// We are in big trouble: no troparia were found, but we need at least one!
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop2.xml")),
								StandardCharsets.UTF_8));
				troparia.get(1);
				// System.out.println(DataID);
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n<TEXT Value=\"NO RELEVANT TROPARIA FOUND.\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
				System.out.println("Error reading troparia: None found!");

			}

			if (type.equals("HolyWeek")) {
				analyse.dayInfo.put("PFlag2", 3);
			}

		}

		// GET AND CREATE THE APPRORIATE KONTAKION
		// APROPRIATE KONTAKION MUST STILL BE CREATED!
		if (type.equals("Lenten")) {
			if (kontakion1 != null) {

				// Old Version, still only works for the Lenten Part
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKont1.xml")),
								StandardCharsets.UTF_8));
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/" + kontakion1
						+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
			}
		} else {
			String optionsK = primesTrial.get("Kontakion").toString();
			String amountK = primesTrial.get("PickK").toString();
			Map<Object, Object> troparia = contacia(optionsK, amountK);
			// if (Kontakion1 != null)
			{
				GetID dataID = (GetID) troparia.get(1);
				// System.out.println(DataID);
				dataID.Header = "1";
				dataID.ToneA = "1";
				dataID.RedFirst = "1";
				dataID.NewLine = "1";
				dataID.Who = "R";
				dataID.Times = "1";
				versionControl += "K{" + dataID.GetFullID() + "}";

				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKont1.xml")),
								StandardCharsets.UTF_8));
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n" + dataID.getHTML() + "\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
				/*
				 * Old Version, still only works for the Lenten Part BufferedWriter out = new
				 * BufferedWriter(new OutputStreamWriter(new
				 * FileOutputStream("Ponomar/languages/"+analyse.dayInfo.get("LS").toString()+
				 * ServicesFileName+"Var/PKont1.xml"),"UTF8")); String
				 * Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/"
				 * +Kontakion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>"
				 * ; out.write(Data); out.close();
				 */
				// VersionControl+="K{"+DataID.Id+"}";
			}
		}

		// System.out.println("Primes Case A: ");
		strOut = readPrime.startService(ServicesFileName + "Prime.xml") + "<footer><p>" + versionControl + "</p>";
		System.out.println("Type of Primes for Selected Day: " + versionControl);

		return strOut;
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

			if (!analyse.evalbool(table.get("Cmd").toString())) {

				return;
			}
		}
		// if(elem.equals("LANGUAGE") || elem.equals("TONE"))
		// {
		read = true;
		// }
		if (elem.equals("TEXT") && read) {
			text += (String) table.get("Value");

		}
		if (elem.equals("PRIMES") && read) {
			// WE ARE DEALING WITH THE INFORMATION FOR PRIMES (THERE COULD BE INFORMATION
			// FOR OTHER SERVICES)
			// THE VARIABLE COMPONETS IN THIS SERVICE ARE GIVEN BELOW
			String value = (String) table.get("Type");
			if (value != null) {
				type = (String) table.get("Type");
			}
			value = (String) table.get("TROPARION1");
			if (value != null) {
				table.get("TROPARION1");
			}
			value = (String) table.get("KONTAKION1");
			if (value != null) {
				kontakion1 = (String) table.get("KONTAKION1");
			}
			value = (String) table.get("KONTAKION2");
			if (value != null) {
				kontakion1 = (String) table.get("KONTAKION2");
			}
			value = (String) table.get("TROPARION2");
			if (value != null) {
				table.get("TROPARION2");
			}

			value = (String) table.get("LENTENK");
			if (value != null) {
				lentenK = (String) table.get("LENTENK");
				// System.out.println(LentenK);
			}

		}
		// OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

	}

	public void endElement(String elem) {
		if (elem.equals("LANGUAGE") || elem.equals("TONE")) {
			read = false;
		}
	}

	public void text(String text) {

	}

	public static void main(String[] argz) {

		// new Primes(3); //CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
	}

	public String readText(String filename) {
		try {
			text = "";
			BufferedReader fr = new BufferedReader(new InputStreamReader(
					Files.newInputStream(
							Paths.get(helper.langFileFind(analyse.dayInfo.get("LS").toString(), filename))),
					StandardCharsets.UTF_8));
			QDParser.parse(this, fr);
			if (text.isEmpty()) {
				text = null;
			}

		} catch (Exception e) {
			// SERIOUS PROBLEM MISSING A PART OF THE SERVICE!
			System.out.println(filename);
			e.printStackTrace();
			return null;
		}

		return text;
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String name = source.getText();
		if (name.equals(helpNames[2])) {
			Helpers orient = new Helpers(analyse.dayInfo);
			orient.applyOrientation(new About(analyse.dayInfo), (ComponentOrientation) analyse.dayInfo.get("Orient"));
		}
		if (name.equals(helpNames[0])) {
			// LAUNCH THE HELP FILE

		}
		if (name.equals(fileNames[1])) {
			// SAVE THE CURRENT WINDOW
			helper.SaveHTMLFile(primesNames[1] + " " + today, strOut);

		}
		if (name.equals(fileNames[4])) {
			// CLOSE THE PRIMES FRAME
			if (helper.closeFrame(languageNames[7])) {
				frames.dispose();
			}
		}
		if (name.equals(fileNames[6])) {
			// PRINT THE FILE
			helper.sendHTMLToPrinter(output);
		}
		String s = "Action event detected." + newline + "    Event source: " + source.getText() + " (an instance of "
				+ getClassName(source) + ")";
		System.out.println(s);
		// output.append(s + newline);
		// output.setCaretPosition(output.getDocument().getLength());
	}

	protected String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf(".");
		return classString.substring(dotIndex + 1);
	}

	public void itemStateChanged(ItemEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String s = "Item event detected." + newline + "    Event source: " + source.getText() + " (an instance of "
				+ getClassName(source) + ")" + newline + "    New state: "
				+ ((e.getStateChange() == ItemEvent.SELECTED) ? "selected" : "unselected");
		System.out.println(s);
		// output.append(s + newline);
		// output.setCaretPosition(output.getDocument().getLength());
	}

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??
		try {
			strOut = createPrimes();
			output.setText(strOut);
			output.setCaretPosition(0);
		} catch (Exception e1) {

		}

	}

	public Map<Object, Object> troparia(String options, String number) {
		// DETERMINE WHICH TROPARIA CAN BE USED TODAY GIVEN THE RULES
		LinkedHashMap<Object, Object> troparia = new LinkedHashMap<>();
		// FIRST PARSE THE options AND THEN DETERMINE HOW MANY
		String[] cases = options.split(",");
		int countT = 1;
		for (String aCase : cases) {
			// WE NOW NEED TO PARSE THE POTENTIAL TROPARIA
			// THE FIRST LETTER TELLS US HOW SPECIFIC THE REQUIREMENT IS
			String firstLetter = aCase.substring(0, 1);
			// System.out.println("First Letter is: "+firstLetter);
			switch (firstLetter) {
			case "S": {
				// WE ARE DEALING WITH A VERY SPECIFIC REQUIREMENT
				String[] specific = aCase.split("_");
				String cID = specific[1]; // THE SECOND ELEMENT IS ALWAYS THE REQUIRED FILE

				String tType = "1";
				if (specific.length > 2) {
					// A SPECIFIC TYPE HAS BEEN REQUIRED
					tType = specific[2];
				}
				GetID tropar = new GetID(cID, "/LITURGY/TROPARION/" + tType);
				troparia.put(countT, tropar);
				countT = countT + 1;
				// System.out.println("The specific troparion is "+CID);
				break;
			}
			case "T": {
				// WE NEED THE FILE CORRESPONDING TO THE TONE (I will ignore right now the
				// weekday issue)
				int tone = Integer.parseInt(analyse.dayInfo.get("Tone").toString());
				int dow = Integer.parseInt(analyse.dayInfo.get("dow").toString());
				int fileName = 0;
				if (dow == 0) {
					fileName = 9700 + tone;
					if (tone == 8) {
						fileName = 9700;
					}
				} else {
					fileName = 9710 + dow;
				}
				GetID tropar = new GetID(Integer.toString(fileName), "/LITURGY/TROPARION/1");
				troparia.put(countT, tropar);
				countT = countT + 1;

				// System.out.println("The specific troparion based on today�s tone is
				// "+FileName);
				// System.out.println("The specific call is "+tropar.getHTML());
				break;
			}
			case "M":
				// WE NEED THE FILE CORRESPONDING TO THE HIGHEST (OR IF TIED, FIRST) RANKED
				// HOLIDAY

				int dRankM = Integer.parseInt(analyse.dayInfo.get("dRankM").toString());
				// Day menologion=(Day) analyse.dayInfo.get("SolarCycle");
				for (Commemoration1 CurrentC : menologion.getCommemorations()) {
					if (CurrentC.getRank() == dRankM) {
						// We have found a commemoration with a given day rank. We will now search all
						// such commemorations until we find a troparion!
						String node = "/LITURGY/TROPARION";

						if (CurrentC.getService(node, "1") != null) {
							// I should search over other numbers, but I will assume that if "1" is present,
							// then it will be taken!
							// We are in luck, there is a troparion. Add it!
							Map<Object, Object> testing = CurrentC.getService(node, "1");
							// System.out.println(testing);
							if (testing.get("text").toString().length() > 1) {
								String fileName = CurrentC.getCId();
								// System.out.println("The troparion found is for "+FileName);
								GetID tropar = new GetID(fileName, "/LITURGY/TROPARION/1");
								troparia.put(countT, tropar);
								countT = countT + 1;
							}

						}
					}
				}
				/*
				 * Still needs to be worked out.
				 */
				// System.out.println("The Menologion cannot at present be asked.");
				// countT=countT+1;
				break;
			}
			// System.out.println("counterT"+counterT);
		}
		// System.out.println("We stored " +countT+" troparia.");
		return troparia;
	}

	public Map<Object, Object> contacia(String options, String number) {
		// DETERMINE WHICH TROPARIA CAN BE USED TODAY GIVEN THE RULES
		LinkedHashMap<Object, Object> troparia = new LinkedHashMap<>();
		// FIRST PARSE THE options AND THEN DETERMINE HOW MANY
		String[] cases = options.split(",");
		int countT = 1;
		for (String aCase : cases) {
			// WE NOW NEED TO PARSE THE POTENTIAL TROPARIA
			// THE FIRST LETTER TELLS US HOW SPECIFIC THE REQUIREMENT IS
			String firstLetter = aCase.substring(0, 1);
			// System.out.println("First Letter is: "+firstLetter);
			switch (firstLetter) {
			case "S": {
				// WE ARE DEALING WITH A VERY SPECIFIC REQUIREMENT
				String[] specific = aCase.split("_");
				String cID = specific[1]; // THE SECOND ELEMENT IS ALWAYS THE REQUIRED FILE

				String ttType = "1";
				if (specific.length > 2) {
					// A SPECIFIC TYPE HAS BEEN REQUIRED
					ttType = specific[2];
				}
				GetID tropar = new GetID(cID, "/LITURGY/KONTAKION/" + ttType);
				troparia.put(countT, tropar);
				countT = countT + 1;
				// System.out.println("The specific troparion is "+CID);
				break;
			}
			case "T": {
				// WE NEED THE FILE CORRESPONDING TO THE TONE (I will ignore right now the
				// weekday issue)
				int tone = Integer.parseInt(analyse.dayInfo.get("Tone").toString());
				int dow = Integer.parseInt(analyse.dayInfo.get("dow").toString());
				int fileName = 0;
				if (dow == 0) {
					fileName = 9700 + tone;
					if (tone == 8) {
						fileName = 9700;
					}
				} else {
					fileName = 9710 + dow;
				}
				GetID tropar = new GetID(Integer.toString(fileName), "/LITURGY/KONTAKION/1");
				troparia.put(countT, tropar);
				countT = countT + 1;
				System.out.println("The specific contacion based on today�s tone is " + fileName);
				break;
			}
			case "M":
				// WE NEED THE FILE CORRESPONDING TO THE HIGHEST (OR IF TIED, FIRST) RANKED
				// HOLIDAY

				int dRankM = Integer.parseInt(analyse.dayInfo.get("dRankM").toString());
				// Day menologion=(Day) analyse.dayInfo.get("SolarCycle");
				for (Commemoration1 CurrentC : menologion.getCommemorations()) {
					if (CurrentC.getRank() == dRankM) {
						// We have found a commemoration with a given day rank. We will now search all
						// such commemorations until we find a troparion!
						String node = "/LITURGY/KONTAKION";

						if (CurrentC.getService(node, "1") != null) {
							// I should search over other numbers, but I will assume that if "1" is present,
							// then it will be taken!
							// We are in luck, there is a troparion. Add it!
							Map<Object, Object> testing = CurrentC.getService(node, "1");
							// System.out.println(testing);
							if (testing.get("text").toString().length() > 1) {
								String fileName = CurrentC.getCId();
								// System.out.println("The troparion found is for "+FileName);
								GetID tropar = new GetID(fileName, "/LITURGY/KONTAKION/1");
								troparia.put(countT, tropar);
								countT = countT + 1;
							}

						}
					}
				}
				break;
			}
		}
		// System.out.println("We stored " +countT+" troparia.");
		return troparia;
	}

}
