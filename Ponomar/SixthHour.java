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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/***********************************************************************
 * THIS MODULE CREATES THE TEXT FOR THE ORTHODOX SERVICE OF THE FIRST HOUR
 * (PRIME) THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.
 * 
 * (C) 2007, 2008 YURI SHARDT. ALL RIGHTS RESERVED. Updated some parts to make
 * it compatible with the changes in Ponomar, especially the language issues!
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
public class SixthHour implements DocHandler, ActionListener, ItemListener, PropertyChangeListener {
	// SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	// THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	// TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	// DURING THE COURSE OF A SINGLE WEEK.

	private static final String octoecheosFileName = "xml/Services/Octoecheos/"; // THE LOCATION OF THE BASIC SERVICE
																					// RULES
	private static final String ServicesFileName = "xml/Services/"; // THE LOCATION FOR ANY EXTRA INFORMATION
	private static String text;
	private static boolean read = false;
	private static String type;
	private String troparion1;
	private String kontakion1;
	private String troparion2;
	private static final String triodionFileName = "xml/triodion/"; // TRIODION FILE
	private static final String pentecostarionFileName = "/xml/pentecostarion/"; // PENTECOSTARION FILE
	private String filename;
	private int lineNumber;
	private final LanguagePack Text;// = new LanguagePack();
	private final String[] primesNames;// = Text.obtainValues((String) Text.Phrases.get("Sexte"));
	private final String[] languageNames;// = Text.obtainValues((String) Text.Phrases.get("LanguageMenu"));
	private String lentenK; // ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th
							// KATHISMA.
	private JFrame frames;
	private final String[] fileNames;// = Text.obtainValues((String) Text.Phrases.get("File"));
	private final String[] helpNames;// = Text.obtainValues((String) Text.Phrases.get("Help"));
	final String newline = "\n";
	private String strOut;
	private final JDate2 today;
	private final Helpers helper;
	private PrintableTextPane output;
	private String reading6th = "";
	private final StringOp analyse = new StringOp();

	public SixthHour(JDate2 date, Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		primesNames = Text.obtainValues(Text.Phrases.get("Sexte"));
		languageNames = Text.obtainValues(Text.Phrases.get("LanguageMenu"));
		fileNames = Text.obtainValues(Text.Phrases.get("File"));
		helpNames = Text.obtainValues(Text.Phrases.get("Help"));
		new PrimeSelector(dayInfo);

		// analyse.dayInfo = new HashMap();
		// analyse.dayInfo.put("dow", Weekday); //DETERMINE THE DAY OF THE WEEK.

		// CREATING THE SERVICE
		today = date;
		helper = new Helpers(analyse.dayInfo);
		reading6th = "";
		try {
			String strOut = createPrimes();
			if (strOut.equals("No Service Today")) {
				Object[] options = { languageNames[3] };
				JOptionPane.showOptionDialog(null, primesNames[0],
						Text.Phrases.get("0") + Text.Phrases.get("Colon") + primesNames[1],
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			} else {
				PrimesWindow(strOut);
			}
		} catch (IOException j) {
		}

	}

	private void PrimesWindow(String textOut) {
		frames = new JFrame(Text.Phrases.get("0") + Text.Phrases.get("Colon") + primesNames[1]);
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

		filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
		// READ THE PENTECOSTARION / TRIODION INFORMATION
		// IF THERE ARE SPECIAL TROPARION1's FROM THIS FILE THEY CAN OVERRIDE THE SET
		// PIECES

		Day readings = new Day(filename, analyse.dayInfo);
		try {
			LinkedHashMap<String, Object>[] lessons = readings.getReadings();
			LinkedHashMap<String, Object> readings1 = (LinkedHashMap<String, Object>) lessons[0].get("Readings");
			LinkedHashMap<String, Object> lesson = (LinkedHashMap<String, Object>) readings1.get("Readings");
			LinkedHashMap<String, Object> reading = (LinkedHashMap<String, Object>) lesson.get("6th hour");
			// System.out.println("Reading == " +reading);
			LinkedHashMap<String, Object> lesson2 = (LinkedHashMap<String, Object>) reading.get("1");
			// System.out.println("Lesson 2 == "+lesson2);
			reading6th = lesson2.get("Reading").toString();
		} catch (Exception e) { // There are no appointed readings
			reading6th = "";
		}

		// CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
		// POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED
		// EXCEPT FOR ANY OVERRIDES
		ServiceInfo servicePrimes = new ServiceInfo("SEXTE", analyse.dayInfo);
		Map<Object, Object> primesTrial = servicePrimes.ServiceRules();

		type = primesTrial.get("Type").toString();
		lentenK = (String) primesTrial.get("LENTENK");

		String primesAdd1 = "";

		if (type.equals("None")) {
			// THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
			return "No Service Today";
		} else if (type.equals("Paschal")) {

			return readPrime.startService(ServicesFileName + "PaschalHours.xml");
		}

		// I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
		// DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE

		String strOut = "";
		analyse.dayInfo.put("PFlag1", typeP);
		analyse.dayInfo.put("PFlag2", 0);
		analyse.dayInfo.put("PFlag3", 0);
		// NOTE PFlag2 == 3 for Holy Week Services!

		if (type.equals("Lenten")) {
			analyse.dayInfo.put("PFlag2", 1);

			if (lentenK != null) {
				analyse.dayInfo.put("PFlag2", 2);
				// CREATE THE KATHISMA PART
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKath6.xml")),
								StandardCharsets.UTF_8));
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n<GET File=\"Kathisma" + lentenK
						+ "\" Null=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
			}
			// System.out.println("Hello Lent b");
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(
					"Ponomar/languages/" + analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/TP6R.xml")),
					StandardCharsets.UTF_8));
			// System.out.println(Reading6th);
			String data = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out1a = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(
					"Ponomar/languages/" + analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/TP6C.xml")),
					StandardCharsets.UTF_8));
			String data1a = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK61R.xml")),
					StandardCharsets.UTF_8));
			String data1 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK61C.xml")),
					StandardCharsets.UTF_8));
			String data2 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out3 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/STYX61R.xml")),
					StandardCharsets.UTF_8));
			String data3 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out4 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/STYX61C.xml")),
					StandardCharsets.UTF_8));
			String data4 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out5 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK61a.xml")),
					StandardCharsets.UTF_8));
			String data5 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out6 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK61b.xml")),
					StandardCharsets.UTF_8));
			String data6 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out7 = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(
					"Ponomar/languages/" + analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/Intro6.xml")),
					StandardCharsets.UTF_8));
			String data7 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out8 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/Reading6.xml")),
					StandardCharsets.UTF_8));
			String data8 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out9 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK62R.xml")),
					StandardCharsets.UTF_8));
			String data9 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out10 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK62C.xml")),
					StandardCharsets.UTF_8));
			String data10 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out11 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/STYX62R.xml")),
					StandardCharsets.UTF_8));
			String data11 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out12 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/STYX62C.xml")),
					StandardCharsets.UTF_8));
			String data12 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out13 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK62a.xml")),
					StandardCharsets.UTF_8));
			String data13 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			BufferedWriter out14 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PROK62b.xml")),
					StandardCharsets.UTF_8));
			String data14 = "<SERVICE>\r\n<LANGUAGE>\r\n";
			if (reading6th != null && !reading6th.isEmpty()) {
				// System.out.println(Reading6th);
				analyse.dayInfo.put("PFlag3", 1);
				String nday1 = String.valueOf(-nday);
				if (-nday < 10) {
					nday1 = "0" + nday1;
				}

				data = data + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" Header=\"1\" What=\"/SEXTE/TROPARION/1\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\" />";
				data1a = data1a + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/TROPARION/1\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
				data1 = data1 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" Header=\"1\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
				data1 = data1 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"R\"/>";
				data2 = data2 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
				data2 = data2 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"C\"/>";
				data3 = data3 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/STICHOS/1\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
				data4 = data4 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/STICHOS/1\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
				data5 = data5 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
				data6 = data6 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"C\" NewLine=\"1\"/>";
				data7 = data7 + "\r\n<BIBLE getReading=\"" + reading6th + "\" Who=\"SR\" NewLine=\"1\"/>";
				data8 = data8 + "\r\n<BIBLE Verses=\"" + reading6th
						+ "\" Who=\"SR\" RedFirst=\"1\" Header=\"1\" NewLine=\"1\" />";
				data9 = data9 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\" Header=\"1\"/>";
				data9 = data9 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"R\" />";
				data10 = data10 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
				data10 = data10 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"C\" />";
				data11 = data11 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/STICHOS/2\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
				data12 = data12 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/STICHOS/2\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
				data13 = data13 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
				data14 = data14 + "\r\n<GETID Type=\"T\" Id=\"" + nday1
						+ "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"C\" NewLine=\"1\"/>";

			}

			data = data + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data1a = data1a + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data1 = data1 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data2 = data2 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data3 = data3 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data4 = data4 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data5 = data5 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data6 = data6 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data7 = data7 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data8 = data8 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data9 = data9 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data10 = data10 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data11 = data11 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data12 = data12 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data13 = data13 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			data14 = data14 + "\r\n</LANGUAGE>\r\n</SERVICE>";
			out.write(data);
			out1a.write(data1a);
			out1.write(data1);
			out2.write(data2);
			out3.write(data3);
			out4.write(data4);
			out5.write(data5);
			out6.write(data6);
			out7.write(data7);
			out8.write(data8);
			out9.write(data9);
			out10.write(data10);
			out11.write(data11);
			out12.write(data12);
			out13.write(data13);
			out14.write(data14);
			out.close();
			out1a.close();
			out1.close();
			out2.close();
			out3.close();
			out4.close();
			out5.close();
			out6.close();
			out7.close();
			out8.close();
			out9.close();
			out10.close();
			out11.close();
			out12.close();
			out13.close();
			out14.close();
		} else {
			// CREATE THE FIRST TROPAR (BEFORE THE Glory...) PART, IF ANY
			// CREATE THE SECOND TROPAR (NORMAL)
			// APPROPRIATE TROPAR STILL NEEDS TO BE DETERMINED!!
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PTrop61.xml")),
					StandardCharsets.UTF_8));
			String data = "<SERVICE>\r\n<LANGUAGE>";
			String data2 = "<SERVICE>\r\n<LANGUAGE>";
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
							+ ServicesFileName + "Var/PTrop62.xml")),
					StandardCharsets.UTF_8));
			if (troparion1 != null) {
				System.out.println("The first Troparion is " + troparion1 + " Troparion2 is " + troparion2);
				if (troparion2 != null) {
					// BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
					// FileOutputStream(ServicesFileName+"Var/PTrop61.xml"),"UTF8"));
					data = data + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion1
							+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n";

					// Dim out2 = new BufferedWriter(new OutputStreamWriter(new
					// FileOutputStream(ServicesFileName+"Var/PTrop62.xml"),"UTF8"));
					data2 = data2 + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion2
							+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n";

				} else {
					// BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
					// FileOutputStream(ServicesFileName+"Var/PTrop62.xml"),"UTF8"));
					data2 = data2 + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion1
							+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
					// out.write(Data);
					// out.close();
				}
			}
			data = data + "</SERVICE>\r\n</LANGUAGE>";
			data2 = data2 + "</SERVICE>\r\n</LANGUAGE>";
			out.write(data);
			out.close();
			out2.write(data2);
			out2.close();

		}

		// GET AND CREATE THE APPRORIATE KONTAKION
		// APROPRIATE KONTAKION MUST STILL BE CREATED!
		// System.out.println(Kontakion1);
		if (kontakion1 != null) {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(
					"Ponomar/languages/" + analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKont6.xml")),
					StandardCharsets.UTF_8));
			String data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"SR\" What=\"KONTAKION/" + kontakion1
					+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
			out.write(data);
			out.close();
		}
		// Else we are dealing with a Lenten service that does not have any variable
		// parts.
		// System.out.println("Sixth Hour: "+analyse.dayInfo.get("PFlag3"));

		strOut = readPrime.startService(ServicesFileName + "SixthHour.xml") + "</p>";

		return strOut;
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, HashMap table) {

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
		if (elem.equals("SCRIPTURE") && read) {
			String type = (String) table.get("Type");
			String reading = (String) table.get("Reading");
			// System.out.println("The readings that were found were "+type+" "+reading);
			if (type.equals("6th hour")) {
				reading6th = reading;
			}
		}
		if (elem.equals("SEXTE") && read) {
			// WE ARE DEALING WITH THE INFORMATION FOR PRIMES (THERE COULD BE INFORMATION
			// FOR OTHER SERVICES)
			// THE VARIABLE COMPONETS IN THIS SERVICE ARE GIVEN BELOW
			String value = (String) table.get("Type");
			if (value != null) {
				type = (String) table.get("Type");
			}
			value = (String) table.get("TROPARION1");
			if (value != null) {
				troparion1 = (String) table.get("TROPARION1");
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
				troparion1 = (String) table.get("TROPARION2");
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

	private boolean eval() throws IllegalArgumentException {
		return false;
	}

	public static void main(String[] argz) {

		// new SixthHour(3); //CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
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
			output.setText(createPrimes());
			output.setCaretPosition(0);
		} catch (Exception e1) {
		}

	}
}
