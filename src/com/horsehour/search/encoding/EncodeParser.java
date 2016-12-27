package com.horsehour.search.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取文件的字符编码
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20110707
 */
public class EncodeParser {

	/**
	 * @param url
	 * @return Encoding of Web Page
	 */
	public static String parseEncode(String url) {
		URLConnection conn;
		try {
			conn = new URL(url).openConnection();
			conn.setConnectTimeout(6000);
			conn.setReadTimeout(6000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String enc = parseContentType(url, conn);
		if (enc == null || enc.isEmpty())
			enc = parseMeta(conn);
		return enc;
	}

	/**
	 * Parse CharSet from the Parameter Content-Type of Response Head
	 * 
	 * @param url
	 * @param conn
	 * @return Content Type of Web Page
	 */
	private static String parseContentType(String url, URLConnection conn) {
		// Content-Type Parameter in Response Head
		String contentType = conn.getContentType();
		if (contentType == null || contentType.isEmpty())
			return null;

		String charset = null;
		String[] slice = contentType.toLowerCase().split(";");
		if ((slice.length == 2) && (contentType.startsWith("text"))) {
			charset = slice[1].trim();
			charset = charset.replace("charset", "").replace("=", "").trim();
		}
		return charset;
	}

	/**
	 * Parse Charset from Meta Information
	 * 
	 * @param conn
	 * @return The CharSet of Web Page
	 */
	private static String parseMeta(URLConnection conn) {
		final int len = 1024;
		byte[] head = new byte[len];
		try {
			InputStream in = conn.getInputStream();
			int size = 0, offset = 0;
			while ((size = in.read(head, offset, len - offset)) > 0)
				offset += size;

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// default - the platform's charset
		String header = new String(head);
		final String tag = "charset=";
		int offIdx = header.indexOf(tag);
		if (offIdx == -1)
			return null;

		int endIdx = header.indexOf(">", offIdx + tag.length());
		if (endIdx == -1)
			return null;

		String enc = null;
		header = header.substring(offIdx + tag.length(), endIdx).trim();
		String regex = "[\\p{Alnum}|-]{3,}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(header);
		if (matcher.find())
			enc = matcher.group().trim();
		return enc;
	}
}