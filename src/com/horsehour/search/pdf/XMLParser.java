package com.horsehour.search.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.NodeList;

import com.horsehour.util.MathUtils;
import com.horsehour.util.DOMUtils;

/**
 * Extract Meta Data in XML Scentific Paper
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @see http://pdftohtml.sourceforge.net/
 * @see http://coolwanglu.github.io/pdf2htmlEX/
 * @see http://www.foolabs.com/xpdf/
 * @see http://aye.comp.nus.edu.sg/parsCit/
 * @see http://www.sciplore.org/
 * @see http://pdfbox.apache.org/
 * @since 26th Oct. 2013
 */
public class XMLParser {
	private Parser parser;
	private final PrototypicalNodeFactory factory;

	public int[] allFont;
	public int maxFontId, minFontId;
	public List<List<LineEntry>> pageList;

	public int width, height;// page width and height

	public XMLParser(String xml) throws IOException {
		pageList = new ArrayList<List<LineEntry>>();
		factory = new PrototypicalNodeFactory();
		parser = Parser.createParser(FileUtils.readFileToString(new File(xml),""), "utf-8");
		parseFont();
	}

	/**
	 * Parse overall font information
	 */
	private void parseFont(){
		NodeFilter filter = new TagNameFilter("fontspec");
		NodeList font = DOMUtils.getNodeList(parser, filter);

		int n = font.size();
		allFont = new int[n];

		Node node = null;
		for (int i = 0; i < n; i++) {
			node = font.elementAt(i);
			allFont[i] = Integer.parseInt(((TagNode) node).getAttribute("size"));
		}

		int[] rank = MathUtils.getRank(allFont, false);
		maxFontId = rank[0];
		minFontId = rank[allFont.length - 1];

		parser.reset();
	}

	/**
	 * Parse Page
	 * @param lineNodeList line list in one page
	 * @return page
	 */
	private List<LineEntry> parsePage(NodeList lineNodeList){
		Node lineNode, childNode;
		NodeList children;

		String content;

		List<LineEntry> lineEntries = new ArrayList<LineEntry>();
		int len = 12;
		int[] features = new int[len];
		for (int i = 0; i < lineNodeList.size(); i++) {
			lineNode = lineNodeList.elementAt(i);
			features[5] = Integer.parseInt(((TagNode) lineNode).getAttribute("width"));// width
			if (features[5] == 0)// vertical layout
				continue;

			content = lineNode.toPlainTextString();
			content = content.replaceAll("\t", " ").trim();
			if (content.isEmpty())
				continue;

			features[3] = Integer.parseInt(((TagNode) lineNode).getAttribute("left"));// left
			features[4] = Integer.parseInt(((TagNode) lineNode).getAttribute("top"));// top
			features[6] = Integer.parseInt(((TagNode) lineNode).getAttribute("height"));// height
			int id = Integer.parseInt(((TagNode) lineNode).getAttribute("font"));// font
			features[7] = id;// font id
			features[8] = allFont[id];// font size
			if (maxFontId == id)
				features[9] = 1;
			else if (minFontId == id)
				features[9] = -1;

			children = lineNode.getChildren();
			if (children == null)
				continue;

			for (int k = 0; k < children.size(); k++) {
				childNode = children.elementAt(k);
				if (childNode instanceof TagNode) {
					String tagName = ((TagNode) childNode).getTagName();
					if (tagName.equalsIgnoreCase("b"))
						features[10] = 1;
					else if (tagName.equalsIgnoreCase("i"))
						features[11] = 1;
				}
			}

			features[0] = i;// line number
			features[1] = width;
			features[2] = height;
			lineEntries.add(new LineEntry(features, content));
		}
		return lineEntries;
	}

	/**
	 * @return first page 
	 */
	public List<LineEntry> parseFirstPage(){
		factory.registerTag(new PageTag());
		parser.setNodeFactory(factory);

		NodeFilter filter = new NodeClassFilter(PageTag.class);
		NodeList pageNodeList = DOMUtils.getNodeList(parser, filter);

		Node pageNode = pageNodeList.elementAt(0);
		width = Integer.parseInt(((TagNode) pageNode).getAttribute("width"));
		height = Integer.parseInt(((TagNode) pageNode).getAttribute("height"));
		parser = DOMUtils.createParser(pageNode.toHtml(), "utf-8");

		factory.registerTag(new TxtTag());
		parser.setNodeFactory(factory);

		filter = new NodeClassFilter(TxtTag.class);
		return parsePage(DOMUtils.getNodeList(parser, filter));
	}

	/**
	 * Parse All Pages
	 */
	public void parseAllPages(){
		factory.registerTag(new PageTag());
		parser.setNodeFactory(factory);

		NodeFilter filter = new NodeClassFilter(PageTag.class);
		NodeList pageNodeList = DOMUtils.getNodeList(parser, filter);

		String html = "";

		Node pageNode;
		NodeList nodeList;
		for (int i = 0; i < pageNodeList.size(); i++) {
			pageNode = pageNodeList.elementAt(i);
			if (i == 0) {
				width = Integer.parseInt(((TagNode) pageNode).getAttribute("width"));
				height = Integer.parseInt(((TagNode) pageNode).getAttribute("height"));
			}

			html = pageNode.toHtml();
			parser = DOMUtils.createParser(html, "utf-8");
			factory.registerTag(new TxtTag());
			parser.setNodeFactory(factory);

			filter = new NodeClassFilter(TxtTag.class);
			nodeList = DOMUtils.getNodeList(parser, filter);

			pageList.add(parsePage(nodeList));
		}
	}

	/**
	 * Extend CompositeTag
	 * @author Chunheng Jiang
	 * @version 1.0
	 * @since 27 Oct. 2013
	 */
	private static class PageTag extends CompositeTag {
		private static final long serialVersionUID = 8214295352783337598L;
		private static final String[] mIds = new String[]{"page"};

		@Override
		public String[] getIds(){
			return mIds;
		}

		@Override
		public String[] getEnders(){
			return mIds;
		}
	}

	/**
	 * Extend CompositeTag
	 * @author Chunheng Jiang
	 * @version 1.0
	 * @since 27 Oct. 2013
	 */
	private static class TxtTag extends CompositeTag {
		private static final long serialVersionUID = 1L;
		private static final String[] mIds = new String[]{"text"};

		@Override
		public String[] getIds(){
			return mIds;
		}

		@Override
		public String[] getEnders(){
			return mIds;
		}
	}

	public static void main(String[] args) throws IOException{
		String xml = "resource/comment.xml";
		XMLParser parser = new XMLParser(xml);
		parser.parseFirstPage();
	}
}
