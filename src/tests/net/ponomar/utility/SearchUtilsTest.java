package tests.net.ponomar.utility;

import static net.ponomar.utility.SearchUtils.searchName;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SearchUtilsTest {

	//@Disabled //This library can possibly take care of it https://github.com/houbb/opencc4j
	@Test
	void chineseCases(){
		//An example to try: traditional: 格奧爾吉; simplified: 格奥尔吉 (both forms correspond to George in Chinese). 
		// Only the middle two characters are different.
		assertTrue(searchName("格奥尔吉", "格奧爾吉", "zh", true, false));
	}

	@Test
	void greekCases(){
		// For polytonic Greek, I can suggest the form ἅγιος (masculine form of *holy*). With diacritical marks stripped, 
		// it should also match the monotonic Greek form άγιος (and vice versa). 
		assertTrue(searchName("άγιος", "ἅγιος", "gr", true, false));
		assertFalse(searchName("άγιος", "ἅγιος", "gr", false, false));
	}
	
	@Test
	void slavonicCases(){
		// ѻ҆те́цъ (nominative singular), ѻ҆тє́цъ (genitive plural), and then пра́ѻтецъ, which all should be found if we
		// search for “ѻтецъ”. Normalising the forms would give *отецъ*, *отецъ*, and *праотецъ* which will now be easily found.
		assertTrue(searchName("ѻ҆тє́цъ", "пра́ѻтецъ", "cu", true, false));
		assertFalse(searchName("ѻ҆тє́цъ", "пра́ѻтецъ", "cu", false, false));
		assertTrue(searchName("сѷрі́а", "сирі́а", "cu", true, true));
		assertTrue(searchName("сѷрі́аѵ", "сирі́ав", "cu", true, true)); //Nonsense word
		assertTrue(searchName("тьма̀", "тма̀", "cu", true, true)); //In words ending in –ьма, the ь may be dropped
		assertTrue(searchName("тьма̀,", "тма̀,", "cu", true, true)); //As above, but other stuff after it
		assertFalse(searchName("тьма̀b", "тмbа̀", "cu", true, true)); //Nonsense word to subvert case above
		assertTrue(searchName("прокі́мени", "проⷦ ҇", "cu", true, true)); //Abbreviation

	}
	
	void frenchCases(){
		assertTrue(searchName("melece", "Mélèce", "fr", true, true));
		assertFalse(searchName("melece", "Mélèce", "fr", true, false));
		assertFalse(searchName("melece", "Mélèce", "fr", false, true));
	}
}
