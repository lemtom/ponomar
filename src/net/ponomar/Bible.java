package net.ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.panels.PrintableTextPane;
import net.ponomar.parsing.DocHandler;
import net.ponomar.parsing.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
import net.ponomar.utility.OrderedHashtable;
import net.ponomar.utility.StringOp;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.swing.BoxLayout;

/*
 * Copyright 2007, 2008 Aleksandr Andreev.
 * Copyright 2012 Yuri Shardt and Aleksandr Andreev.
 * 
 * Ponomar is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * While Ponomar is distributed in the hope that it will be useful, it comes
 * with ABSOLUTELY NO WARRANTY, without even the implied warranties of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for details.
 */

/**
 * 
 * A scripture reader interface for the Ponomar project. This module outputs,
 * browses, and searches the scriptural texts. It also provides tools for
 * copying, saving, and printing scripture texts and for Cross-referencing
 * translations, versions, and patristic commentary.
 * @author Yuri Shardt and Aleksandr Andreev (aleksandr.andreev@gmail.com)
 * @version 3.0: Changes in layout and capabilities.
 * 
 */
public class Bible extends JFrame implements DocHandler, ListSelectionListener, ActionListener {

	private static final String BIBLE_V = "BibleV";
	private static final String FONT_FACE = "FontFace";
	private static final String FONT_SIZE = "FontSize";
	private static final String ORIENT = Constants.ORIENT;
	private static final String NAME = "^NAME";
	// Parsing and navigation information
    private String curversion = "kjv"; // DEFAULT VERSION
    private String curbook;
    private String curpassage;
    private String lastversion = "";
    private String instructions = "";
    private OrderedHashtable versions = new OrderedHashtable();
    private OrderedHashtable versions2 = new OrderedHashtable();
    private OrderedHashtable books = new OrderedHashtable();
    private OrderedHashtable chapters = new OrderedHashtable();
    private OrderedHashtable abbrev = new OrderedHashtable();
    private boolean readFile = false;
    private OrderedHashtable findId = new OrderedHashtable();	//ADDED Y.S.
    private OrderedHashtable intro = new OrderedHashtable(); //ADDED Y.S.
    private String displayFont = ""; //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
    private String displaySize = "12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
    private Font defaultFont = new Font("", Font.BOLD, 12);		//CREATE THE DEFAULT FONT
    private Font currentFont = defaultFont;
    // components
    private PrintableTextPane textOutput;		// CONTAINS THE TEXT OUTPUT
    private JTextPane instructionsText;
    private JComboBox<Object> versionsBox;	// CONTAINS THE VERSIONS OF SCRIPTURE AVAILABLE
    private JList booksBox;	// CONTAINS THE BOOKS
    private JList<String> chaptersBox;// CONTAINS THE CHAPTERS
    private boolean changeIt = false;
    private boolean changeItBooks = true;	//ADDED Y.S.
    //ADDED Y.S. 20081211 n.s. DEFAULT DISPLAY PARAMETERS
    private String chapterName = "<p style=\"font-style:bold;color:red\">^NAME ^NN</p>";
    private String[] verseNumber = {"<p style=\"font-style:italic;font-size:10pt;\">^TT</p><BR>", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "152", "153", "154", "155", "156", "157", "158", "159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170", "171", "172", "173", "174", "175", "176", "177", "178", "179", "180"};
    private String[] chapterNumber = {"<p style=\"font-style:italic;font-size:10pt;\">^TT</p><BR>", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "152", "153", "154", "155", "156", "157", "158", "159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170", "171", "172", "173", "174", "175", "176", "177", "178", "179", "180"};
    private String cVSep = ":";
    private String duration = "-";
    private String selectionSeparator = ", ";
    private String instructFirst;
    private static final int NUM_BUTTONS = 8;
    private LanguagePack text;//= new LanguagePack();
    private final String[] captions;// = Text.obtainValues((String) Text.Phrases.get("BibleW"));
    private String printText;
    private String currentBible = "";
    private ComponentOrientation orientText = ComponentOrientation.LEFT_TO_RIGHT;
    private String chapterNameI = "^NN";
    private String header = "<h1 style=\"font-style:bold;color:red;\">^NAME ^CNN</h1>";
    private StringOp analyse = new StringOp();
    private String abbrevFormat="^NAME ^CNN";
    private String verseNumbered="";
    private String verseNoNumbered="";
    private String verseLink=" ";
    private boolean versed=true;
    private String[] halfVerse={"a","b","c"};
    //private String prologue="<p style=\"font-style:italic;font-size:10px;\">^TT</p><BR>";
    

    // CONSTRUCTOR
    protected Bible(String curbook, String curpassage, OrderedHashtable dayInfo) {
        
        analyse.setDayInfo(dayInfo);
        text = new LanguagePack(dayInfo);
        captions = text.obtainValues((String) text.getPhrases().get("BibleW"));
        setTitle(captions[7]);
        
        LanguagePack getLang = new LanguagePack(analyse.getDayInfo());
        curversion = getLang.getPhrases().get(BIBLE_V).toString();

        parseBibleXML();
        putXMLinUserInterface();
        
        update(curbook, curpassage);

    }

