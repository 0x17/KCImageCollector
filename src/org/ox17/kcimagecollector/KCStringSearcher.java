package org.ox17.kcimagecollector;

import java.util.LinkedList;
import java.util.List;

public class KCStringSearcher {
	
	private final static String URL_BASE = "http://krautchan.net";
	public List<String> searchStringInBoard(String str, String boardName) throws Exception {
		str = str.toLowerCase();
		List<String> threads = new LinkedList<String>();
		KCImageCollector kcic = new KCImageCollector();
		List<String> threadLinks = kcic.collectThreadLinks(URL_BASE+"/"+boardName+"/", boardName);
		int nhits = 0;
		for(String threadLink : threadLinks) {
			System.out.println("Looking for \""+str+"\" in " + threadLink + " ...");
			String threadSrc = Helpers.loadUrlIntoStr(threadLink).toLowerCase();
			if(threadSrc.contains(str)) {
				System.out.println("Found it: " + threadLink);
				nhits++;
				threads.add(threadLink);
			}
		}
		System.out.println(nhits + " Hits!");
		return threads;
	}
	
	public static void main(String[] args) throws Exception {
		KCStringSearcher kcss = new KCStringSearcher();
		List<String> results = kcss.searchStringInBoard("funpark", "b");
		for(String result : results)
			System.out.println(result);
	}

}
