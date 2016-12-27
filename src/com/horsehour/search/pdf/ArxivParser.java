package com.horsehour.search.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.horsehour.util.TickClock;

/**
 * 解析原始xml形式的arxiv文档
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20110702
 */
public class ArxivParser {
	private final String xmlPath;
	private final String destPath;

	private int totalResults;
	private String date;
	private String subject;

	private String id;
	private String updated;
	private String published;
	private String title;
	private String summary;
	private List<String> authors;
	private String pageUrl;
	private String paperUrl;

	// 提取paper链接url,map的key是subject,value是urls
	private Map<String, List<String>> paperList;
	private ArxivPaper paper;

	public ArxivParser(String xmlPath, String destPath) {
		this.xmlPath = xmlPath;
		this.destPath = destPath;
	}

	/**
	 * 遍历目录下所有xml
	 * @throws IOException 
	 */
	public void run() throws IOException{
		paperList = new HashMap<String, List<String>>();
		for (File file : FileUtils.listFiles(new File(xmlPath), null, false))
			parseXml(file);
	}

	/**
	 * 取得paper的url列表
	 * 
	 * @return
	 */
	public Map<String, List<String>> getPaperList(){
		return paperList;
	}

	/**
	 * 解析xml
	 * 
	 * @param category
	 * @throws IOException 
	 */
	private void parseXml(File category) throws IOException{
		this.subject = category.getName();
		List<String> valueUrls = new ArrayList<String>();
		paperList.put(subject, valueUrls);

		String entries = FileUtils.readFileToString(category,"");
		String[] entry = entries.split("<entry>");
		parseHeader(entry[0]);
		String content = "subject=" + subject + "\ntotalResults=" + totalResults + "\ndate=" + date + "\n\n";
		FileUtils.write(new File(destPath + subject), content,",");
		for (int i = 1; i < entry.length; i++) {
			parseEntry(entry[i]);
			valueUrls.add(paperUrl);// 向valueUrls中添加paper的url
			FileUtils.write(new File(destPath + subject), paper.toString(),"");
		}
	}

	/**
	 * 解析头部,提取totalResults,date
	 * 
	 * @param src
	 */
	private void parseHeader(String src){
		int start = src.indexOf("<updated>");
		src = src.substring(start + 9);
		int end = src.indexOf("</updated>");
		date = src.substring(0, end);
		start = src.indexOf("\">");
		end = src.indexOf("</opensearch:");
		totalResults = Integer.parseInt(src.substring(start + 2, end));
	}

	/**
	 * 根据code确定解析元素类型
	 * 
	 * @param sAuthor
	 */
	private void parseAuthor(String sAuthor){
		authors = new ArrayList<String>();
		String[] aus = sAuthor.split("</name>");
		for (String author : aus) {
			int idx = author.lastIndexOf(">");
			if (idx > 0)
				authors.add(author.substring(idx + 1).trim());
		}
	}

	/**
	 * 解析entry标签下的内容
	 * 
	 * @param src
	 */
	private void parseEntry(String src){
		String tagName = "id";
		int start = src.indexOf("<" + tagName);
		int end = src.indexOf("</" + tagName);
		id = src.substring(start + tagName.length() + 2, end).trim();
		src = src.substring(end);

		tagName = "updated";
		start = src.indexOf("<" + tagName);
		end = src.indexOf("</" + tagName);
		updated = src.substring(start + tagName.length() + 2, end).trim();
		src = src.substring(end);

		tagName = "published";
		start = src.indexOf("<" + tagName);
		end = src.indexOf("</" + tagName);
		published = src.substring(start + tagName.length() + 2, end).trim();
		src = src.substring(end);

		tagName = "title";
		start = src.indexOf("<" + tagName);
		end = src.indexOf("</" + tagName);
		title = src.substring(start + tagName.length() + 2, end).trim();
		src = src.substring(end);

		tagName = "summary";
		start = src.indexOf("<" + tagName);
		end = src.indexOf("</" + tagName);
		summary = src.substring(start + tagName.length() + 2, end).trim();
		src = src.substring(end);

		tagName = "author";
		start = src.indexOf("<" + tagName);
		end = src.lastIndexOf("</" + tagName);
		parseAuthor(src.substring(start + tagName.length() + 2, end));
		src = src.substring(end);

		tagName = "link href=";
		start = src.indexOf("<" + tagName);
		String url = src.substring(start + tagName.length() + 2);
		int idx = url.indexOf("\"");
		pageUrl = url.substring(0, idx).trim();

		tagName = "link title=\"pdf\" href=";
		start = src.indexOf("<" + tagName);
		url = src.substring(start + tagName.length() + 2);
		idx = url.indexOf("\"");
		paperUrl = url.substring(0, idx).trim();

		paper = new ArxivPaper(id, updated, published, title, summary, authors, pageUrl, paperUrl);
	}

	public static void main(String[] args) throws IOException{
		TickClock.beginTick();

		ArxivParser parser;
		parser = new ArxivParser("F://SystemBuilding/Data/arXiv/xml/", "F://SystemBuilding/Data/arXiv/arXivPaper/Info/");

		parser.run();

		Map<String, List<String>> urls = parser.paperList;
		Set<String> subjects = urls.keySet();
		for (String subject : subjects)
			System.out.println(urls.get(subject));

		TickClock.stopTick();
	}
}
