package com.horsehour.search.crawler.atp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import com.horsehour.util.DOMUtils;
import com.horsehour.util.TickClock;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 12:44:05 04/03/2015
 **/
public class ReasonProblemExtractor {
	public Parser parser;
	public NodeFilter nodeFilter;

	public ReasonProblemExtractor() {}

	/**
	 * Fetch Pages and Analyze Them
	 * @throws IOException 
	 */
	public void parseMiamiProblems(String src) throws IOException{
		List<String> problemList = FileUtils.readLines(new File(src),"");
		for (String url : problemList)
			parseMiamiProblem(url, "http://www.cs.miami.edu/~tptp/cgi-bin");
	}

	/**
	 * Extract Core Data
	 * 
	 * @param problemURL
	 * @param baseURL
	 * @throws IOException 
	 */
	public void parseMiamiProblem(String problemURL, String baseURL) throws IOException{
		parser = DOMUtils.getParser(problemURL, "utf-8");
		nodeFilter = new TagNameFilter("pre");
		NodeList nodeList = DOMUtils.getNodeList(parser, nodeFilter);
		if (nodeList == null || nodeList.size() == 0) {
			System.out.println("Failed To Visit Page :" + problemURL);
			return;
		}

		String html = nodeList.elementAt(0).getParent().toPlainTextString();
		int beginIdx = html.indexOf("include('Axioms/");

		int idx = problemURL.lastIndexOf("=");
		String name = problemURL.substring(idx + 1, problemURL.length() - 2);

		String header = "", axiom = "";
		if (beginIdx > -1) {
			html = html.substring(beginIdx);

			String flag = "ax').";
			idx = html.indexOf(flag);
			if (idx == -1) {
				System.out.println("Fail to parse Header:" + problemURL);
				return;
			}
			header = html.substring(0, idx + flag.length()).trim();
			beginIdx = header.indexOf("/");
			idx = header.indexOf("ax");

			String axURL = baseURL + "/SeeTPTP?Category=Axioms&File=" + header.substring(beginIdx + 1, idx + 2);

			axiom = parseAxiom(axURL);
		}

		String problem = "";
		beginIdx = html.indexOf("fof(");
		if (beginIdx == -1) {
		} else
			problem = html.substring(beginIdx);

		StringBuffer sb = new StringBuffer();
		if (!header.isEmpty())
			sb.append("%" + header + "\r\n");
		sb.append(axiom);
		sb.append(problem);

		FileUtils.write(new File("Data/ReasonProblem/Problem-Miami/" + name + ".txt"), sb.toString(),"");
	}

	/**
	 * Parse the Page with an Axiom Block
	 * 
	 * @param url
	 * @return
	 */
	private String parseAxiom(String url){
		parser = DOMUtils.getParser(url, "utf-8");
		NodeList nodeList = DOMUtils.getNodeList(parser, nodeFilter);
		if (nodeList == null || nodeList.size() == 0)
			return null;
		Node node = nodeList.elementAt(0);
		return node.getParent().toPlainTextString();
	}

	public void postprocess() throws IOException{
		String mwsFile = "Data/ReasonProblem/Problem-MWS-/";
		List<String> lines;
		StringBuffer sb = null;
		String name;

		for (File file : FileUtils.listFiles(new File(mwsFile), null, false)) {
			name = file.getName();
			lines = FileUtils.readLines(file,"");

			sb = new StringBuffer();
			for (String line : lines) {
				if (line.startsWith("<pre>")) {
					line = line.substring(5);
					int head = line.indexOf(":");
					int tail = line.indexOf(",");
					if (head == -1 || tail == -1 || tail < head)
						System.out.println(name + " doesn't have valid pre tag.");
					else
						name = line.substring(head + 1, tail).trim();
					sb.append(line + "\r\n");
				} else if (line.startsWith("fof("))
					sb.append(line + "\r\n");
			}

			if (name.endsWith(".txt"))
				name = "Data/ReasonProblem/Problem-MWS-Final/" + name;
			else
				name = "Data/ReasonProblem/Problem-MWS-Final/" + name + ".txt";

			FileUtils.write(new File(name), sb.toString(),"");
		}
	}

	/**
	 * Extract Problems from MWS Website
	 * @throws IOException 
	 */
	public void parseMWSProblems(String src) throws IOException{
		List<String> problemList = FileUtils.readLines(new File(src),"");

		// String root = "http://mws.cs.ru.nl/~mptp/";
		String root = "http://mizar.cs.ualberta.ca/~mptp/";

		String baseURL = root + "mml4.181.1147/mml/";
		String postURL = root + "cgi-bin/MizAR.cgi";

		String formulaURL, name;
		for (String miz : problemList) {
			System.out.println(miz);
			name = miz.substring(0, miz.length() - 4);
			formulaURL = baseURL + miz;
			parseMWSProblem(parseMWSProblems(postURL, formulaURL, name), name);
		}
	}

