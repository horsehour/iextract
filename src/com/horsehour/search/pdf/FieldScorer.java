package com.horsehour.search.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.horsehour.util.MathUtils;
import com.horsehour.util.TickClock;

/**
 * @author Chunheng Jiang
 * @version 3.0
 * @since Apr. 25 2014
 */
public class FieldScorer {
	private boolean simple = false;
	private boolean block = false;
	private boolean cluster = false;

	public FieldScorer() {}

	private void initScore(LineEntry entry){
		Arrays.fill(entry.scores, Parameter.WT_BASELINE);
	}

	/**
	 * score document in naive style
	 * 
	 * @param entries
	 */
	public void simpleScore(List<LineEntry> entries){
		int nLines = entries.size();
		if (nLines == 0)
			return;

		for (int i = 0; i < nLines; i++) {
			initScore(entries.get(i));

			String content = entries.get(i).text;
			String sub = "";
			if (content.length() < Parameter.MAX_LEN_MATCH_ABS)
				sub = content;
			else {
				sub = content.substring(0, Parameter.MAX_LEN_MATCH_ABS).toLowerCase();
				float absScore = 0;
				if (sub.contains("abstract") || sub.contains("summary")) {
					absScore += Parameter.WT_ABS_KEYWORDS;
					if (sub.contains(":"))
						absScore += Parameter.WT_COLON_FOLLOW_ABS;

					for (int j = i; j < nLines; j++) {
						initScore(entries.get(j));

						entries.get(j).scores[2] += absScore;

						// make sure the following lines belong to the same
						// block
						entries.get(j).data[6] = 0;
						entries.get(j).data[5] = Parameter.MAX_FONTSIZE;
					}

					entries.get(nLines - 1).data[6] = Parameter.MAX_MARGIN;
					return;
				}
			}

			// /////////////////////////////////////////////////////////
			scoreTitle(entries.get(i), i);
			scoreAuthor(entries.get(i));
			scoreNoise(entries.get(i));
			scoreAbstract(entries.get(i));
			scoreKeywords(entries.get(i));

			entries.get(i).classify();
		}
	}

	/**
	 * score document in block style
	 * 
	 * @param entries
	 */
	public void blockScore(List<LineEntry> entries){
		simpleScore(entries);
		removeNoise(entries);
		adjustMargin(entries);

		// sortGene(geneList);//1.based on scores
		marginBasedCluster(entries, 3);// 2.based on margins
		// fontBasedCluster(geneList,3);//3.based on fonts

		balance(entries);
	}

	/**
	 * @param entries
	 */
	public void sort(List<LineEntry> entries){
		// assuming the first line is the title paper
		if (entries.get(0).maxId != 0 && entries.get(0).scores[2] < 20) {
			entries.get(0).scores[0] += Parameter.WT_BOOST;
			entries.get(0).maxId = 0;
		}
		boolean hasAuthor = false;
		int size = entries.size();
		for (int i = 1; i < size; i++) {
			int maxId = entries.get(i).maxId;

			if (maxId == 1)
				hasAuthor = true;

			if (maxId == 0 && hasAuthor) {
				entries.get(i).scores[2] += Parameter.WT_BOOST;
				entries.get(i).maxId = 3;
			}

			if (hasAuthor && entries.get(i).maxId == 3 && i < size - 1) {
				for (int j = i + 1; j < size; j++) {
					entries.get(j).scores[2] += Parameter.WT_BOOST;
					entries.get(j).maxId = 3;
				}

				break;
			}
		}
	}

