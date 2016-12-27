package com.horsehour.search.crawler.prof;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;

import weka.core.SerializationHelper;

import com.horsehour.search.crawler.ZhiZhu;
import com.horsehour.util.JDBCUtil;
import com.horsehour.util.TickClock;

/**
 * ProfNet是提取、生成、导入导出Professor信息的类
 * 主要集成了ProfParser、ProfTemplate、ProfPorter和Professor三个类的相应操作
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20120814
 */
public class ProfNet {
	private Properties prop;// 属性文件

	private Professor prof;// 导师对象
	private ProfTemplate profTemplate;// 页面模版
	private ProfParser profParser;// 解析器
	private String imageURLFeature;// 图片地址特征词

	/**
	 * 初始化信息全部在properties文件propFile中
	 */
	public void init(String propFile){
		prop = Util.loadResource(propFile);
	}

	/**
	 * 初始化Professor对象
	 */
	public void setProfessor(){
		prof = new Professor();
	}

	/**
	 * 设置模版
	 * 
	 * @param template
	 */
	public void setProfTemplate(ProfTemplate template){
		profTemplate = template;
	}

	/**
	 * 设置解析器
	 * 
	 * @param parser
	 */
	public void setProfParser(ProfParser parser){
		profParser = parser;
	}

	public void setProfParser(Parser parser){
		profParser = new ProfParser(parser);
	}

	public void setProfParser(String url, String encoding){
		profParser = new ProfParser(url, encoding);
	}

	/**
	 * 将Professor对象拼连并保存到属性文件指定的目标文件
	 * @throws IOException 
	 */
	public void archiveProf() throws IOException{
		postProcess();// 后处理

		String dest = prop.getProperty("fileRoot") + prof.getUniv();
		String dept = prop.getProperty("dept");

		if (dept.isEmpty() || dept == null)
			dest += "/ProfBulk.txt";
		else
			dest += "/" + dept + "-ProfBulk.txt";
		FileUtils.write(new File(dest), prof.toString(),"");
	}

	/**
	 * 将数据上传给数据库
	 * 
	 * @param src
	 * @param enc
	 * @param lineCount
	 */
	public void submit(String src, String enc, int lineCount){
		Connection conn = JDBCUtil.getConnection();
		ProfPorter.insertDatumFrom(src, enc, lineCount, conn);
	}

