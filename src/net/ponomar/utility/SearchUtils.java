package net.ponomar.utility;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.github.houbb.opencc4j.util.*;

public final class SearchUtils {

	private static final String N_A = "N/A";
	private static final String EXISTS = "Exists";
	private static final String LIVES_PATH = "xml/lives/";

	private static final Map<String, String> DEFINITIONS = new HashMap<>();
	private static final Map<String, String> SLAVONIC_CHARACTERS = new HashMap<>();

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

	public static String normalizeCu(String text, boolean skipTitlos, boolean modernRules, boolean noAccents) {

		text = text.replaceAll("\\r?\\n", "");

		if (skipTitlos) {
			text = performAbbreviationExpansions(text);
		}

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

		// TODO numerals

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
			commName = normalizeCu(commName, true, true, ignoreDiacritics);
			searchString = normalizeCu(searchString, true, true, ignoreDiacritics);
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

	public static String performAbbreviationExpansions(String cyrillic) {
		StringBuilder sb = new StringBuilder(cyrillic);
		for (Map.Entry<String, String> entry : DEFINITIONS.entrySet()) {
			String pattern = entry.getKey();
			String replacement = entry.getValue();
			sb.replace(0, sb.length(), sb.toString().replaceAll(pattern, replacement));
		}
		return sb.toString();
	}

}
