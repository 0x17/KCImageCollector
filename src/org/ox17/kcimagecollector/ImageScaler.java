package org.ox17.kcimagecollector;

import java.awt.*;

public class ImageScaler {

	private boolean scaled;
	private final ImagePanel imagePanel;

	public ImageScaler(ImagePanel imgPanel) {
		this.imagePanel = imgPanel;
	}

	private class ScaleData {
		public int imgW;
		public int imgH;
		public float ratio;
	}

	private ScaleData scaleDataForImage(Image image) {
		ScaleData sd = new ScaleData();
		sd.imgW = image.getWidth(imagePanel);
		sd.imgH = image.getHeight(imagePanel);
		sd.ratio = (float)sd.imgW / sd.imgH;
		return sd;
	}

	public int getImgWidth(Image image) {
		ScaleData sd = scaleDataForImage(image);
		int scaledW;
		if(sd.ratio >= 1.0f) {
			scaledW = (int)(imagePanel.getBounds().width * 0.8f);
		} else {
			scaledW = (int)(((float)getImgHeight(image) / sd.imgH) * sd.imgW);
		}
		return scaled ? scaledW : sd.imgW;
	}

	public int getImgHeight(Image image) {
		ScaleData sd = scaleDataForImage(image);
		int scaledH;
		if(sd.ratio < 1.0f) {
			scaledH = (int)(imagePanel.getBounds().height * 0.8f);
		} else {
			scaledH = (int)(((float)getImgWidth(image) / sd.imgW) * sd.imgH);
		}
		return scaled ? scaledH : sd.imgH;
	}

	public void toggleScale() {
		scaled = !scaled;
	}

	public boolean isScaled() {
		return scaled;
	}

}
