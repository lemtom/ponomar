package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

/***************************************************************
 * LanguageSelector.java :: MODULE THAT ALLOWS THE USER TO SELECT, USING A
 * COMBOBOX, WHICH LANGUAGE THE PROGRAMME WILL RUN IN.
 * 
 * LanguageSelector.java is part of the Ponomar project. Copyright 2008 Yuri
 * Shardt version 1.0: August 2008 yuri (dot) shardt (at) gmail.com
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

class LanguageSelector extends JMenu implements ActionListener, PropertyChangeListener {
	private String languageLocation; // DETERMINES THE LOCATION OF THE READINGS
	private JMenu menuPanel;
	private JRadioButtonMenuItem languageBox;
	private String lastLanguage; // AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	private String[] availableLanguages;
	private int defaultLocation;
	private final String Default;
	private final String[] nameLanguages;
	private final StringOp analyse = new StringOp();

	public LanguageSelector(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Default = (String) ConfigurationFiles.Defaults.get("Language");
		String rough = (String) ConfigurationFiles.Defaults.get("AvailableLanguages");
		availableLanguages = rough.split(",");
		languageLocation = Default;
		lastLanguage = Default;
		nameLanguages = availableLanguages;

		defaultLocation = 0;

		for (int i = 0; i < availableLanguages.length; i++) {
			if (availableLanguages[i].equals(Default)) {
				defaultLocation = i;
				break;
			}
		}

	}

	public JMenu createLanguageMenu(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Font currentFont = new Font((String) analyse.dayInfo.get("FontFaceM"), Font.PLAIN,
				Integer.parseInt((String) analyse.dayInfo.get("FontSizeM")));
		LanguagePack Text = new LanguagePack(dayInfo);
		String[] languageNames = Text.obtainValues(Text.Phrases.get("LanguageMenu"));
		// DETERMINE THE DEFAULTS
		String Default = (String) ConfigurationFiles.Defaults.get("Language");
		String rough = (String) ConfigurationFiles.Defaults.get("AvailableLanguages");
		availableLanguages = rough.split(",");
		languageLocation = Default;
		lastLanguage = Default;

		// CREATE THE COMBOBOX
		menuPanel = new JMenu(languageNames[0]);
		menuPanel.setToolTipText(languageNames[1]);
		menuPanel.setMnemonic(KeyEvent.VK_L);
		menuPanel.getAccessibleContext().setAccessibleDescription(languageNames[2]);
		// menuPanel.setFont(CurrentFont);
		ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < availableLanguages.length; i++) {
			// System.out.println(AvailableLanguages[i]);
			Helpers getFile = new Helpers(analyse.dayInfo);
			LanguagePack lang = new LanguagePack(
					getFile.langFileFind(availableLanguages[i], "xml/Commands/LanguagePacks.xml"),
					new LinkedHashMap<>(analyse.dayInfo));
			nameLanguages[i] = lang.Phrases.get("NameLocal");
			languageBox = new JRadioButtonMenuItem(lang.Phrases.get("NameLocal"));
			languageBox.addActionListener(this);
			if (i == defaultLocation) {
				languageBox.setSelected(true);
				lastLanguage = nameLanguages[i];
			}

			if (availableLanguages[i].startsWith("zh")) {
				// We are going to treat Chinese specially
				String fontName = "SimSun";

				if (availableLanguages[i].startsWith("Hant", 3)) {
					fontName = "MingLiU";
				}
				Font chineseFont = new Font(fontName, Font.PLAIN, 20);
				languageBox.setFont(chineseFont);

			} else if (availableLanguages[i].equals("cu/")) {

				// We are going to treat Church Slavonic specially
				Font chineseFont = new Font("Ponomar Unicode TT", Font.PLAIN, 18);
				languageBox.setFont(chineseFont);
				// LanguageBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				// LanguageBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

			} else {
				// We do not want the default font necessarily being used here.
				// if (CurrentFont.getFontName().toString().equals("Ponomar Unicode TT"))
				// This is only an issue for fonts that lack a complete character set.
				{
					Font defaultFont = new Font("Times New Roman", Font.BOLD, 14);
					languageBox.setFont(defaultFont);
				}
			}
			group.add(languageBox);
			menuPanel.add(languageBox);
		}

		return menuPanel;

	}

	public void actionPerformed(ActionEvent e) {
		LanguagePack Text = new LanguagePack(new LinkedHashMap<>(analyse.dayInfo));
		String[] languageNames = Text.obtainValues(Text.Phrases.get("LanguageMenu"));
		// THIS WILL DETERMINE THE APPROPRIATE LANGUAGE LOCATION
		languageLocation = e.getActionCommand();
		// System.out.println(LanguageLocation);
		int newLocation = -1;
		for (int i = 0; i < nameLanguages.length; i++) {
			if (nameLanguages[i].equals(languageLocation)) {
				newLocation = i;
				break;
			}
		}
		// System.out.println("LL: "+LanguageLocation);
		// System.out.println("Location: "+DefaultLocation);
		// String rough = (String)
		// ConfigurationFiles.Defaults.get("AvailableLanguages");
		// AvailableLanguages=rough.split(",");

		if (!languageLocation.equals(lastLanguage)) // I might need to do something here!!!
		{

			defaultLocation = findString(availableLanguages, languageLocation);
			firePropertyChange("Language", languageLocation, lastLanguage); // THIS WILL ONLY CAUSE A
																			// CHANGE IN THE DISPLAY
																			// OF DATA LANGUAGE, BUT
																			// NOT THE INTERFACE
																			// LANGUAGE. THE
																			// PROGRAMME NEEDS TO BE
																			// RESTARTED FOR THIS TO
																			// OCCUR.
			// A MESSAGE BOX SHOULD ALSO BE DISPLAYED!
			Object[] options = { languageNames[3] };
			JOptionPane.showOptionDialog(null, languageNames[4], Text.Phrases.get("0"),
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			// JOptionPane.showMessageDialog(null, "In order for the interface language to
			// change, please restart the programme.","Ponomar");
			lastLanguage = nameLanguages[newLocation];
			ConfigurationFiles.Defaults.put("Language", availableLanguages[newLocation]);
			ConfigurationFiles.WriteFile();
		}

	}

	public static int findString(String[] w, String compare) {
		// FINDS compare IN THE STRING ARRAY w.
		int location = -1;
		for (int i = 0; i < w.length; i++) {
			if (w[i].equals(compare)) {
				location = i;
				break;
			}
		}
		return location;

	}

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??
	}

	protected String getLValue() {
		return Default; // DefaultLocation;
	}

}
