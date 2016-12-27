package com.horsehour.search.word2vec;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年4月28日 下午12:14:53
 **/
public interface HuffmanNode extends Comparable<HuffmanNode> {

	public void setCode(int c);

	public void setFrequency(int freq);

	public int getFrequency();

	public void setParent(HuffmanNode parent);

	public HuffmanNode getParent();

	public HuffmanNode merge(HuffmanNode sibling);
}