	/**
	 * Post and Retrieve the Result
	 * 
	 * @param postURL
	 * @param formulaURL
	 * @param name
	 * @return
	 */
	public String parseMWSProblems(String postURL, String formulaURL, String name){
		String mfd = "multipart/form-data";
		String boundary = "----WebKitFormBoundaryhsBTNAxGCqJ1swBU";

		HttpURLConnection conn = null;
		try {
			URL url = new URL(postURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(6000 * 100);

			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Content-Type", mfd + "; boundary=" + boundary);

			conn.connect();

			DataOutputStream output = null;
			output = new DataOutputStream(conn.getOutputStream());

			output.writeBytes(postWrap(boundary, formulaURL, name));// post body

			output.flush();
			output.close();

			int code = conn.getResponseCode();
			if (code != 200)
				System.err.println("Failed to Get Results!");

			BufferedReader input = null;
			input = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuffer response = new StringBuffer();
			String line;
			while ((line = input.readLine()) != null)
				response.append(line + "\r\n");
			input.close();

			return response.toString();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	/**
	 * Retrieve Theorem from Theorem Page
	 * 
	 * @param problemPage
	 * @param pageName
	 * @throws IOException 
	 */
	public void parseMWSProblem(String problemPage, String pageName) throws IOException{
		parser = DOMUtils.createParser(problemPage, "iso-8859-1");
		nodeFilter = new NodeClassFilter(LinkTag.class);
		NodeList nodes = DOMUtils.getNodeList(parser, nodeFilter);

		String problemName = "";
		int sz = nodes.size();
		Node node;
		List<String> linkList = new ArrayList<String>();
		List<String> nameList = new ArrayList<String>();
		for (int i = 0; i < sz; i++) {
			node = nodes.elementAt(i);

			String link = ((LinkTag) node).extractLink();
			if (link == null)
				continue;

			int beginIdx = -1, endIdx = -1;
			if (link.contains("file=problems")) {
				beginIdx = link.lastIndexOf("/");
				endIdx = link.lastIndexOf("&");
				problemName = link.substring(beginIdx + 1, endIdx);

				linkList.add(link);
				nameList.add(problemName);
			}
		}

		parser = null;
		int count = linkList.size();
		FileUtils.write(new File("Data/ReasonProblem/MIZ-2.stat"), pageName + "\t" + count + "\r\n","");

		FileUtils.write(new File("Data/ReasonProblem/Problem-MWS-Link/" + pageName + ".txt"), StringUtils.join(linkList, "\r\n"),"");

		// for(int i = 0; i < count; i++){
		// FileManager.write(ZhiZhu.crawl(linkList.get(i), "iso-8859-1"),
		// "Data/ReasonProblem/Problem-MWS-1/" + nameList.get(i) + ".txt");
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
	}

	/**
	 * wrap the post body
	 * 
	 * @param boundary
	 * @param formulaURL
	 * @param name
	 * @return post body
	 */
	private String postWrap(String boundary, String formulaURL, String name){
		StringBuffer sb = new StringBuffer();
		String splitLine = "--" + boundary + "\r\n";
		sb.append("\r\n" + splitLine);
		sb.append("Content-Disposition: form-data; name=\"Formula\"\r\n\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"UPLOADProblem\"; filename=\"\"\r\n");
		sb.append("Content-Type: application/octet-stream\r\n\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"ProblemSource\"\r\n\r\n");
		sb.append("URL" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"FormulaURL\"\r\n\r\n");
		sb.append(formulaURL + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"VocFile\"; filename=\"\"\r\n");
		sb.append("Content-Type: application/octet-stream\r\n\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"VocURL\"\r\n\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"Name\"\r\n\r\n");
		sb.append(name + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"MMLVersion\"\r\n\r\n");
		sb.append("4.181.1147" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"Verify\"\r\n\r\n");
		sb.append("1" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"HTMLize\"\r\n\r\n");
		sb.append("1" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"Parallelize\"\r\n\r\n");
		sb.append("1" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"GenATP\"\r\n\r\n");
		sb.append("1" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"ARProofs\"\r\n\r\n");
		sb.append("1" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"ProveUnsolved\"\r\n\r\n");
		sb.append("All" + "\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"Positions\"\r\n\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"Transf\"\r\n\r\n");

		sb.append(splitLine);
		sb.append("Content-Disposition: form-data; name=\"MODE\"\r\n\r\n");
		sb.append("HTML" + "\r\n");
		sb.append("--" + boundary + "--\r\n");

		return sb.toString();
	}

	/**
	 * 抽取链接
	 * 
	 * @param html
	 * @throws IOException 
	 */
	public void extractLink(String html, String name) throws IOException{
		Pattern pattern = Pattern.compile("<a.*?/a>");
		Matcher matcher = pattern.matcher(html);
		String href;
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			href = matcher.group();
			if (href.contains("file=problem")) {
				int idx1 = href.indexOf("http");
				int idx2 = href.lastIndexOf("\"");
				if (idx1 == -1 || idx2 == -1)
					continue;

				sb.append(href.substring(idx1, idx2 - 1) + "\r\n");
			}
		}
		FileUtils.write(new File("Data/ReasonProblem/Problem-MWS-Link/" + name + ".txt"), sb.toString(),"");
	}

	public static void main(String[] args) throws IOException{
		TickClock.beginTick();

		ReasonProblemExtractor te = new ReasonProblemExtractor();
		te.parseMWSProblems("Data/ReasonProblem/MIZList-.txt");
		// te.postprocess();

		// File[] files = FileUtil.getFileList("Data/ReasonProblem/page/");
		// for(File file : files){
		// String name = file.getName().replaceAll(".txt", "");
		// te.extractLink(FileUtil.read(file), name);
		// }

		TickClock.stopTick();
	}
}