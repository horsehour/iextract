package com.horsehour.search.crawler.prof;

import java.util.Vector;

import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.visitors.NodeVisitor;

/**
 * ProfTemplate定义导师页面模版信息，允许继承 主要成员为NodeFilter、NodeVisitor
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20120813
 */
public class ProfTemplate {
	private NodeFilter nameFilter;// 姓名过滤器
	private NodeFilter genderFilter;// 性别过滤器
	private NodeFilter birthFilter;// 出生日期过滤器

	private NodeFilter univFilter;// 院校过滤器
	private NodeFilter emailFilter;// 院校过滤器
	private NodeFilter phoneFilter;// 电话过滤器

	private NodeFilter titleFilter;// 职称过滤器
	private NodeFilter levelFilter;// 级别过滤器
	private NodeFilter degreeFilter;// 学位过滤器
	private NodeFilter expertFilter;// 专家过滤器

	private NodeFilter subjectFilter;// 学科过滤器
	private NodeFilter fieldFilter;// 研究方向过滤器

	private NodeFilter resumeFilter;// 简历过滤器

	private NodeFilter photoFilter;// 头像过滤器

	private Vector<NodeFilter> nodeFilters;// 过滤器族

	// 比如多项信息混杂，使用它比较灵活
	private NodeFilter compositeFilter;// 混杂信息的过滤器
	private NodeFilter plainFilter;// 普通的过滤器，没有具体用途

	public ProfTemplate() {
		nameFilter = null;
		genderFilter = null;
		birthFilter = null;
		univFilter = null;
		emailFilter = null;
		phoneFilter = null;
		titleFilter = null;
		levelFilter = null;
		degreeFilter = null;
		expertFilter = null;
		subjectFilter = null;
		fieldFilter = null;
		resumeFilter = null;
		photoFilter = null;
		compositeFilter = null;
		plainFilter = null;

		nodeFilters = null;
	}

	public NodeFilter getNameFilter() {
		return nameFilter;
	}

	public void setNameFilter(NodeFilter nameFilter) {
		this.nameFilter = nameFilter;
	}

	public NodeFilter getGenderFilter() {
		return genderFilter;
	}

	public void setGenderFilter(NodeFilter genderFilter) {
		this.genderFilter = genderFilter;
	}

	public NodeFilter getBirthFilter() {
		return birthFilter;
	}

	public void setBirthFilter(NodeFilter birthFilter) {
		this.birthFilter = birthFilter;
	}

	public NodeFilter getUnivFilter() {
		return univFilter;
	}

	public void setUnivFilter(NodeFilter univFilter) {
		this.univFilter = univFilter;
	}

	public NodeFilter getEmailFilter() {
		return emailFilter;
	}

	public void setEmailFilter(NodeFilter emailFilter) {
		this.emailFilter = emailFilter;
	}

	public NodeFilter getPhoneFilter() {
		return phoneFilter;
	}

	public void setPhoneFilter(NodeFilter phoneFilter) {
		this.phoneFilter = phoneFilter;
	}

	public NodeFilter getTitleFilter() {
		return titleFilter;
	}

	public void setTitleFilter(NodeFilter titleFilter) {
		this.titleFilter = titleFilter;
	}

	public NodeFilter getLevelFilter() {
		return levelFilter;
	}

	public void setLevelFilter(NodeFilter levelFilter) {
		this.levelFilter = levelFilter;
	}

	public NodeFilter getDegreeFilter() {
		return degreeFilter;
	}

	public void setDegreeFilter(NodeFilter degreeFilter) {
		this.degreeFilter = degreeFilter;
	}

	public NodeFilter getExpertFilter() {
		return expertFilter;
	}

	public void setExpertFilter(NodeFilter expertFilter) {
		this.expertFilter = expertFilter;
	}

	public NodeFilter getSubjectFilter() {
		return subjectFilter;
	}

	public void setSubjectFilter(NodeFilter subjectFilter) {
		this.subjectFilter = subjectFilter;
	}

	public NodeFilter getFieldFilter() {
		return fieldFilter;
	}

	public void setFieldFilter(NodeFilter fieldFilter) {
		this.fieldFilter = fieldFilter;
	}

	public NodeFilter getResumeFilter() {
		return resumeFilter;
	}

