package com.horsehour.search.crawler.prof;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import com.horsehour.search.crawler.ZhiZhu;

public class LostImageExtractor {

	public static void extractFace(String name, String dept, String imgUrl){
		String fileName = "大连理工大学-" + dept + "-" + name + ".jpg";
		ZhiZhu.getBinary(imgUrl, "C://Users/dell/Desktop/Lost/" + fileName);
		System.out.println(name);
	}

	/**
	 * 提取头像链接
	 * 
	 * @param url
	 * @param name
	 * @param matchedList
	 */
	public static void extractLinks(String url, String name, HashMap<String, String> matchedList){
		NodeFilter nodeFilter = new TagNameFilter("img");
		ProfParser parser = new ProfParser(url, "gb2312");
		NodeList list = parser.getNodeList(nodeFilter);
		Node node = list.elementAt(1);
		String imgUrl = "";
		if (node instanceof ImageTag) {
			imgUrl = ((ImageTag) node).getAttribute("src");
			imgUrl = "http://math.dlut.edu.cn" + imgUrl;
			matchedList.put(name, imgUrl);
		}
	}

	/**
	 * 提取列表下的导师页面
	 * 
	 * @param lostList
	 * @param matchedList
	 */
	public static void getTutorPageUrls(HashMap<String, String> lostList, HashMap<String, String> matchedList){
		NodeFilter nodeFilter = new HasAttributeFilter("style", "float:left;width:18%;margin-right:10px;");

		ProfParser parser = new ProfParser("http://math.dlut.edu.cn/a/jiaoshixinxi/", "gb2312");
		NodeList list = parser.getNodeList(nodeFilter);
		Node node = null;
		String name = "", pageUrl = "";
		for (int ix = 0; ix < list.size(); ix++) {
			NodeList subList = list.elementAt(ix).getChildren();
			node = subList.elementAt(1);
			if (node instanceof LinkTag) {
				name = ((LinkTag) node).getLinkText();
				name = name.replaceAll("　", "");
				if (lostList.containsKey(name)) {
					pageUrl = ((LinkTag) node).extractLink();
					matchedList.put(name, pageUrl);
				}
			}
		}
	}

	/**
	 * 提取缺失头像的文件名称中的姓名（全部拷贝到Lost文件夹下）
	 * 
	 * @param fileName
	 * @param lostList
	 */
	public static void fillMap(String fileName, HashMap<String, String> lostList){
		String[] parts = fileName.split("-");
		String dept = parts[1];
		String name = parts[2].substring(0, parts[2].length() - 4);
		lostList.put(name, dept);
	}

	public static void statistic(File[] files){
		HashMap<String, Integer> lostDistribution = new HashMap<String, Integer>();
		String[] parts = null;
		String dept = "";
		int count = 0;
		for (File f : files) {
			parts = f.toString().split("-");
			dept = parts[1];
			if (lostDistribution.containsKey(dept)) {
				count = lostDistribution.get(dept);
				count++;
			} else
				count = 1;
			lostDistribution.put(dept, count);
		}
		Set<Map.Entry<String, Integer>> set = lostDistribution.entrySet();
		for (Map.Entry<String, Integer> entry : set) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}

	public static void main(String[] args){
		String path = "C://Users/dell/Desktop/Lost/";

		// 第一维为姓名，第二维是院系
		HashMap<String, String> lostList = new HashMap<String, String>();
		for (File file : FileUtils.listFiles(new File(path), null, false))
			fillMap(file.toString(), lostList);

		HashMap<String, String> matchedList = new HashMap<String, String>();
		getTutorPageUrls(lostList, matchedList);

		HashMap<String, String> matched = new HashMap<String, String>();
		Set<Map.Entry<String, String>> entries = matchedList.entrySet();
		for (Map.Entry<String, String> entry : entries) {
			extractLinks(entry.getValue(), entry.getKey(), matched);
		}

		Set<Map.Entry<String, String>> set = matched.entrySet();
		for (Map.Entry<String, String> entry : set) {
			String name = entry.getKey();
			extractFace(name, lostList.get(name), matched.get(name));
		}
	}
}
