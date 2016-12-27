package com.horsehour.search.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.horsehour.util.TickClock;

/**
 * UN Comtrade Database Extraction API
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20150408
 * @see http://comtrade.un.org/data/doc/api/
 */
public class UNComtradeAPI {
	public int rateLimit = 1;// one request every second
	public int usageLimit = 100;// 100 requests per hour
	public int maxRecord = 50000;

	public String baseURL = "http://comtrade.un.org/api/get?";
	public String dataType = "C";// commodities
	public String head = "H";// human readable headings
	public String format = "csv";// csv or json

	public int tradeFlow = 2;// imports(1) or exports(2)

	public String freq = "A";// Annual (A) or Monthly (M)
	public String period = "2014";// depends on the freq
	public String cls = "HS";// HS12(H4), HS07(H3), HS02(H2), HS96(H1),
								// HS92(H0)/or "HS"

	public String cc = "AG4";// AG2, AG4, AG6 or 01,02,.../commodity code

	/**
	 * @param reporter
	 * @param partners
	 * @param tradeFlow
	 *            imports(1) or exports(2)
	 * @param cc
	 *            AG2, AG4, AG6 or 01,02,... or 01%2C02...
	 * @param destFile
	 * @throws IOException
	 */
	public void extract(int reporter, int[] partners, int tradeFlow, String cc, File destFile) throws IOException {
		StringBuffer url = new StringBuffer();
		url.append(baseURL).append("max=" + maxRecord + "&").append("type=" + dataType + "&")
				.append("freq=" + freq + "&").append("px=" + cls + "&").append("ps=" + period + "&")
				.append("rg=" + tradeFlow + "&").append("cc=" + cc + "&").append("r=" + reporter + "&").append("p=");

		int n = -1;
		if (partners == null || (n = partners.length) == 0)
			url.append("all");
		else {
			url.append(partners[0]);
			for (int i = 1; i < n; i++)
				url.append("%2C" + partners[i]);
		}

		url.append("&fmt=" + format + "&").append("head=" + head);
		FileUtils.write(destFile, ZhiZhu.get(url.toString()), "UTF8", false);
	}

	public void extract(int reporter, List<Integer> partners, int tradeFlow, String cc, File destFile)
			throws IOException {
		StringBuffer url = new StringBuffer();
		url.append(baseURL).append("max=" + maxRecord + "&").append("type=" + dataType + "&")
				.append("freq=" + freq + "&").append("px=" + cls + "&").append("ps=" + period + "&")
				.append("rg=" + tradeFlow + "&").append("cc=" + cc + "&").append("r=" + reporter + "&").append("p=");

		int n = -1;
		if (partners == null || (n = partners.size()) == 0)
			url.append("all");
		else {
			url.append(partners.get(0));
			for (int i = 1; i < n; i++)
				url.append("%2C" + partners.get(i));
		}

		url.append("&fmt=" + format + "&").append("head=" + head);
		FileUtils.write(destFile, ZhiZhu.get(url.toString(), "utf-8"), "", false);
	}

	/**
	 * @param reporter
	 * @param partner
	 * @param year
	 * @param tradeFlow
	 * @param cc
	 *            AG2, AG4, AG6
	 * @param destFile
	 * @throws IOException
	 */
	public void extract(int reporter, int partner, int year, int tradeFlow, String cc, String destFile)
			throws IOException {
		StringBuffer url = new StringBuffer();
		url.append(baseURL).append("max=" + maxRecord + "&").append("type=" + dataType + "&")
				.append("freq=" + freq + "&").append("px=" + cls + "&").append("ps=" + year + "&")
				.append("rg=" + tradeFlow + "&").append("cc=" + cc + "&").append("r=" + reporter + "&")
				.append("p=" + partner);

		url.append("&fmt=" + format + "&").append("head=" + head);
		FileUtils.write(new File(destFile), ZhiZhu.get(url.toString()), "utf-8");
	}

	/**
	 * Extract Big Chunk Trade Data for Many Big Countries
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void extractBigChunk() throws IOException, InterruptedException {
		List<String> list = FileUtils.readLines(new File("users/chjiang/documents/Data/ReportList.txt"), "");
		List<Integer> reportList = new ArrayList<>();

		for (String rpt : list) {
			reportList.add(Integer.parseInt(rpt));
		}

		List<String> lines = FileUtils.readLines(new File("Data/UNComtrade/BigCountry.txt"), "");
		int i = 1;
		for (; i < lines.size(); i++) {
			String line = lines.get(i);
			int index = line.indexOf("\t");
			int reporter = Integer.parseInt(line.substring(0, index));
			String name = line.substring(index + 1);
			String dest = "Data/UNComtrade/Export/" + reporter + "-" + name + "/";
			for (int y = 2004; y <= 2014; y++) {
				for (int k = 0; k < 52; k++) {// 208=4*52
					period = new Integer(y).toString();
					File file = new File(dest + y + "-" + (k + 1) + ".csv");
					if (file.exists() && FileUtils.sizeOf(file) > 0)
						continue;
					extract(reporter, filter(reportList.subList(k * 4, (k + 1) * 4), reporter), 2, "AG6", file);
				}
				System.out.println(name + "-" + y);
			}
		}
	}

	/**
	 * @param list
	 * @param id
	 * @return filter itself
	 */
	public int[] filter(List<Integer> list, int id) {
		int[] array;
		int idx = list.indexOf(id);
		if (idx == -1) {
			array = new int[list.size()];
			for (int i = 0; i < list.size(); i++)
				array[i] = list.get(i);
		} else {
			array = new int[list.size() - 1];
			for (int i = 0; i < list.size(); i++) {
				if (i < idx)
					array[i] = list.get(i);
				else if (i > idx)
					array[i - 1] = list.get(i);
			}
		}
		return array;
	}

	public void extractMissedData() throws IOException, InterruptedException {
		List<Integer> reportList = Files.lines(Paths.get("/Users/chjiang/Documents/data/ReportList.txt"))
				.map(Integer::parseInt).collect(Collectors.toList());

		List<String> lines = Files.readAllLines(Paths.get("/Users/chjiang/Documents/data/Update.txt"));
		for (String line : lines) {
			File file = new File(line);
			String name = file.getName();
			String year = name.substring(0, name.length() - 4);
			name = file.getParentFile().getName();
			Path destFile = Paths.get("/Users/chjiang/Documents/data/Update/" + name + '/');
			if (!Files.exists(destFile))
				Files.createDirectory(destFile);

			int reporter = Integer.parseInt(name.split("-")[0]);
			for (int k = 0; k < 52; k++) {
				period = year;
				File store = new File(destFile.toString() + "/" + year + "-" + (k + 1) + ".csv");
				if (store.exists() && FileUtils.sizeOf(store) > 0)
					continue;
				extract(reporter, filter(reportList.subList(k * 4, (k + 1) * 4), reporter), 2, "AG6", store);
			}
			System.out.println(name + "-" + year);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		TickClock.beginTick();

		UNComtradeAPI uca = new UNComtradeAPI();
		uca.extractMissedData();

		TickClock.stopTick();
	}
}