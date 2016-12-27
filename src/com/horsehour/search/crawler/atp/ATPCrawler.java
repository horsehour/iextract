package com.horsehour.search.crawler.atp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Web Crawler
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20160326
 */
public class ATPCrawler {
	private static int READTIMELIMIT = 6000 * 100;
	private static int CONNECTTIMELIMIT = 6000 * 100;

	/**
	 * fetch web page with given length of content skipped
	 * 
	 * @param url
	 * @param enc
	 * @return content of web page
	 */
	public static String get(String url, String enc){
		StringBuffer sb = new StringBuffer();
		HttpURLConnection huc = null;
		try {
			huc = (HttpURLConnection) new URL(url).openConnection();
			huc.setConnectTimeout(CONNECTTIMELIMIT);
			huc.setReadTimeout(READTIMELIMIT);
			huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US)");

			// 如果只抓取当前页面，则无需跳转至其他网页
			// HttpURLConnection.setFollowRedirects(false);

			BufferedReader br = null;
			if (enc == null || enc.isEmpty())
				br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
			else
				br = new BufferedReader(new InputStreamReader(huc.getInputStream(), enc));

			huc.connect();
			String line = null;
			while ((line = br.readLine()) != null)
				sb.append(line + "\r\n");

			huc.disconnect();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}
}