package com.horsehour.search.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.horsehour.util.TickClock;

/**
 * @author Chunheng Jiang
 * @version 3.0
 * @since Apr. 25 2014
 */
public class MetaDataScorer {
	public List<Float> simNL;

	public MetaDataScorer() {}

	/**
	 * Compute similarity between near lines
	 * @param pageFeature
	 */
	public void computeSimNL(List<LineFeature> pageFeature){
		simNL = new ArrayList<Float>();

		int fid = -1;

		LineFeature line, nextLine = null;
		for (int i = 0; i < pageFeature.size() - 1; i++) {
			line = pageFeature.get(i);
			nextLine = pageFeature.get(i + 1);
			float sim = 0;

			fid = Feature.LEFT.id();
			if (line.getFeature(fid) == nextLine.getFeature(fid))
				sim++;

			fid = Feature.WIDTH.id();
			if (line.getFeature(fid) == nextLine.getFeature(fid))
				sim++;

			fid = Feature.HEIGHT.id();
			if (line.getFeature(fid) == nextLine.getFeature(fid))
				sim++;

			fid = Feature.LENGTH.id();
			if (line.getFeature(fid) == nextLine.getFeature(fid))
				sim++;

			fid = Feature.FONTID.id();
			if (line.getFeature(fid) == nextLine.getFeature(fid))
				sim++;

			fid = Feature.ALIGNMENT.id();
			if (line.getFeature(fid) == nextLine.getFeature(fid))
				sim++;

			fid = Feature.MARGIN.id();
			float coef = nextLine.getFeature(fid);
			if (coef < 0)// strengthen the cohension a lot
				sim++;
			else
				sim += Math.exp(-nextLine.getFeature(fid));// (0,1]

			fid = Feature.SENTENCEBREAK.id();
			// break to the next
			if (line.getFeature(fid) == 3)
				sim++;

			float nextFeature = nextLine.getFeature(fid);
			// superscript or break to the last
			if (nextFeature == 1 || nextFeature == 2)
				sim++;

			fid = Feature.SUBHEAD.id();
			if (line.getFeature(fid) > 0)
				sim++;

			simNL.add(sim);
		}
	}

	/**
	 * Score line in terms of field scores with relevant features 
	 * @param pageFeature
	 */
	public void scoreField(List<LineFeature> pageFeature){
		for (LineFeature lineFeature : pageFeature) {
			scoreField(lineFeature);
		}

		int fid = Feature.FIELDID.id();
		int bodyId = Feature.BODYSCORE.id();
		float fieldId = -1;

		LineFeature lineFeature, nextFeature;
		for (int i = 0; i < pageFeature.size() - 1; i++) {
			lineFeature = pageFeature.get(i);
			nextFeature = pageFeature.get(i + 1);

			fieldId = lineFeature.getFeature(fid);
			if (fieldId == -1)
				scoreField(lineFeature, nextFeature);
			else if (fieldId == bodyId)// fix all following
				nextFeature.setFeature(fid, bodyId);
		}
	}

	/**
	 * Score line in terms of field scores with relevant features 
	 * @param lineFeature
	 */
	public void scoreField(LineFeature lineFeature){
		int fid = -1;
		float score = 0;

		fid = Feature.TYPE.id();
		score = lineFeature.getFeature(fid);
		lineFeature.setFeature(Feature.TYPESCORE.id(), score);

		fid = Feature.TITLEMINUS.id();
		score = lineFeature.getFeature(fid);
		lineFeature.setFeature(Feature.TITLESCORE.id(), -score);

		fid = Feature.AUTHORMINUS.id();
		score = lineFeature.getFeature(fid);
		lineFeature.setFeature(Feature.AUTHORSCORE.id(), -score);

		fid = Feature.CONTACT.id();
		score = lineFeature.getFeature(fid);
		lineFeature.setFeature(Feature.CONTACTSCORE.id(), score);
		if (score > 0)
			lineFeature.setFeature(Feature.FIELDID.id(), Feature.CONTACTSCORE.id());

		fid = Feature.AFFILIATION.id();
		score = lineFeature.getFeature(fid);
		lineFeature.setFeature(Feature.AFFILIATIONSCORE.id(), score);
		if (score >= 2)
			lineFeature.setFeature(Feature.FIELDID.id(), Feature.AFFILIATIONSCORE.id());

		fid = Feature.SUBHEAD.id();
		score = lineFeature.getFeature(fid);
		lineFeature.setFeature(Feature.BODYSCORE.id(), score);
		if (score == 2)
			lineFeature.setFeature(Feature.FIELDID.id(), Feature.BODYSCORE.id());// determined/fixed

//		lineFeature.addFeature(Feature.AUTHORSCORE.id(), score);// subscript -1
//		lineFeature.addFeature(Feature.AFFILIATIONSCORE.id(), score);
//		lineFeature.addFeature(Feature.CONTACTSCORE.id(), score);

//		fid = Feature.ALIGNMENT.id();
//		score = lineFeature.getFeature(fid);
//		score = (float) Math.exp(-Math.abs(score));// (0,1]
//		lineFeature.addFeature(Feature.TITLESCORE.id(), score);
//		lineFeature.addFeature(Feature.BODYSCORE.id(), score);

		fid = Feature.MAXMINFONT.id();
		score = lineFeature.getFeature(fid);// max + 1, min -1
		lineFeature.addFeature(Feature.TITLESCORE.id(), score);
	}

