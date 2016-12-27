package com.horsehour.search.crawler.atp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2016年3月26日 下午4:26:03
 **/
public class ATPMain {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException{
		String dbLoc = "Data/ATP/";

		String[] sites = {"http://mizar.cs.ualberta.ca/~mptp/", "http://mws.cs.ru.nl/~mptp/"};
		String site, baseURL, postURL, formulaURL, mizName, miz;

		int nThread = 5;
		ExecutorService service = null;
		ATPProblemParser parser = null;

		List<String> mizList = FileUtils.readLines(new File(dbLoc + "MizList.txt"),"");
		ATPMizParser mizParser = null;
		for (int i = 0; i < mizList.size(); i++) {
			miz = mizList.get(i);
			mizName = miz.substring(0, miz.length() - 4);

			site = sites[i % 2];

			baseURL = site + "mml4.181.1147/mml/";
			postURL = site + "cgi-bin/MizAR.cgi";

			formulaURL = baseURL + miz;

			String dest = dbLoc + mizName;
			File mizHome = new File(dest);
			if (!mizHome.exists())
				mizHome.mkdir();

			List<String> linkList = null, visitedList = Collections.synchronizedList(new ArrayList<String>());

			File htmlFile = new File(dest + "/" + mizName + ".html");
			String mizPage = "";
			if (!htmlFile.exists()) {
				System.out.println("Parsing MizAR Page (" + mizName + ")...");
				mizParser = new ATPMizParser(site, dbLoc, postURL, formulaURL, mizName);
				mizPage = mizParser.getMizPage();
				if (mizPage == null) {
					System.err.println("MizAR Page (" + mizName + ") Premature EOF");
					continue;
				}
				FileUtils.write(htmlFile, mizPage,"");
			}

			if (htmlFile.getTotalSpace() == 0)//prematured page
				continue;

			if (mizPage == "") {
				mizParser = new ATPMizParser(site, dbLoc, postURL, formulaURL, mizName);
				mizPage = FileUtils.readFileToString(htmlFile,"");
			}

			System.out.println("Extracting Problem Links in MizAR Page (" + mizName + ")...");
			linkList = mizParser.getProblemLinks(mizPage);

			File linkFile = new File(dest + "/link");
			if (!linkFile.exists())

				if (linkList == null && linkFile.exists())
					linkList = FileUtils.readLines(linkFile,"");

			File problemHome = new File(dest + "/Problems/");
			if (!problemHome.exists())
				problemHome.mkdir();

			service = Executors.newFixedThreadPool(nThread);
			for (String link : linkList) {
				parser = new ATPProblemParser(link, dest);
				Future<String> result = service.submit(parser);
				if (result.get() == null) {
				} else {
					visitedList.add(link);
					System.out.println("..." + link);
				}
			}

			service.shutdown();//结束任务
			StringBuffer sb = new StringBuffer();
			for (String link : visitedList)
				if (linkList.contains(link)) {
					linkList.remove(link);
					sb.append(link + "\r\n");
				}
			FileUtils.write(new File(dest + "/visited"), sb.toString(), "",true);

			sb = new StringBuffer();
			for (String link : linkList)
				sb.append(link + "\r\n");
			FileUtils.write(new File(dest + "/link"), sb.toString(),"");

			Thread.sleep(60000);//waiting for 1 minute/60 seconds for each mizar page
		}
	}
}