	/**
	 * 根据字体对document gene列表聚类
	 * 
	 * @param geneList
	 * @param nCluster
	 */
	private void marginBasedCluster(List<LineEntry> geneList, int nCluster){
		int size = geneList.size();
		float[] margin = new float[size];
		for (int i = 0; i < size; i++)
			margin[i] = geneList.get(i).getDatum(6);

		int[] rank = MathUtils.getRank(margin, false);
		List<Integer> boundary = new ArrayList<Integer>();
		boundary.add(-1);
		boundary.add(size - 1);

		int idx = 0;
		while (boundary.size() < nCluster + 1 && idx < size) {
			if (margin[rank[idx]] == Parameter.MAX_MARGIN) {
				idx++;
				continue;
			}

			boundary.add(rank[idx]);
			idx++;
		}

		rank = MathUtils.getRank(boundary, true);
		for (int k = 0; k < rank.length - 1; k++)
			for (int i = rank[k] + 1; i <= rank[k + 1]; i++)
				geneList.get(i).blockId = k;
	}

	/**
	 * 根据字体对document gene列表聚类
	 * 
	 * @param geneList
	 * @param nCluster
	 * @return uniform fonts
	 */
	public boolean fontBasedCluster(List<LineEntry> geneList, int nCluster){
		boolean isUniform = false;// the fonts may be uniformly distributed
		int count = 1;
		geneList.get(0).data[5] = 0;

		for (int i = 1; i < geneList.size(); i++) {
			if ((geneList.get(i).data[5] != geneList.get(i - 1).data[5]) && count < nCluster) {
				geneList.get(i).data[5] = geneList.get(i - 1).data[5] + 1;
				count++;
			} else
				geneList.get(i).data[5] = geneList.get(i - 1).data[5];
		}

		if (count == 1)
			isUniform = true;
		return isUniform;
	}

	/**
	 * 剔除噪声项
	 * 
	 * @param geneList
	 */
	private void removeNoise(List<LineEntry> geneList){
		int sz = geneList.size();
		if (sz <= 0)
			return;

		for (int i = 0; i < geneList.size(); i++) {
			if (geneList.get(i).maxId == 2) {
				if (i > 0)
					geneList.get(i - 1).data[6] += geneList.get(i).data[6];
				geneList.remove(i);
				i--;
			}
		}
	}

	/**
	 * 根据相邻条目的font-size特征调整margin特征
	 * 
	 * @param entries
	 */
	private void adjustMargin(List<LineEntry> entries){
		for (int i = 0; i < entries.size() - 1; i++) {
			if (entries.get(i).getDatum(5) != entries.get(i + 1).getDatum(5))
				entries.get(i).data[6] += entries.get(i + 1).data[6];
		}
	}

	/**
	 * 根据分块情况重新平衡各个gene的maxId
	 * 
	 * @param entries
	 */
	private void balance(List<LineEntry> entries){
		int nLine = entries.size();
		if (nLine <= 0)
			return;

		int status = 0;
		for (int i = 0; i < nLine; i++) {
			status = entries.get(i).blockId;

			entries.get(i).scores[4] = 0;
			if (status == 0) {
				entries.get(i).scores[0] += Parameter.WT_TITLE_AT_STATUS_0;
				entries.get(i).scores[1] += Parameter.WT_AUTHOR_AT_STATUS_0;
			}
			if (status == 1) {
				entries.get(i).scores[1] += Parameter.WT_AUTHOR_AT_STATUS_1;
				entries.get(i).scores[0] += Parameter.WT_TITLE_AT_STATIS_1;
			}
			if (status == 2) {
				entries.get(i).scores[2] += Parameter.WT_ABS_AT_STATUS_2;
				entries.get(i).scores[1] += Parameter.WT_AUTHOR_AT_STATUS_2;
				entries.get(i).scores[0] = 0;
			}
			entries.get(i).classify();
		}
	}

