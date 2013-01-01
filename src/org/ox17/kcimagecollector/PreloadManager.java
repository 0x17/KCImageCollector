package org.ox17.kcimagecollector;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PreloadManager {

	private static final int NUM_PRELOADED_THUMBS = 20;
	private final ImageViewer viewer;
	private ImagePanel imgPanel;
	private Map<Integer, Image> preloadedThumbs = new HashMap<Integer, Image>(NUM_PRELOADED_THUMBS);
	private final List<String> imgLinks;

	public PreloadManager(ImageViewer viewer) {
		this.viewer = viewer;
		this.imgPanel = viewer.getImagePanel();
		this.imgLinks = viewer.getImageLinks();
	}

	public void updatePreloads(int curImgIndex) throws Exception {
		MediaTracker tracker = new MediaTracker(viewer);

		int[] addedIndices = new int[NUM_PRELOADED_THUMBS];
		int j = 0;
		for(int i = curImgIndex + 1; i < curImgIndex + 1 + NUM_PRELOADED_THUMBS && i < imgLinks.size(); i++) {
			addedIndices[j++] = i;
			if(!preloadedThumbs.containsKey(i) && i != 0) {
				Image imgObj = Helpers.imgFromUrl(KCImageCollector.thumbnailLinkFromImgLink(imgLinks.get(i)));
				preloadedThumbs.put(i, imgObj);
				imgPanel.prepareImage(imgObj, tracker, i);
			}
		}

		List<Integer> keysToRemove = new LinkedList<Integer>();
		for(int ix : preloadedThumbs.keySet()) {
			boolean wasAdded = false;
			for(int i=0; i<j; i++) {
				if(addedIndices[i] == ix) {
					wasAdded = true;
					break;
				}
			}
			if(!wasAdded)
				keysToRemove.add(ix);
		}

		for(int key : keysToRemove) {
			preloadedThumbs.get(key).flush();
			preloadedThumbs.remove(key);
		}

		tracker.waitForAll();
	}

	public boolean preloadsInvalidated(int curImgIndex) {
		return !preloadedThumbs.containsKey(curImgIndex+1);
	}

	public boolean isImgPreloaded(int imgIndex) {
		return preloadedThumbs.containsKey(imgIndex);
	}

	public Image getPreloadedImg(int imgIndex) {
		return preloadedThumbs.get(imgIndex);
	}
}
