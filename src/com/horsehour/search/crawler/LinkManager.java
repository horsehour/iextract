package com.horsehour.search.crawler;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年5月3日 下午10:59:18
 **/
public interface LinkManager {
	/**
	 * Places the link in the queue
	 * 
	 * @param link
	 * @throws Exception
	 */
	void queueLink(String link);

	/**
	 * Returns the number of visited links
	 * 
	 * @return
	 */
	int size();

	/**
	 * Checks if the link was already visited
	 * 
	 * @param link
	 * @return
	 */
	boolean visited(String link);

	/**
	 * Marks this link as visited
	 * 
	 * @param link
	 */
	void addVisited(String link);
}