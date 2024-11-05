package Ponomar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

/***************************************************************
 * GospelSelector.java :: MODULE THAT ALLOWS THE USER TO SELECT, USING RADIO
 * BUTTONS, WHICH TYPE OF REPEATS ARE BEING USED FOR THE GOSPEL READINGS
 * CURRENTLY BOTH THE LUCAN JUMP AND JORDANVILLE IMPLEMENTATIONS ARE AVAILABLE.
 * 
 * GospelSelector.java is part of the Ponomar project. Copyright 2008 Yuri
 * Shardt
 * 
 * version 1.0: August 2008 yuri.shardt (at) gmail.com
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

class PrimeSelector extends JPanel implements ActionListener, PropertyChangeListener {
	private static String a = (String) ConfigurationFiles.Defaults.get("Primes");
	private static final String[] Default = a.split(",");
	private static String readingLocation = Default[0]; // DETERMINES THE LOCATION OF THE READINGS
	private static String lastLocation = Default[0]; // AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	private JRadioButtonMenuItem rbMenu1Item, rbMenu2Item, rbMenu4Item, rbMenu5Item, rbMenu6Item;
	private final LanguagePack Text;// =new LanguagePack();
	private final String[] selectorNames;// =Text.obtainValues((String)Text.Phrases.get("PrimeSelection"));
	private static String lastLocation2 = Default[1];
	private final StringOp analyse = new StringOp();

	public PrimeSelector(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		selectorNames = Text.obtainValues(Text.Phrases.get("PrimeSelection"));
	}

	public JMenu createPrimeMenu() {

		// GospelSelector sample=new GospelSelector();
		// CREATE THE DEFAULT MENU
		a = (String) ConfigurationFiles.Defaults.get("Primes");
		String[] Default = a.split(",");

		JMenu menu = new JMenu(selectorNames[7]);
		menu.setMnemonic(KeyEvent.VK_T);
		menu.getAccessibleContext().setAccessibleDescription(selectorNames[7]);

		// DETERMINE THE DEFAULTS

		ButtonGroup group = new ButtonGroup();
		rbMenu1Item = new JRadioButtonMenuItem(selectorNames[0]);
		rbMenu1Item.addActionListener(this);
		rbMenu1Item.setMnemonic(KeyEvent.VK_R);
		rbMenu1Item.setActionCommand("Reader");
		rbMenu1Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		group.add(rbMenu1Item);

		rbMenu2Item = new JRadioButtonMenuItem(selectorNames[1]);
		rbMenu2Item.setMnemonic(KeyEvent.VK_T);
		rbMenu2Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		rbMenu2Item.addActionListener(this);
		rbMenu2Item.setActionCommand("Priest");
		group.add(rbMenu2Item);

		if (Default[0].equals("Reader")) {
			rbMenu1Item.setSelected(true);
			readingLocation = "Reader";
			lastLocation = "Reader";
		} else {
			rbMenu2Item.setSelected(true);
			readingLocation = "Priest";
			lastLocation = "Priest";
		}

		menu.add(rbMenu1Item);
		menu.add(rbMenu2Item);
		menu.addSeparator();

		ButtonGroup group1 = new ButtonGroup();
		JRadioButtonMenuItem rbMenu3Item = new JRadioButtonMenuItem(selectorNames[3]);
		rbMenu3Item.addActionListener(this);
		rbMenu3Item.setMnemonic(KeyEvent.VK_I);
		rbMenu3Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
		rbMenu3Item.setActionCommand("Independent");
		group1.add(rbMenu3Item);

		rbMenu4Item = new JRadioButtonMenuItem(selectorNames[4]);
		rbMenu4Item.setMnemonic(KeyEvent.VK_B);
		rbMenu4Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
		rbMenu4Item.addActionListener(this);
		rbMenu4Item.setActionCommand("W.Beginning");
		group1.add(rbMenu4Item);

		rbMenu5Item = new JRadioButtonMenuItem(selectorNames[5]);
		rbMenu5Item.setMnemonic(KeyEvent.VK_E);
		rbMenu5Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		rbMenu5Item.addActionListener(this);
		rbMenu5Item.setActionCommand("W.Ending");
		group1.add(rbMenu5Item);

		rbMenu6Item = new JRadioButtonMenuItem(selectorNames[6]);
		rbMenu6Item.setMnemonic(KeyEvent.VK_W);
		rbMenu6Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		rbMenu6Item.addActionListener(this);
		rbMenu6Item.setActionCommand("W.BeginningEnding");
		group1.add(rbMenu6Item);

		switch (Default[1]) {
		case "Independent":
			rbMenu3Item.setSelected(true);
			lastLocation2 = "Independent";
			break;
		case "W.Beginning":
			rbMenu4Item.setSelected(true);
			lastLocation2 = "W.Beginning";
			break;
		case "W.Ending":
			rbMenu5Item.setSelected(true);
			lastLocation2 = "W.Ending";
			break;
		default:
			rbMenu6Item.setSelected(true);
			lastLocation2 = "W.BeginningEnding";
			break;
		}

		menu.add(rbMenu3Item);
		menu.add(rbMenu4Item);
		menu.add(rbMenu5Item);
		menu.add(rbMenu6Item);

		return menu;
	}

	public void actionPerformed(ActionEvent e) {
		// THIS WILL DETERMINE THE PATH TO THE APPROPRIATE READING LOCATION
		readingLocation = e.getActionCommand();
		String last1 = lastLocation;
		String last2 = lastLocation2;
		if (!readingLocation.equals(lastLocation)
				&& (readingLocation.equals("Priest") || readingLocation.equals("Reader"))) {

			lastLocation = readingLocation;
			ConfigurationFiles.Defaults.put("Primes", lastLocation + "," + lastLocation2);
			ConfigurationFiles.WriteFile();
			firePropertyChange("Who Change", readingLocation, last1);
		} else if (!readingLocation.equals(lastLocation2)) {

			lastLocation2 = readingLocation;
			ConfigurationFiles.Defaults.put("Primes", lastLocation + "," + lastLocation2);
			ConfigurationFiles.WriteFile();
			firePropertyChange("Type Change", readingLocation, last2);

		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??

	}

	protected static int getWhoValue() {
		if (lastLocation.equals("Reader")) {
			return 0;
		}
		return 1;

	}

	protected static int getTypeValue() {
		switch (lastLocation2) {
		case "Independent":
			return 0;
		case "W.Beginning":
			return 1;
		case "W.Ending":
			return 2;
		}

		return 3;

	}

}