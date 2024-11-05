package Ponomar;

import java.io.FileReader;
import java.util.*;

/***************************************************************
DivineLiturgy1.java :: MODULE THAT TAKES THE GIVEN DIVIN LITURGY (GOSPEL AND EPISTLE) READINGS FOR THE DAY,
THAT IS, PENTECOSTARION, MENELOGION, AND FLOATERS AND RETURNS
THE APPROPRIATE SET OF READINGS FOR THE DAY AND THEIR ORDER
ASSUMING THAT THE "LUCAN JUMP" IS BEING USED. THIS FUNCITON MUST BE
PREFORMED SEPARATELY FOR EACH TYPE OF READING: EPISTLE AND GOSPEL.
THIS PROGRAMME HAS BEEN GENERALISED TO ALLOW ANY SET OF RULES TO BE USED.

Further work will convert this into the programme that will allow the creation of the text for the Divine Liturgy.

DivineLiturgy1.java is part of the Ponomar project.
Copyright 2008, 2012, 2015 Yuri Shardt
version 1.0: May 2008
 * version 2.0: June 2012, further updates and corrections to the new format.
 * version 2.5: 2015, updates and corrections


PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 **************************************************************/
public class DivineLiturgy1 implements DocHandler {

    //private static final String generalFileName="Ponomar/xml/";
    private static final String triodionFileName = "xml/triodion/";   // TRIODION FILE
    private static final String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
    private static LinkedHashMap<Object, Object> information;		//CONTAINS COMMANDS ABOUT HOW TO CARRY OUT THE ORDERING OF THE READINGS
    private static LanguagePack phrases;// = new LanguagePack();
    private static String[] transferredDays;// = Phrases.obtainValues((String) Phrases.Phrases.get("DayReading"));
    private static String[] error;// = Phrases.obtainValues((String) Phrases.Phrases.get("Errors"));
    private static Helpers findLanguage;// = new Helpers();
    private static final Vector dailyV = new Vector();
    private static final Vector dailyR = new Vector();
    private static final Vector dailyT = new Vector();
    private static final Vector menaion2V = new Vector();
    private static final Vector menaion2R = new Vector();
    private static final Vector menaion2T = new Vector();
    private static final Vector menaionV = new Vector();
    private static final Vector menaionR = new Vector();
    private static final Vector menaionT = new Vector();
    private static final Vector suppressedV = new Vector();
    private static final Vector suppressedR = new Vector();
    private static final Vector suppressedT = new Vector();
    private static LinkedHashMap<Object, Object> tomorrowRead = new LinkedHashMap<>();
    private static LinkedHashMap<Object, Object> yesterdayRead = new LinkedHashMap<>();
    private static final StringOp Information3  = new StringOp();
    private static final StringOp analyse=new StringOp();

    public DivineLiturgy1(Map<Object, Object> dayInfo) {
        analyse.dayInfo=dayInfo;
          phrases = new LanguagePack(dayInfo);
    transferredDays = phrases.obtainValues((String) phrases.Phrases.get("DayReading"));
     error = phrases.obtainValues((String) phrases.Phrases.get("Errors"));
     findLanguage=new Helpers(analyse.dayInfo);
    }

//THESE ARE THE SAME FUNCTION AS IN MAIN, BUT TRIMMED FOR THE CURRENT NEEDS
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

