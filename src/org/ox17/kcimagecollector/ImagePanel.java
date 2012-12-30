package org.ox17.kcimagecollector;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Image img = null;
	private boolean scaled;
	
	public ImagePanel() throws Exception {
	}
	
	public void setToImageFromLink(String urlStr) throws Exception {
		img = Helpers.imgFromUrl(urlStr);
		repaint();		
	}
	
	public void setToImage(Image img) {
		this.img = img;
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(img != null) {
			Dimension boundsDim = new Dimension(getBounds().width, getBounds().height);
			
			int imgWidth = getImgWidth();
			int imgHeight = getImgHeight();
			
			if(!scaled) {
				int x = (int)((boundsDim.width - imgWidth) / 2.0f);
				int y = (int)((boundsDim.height - imgHeight) / 2.0f);
				g.drawImage(img, x, y, this);
			} else {
				int x = (int)((boundsDim.width - imgWidth) / 2.0f);
				int y = (int)((boundsDim.height - imgHeight) / 2.0f);
				g.drawImage(img, x, y, imgWidth, imgHeight, this);
			}
		}
	}
	
	public int getImgWidth() {
		return scaled ? (int)(getBounds().width * 0.8f) : img.getWidth(this);
	}
	
	public int getImgHeight() {
		return scaled ? (int)(getBounds().height * 0.8f) : img.getHeight(this);
	}

	public void toggleScale() {
		scaled = !scaled;
	}

	public boolean isScaled() {
		return scaled;
	}
}
