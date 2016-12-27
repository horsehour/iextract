package com.horsehour.search.word2vec;

import java.util.List;
import java.util.Random;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年4月28日 下午12:30:21
 **/
public class WordNeuron extends HuffmanNeuron {
	private String name;
	private List<HuffmanNode> pathNeurons;

	public WordNeuron(String name, int freq, int vectorSize) {
		super(freq, vectorSize);
		this.name = name;
		Random random = new Random();
		for (int i = 0; i < vector.length; i++) {
			vector[i] = (random.nextDouble() - 0.5) / vectorSize;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HuffmanNode> getPathNeurons() {
		if (pathNeurons != null) {
			return pathNeurons;
		}
		pathNeurons = HuffmanTree.getPath(this);

		return pathNeurons;
	}
}
