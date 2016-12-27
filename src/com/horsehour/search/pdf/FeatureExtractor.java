package com.horsehour.search.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p> Extract Important Features from Document Page
 * <p> Features or Signals:
 * <p> -- <b>layout</b>: left, top, width, height
 * <p> -- <b>punctuation and special characters</b>:
 * <li> -|.|,|"|'|:|@ <li> position
 * <p> -- <b>number</b>: 1|2|3|4, date|phone|fax, superscript|footnote
 * <p> -- <b>format</b>: font id,size|bold|italics|capitalization
 * <p> -- <b>keywords</b>:
 * <li> n|univ,dept,phone,conference,journal <li> prep|of,in,on,at,for,the 
 * <li> pron|we,it,they <li> n|abstract,introduction,summary,keywords,contents
 * <p> -- <b>count</b>: length|page|punctuation
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since Oct 27 2013
 */
public class FeatureExtractor {
	public List<LineEntry> pageEntry;
	public List<LineFeature> pageFeature;

	public Map<Character, List<Integer>> charIndex;
	public Map<String, List<Integer>> wordIndex;

	public String wordBegin, wordEnd;
	public int len, nAlpha, nDigit, nAlnum, nUpperCase;

	public int width, height;

	public int nLine;

	public FeatureExtractor(List<LineEntry> pageEntry) {
		this.pageEntry = pageEntry;
		this.nLine = pageEntry.size();
		this.pageFeature = new ArrayList<LineFeature>();
	}

	public void extractFeatures(){
		LineEntry lineEntry = null;
		LineFeature lineFeature = null, lastLineFeature;
		for (int i = 0; i < nLine; i++) {
			lineFeature = new LineFeature();
			pageFeature.add(lineFeature);
			lineEntry = pageEntry.get(i);
			lineFeature.text = lineEntry.text;

			extractLayoutFormatFeatures(lineEntry, lineFeature);
			extractCountFeatures(lineFeature);
			if (i == 0)
				lastLineFeature = null;
			else
				lastLineFeature = pageFeature.get(i - 1);

			extractSentenceBreakFeature(lineFeature, lastLineFeature);
			extractWordLevelFeatures(lineFeature);
			extractDerivedFeatures(lineFeature, lastLineFeature);
			extractSubheadFeature(lineFeature);
			addScoreFeatures(lineFeature);
		}
	}

	/**
	 * Layout features, including line number, left, top, width, height 
	 * and format features, such as font id, font size, maxmin font(1/0/-1),
	 * bold(0/1), italic(0/1), begin and end char, the begin word has an 
	 * initial captital letter(0/1/-1), the end word has an initial 
	 * capital letter(0/1/-1), 
	 * @param lineEntry
	 * @param lineFeature
	 */
	public void extractLayoutFormatFeatures(LineEntry lineEntry, LineFeature lineFeature){
		int dim = lineEntry.data.length;
		// LINENUM,PAGEWIDTH,PAGEHEIGHT,LEFT,TOP,WIDTH,HEIGHT,FONTID,FONTSIZE,BOLD,ITALICS
		for (int i = 0; i < dim; i++)
			lineFeature.addFeature(lineEntry.getDatum(i));

		String text = lineEntry.text;
		createCharIndex(text);
		lineFeature.addFeature(text.charAt(0));// BEGINCHAR
		lineFeature.addFeature(text.charAt(len - 1));// ENDCHAR

		createWordIndex(text);
		if (wordBegin == null)// BEGINWORDICL
			lineFeature.addFeature(-1);
		else if (Character.isUpperCase(wordBegin.charAt(0)))
			lineFeature.addFeature(1);
		else
			lineFeature.addFeature(0);

		if (wordEnd == null)// ENDWORDICL
			lineFeature.addFeature(-1);
		else if (Character.isUpperCase(wordEnd.charAt(0)))
			lineFeature.addFeature(1);
		else
			lineFeature.addFeature(0);
	}