	public void setResumeFilter(NodeFilter resumeFilter) {
		this.resumeFilter = resumeFilter;
	}

	public NodeFilter getPhotoFilter() {
		return photoFilter;
	}

	public void setPhotoFilter(NodeFilter photoFilter) {
		this.photoFilter = photoFilter;
	}

	public NodeFilter getCompositeFilter() {
		return compositeFilter;
	}

	public void setCompositeFilter(NodeFilter compositeFilter) {
		this.compositeFilter = compositeFilter;
	}

	public void setNodeFilters(Vector<NodeFilter> nodeFilterArray) {
		this.nodeFilters = nodeFilterArray;
	}

	public Vector<NodeFilter> getNodeFilters() {
		return nodeFilters;
	}

	public NodeFilter getPlainFilter() {
		return plainFilter;
	}

	public void setPlainFilter(NodeFilter plainFilter) {
		this.plainFilter = plainFilter;
	}

	/**
	 * 设置主要用于处理resume的visit模式为符合标准: 无论是strong还是b统一换成b br换成br/
	 * 
	 * @return NodeVisitor
	 */
	public static NodeVisitor getTheVisitor() {
		NodeVisitor visitor = new NodeVisitor(true, true) {
			String ret = "";
			String tagName = "";
			int flag = -1;
			boolean isStyle = false, isScript = false;

			// 访问一般结点
			public void visitTag(Tag tag) {
				tagName = tag.getTagName().toLowerCase();
				// 段落标记
				if (tag instanceof ParagraphTag)
					ret += "<p>";
				// headingTag:h1-h6
				else if ((tag instanceof HeadingTag))
					ret += "<b>";
				// 换行标记
				else if (tagName.equalsIgnoreCase("br"))
					ret += "<br/>";
				// 加粗标记
				else if (tagName.equalsIgnoreCase("strong")
				        || tagName.equalsIgnoreCase("b"))
					ret += "<b>";
				// 列表标记
				else if (tagName.equalsIgnoreCase("ol")
				        || tagName.equalsIgnoreCase("ul")
				        || tagName.equalsIgnoreCase("li"))

					ret += "<" + tagName + ">";

				// script和style
				else if (tagName.equalsIgnoreCase("style"))
					isStyle = true;
				else if (tagName.equalsIgnoreCase("script"))
					isScript = true;
				// 链接
				else if (tag instanceof LinkTag) {
					String link = ((LinkTag) tag).getLink();
					// String anchor = ((LinkTag)
					// tag).getLinkText().replaceAll("&nbsp;", "").trim();
					// if(link.endsWith(".pdf")||link.contains(".doc")){
					// ret += "<a href=\"" + link + "\">";
					// if(anchor.isEmpty()){
					// ret += "<b>点击查看</b>";
					// flag = 0;
					// }else
					// flag = 1;
					// }

					if (link != null && !link.isEmpty()) {
						ret += "<a href=\"" + link + "\">";
						flag = 2;// 普通链接
					}

				}
				// 灵活处理
			}

			// 访问文本结点
			public void visitStringNode(Text text) {
				if (!isStyle && !isScript) {
					String str = text.toPlainTextString();
					str = str.replaceAll("\r|\n", "<br/>");
					ret += str;
				}
			}

			// 访问结束结点-导致换行的标签
			public void visitEndTag(Tag tag) {
				tagName = tag.getTagName().toLowerCase();
				// 段落结束标记
				if (tagName.equalsIgnoreCase("p"))
					ret += "</p>";
				else if (tagName.equalsIgnoreCase("tr"))
					ret += "<br/>";
				// 换行结束标记-比如div
				else if (tagName.equalsIgnoreCase("div"))
					ret += "<br/>";
				// 加粗结束标记
				else if (tagName.equalsIgnoreCase("strong")
				        || tagName.equalsIgnoreCase("b"))
					ret += "</b>";
				// headingTag会自动换行
				else if (tagName.startsWith("h") && tagName.length() == 2) {
					String sub = tagName.replaceAll("\\d", "");
					if (sub.length() == 1)
						ret += "</b><br/>";
				}
				// 列表标记
				else if (tagName.equalsIgnoreCase("ol")
				        || tagName.equalsIgnoreCase("ul")
				        || tagName.equalsIgnoreCase("li"))

					ret += "</" + tagName + ">";
				// 链接结束标记
				else if (flag != -1 && tagName.equalsIgnoreCase("a")) {
					ret += "</a>";
					flag = -1;
				} else if (tagName.equalsIgnoreCase("script"))
					isScript = false;
				else if (tagName.equalsIgnoreCase("style"))
					isStyle = false;
				// 灵活处理
			}

			public String toString() {
				return ret;
			}
		};
		return visitor;
	}