        if (elem.equals("COMMAND")) {
            //THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
            String name = (String) table.get("Name");
            String value = (String) table.get("Value");
            //IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
            if (information.containsKey(name)) {
                Vector previous = (Vector) information.get(name);
                previous.add(value);
                information.put(name, previous);
            } else {
                Vector vect = new Vector();
                vect.add(value);
                information.put(name, vect);
            }

        }
        //ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
    }

    public void endElement(String elem) {
    }

    public void text(String text) {
    }

    public String Readings(LinkedHashMap<Object, Object> readingsIn, String readingType, JDate2 today) {
        /********************************************************
        SINCE I HAVE CORRECTED THE SCRIPTURE READINGS IN THE MAIN FILE, I CAN NOW PRECEDE WITH A BETTER VERSION OF THIS PROGRAMME!
         ********************************************************/
        //PROCESS THE READINGS INTO THE DESIRED FORMS:
        classifyReadings orderedReadings = new classifyReadings(readingsIn);
       /* Information3.dayInfo.put("doy","12");
        Information3.dayInfo.put("dow","1");
        Information3.dayInfo.put("nday","2");
        System.out.println("Testing the new StringOp formulation is " + Information3.evalbool("doy == 12"));*/

        information = new LinkedHashMap<>();
        int doy = Integer.parseInt(analyse.dayInfo.get("doy").toString());
        int dow = Integer.parseInt(analyse.dayInfo.get("dow").toString());
        int nday = Integer.parseInt(analyse.dayInfo.get("nday").toString());


        //DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
        try {
            FileReader frf = new FileReader(findLanguage.langFileFind(analyse.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
            DivineLiturgy1 a1 = new DivineLiturgy1(analyse.dayInfo);
            QDParser.parse(a1, frf);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*NOTE: SINCE THE 33rd SUNDAY AFTER PENTECOST DOES NOT HAVE ANY ASSOCIATED READINGS IN THE PENTECOSTARION,
        THIS CAN LEAD TO DIFFICULTIES IN DOING CERTAIN THINGS! THUS, THE FOLLOWING CORRECTIONS.
         */
        if ((doy >= 4 && doy <= 10) && (dow == 0) && readingType.equals("apostol")) {
            //IF THERE IS AN APOSTOL ON THIS DAY, THEN THERE MAY BE ISSUES WITH ITS PRESENCE.
            //NOTE: NOTHING IS CURRENTLY DONE ABOUT THIS!           
        }

        //CHECK WHETHER OR NOT IT IS DESIRED TO TRANSFER THE SKIPPED SEQUENTIAL READINGS
        Vector transfer = (Vector) information.get("Transfer");
        boolean transfer1 = analyse.evalbool((String) transfer.get(0));
        classifyReadings tomorrows = new classifyReadings();
        classifyReadings yesterdays = new classifyReadings();
        if (transfer1) {
            /*NOW CONSIDER ANY SUPPRESSED READINGS:
            //THE FOLLOWING SHOULD BE NOTED:
            1. READINGS ARE NEVER TRANSFERRED TO A SUNDAY
            2. TUESDAY CAN HAVE 2 SETS OF READINGS TRANSFERRED TO IT: MONDAY'S AND WEDNESDAY'S
             */
            //NOTE 2: NO READINGS ARE TRANSFERRED DURING LENT, THAT IS, -48 <= nday <=0.
            Vector transferRule = (Vector) information.get("TransferRulesB");
            boolean transfer2 =analyse.evalbool((String) transferRule.get(0));
            if (transfer2) //St. NICHOLAS'S DAY HAS A SPECIAL SET OF RULES
            {
                //IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
                //THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!


                //Rewriting the transferring rules based on the changes in the file format for the ranking and the like (Y.S. 20120610 n.s.)

                
                StringOp Transfers=new StringOp();
                Transfers.dayInfo = new LinkedHashMap<>(analyse.dayInfo);//findLanguage.deepCopy((Hashtable)StringOp.dayInfo.clone());
                Information3.dayInfo= new LinkedHashMap<>(analyse.dayInfo);
                today.addDays(1);
                // PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
                //System.out.println("Case I: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("dRank").toString()+" In Information3, doy = "+Information3.dayInfo.get("dRank").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("dRank"));
                Information3.dayInfo.put("dow", today.getDayOfWeek());
                Information3.dayInfo.put("doy", today.getDoy());
                Information3.dayInfo.put("dRank","0");
                //System.out.println("Case II: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("doy").toString()+" In Information3, doy = "+Information3.dayInfo.get("doy").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("doy"));
                nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(), JDate2.getCalendar2()));
                int ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1, JDate2.getCalendar2()));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                int ndayF = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() + 1, JDate2.getCalendar2()));
                Information3.dayInfo.put("nday", nday);
                Information3.dayInfo.put("ndayP", ndayP);
                Information3.dayInfo.put("ndayF", ndayF);

                getReadings(today, readingType);
                tomorrowRead = getReadings(today, readingType);
                tomorrows = new classifyReadings(tomorrowRead, new LinkedHashMap<>(Information3.dayInfo));
                //System.out.println("Case III: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("dRank").toString()+" In Information3, doy = "+Information3.dayInfo.get("dRank").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("dRank"));


                today.subtractDays(1);
                /*analyse.dayInfo.put("dow", today.getDayOfWeek());
                analyse.dayInfo.put("doy", today.getDoy());
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                analyse.dayInfo.put("nday", nday);
                analyse.dayInfo.put("ndayP", ndayP);
                analyse.dayInfo.put("ndayF", ndayF);
                analyse.dayInfo.put("dRank",dRankOld);*/
            }
            //NOW WE NEED TO CHECK YESTERDAY'S READINGS, BUT THIS WILL ONLY OCCUR ON A TUESDAY OR DEC. 6th
            transferRule = (Vector) information.get("TransferRulesF");
            transfer2 = analyse.evalbool((String) transferRule.get(0));

            if (transfer2) //IF IT IS A SATURDAY, THEN THE READINGS WILL BE SKIPPED, ???
            {
                //IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
                //THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!


                StringOp Transfers=new StringOp();
                Transfers.dayInfo.putAll(new LinkedHashMap<>(analyse.dayInfo));
                Information3.dayInfo= new LinkedHashMap<>(analyse.dayInfo);
                today.subtractDays(1);


                // PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
                Information3.dayInfo.put("dow", today.getDayOfWeek());
                Information3.dayInfo.put("doy", today.getDoy());
                Information3.dayInfo.put("dRank","0");
                nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(), JDate2.getCalendar2()));
                int ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1, JDate2.getCalendar2()));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                int ndayF = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() + 1, JDate2.getCalendar2()));
                Information3.dayInfo.put("nday", nday);
                Information3.dayInfo.put("ndayP", ndayP);
                Information3.dayInfo.put("ndayF", ndayF);

                yesterdayRead = getReadings(today, readingType);
                yesterdays = new classifyReadings(yesterdayRead, new LinkedHashMap<>(Information3.dayInfo));



                today.addDays(1);
                /*analyse.dayInfo.put("dow", today.getDayOfWeek());
                analyse.dayInfo.put("doy", today.getDoy());
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                analyse.dayInfo.put("nday", nday);
                analyse.dayInfo.put("ndayP", ndayP);
                analyse.dayInfo.put("ndayF", ndayF);
                analyse.dayInfo.put("dRank",dRankOld);*/
            }
        }


        Vector dailyVf = new Vector();
        Vector dailyRf = new Vector();
        Vector dailyTf = new Vector();        
        for (int i=0;i<yesterdays.suppressedV.size();i++){
            dailyVf.add(yesterdays.suppressedV.get(i));
            dailyRf.add(yesterdays.suppressedR.get(i));
            dailyTf.add((dow - 1 + 7) % 7);

        }
        for (int i=0;i<orderedReadings.dailyV.size();i++){
            dailyVf.add(orderedReadings.dailyV.get(i));
            dailyRf.add(orderedReadings.dailyR.get(i));
            dailyTf.add(dow);

        }
        for (int i=0;i<tomorrows.suppressedV.size();i++){
            dailyVf.add(tomorrows.suppressedV.get(i));
            dailyRf.add(tomorrows.suppressedR.get(i));
            dailyTf.add((dow + 1) % 7);

        }        
        

        
        //System.out.println("Testing some math: " + (0 - 1 + 7) % 7);
        Vector menaionV = new Vector();
        Vector menaionR = new Vector();
        Vector menaionT = new Vector();

        for (int i=0;i<orderedReadings.menaionV.size();i++){
            menaionV.add(orderedReadings.menaionV.get(i));
            menaionR.add(orderedReadings.menaionR.get(i));
            menaionT.add(orderedReadings.menaionT.get(i));

        }

        


        //THE GENERAL FORMAT IS: FLOATERS, PENTECOSTARION, MENALOGION, EXCEPT ON SATURDAYS WHERE IT IS FLOATERS, MENALOGION, PENTECOSTARION

        if (dow == 6) {
            //ON SATURDAYS, THE READINGS FROM THE MENALOGION TAKE PRECEDENCE.
            for (int i=0;i<dailyVf.size();i++){
            menaionV.add(dailyVf.get(i));
            menaionR.add(dailyRf.get(i));
            menaionT.add(dailyTf.get(i));
            
            return format(menaionV, menaionR, menaionT);
            }
        }
        for (int i=0;i<menaionV.size();i++){
            dailyVf.add(menaionV.get(i));
            dailyRf.add(menaionR.get(i));
            dailyTf.add(menaionT.get(i));

        }
         //System.out.println("---Testing Main Programme-----");

        //System.out.println(menaionV);
        //System.out.println(dailyVf);
        
        return format(dailyVf, dailyRf, dailyTf);
    }

    private static LinkedHashMap<Object, Object> getReadings(JDate2 today, String readingType) {
        String filename = "";
        int lineNumber = 0;

        int nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(), JDate2.getCalendar2()));

        //I COPIED THIS FROM THE Main.java FILE BY ALEKS WITH MY MODIFICATIONS (Y.S.)
        //FROM HERE UNTIL
        if (nday >= -70 && nday < 0) {
            filename = triodionFileName;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = pentecostarionFileName;
            JDate2 lastPascha = Paschalion.getPascha(today.getYear() - 1, JDate2.getCalendar2());
            lineNumber = (int) JDate2.difference(today, lastPascha) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = pentecostarionFileName;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber + "" : "0" + lineNumber; // CLEANED UP
        // READ THE PENTECOSTARION / TRIODION INFORMATION
        Day checkingP = new Day(filename,Information3.dayInfo);


        //ADDED 2008/05/19 n.s. Y.S.
        //COPYING SOME READINGS FILES



        // GET THE MENAION DATA
        int m = today.getMonth();
        int d = today.getDay();

        filename = "";
        filename += m < 10 ? "xml/0" + m : "xml/" + m;  // CLEANED UP
        filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
        filename += "";
        
        Day checkingM = new Day(filename,Information3.dayInfo);
        Information3.dayInfo.put("dRank",Math.max(checkingP.getDayRank(), checkingM.getDayRank()));

        LinkedHashMap<String, Object>[] paschalReadings = checkingP.getReadings();
        LinkedHashMap<String, Object>[] menaionReadings = checkingM.getReadings();
        LinkedHashMap<Object, Object> combinedReadings = new LinkedHashMap<>();


        for (LinkedHashMap<String, Object> menaionReading : menaionReadings) {
            LinkedHashMap<String, Object> Reading = (LinkedHashMap<String, Object>) menaionReading.get("Readings");
            LinkedHashMap<String, Object> Readings = (LinkedHashMap<String, Object>) Reading.get("Readings");
            for (Map.Entry<String, Object> entry : Readings.entrySet()) {
                String element1 = entry.getKey();
                if (combinedReadings.get(element1) != null) {
                    //Type of Reading already exists combine them
                    LinkedHashMap<String, Object> temp = (LinkedHashMap<String, Object>) combinedReadings.get(element1);
                    Vector readings2 = (Vector) temp.get("Readings");
                    Vector rank = (Vector) temp.get("Rank");
                    Vector tag = (Vector) temp.get("Tag");
                    readings2.add(entry.getValue());
                    rank.add(Reading.get("Rank"));
                    tag.add(Reading.get("Name"));
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
                    rank.add(Reading.get("Rank"));
                    tag.add(Reading.get("Name"));
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


        LinkedHashMap<Object, Object> temp = (LinkedHashMap<Object, Object>) combinedReadings.get("LITURGY");
        //System.out.println("temp values (423)" + temp);
        Vector readings = (Vector) temp.get("Readings");
        Vector rank = (Vector) temp.get("Rank");
        Vector tag = (Vector) temp.get("Tag");
        //Special case and consider it differently


        Vector type = new Vector();


        for (Object reading : readings) {
            LinkedHashMap<Object, Object> liturgy = (LinkedHashMap<Object, Object>) reading;
            LinkedHashMap<Object, Object> stepE = (LinkedHashMap<Object, Object>) liturgy.get(readingType);
            if (stepE != null) {

                type.add(stepE.get("Reading").toString());
            } else {
                //type.add("");
            }


        }


        //output += RSep;
        LinkedHashMap<Object, Object> Final2 = new LinkedHashMap<>();
        Final2.put("Readings", type);
        Final2.put("Rank", rank);
        Final2.put("Tag", tag);        



        return Final2;
    }

    protected String Display(String a, String b, String c) {
        //THIS FUNCTION TAKES THE POSSIBLE 3 READINGS AND COMBINES THEM AS APPROPRIATE, SO THAT NO SPACES OR OTHER UNDESIRED STUFF IS DISPLAYED!
        String output = "";
        if (!a.isEmpty()) {
            output += a;
        }
        if (!b.isEmpty()) {
            if (!output.isEmpty()) {
                output += analyse.dayInfo.get("ReadSep") + " ";
            }
            output += b;
        }
        if (!c.isEmpty()) {
            if (!output.isEmpty()) {
                output += analyse.dayInfo.get("ReadSep") + " ";
            }
            output += c;

        }

        //TECHNICALLY, IF THERE ARE 3 OR MORE READINGS, THEN SOME SHOULD BE TAKEN "FROM THE BEGINNING" (nod zachalo).
        return output;
    }

    public String format(Vector vectV, Vector vectR, Vector vectT) {
        StringBuilder output = new StringBuilder();
        //AT THIS POINT, THE PENTECOSTARION READINGS WILL BE FORMATED SO THAT THEY ARE SEQUENTIAL BY THE WEEK,
        //ESPECIALLY IF THERE ARE ANY RETRACTIONS OR THE LIKE.
        /*try {
        //The Readings should be sorted based on the order of values in Type, but only if it is numeric, that is, it is the Pentecostarion data
        if (vectV.size() > 1) {
        //THIS IS NOT THE MOST EFFECTIVE TECHNIQUE, BUT THEN THERE WILL ONLY EVER TRULY BE 2 READINGS TO MOVE!
        int secondDay = Integer.parseInt((String) vectT.get(1));
        int firstDay = Integer.parseInt((String) vectT.get(0));
        //SOLVES AN ORDERING ISSUE WITH SUNDAY BEING ORIGINALLY PLACED BEFORE SATURDAY,
        //WHEN IT SHOULD HAVE BEEN AFTER
        //ADDED 2008/08/04 n.s. by Y.S.
        if (secondDay == 0) {
        secondDay = 7;
        }
        if (firstDay == 0) {
        firstDay = 7;
        }

        if (Integer.parseInt((String) vectT.get(0)) > secondDay) {

        Object a = vectV.set(0, vectV.get(1));
        vectV.set(1, a);
        a = Type.set(0, Type.get(1));
        Type.set(1, a);
        }

        }
        } catch (Exception e) {
        }*/
        Bible shortForm = new Bible(analyse.dayInfo);
        try {
            vectV.elements();
            for (int k = 0; k < vectV.size(); k++) {
                String reading = (String) vectV.get(k);
                output.append(shortForm.getHyperlinkLoc(reading));

                if ((Integer) vectR.get(k) == -2 ) {
                    if (vectV.size()>1){
                    output.append(" (").append(Week(vectT.get(k).toString())).append(")");
                    }
                } else {
                    output.append(vectT.get(k));
                }

                if (k < vectV.size() - 1) {
                    output.append(analyse.dayInfo.get("ReadSep"));		//IF THERE ARE MORE READINGS OF THE SAME TYPE APPEND A SEMICOLON!
                }
            }
        } catch (Exception a) {
            
            System.out.println(a);
            StackTraceElement[] trial=a.getStackTrace();
            System.out.println(trial[0].toString());

        }
        return output.toString();
    }

    private String Week(String dow) {
        //CONVERTS THE DOW STRING INTO A NAME. THIS SHOULD BE IN THE ACCUSATIVE CASE
        try {
            return transferredDays[Integer.parseInt(dow)];
        } catch (Exception a) {
            return dow;		//A DAY OF THE WEEK WAS NOT SENT
        }
    }

    public static void main(String[] argz) {
    }

    class classifyReadings implements DocHandler {

        private LinkedHashMap<Object, Object> information2;		//CONTAINS COMMANDS ABOUT HOW TO CARRY OUT THE ORDERING OF THE READINGS
        public Vector dailyV = new Vector();
        public Vector dailyR = new Vector();
        public Vector dailyT = new Vector();
        public final Vector menaionV = new Vector();
        public final Vector menaionR = new Vector();
        public final Vector menaionT = new Vector();
        public final Vector suppressedV = new Vector();
        public final Vector suppressedR = new Vector();
        public final Vector suppressedT = new Vector();
        private final StringOp parameterValues=new StringOp();

        public classifyReadings() {
        }

        public classifyReadings(Map<Object, Object> readingsInA) {
            new StringOp();
            parameterValues.dayInfo=analyse.dayInfo;
            //System.out.println("In ParameterValues, we have LS = " + ParameterValues.dayInfo.get("LS")+" while in analyse, we have "+analyse.dayInfo.get("LS"));
            classify(readingsInA);
        }

       public classifyReadings(Map<Object, Object> readingsInA, Map<Object, Object> dayInfo) {
           parameterValues.dayInfo=dayInfo;
            classify(readingsInA);

        }
        private void classify(Map<Object, Object> readingsIn)
        {
            //Initialise Information.
            information2= new LinkedHashMap<>();
            findLanguage=new Helpers(parameterValues.dayInfo);
            //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
            try {
                FileReader frf = new FileReader(findLanguage.langFileFind(parameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
                //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
                //DivineLiturgy a1 = new classifyReadin();
                QDParser.parse(this, frf);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Vector paschalV = (Vector) readingsIn.get("Readings");
            Vector paschalR = (Vector) readingsIn.get("Rank");
            Vector paschalT = (Vector) readingsIn.get("Tag");

            dailyV = new Vector();
            dailyR = new Vector();
            dailyT = new Vector();

            
            if (paschalV == null){
                return;
            }
           

            for (int k = 0; k < paschalV.size(); k++) {
                
                if ((Integer) paschalR.get(k) == -2) {
                    //THIS IS A DAILY READING THAT CAN BE SKIPPED, EXCEPT MAYBE ON SUNDAYS.
                    dailyV.add(paschalV.get(k));
                    dailyR.add(paschalR.get(k));
                    dailyT.add(paschalT.get(k));
                } else {
                    menaionV.add(paschalV.get(k));
                    menaionR.add(paschalR.get(k));
                    menaionT.add(paschalT.get(k));
                }

            }           
            
            Suppress();
            //LeapReadings();            


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

                if (!parameterValues.evalbool(table.get("Cmd").toString())) {
                    return;
                }
            }

            if (elem.equals("COMMAND")) {
                //THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
                String name = (String) table.get("Name");
                String value = (String) table.get("Value");
                //IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
                //System.out.println("==============================\nTesting Information\n++++++++++++++++++++");
                if (information2.containsKey(name)) {
                    Vector previous = (Vector) information2.get(name);
                    previous.add(value);
                    information2.put(name, previous);
                } else {
                    Vector vect = new Vector();
                    vect.add(value);
                    information2.put(name, vect);
                }

            }
            //ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
        }

        public void endElement(String elem) {
        }

        public void text(String text) {
        }

        private void Suppress() {
            LeapReadings();		//THIS ALLOWS APPROPRIATE SKIPPING OF READINGS OVER THE NATIVITY SEASON!

            /******************************************************
            FOR ALL HOLIDAYS OF THE FIRST CLASS, THAT IS, OF THE LORD, THEN ONLY THE MENALOGION
            READINGS ARE TAKEN. THE PENTECOSTARION READINGS CAN BE TRANSFERRED.
            THE FOLLOWING FESTIVALS ARE CONSIDERED:
            1. EXALTATION: SEPTEMBER 14th: DOY == 256
            2. CHRISTMAS: DECEMBER 25th: DOY == 358
            3. THEOPHANY: JANUARY 6th: DOY == 5
            4. TRANSFIGURATION: AUGUST 6th: DOY == 217
             ******************************************************/
   /*         if (doy == 256 || doy == 358 || doy == 5 || doy == 217) {
                for (int k = 0; k < dailyV.size(); k++) {
                    suppressedV.add(dailyV.get(k));
                    suppressedR.add(dailyR.get(k));
                    suppressedT.add(dailyT.get(k));
                }
                dailyV.clear();
                dailyR.clear();
                dailyT.clear();                
                return;				//There is no need for any other readings to be considered!
            }
*/
            /********************************
            FOR ALL HOLIDAY OF THE SECOND CLASS, THAT IS, OF THE MOTHER OF GOD, THEN ONLY THE MENALOGION
            READINGS ARE TAKEN, IF IT FALLS DURING MONDAY TO SATURDAY, OTHERWISE THE READINGS
            ARE COMBINED WITH THE SEQUENTIAL READINGS.
            THE FOLLOWING FESTIVALS ARE CONSIDERED:
            1. ANNUNCIATION: MARCH 25th: DOY == 83 (ALTHOUGH THE RULES ARE ACTUALLY MORE INVOLVED, HENCE SKIPPED)
            2. PRESENTATION OF THE LORD: FEBRUARY 2nd: DOY == 32, BUT NOT IF NDAY == -48 (FIRST DAY OF LENT).
            3. NATIVITY OF THE MOTHER OF GOD: SEPTEMBER 8th: DOY == 250
            4. DORMITION OF THE MOTHER OF GOD: AUGUST 15th: DOY == 226
            5. ENTRY OF THE MOTHER OF GOD INTO THE TEMPLE: NOVEMBER 21st: 324
             **************************************************************************************/
 /*           if ((doy == 32 && nday != -48) || doy == 250 || doy == 226 || doy == 324) {
                if (dow != 0) {
                    for (int k = 0; k < dailyV.size(); k++) {
                        suppressedV.add(dailyV.get(k));
                        suppressedR.add(dailyR.get(k));
                        suppressedT.add(dailyT.get(k));
                    }
                    dailyV.clear();
                    dailyR.clear();
                    dailyT.clear();
                    return;					//There is no need for any other readings to be considered!
                } else {
                    //ALL THE READINGS ARE COMBINED IN SOME FASHION, HOWEVER SOME COULD POTENTIAL BE REDUCED DUE TO REPEATS

                    return;					//CHECK WHETHER IS TRUE
                }
            }


            if (dow != 0) {*/
                Vector vect = (Vector) information2.get("Class3Transfers");
                if (vect != null) {
                    for (Enumeration e2 = vect.elements(); e2.hasMoreElements();) {
                        String Command = (String) e2.nextElement();
                        if (parameterValues.evalbool(Command)) {
                            //THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SUPPRESSED/TRANSFERRED
                            for (int k = 0; k < dailyV.size(); k++) {
                                suppressedV.add(dailyV.get(k));
                                suppressedR.add(dailyR.get(k));
                                suppressedT.add(dailyT.get(k));
                            }
                            dailyV.clear();
                            dailyR.clear();
                            dailyT.clear();
                            return;
                        }
                    }
                }
            //There is no need for any other readings to be considered!
            //}

            //AT THIS POINT, THE PENTECOSTARION READINGS MAY BE REDUCED DUE TO REPEATS

            //return;
        }

        protected void LeapReadings() {
            //USING THE NEWER VERSION OF STORED VALUES
            //EACH OF THE STORED COMMANDS ARE EVALUATED IF ANY ARE TRUE THEN THE READINGS ARE SKIPPED IF THERE ARE ANY FURTHER READINGS ON THAT DAY.
            int available = menaionV.size();

            if (available > 0) {
                Vector vect = (Vector) information2.get("Suppress");
                if (vect != null) {
                    for (Enumeration e2 = vect.elements(); e2.hasMoreElements();) {
                        String command = (String) e2.nextElement();
                        if (parameterValues.evalbool(command)) {
                            //THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SKIPPED
                            dailyV.clear();
                            dailyR.clear();
                            dailyT.clear();
                            suppressedV.clear();
                            suppressedR.clear();
                            suppressedT.clear();
                            return;
                        }

                    }
                }
            }

        }
    }
}
