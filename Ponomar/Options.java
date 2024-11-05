package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/***********************************************************************
 * THIS MODULE ALLOWS THE USER TO CHANGE THE DEFAULTS ASSOCIATED WITH THE JAVA
 * PROGRAMME THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.
 * 
 * (C) 2013 YURI SHARDT. ALL RIGHTS RESERVED.
 * 
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
public class Options extends JFrame implements ActionListener, ItemListener, PropertyChangeListener {
	private static final String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
	private final LanguagePack Text;// =new LanguagePack();
	private final StringOp analyse = new StringOp();
	private final String[] options;
	// private JFrame frames;
	private final String newline = "\n";
	private JTextField latitude;
	private JTextField longitude;
	private JComboBox<String> timeZone2;
	private ButtonGroup calendar;
	private ButtonGroup calendar2;
	private JButton okay;
	private JButton cancel;
	private final String[] zone = { "-12", "-11", "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1", "0", "+1",
			"+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9", "+10", "+11", "+12", };
	private JRadioButton jRadioButton2;
	private JRadioButton jRadioButton1;
	private JRadioButton jRadioButton22;
	private JRadioButton jRadioButton12;
	private final Font currentFont;
	private boolean ignore = true;

	public Options(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		ConfigurationFiles.Defaults = new LinkedHashMap<>();
		ConfigurationFiles.ReadFile();

		options = Text.obtainValues(Text.Phrases.get("Options2"));
		currentFont = new Font((String) analyse.dayInfo.get("FontFaceM"), Font.PLAIN,
				Integer.parseInt((String) analyse.dayInfo.get("FontSizeM")));

		// createDefaultWindow();

	}

	public void createDefaultWindow() {
		// frames=new JFrame(Options[0]);//(String)Text.Phrases.get("0") +
		// (String)Text.Phrases.get("Colon")+ PrimesNames[1]);
		setTitle(options[0]);
		// frames.setSize(200,200);
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(4, 3));
		JLabel blank = new JLabel();
		blank.setFont(currentFont);
		top.add(blank);
		JLabel blank2 = new JLabel(options[1], SwingConstants.CENTER);
		blank2.setFont(currentFont);
		top.add(blank2);
		JLabel blank3 = new JLabel();
		blank3.setFont(currentFont);
		top.add(blank3);

		JLabel latitude2 = new JLabel(options[2], SwingConstants.RIGHT);
		latitude2.setFont(currentFont);
		top.add(latitude2);
		latitude = new JTextField();
		latitude.setEditable(true);
		latitude.setText(ConfigurationFiles.Defaults.get("Latitude").toString());
		latitude.setHorizontalAlignment(SwingConstants.RIGHT);
		latitude.setFont(currentFont);
		top.add(latitude);
		JLabel units = new JLabel(options[3], SwingConstants.LEFT);
		units.setFont(currentFont);
		top.add(units);

		JLabel longitude2 = new JLabel(options[4], SwingConstants.RIGHT);
		longitude2.setFont(currentFont);
		top.add(longitude2);
		longitude = new JTextField();
		longitude.setEditable(true);
		longitude.setText(ConfigurationFiles.Defaults.get("Longitude").toString());
		longitude.setHorizontalAlignment(SwingConstants.RIGHT);
		longitude.setFont(currentFont);
		top.add(longitude);
		JLabel units2 = new JLabel(options[5], SwingConstants.LEFT);
		units2.setFont(currentFont);
		top.add(units2);

		JLabel timezone = new JLabel(options[6], SwingConstants.RIGHT);
		timezone.setFont(currentFont);
		top.add(timezone);

		timeZone2 = new JComboBox<>();
		timeZone2.setFont(currentFont);

		/*
		 * TimeZonesAll timesGet=new TimeZonesAll(); java.util.List<TimeZone> timeZones
		 * = timesGet.getTimeZones(); for (TimeZone timeZone : timeZones) {
		 * System.out.println(timesGet.getName(timeZone));
		 * 
		 * }
		 */
		for (String s : zone) {
			timeZone2.addItem(s);
		}
		timeZone2.setSelectedItem(ConfigurationFiles.Defaults.get("TimeZone").toString());
		timeZone2.setEditable(false);
		top.add(timeZone2, BorderLayout.CENTER);

		JLabel units3 = new JLabel(options[7], SwingConstants.LEFT);
		units3.setFont(currentFont);
		top.add(units3);

		top.setBorder(BorderFactory.createBevelBorder(0));

		JPanel centre = new JPanel();
		centre.setLayout(new GridLayout(2, 3));

		// JLabel blanka=new JLabel();
		// blanka.setFont(CurrentFont);
		// centre.add(blanka);
		// JLabel blank5=new JLabel(Options[8],SwingConstants.CENTER);
		// blank5.setFont(CurrentFont);
		// centre.add(blank5);
		// JLabel blank4=new JLabel();
		// blank4.setFont(CurrentFont);
		// centre.add(blank4);
		// centre.setPreferredSize(new Dimension(100,150));

		JLabel name = new JLabel(options[9], SwingConstants.LEFT);
		name.setFont(currentFont);
		name.setPreferredSize(new Dimension(50, 50));
		centre.add(name);

		String defaultCalendar = ConfigurationFiles.Defaults.get("DisplayCalendar").toString();

		jRadioButton1 = new JRadioButton();
		jRadioButton1.setText(options[10]);
		jRadioButton1.setFont(currentFont);

		if (!(defaultCalendar.equals("1"))) {
			jRadioButton1.setSelected(true);
		}

		jRadioButton2 = new JRadioButton();

		jRadioButton2.setText(options[11]);
		jRadioButton2.setFont(currentFont);
		if ((defaultCalendar.equals("1"))) {
			jRadioButton2.setSelected(true);
		}

		calendar = new ButtonGroup();
		calendar.add(jRadioButton1);
		calendar.add(jRadioButton2);

		centre.add(jRadioButton1);
		centre.add(jRadioButton2);

		centre.setBorder(BorderFactory.createBevelBorder(0));

		// Adding the ability to change the religious calendar