	/**
	 * Count features, including text length, number of uppercase characters,
	 * number of all alphabetic characters, number of digits, and others
	 * (including punctuations and whitespaces), number of commas, and number 
	 * of periods.
	 * @param lineFeature
	 */
	public void extractCountFeatures(LineFeature lineFeature){
		lineFeature.addFeature(len);// LENGTH

		nDigit = 0;
		nUpperCase = 0;
		int nComma = 0, nPeriod = 0;
		for (int i = 0; i < len; i++) {
			char ch = lineFeature.text.charAt(i);
			if (Character.isDigit(ch))
				nDigit++;
			else if (Character.isUpperCase(ch))
				nUpperCase++;
			else if (ch == ',')
				nComma++;
			else if (ch == '.')
				nPeriod++;
		}

		nAlpha = nAlnum - nDigit;
		lineFeature.addFeature(nAlpha);// ALPHACOUNT
		lineFeature.addFeature(nUpperCase);// UPPERCASECOUNT
		lineFeature.addFeature(nDigit);// DIGITCOUNT
		lineFeature.addFeature(len - nAlnum);// NONALNUM
		lineFeature.addFeature(nComma);// COMMACOUNT
		lineFeature.addFeature(nPeriod);// PERIODCOUNT

		int nWord = 0;
		for (String word : wordIndex.keySet())
			nWord += wordIndex.get(word).size();
		lineFeature.addFeature(nWord);// WORDCOUNT
	}

	/**
	 * @param text
	 */
	private void createCharIndex(String text){
		charIndex = Collections.synchronizedMap(new HashMap<Character, List<Integer>>());
		len = text.length();
		nAlnum = 0;
		for (int i = 0; i < len; i++) {
			char ch = text.charAt(i);
			if (Character.isAlphabetic(ch) || Character.isDigit(ch))
				nAlnum++;
			if (!charIndex.containsKey(ch))
				charIndex.put(ch, new ArrayList<Integer>());
			charIndex.get(ch).add(i);
		}
	}

	/**
	 * @param text
	 */
	private void createWordIndex(String text){
		wordIndex = Collections.synchronizedMap(new HashMap<String, List<Integer>>());
		wordBegin = wordEnd = null;

		String word;
		int head = 0;
		for (int i = 0; i < len; i++) {
			char ch = text.charAt(i);
			if (Character.isAlphabetic(ch)) {
			} else if (head == i)
				head++;
			else {// head < i
				word = text.substring(head, i).toLowerCase();
				if (!wordIndex.containsKey(word))
					wordIndex.put(word, new ArrayList<Integer>());
				wordIndex.get(word).add(head);

				if (wordIndex.size() == 1)
					wordBegin = text.substring(head, i);
				head = i + 1;
			}
		}

		if (head < len - 1) {
			word = text.substring(head).toLowerCase();
			if (!wordIndex.containsKey(word))
				wordIndex.put(word, new ArrayList<Integer>());
			wordIndex.get(word).add(head);
			wordEnd = text.substring(head);
		}

		if (wordIndex.size() == 1) {
			if (wordBegin == null)
				wordBegin = wordEnd;
			if (wordEnd == null)
				wordEnd = wordBegin;
		}
	}

	/**
	 * Extract Superscript Feature
	 * @param lineFeature
	 * @param lastLineFeature
	 */
	public void extractSentenceBreakFeature(LineFeature lineFeature, LineFeature lastLineFeature){
		int feature = 0;// unknown
		if (lastLineFeature == null) {
			feature = -1;// null
		} else {
			char charBegin = (char) lineFeature.getFeature(Feature.BEGINCHAR.id());
			float lastWordICL = lastLineFeature.getFeature(Feature.ENDWORDICL.id());
			if (Character.isDigit(charBegin) && lastWordICL == 1) {
				if (lineFeature.getFeature(Feature.LEFT.id()) > lastLineFeature
				        .getFeature(Feature.LEFT.id())
				        && lineFeature.getFeature(Feature.TOP.id()) < lastLineFeature
				                .getFeature(Feature.TOP.id())) {
					feature = 1;// superscript
				}
			} else if (Character.isLowerCase(charBegin) || charBegin == '.' || charBegin == ','
			        || (wordBegin != null && wordBegin.equalsIgnoreCase("and"))) {
				feature = 2;// break sentence to the last line
			}
		}

		char charEnd = (char) lineFeature.getFeature(Feature.ENDCHAR.id());
		if (charEnd == ',' || charEnd == '-')
			feature = 3; // break sentence to the next line
		else if (charEnd == '.')
			feature = 4; // a complete sentence itself

		lineFeature.addFeature(feature);// SENTENCE BREAK
	}

