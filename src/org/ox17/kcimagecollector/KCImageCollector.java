package org.ox17.kcimagecollector;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KCImageCollector {
	
	private final static String URL_BASE = "http://krautchan.net";
	private Map<String, List<String>> threadLinkToImgLinks = new HashMap<String, List<String>>();
	private final static boolean USE_CACHING = true;
	
	public List<String> collectPageLinks(String rootUrl, String boardName) throws Exception {
		Helpers.log("Collecting page links...");
		
		String rootPageSrc = Helpers.loadUrlIntoStr(rootUrl);
		List<String> pageLinks = new LinkedList<String>();
		
		pageLinks.add(rootUrl);	
		
		Pattern p = Pattern.compile("<a href=\"(/"+boardName+"/[0-9]+.html)\">");
		Matcher m = p.matcher(rootPageSrc);
		
		while(m.find()) {
			if(m.groupCount() == 1) {
				String pageLink = URL_BASE+m.group(1);
				if(!pageLinks.contains(pageLink)) {
					pageLinks.add(pageLink);
				}
			}
		}
		
		Helpers.log("Found " + pageLinks.size() + " pages on /" + boardName + "/");
		
		return pageLinks;
	}
	
	public List<String> collectThreadLinks(String rootUrl, String boardName) throws Exception {
		List<String> pageLinks = collectPageLinks(rootUrl, boardName);
		List<String> threadLinks = new LinkedList<String>();
		
		for(String pageLink : pageLinks) {
			threadLinks.addAll(collectThreadLinksForPage(pageLink, boardName));
		}	
						
		return threadLinks;
	}
	
	public List<String> collectThreadLinksForPage(String pageLink, String boardName) throws Exception {
		String pageSrc = Helpers.loadUrlIntoStr(pageLink);
		Pattern p = Pattern.compile("<a href=\"(/"+boardName+"/thread-[0-9]+.html)\" class=\"quotelink\">");
		Matcher m = p.matcher(pageSrc);
		
		List<String> threadLinks = new LinkedList<String>();
		
		while(m.find()) {
			if(m.groupCount() == 1) {
				threadLinks.add(URL_BASE+m.group(1));
			}
		}
		
		Helpers.log("Found " + threadLinks.size() + " threads on page " + pageLink);
		
		return threadLinks;
	}
	
	public List<String> collectImgLinksForThread(String threadLink) throws Exception {		
		if(USE_CACHING && threadLinkToImgLinks.containsKey(threadLink)) {
			return threadLinkToImgLinks.get(threadLink);
		} else {
			List<String> imgLinks = new LinkedList<String>();
			String threadSrc;			
			try {
			threadSrc = Helpers.loadUrlIntoStr(threadLink);
			} catch(Exception e) { return imgLinks; }
			String regex = "<a href=\"(/files/[0-9]+.(jpg|png))\" target=\"_blank\">";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(threadSrc);
			
			while(m.find()) {
				if(m.groupCount() == 2) {
					String imgLink = URL_BASE+m.group(1);				
					imgLinks.add(imgLink);
				}
			}
			if(USE_CACHING)
				threadLinkToImgLinks.put(threadLink, imgLinks);
			return imgLinks;
		}
	}
	
	public static void buildHtmlFileOutOfImgLinks(List<String> imgLinks, String outFilename) throws IOException {
		StringBuilder outHtml = new StringBuilder();
		outHtml.append("<html><head><title>Krautchan Dump</title></head><body>");
		outHtml.append("<table border=\"1\">");
		outHtml.append("<tr><td>");
		int ctr = 0;
		for(String imgLink : imgLinks) {
			outHtml.append("<a href=\""+imgLink+"\" target=\"_blank\"><img src=\""+thumbnailLinkFromImgLink(imgLink)+"\" width=200 height=200 /></a>");
			if(ctr % 4 == 0)
				outHtml.append("</td></tr><tr><td>");
			ctr++;
		}
		outHtml.append("</td></tr>");
		outHtml.append("</table></html>");
		Helpers.writeStrToFile(outHtml.toString(), outFilename);
	}
	
	public List<String> collectImgLinksForBoards(String[] boardNames) throws Exception {
		List<String> imgLinks = new LinkedList<String>();		
		int i=1;
		int numBoards = boardNames.length;
		for(String board : boardNames) {
			Helpers.log("Collect for /" + board + "/... ("+(i++)+"/"+numBoards+")");
			imgLinks.addAll(collectImgLinksForBoard(board));
		}
		
		return imgLinks;
	}
	
	public List<String> collectImgLinksForBoard(String boardName) throws Exception {
		List<String> imgLinks = new LinkedList<String>();
		List<String> tls = collectThreadLinks(URL_BASE+"/"+boardName+"/", boardName);
		int j=1;
		int numThreads = tls.size();
		for(String tl : tls) {
			Helpers.log("Collecting image links for thread " + tl + " ... ("+(j++)+"/"+numThreads+")");			
			imgLinks.addAll(collectImgLinksForThread(tl));
		}
		return imgLinks;
	}
	
	public static Thread collectImgLinksForBoardConcurrent(String boardName, LinkFoundCallback callback) throws Exception {
		Thread t = new Thread(new CollectRunner(boardName, callback));
		t.start();
		return t;		
	}
	
	private static class CollectRunner implements Runnable {		
		private String boardName;
		private LinkFoundCallback callback;

		public CollectRunner(String boardName, LinkFoundCallback callback) {
			this.boardName = boardName;
			this.callback = callback;	
		}

		@Override
		public void run() {
			KCImageCollector kic = new KCImageCollector();
			try {
				List<String> tls = kic.collectThreadLinks(URL_BASE+"/"+boardName+"/", boardName);
				for(String tl : tls) {			
					collectImgLinksForThreadConcurrent(tl);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void collectImgLinksForThreadConcurrent(String threadLink) {
			String threadSrc;
			try {
			threadSrc = Helpers.loadUrlIntoStr(threadLink);
			} catch(Exception e) { e.printStackTrace(); return; }
			String regex = "<a href=\"(/files/[0-9]+.(jpg|png))\" target=\"_blank\">";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(threadSrc);
			
			int ctr = 0;
			
			while(m.find()) {
				if(m.groupCount() == 2) {
					String imgLink = URL_BASE+m.group(1);
					callback.found(imgLink);
					ctr ++;
				}
			}
			
			Helpers.log("Found " + ctr + " images on " + threadLink);
		}		
	}

	public static void main(String[] args) throws Exception {
		final String[] boards = {"b", "v", "int"};
		
		KCImageCollector tlc = new KCImageCollector();		
		List<String> imgLinks = tlc.collectImgLinksForBoards(boards);				
		buildHtmlFileOutOfImgLinks(imgLinks, "kcdump.html");
	}
	
	public static String thumbnailLinkFromImgLink(String imgLink) {
		return imgLink.replace("/files/", "/thumbnails/");
	}
	
	public String pollUnvisitedThreadLink(String boardName, List<String> pageLinks, List<String> visitedThreadLinks) throws Exception {
		for(String pageLink : pageLinks) {
			List<String> tls = collectThreadLinksForPage(pageLink, boardName);
			for(String tl : tls) {
				if(!visitedThreadLinks.contains(tl)) {
					visitedThreadLinks.add(tl);
					return tl;
				}
			}
		}
		return null;
	}

	public List<String> startImgLinkIteration(IterationState is, int minImgCount, ProgressCallback callback) throws Exception {
		callback.update(-1);
		is.pageLinks = collectPageLinks(URL_BASE+"/"+is.boardName+"/", is.boardName);
		return continueImgLinkIteration(is, minImgCount, callback);
	}
	
	public List<String> continueImgLinkIteration(IterationState is, int minImgCount, ProgressCallback callback) throws Exception {		
		List<String> imgLinks = new LinkedList<String>();
		callback.update(-1);
		while(imgLinks.size() < minImgCount) {
			String threadLink = pollUnvisitedThreadLink(is.boardName, is.pageLinks, is.visitedThreadLinks);
			if(threadLink != null) {
				imgLinks.addAll(collectImgLinksForThread(threadLink));
				callback.update((int)(Math.min((float)imgLinks.size() / minImgCount * 100.0f, 100.0f)));
			} else {
				callback.update(100);
				is.done = true;
				return imgLinks;
			}
		}
		return imgLinks;
	}
	
}
