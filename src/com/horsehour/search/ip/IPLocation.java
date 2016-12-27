package com.horsehour.search.ip;

/**
 * 用来封装ip相关信息，当前版本仅有两个字段，ip所在的国家和地区
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130430
 */
public class IPLocation {
	private String country;
	private String area;

	public IPLocation() {
		country = area = "";
	}

	public IPLocation copy() {
		IPLocation ret = new IPLocation();
		ret.country = country;
		ret.area = area;
		return ret;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		if (area.trim().equals("CZ88.NET"))
			area = "本机或本网络";
		else
			this.area = area;
	}

	public String toString() {
		return country + ":" + area;
	}
}