	/**
	 * 获得抽取表格的Visitor
	 * 
	 * @return tableVisitor
	 */
	public static NodeVisitor getTableVisitor() {
		NodeVisitor tableVisitor = new NodeVisitor(true, true) {
			String trace = "";

			public void visitTag(Tag tag) {
				if (tag instanceof TableRow) {
					trace += "<tr>";
				} else if (tag instanceof TableColumn) {
					trace += "<td>";
				} else if (tag instanceof ParagraphTag) {
					trace += "<p>";
				}
			}

			public void visitEndTag(Tag tag) {
				String tagName = tag.getTagName();
				if (tagName.equalsIgnoreCase("tr")) {
					trace += "</tr>";
				} else if (tagName.equalsIgnoreCase("td")) {
					trace += "</td>";
				} else if (tagName.equalsIgnoreCase("p")) {
					trace += "</p>";
				}
			}

			public void visitStringNode(Text text) {
				String str = text.toPlainTextString();
				str = str.replaceAll("\r|\n", "").trim();
				trace += str;
			}

			public String toString() {
				return "<table>" + trace + "</table>";
			}
		};
		return tableVisitor;
	}

	public static NodeVisitor getPlainVisitor() {
		NodeVisitor nodeVisitor = new NodeVisitor(true, true) {
			String ret = "";
			String tagName = "";
			boolean isPre = false;

			// 访问一般结点
			public void visitTag(Tag tag) {
				tagName = tag.getTagName().toLowerCase();
				// 段落标记
				if (tag instanceof ParagraphTag)
					ret += "<p>";
				// 换行标记
				else if (tagName.equalsIgnoreCase("br"))
					ret += "<br/>";
				// 加粗标记
				else if (tagName.equalsIgnoreCase("strong")
				        || tagName.equalsIgnoreCase("b"))
					ret += "<b>";
				// 灵活处理
				else if (tagName.equalsIgnoreCase("legend"))
					ret += "<b>";
				else if (tagName.equalsIgnoreCase("pre")) {
					ret += "<p>";
					isPre = true;
				}
			}

			// 访问文本结点
			public void visitStringNode(Text text) {
				String str = text.toPlainTextString();
				if (!isPre)
					str = str.replaceAll("\r|\n", "").trim();
				else {
					str = str.replaceAll("\r", "");
					str = str.replaceAll("\n", "<br/>").trim();
				}
				ret += str;
			}

			// 访问结束结点-导致换行的标签
			public void visitEndTag(Tag tag) {
				tagName = tag.getTagName().toLowerCase();
				// 段落结束标记
				if (tagName.equalsIgnoreCase("p"))
					ret += "</p>";
				else if (tagName.equalsIgnoreCase("tr"))
					ret += "<br/>";
				// 换行结束标记-比如div
				else if (tagName.equalsIgnoreCase("div"))
					ret += "<br/>";
				// 加粗结束标记
				else if (tagName.equalsIgnoreCase("strong")
				        || tagName.equalsIgnoreCase("b"))
					ret += "</b>";
				// 灵活处理
				else if (tagName.equalsIgnoreCase("legend"))
					ret += "</b><br/>";
				else if (tagName.equalsIgnoreCase("pre")) {
					ret += "</p>";
					isPre = false;
				}
			}

			public String toString() {
				return ret;
			}
		};
		return nodeVisitor;
	}

	public static NodeVisitor getTextVisitor() {
		NodeVisitor visitor = new NodeVisitor(true, true) {
			String ret = "";

			// 访问文本结点
			public void visitStringNode(Text text) {
				String str = text.toPlainTextString().trim();
				ret += str;
			}

			public String toString() {
				return ret;
			}
		};
		return visitor;
	}
}