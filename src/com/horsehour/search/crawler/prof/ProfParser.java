package com.horsehour.search.crawler.prof;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import com.horsehour.util.DOMUtils;

/**
 * ProfParser主要负责提取、解析导师数据
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20120813
 */
public class ProfParser {
	private Parser parser;

	public ProfParser(Parser iParser) {
		parser = iParser;
	}

	public ProfParser(String url, String enc) {
		parser = DOMUtils.getParser(url, enc);
	}

	public Parser getParser() {
		return parser;
	}

	public void reset() {
		parser.reset();
	}

	/**
	 * 根据NodeFilter获取对应NodeList
	 * 
	 * @param nodeFilter
	 * @return NodeList
	 */
	public NodeList getNodeList(NodeFilter nodeFilter) {
		NodeList nodeList = null;
		try {
			nodeList = parser.extractAllNodesThatMatch(nodeFilter);
		} catch (ParserException e) {
			e.printStackTrace();
			return null;
		}
		return nodeList;
	}

	/**
	 * 通过修改ProfTemplate的访问模式抽取visitor对nodeList的访问轨迹
	 * 
	 * @param nodeVisitor
	 * @param nodeList
	 * @return visitor's trace
	 */
	public String getVisitorTrace(NodeVisitor nodeVisitor, NodeList nodeList) {
		if (nodeList == null)
			return null;
		else
			try {
				nodeList.visitAllNodesWith(nodeVisitor);
			} catch (ParserException e) {
				e.printStackTrace();
				return null;
			}
		return nodeVisitor.toString();
	}

	/**
	 * 根据NodeFilter以及visitor的访问模式提取其访问轨迹
	 * 
	 * @param nodeVisitor
	 * @param filter
	 * @return visitor's trace
	 */
	public String getVisitorTrace(NodeVisitor nodeVisitor, NodeFilter filter) {
		// 首先提取契合filter的结点列表
		NodeList nodes = getNodeList(filter);
		return getVisitorTrace(nodeVisitor, nodes);
	}

	/**
	 * TODO:利用关键词从纯文本提取关键信息局限颇多，考虑借调百度等搜索引擎搜索结果 从纯文本中利用关键词解析学位
	 * 
	 * @param content
	 * @return degree
	 */
	public static String parseDegree(String content) {
		String degree = "";
		// 先把干扰项剔除
		content = content.replaceAll("(博|硕){1}士(研究)?生导师", "");
		content = content.replaceAll(" +|　+", "").toLowerCase();
		if (content.contains("博士") || content.contains("phd"))
			degree = "博士";
		else if (content.contains("硕士"))
			degree = "硕士";

		return degree;
	}

	/**
	 * 解析导师级别-硕导/博导
	 * 
	 * @param content
	 * @return profLevel
	 */
	public static String parseLevel(String content) {
		String level = "";
		if (content.contains("博导") || content.contains("博士生导师")
		        || content.contains("博士研究生导师"))
			level = "博导";
		else if (content.contains("硕导") || content.contains("硕士生导师")
		        || content.contains("硕士研究生导师"))
			level = "硕导";

		return level;
	}

	/**
	 * 解析职称
	 * 
	 * @param content
	 * @return profTitle
	 */
	public static String parseTitle(String content) {
		String title = "";
		int profNum = 0, viceProfNum = 0;
		Matcher matcher = Pattern.compile("教授").matcher(content);
		while (matcher.find())
			profNum++;
		matcher = Pattern.compile("副教授").matcher(content);
		while (matcher.find())
			viceProfNum++;

		int len = content.length();
		if (profNum == 0) {
			if (!content.contains("研究员") && content.contains("讲师"))
				title = "讲师";
			// 研究员、副研究员、助理研究员、工程师、高级工程师、高级实验员-主要是针对工科
			else {// 直接从前200个字符中找相关关键词
				len = (200 > len) ? len : 200;
				content = content.substring(0, len);
				if (content.contains("副研究员"))
					title = "副研究员";
				else if (content.contains("助理研究员") || content.contains("助研"))
					title = "助理研究员";
				else if (content.contains("研究员"))
					title = "研究员";
				else if (content.contains("高级工程师"))
					title = "高级工程师";
				else if (content.contains("高级实验员"))
					title = "高级实验员";
				else if (content.contains("工程师"))
					title = "工程师";
			}
		} else if (viceProfNum == profNum) {
			title = "副教授";
		} else
			// 只要教授的数目比副教授的多
			title = "教授";
		return title;
	}

