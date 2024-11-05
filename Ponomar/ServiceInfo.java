package Ponomar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/***********************************************************************
 * THIS MODULE READS THE ServiceRules.XML FILE TO DETERMINE THE CORRECT ORDER
 * FOR PRIMES ON A GIVEN DAY
 * 
 * (C) 2009 YURI SHARDT. ALL RIGHTS RESERVED.
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

public class ServiceInfo implements DocHandler {
	private static String fileName = "xml/Commands/ServiceRules.xml";
	private static boolean readPeriod = false;
	private static boolean readLanguage = false;
	private static LinkedHashMap<Object, Object> information;

	// GET THE APPROPRIATE FASTING LINES
	// private String[]
	// ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	// private String[]
	// LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private static LinkedHashMap<Object, Object> service;
	private final String type;
	private final Helpers findLanguage;
	private final StringOp analyse = new StringOp();

	public ServiceInfo(String info, Map<Object, Object> dayInfo) {
		type = info;
		findLanguage = new Helpers(analyse.dayInfo);
		analyse.dayInfo = dayInfo;
		new LanguagePack(dayInfo);
	}

	public ServiceInfo(String info, String file, Map<Object, Object> dayInfo) {
		type = info;
		fileName = file;
		findLanguage = new Helpers(analyse.dayInfo);
		analyse.dayInfo = dayInfo;
		new LanguagePack(dayInfo);
	}

	public Map<Object, Object> ServiceRules() // throws IOException
	{
		service = new LinkedHashMap<>();
		information = new LinkedHashMap<>();
		try {
			BufferedReader frf1 = new BufferedReader(new InputStreamReader(
					Files.newInputStream(
							Paths.get(findLanguage.langFileFind(analyse.dayInfo.get("LS").toString(), fileName))),
					StandardCharsets.UTF_8));
			QDParser.parse(this, frf1);
		} catch (Exception e) {
			e.printStackTrace();
			return null; // THERE WAS AN ERROR IN PROCESSING THE FILES
		}
		// NOW IT IS NECESSARY TO CONVERT THE COMPUTER SPEAK TO HUMAN SPEAK
		return service;
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
		readLanguage = true;
		// }
		if (elem.equals("PERIOD")) {
			readPeriod = true;
		}
		if (elem.equals(type) && readPeriod && readLanguage) {
			// A POTENTIAL ORDER RULE HAS BEEN ENCOUNTERED.
			Enumeration<?> listed = table.keys();
			while (listed.hasMoreElements()) {
				String nextEle = listed.nextElement().toString();

				if (nextEle == null) {
					continue;
				}
				service.put(nextEle, table.get(nextEle).toString());

			}
		}
		// THE FOLLOWING SECTION SHOULD BE REMOVED ONCE THERE IS A PROPER RANKING OF
		// DAYS
		if (elem.equals("COMMAND")) {
			// THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE
			// RESULTS TO BE DETEMINED.
			String name = table.get("Name").toString();
			String value = table.get("Value").toString();
			// IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS
			// VALUES.
			if (information.containsKey(name)) {
				Vector<String> previous = (Vector<String>) information.get(name);
				previous.add(value);
				information.put(name, previous);
			} else {
				Vector<String> vect = new Vector<>();
				vect.add(value);
				information.put(name, vect);
			}

		}
		// TO HERE REMOVE

	}

	public void endElement(String elem) {
		if (elem.equals("LANGUAGE")) {
			readLanguage = false;
		}
		if (elem.equals("PERIOD")) {
			readPeriod = false;
		}
	}

	public void text(String text) {

	}

	private boolean eval(String expression) throws IllegalArgumentException {
		return false;
	}

}