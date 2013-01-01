package org.ox17.kcimagecollector;

import java.util.List;

public class BatchRunner {

	public static void main(String[] args) throws Exception {
		final String[] boards = {"b", "v", "int"};

		KCImageCollector tlc = new KCImageCollector();
		List<String> imgLinks = tlc.collectImgLinksForBoards(boards);
		KCImageCollector.buildHtmlFileOutOfImgLinks(imgLinks, "kcdump.html");
	}

}