	/**
	 * score document gene w.r.t title
	 * 
	 * @param entry
	 */
	public void scoreTitle(LineEntry entry){
		String text = entry.text;

		float score = 0;
		if (countFilterTerm(entry.text) > 0 || lenBlockDigit(text) > 0
		        || countContactTerm(text) > 0) {
			entry.scores[0] = 0;
			return;
		}

		// In fact,the authors' name may contains "ing"
		if (text.contains("ing"))
			score += Parameter.WT_POSTFIX_ING;
		if (!text.contains(" ") && Parameter.THIN_MARGIN <= entry.getDatum(6)
		        && entry.getDatum(6) != Parameter.MAX_MARGIN)
			score += Parameter.WT_THIN_MARGIN;

		else if (entry.getDatum(3) > Parameter.ALPHA_WIDTH_TITLE * Parameter.PAGEWIDTH)
			score += Parameter.WT_ALPHA_WIDTH_TITLE;

		entry.scores[0] += score;
	}

	public void scoreTitle(LineEntry entry, int nLine){
		scoreTitle(entry);

		float score = 0;
		if (nLine < Parameter.EFFECT_LINENUM_TITLE)
			score += (Parameter.EFFECT_LINENUM_TITLE - nLine);

		entry.scores[0] += score;
	}

	/**
	 * score document gene w.r.t author
	 * 
	 * @param entry
	 */
	public void scoreAuthor(LineEntry entry){
		String text = entry.text;

		float score = 0;
		if (countFilterTerm(text) > 0 || countContactTerm(text) > 0 || lenBlockDigit(text) > 0) {
			entry.scores[1] = 0;
			return;
		}

		String[] subs = text.trim().split(" ");
		int nTerm = subs.length;
		if (1 <= nTerm && nTerm <= 10) {
			if (nTerm == 2 && text.length() <= 30) {
				char ch1 = subs[0].charAt(0);
				char ch2 = subs[1].charAt(0);
				if (Character.isAlphabetic(ch1) && Character.isAlphabetic(ch2))
					score += Parameter.WT_INIT;
			}

			for (int i = 0; i < nTerm; i++)
				for (int j = Parameter.AUTHORPOISON.length - 1; j >= 0; j--)
					if (subs[i].equalsIgnoreCase(Parameter.AUTHORPOISON[j])) {
						score += Parameter.WT_PREP_NEGATIVE;
						break;
					}

		}

		// ///////////////////////////////////////////////////
		// It's helpful to identify the noise like:. . . . .
		if (nTerm / text.length() >= Parameter.ALPHA_PERIOD_LINE)
			entry.scores[4] += Parameter.WT_ALPHA_PERIOD;

		// ///////////////////////////////////////////////////
		score += Parameter.BOOST_PERIOD_AUTHOR * countPeriod(text) + Parameter.BOOST_COMMA_AUTHOR
		        * countComma(text);

		int idx = -1;
		String and = " and ";
		if ((idx = text.indexOf(and)) == -1) {
		} else if (text.substring(idx + and.length()).length() > 0) {
			int ch = text.charAt(idx + and.length());
			if (Character.isAlphabetic(ch))
				score += Parameter.WT_INIT_FOLLOW_AND;
		}
		entry.scores[1] += score;
	}

	/**
	 * score document gene w.r.t abstract
	 * 
	 * @param entry
	 */
	public void scoreAbstract(LineEntry entry){
		float score = 0;

		if (entry.getDatum(3) > Parameter.ALPHA_WIDTH_ABSTRACT * Parameter.PAGEWIDTH
		        && entry.getDatum(1) > Parameter.ALPHA_HEIGHT_ABSTRACT * Parameter.PAGEHEIGHT)
			score += Parameter.WT_POSITION_ABSTRACT;

		score += entry.getDatum(1) * Parameter.BOOST_TOP_TO_HEIGHT_ABS / Parameter.PAGEHEIGHT;

		String text = entry.text;
		score += countFilterTerm(text);
		score += Parameter.BOOST_CONTACT_NEGATIVE * countContactTerm(text);
		score += Parameter.BOOST_PERIOD_NEGATIVE * countPeriod(text);
		entry.scores[2] += score;
	}

