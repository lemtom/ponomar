package net.ponomar.utility;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.houbb.opencc4j.util.*;

public final class SearchUtils {

	private static final String N_A = "N/A";
	private static final String EXISTS = "Exists";
	private static final String LIVES_PATH = "xml/lives/";

	private static final Map<String, String> DEFINITIONS = new HashMap<>();
	private static final Map<String, String> SLAVONIC_CHARACTERS = new HashMap<>();
	private static final HashMap<String, Integer> DIGITS = new HashMap<>();

	static {
		SLAVONIC_CHARACTERS.put("\u0405", "З"); // capital Zelo
		SLAVONIC_CHARACTERS.put("\u0404", "Е"); // capital wide Est
		SLAVONIC_CHARACTERS.put("\u0454", "е"); // lowercase wide est
		SLAVONIC_CHARACTERS.put("\u0455", "з"); // lowercase zelo
		SLAVONIC_CHARACTERS.put("\u0456\u0308", "\u0456"); // double-dotted i
		SLAVONIC_CHARACTERS.put("\u0457", "\u0456");
		SLAVONIC_CHARACTERS.put("\u0460", "О"); // capital Omega
		SLAVONIC_CHARACTERS.put("\u0461", "о"); // lowercase omega
		SLAVONIC_CHARACTERS.put("\u0466", "Я"); // capital small Yus
		SLAVONIC_CHARACTERS.put("\u0467", "я"); // lowercase small yus
		SLAVONIC_CHARACTERS.put("\u046E", "Кс"); // capital Xi
		SLAVONIC_CHARACTERS.put("\u046F", "кс"); // lowercase xi
		SLAVONIC_CHARACTERS.put("\u0470", "Пс"); // capital Psi
		SLAVONIC_CHARACTERS.put("\u0471", "пс"); // lowercase psi
		SLAVONIC_CHARACTERS.put("\u0472", "Ф"); // capital Theta
		SLAVONIC_CHARACTERS.put("\u0473", "ф"); // lowercase theta
		SLAVONIC_CHARACTERS.put("\u0474", "В"); // izhitsa
		SLAVONIC_CHARACTERS.put("\u0475", "в"); // izhitsa
		SLAVONIC_CHARACTERS.put("\u047A", "О"); // wide O
		SLAVONIC_CHARACTERS.put("\u047B", "о"); // wide o
		SLAVONIC_CHARACTERS.put("\u047C", "О"); // omega with great apostrophe
		SLAVONIC_CHARACTERS.put("\u047D", "о"); // omega with great apostrophe
		SLAVONIC_CHARACTERS.put("\u047E", "Отъ"); // Ot
		SLAVONIC_CHARACTERS.put("\u047F", "отъ"); // ot
		SLAVONIC_CHARACTERS.put("\uA64A", "У"); // Uk
		SLAVONIC_CHARACTERS.put("\uA64B", "у"); // uk
		SLAVONIC_CHARACTERS.put("\uA64C", "О"); // wide omega
		SLAVONIC_CHARACTERS.put("\uA64D", "о"); // wide omega
		SLAVONIC_CHARACTERS.put("\uA656", "Я"); // Ioted a
		SLAVONIC_CHARACTERS.put("\uA657", "я"); // ioted a

		DIGITS.put("", 0);
		DIGITS.put("а", 1);
		DIGITS.put("в", 2);
		DIGITS.put("г", 3);
		DIGITS.put("д", 4);
		DIGITS.put("є", 5);
		DIGITS.put("ѕ", 6);
		DIGITS.put("з", 7);
		DIGITS.put("и", 8);
		DIGITS.put("ѳ", 9);
		DIGITS.put("і", 10);
		DIGITS.put("к", 20);
		DIGITS.put("л", 30);
		DIGITS.put("м", 40);
		DIGITS.put("н", 50);
		DIGITS.put("ѯ", 60);
		DIGITS.put("о", 70);
		DIGITS.put("п", 80);
		DIGITS.put("ч", 90);
		DIGITS.put("р", 100);
		DIGITS.put("с", 200);
		DIGITS.put("т", 300);
		DIGITS.put("у", 400);
		DIGITS.put("ф", 500);
		DIGITS.put("х", 600);
		DIGITS.put("ѱ", 700);
		DIGITS.put("ѿ", 800);
		DIGITS.put("ц", 900);

		// Load the Titlo resolution data into memory
		try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(
				Objects.requireNonNull(SearchUtils.class.getResourceAsStream("data.txt")), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.charAt(1) == '#') {
					continue;
				}
				line = line.replaceAll("\\r?\\n", "");
				if (line.isEmpty()) {
					continue;
				}
				String[] parts = line.split("\t");
				parts[0] = parts[0].replaceAll("\\.", "\\\\b");
				DEFINITIONS.put(parts[0], parts[1]);
			}
		} catch (java.io.IOException e) {
			throw new RuntimeException("Error loading Titlo resolution data", e);
		}
	}

	private SearchUtils() {
	}

	/**
	 * 
	 * Takes well-formatted Church Slavonic text and transforms it into Russian
	 * (civil) orthography. The following operations are performed:
	 * <p>
	 * Titli and lettered titli are resolved
	 * <p>
	 * Cyrillic numerals are resolved to Ascii numbers (but see note concerning
	 * B<сн҃а>) TO BE IMPLEMENTED
	 * <p>
	 * Stress marks are transformed to the acute accent (U+0301) and all other
	 * diacritical marks are removed
	 * <p>
	 * Letters that do not occur in Russian are transformed into their Russian
	 * analogs (e.g., ѧ is transformed to я)
	 * <p>
	 * Some spelling is normalized to agree with common Russian rules (e.g., шы is
	 * transformed to ши)
	 * 
	 * @param text        Well-formatted Church Slavonic text
	 * @param modernRules If true, the text is further simplified into modern
	 *                    Russian orthography (that means that і is resolved to и, ѣ
	 *                    is resolved to е, and trailing ъ is removed); otherwise,
	 *                    traditional (pre-1918) orthography is assumed.
	 * @param noAccents   If true, all stress marks are removed; otherwise, stress
	 *                    indications remain in the text, but only as the acute
	 *                    accent (U+0301)
	 * @return Russian (civil) orthography
	 */
	public static String normalizeCu(String text, boolean modernRules, boolean noAccents) {

		text = text.replaceAll("\\r?\\n", "");

		text = performAbbreviationExpansions(text);

		// Convert yerok to hard sign
		text = text.replaceAll("[\u033E\u2E2F]", "ъ");

		// Convert grave and circumflex accents to acute
		text = text.replaceAll("[\u0300\u0311]", "\u0301");

		// Convert izhitsa
		text = text.replaceAll("\u0474([\u0486\u0301])", "И$1");
		text = text.replaceAll("\u0475([\u0486\u0301])", "и$1");

		// Remove all breathing marks and double dots
		text = text.replaceAll("[\u0486\uA67C\uA67E\u0308]", "");

		// Resolve diagraph OU to U
		text = text.replaceAll("ᲂу|ѹ", "у");
		text = text.replaceAll("Оу|Ѹ", "У");

		text = resolveIzhitsaForms(text);

		// Remove all variation selectors
		text = text.replaceAll("[\uFE00\uFE01]", "");

		// Convert semicolon to question mark
		text = text.replace(";", "?");

		// Attempt to resolve any numerals
		text = resolveNumerals(text);

		text = resolvePeculiarLetters(text);

		text = standardizeSpellings(text);

		if (modernRules) {
			text = implementModernRules(text);
		}

		if (noAccents) {
			// Remove stress mark (acute accent)
			text = text.replaceAll("\u0301", "");
		}

		return text;
	}

	/**
	 * Only works for numerals below one thousand
	 * @param text
	 * @return
	 */
	private static String resolveNumerals(String text) {
		String who = String.join("|", DIGITS.keySet());

		text = text.replaceAll("([" + who + "][" + who + "][\\u0483][" + who + "])",
				String.valueOf(cyrillicToAscii("$1")));
		text = text.replaceAll("([" + who + "][\\u0483][" + who + "])", String.valueOf(cyrillicToAscii("$1")));
		text = text.replaceAll("([" + who + "][" + who + "][\\u0483])", String.valueOf(cyrillicToAscii("$1")));
		text = text.replaceAll("([" + who + "][\\u0483])", String.valueOf(cyrillicToAscii("$1")));
		return text;
	}

	private static String implementModernRules(String text) {
		// Get rid of the decimal I
		text = text.replaceAll("\u0406", "И");
		text = text.replaceAll("\u0456", "и");

		// Get rid of the yat
		text = text.replaceAll("\u0462", "Е");
		text = text.replaceAll("\u0463", "е");

		// Get rid of all trailing hard signs
		text = text.replaceAll("ъ\\b|Ъ\\b", "");
		return text;
	}

	/**
	 * Resolve all forms of izhitsa with accent
	 * 
	 * @param text
	 * @return
	 */
	private static String resolveIzhitsaForms(String text) {
		text = text.replaceAll("\u0474\u0301", "И\u0301");
		text = text.replaceAll("\u0475\u0301", "и\u0301");
		text = text.replaceAll("\u0474\u030F", "И");
		text = text.replaceAll("\u0475\u030F", "и");
		return text;
	}

	/**
	 * Standardize Russian-style spelling
	 * 
	 * @param text
	 * @return
	 */
	private static String standardizeSpellings(String text) {
		text = text.replace("ъи", "ы");
		text = text.replaceAll("([жшщ])ы", "$1и");
		text = text.replaceAll("([жшщч])я", "$1а");
		text = text.replaceAll("([оО])тъ([абвгджзклмнопрстуфхцчшщ])", "$1т$2");
		return text;
	}

	public static boolean searchName(String searchString, String commName, String lang, boolean ignoreDiacritics,
			boolean ignoreCapitalization) {
		// Checks if the search term is in latin text
		if (lang.contains("cu")) {
			commName = normalizeCu(commName, true, ignoreDiacritics);
			searchString = normalizeCu(searchString, true, ignoreDiacritics);
		} else {
			if (lang.contains("zh")) {
				commName = ZhConverterUtil.toSimple(commName);
				searchString = ZhConverterUtil.toSimple(searchString);
			}
			if (ignoreDiacritics) {
				commName = stripDiacriticalMarks(commName);
				searchString = stripDiacriticalMarks(searchString);
			}
		}
		if (ignoreCapitalization) {
			commName = commName.toLowerCase();
			searchString = searchString.toLowerCase();
		}

		return commName.contains(searchString);
	}

	public static String checkIfFound(File file, String langPath) {
		if (new File(Constants.LANGUAGES_PATH + "/" + langPath + LIVES_PATH + file.getName()).exists()) {
			return EXISTS;
		} else {
			return N_A;
		}
	}

	private static String stripDiacriticalMarks(String word) {
		word = Normalizer.normalize(word, Normalizer.Form.NFD);
		// Mn => Nonspacing_Mark, which also encompasses CombiningDiacriticalMarks
		word = word.replaceAll("[\\p{Mn}]", "");
		return word;
	}

	private static String resolvePeculiarLetters(String cyrillic) {
		String resolverPattern = "(" + String.join("|", SLAVONIC_CHARACTERS.keySet()) + ")";
		return StringReplacer.replace(cyrillic, Pattern.compile(resolverPattern),
				match -> SLAVONIC_CHARACTERS.get(match.group()));
	}

	/**
	 * Takes a word that is written with a titlo or lettered titlo (as an
	 * abbreviation or <i>nomen sacrum</i>) and writes it out in full, resolving the
	 * abbreviation.
	 * <p>
	 * Bugs: correct placement of stress marks and capitalization are not
	 * guaranteed. Titlo resolution relies on a list that can still be improved.
	 * <p>
	 * Warning: the Slavonic word сн҃а could both be an abbreviation for Сы́на and a
	 * numeral (251). Thus, this method will return Сы́на but
	 * cyrillicToAscii("сн҃а") will return 251.
	 * 
	 * @param text
	 * @return
	 */
	public static String performAbbreviationExpansions(String text) {
		StringBuilder sb = new StringBuilder(text);
		for (Map.Entry<String, String> entry : DEFINITIONS.entrySet()) {
			String pattern = entry.getKey();
			String replacement = entry.getValue();
			sb.replace(0, sb.length(), sb.toString().replaceAll(pattern, replacement));
		}
		return sb.toString();
	}

	/**
	 * Takes a Cyrillic numeral and returns the corresponding ASCII digits.
	 * <p>
	 * Example: cyrillicToAscii("рк҃а") returns 121.
	 * 
	 * @param number Cyrillic numeral
	 * @return Corresponding ASCII digits if it's a well-formed number
	 */
	public static int cyrillicToAscii(String number) {
		String o = String.join("|", DIGITS.keySet().stream().filter(k -> DIGITS.get(k) < 10).toArray(String[]::new));
		String t = String.join("|", DIGITS.keySet().stream().filter(k -> DIGITS.get(k) >= 10 && DIGITS.get(k) < 100)
				.toArray(String[]::new));
		String h = String.join("|", DIGITS.keySet().stream().filter(k -> DIGITS.get(k) >= 100).toArray(String[]::new));

		number = number.replaceAll("\u0483", "");
		int result = 0;
		Matcher matcher;
		if (number.contains(" ")) {
			String umpteen = number.substring(0, number.indexOf(" "));
			umpteen = umpteen.replaceAll("҂", "");
			matcher = Pattern.compile("^([" + h + "]?)([клмнѯопч]?)([" + o + "]?)$").matcher(umpteen);
			if (matcher.matches()) {
				result += (matchGroup(matcher, 1) + matchGroup(matcher, 2) + matchGroup(matcher, 3)) * 1000;
			} else {
				matcher = Pattern.compile("^([" + h + "]?)([" + o + "]?)(і)$").matcher(umpteen);
				if (matcher.matches()) {
					result += (matchGroup(matcher, 1) + matchGroup(matcher, 2) + matchGroup(matcher, 3)) * 1000;
				}
				// else not a valid Cyrillic number
			}
			number = number.substring(number.indexOf(" ") + 1);
		}

		matcher = Pattern.compile(
				"^(?:҂([" + h + "]))*(?:҂([" + t + "]))*(?:҂([" + o + "]))*([" + h + "]?)([клмнѯопч]?)([" + o + "]?)$")
				.matcher(number);
		if (matcher.matches()) {
			result += 1000 * (matchGroup(matcher, 1) + matchGroup(matcher, 2) + matchGroup(matcher, 3))
					+ matchGroup(matcher, 4) + matchGroup(matcher, 5) + matchGroup(matcher, 6);
		} else {
			matcher = Pattern.compile(
					"^(?:҂([" + h + "]))*(?:҂([" + t + "]))*(?:҂([" + o + "]))*([" + h + "]?)([" + o + "]?)(і)$")
					.matcher(number);
			if (matcher.matches()) {
				result += 1000 * (matchGroup(matcher, 1) + matchGroup(matcher, 2) + matchGroup(matcher, 3))
						+ matchGroup(matcher, 4) + matchGroup(matcher, 5) + matchGroup(matcher, 6);
			}
			// else not a valid Cyrillic number

		}
		return result;
	}

	private static int matchGroup(Matcher matcher, int i) {
		Integer value = DIGITS.get(matcher.group(i));
		if (value != null) {
			return value;
		}
		return 0;
	}

}
