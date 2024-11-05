package Ponomar;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

class Languagizer extends JFrame implements DocHandler, ListSelectionListener, ActionListener {

	private String bmlfile;
	private final JList<String> russianBox;
	private final JList<String> englishBox;
	private final JList<String> frenchBox;
	private final JLabel russianLabel = new JLabel();
	private final JLabel englishLabel = new JLabel();
	private final JLabel frenchLabel = new JLabel();
	private final JButton assignButton = new JButton("Assign...");
	private final JButton priorButton = new JButton("Previous Day ...");
	private final JButton nextButton = new JButton("Next Day ...");
	private final JButton writeButton = new JButton("Write this ...");
	private final List<String> russianNames = new ArrayList<>();
	private final HashMap<String, String> russianIDs = new HashMap<>();
	private final HashMap<String, String> russianTypes = new HashMap<>();
	private final List<String> englishNames = new ArrayList<>();
	private final HashMap<String, String> englishIDs = new HashMap<>();
	private final HashMap<String, String> englishTypes = new HashMap<>();
	private final List<String> frenchNames = new ArrayList<>();
	private final HashMap<String, String> frenchIDs = new HashMap<>();
	private final HashMap<String, String> frenchTypes = new HashMap<>();
	private boolean readFile = false;
	private int ls = 3;
	private final StringOp analyse = new StringOp();

	private final JDate today = new JDate(1, 1, 2009);

	public Languagizer() {

		super("Languagizer");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// WE NEED THIS HANDY STORER OF VALUES NOW.
		analyse.dayInfo = new LinkedHashMap<>();

		analyse.dayInfo.put("LS", ls);
		analyse.dayInfo.put("nday", 1);
		analyse.dayInfo.put("dow", 0);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout());

		int month = today.getMonth();
		int day = today.getDay();
		bmlfile = "Ponomar/xml/" + (month < 10 ? "0" + month : Integer.toString(month)) + "/"
				+ (day < 10 ? "0" + day : Integer.toString(day)) + ".xml";

		try {
			BufferedReader frf = new BufferedReader(
					new InputStreamReader(Files.newInputStream(Paths.get(bmlfile)), StandardCharsets.UTF_8)); // Unicodised
																												// it.
			QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}

		JPanel russianPanel = new JPanel();
		russianPanel.setLayout(new BorderLayout());

		russianBox = new JList<>(russianNames.toArray(new String[0]));
		russianBox.addListSelectionListener(this);
		JScrollPane russianScrollPane = new JScrollPane(russianBox);
		russianPanel.add(russianScrollPane, BorderLayout.NORTH);
		russianPanel.add(russianLabel, BorderLayout.SOUTH);
		topPanel.add(russianPanel);

		ls = 0;
		analyse.dayInfo.put("LS", ls);
		try {
			BufferedReader frf = new BufferedReader(
					new InputStreamReader(Files.newInputStream(Paths.get(bmlfile)), StandardCharsets.UTF_8)); // Unicodised
																												// it.
			QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}

		JPanel englishPanel = new JPanel();
		englishPanel.setLayout(new BorderLayout());

		englishBox = new JList<>(englishNames.toArray(new String[0]));
		englishBox.addListSelectionListener(this);
		JScrollPane englishScrollPane = new JScrollPane(englishBox);
		englishPanel.add(englishScrollPane, BorderLayout.NORTH);
		englishPanel.add(englishLabel, BorderLayout.SOUTH);
		topPanel.add(englishPanel);

		ls = 1;
		analyse.dayInfo.put("LS", ls);
		try {
			BufferedReader frf = new BufferedReader(
					new InputStreamReader(Files.newInputStream(Paths.get(bmlfile)), StandardCharsets.UTF_8)); // Unicodised
																												// it.
			QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}

		JPanel frenchPanel = new JPanel();
		frenchPanel.setLayout(new BorderLayout());

		frenchBox = new JList<>(frenchNames.toArray(new String[0]));
		frenchBox.addListSelectionListener(this);
		JScrollPane frenchScrollPane = new JScrollPane(frenchBox);
		frenchPanel.add(frenchScrollPane, BorderLayout.NORTH);
		frenchPanel.add(frenchLabel, BorderLayout.SOUTH);
		topPanel.add(frenchPanel);