	/**
	 * Extract word-level features
	 * @param lineFeature
	 */
	public void extractWordLevelFeatures(LineFeature lineFeature){
		int[] signalList = new int[5];
		for (String word : wordIndex.keySet()) {
			for (String s : Parameter.TYPESIGNALS)
				if (word.toLowerCase().startsWith(s))
					signalList[0] += wordIndex.get(word).size();// TYPE
			for (String s : Parameter.TITLEPOISON)
				if (word.toLowerCase().startsWith(s))
					signalList[1] += wordIndex.get(word).size();// TITLEMINUS
			for (String s : Parameter.AUTHORPOISON)
				if (word.toLowerCase().startsWith(s))
					signalList[2] += wordIndex.get(word).size();// AUTHORMINUS
			for (String s : Parameter.AFFILIATIONSIGNALS)
				if (word.toLowerCase().startsWith(s))
					signalList[3] += wordIndex.get(word).size();// AFFILIATION
			for (String s : Parameter.CONTACTSIGANALS)
				if (word.toLowerCase().startsWith(s))
					signalList[4] += wordIndex.get(word).size();// CONTACT
		}

		for (char ch : charIndex.keySet())
			if (ch == '@')
				signalList[4] += charIndex.get(ch).size();

		for (int signal : signalList)
			lineFeature.addFeature(signal);
	}

	/**
	 * Extract Derived features such as alignment and margin
	 * @param lineFeature
	 * @param lastLineFeature
	 */
	public void extractDerivedFeatures(LineFeature lineFeature, LineFeature lastLineFeature){
		float align = lineFeature.getFeature(Feature.LEFT.id())
		        + lineFeature.getFeature(Feature.WIDTH.id()) / 2
		        - lineFeature.getFeature(Feature.PAGEWIDTH.id()) / 2;
		lineFeature.addFeature(align);// ALIGNMENT, right(+)|left(-)
		float margin = -1;
		if (lastLineFeature == null)
			margin = lineFeature.getFeature(Feature.TOP.id());
		else
			margin = lineFeature.getFeature(Feature.TOP.id())
			        - lastLineFeature.getFeature(Feature.TOP.id())
			        - lineFeature.getFeature(Feature.HEIGHT.id());
		lineFeature.addFeature(margin);// MARGIN
	}

	/**
	 * Extract subhead feature such as abstract, intro
	 * @param lineFeature
	 */
	public void extractSubheadFeature(LineFeature lineFeature){
		lineFeature.addFeature(0);

		if (wordBegin == null) {
			lineFeature.setFeature(Feature.SUBHEAD.id(), -1);
			return;
		}

		float icl = lineFeature.getFeature(Feature.BEGINWORDICL.id());
		wordBegin = wordBegin.toLowerCase();
		if (icl == 1) {
			if (wordBegin.startsWith("abstract") || wordBegin.startsWith("summary"))
				lineFeature.setFeature(Feature.SUBHEAD.id(), 2);// abstract
			else if (wordBegin.startsWith("intro") || wordBegin.startsWith("content")
			        || wordBegin.startsWith("section"))// main body
				lineFeature.setFeature(Feature.SUBHEAD.id(), 1);
		}
	}

	/**
	 * Add score features: 
	 * TYPESCORE, TITLESCORE, AUTHORSCORE, 
	 * AFFILIATIONSCORE, CONTACTSCORE, 
	 * BODYSCORE and FIELDID
	 * @param lineFeature
	 */
	public void addScoreFeatures(LineFeature lineFeature){
		for (int i = 0; i < 6; i++)
			lineFeature.addFeature(0);
		// indeterminate/undetermined -1
		lineFeature.addFeature(-1);
	}

	public static void main(String[] args) throws IOException{
		String xml = "resource/clon.xml";
		XMLParser xmlParser = new XMLParser(xml);
		List<LineEntry> pageEntry = null;
		pageEntry = xmlParser.parseFirstPage();

		FeatureExtractor fe = new FeatureExtractor(pageEntry);
		fe.width = xmlParser.width;
		fe.height = xmlParser.height;
		fe.extractFeatures();
		for (LineFeature lineFeature : fe.pageFeature)
			System.out.println(lineFeature);
	}
}
