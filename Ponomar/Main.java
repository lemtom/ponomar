package Ponomar;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.*;

/***********************************************************************
Main.java :: MAIN MODULE FOR THE PONOMAR PROGRAM.
THIS MODULE CONSTITUTES THE PRIMARY PONOMAR GUI AND CENTRE OF THE PROGRAM.
TO START THE PROGRAM, INVOKE main(String[]) OF THIS CLASS.
OUTPUTS RELEVANT INFORMATION FOR EACH DAY, WITH LINKS TO DETAILED INFO.

Main.java is part of the Ponomar program.
Copyright 2006, 2007, 2008, 2009, 2010, 2012 Aleksandr Andreev and Yuri Shardt.
Corresponding e-mail aleksandr.andreev@gmail.com

Ponomar is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

While Ponomar is distributed in the hope that it will be useful,
it comes with ABSOLUTELY NO WARRANTY, without even the implied warranties of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for details.
 ***********************************************************************/
public class Main extends JFrame implements PropertyChangeListener, HyperlinkListener, ActionListener {
    // First, some relevant constants

    //private static final String generalFileName="Ponomar/xml/";
    private static final String triodionFileName = "xml/triodion/";   // TRIODION FILE
    private static final String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
    private static final String newline = "\n";
    // Elements of the interface
    JDate2 today; 		// "TODAY" (I.E. THE DATE WE'RE WORKING WITH
    private final JCalendar calendar; 	// THE CALENDAR OBJECT
    private final PrintableTextPane text; 	// MAIN TEXT AREA FOR OUTPUT
    private JDate2 pascha; 		// THIS YEAR'S PASCHA
    private JDate2 pentecost; 	// THIS YEAR'S PENTECOST
    private Stack fastInfo;		// CONTAINS A VECTOR OF THE FASTING INFORMATION FOR TODAY, WHICH IS LATER PASSED TO CONVOLVE()
    private LinkedHashMap<Object, Object> readings;	// CONTAINS TODAY'S SCRIPTURE READING
    private String output;  	// TODAY'S CALENDAR OUTPUT
    private boolean inited = false; // PREVENTS MULTIPLE READING OF XML FILES ON LAUNCH
    private Bible bible;
    private final GospelSelector gospelLocation;		//THE GOSPEL SELECTOR OBJECT
    private final LanguageSelector languageLocation;
    private final JMenuBar menuBar;
    private final MenuFiles demo;
    private final LanguagePack phrases;
    private static String[] toneNumbers;
    private static String[] errors;
    private static String[] mainNames;
    private final String[] saintNames;
    private final String[] fileNames;
    private final String[] serviceNames;
    private final String[] bibleName;
    private final String[] helpNames;
    private String displayFont = ""; //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
    private String displaySize = "12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
    private final Font defaultFont = new Font("", Font.BOLD, 12);		//CREATE THE DEFAULT FONT
    private Font currentFont = defaultFont;
    private String rSep = "";
    private String cSep = "";
    private String colon = "";
    private String ideographic = "";
    private DoSaint1 saintLink;
    private final IconDisplay displayIcon;
    private Vector iconImages;
    private Vector iconNames;
    private final String orderBox;
    private final StringOp analyse = new StringOp();
    private int displayCal=0;
    private int religiousCal=0;
    //private GospelSelector Selector;
    final Helpers findLanguage;

