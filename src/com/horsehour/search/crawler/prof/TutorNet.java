package com.horsehour.search.crawler.prof;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.horsehour.search.crawler.ZhiZhu;
import com.horsehour.util.TickClock;

/**
 * <p>
 * 抽取教育在线的导师信息 1.教育在线http://cksp.eol.cn/tutor_detail.php?id=# 11<=#<=31490
 * 2.我去考研http://www.57kaoyan.net/daoshi/*.html *<13141
 * 3.考研网http://www.kaoyan.com/s/daoshi/index_2.html 总共2万多
 * 4.54分享者http://www.54share.com/category/262
 * 5.教师库http://teacher.cucdc.com/laoshi/school/10001.html 声称150万
 * 6.科研人搜索http://prof.ncic.ac.cn/person/$ 1<=$<=161353
 * 7.中国高校之窗http://www.gx211.com/news/szll/index_*.html *表示页数 129页，没有20个，近2580个
 * 中国研究生网导师库：http://cgkn.chinajournal.net.cn/china_graduate/tutor.asp
 */
public class TutorNet {
	private static String rootUrl = "http://cksp.eol.cn/tutor_detail.php?id=";
	private static String rootDest = "/EOL_Tutors/";
	private static int tutorMax = 31490;
	private static long skipLen = 14500;// 读取时跳过的字节数
	private static int tutorId;
	private static String tutorName;
	private static String univName;
	private static String imageUrl;
	private static StringBuffer sb = new StringBuffer();

	public static String getTutorPageAt(int id, String charset){
		if (id < 12 || id > tutorMax)
			System.exit(-1);
		return ZhiZhu.skipGET(rootUrl + id, charset, skipLen);
	}

	public static String getTutorPageAt(int id){
		return getTutorPageAt(id, "UTF-8");
	}

	public static void setSkipLen(long len){
		skipLen = len;
	}

	public static void extractTutor(int id) throws ParserException, IOException{
		tutorId = id;
		tutorName = "";
		univName = "";
		imageUrl = "";
		extractTutor(rootUrl + id);
		extractFaceImage();
	}

