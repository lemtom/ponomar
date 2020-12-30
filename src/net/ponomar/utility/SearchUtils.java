package net.ponomar.utility;

import java.io.File;
import java.text.Normalizer;

public final class SearchUtils {

	private static final String N_A = "N/A";
	private static final String EXISTS = "Exists";
	private static final String LIVES_PATH = "xml/lives/";
	
	private SearchUtils() {
	}

	public static boolean searchName(String searchString, String commName, String lang, boolean ignoreDiacritics,
			boolean ignoreCapitalization) {
		// Checks if the search term is in latin text
		if (ignoreDiacritics) {
			commName = SearchUtils.stripDiacriticalMarks(commName);
			searchString = SearchUtils.stripDiacriticalMarks(searchString);
			if (lang.contains("cu")) {
				commName = normalizeCyrillic(commName);
				searchString = normalizeCyrillic(searchString);
				System.out.println(searchString);
			}
			if (lang.contains("zh")) {
				// Not implemented
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

	// There has to be a more efficient way
	private static String normalizeCyrillic(String cyrillic) {
		return cyrillic.replace("є", "е").replace("і", "и");
	}

}
