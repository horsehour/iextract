package com.horsehour.search.crawler.atp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20160326
 */
public class ATPMizParser {
	private final String postURL;
	private final String formulaURL;
	private final String mizName;
	private final String dbLoc;
	private final String site;

	public ATPMizParser(String site, String dbLoc, String postURL, String formulaURL, String name) {
		this.site = site;
		this.dbLoc = dbLoc;
		this.postURL = postURL;
		this.formulaURL = formulaURL;
		this.mizName = name;
	}

	/**
	 * @return Post and Retrieve the Result
	 */
	public String getMizPage(){
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

			output.writeBytes(postConstruct(boundary, formulaURL, mizName));// post body

			output.flush();
			output.close();

			BufferedReader input = null;
			int code = conn.getResponseCode();
			if (code == 200)
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
	 * construct the post body
	 * 
	 * @param boundary
	 * @param formulaURL
	 * @param name
	 * @return post body
	 */
	private String postConstruct(String boundary, String formulaURL, String name){
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
	 * Extract Problem Link in Miz Page
	 * @param mizPage
	 * @return Set of problem links
	 * @throws IOException 
	 */
	public List<String> getProblemLinks(String mizPage) throws IOException{
		String regex = "this\\S+lc=\\S+ATP";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(mizPage);

		Set<String> paramSet = new HashSet<String>();
		int idx = -1;
		while (matcher.find()) {
			String params = matcher.group().trim();
			if (idx == -1)
				idx = params.indexOf("lc=");
			params = params.substring(idx + 3).replace("&amp;ATP", "");
			params = params.replaceAll("&amp;", "&");
			paramSet.add(params);
		}

		StringBuffer sb = new StringBuffer();
		List<String> linkList = new ArrayList<String>();
		for (String params : paramSet) {
			String link = site + "cgi-bin/showtmpfile.cgi?file=problems/" + mizName + "/" + mizName + "__" + params;
			linkList.add(link);
			sb.append(link + "\r\n");
		}
		FileUtils.write(new File(dbLoc + mizName + "/link"), sb.toString(),"");
		return linkList;
	}
}