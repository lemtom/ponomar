package tests.net.ponomar.utility;

import static net.ponomar.utility.SearchUtils.cyrillicToAscii;
import static net.ponomar.utility.SearchUtils.normalizeCu;
import static net.ponomar.utility.SearchUtils.performAbbreviationExpansions;
import static net.ponomar.utility.SearchUtils.searchName;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SearchUtilsTest {
	@Test
	void chineseCases() {
		// An example to try: traditional: 格奧爾吉; simplified: 格奥尔吉 (both forms correspond
		// to George in Chinese).
		// Only the middle two characters are different.
		assertTrue(searchName("格奥尔吉", "格奧爾吉", "zh", true, false));
	}

	@Test
	void greekCases() {
		// For polytonic Greek, I can suggest the form ἅγιος (masculine form of
		// *holy*). With diacritical marks stripped,
		// it should also match the monotonic Greek form άγιος (and vice versa).
		assertTrue(searchName("άγιος", "ἅγιος", "gr", true, false));
		assertFalse(searchName("άγιος", "ἅγιος", "gr", false, false));
	}

	@Test
	void slavonicCases() {
		// These are taken from the Python tests
		assertEquals("свѧ́тъ", performAbbreviationExpansions("ст҃ъ"), "Titlo resolution doesn't work.");
		assertEquals("свя́тъ", normalizeCu("ст҃ъ", false, false), "Slavonic to Russian conversion doesn't work.");
		assertEquals("святъ", normalizeCu("ст҃ъ", false, true), "NoAccents option not honored.");
		assertEquals("свят", normalizeCu("ст҃ъ", true, true), "ModernRules option not honored.");

		//Numbers
		assertEquals(121, cyrillicToAscii("рк҃а"), "Cyrillic numbers to ascii numbers not working.");

		// the Slavonic word сн҃а could both be an abbreviation for Сы́на and a numeral
		// (251)
		assertEquals("сы́на", normalizeCu("сн҃а", false, false));
		assertEquals(251, cyrillicToAscii("сн҃а"), "Cyrillic numbers to ascii numbers not working.");

		// ѻ҆те́цъ (nominative singular), ѻ҆тє́цъ (genitive plural), and then пра́ѻтецъ,
		// which all should be found if we
		// search for “ѻтецъ”. Normalising the forms would give *отецъ*, *отецъ*, and
		// *праотецъ* which will now be easily found.
		assertTrue(searchName("ѻтецъ", "пра́ѻтецъ", "cu", true, false));
		assertFalse(searchName("ѻ҆тє́цъ", "пра́ѻтецъ", "cu", false, false));
		assertTrue(searchName("ѻтецъ", "ѻ҆тє́цъ", "cu", true, false));
		assertTrue(searchName("ѻтецъ", "ѻ҆те́цъ", "cu", true, false));
	}

	void frenchCases() {
		assertTrue(searchName("melece", "Mélèce", "fr", true, true));
		assertFalse(searchName("melece", "Mélèce", "fr", true, false));
		assertFalse(searchName("melece", "Mélèce", "fr", false, true));
	}
}
