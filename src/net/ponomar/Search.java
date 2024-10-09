package net.ponomar;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.Commemoration;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
import net.ponomar.utility.SearchTableModel;
import net.ponomar.utility.SearchUtils;
import net.ponomar.utility.StringOp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Vector;

/***********************************************************************
(C) 2013 YURI SHARDT. ALL RIGHTS RESERVED.


 PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
***********************************************************************/

/**
 * 
 * This module creates the text for the searching for commemorations across
 * languages and jurisdictions.
 * 
 * @author Yuri Shardt
 * 
 */
public class Search extends JFrame implements ActionListener {
	private static final String LIVES_PATH = "xml/lives/";

	// SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	// THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	// TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	// DURING THE COURSE OF A SINGLE WEEK.
	public String usualBeginning1;
	private StringOp analyse = new StringOp();
	private JTextField searchTerm = new JTextField("");
	private JButton okay;
	private JTable results;
	DefaultTableModel tableModel;
	String[] availableLanguages;
	private JCheckBox ignoreDiacritics;
	private JCheckBox ignoreCapitalization;
	private JCheckBox searchFullText;

	public Search(LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);

		setTitle("Search Commemorations");

		JPanel top = new JPanel();
		JPanel bottom = new JPanel();
		top.setLayout(new GridLayout(2, 3));
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));

		JLabel text = new JLabel("Search Term: ", SwingConstants.RIGHT);
		top.add(text);
		searchTerm.setEditable(true);
		searchTerm.setText("");
		top.add(searchTerm);

		okay = new JButton("Search");
		okay.addActionListener(this);
		// okay.setFont(CurrentFont); NEEDS TO BE IMPLEMENTED IN THE FINAL VERSION
		top.add(okay);

		ignoreDiacritics = new JCheckBox("Ignore diacritical marks");
		ignoreDiacritics.setSelected(false);
		top.add(ignoreDiacritics);
		ignoreCapitalization = new JCheckBox("Ignore capitalization");
		ignoreCapitalization.setSelected(true);
		top.add(ignoreCapitalization);
		
		searchFullText = new JCheckBox("Search full text");
		searchFullText.setSelected(false);
		top.add(searchFullText);

		JSplitPane truetop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		truetop.setTopComponent(top);
		// JPanel warningTextPanel = new JPanel();
		JTextArea warningText = new JTextArea();
		// warningText.setContentType(Constants.CONTENT_TYPE);
		warningText.setText(
				"This is a trial search of the commemorations in a given language with display across languages. Unfortunately, no stemming or collation is available so that the results are very, very, very dependent on what you enter. The fewer letters or words that are entered here, the more likely you are to find what you are looking for. Entering \"George\" is more likely to give results than \"George the New Martyr.\"");
		warningText.setEditable(false);
		warningText.setLineWrap(true);
		warningText.setWrapStyleWord(true);
		warningText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		truetop.setBottomComponent(warningText);

		tableModel = new SearchTableModel();
		String rough = ConfigurationFiles.getDefaults().get("AvailableLanguages");
		availableLanguages = rough.split(",");
		tableModel.addColumn("ID");
		tableModel.addColumn("Name");
		Helpers getFile = new Helpers(analyse.getDayInfo());

		for (int i = 0; i < availableLanguages.length; i++) {
			LanguagePack lang = new LanguagePack(getFile.langFileFind(availableLanguages[i], Constants.LANGUAGE_PACKS),
					(LinkedHashMap) analyse.getDayInfo().clone());
			tableModel.addColumn(lang.getPhrases().get("NameLocal"));
		}

		results = new JTable(tableModel);
		// Times New Roman doesn't properly fall back on Linux
		results.setFont(new Font(Font.SANS_SERIF, 0, 14));
		results.getTableHeader().setFont(new Font(Font.SANS_SERIF, 0, 14));
		setFont(new Font(Font.SANS_SERIF, 0, 14));
		JScrollPane scrollPane3 = new JScrollPane(results);
		bottom.add(scrollPane3);

		results.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				int index = results.getSelectedRow();
				String id = tableModel.getValueAt(index, 0).toString();
				Commemoration commemoration = new Commemoration(id, id, analyse.getDayInfo());
				new DoSaint(commemoration, analyse.getDayInfo());
			}
		});

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setTopComponent(truetop);
		splitter.setBottomComponent(bottom);

		add(splitter);

		// Adding a Menu Bar
		/*
		 * MenuFiles demo = new MenuFiles(Analyse.getDayInfo().clone()); JMenuBar
		 * MenuBar = new JMenuBar(); MenuBar.add(demo.createFileMenu(this));
		 * MenuBar.add(demo.createHelpMenu(this)); MenuBar.setFont(CurrentFont);
		 * setJMenuBar(MenuBar);
		 */

		pack();
		setSize(700, 600);
		setVisible(true);

	}

	public String getUsualBeginning() {
		return usualBeginning1;
	}

	public void actionPerformed(ActionEvent e) {
		// JMenuItem source = (JMenuItem)(e.getSource());
		// String name = source.getText();

		// Helpers helper = new Helpers(Analyse.getDayInfo());
		String name = e.getActionCommand();
		// ALLOWS A MULTILINGUAL PROPER VERSION
		if (name.equals("Search")) {
			search();
		}

	}

	private void search() {
		tableModel.setRowCount(0);
		String search = searchTerm.getText();
		String localPath = "/" + analyse.getDayInfo().get("LS").toString();
		
		File folder = new File(Constants.LANGUAGES_PATH + localPath + LIVES_PATH);

		File[] listOfFiles = folder.listFiles();

		Commemoration commemoration;
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().endsWith("xml")) {
				commemoration = new Commemoration(file.getName().substring(0, file.getName().length() - 4),
						file.getName().substring(0, file.getName().length() - 4), analyse.getDayInfo());
				String foundCommemoration = extractCommemorationText(commemoration);
				if (SearchUtils.searchName(search, foundCommemoration, localPath, ignoreDiacritics.isSelected(), ignoreCapitalization.isSelected())) {
					tableModel.addRow(prepareRow(commemoration, file));
				}
			}
		}
	}

	private Vector<String> prepareRow(Commemoration commemoration, File file) {
		Vector<String> foundFile = new Vector<>();
		String id = (file.getName().subSequence(0, file.getName().length() - 4)).toString();
		foundFile.add(id);
		foundFile.add(getCleanCommemorationTitle(commemoration));
		for (int i = 0; i < availableLanguages.length; i++) {
			foundFile.add(SearchUtils.checkIfFound(file, availableLanguages[i]));
		}
		return foundFile;
	}

	private String getCleanCommemorationTitle(Commemoration commemoration) {
		return commemoration.getGrammar(Constants.NOMINATIVE).replaceAll("\\<.*?>", "");
	}

	private String extractCommemorationText(Commemoration commemoration) {
		String extractedText = commemoration.getGrammar(Constants.NOMINATIVE);
		if (searchFullText.isSelected() && commemoration.getLife() != null) {
			return extractedText + " " + commemoration.getLife().replaceAll("\\<.*?>", "");
		}
		return extractedText;
	}

	public static void main(String[] argz) {
		LinkedHashMap<String, Object> dayinfo = new LinkedHashMap<>();
		// dayinfo.put("LS", "fr/");
		dayinfo.put("LS", "en/");
		new Search(dayinfo);
	}
}