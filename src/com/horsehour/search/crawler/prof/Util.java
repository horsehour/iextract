package com.horsehour.search.crawler.prof;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2016年1月23日 下午10:06:37
 **/
public class Util {
	/**
	 * 读取文件中的键值对(一行一对,无重复)
	 * 
	 * @param src
	 * @param enc
	 * @param map
	 */
	public static void loadResource(String src, String enc, Map<String, String> map){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(src), enc));
			String line = "";
			int idx = 0;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				idx = line.indexOf("=");
				if (idx > 1)
					map.put(line.substring(0, idx), line.substring(idx + 1));
			}
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 读取文件中键值对（循环读取,有重复）,count行构成一组
	 * 
	 * @param br
	 * @param map
	 * @param count
	 */
	public static void loadResource(BufferedReader br, HashMap<String, String> map, int count){
		String line = "";
		int idx = 0;
		try {
			while ((line = br.readLine()) != null && map.size() < count) {
				idx = line.indexOf("=");
				if (idx != -1) {
					line = line.trim();
					map.put(line.substring(0, idx), line.substring(idx + 1));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 加载属性文件到Properties对象
	 * 
	 * @param propFile
	 * @return Properties
	 */
	public static Properties loadResource(String propFile){
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(propFile);
			prop.load(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return prop;
	}
}
