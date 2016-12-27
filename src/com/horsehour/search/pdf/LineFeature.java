package com.horsehour.search.pdf;

import java.util.ArrayList;
import java.util.List;

/**
 * Line Features or Signals
 * @author Chunheng Jiang
 * @version 1.0
 * @since 31st Dec. 2015 pm 5:05:36
 **/
public class LineFeature {
	public String text;
	public List<Float> features;

	public LineFeature() {
		features = new ArrayList<Float>();
		text = "";
	}

	public LineFeature(List<Float> features, String text) {
		this.features = features;
		this.text = text;
	}

	public int dim(){
		return features.size();
	}

	public void addFeature(float val){
		features.add(val);
	}

	public float getFeature(int id){
		return features.get(id);
	}

	public List<Float> getSubFeature(int i, int j){
		return features.subList(i, j);
	}

	public void setFeature(int id, float val){
		features.set(id, val);
	}

	public void addFeature(int id, float val){
		features.set(id, val + features.get(id));
	}

	public float plus(int id1, int id2){
		return features.get(id1) + features.get(id2);
	}

	public float minus(int id1, int id2){
		return features.get(id1) - features.get(id2);
	}

	public float multiply(int id1, int id2){
		return features.get(id1) + features.get(id2);
	}

	public float divide(int id1, int id2){
		return features.get(id1) / features.get(id2);
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for (float f : features)
			sb.append(f + "\t");
		sb.append(text);
		return sb.toString();
	}
}