package org.ox17.kcimagecollector;

import java.awt.*;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Image img = null;
	private ImageScaler imageScaler = new ImageScaler(this);
	
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
			
			if(!imageScaler.isScaled()) {
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

	public void prepareImage(Image imgObj, MediaTracker tracker, int id) {
		int w = imageScaler.getImgWidth(imgObj);
		int h = imageScaler.getImgHeight(imgObj);
		tracker.addImage(imgObj, id, w, h);
		Toolkit.getDefaultToolkit().prepareImage(imgObj, w, h, this);
	}
	
	public int getImgWidth() {
		return imageScaler.getImgWidth(this.img);
	}
	
	public int getImgHeight() {
		return imageScaler.getImgHeight(this.img);
	}


	public ImageScaler getScaler() {
		return imageScaler;
	}
}
