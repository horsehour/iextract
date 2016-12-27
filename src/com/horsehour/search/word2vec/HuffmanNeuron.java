package com.horsehour.search.word2vec;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年4月28日 下午12:23:56
 **/
public class HuffmanNeuron implements HuffmanNode {
	protected HuffmanNode parentNeuron;
	protected double[] vector;
	protected int frequency = 0;
	protected int code = 0;

	public HuffmanNeuron(int freq, int vectorSize) {
		this.frequency = freq;
		this.vector = new double[vectorSize];
		parentNeuron = null;
	}

	@Override
	public void setCode(int c) {
		code = c;
	}

	@Override
	public void setFrequency(int freq) {
		frequency = freq;
	}

	@Override
	public int getFrequency() {
		return frequency;
	}

	@Override
	public void setParent(HuffmanNode parent) {
		parentNeuron = parent;
	}

	@Override
	public HuffmanNode getParent() {
		return parentNeuron;
	}

	@Override
	public HuffmanNode merge(HuffmanNode right) {
		HuffmanNode parent;
		parent = new HuffmanNeuron(frequency + right.getFrequency(),
		        vector.length);
		parentNeuron = parent;
		this.code = 0;
		right.setParent(parent);
		right.setCode(1);
		return parent;
	}

	@Override
	public int compareTo(HuffmanNode hn) {
		if (frequency > hn.getFrequency()) {
			return 1;
		}
		return -1;
	}
}
