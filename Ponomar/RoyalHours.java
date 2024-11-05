package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class RoyalHours implements DocHandler, ActionListener, ItemListener, PropertyChangeListener {
	// private static final String octoecheosFileName = "xml/Services/Octoecheos/";
	// // THE LOCATION OF THE BASIC SERVICE RULES
	private static final String ServicesFileName = "xml/Services/"; // THE LOCATION FOR ANY EXTRA INFORMATION
	// private static LinkedHashMap<Object, Object> PrimesTK;
	// private static String FileNameIn="xml/Services/PRIMES1/";
	// private static String FileNameOut=FileNameIn+"Primes.html";
	private static String text;
	private static boolean read = false;
	private final LanguagePack Text;// =new LanguagePack();
	private final String[] primesNames;// =Text.obtainValues((String)Text.Phrases.get("RoyalHours"));
	private final String[] languageNames;// =Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	// private String LentenK; //ANY REQUIRED KATHISMA REFERENCED USING "LENTENK =
	// "17"" WOULD BE THE 17th KATHISMA.
	private JFrame frames;
	private final String[] fileNames;// =Text.obtainValues((String)Text.Phrases.get("File"));
	private final String[] helpNames;// =Text.obtainValues((String)Text.Phrases.get("Help"));
	final String newline = "\n";
	private String strOut;
	private final JDate2 today;
	private final Helpers helper;
	// private PrimeSelector SelectorP=new PrimeSelector();
	private PrintableTextPane output;
	private final StringOp analyse = new StringOp();

	public RoyalHours(JDate2 date, Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		primesNames = Text.obtainValues(Text.Phrases.get("RoyalHours"));
		languageNames = Text.obtainValues(Text.Phrases.get("LanguageMenu"));
		fileNames = Text.obtainValues(Text.Phrases.get("File"));
		helpNames = Text.obtainValues(Text.Phrases.get("Help"));
		today = date;
		helper = new Helpers(analyse.dayInfo);
		analyse.dayInfo.put("PS", 1);

		try {
			String strOut = createHours();
			if (strOut.equals("Royal Hours are not served today.")) {
				Object[] options = { languageNames[3] };
				JOptionPane.showOptionDialog(null, primesNames[0],
						Text.Phrases.get("0") + Text.Phrases.get("Colon") + primesNames[1],
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			} else {
				RoyalHoursWindow(strOut);
			}
		} catch (IOException j) {
		}

	}

	private void RoyalHoursWindow(String textOut) {
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
		// PrimeSelector trial=new PrimeSelector();
		menuBar.add(demo.createFileMenu(this));
		// MenuBar.add(trial.createPrimeMenu());
		menuBar.add(demo.createHelpMenu(this));
		frames.setJMenuBar(menuBar);
		// trial.addPropertyChangeListener(this);

		contentPane.add(scrollPane, BorderLayout.CENTER);
		frames.setContentPane(contentPane);
		frames.pack();
		frames.setSize(800, 700);
		frames.setVisible(true);

		Helpers orient = new Helpers(analyse.dayInfo);
		orient.applyOrientation(frames, (ComponentOrientation) analyse.dayInfo.get("Orient"));

		// scrollPane.top();
	}

	private String createHours() throws IOException {
		// OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		// analyse.dayInfo.put("PS",SelectorP.getWhoValue());
		// MUST ADD APPROPRIATE SELECTOR OF TYPE OF SERVICE
		// int TypeP=SelectorP.getTypeValue();

		Service readHours = new Service(analyse.dayInfo);

		int eDay = Integer.parseInt(analyse.dayInfo.get("nday").toString());
		int day = Integer.parseInt(analyse.dayInfo.get("doy").toString());
		int dow = Integer.parseInt(analyse.dayInfo.get("dow").toString());

		if (!((eDay == -2) || (day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5)
				|| (day == 357 && ((dow != 6) && dow != 0)) || (day == 356 && dow == 5) || (day == 355 && dow == 5))) {
			return "Royal Hours are not served today.";
		}
		// BASED ON THE DATE DETERMINE THE CORRECT FLAGS
		analyse.dayInfo.put("PFlag", 0); // FOR EVE OF NATIVITY!
		if ((eDay == -2)) {
			analyse.dayInfo.put("PFlag", 2); // FOR GOOD FRIDAY

		}
		if ((day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5)) {
			analyse.dayInfo.put("PFlag", 1);
		}

		String strOut = "";
		// IT IS TO BE DECIDED WHETHER IT IS DESIRED TO SET THE TROPARIA PROPERLY!

		strOut = readHours.startService(ServicesFileName + "RoyalHours.xml") + "</p>";

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
		if (elem.equals("PRIMES") && read) {

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
			// CLOSE THE HOURS FRAME
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
			output.setText(createHours());
			output.setCaretPosition(0);
		} catch (Exception e1) {

		}

	}

	public static void main(String[] argz) {
		// DEBUG MODE
		System.out.println("RoyalHours.java running in Debug mode");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");

		LinkedHashMap<Object, Object> dayInfo = new LinkedHashMap<>();

		dayInfo.put("dow", 3);
		dayInfo.put("doy", 357);
		dayInfo.put("nday", -256);
		dayInfo.put("LS", 0); // ENGLISH
		dayInfo.put("PS", 1);

		JDate2 todays = new JDate2(12, 24, 2009, 0);

		new RoyalHours(todays, dayInfo);
	}

}
