package com.horsehour.search.crawler.atp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * Problem Parser
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20160326
 */
public class ATPProblemParser implements Callable<String> {
	public String dest;
	public String link;

	public ATPProblemParser(String link, String dest) {
		this.link = link;
		this.dest = dest;
	}

	@Override
	public String call() throws IOException{
		String content = ATPCrawler.get(link, "utf-8");
		if (content == null || content.isEmpty())
			return null;//留下次处理

		String regex = "<pre>[\\s\\S]+<pre/>";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		if (matcher.find()) {
			content = matcher.group().trim();
			content = content.replace("<pre>", "").replace("<pre/>", "");
			int endLineIdx = content.indexOf("\r\n");
			String head = content.substring(content.indexOf(":") + 1, endLineIdx).trim();
			String[] field = head.split(",");
			if (field[0].startsWith("t"))
				return link;//视为访问过,但不保存

			FileUtils.write(new File(dest + "/Problems/" + field[0] + "__" + field[1] + ".txt"), content,"");
		}
		return link;
	}
}