package com.horsehour.search.word2vec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年4月28日 下午12:22:55
 **/
public class HuffmanTree {
	public static void make(Collection<? extends HuffmanNode> nodes) {
		TreeSet<HuffmanNode> tree = new TreeSet<HuffmanNode>(nodes);
		while (tree.size() > 1) {
			HuffmanNode left = tree.pollFirst();
			HuffmanNode right = tree.pollFirst();
			HuffmanNode parent = left.merge(right);
			tree.add(parent);
		}
	}

	public static List<HuffmanNode> getPath(HuffmanNode leafNode) {
		List<HuffmanNode> nodes = new ArrayList<HuffmanNode>();
		for (HuffmanNode hn = leafNode; hn != null; hn = hn.getParent())
			nodes.add(hn);

		Collections.reverse(nodes);
		return nodes;
	}
}