	/**
	 * score document gene w.r.t keywords
	 * 
	 * @param entry
	 */
	public void scoreKeywords(LineEntry entry){
		String line = entry.text;

		// digits are negative factors for keywords
		if (countContactTerm(line) > 0 || countFilterTerm(line) > 0 || lenBlockDigit(line) > 0) {
			entry.scores[3] = 0;
			return;
		}

		float score = 1.0f * Parameter.BOOST_TOP_TO_HEIGHT_KEY
		        * (entry.getDatum(1) / Parameter.PAGEHEIGHT);

		score += countComma(line);
		entry.scores[3] += score;
	}

	/**
	 * score document gene w.r.t noise information
	 * 
	 * @param entry
	 */
	public void scoreNoise(LineEntry entry){
		String text = entry.text;
		int len = lenBlockDigit(text);

		float score = 0;
		if (countContactTerm(text) > 0 && text.contains(" of ") && len > 0)
			score += Parameter.WT_CONTACT_BLOCKDIGIT;
		else if (countContactTerm(text) > 0)
			score += Parameter.WT_CONTACT;
		else if (len > 0)
			score += len;

		score -= countFilterTerm(text);
		entry.scores[4] += score;
	}

	/**
	 * @param text
	 * @return statistics of filter terms
	 */
	public int countFilterTerm(String text){
		int count = 0;
		int len = Parameter.TITLEPOISON.length;
		for (int i = 0; i < len; i++)
			if (text.contains(Parameter.TITLEPOISON[i]))
				count++;

		return count;
	}

	/**
	 * contact information
	 * 
	 * @param text
	 * @return statistics of contect terms
	 */
	public int countContactTerm(String text){
		int count = 0;
		int len = Parameter.CONTACT_WORDS.length;
		for (int i = 0; i < len; i++)
			if (text.contains(Parameter.CONTACT_WORDS[i]))
				count++;

		return count;
	}

	/**
	 * 数字串最大长度
	 * 
	 * @param text
	 * @return length of the longest digit sequences
	 */
	public int lenBlockDigit(String text){
		int size = text.length();

		int maxLen = 0;
		for (int i = 0; i < size;) {
			int len = 0;
			while (i < size && Character.isDigit(text.charAt(i))) {
				len++;
				i++;
			}

			if (len > maxLen)
				maxLen = len;

			i++;
		}

		return maxLen;
	}

	/**
	 * statisitc of valid periods for author-field
	 * 
	 * @param text
	 * @return number of valid periods
	 */
	public int countPeriod(String text){
		int size = text.length();
		int count = 0;
		if (size <= 2)
			count = 0;

		for (int i = 1; i < size; i++)
			if (text.charAt(i) == '.' && Character.isAlphabetic(text.charAt(i - 1)))
				count++;
		return count;
	}

	/**
	 * 统计有效的逗号数目
	 * 
	 * @param text
	 * @return number of valid commas in text
	 */
	public int countComma(String text){
		int size = text.length();
		int count = 0;
		for (int i = 0; i < size; i++)
			if (text.charAt(i) == ',')
				count++;
		return count;
	}

	public boolean isNaive(){
		return simple;
	}

	public void setNaive(boolean naive){
		this.simple = naive;
	}

	public boolean isBlock(){
		return block;
	}

	public void setBlock(boolean block){
		this.block = block;
	}

	public boolean isCluster(){
		return cluster;
	}

	public void setCluster(boolean cluster){
		this.cluster = cluster;
	}

	public static void main(String[] args) throws IOException{
		TickClock.beginTick();

		String src = "resource/comment.xml";
		XMLParser metaEx = new XMLParser(src);
		List<LineEntry> entries = metaEx.parseFirstPage();

		FieldScorer fieldScorer = new FieldScorer();
//		fieldScorer.blockScore(entries);

		fieldScorer.simpleScore(entries);
		int count = 0;
		for (LineEntry entry : entries) {
			if (count > 5)
				break;

			if (entry.maxId == 0)
				System.out.println("Title:\t" + entry.text);
			else if (entry.maxId == 1)
				System.out.println("Author:\t" + entry.text);
			count++;
		}

		TickClock.stopTick();
	}
}
