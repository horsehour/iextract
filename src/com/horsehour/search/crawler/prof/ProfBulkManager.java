package com.horsehour.search.crawler.prof;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import weka.core.SerializationHelper;

import com.horsehour.util.TickClock;

/**
 * ProfBulkManager管理导师文件，实现导师列表信息的原子化、合并、查重等功能
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130313
 */
public class ProfBulkManager {
	/**
	 * 把同一个文件中的多个导师信息拆分为多个professor文件,后缀为prof
	 * 
	 * @param profBulkFile
	 * @return atom directory
	 */
	public static String atom(String profBulkFile){
		HashMap<String, String> profMaps;
		String temp = new File(profBulkFile).getParent() + "/atom_" + Math.random() + "/";
		File atom = new File(temp);
		if (!atom.exists())
			atom.mkdir();// 创建临时文件夹

		BufferedReader br = null;
		Professor prof = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(profBulkFile)));
			while (br.ready()) {
				profMaps = new HashMap<String, String>();
				// 从profBulkFile文件中加载导师信息
				Util.loadResource(br, profMaps, Professor.memberNum);
				prof = new Professor(profMaps);

				String dest = temp + prof.getDept() + "-" + prof.getName() + ".prof";

				if (new File(dest).exists()) {
					System.out.println("重名:" + prof.getName());
					continue;
				}
				SerializationHelper.write(dest, prof);// 将导师信息序列化
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return temp;
	}

	public static void serializeList(String src, String enc, String dest){
		Map<String, String> list = new HashMap<String, String>();
		Map<String, Vector<String>> bulk = new HashMap<String, Vector<String>>();
		Util.loadResource(src, enc, list);
		Set<String> keySet = list.keySet();
		for (String key : keySet) {
			String[] entries = list.get(key).split("\t");
			Vector<String> val = new Vector<String>();
			for (String entry : entries)
				val.add(entry);
			bulk.put(key, val);
		}
		try {
			SerializationHelper.write(dest, bulk);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 合并两个prof文件：使用第二个文件来补充完善第一个prof文件的信息，第一个作为基准
	 * 
	 * @param profBase
	 * @param profFile
	 */
	public static void mergeProfs(String profBase, String profFile){
		Professor prof1 = null, prof2 = null;
		try {
			prof1 = (Professor) SerializationHelper.read(profBase);
			prof2 = (Professor) SerializationHelper.read(profFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String temp = new File(profBase).getParentFile().getParent() + "/merge/";
		File mergeFile = new File(temp);
		if (!mergeFile.exists())
			mergeFile.mkdir();

		if (prof1.getGender().isEmpty())
			prof1.setGender(prof2.getGender());
		if (prof1.getBirth().isEmpty())
			prof1.setBirth(prof2.getBirth());
		if (prof1.getEmail().isEmpty())
			prof1.setEmail(prof2.getEmail());
		if (prof1.getPhone().isEmpty())
			prof1.setPhone(prof2.getPhone());
		if (prof1.getTitle().isEmpty())
			prof1.setTitle(prof2.getTitle());
		if (prof1.getLevel().isEmpty())
			prof1.setLevel(prof2.getLevel());
		if (prof1.getDegree().isEmpty())
			prof1.setDegree(prof2.getDegree());
		if (prof1.getField().isEmpty())
			prof1.setField(prof2.getField());
		if (prof1.getSubject().isEmpty())
			prof1.setSubject(prof2.getSubject());

		String resume1 = prof1.getResume();
		String resume2 = prof2.getResume();
		if (resume1.length() <= resume2.length())
			prof1.setResume(prof2.getResume());

		// resume1 = prof1.getResume();
		// resume1 += "<p>（参考信息源：<a href=\"" + prof2.getSrc() + "\">" +
		// prof2.getSrc() + "</a>）</p>";
		// prof1.setResume(resume1);
		// prof1.setDept(prof2.getDept());

		try {
			SerializationHelper.write(temp + prof1.getDept() + "-" + prof1.getName() + ".prof", prof1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new File(profFile).delete();// 将profFile删除
		new File(profBase).delete();// 将profBase删除

		// FileManager.write(prof1.toString(), temp + prof1.getDept() + "-" +
		// prof1.getName() + ".txt");
	}

	/**
	 * 合并两个文件夹内的所有prof文件
	 * 
	 * @param base
	 * @param comp
	 * @return 合并文件夹
	 */
	public static File mergeBulkProfs(String base, String comp){
		File destDir = new File(new File(base).getParent() + "/merge/");
		if (!destDir.exists())
			destDir.mkdir();

		String dest = destDir.getAbsolutePath();

		for (File file2 : FileUtils.listFiles(new File(comp), null, false)) {
			String name2 = file2.getName();
			boolean found = false;
			for (File file1 : FileUtils.listFiles(new File(base), null, false)) {
				if (file1.getName().equalsIgnoreCase(name2)) {
					mergeProfs(file1.toString(), file2.toString());
					found = true;
					break;
				}
			}
			// 如果没有找到，则将文件直接转移到merge文件夹
			if (!found) {
				file2.renameTo(new File(dest + "/" + name2));
				System.out.println(name2);
			}
		}

		// 将文件夹1部分没有匹配的文件直接转移到merge文件夹
		for (File file1 : FileUtils.listFiles(new File(base), null, false))
			file1.renameTo(new File(dest + "/" + file1.getName()));

		// 删除两个文件夹
		new File(base).delete();
		new File(comp).delete();

		return destDir;
	}

	/**
	 * 合并文件夹下全部prof文件到merge文件中
	 * 
	 * @param path
	 * @throws IOException 
	 */
	public static void mergeProfList(String path) throws IOException{
		String fileName = "";
		Professor prof = null;
		for (File file : FileUtils.listFiles(new File(path), null, false)) {
			fileName = file.getName();
			if (fileName.endsWith(".prof")) {
				try {
					prof = (Professor) SerializationHelper.read(file.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileUtils.write(new File(path + "/merge.txt"), prof.toString(),"");
			}
		}
	}

	/**
	 * 使用源src中的导师信息补充dest导师信息
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static void complement(String src, String dest) throws IOException{
		String serialized = new File(src).getParent() + "/temp";
		String profFile = new File(src).getParent() + "/profs.txt";

		// 将src中的导师信息载入到profBulk
		serializeList(src, "gbk", serialized);
		HashMap<String, Vector<String>> profBulk = null;
		try {
			profBulk = (HashMap<String, Vector<String>>) SerializationHelper.read(serialized);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new File(serialized).delete();// 删除

		// 将profBulk导出到profFile中去
		populateProf(profBulk, profFile);

		mergeProfList(mergeBulkProfs(atom(dest), atom(profFile)).toString());
	}

	/**
	 * 使用profBulk填充professor
	 * 
	 * @param profBulk
	 * @throws IOException 
	 */
	public static void populateProf(HashMap<String, Vector<String>> profBulk, String file) throws IOException{
		Set<Entry<String, Vector<String>>> entries = profBulk.entrySet();
		Vector<String> value = null;
		Professor prof = null;

		String propFile = "./src/node/person/professor/config.properties";

		Properties prop = new Properties();
		prop.load(new FileInputStream(propFile));

		for (Entry<String, Vector<String>> entry : entries) {
			prof = new Professor();
			prof.setUniv(prop.getProperty("univ"));
			prof.setDept(prop.getProperty("dept"));

			String name = entry.getKey();
			value = entry.getValue();
			prof.setName(name);
			prof.setSrc("http://history.nankai.edu.cn/PublicView/PersonnelManage.aspx");

			System.out.println(name);

			prof.setGender(value.get(0));
			// prof.setBirth(value.get(1));
			int sz = value.size();
			// if(sz > 2)
			// prof.setDegree(value.get(2));
			if (sz > 2)
				prof.setTitle(value.get(2));
			// if(sz > 4)
			// prof.setLevel(value.get(4));
			// if(sz > 5)
			// prof.setField(value.get(5));

			// prof.setDept(value.get(2));
			// prof.setPhone(value.get(2));
			// prof.setEmail(value.get(3));
			// prof.setSubject(value.get(1));

			FileUtils.write(new File(file), prof.toString(),"");
		}
	}

	/**
	 * 处理导师信息,比如从resume中提取其他信息
	 * @throws IOException 
	 */
	public static void processProf(String dir) throws IOException{
		String fileName = "";
		Professor prof = null;
		String gender = "", birth = "", phone = "", field = "";
		String resume = "";
		for (File file : FileUtils.listFiles(new File(dir), null, false)) {
			fileName = file.getName();
			if (fileName.endsWith(".prof")) {
				try {
					prof = (Professor) SerializationHelper.read(file.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				resume = prof.getResume();

				gender = prof.getGender();
				if (gender.isEmpty()) {
					gender = ProfParser.parseRegex(resume, "性别：[\\s\\S]+?<br/>");
					gender = gender.replaceAll("<[^>]*>|性别|&nbsp;|：", "").trim();
					prof.setGender(gender);
				}

				birth = prof.getBirth();
				if (birth.isEmpty()) {
					birth = ProfParser.parseRegex(resume, "出生日期[\\s\\S]+?<br/>");
					birth = birth.replaceAll("<[^>]*>|出生日期|&nbsp;|：", "").trim();
					prof.setBirth(birth);
				}

				phone = prof.getPhone();
				if (phone.isEmpty()) {
					phone = ProfParser.parseRegex(resume, "办公电话[\\s\\S]+?</p>");
					phone = phone.replaceAll("<[^>]*>|办公电话|&nbsp;|：", "").trim();
					prof.setPhone(phone);
				}

				field = prof.getField();
				if (field.isEmpty()) {
					field = ProfParser.parseRegex(resume, "科研方向[\\s\\S]+?<br/>");
					field = field.replaceAll("<[^>]*>|科研方向|&nbsp;|：", "").trim();
					prof.setField(field);
				}
				FileUtils.write(new File(dir + "/merge.txt"), prof.toString(),"");
			}
		}
	}

	public static void main(String[] args) throws IOException{
		TickClock.beginTick();

		String file = "C:/Users/dell/Desktop/历史学院-ProfBulk.txt";
		processProf(atom(file));

		TickClock.stopTick();
	}
}