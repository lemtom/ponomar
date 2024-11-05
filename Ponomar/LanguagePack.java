package Ponomar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/***************************************************************
 * LanguagePack.java :: MODULE THAT DETERMINES THE LANGUAGE SPECIFIC OUTPUTS
 * 
 * LanguagePack.java is part of the Ponomar project. Copyright 2008 Yuri Shardt
 * version 1.0: August 2008 yuri (dot) shardt (at) gmail.com
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

class LanguagePack implements DocHandler {
	final LinkedHashMap<String, String> Phrases; // STORES ALL THE REQUIRED PHRASES FOR THE INTERFACE IN THE CURRENT
													// INTERFACE
	// LANGUAGE.
	private boolean readPhrases = false; // DETERMINE WHETHER TO READ OR NOT TO READ THE GIVEN PHRASES (THIS MUST BE
											// ADDED TO ALL THE READERS).
	private final StringOp analyse = new StringOp();

	public LanguagePack(Map<Object, Object> dayInfo) {
		Phrases = new LinkedHashMap<>();
		analyse.dayInfo = dayInfo;

		readPhrases();

	}

	public LanguagePack(String path, Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		Phrases = new LinkedHashMap<>();
		readPhrases(path);

	}

	private void readPhrases() {
		Helpers getFile = new Helpers(analyse.dayInfo);
		readPhrases(getFile.langFileFind(analyse.dayInfo.get("LS").toString(), "xml/Commands/LanguagePacks.xml"));
	}

	private void readPhrases(String langPath) {
		String filename = langPath;
		// System.out.println("Language Path: "+langPath);
		try {
			// ALLOWS MULTILINGUAL SUPPORT, WHICH IS A MUST IN OUR CASE.
			BufferedReader fr = new BufferedReader(
					new InputStreamReader(Files.newInputStream(Paths.get(filename)), StandardCharsets.UTF_8));
			// FileReader fr = new FileReader(filename);
			QDParser.parse(this, fr);
		} catch (Exception e) {
			// THIS STATEMENT CANNOT BE MULTILINGUAL!
			System.out.println("Unable to find " + filename);
			System.out.println(e);
			for (int i = 0; i < e.getStackTrace().length; i++) {
				System.out.println(e.getStackTrace()[i].toString());
			}
			System.out.println("------------------");
		}

	}

	public String[] obtainValues(String in) {
		// THIS FUNCTION TAKES A STTRING SEPARATED BY '\,' AND RETURNS A STRING ARRAY.
		String[] rough = in.split("/,");
		// System.out.println(rough[0] + " " +rough[1]);
		return rough;
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, HashMap<String, String> table) {
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.

		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			if (!analyse.evalbool(table.get("Cmd").toString())) {
				return;
			}
		}

		readPhrases = true;
		if (elem.equals("PHRASE") && readPhrases) {

			String key = table.get("Key");
			String value = table.get("Value");
			Phrases.put(key, value);
		}
	}

	public void endElement(String elem) {
		if (elem.equals("LANGUAGE")) {
			readPhrases = false;
		}
	}

	public void text(String text) {
	}
}