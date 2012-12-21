package org.ox17.kcimagecollector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KCImageCollector {
	
	private final static String URL_BASE = "http://krautchan.net";
	
	public List<String> collectPageLinks(String rootUrl, String boardName) throws Exception {
		String rootPageSrc = Helpers.loadUrlIntoStr(rootUrl);
		List<String> pageLinks = new LinkedList<String>();
		
		pageLinks.add(rootUrl);	
		
		Pattern p = Pattern.compile("<a href=\"(/"+boardName+"/[0-9]+.html)\">");
		Matcher m = p.matcher(rootPageSrc);
		
		while(m.find()) {
			if(m.groupCount() == 1) {
				String pageLink = URL_BASE+m.group(1);
				if(!pageLinks.contains(pageLink))
					pageLinks.add(pageLink);
			}
		}
		
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
		
		return threadLinks;
	}
	
	public List<String> collectImgLinksForThread(String threadLink) throws Exception {
		String threadSrc;
		List<String> imgLinks = new LinkedList<String>();
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
		return imgLinks;
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

	public static void main(String[] args) throws Exception {
		final String[] boards = {"b", "v", "int"};
		
		KCImageCollector tlc = new KCImageCollector();		
		List<String> imgLinks = new LinkedList<String>();
		
		for(String board : boards) {
			Helpers.log("Collect for /" + board + "/...");
			List<String> tls = tlc.collectThreadLinks(URL_BASE+"/"+board+"/", board);
			for(String tl : tls) {
				Helpers.log("Collecting image links for thread " + tl + " ...");
				imgLinks.addAll(tlc.collectImgLinksForThread(tl));
			}
		}
				
		buildHtmlFileOutOfImgLinks(imgLinks, "kcdump.html");
	}
	
	public static String thumbnailLinkFromImgLink(String imgLink) {
		return imgLink.replace("/files/", "/thumbnails/");
	}
	
}
