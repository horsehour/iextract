package com.horsehour.search.pdf;

import java.util.List;

/**
 * Arxiv文档
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20110701
 * @see http://aye.comp.nus.edu.sg/parsCit/genericSect.tagged.txt
 */
public class ArxivPaper {
	public String id, updated, published, title, summary, pageUrl, paperUrl;

	public List<String> authors;

	public ArxivPaper(String id, String updated, String published,
	        String title, String summary, List<String> authors, String pageUrl,
	        String paperUrl) {

		this.id = id;
		this.updated = updated;
		this.published = published;
		this.title = title;
		this.summary = summary;
		this.authors = authors;
		this.pageUrl = pageUrl;
		this.paperUrl = paperUrl;
	}

	/**
	 * 添加作者
	 * 
	 * @param author
	 */
	public void addAuthor(String author) {
		authors.add(author);
	}

	@Override
	public String toString() {
		int sz = authors.size();
		String strAuthor = authors.get(0);

		if (sz > 1) {
			for (int i = 1; i < sz; i++)
				strAuthor += ("," + authors.get(i));
		}

		return "id=" + id + "\ntitle=" + title + "\nsummary=" + summary
		        + "\nauthors=" + strAuthor + "\nupdated=" + updated
		        + "\npublished=" + published + "\npageUrl=" + pageUrl
		        + "\npaperUrl=" + paperUrl + "\n\n";
	}
}