	protected void putXMLinUserInterface() {
        // NOW PUT THE XML INFORMATION INTO THE USER INTERFACE
		JPanel left = new JPanel();
        JPanel right = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
        //CHanging the layout manager;
        //setLayout(new GridBagLayout());
        //GridBagConstraints c = new GridBagConstraints();

        versionsBox = new JComboBox<Object>(new Vector<Object>(versions.values()));
        //versionsBox = new JComboBox(new Vector(comboBoxList.values()));
        //versionsBox.setComponentOrientation(new ComponentOrientation);
        /*c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;*/
        left.add(versionsBox);
        //GET THE DEFAULT LANGUAGE BIBLE INDEX LOCATION IN THE GIVEN LIST
        int indexV = 0;
        Vector vers1 = new Vector(versions2.values());
        for (indexV = 0; indexV < vers1.size(); indexV++) {

            if (findId.get(vers1.get(indexV)).toString().equals(curversion)) {
                break;
            }
        }
        versionsBox.setSelectedIndex(indexV);
        versionsBox.addActionListener(this);

        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.LINE_AXIS));
        booksBox = new JList(new Vector(books.values()));
        /*c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.ipady = 250;
        c.ipadx = 100;
        c.weightx = 0.5;*/
        booksBox.addListSelectionListener(this);
        JScrollPane scrollPane1 = new JScrollPane(booksBox);
        middle.add(scrollPane1);
        //add(scrollPane1, c);

        chaptersBox = new JList<>(new String[]{"size dummy"});
        /*c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.ipadx = 0;
        c.weightx = 0.5;*/
        chaptersBox.addListSelectionListener(this);
        JScrollPane scrollPane2 = new JScrollPane(chaptersBox);
        middle.add(scrollPane2);
        left.add(middle);

        JToolBar toolbar = new JToolBar();
        toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolbar.setFloatable(false);
        for (int bnum = 0; bnum < NUM_BUTTONS; bnum++) {
            String imgLocation = Constants.IMAGES_RESOURCE_PATH + bnum + ".gif";
            URL imgURL = Bible.class.getResource(imgLocation);

            JButton button = new JButton();
            button.setActionCommand(Integer.toString(bnum));

            
            button.addActionListener(this);

            if (imgURL != null) {
                if (bnum<7){
                button.setIcon(new ImageIcon(imgURL, captions[bnum]));
                button.setToolTipText(captions[bnum]);
                }else{
                    String part2=(String)text.getPhrases().get("BibleW2");
                    button.setIcon(new ImageIcon(imgURL,part2));
                    button.setToolTipText(part2);
                }
            } else {
                button.setText(Integer.toString(bnum));
                System.err.println(captions[9] + imgLocation);
            }
            toolbar.add(button);
        }

        /*GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 2;
        c2.gridy = 0;
        c2.gridwidth = 2;
        c2.gridheight = 1;
        c2.fill = GridBagConstraints.BOTH;*/
        right.add(toolbar);

        textOutput = new PrintableTextPane();
        textOutput.setEditable(false);
        textOutput.setContentType(Constants.CONTENT_TYPE);

        textOutput.setSize(400, 600);
        /*c.ipady = 0;
        c.ipadx = 350;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 2;
        c.anchor = GridBagConstraints.PAGE_START;*/
        JScrollPane scrollPane3 = new JScrollPane(textOutput);
        right.add(scrollPane3);

        instructionsText = new JTextPane();
        instructionsText.setEditable(false);
        instructionsText.setContentType("text/html");
        /*c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weighty = 0.5;
        c.weightx = 0.5;
        c.ipady = 0;
        c.ipadx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
         */
        //add(instructionsText, c);





        left.add(instructionsText);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(left);
        splitter.setRightComponent(right);

        add(splitter);

        //Adding a Menu Bar
        MenuFiles demo = new MenuFiles(analyse.getDayInfo().clone());
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(demo.createFileMenu(this));
        menuBar.add(demo.createHelpMenu(this));
        menuBar.setFont(currentFont);
        setJMenuBar(menuBar);


        pack();
        setSize(700, 600);
        setVisible(true);
	}

    /**
     * Parse the bible.xml file to obtain all the necessary information
     */
	protected void parseBibleXML() {
        // PARSE THE BIBLE.XML FILE TO OBTAIN ALL THE NECESSARY INFORMATION
		try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.BML_FILE), StandardCharsets.UTF_8));	//Unicodised it.
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println(captions[8] + e.toString());
        }
	}

    public Bible(OrderedHashtable dayInfo) {
        analyse.setDayInfo(dayInfo);
        text = new LanguagePack(dayInfo);
        captions = text.obtainValues((String) text.getPhrases().get("BibleW"));
        //new Bible("Gen", "1:1-13"); <-- removed by Y.S. (not sure why, A.A.)
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String elem, Hashtable table) {
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
            if (analyse.evalbool(table.get("Cmd").toString()) == false) {
                return;
            }
        }
        
        readFile = true;
        
        if (elem.equals("BIBLE")) {
            versions2.put(table.get("Id").toString(), table.get("Name").toString());
            
            lastversion = (String) table.get("Id");
            currentBible = (String) table.get("Name");
            findId.put(table.get("Name"), table.get("Id"));//ADDED Y.S.
        }
        if (elem.equals("INFO")) {
            //ADDED Y.S. 2001211 n.s.
            Font value1a = (Font) UIManager.get("Menu.font");

            String displayFontA = "";
            String displaySizeA = "";

            if (table.get(FONT_FACE) == null) {

                displayFontA = value1a.getFontName();
                if (displayFontA.equals(Constants.PONOMAR_UNICODE_TT))
                {
                    displayFontA = Constants.TIMES_NEW_ROMAN;
                }

            } else {
                displayFontA = (String) table.get(FONT_FACE);
            }
            if (table.get(FONT_SIZE) == null) {

                displaySizeA = Integer.toString(value1a.getSize());
            } else {
                displaySizeA = (String) table.get(FONT_SIZE);
            }
            String name = currentBible;
            String alignText = "left";
            if (table.get(ORIENT) != null) {
                if (table.get(ORIENT).equals("rtl")) {
                    alignText = "right";
                }
            }

            String entry = "<html><p style=\"font-family:" + displayFontA + ";font-size:" + displaySizeA + "pt;text-align:" + alignText + "\">" + name + "</p></html>";
            versions.put(lastversion, "<html><p style=\"font-family:" + displayFontA + ";font-size:" + displaySizeA + "pt;text-align:" + alignText + "\">" + name + "</p></html>");
            findId.put(entry, lastversion);


            if (curversion.equals(lastversion)) {
                if (table.get(ORIENT) == null) {
                    orientText = ComponentOrientation.LEFT_TO_RIGHT;
                    
                } else {
                    
                    if (table.get(ORIENT).equals("rtl")) {
                        orientText = ComponentOrientation.RIGHT_TO_LEFT;

                    } else {
                        orientText = ComponentOrientation.LEFT_TO_RIGHT;
                        
                        //But there could be other future orientations, such as tlb,etc...
                    }
                }
                chapterName = (String) table.get("ChapterN");
                chapterNameI = (String) table.get("ChapterNI");
                header = (String) table.get("HeaderFormat");
                abbrevFormat=(String)table.get("AbbrevFormat");
                verseNumbered=(String)table.get("VerseNumFormat");
                verseNoNumbered=(String)table.get("VerseNoNumFormat");
                verseLink=(String)table.get("VerseLink");
                String a = (String) table.get("VerseNo");
                verseNumber = a.split(",");
                a = (String) table.get("ChapterNo");
                chapterNumber = a.split(",");
                a= (String) table.get("Parts");
                halfVerse=a.split(",");
                
                cVSep = (String) table.get("CVSep");	//Chapter Verse Separator: Book Chapter:Verse or Book Chapter,Verse or something else
                duration = (String) table.get("Duration"); //SEPARATOR BETWEEN THE ENDS OF A CONTINUOUS READING: 3:2-4:5, or 3:2-10
                selectionSeparator = (String) table.get("SelectionSeparator"); //SEPARATOR BETWEEN SELECTIONS OF READINGS, Exodus 3:2, 4:5-10, 10:10-11:3
                //ALLOWINS DIFFERENT FONTS TO BE USED: 2009/02/16 n.s.
                Font value1 = (Font) UIManager.get("Menu.font");
                


                if (table.get(FONT_FACE) == null) {

                    displayFont = value1.getFontName();
                    if (displayFont.equals(Constants.PONOMAR_UNICODE_TT))
                    {
                        displayFont = Constants.TIMES_NEW_ROMAN;
                    }

                } else {
                    displayFont = (String) table.get(FONT_FACE);
                }
                if (table.get(FONT_SIZE) == null) {

                    displaySize = Integer.toString(value1.getSize());
                } else {
                    displaySize = (String) table.get(FONT_SIZE);
                }

                currentFont = new Font(displayFont, Font.PLAIN, Integer.parseInt(displaySize));               

            }
        } else if (elem.equals("BOOK")) {
            if (curversion.equals(lastversion)) {
                books.put(table.get("Id"), table.get("Name"));
                chapters.put(table.get("Id"), table.get("Chapters"));
                intro.put(table.get("Id").toString(), table.get("Intro").toString());  //ADDED Y.S.

                abbrev.put(table.get("Id"), table.get("Short"));
            }
        }
    }

    public void endElement(String elem) {
        if (elem.equals("BIBLE")) {
            lastversion = "";
        }
        
    }

    public void text(String text) {
    }

    public void valueChanged(ListSelectionEvent e) {
        // FIGURE OUT WHICH LIST WAS CHANGED
        if (e.getSource().equals(booksBox) && changeItBooks) {
            changeIt = false;
            // then, get the book
            int n = booksBox.getSelectedIndex();
            int m = Integer.parseInt(chapters.values().toArray()[n].toString());
            // clear chaptersBox
            chaptersBox.removeAll();
            Vector<String> dummy = new Vector<String>();
            // add the chapters of book n
            for (int i = 1; i <= m; i++) {
                dummy.addElement(chapterNameI.replace("^NN", chapterNumber[i]));	//QAZ CHANGED Y.S. 2008/12/11 n.s. FOR DIFFERENT FORMAT VERSIONS
            }
            chaptersBox.setListData(dummy);
            curbook = books.keySet().toArray()[n].toString();
            int curchap = curpassage.indexOf(':') != -1 ? (int) Integer.parseInt(curpassage.substring(0, curpassage.indexOf(':'))) : (int) Integer.parseInt(curpassage);
            chaptersBox.setSelectedValue(curchap, true);
            changeIt = true;
        } else if ((e.getSource().equals(chaptersBox)) && !e.getValueIsAdjusting()) {
            if (changeIt) {
                // NAVIGATE TO a different chapter
                int n = chaptersBox.getSelectedIndex();
                update(curbook, Integer.toString(n + 1));
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Helpers helper = new Helpers(analyse.getDayInfo());
        String name = e.getActionCommand();
        String[] fileNames = text.obtainValues((String) text.getPhrases().get("File"));
        String[] helpNames = text.obtainValues((String) text.getPhrases().get("Help"));
        //ALLOWS A MULTILINGUAL PROPER VERSION
        if (name.equals("comboBoxChanged")) {
            //curversion = versions2.get(findId.get(versionsBox.getSelectedIndex()).toString()).toString();
            curversion = findId.get(Objects.requireNonNull(versionsBox.getSelectedItem()).toString()).toString();
            //REREAD THE BIBLE.XML FILE FOR THE NEW READINGS
            books = new OrderedHashtable();
            chapters = new OrderedHashtable();
            try {

                BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.BML_FILE), StandardCharsets.UTF_8));	//Unicodised it.
                QDParser.parse(this, frf);
            } catch (Exception ew) {
                System.out.println("Error reading bmlfile: " + ew.toString());
            }
            changeItBooks = false;
            booksBox.removeAll();
            booksBox.setListData(new Vector(books.values()));
            changeItBooks = true;
            //Adding local direction control, so that the direction of the Bible text
            //reflects the correct internal language direction
            //System.out.println(OrientText.toString());
            instructionsText.setComponentOrientation(orientText);
            booksBox.setComponentOrientation(orientText);
            chaptersBox.setComponentOrientation(orientText);
            textOutput.setComponentOrientation(orientText);
            //to here Y.S. 2010/07/01 n.s.
            update(curbook, curpassage);

        } else if (name.equals(helpNames[2])) {
            //new About();
            Helpers orient = new Helpers(analyse.getDayInfo());
            orient.applyOrientation(new About(analyse.getDayInfo()), (ComponentOrientation) analyse.getDayInfo().get(ORIENT));

        } else if (name.equals(helpNames[0])) {
            //HELP FILES
        } else if (name.equals(fileNames[1])) {
            //SAVE THE CURRENT WINDOW
            helper.saveHTMLFile((String) books.get(curbook) + "_" + formatPassage(curpassage).replace(":", "-") + ".html", "<html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><TITLE>" + (String) books.get(curbook) + " " + formatPassage(curpassage) + "</TITLE>" + printText);
        } else if (name.equals(fileNames[4])) {
            dispose();
        } else if (name.equals(fileNames[6])) {
            helper.sendHTMLToPrinter(textOutput);
        } else {
            int butnum = Integer.parseInt(name);
            if (butnum == 0) {
                // go back (prior chapter)
                int curchap = processCurrentChapter();
                curchap--;

                if (curchap == 0) {
                    // we have reached the beginning of this book
                    // get the previous book
                    if (curbook.contains("Gen")) {
                        curbook = "Apoc";
                    } else {
                        Object[] booknames = books.keySet().toArray();
                        for (int i = 0; i < booknames.length; i++) {
                            if (booknames[i].toString().equals(curbook)) {
                                curbook = booknames[i - 1].toString();
                                break;
                            }
                        }
                    }
                    curchap = Integer.parseInt((String) chapters.get(curbook));
                }
                curpassage = Integer.toString(curchap);

                update(curbook, curpassage);
                chaptersBox.setSelectedValue(curchap, true);
                changeIt = true;
            } else if (butnum == 1) {
                // get whole chapter button
                int curchap = 0;
                if (curpassage.indexOf(':') == -1) {
                    return;
                } else {
                    curchap = Integer.parseInt(curpassage.substring(0, curpassage.indexOf(':')));
                }

                curpassage = Integer.toString(curchap);
                update(curbook, curpassage);
            } else if (butnum == 2) {
                // go forward (next chapter)
                int curchap = processCurrentChapter();
                curchap++;

                if (curchap > Integer.parseInt((String) chapters.get(curbook))) {
                    // we have reached the beginning of this book
                    // get the previous book
                    if (curbook.contains("Apoc")) {
                        curbook = "Gen";
                    } else {
                        Object[] booknames = books.keySet().toArray();
                        for (int i = 0; i < booknames.length - 1; i++) {
                            if (booknames[i].toString().equals(curbook)) {
                                curbook = booknames[i + 1].toString();
                                break;
                            }
                        }
                    }
                    curchap = 1;
                }
                curpassage = Integer.toString(curchap);

                update(curbook, curpassage);
                chaptersBox.setSelectedValue(curchap, true);
                changeIt = true;
            } else if (butnum == 3) {
                // bookmark button clicked ...
                System.out.println("Bookmark option not implemented ...");
            } else if (butnum == 4) {
                // requires a selection! ... according to Sun, getSelectionStart() should return
                // "the value of dot" if there is no seleciton. But it appears instead to return zero ...
                if (textOutput.getSelectionStart() == 0) {
                    textOutput.selectAll();
                }
                textOutput.copy();
            } else if (butnum == 5) {
                // save button clicked ...
                
                helper.saveHTMLFile((String) books.get(curbook) + "_" + formatPassage(curpassage).replace(":", "-") + ".html", "<html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><TITLE>" + (String) books.get(curbook) + " " + formatPassage(curpassage) + "</TITLE>" + printText);
            } else if (butnum == 6) {
                // print button clicked ...
                helper.sendHTMLToPrinter(textOutput);
            }else if (butnum ==7){
                versed=!versed;
                update(curbook, curpassage);
            }
        }
    }

	protected int processCurrentChapter() {
		int currentChapter;
		changeIt = false;
		if (curpassage.indexOf(':') != -1) {
		    // we have a composite passage
		    currentChapter = Integer.parseInt(curpassage.substring(0, curpassage.indexOf(':')));
		} else {
		    currentChapter = Integer.parseInt(curpassage);
		}
		return currentChapter;
	}

	/**
	 * Updates the Scripture reading, setting a new reading.
	 */
    protected void update(String newBook, String newPassage) {
        // we will parse the passage
        // update the variables
        // fetch the new text from the file
        // and update the GUI
        // first, define some variables
        changeIt = false;
        instructions = "";

        String[] stuff = parseReadings(newBook, newPassage, versed);

        curbook = newBook;
        curpassage = newPassage;

        textOutput.setCaretPosition(0);

        textOutput.setContentType(Constants.CONTENT_TYPE);
        textOutput.setFont(currentFont);
        String headerA = header.replace(NAME, (String) books.get(curbook));
        headerA = headerA.replace("^CNN", formatPassage(curpassage));
        

        printText = "<body style=\"font-family:" + displayFont + ";font-size:" + displaySize + "pt\">" + headerA +  stuff[0] + "</body>";

        textOutput.setText(printText);
       

        //ALLOWING THE FONTS TO BE CHANGED: 2009/02/16 n.s.
        booksBox.setFont(currentFont);
        chaptersBox.setFont(currentFont);

        booksBox.setSelectedValue(books.get(curbook), true);
        textOutput.setCaretPosition(0);
        instructionsText.setText("<body style=\"font-family:" + displayFont + ";font-size:" + displaySize + "pt\">" + instructions + "</body>");
        instructionsText.setFont(currentFont);
        changeIt = true;


    }

    public String[] getText(String book, String passage, boolean redStuff) {
        //SELECT THE CURRENT GENERAL BIBLE
        
        LanguagePack getLang = new LanguagePack(analyse.getDayInfo());
        curversion = getLang.getPhrases().get(BIBLE_V).toString();
        parseBibleXML();

        //CALL THE PARSE READER AND OBTAIN THE RETURNED RESULTS: [0] contains the readings with or without extra markings, [1] contains any special instructions, and [2] contains the header, that is, a properly formated version of the reading.
        return parseReadings(book, passage, redStuff);
    }

    private String process(String mText, boolean redStuff) {
        int k = mText.indexOf("**");
        if (k != -1) {
            // found scripture reading instructions ... parse
            String first = mText.substring(0, k);
            String third = mText.substring(mText.lastIndexOf("**") + 2);
            String second = mText.substring(k + 2, mText.lastIndexOf("**"));
            if (redStuff) {
                mText = first + "<SPAN style=\"color:red;\">**</SPAN>" + third;
            } else {
                mText = first + "**" + third;
            }
            instructions += "**" + second + "<BR>";
        }
        if (redStuff) {
            mText = mText.replace("*(", "<SPAN style=\"color:red;\">");
            mText = mText.replace(")*", "</SPAN>");
        } else {
            mText = giveRequiredTextWithoutComments(mText);
        }
        k = mText.indexOf('|');
        if (k != -1 && redStuff) {
            int verseNo=Integer.parseInt(mText.substring(0, k));
            String textVerse=mText.substring(k+1);
            if (mText.substring(k+1).equals(" ")){
                textVerse=mText.substring(k+2);
            }
            if (verseNo==0){
                return verseNumber[0].replace("^TT",textVerse); //mText.substring(k+2));
            }
            else{
                String versedText=verseNumbered.replace("^VN",verseNumber[Integer.parseInt(mText.substring(0, k))]);
                versedText=versedText.replace("^VT",textVerse);//mText.substring(k+2));
                return versedText;
            //return " <SUP>" + VerseNumber[Integer.parseInt(mText.substring(0, k))] + "</SUP>" + mText.substring(k + 1); //ADDED A SPACE BETWEEN THE LAST SENTENCE AND THE VERSE NUMBER Y.S. AND CORRECTED MULTILINGUAL ISSUES
            }
        } else if (k != -1 && !redStuff) {
            int verseNo=Integer.parseInt(mText.substring(0, k));
             String textVerse=mText.substring(k+1);
            if (mText.substring(k+1).equals(" ")){
                textVerse=mText.substring(k+2);
            }
            if (verseNo==0){
                return "";//VerseNumber[0].replace("^TT", mText.substring(k+2));
            }
            else{
                return verseNoNumbered.replace("^VT",textVerse);//mText.substring(k + 2));
            }
        }

        k = mText.indexOf('#');
        if (k != -1 && redStuff) {
            String chapterNameF = chapterName.replace("^NN", chapterNumber[Integer.parseInt(mText.substring(1))]); //ToneFormat.replace("TT",toneNumbers[tone])
            chapterNameF=chapterNameF.replace(NAME, (String) books.get(curbook));
            return "<BR>" + chapterNameF + ""; //ADDED MULTILINGUAL SUPPORT
        } else if (k != -1 && !redStuff) {
            return "";
        }
        return mText;
    }

	protected String giveRequiredTextWithoutComments(String mText) {
		int k2 = mText.indexOf("*(");
		while (k2 != -1) {
		    String first = mText.substring(0, k2);
		    String third = mText.substring(mText.indexOf(")*") + 2);
		    mText = first + " " + third;
		    k2 = mText.indexOf("*(");
		}
		return mText;
	}

    //ADDED BY YURI SHARDT 2008/09/20 TO MULTILINGUILISE THE READINGS!
    public String getAbbrev(String id) {
        id = id.replace(' ', '_'); //THIS CAN BE AVOIDED IF THE DEFINITIONS ARE CHANGED

        //INITIALISE TO THE DEFAULT BIBLE FOR THE GIVEN LANGUAGE
        //ADDED Y.S. TO ALLOW FOR MULTILINGUAL AND ALL BIBLE READING
        
        LanguagePack getLang = new LanguagePack(analyse.getDayInfo());
        curversion = getLang.getPhrases().get(BIBLE_V).toString();
        //System.out.println("Bible: " + curversion);

        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.BML_FILE), StandardCharsets.UTF_8));
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println(captions[9] + e.toString());
        }

        return (String) abbrev.get(id);
    }

    private String formatPassage(String newPassage) {
        //System.out.println("Hello there, passage: "+newPassage);
        if (newPassage.indexOf(':') == -1) {
            // just a chapter specification, e.g. Gen_1
            int d = Integer.parseInt(newPassage);
            String chapterNameF = chapterNameI.replace("^NN", chapterNumber[d]);
            newPassage = chapterNameF;
            return newPassage;
        } else {
            // e.g. 2:11-3:2, 5, 13-14, 17-4:1
            String[] parts = newPassage.split(",");

            StringBuilder newPassageBuilder = new StringBuilder(newPassage);
            for (int j = 0; j < parts.length; j++) {
                
                //e.g. 2:11-3:2 or 13-14 or 5 or 4:5
                if (parts[j].indexOf('-') == -1) {
                    // the example of 5 or 4:5; replicate
                    if (parts[j].indexOf(':') == -1) {
                        //System.out.println("Testing the parsing function: Integer: "+Integer.toString(obtainNumber(parts[j]))+" Fraction: "+obtainPart(parts[j]));
                        parts[j] = verseNumber[obtainNumber(parts[j])]+obtainPart(parts[j]);
                    } else {
                        
                        //System.out.println("Testing the parsing function: Integer: "+Integer.toString(obtainNumber(parts[j]))+" Fraction: "+obtainPart(parts[j]));
                        int verse = +obtainNumber(parts[j].split(":")[1]);
                        int chapter = Integer.parseInt(parts[j].split(":")[0]);
                        parts[j] = chapterNumber[chapter] + cVSep + verseNumber[verse]+obtainPart(parts[j].split(":")[1]);
                    }
                } else {
                    String[] sections = parts[j].split("-");
                    

                    for (int k = 0; k < sections.length; k++) {
                        
                        if (sections[k].indexOf(':') == -1) {
                            
                            // E.g. 13 or 5
                            //System.out.println("Testing the parsing function: Integer: "+Integer.toString(obtainNumber(sections[k]))+" Fraction: "+obtainPart(sections[k]));
                            sections[k] = verseNumber[obtainNumber(sections[k])]+obtainPart(sections[k]);
                            
                        } else {
                            //System.out.println("Hello midpoint + " + sections[k] + " "+sections[k]);
                            //System.out.println(sections[k].split(":")[1]);
                            int verse = +obtainNumber(sections[k].split(":")[1]);
                            //System.out.println(verse);
                            int chapter = Integer.parseInt(sections[k].split(":")[0]);
                            //System.out.println(chapter);
                            sections[k] = chapterNumber[chapter] + cVSep + verseNumber[verse]+obtainPart(sections[k].split(":")[1]);
                        }
                        //System.out.println(sections[k] + "testing parts: "+parts[j]);
                        if (k == 0) {
                            parts[j] = sections[k];
                        } else {
                            parts[j] = parts[j] + duration + sections[k];
                        }
                        
                    }
                }
                //RECONSTRUCT THE GIVEN READING PART
                if (j == 0) {
                    newPassageBuilder = new StringBuilder(parts[j]);
                } else {
                    newPassageBuilder.append(selectionSeparator).append(parts[j]);
                }
            }
            newPassage = newPassageBuilder.toString();
            //System.out.println("Hello end");
        }
        return newPassage;
    }

    /**
     * This function creates the hyperlink for Bible readings.
     * <p>
     * Created Y.S. 2008/12/11 n.s.
     * @return A String containing the hyperlink
     */
    public String getHyperlink(String reading) {
       if (reading.length()<1)
       {
           return "";
       }
        LanguagePack getLang = new LanguagePack(analyse.getDayInfo());
        curversion = getLang.getPhrases().get(BIBLE_V).toString();

        parseBibleXML();

        String[] parts = reading.split("_");
        //TODO: get rid of these string operations. They are bad, and take up too much CPU time
        //I've replaced replaceAll() with replace(). SonarLint suggests that this has less performance 
        //impact, because replaceAll() doesn't call regex-related methods
   
        String passage = parts[1].replace(" ", "");
        String output = "<A Href=reading#" + parts[0].replace(' ', '_') + "#" + passage + ">";
        String headerA = abbrevFormat.replace(NAME, getAbbrev(parts[0]));
        headerA = headerA.replace("^CNN", formatPassage(passage));
        
        output += headerA + "</A>";
        return output;
    }

    /**
     * This function creates the hyperlink for Bible readings.
     * <p>
     * Created Y.S. 2008/12/11 n.s.
     * @param redStuff  Boolean that determines whether or not any of the read comments in the bible readings are retained!
     */
    public String[] parseReadings(String newBook, String newPassage, boolean redStuff) {
        //redStuff IS A BOOLEAN THAT DETERMINES WHETHER OR NOT ANY OF THE READ COMMENTS IN THE BIBLE READINGS ARE RETAINED!
        //THIS INCLUDES COMMENTS, (VERSE NUMBERS), CHAPTER NUMBERS,...
        //ALL EXTRA HEADERS WILL BE SENT TO A SEPARATE HOLDING VARIABLE AND ONLY THE FIRST ** IS KEPT!
        Vector<Integer> pChapters = new Vector<>(); // stores the chapter #s
        Vector<Integer> pVerses = new Vector<>(); // stores the verse #s
        int i = 0;			 // dummy
        newBook = newBook.replace(" ", "_");
        instructions = "";

        if (newPassage.indexOf(':') == -1) {
            // just a chapter specification, e.g. Gen_1
            int d = Integer.parseInt(newPassage);
            pChapters.add(d);
            pChapters.add(d + 1);
            pVerses.add(1);
            pVerses.add(-1); // -1 means -- stop before chapter starts
        } else {
            // e.g. 2:11-3:2, 5, 13-14, 17-4:1
            String[] parts = newPassage.split(",");

            for (int j = 0; j < parts.length; j++) {
                //e.g. 2:11-3:2 or 13-14 or 5
                if (parts[j].indexOf('-') == -1) {
                    // the example of 5; replicate
                    parts[j] = parts[j] + "-" + parts[j];
                }
                String[] sections = parts[j].split("-");

                for (String section : sections) {
                    int verse = 0;
                    int chapter = i;
                    if (section.indexOf(':') == -1) {
                        // E.g. 13 or 5
                        verse = obtainNumber(section); //UPDATABLE: Once we can deal with partial verses.
                    } else {
                        verse = obtainNumber(section.split(":")[1]); //UPDATABLE: Once we can deal with partial verses.
                        chapter = Integer.parseInt(section.split(":")[0]);
                        i = chapter;
                    }

                    pChapters.add(chapter);
                    pVerses.add(verse);
                }
            }
        }

        // that completes the parsing process
        // now update the system variables
        curbook = newBook;
        curpassage = newPassage;
        return readBibleFiles(curbook, curpassage, redStuff, pVerses, pChapters);
    }

    public String[] readBibleFiles(String curbook, String curpassage, boolean redStuff, Vector<Integer> pVerses, Vector<Integer> pChapters) {
        String filename = Constants.LANGUAGES_PATH + "/" + curversion + "/" + curbook + ".text";
        StringBuilder ret = new StringBuilder();

        try {
            //BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), StandardCharsets.UTF_8));
            FileInputStream fis = new FileInputStream(filename);
            //REMOVED TO ALLOW FOR NON-ASCII FILES Y.S. 2008/10/24 ns
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            String mLine = "";
            int nCurChapter = 0;
            int nCurVerse = -1;
            boolean printMe = false;

            Enumeration<Integer> e2 = pVerses.elements();

            for (Enumeration<Integer> e = pChapters.elements(); e.hasMoreElements();) {
                Object c = e.nextElement();
                //Correcting issues with not being able to read all the desired readings Y.S. 2008/12/12 n.s.
                while (nCurChapter != Integer.parseInt(c.toString())) {
                    mLine = br.readLine();
                    if (mLine == null) {
                        break;
                    }

                    // format #number, where number is the chapter #
                    if (mLine.indexOf('#') != -1) {
                        //CHANGED DUE TO UNICODE ISSUES Y.S. 2008/10/24 ns
                        //System.out.println(mLine);
                        if (mLine.indexOf('#') == 1) {
                            nCurChapter = Integer.parseInt(mLine.substring(2));
                        } else {
                            nCurChapter = Integer.parseInt(mLine.substring(1));
                        }
                        nCurVerse = -1;
                    }

                    if (printMe) {
                        if (!ret.toString().equals("")){
                        ret.append(verseLink).append(process(mLine, redStuff));
                        }else{
                            ret.append(process(mLine, redStuff));
                        }
                    }
                }

                Object v = e2.nextElement();
                //Correcting issues with not being able to read all the desired readings Y.S. 2008/12/12 n.s.
                while (nCurVerse != Integer.parseInt(v.toString())) {
                    if (mLine == null) {
                        break;
                    }

                    mLine = br.readLine();
                    // format number|text, where number is verse number
                    int n = mLine.indexOf('|');
                    if (n != -1) {
                        nCurVerse = Integer.parseInt(mLine.substring(0, n));
                    }
                    //Correcting issues with not being able to read all the desired readings Y.S. 2008/12/12 n.s.
                    if (printMe || nCurVerse == Integer.parseInt(v.toString()) || nCurVerse == 0) {
                        if (!ret.toString().equals("")){
                        ret.append(verseLink).append(process(mLine, redStuff));
                        }else{
                            ret.append(process(mLine, redStuff));
                        }
                        if (nCurChapter == Integer.parseInt(pChapters.elementAt(0).toString()) && nCurVerse == Integer.parseInt(pVerses.elementAt(0).toString())) {
                            //THE FIRST VERSE HAS BEEN READ, THE INSTRUCTIONS ASSOCIATED WITH THIS VERSE NEED TO BE SAVED
                            instructFirst = instructions;
                        }

                    }
                }
                printMe = !printMe;
            }
            //CHANGED TO ALLOW FOR NON-ASCII FILES Y.S. 2008/10/24 ns
            br.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        String[] output1 = new String[3];
        output1[0] = ret.toString();
        output1[1] = instructFirst;
        String headerA = header.replace(NAME, (String) books.get(curbook));
        headerA = headerA.replace("^CNN", formatPassage(curpassage));
        
        output1[2] = headerA;
        return output1;
    }

	/**
	 * Only the complete reading needs to be sent for the composite reading;
	 * otherwise the book of the bible will suffice. Initialise to the default bible
	 * for the given language. Added Y.S. To allow for multilingual and all bible
	 * reading
	 */
    public String getIntro(String id) {
        //ONLY THE COMPLETE READING NEEDS TO BE SENT FOR THE COMPOSITE READING; OTHERWISE THE BOOK OF THE BIBLE WILL SUFFICE
        //INITIALISE TO THE DEFAULT BIBLE FOR THE GIVEN LANGUAGE
        //ADDED Y.S. TO ALLOW FOR MULTILINGUAL AND ALL BIBLE READING
       
        LanguagePack getLang = new LanguagePack(analyse.getDayInfo());
        curversion = getLang.getPhrases().get(BIBLE_V).toString();

        parseBibleXML();
        int k = id.lastIndexOf('_');
        if (k != -1) {
            if (id.substring(0, k).equals("Composite")) {
                String[] result = parseReadings(id.substring(0, k), id.substring(k + 1), false);
                int star1 = result[1].indexOf('\"');	//WE ARE INTERESTED IN THE QUOTATION INFORMATION
                int starL = result[1].lastIndexOf('\"');
                System.out.println(result[1].substring(star1 + 1, starL) + " value of star1 " + star1 + " value of starL " + starL);
                return result[1].substring(star1 + 1, starL).replace("...", "");
            } else {
                return (String) intro.get(id.substring(0, k));
            }
        }
        return (String) intro.get(id);


    }
    private int obtainNumber(String verse)
    {
        //System.out.println(verse+" inside the obtainNumber function");
        //Allows us to separate the part after the number from the number in the case that
        //we are dealing with part of a verse, for example 29a or 30b, where the verse is 29 and the part is a.
        int size=verse.length();
        //System.out.println(size);
        if (Character.isDigit(verse.charAt(size-1)))
        {
            //System.out.println("Case 1");
            return Integer.parseInt(verse);
        }else
        {
            //System.out.println("Case 2");
            if (size<3){
                //System.out.println("Case 2a");
                return Integer.parseInt(verse.substring(0,1));
            }else{
                //System.out.println("Case 2b");
            return Integer.parseInt(verse.substring(0,size-1));
            }
        }
    }
    private String obtainPart(String verse)
    {
        //Allows us to separate the part after the number from the number in the case that
        //we are dealing with part of a verse, for example 29a or 30b, where the verse is 29 and the part is a.
        int size=verse.length();
        //System.out.println("Testing obtainpart: "+verse.substring(size-1));
        if (Character.isDigit(verse.charAt(size-1)))
        {
            return "";
        }else
        {
            String part=verse.substring(size-1);
            switch (part) {
                case "a":
                    return halfVerse[0];
                case "b":
                    return halfVerse[1];
                case "c":
                    return halfVerse[2];
            }
            
            return verse.substring(size-1);
        }
    }

    public static void main(String[] argz) {
        //DEBUG MODE
        System.out.println("Bible.java running in Debug mode");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");
        OrderedHashtable dayInfo = new OrderedHashtable();
        dayInfo.put("LS", "0");
        new Bible(dayInfo);
    }
}