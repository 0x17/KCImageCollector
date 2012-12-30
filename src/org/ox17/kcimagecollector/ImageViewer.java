package org.ox17.kcimagecollector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageViewer extends JFrame {
	
	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Image img = null;
		
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
		
		private int getImgWidth() {
			return scaled ? (int)(getBounds().width * 0.8f) : img.getWidth(this);
		}
		
		private int getImgHeight() {
			return scaled ? (int)(getBounds().height * 0.8f) : img.getHeight(this);
		}
	}
	
	private static final long serialVersionUID = 1L;
	private static final int NUM_PRELOADED_THUMBS = 10;
	
	private Map<Integer, Image> preloadedThumbs = new HashMap<Integer, Image>(NUM_PRELOADED_THUMBS);
	
	private ImagePanel imgPanel;
	private JLabel topLbl = new JLabel(":3");
	private JButton backBtn, discardBtn, saveBtn, scaleBtn;
	private KCImageCollector kic = new KCImageCollector();
	private List<String> imgLinks = null;
	private int numImgLinks;
	private int curImgIndex;

	private Dimension initialDim = new Dimension(800, 600);
	private boolean scaled = false;
	
	public ImageViewer() throws Exception {
		super("KCImageViewer");
		Helpers.addLogCallback(new MessageCallback() {
			public void action(String msg) {
				topLbl.setText(msg);
				getContentPane().repaint();
			}
		});
		initFrame();
		initImgPanel();
		initButtons();
		initButtonPanel();
		initKeyEventDispatcher();
	}

	private void initKeyEventDispatcher() {
		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		KeyEventDispatcher ked = new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				switch(e.getID()) {
				case KeyEvent.KEY_PRESSED:
					keyPressed(e);
					break;
				case KeyEvent.KEY_RELEASED:
					break;
				case KeyEvent.KEY_TYPED:
					break;
				}
				return false;
			}
			private void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					backBtn.doClick();
					break;
				case KeyEvent.VK_RIGHT:
					discardBtn.doClick();
					break;
				case KeyEvent.VK_DOWN:
					saveBtn.doClick();
					break;
				case KeyEvent.VK_ESCAPE:
					System.exit(0);
					break;
				}
			}
		};
		kfm.addKeyEventDispatcher(ked);
	}

	private void loadLinks() throws Exception {
		imgLinks = kic.collectImgLinksForBoards(new String[]{"m"});
		numImgLinks = imgLinks.size();
		curImgIndex = -1;
		showNextImg();
	}

	public void showNextImg() throws Exception {
		if(imgLinks != null) {
			curImgIndex++;
			if(curImgIndex >= numImgLinks) {
				curImgIndex = numImgLinks - 1;
			}
			paintCurImg();
			updatePreloads();
		}
	}
	
	// FIXME: Does this delay painting?
	private void updatePreloads() throws Exception {
		int[] addedIndices = new int[NUM_PRELOADED_THUMBS];
		int j = 0;
		for(int i = curImgIndex + 1; i < curImgIndex + 1 + NUM_PRELOADED_THUMBS && i < numImgLinks; i++) {
			addedIndices[j++] = i;
			if(!preloadedThumbs.containsKey(i)) {
				Image imgObj = Helpers.imgFromUrl(KCImageCollector.thumbnailLinkFromImgLink(imgLinks.get(i)));
				preloadedThumbs.put(i, imgObj);
				Toolkit.getDefaultToolkit().prepareImage(imgObj, imgPanel.getImgWidth(), imgPanel.getImgHeight(), imgPanel);
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
	}

	private void paintCurImg() throws Exception {
		String curLink = imgLinks.get(curImgIndex);
		boolean hit;
		if(preloadedThumbs.containsKey(curImgIndex)) {
			imgPanel.setToImage(preloadedThumbs.get(curImgIndex));
			hit = true;
		} else {
			imgPanel.setToImageFromLink(KCImageCollector.thumbnailLinkFromImgLink(curLink));
			hit = false;
		}
		Helpers.log("Showing image: " + curLink + " (" + (curImgIndex+1) + "/" + numImgLinks + ") cache " + (hit ? "hit" : "miss"));		
	}

	public void showPrevImg() throws Exception {
		if(imgLinks != null) {
			curImgIndex--;
			curImgIndex = curImgIndex < 0 ? 0 : curImgIndex;
			paintCurImg();
			updatePreloads();
		}
	}
	
	public void saveCurImg() throws Exception {
		if(imgLinks != null) {
			String imgUrl = imgLinks.get(curImgIndex);
			Helpers.loadUrlIntoFile(imgUrl, new File(Helpers.getImgFilenameFromUrl(imgUrl)));
		}
	}

	private void initImgPanel() throws Exception {
		imgPanel = new ImagePanel();
		add(imgPanel, BorderLayout.CENTER);
	}
	
	private void initButtons() {
		initBackButton();
		initDiscardButton();
		initSaveButton();
		initScaleButton();
	}
	
	private void initScaleButton() {
		scaleBtn = new JButton("Maximize");
		scaleBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				scaled = !scaled;
				scaleBtn.setText(scaled ? "Minimize" : "Maximize");
				imgPanel.repaint();
			}
		});
	}

	private void initSaveButton() {
		saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					saveCurImg();
					showNextImg();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	private void initDiscardButton() {
		discardBtn = new JButton("Next");
		discardBtn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					showNextImg();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void initBackButton() {
		backBtn = new JButton("Prev");
		backBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					showPrevImg();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void initButtonPanel() {	
		JPanel lowerPane = new JPanel(new FlowLayout());
		lowerPane.add(backBtn);	
		lowerPane.add(saveBtn);
		lowerPane.add(discardBtn);
		lowerPane.add(scaleBtn);
		add(lowerPane, BorderLayout.SOUTH);
	}

	private void initFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(initialDim);
		setResizable(true);
		setLocationRelativeTo(null);
		add(topLbl, BorderLayout.NORTH);
	}

	public static void main(String[] args) throws Exception {
		ImageViewer iv = new ImageViewer();
		iv.setVisible(true);
		iv.loadLinks();
	}
}
