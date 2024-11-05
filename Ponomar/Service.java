package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/***********************************************************************
 * THIS MODULE READ XML FILES THAT CONTAIN A SET OF <CREATE> TAGS THAT SET THE
 * RULES FOR THE CREATION OF A SERVICE
 * 
 * (C) 2008, 2009, 2011, 2023 YURI SHARDT. ALL RIGHTS RESERVED. TO START THE
 * READING OF THE SERVICE FILES, CALL startService(FileName) TO CONTINUE
 * READING, THE SAME SERVICE, BUT WITH POSSIBLY DIFFERENT FILES, CALL
 * readService(FileName) TO END THE SERVICE READER CALL, closeService();
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

public class Service implements DocHandler {
	private static final String CommonPrayersFileName = "xml/Services/CommonPrayers/"; // THE LOCATION OF THE BASIC
																						// SERVICE RULES
	private static final String ServiceFileName = "xml/Services/";
	public static String Service1;
	private static boolean read = false;
	private final LanguagePack Text;// =new LanguagePack();
	private final String[] serviceNames;// =Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	// private String[]
	// LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private String who;
	private String what;
	private String redFirst;
	private String newLine;
	private String times;
	private int header;
	private String commandB;
	private String whoLast = "";
	private String command;
	private int count = -1;
	private final String[] oldText = new String[10];
	private String[] parsedBible;
	private String textTimes;
	private String style;
	private String header1;
	private Helpers findLanguage;
	private final StringOp analyse = new StringOp();
	private final String[] serviceFormat;
	private final String[] serviceCSSFormat;

	// private Font CurrentFont=new
	// Font((String)StringOp.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
	public Service(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Text = new LanguagePack(dayInfo);
		serviceNames = Text.obtainValues((String) Text.Phrases.get("ServiceRead"));
		serviceFormat = Text.obtainValues((String) Text.Phrases.get("ServiceFormat"));
		serviceCSSFormat = Text.obtainValues((String) Text.Phrases.get("ServiceCSSFormat"));
		// ServiceFormat=Text.obtainValues("<B><FONT
		// color=\"red\">$redNow</FONT></B>$rest/, <I><Font color=\"red\">($repeat$textR
		// $textCommand)</Font></I>/, <I><Font color=\"red\">($repeat)</Font></I>/,
		// <I><Font color=\"red\">($textCommand)</Font></I>/,<B><FONT
		// color=\"red\">$textWho</FONT></B>/,<Font
		// color=\"red\"><I><small>$text4</small></I><BR>/,<I><small>$text4</small></I><BR>/,
		// <I><Font color=\"red\"> $text3</Font></I> ");
		// ServiceCSSFormat=Text.obtainValues("rubric {color:red;font-weight:bold}/,p
		// {margin-left:.5in;text-indent:-.5in}/,h1
		// {color:red;font-weight:bold;text-align:center}/,comment
		// {color:red;font-size:50%;font-style:italic}/,command
		// {color:red;font-style:italic}/,h2
		// {color:red;font-size:110%;text-align:center;letter-spacing:1px}");
	}

	public String startService(String fileName) {
		findLanguage = new Helpers(analyse.dayInfo);

		whoLast = "";
		count = -1;
		// Service1="";
		header1 = "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n<head>\n";// <meta
																										// http-equiv=\"Content-Type\"
																										// content=\"text/html;charset=UTF-8\">\n";
		style = "<style type=\"text/css\">\n" + serviceCSSFormat[0] + "\n" + serviceCSSFormat[1] + "\n"
				+ serviceCSSFormat[2] + "\n" + serviceCSSFormat[3] + "\n" + serviceCSSFormat[4] + "\n"
				+ serviceCSSFormat[5] + "\n";

		String displayFont = (String) Text.Phrases.get("FontFaceL");
		String displaySize = (String) Text.Phrases.get("FontSizeL");

		Font value1 = (Font) UIManager.get("Menu.font");
		if (displaySize == null || displaySize.isEmpty()) {
			displaySize = Integer.toString(value1.getSize());
		}
		if (displayFont == null || displayFont.isEmpty()) {
			displayFont = value1.getFontName();
		}
		displaySize = Integer.toString(Math.max(Integer.parseInt(displaySize), value1.getSize())); // If the default
																									// user's font size
																									// is larger than
																									// the required
																									// there is not need
																									// to change it.
		// The specified fonts sizes are the mininum required.
		style += "body {font-family:" + displayFont + ";font-size:" + displaySize + "}\n</head>";
		return readService(fileName);
	}

	public String readService(String fileName) // throws IOException
	{
		Service1 = "";
		// System.out.println("In the body, we have that "+FileName);

		try {
			BufferedReader frf = new BufferedReader(new InputStreamReader(
					Files.newInputStream(
							Paths.get(findLanguage.langFileFind(analyse.dayInfo.get("LS").toString(), fileName))),
					StandardCharsets.UTF_8));
			QDParser.parse(this, frf);
		} catch (Exception e) {
			e.printStackTrace();
			// return ""; //THERE WAS AN ERROR IN PROCESSING THE FILES
		}

		return "<html>\n" + header1 + style + "</style>\n</head>\n<body>" + Service1 + "</body></html>";
	}

	public String closeService() {
		// THIS CLOSES THE SERVICE TEXT APPROPRIATELY
		whoLast = "";
		count = -1;
		return "</p>";
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

			if (!analyse.evalbool(table.get("Cmd").toString())) {
				return;
			}
		}
		// if(elem.equals("LANGUAGE"))
		// {
		read = true;
		// }
		if (elem.equals("GET") && read) {
			// WE NEED TO GET ANOTHER SERVICE OR PART THEREOF.
			read = false;
			String getFile = table.get("File").toString();
			count++;
			oldText[count] = Service1;
			readService(ServiceFileName + getFile + ".xml");
			int nullCheck = 0;
			if (table.get("Null") != null) {
				nullCheck = Integer.parseInt(table.get("Null").toString());
			}

			if (nullCheck == 0 || (nullCheck == 1 && Service1 != null)) {
				// Service1=OldText[count]+"<font face=\"Ponomar Unicode TT\"
				// size=\"5\">"+Service1+"</font>";
				Service1 = oldText[count] + Service1;
			} else {
				Service1 = oldText[count];
			}
			count--;
			read = true;
		}
		if (elem.equals("TITLE") && read) {
			// WE ARE DEALING WITH THE TITLE OF THE SERVICE. IT CAN HAVE 3 PARTS: THE TITLE
			// ITSELF, THE SOURCE FOR
			// SERVICE, AND SOME ADDITIONAL COMMENTS.
			String title = table.get("Header").toString();
			ReadText textGet1 = new ReadText(new LinkedHashMap<>(analyse.dayInfo));
			whoLast = "";
			String text4 = textGet1.readText(ServiceFileName + "Text/" + title + ".xml");
			String ponomar = Text.Phrases.get("0").toString();
			String colon = Text.Phrases.get("Colon").toString();
			if (text4 != null) {
				header1 = header1 + "<title>" + ponomar + colon + text4 + "</title>";

			} else {
				header1 = header1 + serviceNames[0];
			}
			title = table.get("Value").toString();
			text4 = textGet1.readText(ServiceFileName + "Text/" + title + ".xml");
			if (text4 != null) {
				Service1 += "<h1> " + text4 + "</h1>\n";
			} else {
				Service1 += "<h1>" + serviceNames[0] + "</h1>\n";
			}

			if (table.get("Source") != null) {
				String source = table.get("Source").toString();
				text4 = textGet1.readText(ServiceFileName + "Text/" + source + ".xml");
				Service1 += serviceFormat[5].replace("$text4", text4);// "<Font
																		// color=\"red\"><I><small>"+text4+"</small></I><BR>";
			}

			if (table.get("Comment") != null) {
				String comment = table.get("Comment").toString();
				text4 = textGet1.readText(ServiceFileName + "Text/" + comment + ".xml");
				if (text4 != null) {
					Service1 += serviceFormat[6].replace("$text4", text4);// "<I><small>"+text4+"</small></I><BR>";
				}
			}
			Service1 += "</Font>";
		}
		if (elem.equals("SUBTITLE") && read) {
			// WE ARE DEALING WITH THE TITLE OF THE SERVICE. IT CAN HAVE 3 PARTS: THE TITLE
			// ITSELF, THE SOURCE FOR
			// SERVICE, AND SOME ADDITIONAL COMMENTS.
			// String Subtitle=table.get("Header").toString();
			ReadText textGet1 = new ReadText(new LinkedHashMap<>(analyse.dayInfo));
			whoLast = "";

			String subtitle = table.get("Value").toString();
			String text4 = textGet1.readText(ServiceFileName + "Text/" + subtitle + ".xml");
			if (text4 != null) {
				// text4=text4.substring(0,1).toUpperCase()+text4.substring(1);
				Service1 += "<h2> " + text4 + "</h2>\n";
			} else {
				Service1 += "<h2>" + serviceNames[0] + "</h2>\n";
			}

			if (table.get("Comment") != null) {
				String comment = table.get("Comment").toString();
				text4 = textGet1.readText(ServiceFileName + "Text/" + comment + ".xml");
				if (text4 != null) {
					Service1 += serviceFormat[6].replace("$text4", text4);// <I><small>"+text4+"</small></I><BR>";
				}
			}
			Service1 += "</Font>";
		}
		if (elem.equals("TEXT") && read) {
			// HERE IS IT IS ASSUMED THAT THE TEXT AND THE HEADER HAVE BEEN CREATED
			// PROGRAMMATICALLY AND HAVE BEEN ASSIGNED FIXED VALUES
			// APPROPRIATE TO THE GIVEN LANGUAGE. THUS, BOTH what AND HeaderText ARE ASSUMED
			// TO CONTAIN TEXT
			what = table.get("What").toString();
			String headerText = "";
			if (table.get("Header") != null) {
				headerText = table.get("Header").toString();
				header = 1;
			}
			readIncidentals(table);
			Service1 += Implement(header, headerText, what) + "\n";
		}
		if (elem.equals("BIBLE") && read) {
			String what2 = "";
			if (table.get("Verses") != null) {
				// ALLOWING THE BIBLE TO BE READ Y.S. 2008/12/11 n.s.
				// THE FORMAT FOR A BIBLE STATEMENT IS Bible="Book_Chapter:VerseStart-VerseEnd"
				// or ="Book_Chapter:Verse,Chapter:Verse" or ="Book_Chapter"
				// THE BOOK COULD BE OF THE FORM
				// II_NAME_Chapter:VerseStart-VerseEnd,Verse,Verse,Chapter:Verse
				String reading1 = table.get("Verses").toString();
				int k = reading1.lastIndexOf("_");
				Bible reader = new Bible(analyse.dayInfo);
				parsedBible = reader.getText(reading1.substring(0, k), reading1.substring(k + 1), false);
				what2 = parsedBible[0];
			}
			if (table.get("getReading") != null) {
				// THIS ALLOWS THE READING HEADER FOR THE GIVEN SELECTION TO BE OBTAINED, THAT
				// IS, "A reading from the Book of...."
				Bible reader = new Bible(analyse.dayInfo);
				what2 = reader.getIntro(table.get("getReading").toString());
			}
			int stars2 = -1; // THIS VARIABLE CONSIDERS WHAT TO DO WITH ANY POSSIBLE 2 STARS IN THE TEXT "**"
			int starsBible = parsedBible[1].indexOf("**"); // IF THERE ARE NO ** TO BE FOUND IN THE TEXT THEN THERE IS
															// NO NEED TO CONTINUE!
			if ((table.get("2Stars") != null) && starsBible != -1) {
				stars2 = Integer.parseInt(table.get("2Stars").toString());
				if (stars2 == 1) {
					// USE THE 2 STARS DATA AS AN ADDITIONAL HEADER
					parsedBible[2] = parsedBible[1].substring(3) + "<BR>" + parsedBible[2];

				}
				if (stars2 == 2) {
					int k = what2.indexOf("**");
					String[] splitString = parsedBible[1].split("<BR>");
					int a1 = splitString[0].indexOf("\"");
					int a2 = splitString[0].substring(a1 + 1).indexOf("\"");
					String textNew = "";
					if (a1 != -1) {
						textNew = parsedBible[1].substring(a1 + 1, a2 + a1 + 1).replace("...", ""); // 3 separate dots
						textNew = textNew.replace("...", ""); // The 3 dots combined as a single symbol
					}

					what2 = textNew + " " + what2.substring(k + 2);
				}
			}
			// REMOVE THE 2 STARS FROM THE ORIGINAL READING
			int stars = what2.indexOf("**");

			while (stars != -1) {
				// System.out.println(What2);
				if (stars == 0) {
					what2 = what2.substring(3);
				} else {
					what2 = what2.substring(0, stars - 1) + what2.substring(stars + 2);
				}
				stars = what2.indexOf("**");
			}

			if (table.get("Header") != null) {
				if (table.get("Header").toString().equals("1")) {
					header = Integer.parseInt(table.get("Header").toString());
				} else {
					header = 0;
				}
			} else {
				header = 0;
			}
			readIncidentals(table);
			Service1 += Implement(header, parsedBible[2], what2) + "\n";
			read = true;
		}
		if (elem.equals("GETID") && read) {
			String type = "M";
			// System.out.println(table.get("Type"));
			if (table.get("Type") != null) {
				type = table.get("Type").toString();
			}
			String lifeId = table.get("Id").toString();
			if (type.equals("T")) {
				lifeId = "98" + lifeId;
			}
			Commemoration1 data = new Commemoration1("0", lifeId, analyse.dayInfo);
			String info = table.get("What").toString();
			int parsedInfo1 = info.lastIndexOf("/");
			// System.out.println(parsedInfo[0]);
			// The last 2 such elements are important as they contain the general location
			// of what is desired!!!
			// System.out.println(Info);
			// System.out.println(parsedInfo[1]);
			// GENERALISED THE VERSION TO ANYTHING LOCATED INSIDE THE SERVICE TAGS!!!
			Map<Object, Object> royalHours = data.getService(info.substring(0, parsedInfo1),
					info.substring(parsedInfo1 + 1));
			// System.out.println((LinkedHashMap<Object,
			// Object>)data.getService("/ROYALHOURS/VERSE","9P"));
			// System.out.println(RoyalHours);
			String readerRH = "";
			if (royalHours.get("Header") != null) {
				readerRH = royalHours.get("Header").toString();
			}
			if (table.get("Header") != null) {
				header = Integer.parseInt(table.get("Header").toString());
			} else {
				header = 0;
			}
			// System.out.println(RoyalHours.get("Tone").toString());
			if (table.get("ToneA") != null && !table.get("ToneA").equals("0")) {
				table.put("CommandB", "Tone" + royalHours.get("Tone").toString());
				if (royalHours.get("Tone").toString().equals("0")) {
					table.put("CommandB", "Tone8");
				}
			}

			// System.out.println(RoyalHours);
			if (royalHours.get("text") == null) {
				Service1 += "<BR><Font color=\"red\"> " + serviceNames[4] + info + "</Font><BR>";

			} else {
				// System.out.println(table);
				readIncidentals(table);
				// System.out.println(RoyalHours.get("text").toString().substring(1));
				// System.out.println(RoyalHours.get("text").toString());
				String textO = royalHours.get("text").toString();
				if (textO.charAt(0) == '\n') {
					textO = textO.substring(1);
				}
				Service1 += Implement(header, readerRH, textO) + "\n"; // .substring(1)
			}
			read = true;
		}
		if (elem.equals("CREATE") && read) {
			String what2 = "";
			if (table.get("What") != null) {
				what = table.get("What").toString();
			} else {
				what = null;
			}
			if (table.get("Header") != null) {
				if (table.get("Header").toString().equals("1")) {
					header = Integer.parseInt(table.get("Header").toString());

				} else {
					header = 0;
				}
			} else {
				header = 0;
			}
			readIncidentals(table);
			// System.out.println(What);
			ReadText textGet = new ReadText(new LinkedHashMap<>(analyse.dayInfo));
			if (what != null) {
				what2 = textGet.readText(CommonPrayersFileName + what + ".xml");
				if (what2 == null) {
					Service1 += "<BR><Font color=\"red\"/>" + serviceNames[1] + " " + CommonPrayersFileName + what
							+ ".xml</FONT><BR>";
					return;
				}
			}

			Service1 += Implement(header, textGet.readHeader(CommonPrayersFileName + what + ".xml"), what2) + "\n";
			read = true;
		}
		if (elem.equals("TIMES") && read) {
			textTimes = table.get("Value").toString();
		}

	}

	public void endElement(String elem) {
		if (elem.equals("LANGUAGE") || elem.equals("TONE")) {
			read = false;
		}
	}

	public void text(String text) {

	}

	private void readIncidentals(Hashtable table) {
		// THIS READS THE COMMON LABELS FOR CREATE, BIBLE, AND TEXT TAGS.

		who = table.get("Who").toString();
		if (table.get("CommandB") != null) {
			commandB = table.get("CommandB").toString();
		} else {
			commandB = null;
		}
		if (table.get("Command") != null) {
			command = table.get("Command").toString();
		} else {
			command = null;
		}
		if (table.get("RedFirst") != null) {
			redFirst = table.get("RedFirst").toString();
		} else {
			redFirst = null;
		}
		if (table.get("NewLine") != null) {
			newLine = table.get("NewLine").toString();
		} else {
			newLine = null;
		}
		if (table.get("Times") != null) {
			times = table.get("Times").toString();
		} else {
			times = null;
		}

	}

	private String Implement(int header, String text4, String what) {
		String text2 = what;
		ReadText textGet = new ReadText(new LinkedHashMap<>(analyse.dayInfo));

		/*
		 * Original place of this when there the times is used as it was orignally.
		 * if(Command != null) { String
		 * text3=textGet.readText(ServiceFileName+"Command/"+Command+".xml");
		 * 
		 * if(text3 != null) {
		 * text2=text2+" <I><Font color=\"red\"> "+text3+"</Font></I> "; } else {
		 * text2=text2+" <I><Font color=\"red\">" +ServiceNames[2]+"</Font></I>"; } }
		 */
		if (redFirst != null) {
			if (redFirst.equals("1")) {
				// ADDED 2009/10/20 n.s Yuri Shardt
				// TAKING THE FIRST LETTER IS INSUFFICIENT FOR PROPERLY PROCESSING THE RED, AS
				// ANY DIACRITICS MUST ALSO BE IN READ,
				// FOR CHURCH SLAVONIC AND OTHER LANGUAGES, THIS MARKS DO NOT CREATE THEIR OWN
				// SEPARATE LETTERS AS IN A+grave != A`
				// AS ONE CHARACTER, BUT AS TWO CHARACTERS.
				// LISTING ALL DIACRITIC MARKS THAT I WISH TO RECOGNISE: COMMON, SLAVIC, AND
				// GREEK
				String[] markList = { "\u0300", "\u0301", "\u0302", "\u0303", "\u0304", "\u0305", "\u0306", "\u0307",
						"\u0308", "\u0309", "\u030a", "\u030b", "\u030c", "\u030d", "\u030e", "\u030f", "\u0310",
						"\u0311", "\u0312", "\u0313", "\u0314", "\u0315", "\u0316", "\u0317", "\u0318", "\u0319",
						"\u031a", "\u031b", "\u031c", "\u031d", "\u031e", "\u031f", "\u0320", "\u0321", "\u0322",
						"\u0323", "\u0324", "\u0325", "\u0326", "\u0327", "\u0328", "\u0329", "\u032a", "\u032b",
						"\u032c", "\u032d", "\u032e", "\u032f", "\u0330", "\u0331", "\u0332", "\u0333", "\u0334",
						"\u0335", "\u0336", "\u0337", "\u0338", "\u0339", "\u033a", "\u033b", "\u033c", "\u033d",
						"\u033e", "\u033f", "\u0384", "\u0385", "\u037A", // Greek Diacritics I
						"\u0483", "\u0484", "\u0485", "\u0486", "\u0487", "\u0488", "\u0489", // Cyrillic Diacritics I
						"\u1fbd", "\u1fbe", "\u1fbf", "\u1fc0", "\u1fc1", "\u1fcd", "\u1fce", "\u1fcf", "\u1fdd",
						"\u1fde", "\u1fdf", "\u1fed", "\u1fee", "\u1fef", "\u1ffd", "\u1ffe", // Greek Diacritics II
						"\u2de0", "\u2de1", "\u2de2", "\u2de3", "\u2de4", "\u2de5", "\u2de6", "\u2de7", "\u2de8",
						"\u2de9", "\u2dea", "\u2deb", "\u2dec", "\u2ded", "\u2dee", "\u2def", // Cyrillic Combining
																								// Letters I
						"\u2df0", "\u2df1", "\u2df2", "\u2df3", "\u2df4", "\u2df5", "\u2df6", "\u2df7", "\u2df8",
						"\u2df9", "\u2dfa", "\u2dfb", "\u2dfc", "\u2dfd", "\u2dfe", "\u2dff", // Cyrillic Combining
																								// Letters II
						"\ua66f", "\ua670", "\ua671", "\ua672", "\ua673", "\ua67c", "\ua67d" }; // Some more Cyrilic
																								// Diacritics
				// Continuing to add letters to the red part until none from the above list are
				// found.
				StringBuilder redNow = new StringBuilder();
				int countRed = 1;
				redNow = new StringBuilder(text2.substring(0, 1));

				boolean stopRed = false;
				while (!stopRed) {
					stopRed = true;
					for (String s : markList) {
						// System.out.println(redNow +" a");
						// System.out.println(text2.substring(countRed,countRed+1));
						if (text2.substring(countRed, countRed + 1).equals(s)) {
							stopRed = false;
							redNow.append(text2.charAt(countRed));
							countRed = countRed + 1;
							break;
						}
					}
				}
				String newtext = serviceFormat[0].replace("$redNow", redNow.toString());
				newtext = newtext.replace("$rest", text2.substring(countRed));
				text2 = newtext;// "<B><FONT color=\"red\">"+redNow+"</FONT></B>"+text2.substring(countRed);
			}
		}
		String textRepeat = "";
		if (times != null) {
			/*
			 * int Time= Integer.parseInt(Times); String textR=text2; for(int i=1;i <
			 * Time;i++) { text2+="<BR>"+textR; }
			 */
			// THIS IS THE ORIGINAL VERSION OF TIMES. CHANGED TO A BETTER VERSION.
			// 2009/05/18 Y.S.
			textTimes = "";
			analyse.dayInfo.put("Times", Integer.parseInt(times));

			try {
				String fileName = "xml/Commands/Times.xml";
				BufferedReader frf1 = new BufferedReader(new InputStreamReader(
						Files.newInputStream(
								Paths.get(findLanguage.langFileFind(analyse.dayInfo.get("LS").toString(), fileName))),
						StandardCharsets.UTF_8));
				QDParser.parse(this, frf1);
			} catch (Exception e) {
				e.printStackTrace();
				// THERE WAS AN ERROR IN PROCESSING THE FILES
			}
			int splitting = textTimes.indexOf("^#");
			if (splitting != -1) {
				// REPLACE ^# BY THE ACTUAL NUMBER
				String lhs = "";
				String rhs = "";
				if (splitting > 1) {
					lhs = textTimes.substring(0, splitting).trim();
				}
				if (splitting + 2 < textTimes.length()) {
					rhs = textTimes.substring(splitting + 2);
				}
				textRepeat = lhs + times + rhs;

			} else {
				textRepeat = textTimes;
			}

		}
		String textCommand = "";
		if (command != null) {
			String text3 = textGet.readText(ServiceFileName + "Command/" + command + ".xml");

			if (text3 != null) {
				textCommand = text3;
			} else {
				textCommand = serviceNames[2];
			}
			// PROVIDING A PROPER COMBINATION OF THE 2 EVENTS: REPEAT AND COMMAND
			// CORRECTED A FORMATING ERROR IN THE FOLLOWING LINES MISSING A CLOSING ANGLE
			// BRACKET FOR </I>
			// YURI SHARDT 2009/10/31 n.s.

			if (times != null) {
				String textR = textGet.readText(ServiceFileName + "Command/AfterEach.xml");
				if (textR != null) {
					String newText = serviceFormat[1].replace("$repeat", textRepeat);
					newText = newText.replace("$textR", textR);
					newText = newText.replace("$textCommand", textCommand);
					// " <I><Font color=\"red\">("+textRepeat+textR+" "+textCommand+")</Font></I>"
					text2 = text2 + newText;// " <I><Font color=\"red\">("+textRepeat+textR+"
											// "+textCommand+")</Font></I>";

				} else {
					String newText = serviceFormat[1].replace("$repeat", textRepeat);
					newText = newText.replace("$textR", serviceNames[2]);
					newText = newText.replace("$textCommand", textCommand);
					// " <I><Font color=\"red\">("+textRepeat+textR+" "+textCommand+")</Font></I>"
					text2 = text2 + newText;

					// text2=text2+" <I><Font color=\"red\">("+textRepeat+ServiceNames[2]+"
					// "+textCommand+")</Font></I>";

				}
			} else {
				String newText = serviceFormat[3].replace("$textCommand", textCommand);
				// " <I><Font color=\"red\">("+textRepeat+textR+" "+textCommand+")</Font></I>"
				text2 = text2 + newText;

				// text2=text2+" <I><Font color=\"red\">("+textCommand+")</Font></I>";

			}
		} else {
			if (times != null) {
				String newText = serviceFormat[2].replace("$repeat", textRepeat);
				// newText=newText.replace("$textR","");
				// newText=newText.replace("$textCommand","");
				// " <I><Font color=\"red\">("+textRepeat+textR+" "+textCommand+")</Font></I>"
				text2 = text2 + newText;

				// text2=text2+" <I><Font color=\"red\">("+textRepeat+")</Font></I>";
			}
		}

		if (commandB != null) {
			String text3 = textGet.readText(ServiceFileName + "Command/" + commandB + ".xml");

			if (text3 != null) {
				text2 = serviceFormat[7].replace("$text3", text3) + text2;// " <I><Font color=\"red\">
																			// "+text3+"</Font></I> "+text2;
			} else {
				text2 = serviceFormat[7].replace("$text3", serviceNames[2]) + text2;// " <I><Font
																					// color=\"red\">"+ServiceNames[2] +
																					// "</Font></I>"+text2;
			}
		}

		if (header != 0) {
			if (text4 != null && !text4.isEmpty()) {
				// System.out.println(text4 +" lenght: "+text4.length());
				text4 = text4.substring(0, 1).toUpperCase() + text4.substring(1);

				text4 = "<h2>" + text4 + "</h2> ";
			} else {
				text4 = "<BR><B><Font color=\"red\">" + serviceNames[3] + " </Font></B><BR>";
			}

			if (!whoLast.isEmpty()) {
				text4 = "</p>" + text4;
			}
			whoLast = "";

		} else {
			text4 = "";
		}
		if (!who.equals(whoLast) || whoLast.isEmpty()) {
			// THERE HAS BEEN A CHANGE IN WHO IS READING THE SERVICE READER TO PRIEST OR
			// SOMETHING SIMILAR
			// THERE IS A NEED TO AFFIX THE NEW READER.
			// THIS WILL ENTAIL BY DEFAULT A NEW LINE
			if (!who.isEmpty()) {
				String textWho = textGet.readText(CommonPrayersFileName + who + ".xml");
				String newText = serviceFormat[4].replace("$textWho", textWho);
				text2 = "<p>" + newText + text2;
				// text2="<p><B><FONT color=\"red\">"+textWho+"</FONT></B>"+text2;
				if (!whoLast.isEmpty()) {
					text2 = "</p>" + text4 + text2;
				} else {
					text2 = text4 + text2;
				}
			} else {
				if (newLine != null) {
					if (newLine.equals("1")) {
						text2 = "</p><BR>" + text4 + text2;
					} else {
						text2 = "</p>" + text4 + text2;
					}

				} else {
					text2 = "</p>" + text4 + text2;
				}
			}

			whoLast = who;
		} else {
			// THE READER IS THE SAME. CHECK IF A NEW LINE IS DESIRED

			if (newLine != null) {
				if (newLine.equals("1")) {
					text2 = "<BR>" + text4 + text2;
				} else {
					text2 = text4 + text2;
				}

			} else {
				text2 = text4 + text2;
			}

		}
		return text2;
	}

	private boolean eval() throws IllegalArgumentException {
		return false;
	}

}
