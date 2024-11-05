package Ponomar;

import javax.swing.*;
import java.awt.*;
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

class GospelSelector extends JPanel implements ActionListener, PropertyChangeListener {
	private static String readingLocation; // DETERMINES THE LOCATION OF THE READINGS
	private JPanel radioPanel;
	private JRadioButton lucanButton;
	private JRadioButton jordanvilleButton;
	private static String lastLocation; // AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	private JMenu submenu;
	private JRadioButtonMenuItem rbMenu1Item, rbMenu2Item;
	private final LanguagePack Text;// =new LanguagePack();
	private String[] selectorNames;// =Text.obtainValues((String)Text.Phrases.get("GospelSelection"));
	private final StringOp analyse = new StringOp();

	public GospelSelector(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		selectorNames = Text.obtainValues(Text.Phrases.get("GospelSelection"));
		Font currentFont = new Font((String) analyse.dayInfo.get("FontFaceM"), Font.PLAIN,
				Integer.parseInt((String) analyse.dayInfo.get("FontSizeM")));

	}

	public JPanel createGospelSelector() {

		selectorNames = Text.obtainValues(Text.Phrases.get("GospelSelection"));
		Font currentFont = new Font((String) analyse.dayInfo.get("FontFaceM"), Font.PLAIN,
				Integer.parseInt((String) analyse.dayInfo.get("FontSizeM")));
		// DETERMINE THE DEFAULTS
		String Default = (String) ConfigurationFiles.Defaults.get("GospelSelector");
		// Create the radio buttons.
		jordanvilleButton = new JRadioButton(selectorNames[0]);
		jordanvilleButton.setMnemonic(KeyEvent.VK_T);
		jordanvilleButton.setActionCommand("TheophanyJump");
		jordanvilleButton.setFont(currentFont);
		lucanButton = new JRadioButton(selectorNames[1]);
		lucanButton.setMnemonic(KeyEvent.VK_L);
		lucanButton.setActionCommand("LucanJump");
		lucanButton.setFont(currentFont);

		if (Default.equals("TheophanyJump")) {
			jordanvilleButton.setSelected(true);
			readingLocation = "TheophanyJump";
			lastLocation = "TheophanyJump";
		} else {
			lucanButton.setSelected(true);
			readingLocation = "LucanJump";
			lastLocation = "LucanJump";
		}

		// GROUP THE RADIO BUTTONS
		ButtonGroup group = new ButtonGroup();
		group.add(jordanvilleButton);
		group.add(lucanButton);

		// Register a listener for the radio buttons.
		jordanvilleButton.addActionListener(this);
		lucanButton.addActionListener(this);

		// CREATE THE RADIO BUTTONS AND ADD THEM
		radioPanel = new JPanel(new GridLayout(0, 2));
		radioPanel.add(jordanvilleButton);
		radioPanel.add(lucanButton);
		JTextPane text = new JTextPane();
		text.setEditable(false);
		text.setContentType("text/html");
		text.setText(selectorNames[2]);
		text.setOpaque(false);
		add(text);
		add(radioPanel, BorderLayout.LINE_START);
		// radioPanel.addPropertyChangeListener(this);

		return radioPanel;

	}

	public JMenu createGospelMenu() {

		// GospelSelector sample=new GospelSelector();
		// CREATE THE DEFAULT MENU
		// Font CurrentFont=new
		// Font((String)StringOp.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
		submenu = new JMenu(selectorNames[2]);
		submenu.setToolTipText(selectorNames[3]);
		submenu.setMnemonic(KeyEvent.VK_G);
		submenu.getAccessibleContext().setAccessibleDescription(selectorNames[3]);
		// submenu.setFont(CurrentFont);
		// DETERMINE THE DEFAULTS
		String Default = (String) ConfigurationFiles.Defaults.get("GospelSelector");

		ButtonGroup group = new ButtonGroup();
		rbMenu1Item = new JRadioButtonMenuItem(selectorNames[0]);
		rbMenu1Item.addActionListener(this);
		rbMenu1Item.setActionCommand("TheophanyJump");
		rbMenu1Item.setMnemonic(KeyEvent.VK_T);
		rbMenu1Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
		// rbMenu1Item.setFont(CurrentFont);

		rbMenu2Item = new JRadioButtonMenuItem(selectorNames[1]);
		rbMenu2Item.setMnemonic(KeyEvent.VK_L);
		rbMenu2Item.setActionCommand("LucanJump");
		rbMenu2Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		// rbMenu2Item.setFont(CurrentFont);

		if (Default.equals("TheophanyJump")) {
			rbMenu1Item.setSelected(true);
			readingLocation = "TheophanyJump";
			lastLocation = "TheophanyJump";
		} else {
			rbMenu2Item.setSelected(true);
			readingLocation = "LucanJump";
			lastLocation = "LucanJump";
		}

		group.add(rbMenu1Item);
		submenu.add(rbMenu1Item);

		group.add(rbMenu2Item);
		rbMenu2Item.addActionListener(this);
		submenu.add(rbMenu2Item);

		return submenu;
	}

	public void actionPerformed(ActionEvent e) {
		// THIS WILL DETERMINE THE PATH TO THE APPROPRIATE READING LOCATION
		readingLocation = e.getActionCommand();

		if (!readingLocation.equals(lastLocation)) {
			firePropertyChange("Gospel Lectionary", readingLocation, lastLocation);
			lastLocation = readingLocation;
			ConfigurationFiles.Defaults.put("GospelSelector", lastLocation);
			ConfigurationFiles.WriteFile();

		}

	}

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??

	}

	protected static int getGValue() {

		if (readingLocation.equals("TheophanyJump")) {
			return 0;
		}
		return 1;

	}

}