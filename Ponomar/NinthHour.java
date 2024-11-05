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
public class NinthHour implements DocHandler, ActionListener, ItemListener, PropertyChangeListener {
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
	private static final String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	private String filename;
	private int lineNumber;
	private final LanguagePack Text;// =new LanguagePack();
	private final String[] primesNames;// =Text.obtainValues((String)Text.Phrases.get("None"));
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
	// private Helpers findLanguage;

	public NinthHour(JDate2 date, Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		primesNames = Text.obtainValues((String) Text.Phrases.get("None"));
		languageNames = Text.obtainValues((String) Text.Phrases.get("LanguageMenu"));
		fileNames = Text.obtainValues((String) Text.Phrases.get("File"));
		helpNames = Text.obtainValues((String) Text.Phrases.get("Help"));
		new PrimeSelector(dayInfo);

		// StringOp.dayInfo = new Hashtable();
		// StringOp.dayInfo.put("dow", Weekday); //DETERMINE THE DAY OF THE WEEK.

		// CREATING THE SERVICE
		today = date;
		helper = new Helpers(analyse.dayInfo);
		try {
			String strOut = createPrimes();
			if (strOut.equals("No Service Today")) {
				Object[] options = { languageNames[3] };
				JOptionPane.showOptionDialog(null, primesNames[0],
						Text.Phrases.get("0") + (String) Text.Phrases.get("Colon") + primesNames[1],
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			} else {
				// strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt
				// at creating the Primes service.</Font></p>";
				// int LangCode=Integer.parseInt(StringOp.dayInfo.get("LS").toString());
				// if (LangCode==2 || LangCode==3 ){
				// strOut="<meta http-equiv=\"Content-Type\"
				// content=\"text/html;charset=UTF-8\"><p><font face=\"Ponomar Unicode TT\"
				// size=\"5\">"+strOut+"</font></p>";
				// System.out.println("Added Font");
				// }

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

		// CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
		// POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED
		// EXCEPT FOR ANY OVERRIDES
		ServiceInfo servicePrimes = new ServiceInfo("NONE", analyse.dayInfo);
		Map<Object, Object> primesTrial = servicePrimes.ServiceRules();

		type = primesTrial.get("Type").toString();
		lentenK = (String) primesTrial.get("LENTENK");

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
		// NOTE PFlag2 == 3 for Holy Week Services!
		if (type.equals("Lenten")) {
			analyse.dayInfo.put("PFlag2", 1);

			if (lentenK != null) {
				analyse.dayInfo.put("PFlag2", 2);
				// CREATE THE KATHISMA PART
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKath9.xml")),
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
			if (troparion1 != null) {
				if (troparion2 != null) {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
							Files.newOutputStream(Paths.get("Ponomar/languages/" + analyse.dayInfo.get("LS").toString()
									+ ServicesFileName + "Var/PTrop91.xml")),
							StandardCharsets.UTF_8));
					String data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion1
							+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
					out.write(data);
					out.close();

					out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get("Ponomar/languages/"
							+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop92.xml")),
							StandardCharsets.UTF_8));
					data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion2
							+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
					out.write(data);
					out.close();

				}
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(
								Files.newOutputStream(Paths.get("Ponomar/languages/"
										+ analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop92.xml")),
								StandardCharsets.UTF_8));
				String data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion1
						+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
				out.write(data);
				out.close();
			}

		}

		// GET AND CREATE THE APPRORIATE KONTAKION
		// APROPRIATE KONTAKION MUST STILL BE CREATED!
		if (kontakion1 != null) {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(
					"Ponomar/languages/" + analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKont9.xml")),
					StandardCharsets.UTF_8));
			String data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/" + kontakion1
					+ "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
			out.write(data);
			out.close();
		}
		// Else we are dealing with a Lenten service that does not have any variable
		// parts.

		strOut = readPrime.startService(ServicesFileName + "NinthHour.xml") + "</p>";

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
		if (elem.equals("NONE") && read) {
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
			output.setText(createPrimes());
			output.setCaretPosition(0);
		} catch (Exception e1) {

		}

	}

}
