package com.horsehour.search.crawler;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import com.horsehour.util.DOMUtils;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年5月3日 下午11:02:12
 **/
public class LinkExtractor implements Runnable {
	private String url;
	private LinkManager linkManager;

	public LinkExtractor(String url, LinkManager manager) {
		this.url = url;
		this.linkManager = manager;
	}

	@Override
	public void run() {
		getSimpleLinks(url);
	}

	private void getSimpleLinks(String url) {
		if (linkManager.visited(url))// have been visited
			return;

		try {
			Parser parser = DOMUtils.getParser(url, "utf-8");
			NodeList list = DOMUtils.getNodeList(parser,
			        new NodeClassFilter(LinkTag.class));

			List<String> urls = new ArrayList<String>();

			for (int i = 0; i < list.size(); i++) {
				LinkTag extracted = (LinkTag) list.elementAt(i);

				String link = extracted.getLink();
				if (link.isEmpty() || linkManager.visited(link))
					continue;

				urls.add(link);
			}

			linkManager.addVisited(url);
			for (String l : urls)
				linkManager.queueLink(l);

			System.out.println(Thread.currentThread().getId() + ":"
			        + linkManager.size());
			// download the corresponding pages...
		} catch (Exception e) {
			e.getStackTrace();
			return;
		}
	}
}