	/**
	 * 如果头像缺失，创建空文件（便于手动补充）
	 * 
	 * @param filePath
	 * @return boolean
	 */
	public boolean markImage(String filePath){
		File file = new File(filePath);
		boolean ret = false;
		try {
			ret = file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return ret;
	}

	/**
	 * 设置图片特征，比如图片链接地址所包含的关键词
	 * 
	 * @param featureKeywords
	 */
	public void setImageURLFeature(String featureKeywords){
		this.imageURLFeature = featureKeywords;
	}

	public String getImageURLFeature(){
		return imageURLFeature;
	}

	// TODO:<活动成员方法>***********************************************************
	/**
	 * 提取导师列表
	 * 
	 * @param profBulk
	 */
	public void extractProfList(HashMap<String, Vector<String>> profBulk, boolean isTable){
		if (isTable)
			extractProfListFromTable(profBulk);
		else
			extractProfListFromPlain(profBulk);
	}

	/**
	 * 从普通页面中提取导师信息列表
	 * 
	 * @param profBulk
	 */
	public void extractProfListFromPlain(HashMap<String, Vector<String>> profBulk){
		NodeList nodeList = profParser.getNodeList(profTemplate.getNameFilter());
		int sz = nodeList.size();
		int count = 0;// 重名的个数
		Node node = null;
		String name = "", link = "";
		// String title = "", level = "", degree = "", img = "", field = "",
		// phone = "", email = "", gender = "", subject = "", expert = "", dept
		// = "", resume = "";

		for (int id = 0; id < sz; id++) {
			node = nodeList.elementAt(id);
			if (node instanceof LinkTag) {
				link = ((LinkTag) node).getLink();
				if (link.contains("szdw/") && link.contains(".html"))
					name = ((LinkTag) node).getLinkText();
				else
					continue;
			}

			name = name.replaceAll("&nbsp;|\t| |　|", "").trim();
			if (name.isEmpty())
				continue;

			if (link.contains("#")) {
				System.out.println(name + ":链接无效！");
				continue;
			} else if (!link.startsWith("http"))
				link = "x" + link;

			System.out.println(name);

			if (profBulk.containsKey(name)) {
				System.out.println("重名:" + name);
				count++;
				name += "(" + count + ")";
			}
			Vector<String> val = new Vector<String>();
			if (!name.isEmpty()) {
				val.add(link);
				profBulk.put(name, val);
			}
		}
	}

	/**
	 * 从表格中提取导师信息列表
	 * 
	 * @param profBulk
	 */
	public void extractProfListFromTable(HashMap<String, Vector<String>> profBulk){
		NodeList nodeList = profParser.getNodeList(profTemplate.getNameFilter());
		// int sz = nodeList.size();
		int count = 0;// 重名的个数
		Node node = null;
		String name = "", link = "";
		// String title = "", level = "", degree = "", img = "", field = "",
		// phone = "", email = "", gender = "", subject = "", expert = "", dept
		// = "";

		node = nodeList.elementAt(0);
		TableRow[] rows = ((TableTag) node).getRows();
		int rowCount = rows.length;
		for (int rowId = 1; rowId < rowCount; rowId++) {
			TableRow row = rows[rowId];
			TableColumn[] cols = row.getColumns();
			int colCount = cols.length;

			Vector<String> val = new Vector<String>();
			for (int colId = 0; colId < colCount; colId++) {
				if (colId == 1) {
					node = cols[colId].getFirstChild();
					if (node instanceof LinkTag) {
						link = ((LinkTag) node).getLink();
						name = ((LinkTag) node).getLinkText();
					} else
						continue;
					name = node.toPlainTextString();
					name = name.replaceAll("&nbsp;|\r|\n|\t| ", "");
					if (profBulk.containsKey(name)) {
						System.out.println("重名:" + name);
						count++;
						name += "(" + count + ")";
					} else
						System.out.println(name);

					val.add(link);
				} else
					val.add(cols[colId].toPlainTextString().replaceAll("&nbsp;|\r|\n|\t| ", ""));
			}
			profBulk.put(name, val);
		}
	}

	/**
	 * 下载头像
	 * 
	 * @param profName
	 */
	public void harvestPhoto(String profName){
		String imgURL = "";
		NodeFilter photoFilter = profTemplate.getPhotoFilter();
		if (photoFilter != null) {
			NodeList nodeList = profParser.getNodeList(photoFilter);
			Node node = null;
			int sz = 0;
			if (nodeList == null || (sz = nodeList.size()) == 0)
				return;

			for (int id = 0; id < sz; id++) {
				node = nodeList.elementAt(id);
				imgURL = ((ImageTag) node).getImageURL();

				if (imgURL.toLowerCase().contains(getImageURLFeature()))
					break;
				else
					imgURL = "";
			}

			// node = nodeList.elementAt(0);
			// imgURL = ((ImageTag)node).getImageURL();
			// if(!imgURL.isEmpty() && !imgURL.startsWith("http"))
			// imgURL = "x" + imgURL;
		}
		harvestPhoto(imgURL, profName);
		profParser.reset();
	}

	/**
	 * 直接根据提供的链接地址下载头像
	 * 
	 * @param imgURL
	 * @param profName
	 */
	public void harvestPhoto(String imgURL, String profName){
		String dest = prop.getProperty("fileRoot") + prof.getUniv() + "/" + prof.getDept() + "/";
		profName = prof.getDept() + "-" + profName + ".jpg";
		dest += profName;

		if (imgURL.isEmpty())
			markImage(dest);
		else
			ZhiZhu.getBinary(imgURL, dest);
	}

	/**
	 * 提取简介部分
	 */
	public void harvestResume(){
		// NodeList nodeList =
		// profParser.getNodeList(profTemplate.getResumeFilter());
		String resume = profParser.getVisitorTrace(ProfTemplate.getTheVisitor(), profTemplate.getResumeFilter());
		resume = resume.replaceAll(" +|　+", " ").trim();
		resume = resume.replaceAll("\t|\r|\n", "").trim();
		int idx = 0;
		while (idx != -1) {
			resume = resume.replaceAll("<br/><br/>", "<br/>").trim();
			idx = resume.indexOf("<br/><br/>");
		}
		prof.setResume(resume);
		profParser.reset();
	}

	/**
	 * 从简历中提取其他信息-头部
	 */
	public void harvestDetail(){
		String resume = prof.getResume();
		int len = resume.length();
		if (len > 0) {
			len = (len > 10240) ? 10240 : len;

			String briefResume = resume.substring(0, len);
			briefResume = briefResume.replaceAll(" +|&nbsp;|教授课程", "");

			if (prof.getDegree().isEmpty())
				prof.setDegree(ProfParser.parseDegree(briefResume));// 学位

			if (prof.getLevel().isEmpty())
				prof.setLevel(ProfParser.parseLevel(briefResume));// 级别

			// String title = ProfParser.parseRegex(briefResume,
			// "职务：[\\S]*?<br/>");
			// title = title.replaceAll("职务|：|<[^>]*>| ", "");
			// prof.setTitle(title);

			if (prof.getTitle().isEmpty())
				prof.setTitle(ProfParser.parseTitle(briefResume));// 职称

			if (prof.getExpert().isEmpty())
				prof.setExpert(ProfParser.parseExpert(briefResume));// 专家类型

			if (prof.getGender().isEmpty()) {
				// String regex = "性别[\\s\\S]*?<p>";
				// String gender = ProfParser.parseRegex(briefResume, regex);
				// gender = gender.replaceAll("性别|：|&nbsp;|<[^>]*>", "").trim();
				// prof.setGender(gender);

				prof.setGender(ProfParser.parseGender(briefResume));// 性别
			}

			if (prof.getBirth().isEmpty()) {
				// String regex = "出生日期[\\s\\S]*?国籍";
				// String birth = ProfParser.parseRegex(briefResume, regex);
				// birth = birth.replaceAll("&nbsp;|出生日期|国籍|<[^>]*>",
				// "").trim();
				// prof.setBirth(birth);

				prof.setBirth(ProfParser.parseBirth(briefResume));// 出生日期
			}

			if (prof.getPhone().isEmpty()) {
				prof.setPhone(ProfParser.parsePhone(briefResume));// 电话
				if (prof.getPhone().startsWith("7"))
					prof.setPhone("");
			}

			if (prof.getEmail().isEmpty())
				prof.setEmail(ProfParser.parseEmail(briefResume));// 邮箱

			if (prof.getField().isEmpty()) {
				// String regex =
				// "研究[方向|领域|兴趣]{1}?[\\s\\S]*?<p>|从事[\\s\\S]*?研究";
				// String regex = "科研方向[\\s\\S]*?教授课程";
				// String field = ProfParser.parseRegex(briefResume,regex);
				// field = field.replaceAll("</li>", "; ");
				// field = field.replaceAll("科研方向|教授课程|<[^>]*>","").trim();
				// field = field.replaceAll("研究方向：|<[^>]*>", "").trim();//剔除全部标签
				// field = ProfParser.removeNoiseIn(field);

				String field = ProfParser.parseField(briefResume);
				prof.setField(field);
			}

			// if(prof.getSubject().isEmpty()){
			// String regex = "招生专业[\\S]*?招生方向";
			// String subject = ProfParser.parseRegex(briefResume, regex);
			// subject = subject.replaceAll("招生专业|招生方向|：|&#160;|&nbsp;|<[^>]*>",
			// "").trim();
			// prof.setSubject(subject);
			// }
		}
	}

	/**
	 * 后处理
	 */
	public void postProcess(){
		String field = prof.getField();
		if (field.startsWith("为"))
			field = field.substring(1);
		else if (field.startsWith("包括"))
			field = field.substring(2);
		else if (field.startsWith("主要为"))
			field = field.substring(3);
		else if (field.startsWith("从事博士后研究"))
			field = field.substring(7);

		prof.setField(field.trim());

		String expert = prof.getExpert();
		if (expert.equals("院士")) {
			if (prof.getTitle().equals("副教授") || prof.getLevel().equals("硕导"))
				prof.setExpert("");
		}
	}

	/**
	 * 手动输入网址提取对象
	 * 
	 * @param profName
	 * @param profUrl
	 * @param enc
	 * @throws IOException 
	 */
	public void harvestProf(String profName, String profUrl, String enc) throws IOException{
		prof = new Professor();
		prof.setUniv(prop.getProperty("univ"));
		prof.setDept(prop.getProperty("dept"));

		prof.setName(profName);
		prof.setSrc(profUrl);

		// String html = TinyCrawler.fetchPageAt(profUrl, enc);
		// String html = FileIO.readFile(profUrl);
		// html = html.replaceAll("<![\\s\\S]*?>", "");
		// profParser = new ProfParser(Parser.createParser(html, enc));

		setProfParser(profUrl, enc);

		// harvestPhoto(profName);
		harvestResume();

		harvestDetail();
		archiveProf();
	}

	/**
	 * 启动提取程序
	 * 
	 * @param profBulk
	 * @param enc
	 * @throws IOException 
	 */
	public void start(HashMap<String, Vector<String>> profBulk, String enc) throws IOException{
		Set<Entry<String, Vector<String>>> entries = profBulk.entrySet();

		// 头像文件处理,没有相应的文件夹则创建新的
		String imageBase = prop.getProperty("fileRoot") + prop.getProperty("univ") + "/" + prop.getProperty("dept") + "/";
		File imageDir = new File(imageBase);
		if (!imageDir.exists())
			imageDir.mkdir();

		Vector<String> value = null;

		for (Entry<String, Vector<String>> entry : entries) {
			setProfessor();// 初始化Professor对象,防止重复
			prof.setUniv(prop.getProperty("univ"));
			prof.setDept(prop.getProperty("dept"));

			String name = entry.getKey();
			value = entry.getValue();
			String link = value.get(0);
			prof.setName(name);
			prof.setSrc(link);

			System.out.println(name);

			// 如果profParser无法直接解析,则抓取下来剔除噪声（如虚线注释）,然后构造新的profParser
			// String html = TinyCrawler.fetchPageAt(link, enc);
			// html = html.replaceAll("<![\\s\\S]*?>", "");
			// profParser = new ProfParser(Parser.createParser(html, enc));

			profParser = new ProfParser(link, enc);
			if (profParser.getParser() == null) {
				System.out.println(name + ":页面异常！");
				continue;
			}

			harvestPhoto(name);
			harvestResume();

			// prof.setGender(value.get(1));
			// prof.setBirth(value.get(2));
			// prof.setTitle(value.get(3));
			// prof.setField(value.get(4));

			// if(value.size() > 2)
			// prof.setDegree(value.get(2));
			// if(value.size() > 2)
			// prof.setLevel(value.get(2));
			// if(value.size() > 3)
			// prof.setField(value.get(3));
			// prof.setPhone(value.get(3));
			// prof.setEmail(value.get(1));
			// prof.setExpert(value.get(2));
			// prof.setSubject(value.get(2));
			// harvestPhoto(value.get(1), name);

			harvestDetail();
			// int idx = -1;
			// String field = prof.getField();
			// idx = field.indexOf("联系电话");
			// if(idx != -1)
			// prof.setField(field.substring(0, idx));

			archiveProf();// 保存prof
		}
	} // TODO:</活动成员方法>***********************************************************

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception{
		TickClock.beginTick();

		ProfNet profNet = new ProfNet();
		String propFile = "./src/node/person/professor/config.properties";
		profNet.init(propFile);

		ProfTemplate template = new ProfTemplate();

		// TODO:<活动模块>========================================================
		HashMap<String, Vector<String>> profBulk = new HashMap<String, Vector<String>>();

		// （１）设置【导师列表页面】的编码encProfList和【导师页面】的编码encProf
		String root = profNet.prop.getProperty("fileRoot") + profNet.prop.getProperty("univ"),
		// src = "C:/Users/dell/Desktop/ProfList.html",
		encProfList = "utf-8", encProf = "utf-8";

		// （２）设置提取导师列表的Filter
		template.setNameFilter(new HasAttributeFilter("class", "dgrdGlobal"));
		// template.setNameFilter(new NodeClassFilter(LinkTag.class));
		// template.setNameFilter(new AndFilter(new
		// NodeClassFilter(LinkTag.class), new HasAttributeFilter("class",
		// "name")));

		// （３）设置提取导师头像的Filter
		template.setPhotoFilter(new NodeClassFilter(ImageTag.class));
		// template.setPhotoFilter(new HasAttributeFilter("icon", "photo"));

		// （４）设置提取导师简介内容的Filter
		template.setResumeFilter(new HasAttributeFilter("id", "tbl_2"));
		// template.setResumeFilter(new NodeClassFilter(BodyTag.class));

		// （５）设置提取研究方向的Filter
		// template.setFieldFilter(new HasAttributeFilter("class", "menuimg2"));

		profNet.setProfTemplate(template);

		// （６）启动解析导师列表的解析器，输入导师列表网页地址或本地文件名，提取导师列表
		/*
		 * String html =
		 * TinyCrawler.fetchPageAt("C:/Users/dell/Desktop/ProfList.html",
		 * encProfList); String html =
		 * FileIO.readFile("C:/Users/dell/Desktop/ProfList.html", encProfList);
		 * profNet.setProfParser(Parser.createParser(html, encProfList));
		 */

		int[] ids = {7, 8, 9, 10, 11, 12, 16, 20, 21, 22, 23};
		for (int id : ids) {
			profNet.setProfParser("http://it.nankai.edu.cn:8080/itemis/Teachers/Depart.aspx?did=" + id, encProfList);
			profNet.extractProfList(profBulk, true);// true if in table
		}

		// （７）序列化保存导师列表或者解序列对象
		String serialized = root + "/" + profNet.prop.getProperty("dept") + "-序列化列表";
		// FileManager.serialize(profBulk, serialized);
		// FileManager.serializeList("C:/Users/dell/Desktop/ProfList.html",
		// encProfList, serialized);
		profBulk = (HashMap<String, Vector<String>>) SerializationHelper.read(serialized);

		profNet.setImageURLFeature("TeacherImage".toLowerCase());
		// （８）启动解析程序
		profNet.start(profBulk, encProf);

		// （９）解析单个导师页面
		// String profUrl =
		// "http://apec.nankai.edu.cn/noscript/apec/gaikuang/9.htm";
		// String profName = "孟夏";
		// profNet.harvestProf(profName, profUrl, encProf);

		// TODO:</活动模块>========================================================
		TickClock.stopTick();
	}
}