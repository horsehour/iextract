package com.horsehour.search.crawler.prof;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

/**
 * 构造的导师类，主要用于存储导师信息
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2012-08-13
 */
public class Professor implements Serializable {
	private static final long serialVersionUID = 1L;

	private String src;// 信息来源

	private String name;
	private String gender;
	private String birth;

	private String univ;// 工作院校
	private String dept;

	private String email;
	private String phone;

	private String title;// 职称，如教授、副教授等
	private String level;// 导师级别，如硕导、博导
	private String degree;// 学历
	private String expert;// 专家类型，如院士等

	private String subject;// 专业或招生专业-一级学科或者二级学科
	private String field;// 研究领域

	// 个人简历，包含各种详细的介绍信息，可能包含大量的内容
	private String resume;

	public final static int memberNum = 15;// 成员变量的个数

	public Professor() {
		name = "";
		gender = "";
		birth = "";// 人的信息

		univ = "";
		dept = "";
		email = "";
		phone = "";// 联系方式

		title = "";
		level = "";
		degree = "";
		expert = "";// 学术地位

		subject = "";
		field = ""; // 学科方向

		resume = "";// 个人简历
	}

	/**
	 * 基本信息都存储在map中执行初始化
	 * 
	 * @param map
	 */
	public Professor(HashMap<String, String> map) {
		this();// 默认首先初始化
		int sz = map.size();
		if (sz > 0) {
			src = map.get("Src");
			name = map.get("Name");
			gender = map.get("Gender");
			birth = map.get("Birth");

			univ = map.get("Univ");
			dept = map.get("Dept");

			email = map.get("Email");
			phone = map.get("Phone");

			title = map.get("Title");
			level = map.get("Level");
			degree = map.get("Degree");
			expert = map.get("Expert");

			subject = map.get("Subject");
			field = map.get("Field");

			resume = map.get("Resume");
		}
	}

	/**
	 * 如果文件按照键值对的形式存储导师信息，解析并初始化 如果多个导师信息放到同一个文件中,每个导师信息按照格式存放到一个文件中,可以
	 * 通过循环调用getInstance取得，但不推荐
	 * 
	 * @param src
	 * @param srcEncoding
	 * @return Professor
	 */
	public Professor getInstance(String src, String srcEncoding){
		HashMap<String, String> map = new HashMap<String, String>();
		Util.loadResource(src, srcEncoding, map);
		return new Professor(map);
	}

	/**
	 * 将导师信息导出到map容器中
	 * 
	 * @param map
	 */
	public void export(HashMap<String, String> map){
		map.put("Src", src);
		map.put("Name", name);
		map.put("Gender", gender);
		map.put("Birth", birth);

		map.put("Univ", univ);
		map.put("Dept", dept);

		map.put("Email", email);
		map.put("Phone", phone);

		map.put("Title", title);
		map.put("Level", level);
		map.put("Degree", degree);
		map.put("Expert", expert);

		map.put("Subject", subject);
		map.put("Field", field);

		map.put("Resume", resume);
	}

	/**
	 * 将导师信息拼连并导出到指定文件中
	 * 
	 * @param dest
	 * @throws IOException 
	 */
	public void export(String dest) throws IOException{
		FileUtils.write(new File(dest), toString(),"");
	}

	public String getSrc(){
		return src;
	}

	public void setSrc(String src){
		this.src = src;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getGender(){
		return gender;
	}

	public void setGender(String gender){
		this.gender = gender;
	}

	public String getBirth(){
		return birth;
	}

	public void setBirth(String birth){
		this.birth = birth;
	}

	public String getUniv(){
		return univ;
	}

	public void setUniv(String univ){
		this.univ = univ;
	}

	public String getDept(){
		return dept;
	}

	public void setDept(String dept){
		this.dept = dept;
	}

	public String getEmail(){
		return email;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getPhone(){
		return phone;
	}

	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getLevel(){
		return level;
	}

	public void setLevel(String level){
		this.level = level;
	}

	public String getDegree(){
		return degree;
	}

	public void setDegree(String degree){
		this.degree = degree;
	}

	public String getExpert(){
		return expert;
	}

	public void setExpert(String expert){
		this.expert = expert;
	}

	public String getSubject(){
		return subject;
	}

	public void setSubject(String subject){
		this.subject = subject;
	}

	public String getField(){
		return field;
	}

	public void setField(String field){
		this.field = field;
	}

	public String getResume(){
		return resume;
	}

	public void setResume(String resume){
		this.resume = resume;
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();

		sb.append("Src=" + src + "\r\n");// 页面

		sb.append("Name=" + name + "\r\n");// 姓名
		sb.append("Gender=" + gender + "\r\n");// 性别
		sb.append("Birth=" + birth + "\r\n");// 出生日期

		sb.append("Univ=" + univ + "\r\n");// 院校
		sb.append("Dept=" + dept + "\r\n");// 院系
		sb.append("Email=" + email + "\r\n");// 邮箱
		sb.append("Phone=" + phone + "\r\n");// 电话

		sb.append("Title=" + title + "\r\n");// 职称
		sb.append("Level=" + level + "\r\n");// 级别（硕导/博导）
		sb.append("Degree=" + degree + "\r\n");// 学历
		sb.append("Expert=" + expert + "\r\n");// 专家类型

		sb.append("Subject=" + subject + "\r\n");// 专业
		sb.append("Field=" + field + "\r\n");// 研究领域（方向、兴趣）

		sb.append("Resume=" + resume + "\r\n");// 简历
		sb.append("\r\n");

		return sb.toString();
	}
}