    // CONSTRUCTOR
    public Main() {
        //super("Ponomar");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        //WE NEED THIS HANDY STORER OF VALUES NOW.
        //StringOp.dayInfo = new LinkedHashMap<Object, Object>();
        //DETERMINE THE DEFAULTS
        ConfigurationFiles.Defaults = new LinkedHashMap<>();
        ConfigurationFiles.ReadFile();
	displayCal=Integer.parseInt(ConfigurationFiles.Defaults.get("DisplayCalendar").toString());
	religiousCal=Integer.parseInt(ConfigurationFiles.Defaults.get("ReligiousCalendar").toString());
        languageLocation = new LanguageSelector(analyse.dayInfo);
	//System.out.println("Language Selected: "+LanguageLocation.getLValue().toString());
        analyse.dayInfo.put("LS", languageLocation.getLValue());
        phrases = new LanguagePack(analyse.dayInfo);
        //Changing language storage format
        findLanguage = new Helpers(analyse.dayInfo);

        toneNumbers = phrases.obtainValues((String) phrases.Phrases.get("Tones"));
        saintNames = phrases.obtainValues((String) phrases.Phrases.get("SMenu"));
        fileNames = phrases.obtainValues((String) phrases.Phrases.get("File"));
        serviceNames = phrases.obtainValues((String) phrases.Phrases.get("Services"));
        bibleName = phrases.obtainValues((String) phrases.Phrases.get("Bible"));
        helpNames = phrases.obtainValues((String) phrases.Phrases.get("Help"));
	//EditComm = Phrases.obtainValues((String) Phrases.Phrases.get("EditComm"));
        //EditPrayers=Phrases.obtainValues((String) Phrases.Phrases.get("EditPrayers")); //to change to Prayers!

        errors = phrases.obtainValues((String) phrases.Phrases.get("Errors"));
        mainNames = phrases.obtainValues((String) phrases.Phrases.get("Main"));
        displayFont = (String) phrases.Phrases.get("FontFaceM");
        displaySize = (String) phrases.Phrases.get("FontSizeM");
        orderBox = (String) phrases.Phrases.get("OrderBox");

        Font value1 = (Font) UIManager.get("Menu.font");
        if (displaySize == null || displaySize.isEmpty()) {
            displaySize = Integer.toString(value1.getSize());
        }
        if (displayFont == null || displayFont.isEmpty()) {
            displayFont = value1.getFontName();
        }
        displaySize = Integer.toString(Math.max(Integer.parseInt(displaySize), value1.getSize())); //If the default user's font size is larger than the required there is not need to change it.
        //The specified fonts sizes are the mininum required.
        currentFont = new Font(displayFont, Font.PLAIN, Integer.parseInt(displaySize));
        //System.out.println(this.getFont());
        //System.out.println("Pause");
        //setDefaultLookAndFeelDecorated( true );
        //UIManager.put("Frame.font",CurrentFont);
        //this.setFont(CurrentFont);
        //This is a nifty way to set the default font for displaying everything in a programme. I (Y.S.) will
        //later work to implement it properly. At present, there seem to be some technical issues with obtaining
        //everything properly.
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                Font keyF = (Font) value;
                String[] splitkey = key.toString().replace(".", ":").split(":");
                //This prevents the font from being changed for those things that are to remain in the Latin alphabet!
                if (splitkey.length > 1) {
                    if (splitkey[splitkey.length - 1].equals("acceleratorFont")) {
                        continue;
                    }
                    /*if (splitkey[0].equals("Button"))
                    {
                    continue;
                    }*/
                }
                if (key.toString().equals("MenuItem.acceleratorFont")) {
                    continue;
                }
                Font newFont = new Font(currentFont.getFontName(), keyF.getStyle(), currentFont.getSize());
                UIManager.put(key, newFont);
                //System.out.println(key);
            }
        }

        //System.out.println(this.getFont());
        setTitle((String) phrases.Phrases.get("0"));
        rSep = (String) phrases.Phrases.get("ReadSep");
        cSep = (String) phrases.Phrases.get("CommSep");
        colon = (String) phrases.Phrases.get("Colon");
        analyse.dayInfo.put("FontFaceM", displayFont);
        analyse.dayInfo.put("FontSizeM", displaySize);
        analyse.dayInfo.put("ReadSep", rSep);
        analyse.dayInfo.put("Colon", colon);
        ideographic = (String) phrases.Phrases.get("Ideographic");
        analyse.dayInfo.put("Ideographic", ideographic);
        gospelLocation = new GospelSelector(analyse.dayInfo);

        //ADD A MENU BAR Y.S. 2008/08/11 n.s.
        demo = new MenuFiles(new LinkedHashMap<>(analyse.dayInfo));
        menuBar = new JMenuBar();
        menuBar.add(demo.createFileMenu(this));
        menuBar.add(demo.createOptionsMenu(this,this));
        menuBar.add(demo.createSaintsMenu(this));
        menuBar.add(demo.createServicesMenu(this));
        menuBar.add(demo.createBibleMenu(this));
        menuBar.add(demo.createHelpMenu(this));
        menuBar.setFont(currentFont);
        //MenuBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setJMenuBar(menuBar);

        JPanel left = new JPanel(new GridLayout(3, 0));
        calendar = new JCalendar(analyse.dayInfo);
        //System.out.println(calendar);
        calendar.addPropertyChangeListener(this);
        left.setLayout(new BorderLayout());
        left.add(calendar, BorderLayout.NORTH);
        displayIcon = new IconDisplay(new String[0], new String[0], analyse.dayInfo);
        left.add(displayIcon, BorderLayout.CENTER);



        JPanel right = new JPanel();
        text = new PrintableTextPane();
        text.setEditable(false);
        text.addHyperlinkListener(this);
        right.setLayout(new BorderLayout());
        right.add(text, BorderLayout.CENTER);
        right.setSize(200, 400);
        JScrollPane scrollPane3 = new JScrollPane(text);
        right.add(scrollPane3);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(left);
        splitter.setRightComponent(right);

        today = new JDate2(calendar.getMonth(), calendar.getDay(), calendar.getYear(),displayCal);
                
        setContentPane(splitter);

        Locale place = new Locale(phrases.Phrases.get("Language").toString(), phrases.Phrases.get("Country").toString());
        Helpers orient = new Helpers(analyse.dayInfo);
        analyse.dayInfo.put("Locale", place);
        analyse.dayInfo.put("Orient", ComponentOrientation.getOrientation(place));
        orient.applyOrientation(this, ComponentOrientation.getOrientation(place));
        this.validate();

        pack();
        setSize(700, 500);
        setVisible(true);
        
        //System.out.println("Testing the year: "+today.getYear());

        pascha = Paschalion.getPascha(today.getYear(),religiousCal);
        pentecost = Paschalion.getPentecost(today.getYear(),religiousCal);


        inited = true;
        Dimension screen = this.getSize();
        //Default screen size issues for East Asian languages!
        if (value1.getSize() < Integer.parseInt(displaySize)) {
            Dimension defaultScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

            //System.out.println(screen);
            int newSize = Integer.parseInt(displaySize);
            int maxW = 95 * defaultScreen.width / 100;
            int maxH = 95 * defaultScreen.height / 100;
            screen.width = java.lang.Math.min(screen.width * newSize / value1.getSize(), maxW);
            screen.height = java.lang.Math.min(screen.height * newSize / value1.getSize(), maxH);
            this.setSize(screen);
            //System.out.println(screen);
        }

        write();
    }

    public void propertyChange(PropertyChangeEvent e) {
        
        if (inited) {
            // FIND OUT THE OLD YEAR
            int year = today.getYear();
            today = new JDate2(calendar.getMonth(), calendar.getDay(), calendar.getYear(),displayCal);
            JDate2.setCalendar(religiousCal);
            //System.out.println("year is: "+year+" and religious year is: " +today.getYear());
            if (year != today.getYear()) {
                pascha = Paschalion.getPascha(today.getYear(),religiousCal);
                pentecost = Paschalion.getPentecost(today.getYear(),religiousCal);
            }

            write();
        }
    }

    public void actionPerformed(ActionEvent e) {
        Helpers helper = new Helpers(analyse.dayInfo);
        JMenuItem source = (JMenuItem) (e.getSource());
        String name = source.getText();
        if (name.equals(helpNames[2])) {
            //new About();
            Helpers orient = new Helpers(analyse.dayInfo);
            orient.applyOrientation(new About(analyse.dayInfo), (ComponentOrientation) analyse.dayInfo.get("Orient"));
        }
        if (name.equals(helpNames[0])) {
            //HELP FILES
        }
        if (name.equals(fileNames[1])) {
            //SAVE THE CURRENT WINDOW
            helper.SaveHTMLFile(mainNames[5] + " " + today + ".html", "<html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"><title>" + phrases.Phrases.get("0") + colon + today + "</title>" + output);
        }
        if (name.equals(fileNames[4])) {
            if (helper.closeFrame(mainNames[6])) {
                System.exit(0);
            }
        }
        if (name.equals(serviceNames[1])) {
            //DIVINE LITURGY
        }
        if (name.equals(serviceNames[2])) {
            //VESPERS
        }
        if (name.equals(serviceNames[3])) {
            //COMPLINE
        }
        if (name.equals(serviceNames[4])) {
            //MATINS
        }
        if (name.equals(serviceNames[5])) {
            //Create the primes service
            new Primes(today, analyse.dayInfo);
        }
        if (name.equals(serviceNames[6])) {
            new ThirdHour(today, analyse.dayInfo);
        }
        if (name.equals(serviceNames[7])) {
            new SixthHour(today, analyse.dayInfo);
            //SEXT
        }
        if (name.equals(serviceNames[8])) {
            new NinthHour(today, analyse.dayInfo);
            //NONE
        }
        if (name.equals(serviceNames[9])) {
            //ROYAL HOURS
            new RoyalHours(today, analyse.dayInfo);
        }
        if (name.equals(serviceNames[10])) {
            //ALL-NIGHT VIGIL
        }
        if (name.equals(serviceNames[11])) {
            //MIDNIGHT OFFICE
        }
        if (name.equals(serviceNames[12])) {
            //TYPICA
        }
        if (name.equals(saintNames[2])) {
            //System.out.print("Hellow");
            new Search(analyse.dayInfo);
        }
        if (name.equals(bibleName[0])) {
            //Launch the Bible Reader
            Helpers orient = new Helpers(analyse.dayInfo);
            orient.applyOrientation(new Bible("Gen", "1:1-31", analyse.dayInfo), (ComponentOrientation) analyse.dayInfo.get("Orient"));
        }
        if (name.equals(fileNames[6])) {
            helper.sendHTMLToPrinter(text);
        }
	  /*if (name.equals(EditComm[0])) {
            new EditCommemoration(analyse.dayInfo);
        }
          if (name.equals(EditPrayers[0])) {
            //new EditPrayers(analyse.dayInfo);
        }*/

        if (name.equals( phrases.Phrases.get("OptionMenu"))){
            Options optionsN=new Options(analyse.dayInfo);
            optionsN.addPropertyChangeListener("CalendarChange",this); //nifty way of only listening to what I want to hear!
            optionsN.createDefaultWindow();

        }

        String s = "Action event detected." + newline + "    Event source: " + source.getText() + " (an instance of " + getClassName(source) + ")";
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }


    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().toString().equals("ACTIVATED")) {
            String cmd = e.getDescription();
            String[] parts = cmd.split("#");
            if (parts[0].contains("reading")) {
                try {
                    bible.update(parts[1], parts[2]);
                    bible.show();
                } catch (NullPointerException npe) {
                    bible = new Bible(parts[1], parts[2], analyse.dayInfo);
                    Helpers orient = new Helpers(analyse.dayInfo);
                    orient.applyOrientation(bible, (ComponentOrientation) analyse.dayInfo.get("Orient"));
                }
            } else {
                parts = cmd.split("\\?");
                if (parts[0].contains("goDoSaint")) {

                    String[] parts2 = parts[1].split("=");
                    //System.out.println(parts2[1]);
                    String[] parts3 = parts2[1].split(",");

                    Commemoration1 trial1 = new Commemoration1(parts3[parts3.length - 2], parts3[parts3.length - 1], analyse.dayInfo);
                    if (saintLink == null) {
                        System.out.println(parts3[parts3.length - 1]);

                        saintLink = new DoSaint1(trial1, analyse.dayInfo);
                    } else {
                        saintLink.refresh(trial1);
                    }

                }
            }
        }
    }

   
    

    private void write() {
     
         output = "<body style=\"font-family:" + displayFont + ";font-size:" + displaySize + "pt\">";
     

         JDate2.setCalendar(displayCal);
        String amc = (String) phrases.Phrases.get("AMC");
        String aml = (String) phrases.Phrases.get("AML");
        String cEnd=(String) phrases.Phrases.get("CEnd"); //"."; //Later make it come from the configuration files for a given language.
        String format = "";
        if (amc.equals("1")) {
            //PCalendar checking = new PCalendar(today, PCalendar.julian, analyse.dayInfo);
            JDate2.setCalendar(religiousCal);
            
            format = (String) phrases.Phrases.get("AM");
            if (analyse.dayInfo.get("Ideographic").equals("1"))
                {
                    RuleBasedNumber convertN=new RuleBasedNumber(analyse.dayInfo);
                    
                    format = format.replace("^YYAM", convertN.getFormattedNumber(Long.parseLong(Integer.toString(today.getAM()))));

                }
                else
                {
		format = format.replace("^YYAM", Integer.toString(today.getAM()));
                }
        }
        //System.out.println("AML = " + AML.equals("B"));
        if (aml.equals("B")) {
            output += "<B>" + format + today.toString(analyse.dayInfo) + "</B><BR>";
        } else {
            output += "<B>" + today.toString(analyse.dayInfo) + format + "</B><BR>";
        }
        if (religiousCal!=displayCal)
                {
                    JDate2.setCalendar(displayCal);
                    if (displayCal==0)
                    {
                        
                        output += mainNames[7];
                        
                    }
                    else
                    {
                        output += mainNames[0];
                    }
                    output += colon + today.toString(analyse.dayInfo) + "<BR>";
                    JDate2.setCalendar(religiousCal);
                }
        else
        {
            if (displayCal==0)
            {
                output += mainNames[8]+ "<BR>";
            }
            else
            {
                output += mainNames[9]+ "<BR>";
            }
        }
        //output += MainNames[0] + Colon + (String) today.getGregorianDateS(analyse.dayInfo) + "<BR>";
        String filename = "";
        int lineNumber = 0;
        JDate2.setCalendar(religiousCal);
        int dow = today.getDayOfWeek();
        int doy = today.getDoy();
        int nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(),religiousCal));
        int ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1,religiousCal));
        //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
        int ndayF = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() + 1,religiousCal));
        //System.out.println("Checking the nday: "+nday+" ndayP: "+ndayP+" today’s religious year is: "+today.getYear());
        //Clearing the holders for the icons and names
        iconImages = new Vector();
        iconNames = new Vector();

        // PUT THE RELEVANT DATA IN THE HASH
        analyse.dayInfo.put("dow", dow);	// THE DAY'S DAY OF WEEK
        analyse.dayInfo.put("doy", doy);	// THE DAY'S DOY (see JDate.java for specification)
        //System.out.println(doy);
        analyse.dayInfo.put("nday", nday);	// THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
        analyse.dayInfo.put("ndayP", ndayP);	// THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
        //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
        analyse.dayInfo.put("ndayF", ndayF);	// THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
        //ADDING THE TYPE OF GOSPEL READINGS TO BE FOLLOWED
        analyse.dayInfo.put("GS", GospelSelector.getGValue());
        
        //INTERFACE LANGUAGE
        analyse.dayInfo.put("LS", languageLocation.getLValue());
        analyse.dayInfo.put("Year", today.getYear());
        analyse.dayInfo.put("dRank", 0); //The default rank for a day is 0. Y.S. 2010/02/01 n.s.
        analyse.dayInfo.put("dRankM",0);
        analyse.dayInfo.put("Ideographic", ideographic);
        analyse.dayInfo.put("isLeapYear",today.isLeapYear(today.getYear()) ? 1 : 0);

        readings = new LinkedHashMap<>();
        fastInfo = new Stack();
        //MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/24 n.s. YURI SHARDT
		/*ReadScriptures = new LinkedHashMap<Object, Object>[3];		//CONTAINS A SORTED ARRAY OF ALL THE READINGS
        ReadScriptures[0] = new LinkedHashMap<Object, Object>();		//STORES THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
        ReadScriptures[1] = new LinkedHashMap<Object, Object>();		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
        ReadScriptures[2] = new LinkedHashMap<Object, Object>();		//CONTAINS THE FLOATER READINGS.
         */
        //TESTING THE LANGUAGE PACKS
        String rough = (String) phrases.Phrases.get("1");



        new Sunrise(analyse.dayInfo);
        String[] sunriseSunset = Sunrise.getSunriseSunsetString(today, (String) ConfigurationFiles.Defaults.get("Longitude"), (String) ConfigurationFiles.Defaults.get("Latitude"), (String) ConfigurationFiles.Defaults.get("TimeZone"));
        output += "<BR>" + mainNames[1] + sunriseSunset[0];
        output += "<BR>" + mainNames[2] + sunriseSunset[1];
        output += "<BR><BR>"; //<B>"+MainNames[3]+"</B>"+Colon+ Paschalion.getLunarPhaseString(today) +"<BR><BR>";
        // getting rid of the lunar phase until we program a paschalion ...
        //adding the civil Lunar phase by request of Mitrophan
        Astronomy sky = new Astronomy();

        output += mainNames[3] + sky.lunarphase(today.getJulianDay(), analyse.dayInfo);
        output += "<BR><BR>";

        if (nday >= -70 && nday < 0) {
            filename = triodionFileName;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = pentecostarionFileName;
            JDate2 lastPascha = Paschalion.getPascha(today.getYear() - 1,religiousCal);
            lineNumber = (int) JDate2.difference(today, lastPascha) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = pentecostarionFileName;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
        //System.out.println("++++++++++++++++++++++\n"+filename+"\n+++++++++++++++++++\n");
        //System.out.println("File name in Main: " + analyse.dayInfo.get("LS").toString());
        Day paschalCycle = new Day(filename, analyse.dayInfo);

        // READ THE PENTECOSTARION / TRIODION INFORMATION

        // GET THE MENAION DATA, THESE MAY BE INDEPENDENT OF THE GOSPEL READING IMPLEMENTATION, BUT WILL NOT BE SO IMPLEMENTED
        int m = today.getMonth();
        int d = today.getDay();

        filename = "xml/";
        filename += m < 10 ? "0" + m : "" + m;  // CLEANED UP
        filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
        //filename += ".xml";
        Day solarCycle = new Day(filename, analyse.dayInfo);
        analyse.dayInfo.put("dRank", Math.max(solarCycle.getDayRank(), paschalCycle.getDayRank()));
        analyse.dayInfo.put("dRankM",solarCycle.getDayRank());
        output += paschalCycle.getCommsHyper() + cSep;
        output += solarCycle.getCommsHyper()+cEnd;
        analyse.dayInfo.put("Tone", paschalCycle.getTone());
        analyse.dayInfo.put("SolarPath",filename);
        //System.out.println("The Solar Path is "+filename);
        //analyse.dayInfo.put("SolarCycle",SolarCycle.getCommemorations());
        //analyse.dayInfo.put("PaschalCycle",PaschalCycle);


        output += "<BR><BR>";
        LinkedHashMap<String, Object>[] paschalReadings = paschalCycle.getReadings();
        //System.out.println("Length of Ordinary Readings="+PaschalReadings.length);

        LinkedHashMap<String, Object>[] menaionReadings = solarCycle.getReadings();
        Bible shortForm = new Bible(analyse.dayInfo);
        //System.out.println("First Paschal Reading is :"+PaschalReadings[0].get("Readings"));
        //System.out.println("First Menologion Reading is :"+MenaionReadings[0].get("Readings"));
        LinkedHashMap<String, Object> combinedReadings = new LinkedHashMap<>();
        //for(int j=0;j<7;j++){
        for (LinkedHashMap<String, Object> menaionReading : menaionReadings) {
            LinkedHashMap<String, Object> reading = (LinkedHashMap<String, Object>) menaionReading.get("Readings");
            LinkedHashMap<String, Object> readings = (LinkedHashMap<String, Object>) reading.get("Readings");
            for (Map.Entry<String, Object> entry : readings.entrySet()) {
                String element1 = entry.getKey();
                if (combinedReadings.get(element1) != null) {
                    //Type of Reading already exists combine them
                    LinkedHashMap<Object, Object> temp = (LinkedHashMap<Object, Object>) combinedReadings.get(element1);
                    Vector readings2 = (Vector) temp.get("Readings");
                    Vector rank = (Vector) temp.get("Rank");
                    Vector tag = (Vector) temp.get("Tag");
                    readings2.add(entry.getValue());
                    rank.add(reading.get("Rank"));
                    tag.add(reading.get("Name"));
                    temp.put("Readings", readings2);
                    temp.put("Rank", rank);
                    temp.put("Tag", tag);
                    combinedReadings.put(element1, temp);
                } else {
                    //Reading does not exist
                    Vector readings2 = new Vector();
                    Vector rank = new Vector();
                    Vector tag = new Vector();
                    readings2.add(entry.getValue());
                    rank.add(reading.get("Rank"));
                    tag.add(reading.get("Name"));
                    LinkedHashMap<Object, Object> temp = new LinkedHashMap<>();
                    temp.put("Readings", readings2);
                    temp.put("Rank", rank);
                    temp.put("Tag", tag);
                    combinedReadings.put(element1, temp);
                }
            }
        }
        for (LinkedHashMap<String, Object> paschalReading : paschalReadings) {
            LinkedHashMap<String, Object> reading = (LinkedHashMap<String, Object>) paschalReading.get("Readings");
            LinkedHashMap<String, Object> readings = (LinkedHashMap<String, Object>) reading.get("Readings");
            for (Map.Entry<String, Object> entry : readings.entrySet()) {
                String element1 = entry.getKey();
                if (combinedReadings.get(element1) != null) {
                    //Type of Reading already exists combine them
                    LinkedHashMap<Object, Object> temp = (LinkedHashMap<Object, Object>) combinedReadings.get(element1);
                    Vector readings2 = (Vector) temp.get("Readings");
                    Vector rank = (Vector) temp.get("Rank");
                    Vector tag = (Vector) temp.get("Tag");
                    readings2.add(entry.getValue());
                    rank.add(reading.get("Rank"));
                    tag.add(reading.get("Name"));
                    temp.put("Readings", readings2);
                    temp.put("Rank", rank);
                    temp.put("Tag", tag);
                    combinedReadings.put(element1, temp);
                } else {
                    //Reading does not exist
                    Vector readings2 = new Vector();
                    Vector rank = new Vector();
                    Vector tag = new Vector();
                    readings2.add(entry.getValue());
                    rank.add(reading.get("Rank"));

                    tag.add(reading.get("Name"));
                    LinkedHashMap<Object, Object> temp = new LinkedHashMap<>();
                    temp.put("Readings", readings2);
                    temp.put("Rank", rank);
                    temp.put("Tag", tag);
                    combinedReadings.put(element1, temp);
                }
            }
        }
        //}
        boolean firstTime = true;
        for (Map.Entry<String, Object> entry : combinedReadings.entrySet()) {
            String element1 = entry.getKey();
            //Temperary solution
            LinkedHashMap<Object, Object> temp = (LinkedHashMap<Object, Object>) entry.getValue();
            Vector readings = (Vector) temp.get("Readings");
            Vector rank = (Vector) temp.get("Rank");
            Vector tag = (Vector) temp.get("Tag");
            if (element1.equals("LITURGY")) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    output += rSep;
                }
                //Special case and consider it differently
                Vector epistle = new Vector();

                Vector gospel = new Vector();


                for (Object reading : readings) {
                    LinkedHashMap<Object, Object> liturgy = (LinkedHashMap<Object, Object>) reading;
                    LinkedHashMap<Object, Object> stepE = (LinkedHashMap<Object, Object>) liturgy.get("apostol");
                    LinkedHashMap<Object, Object> stepG = (LinkedHashMap<Object, Object>) liturgy.get("gospel");

                    if (stepE != null) {
                        epistle.add(stepE.get("Reading").toString());
                    } else {
                        epistle.add("");
                    }
                    if (stepG != null) {
                        gospel.add(stepG.get("Reading").toString());
                    } else {
                        gospel.add("");
                    }


                }
                LinkedHashMap<Object, Object> readingsA = new LinkedHashMap<>();

                if (!epistle.get(0).equals("")) {
                    readingsA.put("Readings", epistle);
                    readingsA.put("Rank", rank);
                    readingsA.put("Tag", tag);
                    //System.out.println(Tag);
                    //System.out.println("Hello World");
                    DivineLiturgy1 trial1 = new DivineLiturgy1(analyse.dayInfo);
                    String type1 = (String) phrases.Phrases.get("apostol");
                    output += "<B>" + type1 + "</B>" + colon;
                    //System.out.println(readingsA);
                    output += trial1.Readings(readingsA, "apostol", today);
                    output += rSep;
                }
                if (!gospel.get(0).equals("")) {
                    readingsA.put("Readings", gospel);
                    readingsA.put("Rank", rank);
                    readingsA.put("Tag", tag);
                    String type1 = (String) phrases.Phrases.get("gospel");
                    DivineLiturgy1 trial1 = new DivineLiturgy1(analyse.dayInfo);
                    output += "<B>" + type1 + "</B>" + colon;
                    output += trial1.Readings(readingsA, "gospel", today);
                }

                continue;

            }
            if (element1.equals("MATINS")) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    output += rSep;
                }
                Vector matins2 = new Vector();

                for (Object reading : readings) {
                    LinkedHashMap<Object, Object> matins = (LinkedHashMap<Object, Object>) reading;
                    //System.out.println("In Main1, we have "+matins+"\n matings.get(\"matins\")");
                    LinkedHashMap<Object, Object> stepE = (LinkedHashMap<Object, Object>) matins.get("matins");
                    if (stepE == null) {
                        //stepE = (LinkedHashMap<Object, Object>) matins.get("1");
                        LinkedHashMap<Object, Object> testing3 = (LinkedHashMap<Object, Object>) matins.get("1");
                        //  System.out.println("kl: 0; readings: "+testing3);
                        matins2.add(testing3.get("Reading").toString());

                        for (int kl = 1; kl <= matins.size() - 1; kl++) {
                            testing3 = (LinkedHashMap<Object, Object>) matins.get(Integer.toString(kl + 1));
                            //System.out.println("kl: "+kl+"; readings: "+testing3);
                            matins2.add(testing3.get("Reading").toString());
                        }

                    }
                    //LinkedHashMap<Object, Object> stepE=(LinkedHashMap<Object, Object>)matins.get("matins");
                    //System.out.println("In Main1, we have "+matins2);
                    //System.out.println(stepE);

                    if (stepE != null) {
                        matins2.add(stepE.get("Reading").toString());
                    } else {
                        //matins2.add("");
                    }
                }

                LinkedHashMap<Object, Object> readingsA = new LinkedHashMap<>();

                readingsA.put("Readings", matins2);
                readingsA.put("Rank", rank);
                readingsA.put("Tag", tag);
                Matins trial1 = new Matins(analyse.dayInfo);
                String type1 = (String) phrases.Phrases.get("matins");
                output += "<B>" + type1 + "</B>" + colon;
                //System.out.println("Matins: "+ readingsA);
                output += trial1.Readings(readingsA, today);
                //output+=RSep;

                continue;

            }
            if (firstTime) {
                firstTime = false;
            } else {
                output += rSep;
            }
            String type1 = (String) phrases.Phrases.get(element1.toLowerCase());
            output += "<B>" + type1 + "</B>" + colon;
            for (int i = 0; i < readings.size(); i++) {
                LinkedHashMap<String, Object> Reading = (LinkedHashMap<String, Object>) readings.get(i);
                if (i != 0) {
                    output += rSep;
                }
                //System.out.println(Reading);
                //System.out.println(Tag.get(i));
                boolean first = true;

                for (Object object : readings) {
                    if (first) {
                        first = false;
                    } else {
                        output += rSep;
                    }
                    String element2 = object.toString();
                    LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) Reading.get(element2);
                    String bibleText = "";
                    if (stuff != null && stuff.get("Reading") != null){
                        bibleText = stuff.get("Reading").toString();
                    }
                    output += shortForm.getHyperlinkLoc(bibleText);
                }
                if (readings.size() > 1) {
                    output += tag.get(i).toString();

                }

            }//output += RSep;
        }



        paschalCycle.getIcon();
        Map<Object, Object> iconsM = solarCycle.getIcon();
        //String[] ss = (String[])v.toArray(new String[v.size()]);
        List imageList = (List) iconsM.get("Images");
        List namesList = (List) iconsM.get("Names");
        String[] iconImages = new String[imageList.size()];
        String[] iconNames = new String[namesList.size()];

        iconImages = (String[]) imageList.toArray(new String[0]);
        iconNames = (String[]) namesList.toArray(new String[0]);



        {
            displayIcon.updateImagesFiled(iconImages, iconNames);

        }

        phrases.obtainValues((String) phrases.Phrases.get("Fasts"));
        Fasting getfast = new Fasting(analyse.dayInfo);
        output += "<BR><BR>" + getfast.FastRules() + "<BR><BR>";
        //output+="</FONT>";
        output += "</body>";

        text.setContentType("text/html; charset=UTF-8");
        text.setFont(currentFont);
        text.setText(output);
        text.setCaretPosition(0);



    }

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex + 1);
    }

    public static void main(String[] argz) {
        new Main();
    }
}
