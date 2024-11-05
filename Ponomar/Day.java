package Ponomar;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/***********************************************************************
 * THIS MODULE READS XML FILES THAT CONTAIN THAT ARE OF THE <DAY> TYPE AND
 * STORES THE INFORMATION IN A MANNER USUABLE BY OTHER COMPONENTS OF THE
 * PROGRAMME.
 * 
 * (C) 2010, 2012 YURI SHARDT. ALL RIGHTS RESERVED.
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
public class Day implements DocHandler {

	private static boolean read = false;
	private LinkedHashMap<Object, Object> information;
	private LinkedHashMap<Object, Object> royalHours;
	private LinkedHashMap<Object, Object> serviceInfo;
	private LanguagePack Text;// = new LanguagePack();
	private String[] commNames;// = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
	private Helpers helper;
	private List<Commemoration1> orderedCommemorations;
	private int dayRank = -100;
	private int tone = -1;
	private String[] mainNames;// =Text.obtainValues((String)Text.Phrases.get("Main"));
	private String[] toneNumbers;// = Text.obtainValues((String)Text.Phrases.get("Tones"));
	private String forComm;// =(String)Text.Phrases.get("Commemoration2");
	private static final StringOp parameterValues = new StringOp();

	protected Day(String FileName, Map<Object, Object> dayInfo) {
		information = new LinkedHashMap<>();
		new LinkedHashMap<>();
		royalHours = new LinkedHashMap<>();
		parameterValues.dayInfo = dayInfo;
		helper = new Helpers(parameterValues.dayInfo);
		Text = new LanguagePack(parameterValues.dayInfo);
		commNames = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
		mainNames = Text.obtainValues((String) Text.Phrases.get("Main"));
		toneNumbers = Text.obtainValues((String) Text.Phrases.get("Tones"));
		forComm = (String) Text.Phrases.get("Commemoration2");
		orderedCommemorations = new ArrayList<>();
		dayRank = -100;
		information.put("ID", FileName);
		readDay(FileName);

	}

	protected Day(String fileName) {
		/*
		 * ParameterValues.dayInfo=StringOp.dayInfo; Information = new
		 * LinkedHashMap<Object, Object>(); readings = new LinkedHashMap<Object,
		 * Object>(); RoyalHours = new LinkedHashMap<Object, Object>();
		 * Information.put("ID", FileName); helper = new Helpers();
		 * 
		 * counter = 0; OrderedCommemorations = new Vector(); dayRank = -100;
		 * readDay(FileName);
		 */
	}

	protected Day() {
		orderedCommemorations = new ArrayList<>();
		dayRank = -100;

		information = new LinkedHashMap<>();
		new LinkedHashMap<>();
		helper = new Helpers(parameterValues.dayInfo);
		System.out.println("NOTE USING WRONG DAY INPUT FORMAT!!!!");
	}

	public void readDay(String fileName) // throws IOException
	{
		try {
			// BufferedReader frf = new BufferedReader(new InputStreamReader(new
			// FileInputStream(helper.langFileFind(ParameterValues.dayInfo.get("LS").toString(),FileName+".xml")),
			// "UTF8"));
			BufferedReader frf = new BufferedReader(new InputStreamReader(
					Files.newInputStream(Paths
							.get(helper.langFileFind((String) parameterValues.dayInfo.get("LS"), fileName + ".xml"))),
					StandardCharsets.UTF_8));
			// System.out.println("===============\n"+helper.langFileFind(ParameterValues.dayInfo.get("LS").toString(),
			// FileName + ".xml"));
			QDParser.parse(this, frf);
		} catch (Exception e) {
			System.out.println("In file name, "
					+ helper.langFileFind((String) parameterValues.dayInfo.get("LS"), fileName + ".xml")
					+ " an error occurred of type: ");
			e.printStackTrace();
		}
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, Hashtable table) {

		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		// IT WOULD BE VERY RARE IN THIS CASE
		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

			if (!parameterValues.evalbool(table.get("Cmd").toString())) {

				return;
			}
		}
		// if(elem.equals("LANGUAGE"))
		// {
		read = true;
		// System.out.println(table.get("Cmd").toString());
		// return;
		// }
		if (elem.equals("SAINT") && read) {
			// System.out.println(table);
			String sId = "1";
			if (table.get("SId") != null) {
				sId = table.get("SId").toString();
			}

			String Cid = table.get("CId").toString();
			if (table.get("Tone") != null) {
				tone = (int) Math.floor(parameterValues.eval(table.get("Tone").toString()));
			}
			Commemoration1 dayA = new Commemoration1(sId, Cid, parameterValues.dayInfo);

			orderedCommemorations.add(dayA);

		}

	}

	public void endElement(String elem) {
	}

	public void text(String text) {
	}

	public int getDayRank() {
		if (dayRank == -100) {

			for (Commemoration1 orderedCommemoration : orderedCommemorations) {
				dayRank = Math.max(orderedCommemoration.getRank(), dayRank);
			}
		}
		return dayRank;
	}

	public int getTone() {

		if (tone == 0) {
			tone = 8;
		}
		return tone;
	}

	public String getCommsHyper() {
		// Returns a hyperlinked listing of all the commemorations for a given day.
		String cSep = (String) Text.Phrases.get("CommSep");
		StringBuilder output = new StringBuilder();
		for (Commemoration1 CCom : orderedCommemorations) {

			String sId = CCom.getSId();
			String cId = CCom.getCId();
			String nameF = CCom.getName();

			if (!output.toString().isEmpty() && !nameF.isEmpty()) {
				// System.out.println(output);
				output.append(cSep);
			}

			// System.out.println(NameF);
			if (CCom.checkLife() || CCom.checkPropers()) {
				output.append("<A Href='goDoSaint?id=").append(sId).append(",").append(cId).append("'>");
			}
			int rank = CCom.getRank();
			String Rank0Format = (String) Text.Phrases.get("Rank0");
			String Rank1Format = (String) Text.Phrases.get("Rank1");
			String Rank2Format = (String) Text.Phrases.get("Rank2");
			String Rank3Format = (String) Text.Phrases.get("Rank3");
			String Rank4Format = (String) Text.Phrases.get("Rank4");
			String Rank5Format = (String) Text.Phrases.get("Rank5");
			String Rank6Format = (String) Text.Phrases.get("Rank6");

			switch (rank) {
			case 8:
			case 7:
			case 6:
				output.append(Rank6Format.replace("^NF", nameF));
				break;
			case 5:
				output.append(Rank5Format.replace("^NF", nameF));
				break;
			case 4:
				output.append(Rank4Format.replace("^NF", nameF));
				break;

			case 3:
				output.append(Rank3Format.replace("^NF", nameF));
				break;
			case 2:
				output.append(Rank2Format.replace("^NF", nameF));
				break;
			case 1:
				output.append(Rank1Format.replace("^NF", nameF));
				break;
			default:
				output.append(Rank0Format.replace("^NF", nameF));
				// Note: \u00A0 is a nonbreaking space.
			}
			if (CCom.checkLife() || CCom.checkPropers()) {
				output.append("</A>");
			}
			if (tone != -1) {
				int cidn = Integer.parseInt(cId);
				if (cidn >= 9000 && cidn < 9900) {
					if (tone == 0) {
						tone = 8;
					}

					String toneFormat = "";
					toneFormat = mainNames[4];
					toneFormat = cSep + toneFormat.replace("TT", toneNumbers[tone]);
					output.append(toneFormat);

				}
			}
		}
		return output.toString();
	}

	public Map<Object, Object> getIcon() {
		// Ordered List of the Icons
		List<String> iconImages = new ArrayList<>();
		List<String> iconNames = new ArrayList<>();

		for (Commemoration1 CCom : orderedCommemorations) {
			CCom.getSId();
			String cId = CCom.getCId();
			String nameF = CCom.getGrammar("Short");
			String[] iconSearch = Text.obtainValues((String) Text.Phrases.get("IconSearch"));

			File fileNew = new File(
					helper.langFileFind(parameterValues.dayInfo.get("LS").toString(), "/icons/" + cId + "/0.jpg"));
			int countSearch = 0;
			String languageString = parameterValues.dayInfo.get("LS").toString();

			while (!(fileNew.exists()) && countSearch < iconSearch.length) {
				languageString = iconSearch[countSearch];
				fileNew = new File(helper.langFileFind(iconSearch[countSearch], "/icons/" + cId + "/0.jpg"));
				countSearch += 1;
			}

			// The above code will add the Greek Icons and this will allow me to do what I
			// wish to do!!!

			int counterI = 0;

			// System.out.println(fileNew.getAbsolutePath());
			while (fileNew.exists()) {

				iconImages.add(fileNew.toString());
				iconNames.add(nameF);
				counterI += 1;
				fileNew = new File(helper.langFileFind(languageString, "/icons/" + cId + "/" + counterI + ".jpg"));
			}
			File file = new File("Ponomar/images/icons/" + cId + ".jpg");
			if (file.exists()) {
				iconImages.add(file.toString());
				iconNames.add(nameF);
			}
		}
		LinkedHashMap<Object, Object> finalI = new LinkedHashMap<>();
		finalI.put("Images", iconImages);
		finalI.put("Names", iconNames);
		return finalI;
	}

	public Map<Object, Object> getService(String node, String type) {
		// System.out.println(ServiceInfo);
		// System.out.println("\n\n");
		// System.out.println(Node+"/"+Type);
		if (serviceInfo.containsKey(node)) {
			LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) serviceInfo.get(node);

			if (stuff.containsKey(type)) {
				LinkedHashMap<Object, Object> stuff1 = (LinkedHashMap<Object, Object>) stuff.get(type);

				return stuff1;
			} else {
				System.out.println(commNames[0] + node + commNames[1] + type);
				return new LinkedHashMap<>();
			}
		} else {
			System.out.println(commNames[2] + node);
			return new LinkedHashMap<>();
		}
	}

	public LinkedHashMap<String, Object>[] getReadings() {
		LinkedHashMap<String, Object>[] rInformation = new LinkedHashMap[orderedCommemorations.size()];
		List<Integer> count = new ArrayList<>();

		for (int i = 0; i < orderedCommemorations.size(); i++) {
			Commemoration1 currentC = orderedCommemorations.get(i);
			int sizeR = currentC.getReadings().size();
			if (currentC.getReadings() != null || sizeR > 0) {
				// There are readings to consider for today.
				// ReadingsA[i]= new LinkedHashMap<Object, Object>();
				// ReadingsA[i]=CurrentC.getReadings();
				count.add(i);
				int Ranked = currentC.getRank();
				// System.out.println("For "+CurrentC.getCId().toString()+" rank is "+Ranked);
				rInformation[i] = new LinkedHashMap<>();

				// System.out.println(CurrentC.getGrammar("Short")+" "+CurrentC.getReadings());
				// forComm=forComm.replace("^CC", CurrentC.getGrammar("Short"));
				// Temporary grammar processor
				int getN = forComm.indexOf("%getN");
				int forwardbracket = forComm.indexOf("(", getN);
				int backbracket = forComm.indexOf(")");
				String info = forComm.substring(forwardbracket + 1, backbracket);
				String[] splits = info.split(",");
				String forCommF = forComm.substring(0, getN) + currentC.getGrammar(splits[1])
						+ forComm.substring(backbracket + 1);

				rInformation[i].put("Rank", Ranked);
				rInformation[i].put("Name", forCommF);
				rInformation[i].put("Readings", currentC.getReadings());
				// dayRank = Math.max(CurrentC.getRank(), dayRank);
			}
		}
		if (!count.isEmpty()) {
			LinkedHashMap<String, Object>[] readings = new LinkedHashMap[count.size()];
			// int count2=0;
			for (int i = 0; i < count.size(); i++) {
				readings[i] = new LinkedHashMap<>();
				// Readings[i].put("Readings",ReadingsA[Integer.parseInt(count.get(i).toString())]);
				readings[i].put("Readings", rInformation[Integer.parseInt(count.get(i).toString())]);
				// count2+=1;
			}

			return readings;
		} else {
			return new LinkedHashMap[0];
		}

	}

	public Map<Object, Object> getRH(String Node, String Type) {

		if (royalHours.containsKey(Node)) {

			LinkedHashMap<Object, Object> stuff = (LinkedHashMap<Object, Object>) royalHours.get(Node);
			// System.out.println(stuff);
			if (stuff.containsKey(Type)) {
				LinkedHashMap<Object, Object> stuff1 = (LinkedHashMap<Object, Object>) stuff.get(Type);
				return stuff1;
			} else {
				System.out.println(commNames[3]);
				return new LinkedHashMap<>();
			}
		} else {
			System.out.println(commNames[3]);
			return new LinkedHashMap<>();
		}

	}

	public List<Commemoration1> getCommemorations() {
		return orderedCommemorations;
	}

	public static void main(String[] argz) {
		parameterValues.dayInfo = new LinkedHashMap<>();
		parameterValues.dayInfo.put("LS", "cu/ru/");
		parameterValues.dayInfo.put("dow", "5");
		// Commemoration Paramony = new Commemoration("P_3174"); //Paramony of Christmas
		System.out.println("THIS IS RUNNING ON DEBUG MODE, USING THE FILE FOR the Paramony of Christmas");
		// LinkedHashMap<Object, Object> stuff=Paramony.getRH("Idiomel","11");
		// System.out.println(Paramony.getService("/ROYALHOURS/IDIOMEL","13"));
		// System.out.println(Paramony .ServiceInfo());
		// System.out.println(Paramony.getRH("IDIOMEL","11"));
		// System.out.println(Paramony);
		Day paramony = new Day("xml/pentecostarion/06"); // Forefeast of Christmas

		System.out.println(paramony.getCommsHyper());
		System.out.println(paramony.getDayRank());
		LinkedHashMap<String, Object>[] testing = paramony.getReadings();
		System.out.println(testing[0].get("Readings"));
		System.out.println(testing[1].get("Readings"));
		// System.out.println(Testing[0].get("Information"));
	}
}
