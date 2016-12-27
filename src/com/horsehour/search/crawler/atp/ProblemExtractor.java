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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import com.horsehour.search.crawler.ZhiZhu;
import com.horsehour.util.DOMUtils;
import com.horsehour.util.TickClock;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130426
 */
public class ProblemExtractor implements Callable<String> {
	private final String postURL;
	private final String formulaURL;
	private final String name;

	public ProblemExtractor(String postURL, String formulaURL, String name) {
		this.postURL = postURL;
		this.formulaURL = formulaURL;
		this.name = name;
	}

	/**
	 * @return Post and Retrieve the Result
	 */
	@Override
	public String call(){
		String mfd = "multipart/form-data";
		String boundary = "----WebKitFormBoundaryqtePYh7XSStTvyzW";

		HttpURLConnection conn = null;
		try {
			URL url = new URL(postURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(6000 * 1000);

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
		Parser parser = DOMUtils.createParser(problemPage, "iso-8859-1");
		NodeFilter nodeFilter = new NodeClassFilter(LinkTag.class);
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
		FileUtils.write(new File("Data/ReasonProblem/MIZ-1.stat"), pageName + "\t" + count + "\r\n","");

		FileUtils.write(new File("Data/ReasonProblem/Problem-MWS-Link/" + pageName + ".txt"), StringUtils.join(linkList, "\r\n"),"");

		// for(int i = 0; i < count; i++)
		// FileManager.write(ZhiZhu.crawl(linkList.get(i), "iso-8859-1"),
		// "Data/ReasonProblem/Problem-MWS-2/" + nameList.get(i) + ".txt");
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

	public void parseProblem(String atpProblemPage, String mizName) throws IOException{
		String params = "";
		String regex = "this\\S+lc=\\S+ATP";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(atpProblemPage);
		int idx = -1;
		while (matcher.find()) {
			params = matcher.group().trim();
			if (idx == -1)
				idx = params.indexOf("lc=");
			params = params.substring(idx + 3).replace("&amp;ATP", "");
			params = params.replaceAll("&amp;", "&");

		}

		String link = "http://mws.cs.ru.nl/~mptp/cgi-bin/showtmpfile.cgi?file=problems/" + mizName + "/" + mizName + "__" + params;
		idx = params.indexOf("&");
		FileUtils.write(new File("Data/ReasonProblem/" + mizName + "__" + params.substring(0, idx) + ".html"), ZhiZhu.get(link),"");

		//		String name = "";
		//		StringBuffer sb = new StringBuffer();
		//		for (String line : lines) {
		//			if (line.startsWith("<pre>")) {
		//				line = line.substring(5);
		//				int head = line.indexOf(":");
		//				int tail = line.indexOf(",");
		//				if (head == -1 || tail == -1 || tail < head)
		//					System.out.println(name + " doesn't have valid pre tag.");
		//				else
		//					name = line.substring(head + 1, tail).trim();
		//				sb.append(line + "\r\n");
		//			} else if (line.startsWith("fof("))
		//				sb.append(line + "\r\n");
		//		}
	}

	public static void main_1(String[] args) throws IOException{
		TickClock.beginTick();

		int nThread = 2;
		ExecutorService service = Executors.newFixedThreadPool(nThread);
		Future<String> future = null;

		List<String> mizList = FileUtils.readLines(new File("Data/ReasonProblem/MizList.txt"),"");
		List<String> visitedMiz = FileUtils.readLines(new File("Data/ReasonProblem/VisitedMiz.txt"),"");

		String root = "http://mws.cs.ru.nl/~mptp/";
		//		String root = "http://mizar.cs.ualberta.ca/~mptp/";

		String baseURL = root + "mml4.181.1147/mml/";
		String postURL = root + "cgi-bin/MizAR.cgi";
		String formulaURL, name;

		ProblemExtractor task = null;
		for (String miz : mizList) {
			if (visitedMiz.contains(miz))
				continue;

			name = miz.substring(0, miz.length() - 4);
			formulaURL = baseURL + miz;

			task = new ProblemExtractor(postURL, formulaURL, name);
			future = service.submit(task);
			System.out.println(miz);

			try {
				FileUtils.write(new File("Data/ReasonProblem/MizAR/" + name + ".html"), future.get(),"");
				//				task.parseProblem(future.get(), name);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		TickClock.stopTick();
	}

	public static void main(String[] args){

	}
}