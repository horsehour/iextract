package com.horsehour.search.ip;

/**
 * <p>
 * IP条目，记录信息包括国家、地区，起始与终止IP
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130430
 */
public class IPEntry {
	public String beginIp;
	public String endIp;

	public String country;
	public String area;

	public IPEntry() {
		beginIp = endIp = country = area = "";
	}
}
