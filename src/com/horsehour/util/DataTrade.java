package com.horsehour.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since Apr. 9, 2016
 **/
public class DataTrade {
	public static void uncomtrade() throws IOException {
		String base = "F:/Data/UNComtrade/Export/";
		String head = "Classification,Year,Reporter,Partner,Commodity Code,Commodity,Qty Unit,Qty,Netweight (kg),Trade Value (US$)\r\n";
		int[][] index = { { 0, 1, 9, 12, 14, 15, 17, 18, 19, 20 }, { 0, 1, 9, 12, 21, 22, 24, 25, 29, 31 } };
		String dest;

		for (File csvFile : FileUtils.listFiles(new File(base), new String[] { "csv" }, true)) {
			CSVParser csvParser = CSVParser.parse(csvFile, Charset.forName("utf-8"), CSVFormat.EXCEL);
			StringBuffer data = new StringBuffer();

			int[] idx = null;

			if (csvFile.getParentFile().getName().contains("China")) {
				dest = "F:/Data/UNComtrade/China-Export/";
				String reporter = "China";

				for (CSVRecord record : csvParser) {
					int size = record.size();
					if (size == 22)
						idx = index[0];
					else if (size == 35)
						idx = index[1];

					String cc = record.get(idx[4]);
					if (cc.trim().length() < 6)// leaf
						continue;

					String partner = record.get(idx[3]);
					if (partner.contains("Other Asia, nes"))
						partner = "Taiwan";
					else if (partner.contains(","))
						partner = "\"" + partner + "\"";

					data.append(record.get(idx[0]) + ",").append(record.get(idx[1]) + ",").append(reporter + ",")
							.append(partner + ",");
					String commodity = record.get(idx[5]);
					if (commodity.contains(","))
						commodity = "\"" + commodity + "\"";
					data.append(cc + ",").append(commodity + ",").append(record.get(idx[6]) + ",")
							.append(record.get(idx[7]) + ",");
					data.append(record.get(idx[8]) + ",").append(record.get(idx[9]) + "\r\n");
				}
			} else {
				dest = "F:/Data/UNComtrade/China-Import/";
				String reporter = "China";
				for (CSVRecord record : csvParser) {
					int size = record.size();
					if (size == 22)
						idx = index[0];
					else if (size == 35)
						idx = index[1];

					if (!record.get(idx[3]).equalsIgnoreCase("China"))
						continue;

					String cc = record.get(idx[4]);
					if (cc.trim().length() < 6)// leaf
						continue;

					String partner = record.get(idx[2]);
					if (partner.contains("Other Asia, nes"))
						partner = "Taiwan";
					else if (partner.contains(","))
						partner = "\"" + partner + "\"";

					data.append(record.get(idx[0]) + ",").append(record.get(idx[1]) + ",").append(reporter + ",")
							.append(partner + ",");
					String commodity = record.get(idx[5]);
					if (commodity.contains(","))
						commodity = "\"" + commodity + "\"";
					data.append(cc + ",").append(commodity + ",").append(record.get(idx[6]) + ",")
							.append(record.get(idx[7]) + ",");
					data.append(record.get(idx[8]) + ",").append(record.get(idx[9]) + "\r\n");
				}
			}

			File store = new File(dest + csvFile.getName());
			if (!store.exists())
				FileUtils.writeStringToFile(store, head + "\r\n" + data.toString(), "utf-8", true);
			else
				FileUtils.writeStringToFile(store, data.toString(), "utf-8", true);
		}
	}

	static Map<Integer, String> getCodeNameTable(String directory) throws IOException {
		List<Path> files = Files.list(Paths.get(directory)).collect(Collectors.toList());

		Map<Integer, String> map = new HashMap<>();
		files.stream().forEach(path -> {
			String name = path.toFile().getName();
			int index = name.indexOf("-");
			if (index > -1)
				map.put(Integer.parseInt(name.substring(0, index)), name.substring(index + 1));
		});
		return map;
	}

	public static void aggregateComtrade(String year) throws IOException {
		String base = "/Users/chjiang/Documents/data/";
		Map<Integer, String> table = getCodeNameTable(base + "Export/");
		
		double[][] trade = new double[table.size()][table.size()];

		List<Integer> codes = new ArrayList<>();
		codes.addAll(table.keySet());
		codes.remove(codes.indexOf(490));
		codes.sort((a, b) -> a.compareTo(b));

		for (Map.Entry<Integer, String> entry : table.entrySet()) {
			if(entry.getKey() == 490)
				continue;
			File csvData = new File(base + "Export/" + entry.getKey() + "-" + entry.getValue() + "/" + year + ".csv");
			CSVParser parser = CSVParser.parse(csvData, StandardCharsets.UTF_8, CSVFormat.RFC4180);
			List<Pair<Integer, Float>> data = new ArrayList<>();

			int prtidx = -1, validx = -1;
			int prt;
			float val;
			boolean find = false;
			for (CSVRecord record : parser) {
				if (!find) {
					for (int i = 0; i < record.size(); i++) {
						if (record.get(i).toLowerCase().contains("classifi")) {
							find = true;
							for (; i < record.size(); i++)
								if (record.get(i).contains("Partner Code"))
									prtidx = i;
								else if (record.get(i).trim().startsWith("Trade Value")) {
									validx = i;
									break;
								}
							break;
						}
					}
				} else {
					String code = record.get(prtidx);
					if (code.isEmpty() || code.equals("0"))
						continue;

					prt = Integer.parseInt(code);
					val = Float.parseFloat(record.get(validx)) / 1000.0F;
					data.add(Pair.of(prt, val));
				}
			}

			Map<Integer, Double> ret = data.stream()
					.collect(Collectors.groupingBy(p -> p.getLeft(), Collectors.summingDouble(p -> p.getRight())));
			System.out.println(entry.getKey() + "-" + entry.getValue() + " : " + ret.size());

			
			int rptId = codes.indexOf(entry.getKey());// reporter
			for (int code : ret.keySet()) {
				int prtId = codes.indexOf(code);// partner
				if (prtId == -1)
					continue;
				trade[rptId][prtId] = ret.get(code);
			}
		}

		StringBuilder sb = new StringBuilder();
		for (int code : codes){
			sb.append("\t" + table.get(code));
		}
		sb.append("\n");

		for (int i = 0; i < codes.size(); i++) {
			sb.append(table.get(codes.get(i)));
			for (int j = 0; j < codes.size(); j++)
				sb.append("\t" + String.format("%.5f", trade[i][j]));
			sb.append("\n");
		}
		FileUtils.write(new File(base + year + ".csv"), sb.toString(), "utf-8");
	}

	public static void main(String[] ags) throws IOException {
		aggregateComtrade("2013");
		aggregateComtrade("2014");
	}
}
