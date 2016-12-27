package com.horsehour.search.pdf;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.horsehour.util.TickClock;

/**
 * 抓取Arxiv网站的论文数据
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20110701
 */
public class ArxivCrawler {
	private List<String> urls;
	private List<String> subjects;
	private Map<String, List<String>> urlsMap;
	private String baseUrl;
	private final String rootPath;
	private boolean queryCall;
	private boolean isMapUrl;

	public ArxivCrawler(List<String> urls, String rootPath) {
		this.urls = urls;
		this.rootPath = rootPath;
	}

	/**
	 * 包含学科信息的url
	 * 
	 * @param urlsMap
	 * @param rootPath
	 */
	public ArxivCrawler(Map<String, List<String>> urlsMap, String rootPath) {
		this.urlsMap = urlsMap;
		this.rootPath = rootPath;
		this.isMapUrl = true;
	}

	/**
	 * 调用query下载页面
	 * 
	 * @param baseUrlStr
	 * @param rootPath
	 */
	public ArxivCrawler(String baseUrlStr, String rootPath) {
		this.baseUrl = baseUrlStr;
		this.rootPath = rootPath;
		this.queryCall = true;
	}

	/**
	 * 启动抓取程序
	 */
	public void start(){
		if (queryCall) {
			loadSubjects();
			for (int i = 0; i < subjects.size(); i++) {
				String queryUrl = queryConstruct(subjects.get(i), 0, 100);
				getFormatedContent(queryUrl, rootPath + "/xml/" + subjects.get(i));
			}
		}

		if (isMapUrl) {
			Set<String> subjects = urlsMap.keySet();
			for (String subject : subjects) {
				File path = new File(rootPath + "/paper/" + subject);
				if (!path.exists())
					path.mkdir();

				List<String> paperUrls = urlsMap.get(subject);
				// 只下载每个主题下的10篇
				for (int i = 0; i < 10; i++) {
					String url = paperUrls.get(i);
					getFormatedContent(url, rootPath + "/paper/" + subject + "/" + savedName(url));
				}

				System.out.println("Have Downloaded 10 papers on " + subject);
			}

		} else {
			for (int i = 0; i < urls.size(); i++) {
				String url = urls.get(i);
				getFormatedContent(url, rootPath + "/paper/" + savedName(url));
			}
		}
	}

	/**
	 * 载入学科列表
	 */
	private void loadSubjects(){
		subjects = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(rootPath + "//subjects.txt"));
			String line = "";
			while ((line = reader.readLine()) != null) {
				int idx = line.indexOf(" ");
				subjects.add(line.substring(0, idx).trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 遍历学科,构造查询语句
	 * 
	 * @param subject
	 * @param start
	 * @param max
	 * @return 查询语句
	 */
	private String queryConstruct(String subject, int start, int max){
		String url = baseUrl + "api/query?search_query=cat:" + subject + "&start=" + start
		        + "&max_results=" + max;

		return url;
	}

	/**
	 * 修改文件保存的名称
	 * 
	 * @param urlSource
	 * @return
	 */
	private String savedName(String urlSource){
		int index = urlSource.lastIndexOf("/");
		String dest = urlSource.substring(index + 1);
		dest = dest.replaceFirst("\\.", "-");

		return dest;
	}

	/**
	 * 抓取（核心,应用于各种格式文件的抓取）
	 * 
	 * @param urlSource
	 * @param destPath
	 */
	private void getFormatedContent(String urlSource, String destPath){
		URL url = null;

		try {
			url = new URL(urlSource);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);

			// 设置用户代理
			// Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.3
			// (KHTML, like Gecko)
			// Chrome/6.0.472.33 Safari/534.3 SE 2.X MetaSr 1.0

			conn.setRequestProperty("User-Agent", "Safari/534.3");
			DataInputStream input = new DataInputStream(conn.getInputStream());
			DataOutputStream output = null;
			output = new DataOutputStream(new FileOutputStream(destPath, false));
			byte[] container = new byte[1024];
			int bytes = 0;
			while ((bytes = input.read(container)) > 0)
				output.write(container, 0, bytes);

			output.flush();
			output.close();
			input.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		System.out.println(url + "文件保存完毕！");
	}

	public void getLost(){
		loadSubjects();
	}

	public float getInvert(int i){
		return 1.0f / i;
	}

	public static void main(String[] args) throws IOException{
		String rootPath = "F://SystemBuilding/Data/arXiv/", xmlPath = rootPath + "xml/", destPath = rootPath
		        + "paper/KeyPoints/";

		ArxivParser arxivParser = new ArxivParser(xmlPath, destPath);

		TickClock.beginTick();

		arxivParser.run();// 解析xml文件

		System.out.println("Parsing...");
		TickClock.stopTick();

		Map<String, List<String>> urls = arxivParser.getPaperList();

		ArxivCrawler arxivCrawler = new ArxivCrawler(urls, rootPath);

		TickClock.beginTick();

		arxivCrawler.start();// 开始抓取

		System.out.println("Download...");
		TickClock.stopTick();
	}
}