	/**
	 * 提取导师关键信息
	 * 
	 * @param url
	 */
	public static void extractTutor(String url){
		URL pageUrl = null;
		HttpURLConnection huc = null;
		try {
			pageUrl = new URL(url);
			huc = (HttpURLConnection) pageUrl.openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// 不限连接时间和读取时间
		huc.setConnectTimeout(0);
		huc.setReadTimeout(0);

		Parser parser = null;
		try {
			parser = new Parser(huc);
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		}

		NodeFilter filter = null;
		NodeList lists = null;

		extractTutorTitle(parser, filter, lists);
		if (tutorName.isEmpty())
			return;

		extractTutorFace(parser, filter, lists);
		extractTutorProfile(parser, filter, lists);
		extractTutorContact(parser, filter, lists);

		// 介绍信息
		filter = new HasAttributeFilter("id", "more_intro");
		sb.append("个人简介：" + "\r\n");
		extractBlockInfo(parser, filter, lists);
		// 奖励情况
		filter = new HasAttributeFilter("id", "more_award");
		sb.append("获得奖项：" + "\r\n");
		extractBlockInfo(parser, filter, lists);
		// 论文信息
		filter = new HasAttributeFilter("id", "more_thesis");
		sb.append("著作及论文：" + "\r\n");
		extractBlockInfo(parser, filter, lists);
		// 主持项目情况
		filter = new HasAttributeFilter("id", "more_project");
		sb.append("承担项目：" + "\r\n");
		extractBlockInfo(parser, filter, lists);

		sb.append("===================================\r\n");
	}

	/**
	 * 主要提取【姓名】、【导师类别】、【人气指数】
	 * 
	 * @param parser
	 * @param filter
	 * @param lists
	 */
	public static void extractTutorTitle(Parser parser, NodeFilter filter, NodeList lists){
		filter = new HasAttributeFilter("width", "76%");

		try {
			lists = parser.parse(filter);
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		}

		for (int ix = 0; ix < lists.size(); ix++) {
			Node node = lists.elementAt(ix);
			String txtContent = node.toPlainTextString();
			String[] entries = txtContent.split(" ");
			for (int i = 0; i < entries.length; i++) {
				String entry = entries[i].trim();
				if (entry.length() > 5) {
					if (i == 1) {
						String[] name = entry.split("：");
						// 判断页面是否跳转到其他页面
						if (!name[0].trim().equalsIgnoreCase("导师姓名"))
							break;
						tutorName = name[1].trim();
					}
					sb.append(entry + "\r\n");
				}
			}
		}
		parser.reset();
	}

	/**
	 * 获取头像图片
	 * 
	 * @param parser
	 * @param filter
	 * @param lists
	 */
	public static void extractTutorFace(Parser parser, NodeFilter filter, NodeList lists){
		filter = new HasAttributeFilter("class", "img_01");
		try {
			lists = parser.parse(filter);
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		}
		String imgUrl = "";
		if (lists.size() > 0) {
			imgUrl = lists.elementAt(0).getText();

			int headIdx = imgUrl.indexOf("\"");
			int tailIdx = imgUrl.indexOf("\"", headIdx + 1);

			imgUrl = imgUrl.substring(headIdx + 1, tailIdx);

			// 如果正常显示图片，则格式如http://p.img.eol.cn/images/1014/2010/0115/1263539586_face_r5pn.jpg
			// 否则，images/pic100x130.jpg;
			if (imgUrl.contains("_face_"))
				imageUrl = imgUrl;
		}
		parser.reset();
	}

	/**
	 * 提取导师的基本概况，还应提取出【招生专业】的代码
	 * 
	 * @param parser
	 * @param filter
	 * @param lists
	 */
	public static void extractTutorProfile(Parser parser, NodeFilter filter, NodeList lists){

		filter = new HasAttributeFilter("class", "tab_02");
		try {
			lists = parser.parse(filter);
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		}
		if (lists.size() > 0) {
			Node node = lists.elementAt(0);

			// 取得所有的行TableRow[]
			TableRow[] rows = ((TableTag) node).getRows();

			int flag = 0;// 标记行号，从第三行中找到招生代码
			// 根据行取得列及数据，使用getColumnCount()可以取得列数
			for (TableRow row : rows) {
				// 取得各列header和data<th>、<td>
				TableHeader[] headers = row.getHeaders();
				TableColumn[] columns = row.getColumns();
				flag++;

				int count = headers.length;
				String headerName = "";
				String data = "";
				for (int ix = 0; ix < count; ix++) {
					headerName = headers[ix].toPlainTextString().trim();
					data = columns[ix].toPlainTextString().trim();
					sb.append(headerName + "：" + data + "\r\n");
				}
				if (flag == 2) {
					TableColumn univ = columns[0];
					univName = univ.toPlainTextString().trim();
				}

				if (flag == 3) {
					TableColumn col = columns[count - 1];
					// 包含【招生代码】的链接
					Node child = col.getChild(3);
					if (child instanceof LinkTag) {
						String link = ((LinkTag) child).getLink();
						String code = extractCode(link);
						sb.append("专业代码：" + code + "\r\n");
					}
				}
			}
		}

		parser.reset();
	}

	/**
	 * 提取导师的联系方式
	 * 
	 * @param parser
	 * @param filter
	 * @param lists
	 */
	public static void extractTutorContact(Parser parser, NodeFilter filter, NodeList lists){
		filter = new HasAttributeFilter("class", "tab_01");

		try {
			lists = parser.parse(filter);
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		}

		if (lists.size() > 0) {
			Node node = lists.elementAt(0);

			// 取得所有的行TableRow[]
			TableRow[] rows = ((TableTag) node).getRows();

			// 根据行取得列及数据，使用getColumnCount()可以取得列数
			for (TableRow row : rows) {
				// 取得各列data<td>
				TableColumn[] columns = row.getColumns();

				int count = columns.length;
				String key = "";
				String value = "";

				for (int ix = 0; ix < count / 2; ix++) {
					key = columns[2 * ix].toPlainTextString().trim();
					value = columns[2 * ix + 1].toPlainTextString().trim();
					sb.append(key + "：" + value + "\r\n");
				}
			}
		}
		parser.reset();
	}

	/**
	 * 提取块信息:介绍、获奖情况、论文信息、主持项目
	 * 
	 * @param parser
	 * @param filter
	 * @param lists
	 */
	public static void extractBlockInfo(Parser parser, NodeFilter filter, NodeList lists){
		try {
			lists = parser.parse(filter);
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		}
		if (lists.size() > 0) {
			lists = lists.elementAt(0).getChildren();
			SimpleNodeIterator iter = lists.elements();
			while (iter.hasMoreNodes()) {
				Node node = iter.nextNode();
				String txt = node.toPlainTextString().trim();
				if (!txt.isEmpty())
					sb.append(txt + "\r\n");
			}
		}
		parser.reset();
	}

	/**
	 * 解析链接中的招生代码
	 * 
	 * @param url
	 * @return 招生代码
	 */
	public static String extractCode(String url){
		int head = url.indexOf("proname=");
		int tail = url.indexOf("%20");
		return url.substring(head + 8, tail);
	}

	/**
	 * 下载导师的头像
	 * @throws IOException 
	 */
	public static void extractFaceImage() throws IOException{
		String dest = rootDest + "FaceImages2/";
		String fileName = univName + "-" + tutorId + "-" + tutorName;
		if (imageUrl.isEmpty())
			FileUtils.write(new File(rootDest + "Record2.txt"), "ImageLost:" + tutorId + "\r\n","");
		else
			ZhiZhu.getImage(imageUrl, dest + fileName + ".png");
	}

	/**
	 * 启动抽取程序
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	public static void run() throws ParserException, IOException{
		int id = 29638;
		for (; id < tutorMax; id++) {
			extractTutor(id);
			if (tutorName.isEmpty()) {
				FileUtils.write(new File(rootDest + "Record2.txt"), "NameLost:" + id + "\r\n","");
				continue;
			}

			System.out.println(id + univName + tutorName);
			// 每5个页面缓冲一次
			if ((id - 13) % 5 == 4) {
				FileUtils.write(new File(rootDest + "tutor3.txt"), sb.toString(),"");
				sb = new StringBuffer();
			}
		}
	}

	public static void main(String[] args) throws ParserException, IOException{
		TickClock.beginTick();

		// TutorNet.extractTutor("http://cksp.eol.cn/tutor_detail.php?id=15");
		// TutorNet.run();

		Parser parser = new Parser("http://cksp.eol.cn/expert.php?id=56&url=daoshi");
		NodeFilter filter = new HasAttributeFilter("id", "more_thesis");
		NodeList lists = null;
		TutorNet.extractBlockInfo(parser, filter, lists);

		TickClock.stopTick();
	}
}