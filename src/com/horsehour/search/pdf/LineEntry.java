package com.horsehour.search.pdf;

import java.util.Arrays;

import com.horsehour.util.MathUtils;

/**
 * <li>Data: line number, left, top, width, height, font id, font size
 * <li>Scores: title, author, abstract, keywords, unknown
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since Oct 27 2013
 */
public class LineEntry implements Cloneable {
	public int dim = 0;
	public int[] data;
	public String text;

	public float[] scores;

	public int maxId;
	public int blockId;

	public LineEntry() {
		text = "";
		maxId = -1;
		scores = new float[5];
	}

	public LineEntry(int[] data, String text) {
		this();
		dim = data.length;
		this.data = Arrays.copyOf(data, dim);
		this.text = text;
	}

	public int getDatum(int id){
		return data[id];
	}

	/**
	 * field id with the highest scores
	 */
	public void classify(){
		int[] rank = MathUtils.getRank(scores, false);
		maxId = rank[0];
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(maxId + "\t");
		for (int i = 0; i < dim; i++)
			sb.append(data[i] + "\t");
		return sb.toString() + text;
	}

	@Override
	public LineEntry clone(){
		LineEntry o = null;
		try {
			o = (LineEntry) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}
}
