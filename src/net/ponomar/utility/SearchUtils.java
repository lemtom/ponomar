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
		if (lang.contains("cu")) {
			if (ignoreDiacritics) {
				commName = performAbbreviationExpansions(commName);
				searchString = performAbbreviationExpansions(searchString);	
				commName = normalizeSlavonicEquivalents(normalizeIzhitsa(commName.toLowerCase()));
				searchString = normalizeSlavonicEquivalents(normalizeIzhitsa(searchString.toLowerCase()));
				System.out.println(searchString);
	
				commName = disambiguateCyrillic(commName);
				searchString = disambiguateCyrillic(searchString);
				// commName = stripDiacriticalMarks(commName);
				// searchString = stripDiacriticalMarks(searchString);
			}
		} else {
			if (ignoreDiacritics) {
				commName = stripDiacriticalMarks(commName);
				searchString = stripDiacriticalMarks(searchString);

				if (lang.contains("zh")) {
					// Not implemented
				}
			}
			if (ignoreCapitalization) {
				commName = commName.toLowerCase();
				searchString = searchString.toLowerCase();
			}
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

	// This follows table 2 of Church Slavonic Grammar chapter 1
	private static String normalizeSlavonicEquivalents(String cyrillic) {
		return cyrillic.replace("є", "е").replace("ᲂу", "ꙋ").replace("ѕ", "з").replace("ї", "и").replace("ѻ", "ѡ")
				.replace("о", "ѡ").replace("ꙗ", "ѧ");
	}

	private static String normalizeIzhitsa(String cyrillic) {
		String strippedVersion = stripDiacriticalMarks(cyrillic);
		if (strippedVersion.contains("ѵ")) {
			int index = cyrillic.indexOf('ѵ');
			char replacement;
			if (index == -1) {
				index = strippedVersion.indexOf('ѵ');
				replacement = 'и';
			} else {
				replacement = 'в';
			}
			StringBuilder normalizedVersion = new StringBuilder(cyrillic);
			normalizedVersion.setCharAt(index, replacement);
			return normalizeIzhitsa(normalizedVersion.toString());

		} else {
			return strippedVersion;
		}

	}

	// This follows the text after Church Slavonic Grammar chapter 1
	private static String disambiguateCyrillic(String cyrillic) {
		String result = cyrillic;
		if (cyrillic.contains("ьма")) {
			result = cyrillic.replaceAll("(ьма\\b)", "ма");
		}
		if (cyrillic.contains("ж")) {
			result = replaceDisambiguatedCharacter(result, 'ж');
		}
		if (cyrillic.contains("ч")) {
			result = replaceDisambiguatedCharacter(result, 'ч');
		}
		if (cyrillic.contains("ш")) {
			result = replaceDisambiguatedCharacter(result, 'ш');
		}
		if (cyrillic.contains("щ")) {
			result = replaceDisambiguatedCharacter(result, 'щ');
		}
		return result;
	}

	private static String replaceDisambiguatedCharacter(String cyrillic, char character) {
		int index = cyrillic.indexOf(character) + 1;
		StringBuilder normalizedVersion = new StringBuilder(cyrillic);
		if (cyrillic.charAt(index + 1) == 'ѧ') {
			normalizedVersion.setCharAt(index, 'а');
		}
		if (cyrillic.charAt(index + 1) == 'ы') {
			normalizedVersion.setCharAt(index, 'и');
		}
		return normalizedVersion.toString();
	}
	
	private static String performAbbreviationExpansions(String cyrillic) {
		
		if (cyrillic.indexOf('҃')>-1) {
			cyrillic = expandTitloAbbreviation(cyrillic);
		}
		if (cyrillic.indexOf('҇')>-1) {
			cyrillic = expandEsPokrytieAbbreviation(cyrillic);
		}
		if (cyrillic.indexOf('ⷣ')>-1) {
			cyrillic = expandDeAbbreviation(cyrillic);
		}
		cyrillic = expandMiscAbbreviation(cyrillic);
		return cyrillic;
	}

	// Church Slavonic Grammar Table 54
	private static String expandTitloAbbreviation(String cyrillic) {
		cyrillic = cyrillic.replace("агг҃лъ", "ангелъ"); // Angel

		cyrillic = cyrillic.replace("бг҃ъ", "бо́гъ"); // God
		cyrillic = cyrillic.replace("бж҃-", "бо́ж-"); // God
		cyrillic = cyrillic.replace("бз҃-", "бо́з-"); // God
		cyrillic = cyrillic.replace("дх҃ъ", "дꙋхъ"); // spirit
		cyrillic = cyrillic.replace("дс҃-", "дꙋ́с-"); // spirit
		cyrillic = cyrillic.replace("дш҃-", "дꙋш-"); // spirit
		cyrillic = cyrillic.replace("дш҃а̀", "дꙋша̀ "); // soul

		if (cyrillic.contains("л҃")) {
			cyrillic = cyrillic.replace("бл҃гъ", "бла́гъ"); // Good (indefinite adjective)
			cyrillic = cyrillic.replace("бл҃ж-", "бла́ж-"); // Good (indefinite adjective)
			cyrillic = cyrillic.replace("бл҃з-", "бла́з-"); // Good (indefinite adjective)
			cyrillic = cyrillic.replace("бл҃гі́й", "благі́й"); // Good (definite adjective)
			cyrillic = cyrillic.replace("бг҃обл҃года́тный", "богоблагода́тный "); // God-given grace (double titlo!)
			cyrillic = cyrillic.replace("бл҃же́нъ", "бла́женъ"); // upright, righteous (adjective)
			cyrillic = cyrillic.replace("гл҃ати", "глаго́лати"); // to speak
			cyrillic = cyrillic.replace("гл҃го́лъ", "глаго́лъ "); // word, commandment
			cyrillic = cyrillic.replace("гл҃ъ", "гласъ "); // voice, word
			cyrillic = cyrillic.replace("гл҃авый", "глаго́вый "); // spoke (past active participle)
			cyrillic = cyrillic.replace("мл҃тва", "моли́тва"); // prayer
			cyrillic = cyrillic.replace("пл҃ть", "пло́ть"); // flesh
			cyrillic = cyrillic.replace("сл҃нце", "со́лнце"); // sun
			cyrillic = cyrillic.replace("сл҃нечный", "со́лнечный"); // solar
			cyrillic = cyrillic.replace("чл҃вкъ", "человкъ"); // male, man
			cyrillic = cyrillic.replace("чл҃ овкъ", "человкъ"); // male, man
			cyrillic = cyrillic.replace("чл҃вчь", "человчь"); // of man (adjective)
			cyrillic = cyrillic.replace("чл҃ овчь", "человчь"); // of man (adjective)
			cyrillic = cyrillic.replace("чл҃ко-", "человоко-"); // man (in compounds)
		}
		if (cyrillic.contains("р҃")) {
			cyrillic = cyrillic.replace("воскр҃си́ти", "воскреси́ти "); // to resurrect
			cyrillic = cyrillic.replace("воскр҃ше́нїе", "воскреше́нїе "); // resurrecting
			cyrillic = cyrillic.replace("воцр҃ит ́и", "воцари́ти"); // to reign
			cyrillic = cyrillic.replace("кр҃сти́сѧ", "крести́сѧ "); // to baptize (perfective)
			cyrillic = cyrillic.replace("кр҃ща́ти", "креща́ти"); // to baptise (imperfective)
			cyrillic = cyrillic.replace("кр҃ща́етъ", "креща́етъ"); // to baptise (imperfective)
			cyrillic = cyrillic.replace("мр҃іѧ", "марі́ѧ"); // Mary
			cyrillic = cyrillic.replace("мр҃іа", "марі́ѧ"); // Mary
			cyrillic = cyrillic.replace("мр҃їа́мь", "марїа́мь"); // Mary (formal nominative), Mariam
			cyrillic = cyrillic.replace("цр҃ь", "ца́ рь"); // king/emperor
			cyrillic = cyrillic.replace("цр҃ца", "ца́рица"); // queen/empress
			cyrillic = cyrillic.replace("цр҃ковь", "це́ рковь"); // church
		}
		if (cyrillic.contains("в҃")) {

			cyrillic = cyrillic.replace("дв҃а", "два "); // virgin
			cyrillic = cyrillic.replace("дв҃дъ", "даві́дъ "); // David
			cyrillic = cyrillic.replace("дв҃ца", "двца "); // young girl (?)
		}
		if (cyrillic.contains("н҃")) {
			cyrillic = cyrillic.replace("дн҃ь", "де́нь "); // day
			cyrillic = cyrillic.replace("нн҃ѣ", "ны́нѣ"); // today
			cyrillic = cyrillic.replace("дн҃сь", "дне́сь "); // today
			cyrillic = cyrillic.replace("кн҃зь", "кнѧ́зь"); // prince
			cyrillic = cyrillic.replace("кн҃ги́нѧ", "кнѧги́нѧ"); // princess
			cyrillic = cyrillic.replace("сн҃ъ", "сы́ нъ"); // son
			cyrillic = cyrillic.replace("ᲂу҆чн҃къ", "ᲂу҆чи́нкъ"); // disciples
			cyrillic = cyrillic.replace("ᲂу҆чн҃къ", "ᲂу҆чи́нкъ"); // disciples (Comm: seems the same?)
		}

		cyrillic = cyrillic.replace("і҆ис ҃ъ", "і҆и́сꙋсъ "); // Jesus
		cyrillic = cyrillic.replace("і҆ил ҃ь", "і҆сра́иль"); // Israel
		cyrillic = cyrillic.replace("і҆ил ҃тѧнъ", "і҆сра́ ильтѧнъ "); // Israeli person
		cyrillic = cyrillic.replace("і҆ил ҃і́те", "і҆сраилі́те "); // Israelite
		cyrillic = cyrillic.replace("і҆ил ҃тескъ", "і҆сраилі́тескъ"); // of Israel (adjective)
		cyrillic = cyrillic.replace("і҆ил ҃ьскїй", "і҆сра́ ильскїй "); // Israeli (adjective)

		cyrillic = cyrillic.replace("мт҃и", "ма́ти"); // mother
		cyrillic = cyrillic.replace("мт҃р-", "ма́тер-"); // mother
		cyrillic = cyrillic.replace("мт҃ер-", "ма́тер-"); // mother
		cyrillic = cyrillic.replace("мт҃ренъ", "ма́теренъ "); // mother (adjective)
		cyrillic = cyrillic.replace("ѻ҆ ́т҃че", "ѻ҆ ́тче"); // father
		cyrillic = cyrillic.replace("ст҃ъ", "свѧ́тъ"); // saint (adjective and noun)
		cyrillic = cyrillic.replace("ст҃и́ти", "свѧти́ти"); // to make holy, sanctify
		cyrillic = cyrillic.replace("ст҃ль", "свѧ́титель"); // enlightener
		cyrillic = cyrillic.replace("ᲂу҆чт҃ль", "ᲂу҆ чи́тель"); // teacher

		cyrillic = cyrillic.replace("мч҃нкъ", "мꙋ́ченикъ"); // martyr
		cyrillic = cyrillic.replace("нб҃о", "не́бо"); // heaven
		cyrillic = cyrillic.replace("нб҃са̀", "небеса̀"); // heaven
		cyrillic = cyrillic.replace("ѻ҆ц҃ъ", "ѻ҆те́цъ"); // father
		cyrillic = cyrillic.replace("ѻ ч҃ь", "ѻ҆ те́чь"); // of the father (?)
		cyrillic = cyrillic.replace("ѻч҃ей", "ѻ҆ те́чь"); // of the father (?)
		cyrillic = cyrillic.replace("ѻч҃и", "ѻ҆ те́чь"); // of the father (?)
		cyrillic = cyrillic.replace("ѻ҆ ч҃ескъ", "ѻ҆ те́ ческъ"); // fatherly (adjective)
		cyrillic = cyrillic.replace("безсм҃ртїе", "безсме́ртїе"); // immortal
		cyrillic = cyrillic.replace("см҃рть", "сме́рть"); // death
		cyrillic = cyrillic.replace("сщ҃а́ти (сщ҃ е́ нъ, сщ҃ а́ етсѧ, сщ҃ ꙋ̀)", "свѧща́ти"); // to make holy (?)
		cyrillic = cyrillic.replace("сщ҃е́нъ", "свѧща́ти"); // to make holy (?)
		cyrillic = cyrillic.replace("сщ҃а́етсѧ", "свѧща́ти"); // to make holy (?)
		cyrillic = cyrillic.replace("сщ҃ ꙋ̀", "свѧща́ти"); // to make holy (?)
		cyrillic = cyrillic.replace("сщ҃е́нникъ", "свѧще́нникъ"); // priest
		cyrillic = cyrillic.replace("сп҃съ", "спа́съ"); // Saviour
		cyrillic = cyrillic.replace("сп҃са́ти", "спаса́ти"); // to save
		cyrillic = cyrillic.replace("сп҃си́тель", "спасиси́тель"); // Saviour

		cyrillic = cyrillic.replace("ᲂу҆чн ҃ їе", "ᲂу҆чи́нїе"); // teachings, doctrine

		cyrillic = cyrillic.replace("ᲂу҆ чт ҃ ель", "ᲂу҆ чи́тель"); // teacher

		return cyrillic;
	}

	// Church Slavonic Grammar Table 55
	private static String expandEsPokrytieAbbreviation(String cyrillic) {
		cyrillic = cyrillic.replace("а҆ пⷭ҇лъ", "а҆по́столъ"); // Apostle
		cyrillic = cyrillic.replace("а҆пⷭ҇толь", "а҆по́столъ"); // Apostle
		cyrillic = cyrillic.replace("бжⷭ҇тво", "во́жество"); // divinity
		cyrillic = cyrillic.replace("бжⷭ҇тенный", "бо́жестенный"); // divine
		cyrillic = cyrillic.replace("блгⷭв ҇е́нъ", "благословенъ"); // blessed (adjective)
		cyrillic = cyrillic.replace("блгⷭв ҇и́ти", "благослови́ти"); // to bless
		cyrillic = cyrillic.replace("воскрⷭн ҇ їе", "воскресе́ нїе"); // Resurrection
		cyrillic = cyrillic.replace("воскрⷭъ ҇", "воскре́съ"); // Arose
		cyrillic = cyrillic.replace("воскрⷭн ҇ꙋти", "воскреснꙋти"); // to resurrect
		cyrillic = cyrillic.replace("воскрⷭн ҇ъ", "воскре́ сенъ"); // of the Arisen One (adjective)
		cyrillic = cyrillic.replace("гдⷭь ҇", "господь"); // Lord
		cyrillic = cyrillic.replace("гпⷭ ҇жа̀", "госпожа̀"); // Lady
		cyrillic = cyrillic.replace("двтⷭ ҇во", "дѣвство"); // virginity
		cyrillic = cyrillic.replace("є҆пⷭ҇пъ", "є҆пі́скопъ"); // bishop
		cyrillic = cyrillic.replace("є҆пⷭ҇копъ", "є҆пі́скопъ"); // bishop
		cyrillic = cyrillic.replace("і҆ерⷭл ҇и́мъ", "і҆ерꙋсали́мъ"); // Jerusalem
		cyrillic = cyrillic.replace("крⷭт ҇ъ", "кре́ стъ"); // Cross
		cyrillic = cyrillic.replace("крⷭт ҇итель", "крести́тель"); // Baptist
		cyrillic = cyrillic.replace("крⷭт ҇и́ти", "крести́ти"); // to baptise
		cyrillic = cyrillic.replace("млⷭт ҇ь", "ми́лость"); // kidness
		cyrillic = cyrillic.replace("млⷭр ҇дъ", "милосе́ рдъ"); // mercy (noun)
		cyrillic = cyrillic.replace("млⷭр ҇дїе", "милосе́ рдїе"); // merciful
		cyrillic = cyrillic.replace("мнⷭ ҇ты́рь", "монасты́ рь"); // monastery
		cyrillic = cyrillic.replace("мцⷭ ҇ъ", "мсѧцъ"); // Month
		cyrillic = cyrillic.replace("нбⷭн ҇ый", "небе́ сный"); // heavenly (adjective)
		cyrillic = cyrillic.replace("новомⷭ ҇чїе", "новомсѧчїе"); // new moon
		cyrillic = cyrillic.replace("причⷭ ҇", "прича́ стенъ"); // Communion hymn
		cyrillic = cyrillic.replace("прⷭт ҇о́лъ", "престо́лъ"); // Altar
		cyrillic = cyrillic.replace("прⷭн ҇ый", "при́сный"); // Good
		cyrillic = cyrillic.replace("прⷭн ҇ѡ", "при́снѡ"); // forever
		cyrillic = cyrillic.replace("прчⷭ ҇тый", "пречи́стый"); // immaculate
		cyrillic = cyrillic.replace("ржⷭ҇тво̀", "рождество̀"); // Nativity
		cyrillic = cyrillic.replace("спⷭ ҇нїе", "спа́сенїе"); // Salvation
		cyrillic = cyrillic.replace("спⷭ ҇тѝ", "спа́сти"); // to save
		cyrillic = cyrillic.replace("стрⷭт ҇ь", "стра́сть"); // Passion
		cyrillic = cyrillic.replace("трⷭт ҇о́е", "трисвѧто́е"); // Trisagion (The Thrice-Holy Hymn)
		cyrillic = cyrillic.replace("чⷭ ҇ть", "че́ сть"); // Honour, virtue
		cyrillic = cyrillic.replace("чⷭт ҇ый", "чи́стый"); // clean
		cyrillic = cyrillic.replace("чтⷭ ҇ый", "чи́стый"); // clean
		cyrillic = cyrillic.replace("чтⷭ ҇енъ", "че́стенъ"); // precious, honourable
		cyrillic = cyrillic.replace("чтⷭ ҇ны́й", "че́стенъ"); // precious, honourable
		cyrillic = cyrillic.replace("чⷭ ҇тенъ", "честны́ й"); // precious, honourable
		cyrillic = cyrillic.replace("чⷭ ҇тны́й", "честны́ й"); // precious, honourable
		cyrillic = cyrillic.replace("хрⷭт ҇о́съ", "христо́съ"); // Christ
		cyrillic = cyrillic.replace("црⷭт ҇во", "ца́ рство"); // kingdom
		cyrillic = cyrillic.replace("црⷭк ҇їй", "ца́рскїй"); // royal (adjective)

		return cyrillic;

	}
	
	// Church Slavonic Grammar Table 56
	private static String expandDeAbbreviation(String cyrillic) {
		cyrillic = cyrillic.replace("бцⷣа", "богоро́дица"); //Godbearer (Theotocos)
		cyrillic = cyrillic.replace("бг҃оро́дица", "богоро́дица"); //Godbearer (Theotocos) 
		cyrillic = cyrillic.replace("бчⷣенъ", "богоро́диченъ"); //Theotocion 
		cyrillic = cyrillic.replace("блгⷣть", "благода́ ть"); //grace 
		cyrillic = cyrillic.replace("бл҃года́ть", "благода́ ть"); //grace 
		cyrillic = cyrillic.replace("влⷣка", "владыка"); //Master 
		cyrillic = cyrillic.replace("влⷣчца", "владычица"); //Mistress (female Master) 
		cyrillic = cyrillic.replace("мрⷣъ", "мꙋ́дръ"); //wise (adjective) 
		cyrillic = cyrillic.replace("мⷣръ", "мꙋ́дръ"); //wise (adjective) 
		cyrillic = cyrillic.replace("млⷣнцъ", "младе́нецъ"); //infant (noun) 
		cyrillic = cyrillic.replace("нлⷣѧ", "недлѧ"); //Sunday 
		cyrillic = cyrillic.replace("првⷣкъ", "пра́ведникъ"); //righteous (adjective and noun) 
		cyrillic = cyrillic.replace("првⷣенъ", "пра́ведникъ"); //righteous (adjective and noun) 
		cyrillic = cyrillic.replace("првⷣн", "пра́веденъ"); //righteous (adjective and noun) 
		cyrillic = cyrillic.replace("првⷣв", "пра́веденъ"); //righteous (adjective and noun) 
		cyrillic = cyrillic.replace("пнⷣе", "понедльникъ"); //Monday 
		cyrillic = cyrillic.replace("пнⷣльникъ", "понедльникъ"); //Monday 
		cyrillic = cyrillic.replace("пртⷣеч ́а", "предте́ча"); //Forerunner 
		cyrillic = cyrillic.replace("прпⷣбенъ", "преподо́бенъ-"); //Venerable 
		cyrillic = cyrillic.replace("прпⷣбн", "преподо́бн"); //Venerable 
		cyrillic = cyrillic.replace("препⷣбн", "преподо́бн"); //Venerable 
		cyrillic = cyrillic.replace("поⷣ", "подо́бенъ"); //prosomœon/podoben (type of key hymn melody) 
		cyrillic = cyrillic.replace("срⷣе", "се́ реда"); //Wednesday 
		cyrillic = cyrillic.replace("срⷣце", "се́ рдце"); //heart 
		return cyrillic;
	}
	
	// Church Slavonic Grammar Table 57-70
	private static String expandMiscAbbreviation(String cyrillic) {

	
	cyrillic = cyrillic.replace("пррⷪк ҇ъ", "проро́къ"); //Prophet 
	cyrillic = cyrillic.replace("прⷪр ҇къ", "проро́къ"); //Prophet 

	cyrillic = cyrillic.replace("трⷪц ҇а", "тро́ица"); //Trinity 
	cyrillic = cyrillic.replace("є҆ѵⷢ҇лїе", "є҆ѵа́нгелїе"); //Gospel 
	cyrillic = cyrillic.replace("им ҇ⷬ къ", "имѧрекъ"); //Say the appropriate name (N. N.) 
	cyrillic = cyrillic.replace("им҃рекъ", "имѧрекъ"); //Say the appropriate name (N. N.) 
	cyrillic = cyrillic.replace("втоⷬ ҇", "вто́рникъ"); //Tuesday 
	cyrillic = cyrillic.replace("заⷱ҇", "зача́ло"); //pericope 
	cyrillic = cyrillic.replace("глⷡа ҇", "гла́ва"); //chapter 
	cyrillic = cyrillic.replace("глаⷡ҇", "гла́ва"); //chapter 

	cyrillic = cyrillic.replace("сⷠ҇", "сꙋббѡ́ та"); //Saturday 
	cyrillic = cyrillic.replace("триⷤ", "три́жды"); //thrice, three times 
	cyrillic = cyrillic.replace("дваⷤ", "два́ жды"); //twice 
	cyrillic = cyrillic.replace("проⷦ ҇", "прокі́мени"); //prokimenon 
	cyrillic = cyrillic.replace("пѧⷦ ҇", "пѧто́къ"); //Friday 
	cyrillic = cyrillic.replace("че ҇ ⷦ", "четверто́къ"); //Thursday 
	cyrillic = cyrillic.replace("ѱаⷧ ҇", "ѱа́ лмъ"); //Psalm 
	cyrillic = cyrillic.replace("ндⷧѧ ҇", "недлѧ"); //Sunday 
	cyrillic = cyrillic.replace("риⷨ", "ри́млѧнъ"); //(Epistle to the) Romans 
	cyrillic = cyrillic.replace("сол҇ ⷩ", "солꙋ́нѧнѡмъ"); //(Epistle to the) Thessalonians 
	cyrillic = cyrillic.replace("сⷯ", "сті́хъ"); //verse (stichos)
	cyrillic = cyrillic.replace("варⷯ", "варꙋ́хъ"); //(Book of) Baruch 

	cyrillic = cyrillic.replace("корі́н҇ ⷴ", "корі́нѳѧнъ"); //(Epistle to the) Corinthians 
	cyrillic = cyrillic.replace("праⷥ", "пра́зникъ"); //feast 
	cyrillic = cyrillic.replace("роⷥ", "розво́дъ"); //resolution
	
	return cyrillic;

	}
}
