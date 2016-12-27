package com.horsehour.search.ip;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * util for dealing with ip
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130430
 */
public class IPUtil {
	private static StringBuilder sb = new StringBuilder();

	/**
	 * 从ip的字符串形式得到字节数组形式
	 * 
	 * @param ip
	 *            字符串形式的ip
	 * @return 字节数组形式的ip
	 */
	public static byte[] getIpByteArrayFromString(String ip) {
		byte[] ret = new byte[4];
		StringTokenizer st = new StringTokenizer(ip, ".");
		ret[0] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
		ret[1] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
		ret[2] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
		ret[3] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
		return ret;
	}

	/**
	 * @param ip
	 *            ip的字节数组形式
	 * @return 字符串形式的ip
	 */
	public static String getIPString(byte[] ip) {
		sb.delete(0, sb.length());
		sb.append(ip[0] & 0xFF);
		sb.append('.');
		sb.append(ip[1] & 0xFF);
		sb.append('.');
		sb.append(ip[2] & 0xFF);
		sb.append('.');
		sb.append(ip[3] & 0xFF);
		return sb.toString();
	}

	/**
	 * 根据某种编码方式将字节数组转换成字符串
	 * 
	 * @param b
	 *            字节数组
	 * @param offset
	 *            要转换的起始位置
	 * @param len
	 *            要转换的长度
	 * @param enc
	 *            编码方式
	 * @return 如果encoding不支持，返回一个缺省编码的字符串
	 */
	public static String getString(byte[] b, int offset, int len, String enc) {
		try {
			return new String(b, offset, len, enc);
		} catch (UnsupportedEncodingException e) {
			return new String(b, offset, len);
		}
	}

	/**
	 * @param url
	 * @return 从url字符串中解析出其ip地址(可能有多个)
	 */
	public static String[] decodeIPs(String url) {
		InetAddress[] addresses = null;
		try {
			addresses = InetAddress.getAllByName(new URL(url).getHost());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}

		String[] ips = null;
		if (addresses != null) {
			ips = new String[addresses.length];
			for (int i = 0; i < ips.length; i++)
				ips[i] = addresses[i].getHostAddress();
		}
		return ips;
	}

	/**
	 * @param url
	 * @return 从url字符串中解析出其ip地址(仅取一个)
	 */
	public static String decodeIP(String url) {
		InetAddress addresses = null;
		try {
			addresses = InetAddress.getByName(new URL(url).getHost());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
		return addresses.getHostAddress();
	}
}
