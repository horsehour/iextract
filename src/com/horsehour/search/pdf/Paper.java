package com.horsehour.search.pdf;

import java.util.List;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since Oct 26 2013
 * @see http://aye.comp.nus.edu.sg/parsCit/genericSect.tagged.txt
 */
public class Paper {
	public String category, id, title, date, summary, keywords, content;
	public String[] authors;
	public int pages;
	public List<Reference> references;
}