//                JLabel blanka2=new JLabel();
//                blanka2.setFont(CurrentFont);
//                centre.add(blanka2);
//                //JLabel blank52=new JLabel(Options[8],SwingConstants.CENTER);
//               // blank52.setFont(CurrentFont);
//                //centre.add(blank52);
//                JLabel blank42=new JLabel();
//                blank42.setFont(CurrentFont);
//                centre.add(blank42);
//                centre.setPreferredSize(new Dimension(100,150));

		JLabel name2 = new JLabel(options[14], SwingConstants.LEFT);
		name2.setFont(currentFont);
		name2.setPreferredSize(new Dimension(50, 50));
		centre.add(name2);

		String religiousCalendar = ConfigurationFiles.Defaults.get("ReligiousCalendar").toString();

		jRadioButton12 = new JRadioButton();
		jRadioButton12.setText(options[10]);
		jRadioButton12.setFont(currentFont);

		if (!(religiousCalendar.equals("1"))) {
			jRadioButton12.setSelected(true);
		}

		jRadioButton22 = new JRadioButton();

		jRadioButton22.setText(options[11]);
		jRadioButton22.setFont(currentFont);
		if ((religiousCalendar.equals("1"))) {
			jRadioButton22.setSelected(true);
		}

		calendar2 = new ButtonGroup();
		calendar2.add(jRadioButton12);
		calendar2.add(jRadioButton22);

		centre.add(jRadioButton12);
		centre.add(jRadioButton22);

		centre.setBorder(BorderFactory.createBevelBorder(0));

		// End of such changes

		JPanel bottom = new JPanel();
		bottom.setLayout(new GridLayout(2, 2));

		JLabel blank6 = new JLabel("Religious Information", SwingConstants.CENTER);
		bottom.add(blank6);
		JLabel blank7 = new JLabel();
		bottom.add(blank7);

		bottom.setBorder(BorderFactory.createBevelBorder(0));

		JPanel footer = new JPanel();
		footer.setLayout(new GridLayout(1, 4));
		okay = new JButton(options[12]);
		cancel = new JButton(options[13]);
		JLabel blank8 = new JLabel();
		JLabel blank9 = new JLabel();
		okay.addActionListener(this);
		okay.setFont(currentFont);
		cancel.addActionListener(this);
		cancel.setFont(currentFont);

		footer.add(blank8);
		footer.add(blank9);
		footer.add(okay);
		footer.add(cancel);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setOpaque(true);

		contentPane.add(top, BorderLayout.NORTH);
		contentPane.add(centre, BorderLayout.CENTER);

		contentPane.add(footer, BorderLayout.SOUTH);

		setContentPane(contentPane);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Helpers orient = new Helpers(analyse.dayInfo);
		orient.applyOrientation(this, (ComponentOrientation) analyse.dayInfo.get("Orient"));

		pack();
		int width = 550;
		if (Text.Phrases.get("OptionsW") != null) {
			width = Integer.parseInt(Text.Phrases.get("OptionsW"));
		}
		int height = 220;
		if (Text.Phrases.get("OptionsH") != null) {
			height = Integer.parseInt(Text.Phrases.get("OptionsH"));
		}
		setSize(width, height);
		setVisible(true);
		ignore = false;

	}

	public void actionPerformed(ActionEvent e) {
		// JMenuItem source = (JMenuItem)(e.getSource());
		// String name = source.getText();

		// Helpers helper = new Helpers(analyse.dayInfo);
		String name = e.getActionCommand();
		// ALLOWS A MULTILINGUAL PROPER VERSION
		if (name.equals(options[12])) {
			// System.out.println("Pressed Okay");
			String latitude2 = latitude.getText();
			double latitude1 = Double.parseDouble(latitude2);
			String longitude2 = longitude.getText();
			double longitude1 = Double.parseDouble(longitude2);
			if (!(latitude1 >= -90 && latitude1 <= 90)) {
				System.out.println("Error in Entering Latitude; value not used");
			} else {
				ConfigurationFiles.Defaults.put("Latitude", latitude.getText());
			}

			if (!(longitude1 >= -180 && longitude1 <= 180)) {
				System.out.println("Error in Entering Longitude; value not used");
			} else {
				ConfigurationFiles.Defaults.put("Longitude", longitude.getText());
			}
			ConfigurationFiles.Defaults.put("TimeZone", timeZone2.getSelectedItem().toString());

			String previous = ConfigurationFiles.Defaults.get("DisplayCalendar").toString();
			if (jRadioButton1.getSelectedObjects() != null) {
				ConfigurationFiles.Defaults.put("DisplayCalendar", "0");
				if (!(previous.equals("0")) && !ignore) {
					firePropertyChange("DisplayCalendarChange", 1, 0);
				}
			} else {

				ConfigurationFiles.Defaults.put("DisplayCalendar", "1");
				if (!(previous.equals("1")) && !ignore) {
					firePropertyChange("DisplayCalendarChange", 0, 1);
				}
			}

			String previous2 = ConfigurationFiles.Defaults.get("ReligiousCalendar").toString();
			if (jRadioButton12.getSelectedObjects() != null) {
				ConfigurationFiles.Defaults.put("ReligiousCalendar", "0");
				if (!(previous2.equals("0")) && !ignore) {
					firePropertyChange("ReligiousCalendarChange", 1, 0);
				}
			} else {

				ConfigurationFiles.Defaults.put("ReligiousCalendar", "1");
				if (!(previous2.equals("1")) && !ignore) {
					firePropertyChange("ReligiousCalendarChange", 0, 1);
				}
			}

			ConfigurationFiles.WriteFile();
			dispose();

		}
		if (name.equals(options[13])) {
			// System.out.println("Pressed Cancel");
			dispose();
		}

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
	}

	public static void main(String[] argz) {
		// DEBUG MODE
		System.out.println("Options.java running in Debug mode");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");

		LinkedHashMap<Object, Object> dayInfo = new LinkedHashMap<>();

		dayInfo.put("dow", 3);
		dayInfo.put("doy", 357);
		dayInfo.put("nday", -256);
		dayInfo.put("LS", "cu/"); // ENGLISH
		dayInfo.put("PS", 1);
		dayInfo.put("FontFaceM", "Ponomar Unicode TT");
		dayInfo.put("FontSizeM", "18");
		dayInfo.put("Orient", ComponentOrientation.getOrientation(new Locale("ru")));

		new Options(dayInfo);
	}

}