	/**
	 * @param lineFeature
	 * @param nextLineFeature
	 */
	public void scoreField(LineFeature lineFeature, LineFeature nextLineFeature){

		int fid = -1;
		float score = 0;

		fid = Feature.WORDCOUNT.id();
		score = lineFeature.getFeature(fid);

		float aut = 0;
		if (score == 0 && lineFeature.getFeature(Feature.DIGITCOUNT.id()) > 0) {
			if (lineFeature.getFeature(Feature.SENTENCEBREAK.id()) == 1)
				aut += 1.5f;
		} else if (score == 2 || score == 3) {
			aut += 0.5f;
			if (lineFeature.getFeature(Feature.UPPERCASECOUNT.id()) == score)
				aut += 0.3f;

			float breakscore = nextLineFeature.getFeature(Feature.SENTENCEBREAK.id());
			if (breakscore == 1)
				aut += 1.5f;
			else if (breakscore == 2) {
				aut += 1.0f;
				nextLineFeature.addFeature(Feature.AUTHORSCORE.id(), 1.0f);
			}

			breakscore = lineFeature.getFeature(Feature.SENTENCEBREAK.id());
			if (breakscore == 2)
				aut += 1.0f;
		}
		lineFeature.addFeature(Feature.AUTHORSCORE.id(), aut);
		if (aut >= 1)
			lineFeature.setFeature(Feature.FIELDID.id(), Feature.AUTHORSCORE.id());
	}

	/**
	 * Score line in terms of field scores with relevant features 
	 * @param pageFeature
	 */
	public void scoreField(List<LineFeature> pageFeature, int span){
		LineFeature lineFeature;
		List<LineFeature> spanFeature;
		for (int i = 0; i <= pageFeature.size() - span; i++) {
			lineFeature = pageFeature.get(i);
			spanFeature = new ArrayList<LineFeature>(span);
			for (int k = 0; k <= span; k++)
				spanFeature.add(pageFeature.get(i + k + 1));
			scoreField(lineFeature, spanFeature);
		}
	}

	/**
	 * Shift window
	 * @param lineFeature
	 * @param spanFeature
	 */
	public void scoreField(LineFeature lineFeature, List<LineFeature> spanFeature){

	}

	public static void main(String[] args) throws IOException{
		TickClock.beginTick();

		String xml = "resource/comment.xml";
		XMLParser xmlParser = new XMLParser(xml);
		List<LineEntry> pageEntry = xmlParser.parseFirstPage();

		FeatureExtractor fe = new FeatureExtractor(pageEntry);
		fe.width = xmlParser.width;
		fe.height = xmlParser.height;
		fe.extractFeatures();

		MetaDataScorer scorer = new MetaDataScorer();
//		scorer.computeSimNL(fe.pageFeature);

//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < pageEntry.size() - 1; i++)
//			sb.append(scorer.simNL.get(i) + "\t" + pageEntry.get(i).text + "\r\n");
//		System.out.println(sb.toString());

		scorer.scoreField(fe.pageFeature);
		for (LineFeature lineFeature : fe.pageFeature) {
			for (int k = Feature.TYPESCORE.id(); k < lineFeature.dim(); k++)
				System.out.print(lineFeature.getFeature(k) + "\t");
			System.out.println(lineFeature.text);
		}

		TickClock.stopTick();
	}
}
