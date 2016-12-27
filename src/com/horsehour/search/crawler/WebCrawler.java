package com.horsehour.search.crawler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2014年5月3日 下午10:56:45
 **/
public class WebCrawler implements LinkManager {
	private final Collection<String> visitedLinks;
	private final String url;
	private final ExecutorService execService;

	public WebCrawler(String seedURL, int maxThreads) {
		url = seedURL;
		execService = Executors.newFixedThreadPool(maxThreads);
		visitedLinks = Collections.synchronizedSet(new HashSet<String>());
	}

	@Override
	public void queueLink(String link){
		startNewThread(link);
	}

	@Override
	public int size(){
		return visitedLinks.size();
	}

	@Override
	public void addVisited(String url){
		visitedLinks.add(url);
	}

	@Override
	public boolean visited(String url){
		return visitedLinks.contains(url);
	}

	private void startNewThread(String link){
		execService.execute(new LinkExtractor(link, this));
	}

	public void startCrawling(){
		startNewThread(url);
	}

	public static void main(String[] args){
		WebCrawler crawler = new WebCrawler(
		        "http://www.autohome.com.cn/news/201512/882425.html#pvareaid=103015", 1);
		crawler.startCrawling();
	}
}