	/**
	 * 解析专家类型
	 * 
	 * @param content
	 * @return expert type
	 */
	public static String parseExpert(String content) {
		String expert = "";

		if (content.contains("院士"))
			expert += " 院士";
		if (content.contains("长江学者"))
			expert += " 长江学者";
		if (content.contains("百人计划"))
			expert += " 百人计划入选者";
		if (content.contains("千人计划"))
			expert += " 千人计划入选者";
		if (content.contains("国家杰出青年基金"))
			expert += " 杰青";

		return expert.trim();
	}

	/**
	 * 解析性别
	 * 
	 * @param content
	 * @return profGender
	 */
	public static String parseGender(String content) {
		String gender = "";
		if (content.contains("男，") || content.contains("男,")
		        || content.contains("性别：男"))
			gender = "男";
		else if (content.contains("女，") || content.contains("女,")
		        || content.contains("性别：女"))
			gender = "女";
		return gender;
	}

	/**
	 * 解析出生日期
	 * 
	 * @param content
	 * @return prof's birthday
	 */
	public static String parseBirth(String content) {
		String birth = "";
		String regex = "19(\\d){2}(年|.)((\\d){1,2}(月|.)?+)?+((\\d){1,2}(日)?+)?+((出)?+生(于)?+)|"
		        + "生于19(\\d){2}(年|.)((\\d){1,2}(月|.)?+)?+((\\d){1,2}(日)?+)?+";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			birth = matcher.group().trim();
			birth = birth.replaceAll("[出|生|于]", "");
		}
		return birth;
	}

	/**
	 * 解析电话-仅限于固话
	 * 
	 * @param content
	 * @return prof's phone
	 */
	public static String parsePhone(String content) {
		String phone = "";
		// String regex = "\\d{3}-[5678]\\d{7}|1[358]\\d{9}|[5678]\\d{7}";
		String regex = "\\d{3}-\\d{8}|1[358]\\d{9}|[5678]\\d{7}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
			phone = matcher.group().trim();
		return phone;
	}

	/**
	 * 解析邮箱
	 * 
	 * @param content
	 * @return prof's emalil
	 */
	public static String parseEmail(String content) {
		String email = "";
		// String regex =
		// "\\w+\\.?\\w+@(\\w+\\.)+(((edu|com)\\.cn)|com|cn|org|net)";
		String regex = "\\w+(\\.|-)?\\w+@(\\w+\\.)+(((edu|com)\\.cn)|com|cn|org|net)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
			email = matcher.group().trim();
		return email;
	}

	/**
	 * 解析所属院系
	 * 
	 * @param content
	 * @return prof's department
	 */
	public static String parseDept(String content) {
		String dept = "";
		String regex = "单位：[\\s\\S]+?<br>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			dept = matcher.group();
			dept = dept.replaceAll("单位：|<br>", "").trim();
		}
		return dept;
	}

	/**
	 * 解析研究领域
	 * 
	 * @param content
	 * @return prof's research field
	 */
	public static String parseField(String content) {
		String field = "";
		String regex = "研究[方向|领域|兴趣]{1}?[\\s\\S]*?$";
		field = parseRegex(content, regex);
		int idx = field.indexOf("。");
		if (idx != -1) {
			field = field.substring(0, idx);
		}

		field = field.replaceAll("&nbsp;| +|　+|:|：", " ");
		field = field.replaceAll("<[^>]*>", "　");
		field = field.replaceAll(" 　", "　");
		field = field.replaceAll("　+", "　");// 中文空格

		field = field.replaceAll("研究方向|研究领域|研究兴趣", "");

		String[] parts = field.split("　");
		int len = parts.length;
		len = (len > 4) ? 4 : len;// 最多选择四个方向
		String part = "";
		field = "";
		for (int id = 0; id < len; id++) {
			part = parts[id];
			if (part.contains("课程") || part.contains("项目")
			        || part.contains("著作") || part.contains("《")
			        || part.contains("课题") || part.contains("讲授")
			        || part.contains("科研成果") || part.contains("获奖")
			        || part.contains("荣誉") || part.contains("任职")
			        || part.contains("论文") || part.contains("联系方式"))

				break;
			else
				field += "; " + part.trim();
		}
		while (field.startsWith("; "))
			field = field.substring(2);

		String temp = field.replaceAll(";", "").trim();
		if (temp.isEmpty()) {
			regex = "从事[\\s\\S]*?(研究|工作|。){1}";
			field = parseRegex(content, regex);
		}
		return field.trim();
	}

	/**
	 * 从文本中剔除噪声-连续的空格,&nbsp;等
	 * 
	 * @param content
	 * @return clean content
	 */
	public static String removeNoiseIn(String content) {
		content = content.replaceAll(" |　|\r|\n", "");// 中英文空格
		content = content.replaceAll("&nbsp;", "");
		return content;
	}

	public static String removeTagIn(String content) {
		return content.replaceAll("<[^>]*>", "").trim();
	}

	/**
	 * 根据提供的正则表达式语句确定匹配字段
	 * 
	 * @param src
	 * @param regex
	 * @return matched content
	 */
	public static String parseRegex(String src, String regex) {
		String ret = "";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(src);
		if (matcher.find())
			ret = matcher.group().trim();
		return ret;
	}

	/**
	 * 提取页面中的全部链接
	 * 
	 * @param list
	 */
	public void extractAllLinks(HashMap<String, String> list) {
		NodeList nodeList = getNodeList(new NodeClassFilter(LinkTag.class));
		Node node = null;
		int sz = nodeList.size();
		String link = "", anchor = "";
		for (int i = 0; i < sz; i++) {
			node = nodeList.elementAt(i);
			if (node instanceof LinkTag) {
				link = ((LinkTag) node).getLink();
				anchor = ((LinkTag) node).getLinkText().trim();
				anchor = anchor.replaceAll(" ", "");
				if (!link.isEmpty() && !anchor.isEmpty())
					list.put(anchor, link);
			}
		}
	}

	/**
	 * 将表格转化为键值对-一行多列，奇数列为key，偶数列为value
	 * 
	 * @param ret
	 */
	public void parseTableContent(HashMap<String, String> ret) {
		NodeList nodeList = getNodeList(new NodeClassFilter(TableTag.class));
		Node node = null;
		int sz = nodeList.size();
		if (sz > 0)
			node = nodeList.elementAt(0);

		TableRow[] rows = ((TableTag) node).getRows();

		int count = 0;
		int rowCount = rows.length;
		for (int rowIdx = 0; rowIdx < rowCount - 1;) {
			TableRow row = rows[rowIdx];
			TableColumn[] cols = row.getColumns();
			String key = "";
			for (TableColumn col : cols) {
				String value = "";
				count++;
				if (count % 2 == 1) {
					key = removeNoiseIn(col.toPlainTextString());
				} else {
					ProfParser profParser = new ProfParser(Parser.createParser(
					        col.toHtml(), parser.getEncoding()));
					value = profParser.getVisitorTrace(ProfTemplate
					        .getTheVisitor(), new TagNameFilter("td"));
				}
				if (!key.isEmpty() && !value.isEmpty()) {
					if (ret.containsKey(key)) {
						value += ret.get(key);
						ret.remove(key);
					}
					ret.put(key, value);
				}
			}
		}
	}

	/**
	 * 主要用于解析script中编码后的邮箱地址
	 * 
	 * @param src
	 * @return email hidden in script
	 */
	public static String decodeEmail(String src) {
		src = src.replaceAll("addy\\d{1,}+=| +|\\+|'", "").trim();
		String regex = "&#\\d{2,3}+;";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(src);

		String encoded = "", cleanEncoded = "";
		int code = 0;
		while (matcher.find()) {
			encoded = matcher.group();
			cleanEncoded = encoded.replaceAll("&#|;", "");
			code = Integer.parseInt(cleanEncoded);
			String newchar = new Character((char) code).toString();
			src = src.replace(encoded, newchar);
		}
		src = src.replace(";", "").trim();
		return src;
	}

	/**
	 * 获取转码后的邮箱地址字符串
	 * 
	 * @return email
	 */
	public String harvestDecodedEmail() {
		String ret = "";
		NodeList nodeList = getNodeList(new NodeClassFilter(ScriptTag.class));
		int sz = nodeList.size();
		String decoded = "";
		String encodedEmail = "";
		if (sz > 0) {
			encodedEmail = nodeList.elementAt(sz - 1).toPlainTextString();
			int idx_start = encodedEmail.indexOf("addy");
			int idx_end = -1;
			if (idx_start != -1) {
				idx_end = encodedEmail.indexOf("\n", idx_start);
				if (idx_end != -1) {
					encodedEmail = encodedEmail.substring(idx_start, idx_end);
					decoded = decodeEmail(encodedEmail);
					ret = decoded;
				}
			}
		}
		parser.reset();
		return ret;
	}
}