package com.horsehour.search.word2vec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年4月28日 下午12:16:55
 **/
public class Counter<T> {
	private Map<T, CountInteger> hm;

	public Counter() {
		hm = new HashMap<T, CountInteger>();
	}

	public Counter(int initialCapacity) {
		hm = new HashMap<T, CountInteger>(initialCapacity);
	}

	public class CountInteger {
		private int count;

		public CountInteger(int initCount) {
			count = initCount;
		}

		public void set(int num) {
			count = num;
		}

		public int value() {
			return count;
		}

		@Override
		public String toString() {
			return "Count: " + String.valueOf(count);
		}
	}

	public void add(T t, int n) {
		CountInteger newCount = new CountInteger(n);
		CountInteger oldCount = hm.put(t, newCount);
		if (oldCount != null) {
			newCount.set(oldCount.value() + 1);
		}
	}

	public void add(T t) {
		add(t, 1);
	}

	public int get(T t) {
		CountInteger count = hm.get(t);
		if (count == null) {
			return 0;
		} else {
			return count.value();
		}
	}

	public int size() {
		return hm.size();
	}

	public void remove(T t) {
		hm.remove(t);
	}

	public Set<T> keySet() {
		return hm.keySet();
	}

	@Override
	public String toString() {
		Iterator<Entry<T, CountInteger>> iterator = hm.entrySet().iterator();
		StringBuilder sb = new StringBuilder();
		Entry<T, CountInteger> next = null;
		while (iterator.hasNext()) {
			next = iterator.next();
			sb.append(next.getKey());
			sb.append("\t");
			sb.append(next.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}