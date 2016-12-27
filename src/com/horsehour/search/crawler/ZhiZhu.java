package com.horsehour.search.crawler;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.horsehour.search.encoding.EncodeParser;
import com.horsehour.util.DOMUtils;
import com.horsehour.util.DOMUtils.TOOL;

/**
 * Tiny Web Crawler
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20110501
 */
public class ZhiZhu {
	private static int READTIMELIMIT = 6000 * 100;
	private static int CONNECTTIMELIMIT = 6000 * 100;

	/**
	 * fetch web page with given length of content skipped
	 * 
	 * @param url
	 * @param enc
	 * @param skipLen
	 * @return content of web page
	 */
	public static String skipGET(String url, String enc, long skipLen){
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

			br.skip(skipLen);
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

	public static String get(String url, String enc){
		return skipGET(url, enc, 0);
	}

	public static String get(URL url, String enc){
		return get(url.toString(), enc);
	}

	public static String get(URL url){
		String enc = EncodeParser.parseEncode(url.toString());
		if (enc == null || enc.isEmpty())
			return get(url, "utf-8");
		else
			return get(url, enc);
	}

	public static String get(String url){
		String enc = EncodeParser.parseEncode(url);
		if (enc == null || enc.isEmpty())
			return get(url, "utf-8");
		else
			return get(url, enc);
	}

	/**
	 * fetch image files
	 * 
	 * @param url
	 * @param dest
	 * @return true if succeed, false if failed
	 */
	public static boolean getImage(String url, String dest){
		HttpURLConnection huc = null;
		try {
			huc = (HttpURLConnection) new URL(url).openConnection();
			huc.setConnectTimeout(CONNECTTIMELIMIT);
			huc.setReadTimeout(READTIMELIMIT);

			// 设置用户代理
			huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US)");

			if (new File(dest).isDirectory()) {
				String type = huc.getContentType();
				String suffix = "";
				boolean undefined = false;
				int idx = 0;
				if (type == null || type.isEmpty() || (idx = type.indexOf("/")) == -1)
					undefined = true;
				else
					suffix = "." + type.substring(idx + 1);

				String name = url.substring(url.lastIndexOf("/") + 1).toLowerCase();
				idx = name.lastIndexOf(".");
				if (idx == -1)
					name = UUID.randomUUID().toString();
				else if (undefined) {
					suffix = name.substring(idx);
					int len = suffix.length();
					if (len >= 5) {
						if (suffix.contains(".jpeg"))
							suffix = ".jpeg";
						else
							suffix = suffix.substring(0, 5);
					}
				} else
					name = name.substring(0, idx);

				if (suffix.isEmpty())
					suffix = ".png";
				else if (suffix.equals(".x-icon"))
					suffix = ".ico";
				dest += name + suffix;
			}

			DataInputStream in = new DataInputStream(huc.getInputStream());

			huc.connect();
			int status = huc.getResponseCode();
			if (status != 200)
				return false;

			DataOutputStream out = null;
			out = new DataOutputStream(new FileOutputStream(dest));

			byte[] buff = new byte[1024];
			int count = 0;
			while ((count = in.read(buff)) > 0)
				out.write(buff, 0, count);

			out.close();
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * fetch favorite icon (favicon) of a site
	 * @param url
	 * @param dest directory
	 * @return true if succeed, false if failed
	 */
	public static boolean getFavicon(String url, String dest){
		String host = "";
		try {
			host = new URL(url).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		String iconLink = "";
		int idx = url.indexOf(":");
		iconLink = url.substring(0, idx) + "://" + host + "/favicon.ico";

		boolean dirFlag = new File(dest).isDirectory();
		String outputFile = dest;
		if (dirFlag)
			outputFile += host + ".ico";

		boolean succeed = ZhiZhu.getImage(iconLink, outputFile);
		String suffix = "", href = "";
		if (!succeed) {
			String html = ZhiZhu.get(url);
			Element elem = DOMUtils.getHTMLDoc(html).select("link[href~=.*\\.(ico|png)]").first();
			if (elem != null) {
				href = elem.attr("abs:href");
				if (href.equals(iconLink))
					return false;
				suffix = (href.contains(".png")) ? ".png" : ".ico";
			}
			if (dirFlag)
				dest += host + suffix;
			succeed = ZhiZhu.getImage(href, dest);
		}
		return succeed;
	}

	/**
	 * fetch binary files such as image, pdf, doc, docx, txt et al. files
	 * 
	 * @param url
	 * @param dest
	 */
	public static void getBinary(String url, String dest){
		URLConnection conn = null;
		try {
			conn = new URL(url).openConnection();
			conn.setConnectTimeout(CONNECTTIMELIMIT);
			conn.setReadTimeout(READTIMELIMIT);

			// 设置用户代理
			conn.setRequestProperty("User-Agent", "Chrome/6.0.472.33");
			DataInputStream input = new DataInputStream(conn.getInputStream());

			DataOutputStream output = null;
			output = new DataOutputStream(new FileOutputStream(dest, false));

			byte[] buff = new byte[10240];
			int bytes = 0;
			while ((bytes = input.read(buff)) > 0)
				output.write(buff, 0, bytes);

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
	}

	/**
	 * @param url
	 * @param parameters
	 * @param enc
	 * @return content of web page according to post method
	 */
	public static String post(String url, String parameters, String enc){
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");

			conn.setDoOutput(true);
			conn.setDoInput(true);

			// Send request
			OutputStream os = conn.getOutputStream();
			os.write(parameters.getBytes());
			os.flush();
			os.close();

			// Get Response
			BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), enc));
			String line = "";
			StringBuffer response = new StringBuffer();
			while ((line = br.readLine()) != null) {
				response.append(line);
				response.append("\r\n");
			}
			br.close();

			return response.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	/**
	 * Use Post Method to Obtain Web Page
	 * 
	 * @return Content of Web Page at URL
	 */
	public static String post(String url, String parameters){
		return post(url, parameters, "utf-8");
	}

	public static String httpGET(String url){
		return httpGET(url, "utf-8");
	}

	/**
	 * Crawl Pages Via HttpClient's Get Method
	 * 
	 * @param url
	 * @return page content
	 */
	public static String httpGET(String url, String enc){
		CloseableHttpClient hc = HttpClients.createDefault();
		// RequestConfig config = RequestConfig.custom()
		// .setSocketTimeout(2000).setConnectTimeout(2000).build();
		// CloseableHttpClient hc = HttpClients.custom()
		// .setDefaultRequestConfig(config).build();

		String html = "";
		HttpGet httpget = new HttpGet(url);
		try {
			CloseableHttpResponse response = hc.execute(httpget);
			HttpEntity entity = response.getEntity();
			int statuscode = response.getStatusLine().getStatusCode();
			if (statuscode == HttpStatus.SC_OK) {
				if (enc == null || enc.isEmpty())
					html = EntityUtils.toString(entity);
				else
					html = EntityUtils.toString(entity, enc);
			}

			response.close();

			hc.close();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return html;
	}

	public static String proxyCrawl(String url, String proxyIP){
		return proxyCrawl(url, "utf-8", proxyIP);
	}

	/**
	 * Crawl Web Page with Proxy IP
	 * 
	 * @param url
	 * @param proxyIP
	 * @return Content of Web Page
	 */
	public static String proxyCrawl(String url, String enc, String proxyIP){
		StringBuffer sb = new StringBuffer();
		String[] bisect = proxyIP.split(":");
		String host = bisect[0];
		int port = Integer.parseInt(bisect[1]);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

		HttpURLConnection huc = null;
		try {
			huc = (HttpURLConnection) new URL(url).openConnection(proxy);

			huc.setConnectTimeout(CONNECTTIMELIMIT);
			huc.setReadTimeout(READTIMELIMIT);

			huc.setRequestProperty("User-Agent",
			        "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");

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

	public static String proxyGET(String url, String enc, String proxyIP){
		String[] bisect = proxyIP.split(":");
		return proxyGET(url, enc, bisect[0], bisect[1]);
	}

	/**
	 * @param url
	 * @param enc
	 * @param host
	 * @param port
	 * @return page content
	 */
	public static String proxyGET(String url, String enc, String host, String port){
		HttpHost proxy = new HttpHost(host, Integer.parseInt(port));
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

		RequestConfig config = RequestConfig.custom().setSocketTimeout(6000)
		        .setConnectTimeout(6000).build();
		CloseableHttpClient hc = HttpClients.custom().setRoutePlanner(routePlanner)
		        .setDefaultRequestConfig(config).build();

		HttpGet httpGet = new HttpGet(url);
		String html = null;

		try {
			CloseableHttpResponse response = hc.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK)
				html = EntityUtils.toString(response.getEntity(), enc);

			response.close();
			hc.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return html;
	}

	/**
	 * Build Proxy IP Data Base from Given Page
	 * 
	 * @param url
	 * @param enc
	 * @return Proxy IP Data Base
	 */
	public static List<String> buildProxyDB(String url, String enc){
		String html = get(url, enc);
		if (html == null || html.isEmpty())
			return null;

		final int maxPort = (2 << 15) - 1;
		String regrex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}";

		Pattern p = Pattern.compile(regrex);
		Matcher m = p.matcher(html);

		List<String> ipList = new ArrayList<String>();
		List<String> hostList = new ArrayList<String>();
		String entry, host, port;
		while (m.find()) {
			entry = m.group().trim();
			String[] bisect = entry.split(":");
			if (bisect.length < 2)
				continue;

			host = bisect[0];
			port = bisect[1];
			if (hostList.contains(host) || Integer.parseInt(port) > maxPort)
				continue;

			ipList.add(entry);
			hostList.add(host);
		}
		return ipList;
	}

	/**
	 * Identify the Authentic IP Based on a Third Website
	 * 
	 * @param ipList
	 * @param hostList
	 * @return Authentic IP
	 */
	public static void getAuthenticIPDB(List<String> ipList, List<String> hostList){
		final String mirrorURL = "http://iframe.ip138.com/ic.asp";
		String ip, host, port;
		String[] bisect;
		for (int i = 0; i < ipList.size(); i++) {
			ip = ipList.get(i);
			bisect = ip.split(":");
			host = bisect[0];
			port = bisect[1];

			String html = null;
			int maxTry = 3;// 最多尝试三次
			while (html == null && maxTry > 0) {
				html = proxyGET(mirrorURL, "utf-8", host, port);
				maxTry--;
			}

			if (html == null || html.isEmpty()) {
				System.out.println("经验证IP" + ip + "无效.");
				ipList.remove(i);
				hostList.remove(i);
				i--;
				continue;
			}

			Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
			Matcher m = p.matcher(html);

			String autIP = "";
			if (m.find()) {
				String autHost = m.group();
				if (host.equals(autHost))
					System.out.println("经验证IP" + ip + "为真实IP.");

				if (!hostList.contains(autHost)) {
					autIP = autHost + ":" + port;
					hostList.add(autHost);
					ipList.add(autIP);
					System.out.println("经验证IP" + ip + "属马甲IP.");
				}
			}
		}
	}

	/**
	 * extract table 
	 * @param url
	 * @param enc
	 * @param nf
	 * @return table in rows
	 */
	public static List<String[]> getTable(String url, String enc, NodeFilter nf){
		Parser parser = DOMUtils.getParser(url, enc);
		NodeList nodes = DOMUtils.getNodeList(parser, nf);
		if (nodes == null || nodes.size() == 0)
			return null;

		List<String[]> table = new ArrayList<String[]>();
		Node node = nodes.elementAt(0);// the 1st element in default
		if (node instanceof TableTag) {
			TableRow[] rows = ((TableTag) node).getRows();// all rows
			for (TableRow row : rows) {
				TableHeader[] head = row.getHeaders();// column header <th>
				int dim = -1;
				String[] headEntry = null;
				if (head != null && (dim = head.length) > -1) {
					headEntry = new String[dim];
					for (int i = 0; i < dim; i++)
						headEntry[i] = head[i].toPlainTextString().trim();
					table.add(headEntry);
				}

				TableColumn[] data = row.getColumns();// column data <td>
				dim = -1;
				String[] dataEntry = null;
				if (data != null && (dim = data.length) > -1) {
					dataEntry = new String[dim];
					for (int i = 0; i < dim; i++)
						dataEntry[i] = data[i].toPlainTextString().trim();
					table.add(dataEntry);
				}
			}
		}
		return table;
	}

	public static List<String[]> getTable(String url, NodeFilter nf){
		return getTable(url, "utf-8", nf);
	}

	/**
	 * extract all links with anchors in a page, using a NodeFilter, 
	 * such as NodeClassFilter, TagNameFilter, or HasAttributeFilter
	 * @param parser
	 * @param nf
	 * @return list of links and anchors
	 */
	public static Map<String, String> getLinks(Parser parser, NodeFilter nf){
		NodeList nodes = DOMUtils.getNodeList(parser, nf);
		int sz = -1;
		if (nodes == null || (sz = nodes.size()) == 0)
			return null;

		Map<String, String> linkList = new HashMap<String, String>();
		Node node;
		String link, anchor;
		for (int i = 0; i < sz; i++) {
			node = nodes.elementAt(i);
			if (node instanceof LinkTag) {
				link = ((LinkTag) node).getLink();
				anchor = ((LinkTag) node).getLinkText().trim();
				if (link.isEmpty())
					continue;
				linkList.put(link, anchor);
			}
		}
		return linkList;
	}

	public static Map<String, String> getLinks(String url, NodeFilter nf){
		Parser parser = DOMUtils.getParser(url, null);
		return getLinks(parser, nf);
	}

	/**
	 * extract all links from page using htmlparser
	 * @param url
	 * @return list of links with anchors
	 */
	private static Map<String, String> getLinksHtmlParser(String url){
		Parser parser = DOMUtils.getParser(url, null);
		NodeFilter nf = new NodeClassFilter(LinkTag.class);
		NodeList nodes = DOMUtils.getNodeList(parser, nf);
		int sz = -1;
		if (nodes == null || (sz = nodes.size()) == 0)
			return null;

		Map<String, String> linkList = new HashMap<String, String>();
		Node node;
		String link, anchor;
		for (int i = 0; i < sz; i++) {
			node = nodes.elementAt(i);
			if (node instanceof LinkTag) {
				link = ((LinkTag) node).getLink();
				anchor = ((LinkTag) node).getLinkText().trim();
				if (link.isEmpty())
					continue;
				linkList.put(link, anchor);
			}
		}
		return linkList;
	}

	/**
	 * extract all links in web page using jsoup selector
	 * @param url
	 * @return all links with anchors
	 */
	public static Map<String, String> getLinks(String url){
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Elements elms = doc.select("a");
		int sz = -1;
		if (elms == null || (sz = elms.size()) == 0)
			return null;

		Map<String, String> linkList = new HashMap<String, String>();

		Element elm = null;
		String link, anchor;
		for (int i = 0; i < sz; i++) {
			elm = elms.get(i);
			link = elm.attr("abs:href");// absolute url
			anchor = elm.text();// anchor
			if (link.isEmpty())
				continue;
			linkList.put(link, anchor);
		}
		return linkList;
	}

	/**
	 * extract all links in page
	 * @param url
	 * @param tool
	 * @return all links with anchors
	 */
	public static Map<String, String> getLinks(String url, TOOL tool){
		if (tool == TOOL.JSOUP)
			return getLinks(url);
		if (tool == TOOL.HTMLPARSER)
			return getLinksHtmlParser(url);
		return null;
	}

	/**
	 * extract all image links in web page using jsoup
	 * @param url
	 * @return all image links
	 */
	private static List<String> getImageListJsoup(String url){
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Elements elms = doc.select("img");
		int sz = -1;
		if (elms == null || (sz = elms.size()) == 0)
			return null;

		List<String> linkList = new ArrayList<String>();

		Element elm = null;
		String link;
		for (int i = 0; i < sz; i++) {
			elm = elms.get(i);
			link = elm.attr("abs:src");// absolute url
			if (link.isEmpty())
				continue;
			linkList.add(link);
		}
		return linkList;
	}

	/**
	 * extract all image links from page using htmlparser
	 * @param list of image links
	 */
	private static List<String> getImageListHtmlParser(String url){
		Parser parser = DOMUtils.getParser(url, null);
		NodeFilter nf = new NodeClassFilter(ImageTag.class);
		NodeList nodes = null;
		try {
			nodes = parser.parse(nf);
		} catch (ParserException e) {
			e.printStackTrace();
			return null;
		}

		if (nodes == null || nodes.size() == 0)
			return null;

		String regex = "src=\"http:[\\s\\S]*?\"";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher mat = null;

		List<String> linkList = new ArrayList<String>();
		Node node;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.elementAt(i);
			String link = node.getText();
			mat = pattern.matcher(link);
			if (mat.find()) {
				link = mat.group().trim();
				link = link.substring(5, link.length() - 1);
				if (link.isEmpty())
					continue;
				linkList.add(link);
			}
		}
		return linkList;
	}

	/**
	 * @param url
	 * @param tool JSOUP or HTMLPARSER
	 * @return list of image links
	 */
	public static List<String> getImageList(String url, TOOL tool){
		if (tool == TOOL.JSOUP)
			return getImageListJsoup(url);
		if (tool == TOOL.HTMLPARSER)
			return getImageListHtmlParser(url);
		return null;
	}

	/**
	 * @param url
	 * @param tool
	 * @return title of web page
	 */
	public static String getTitle(String url, TOOL tool){
		String title = "";
		if (tool == TOOL.HTMLPARSER) {
			Parser parser = DOMUtils.getParser(url, null);
			HtmlPage page = null;
			try {
				page = new HtmlPage(parser);
				parser.visitAllNodesWith(page);
			} catch (ParserException e) {
				e.printStackTrace();
				return null;
			}
			title = page.getTitle();
		} else if (tool == TOOL.JSOUP) {
			Document doc = null;
			try {
				doc = Jsoup.connect(url).get();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			title = doc.title();
		}
		return title;
	}

	/**
	 * set the limit of reading time
	 * 
	 * @param limitedTime
	 */
	public static void setReadTimeLimit(int limitedTime){
		READTIMELIMIT = limitedTime;
	}

	/**
	 * set the limit of connection time
	 * 
	 * @param limitedTime
	 */
	public static void setConnectTimeLimit(int limitedTime){
		CONNECTTIMELIMIT = limitedTime;
	}
}