		add(topPanel, BorderLayout.NORTH);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2, 2));

		assignButton.addActionListener(this);
		writeButton.addActionListener(this);
		priorButton.addActionListener(this);
		nextButton.addActionListener(this);
		bottomPanel.add(assignButton);
		bottomPanel.add(writeButton);
		bottomPanel.add(priorButton);
		bottomPanel.add(nextButton);

		add(bottomPanel, BorderLayout.SOUTH);
		pack();
		setSize(700, 600);
		setVisible(true);

	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, HashMap table) {
		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			if (!analyse.evalbool(table.get("Cmd").toString())) {
				return;
			}
		}
		if (elem.equals("LANGUAGE")) {
			readFile = true;
		}
		if (elem.equals("SAINT") && readFile) {
			String id = (String) table.get("Id");
			String type = (String) table.get("Type");
			String mName = (String) table.get("Name");
			if (ls == 3) {
				try {
					russianNames.add(mName);
					russianIDs.put(mName, id);
					russianTypes.put(mName, type);
				} catch (Exception e) {
				}
			}
			if (ls == 0) {
				try {
					englishNames.add(mName);
					englishIDs.put(mName, id);
					englishTypes.put(mName, type);
				} catch (Exception e) {
				}
			}
			if (ls == 1) {
				try {
					frenchNames.add(mName);
					frenchIDs.put(mName, id);
					frenchTypes.put(mName, type);
				} catch (Exception e) {
				}
			}
		}
	}

	public void endElement(String elem) {
		if (elem.equals("LANGUAGE")) {
			readFile = false;
		}
	}

	public void text(String text) {
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(russianBox)) {
			// update russian label
			int n = russianBox.getSelectedIndex();
			String name = russianNames.get(n);
			String id = "null";
			String type = "null";
			if (russianIDs.containsKey(name)) {
				id = russianIDs.get(name);
			}
			if (russianTypes.containsKey(name)) {
				type = russianTypes.get(name);
			}
			russianLabel.setText("ID: " + id + "; Type: " + type);
		} else if (e.getSource().equals(englishBox)) {
			// update russian label
			int n = englishBox.getSelectedIndex();
			String name = englishNames.get(n);
			String id = "null";
			String type = "null";
			if (englishIDs.containsKey(name)) {
				id = englishIDs.get(name);
			}
			if (englishTypes.containsKey(name)) {
				type = englishTypes.get(name);
			}
			englishLabel.setText("ID: " + id + "; Type: " + type);
		} else if (e.getSource().equals(frenchBox)) {
			// update russian label
			int n = frenchBox.getSelectedIndex();
			String name = frenchNames.get(n);
			String id = "null";
			String type = "null";
			if (frenchIDs.containsKey(name)) {
				id = frenchIDs.get(name);
			}
			if (frenchTypes.containsKey(name)) {
				type = frenchTypes.get(name);
			}
			frenchLabel.setText("ID: " + id + "; Type: " + type);
		}

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(assignButton)) {
			int nRu = russianBox.getSelectedIndex();
			String nameRu = russianNames.get(nRu);
			String idRu = russianIDs.get(nameRu);
			String typeRu = russianTypes.get(nameRu);

			int nEn = englishBox.getSelectedIndex();
			String nameEn = englishNames.get(nEn);

			int nFr = frenchBox.getSelectedIndex();
			String nameFr = frenchNames.get(nFr);

			// set
			englishIDs.put(nameEn, idRu);
			englishTypes.put(nameEn, typeRu);

			frenchIDs.put(nameFr, idRu);
			frenchTypes.put(nameFr, typeRu);
		} else if (e.getSource().equals(priorButton)) {
			today.subtractDays(1);
			loadDay();
		} else if (e.getSource().equals(nextButton)) {
			today.addDays(1);
			loadDay();
		} else if (e.getSource().equals(writeButton)) {

		}
	}

	private void loadDay() {
		System.out.println(today.toString());
		russianNames.clear();
		englishNames.clear();
		frenchNames.clear();
		russianIDs.clear();
		englishIDs.clear();
		frenchIDs.clear();
		russianTypes.clear();
		englishTypes.clear();
		frenchTypes.clear();

		ls = 3;
		analyse.dayInfo.put("LS", ls);
		int month = today.getMonth();
		int day = today.getDay();
		bmlfile = "Ponomar/xml/" + (month < 10 ? "0" + month : Integer.toString(month)) + "/"
				+ (day < 10 ? "0" + day : Integer.toString(day)) + ".xml";

		try {
			BufferedReader frf = new BufferedReader(
					new InputStreamReader(Files.newInputStream(Paths.get(bmlfile)), StandardCharsets.UTF_8)); // Unicodised
																												// it.
			QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}
		russianBox.setListData(russianNames.toArray(new String[0]));

		ls = 0;
		analyse.dayInfo.put("LS", ls);
		try {
			BufferedReader frf = new BufferedReader(
					new InputStreamReader(Files.newInputStream(Paths.get(bmlfile)), StandardCharsets.UTF_8)); // Unicodised
																												// it.
			QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}
		englishBox.setListData(englishNames.toArray(new String[0]));

		ls = 1;
		analyse.dayInfo.put("LS", ls);
		try {
			BufferedReader frf = new BufferedReader(
					new InputStreamReader(Files.newInputStream(Paths.get(bmlfile)), StandardCharsets.UTF_8)); // Unicodised
																												// it.
			QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}
		frenchBox.setListData(frenchNames.toArray(new String[0]));

	}

	public static void main(String[] argz) {
		new Languagizer();
	}
}
