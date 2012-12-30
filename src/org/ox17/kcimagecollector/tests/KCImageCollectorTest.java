package org.ox17.kcimagecollector.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ox17.kcimagecollector.KCImageCollector;

public class KCImageCollectorTest {

	private static final String ROOT_URL = "http://www.krautchan.net/b/";
	private static final String PAGE_URL = "http://www.krautchan.net/b/1.html";
	private static final String BOARD_NAME = "b";
	private KCImageCollector kcic;

	@Before
	public void setUp() throws Exception {
		this.kcic = new KCImageCollector();
	}

	@Test
	public void testCollectPageLinks() throws Exception {
		List<String> pageLinks = kcic.collectPageLinks(ROOT_URL, BOARD_NAME);
		assertFalse(pageLinks.isEmpty());
	}

	@Test
	public void testCollectThreadLinks() throws Exception {
		List<String> threadLinks = kcic.collectThreadLinks(ROOT_URL, BOARD_NAME);
		assertFalse(threadLinks.isEmpty());
	}

	@Test
	public void testCollectThreadLinksForPage() throws Exception {
		List<String> threadLinks = kcic.collectThreadLinksForPage(PAGE_URL, BOARD_NAME);
		assertFalse(threadLinks.isEmpty());
	}

	@Test
	public void testCollectImgLinksForThread() throws Exception {
		List<String> imgLinks = kcic.collectImgLinksForThread(kcic.collectThreadLinksForPage(PAGE_URL, BOARD_NAME).get(0));
		assertFalse(imgLinks.isEmpty());
	}

	@Test
	public void testThumbnailLinkFromImgLink() {
		assertEquals("http://krautchan.net/thumbnails/1355728870001.jpg", KCImageCollector.thumbnailLinkFromImgLink("http://krautchan.net/files/1355728870001.jpg"));
	}

}
