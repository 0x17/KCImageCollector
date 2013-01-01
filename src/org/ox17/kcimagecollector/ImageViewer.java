package org.ox17.kcimagecollector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ImageViewer extends JFrame {
	
	private static final long serialVersionUID = 1L;

	protected static final String NO_IMGS_LOADED_MSG = "No images loaded yet. Please wait for \"Found x images...\" message!";

	private ImagePanel imgPanel;
	private ButtonPanel btnPanel;
	private JLabel topLbl = new JLabel(":3"), imgIdLbl = new JLabel(":3");

	private PreloadManager preloadMgr;

	private List<String> imgLinks;
	private int curImgIndex;

	private Dimension initialDim = new Dimension(800, 600);
	private JProgressBar pbar;

	private PreviewFrame previewFrame = new PreviewFrame();
	
	private LinkFoundCallback lfcallback = new LinkFoundCallback() {
		@Override
		public void found(String linkUrl) {
			if(!imgLinks.contains(linkUrl)) {
				imgLinks.add(linkUrl);
			}
		}
	};

	public ImageViewer() throws Exception {
		super("KCImageViewer");
		Helpers.addLogCallback(new MessageCallback() {
			public void action(String msg) {
				topLbl.setText(msg);
				getContentPane().repaint();
			}
		});
		initFrame();
		initTopPanel();
		initImgPanel();
		initLowerPanel();
		initKeyEventDispatcher();
	}

	private void initTopPanel() {
		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		add(topPanel, BorderLayout.NORTH);
		topPanel.add(topLbl);
		topPanel.add(imgIdLbl);
	}

	private void initLowerPanel() {
		JPanel lowerPane = new JPanel(new BorderLayout());
		btnPanel = new ButtonPanel(this);
		lowerPane.add(btnPanel, BorderLayout.CENTER);
		add(lowerPane, BorderLayout.SOUTH);
		pbar = new JProgressBar();
		pbar.setIndeterminate(true);
		lowerPane.add(pbar, BorderLayout.EAST);
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
					btnPanel.clickPrev();// backBtn.doClick();
					break;
				case KeyEvent.VK_RIGHT:
					btnPanel.clickNext();// discardBtn.doClick();
					break;
				case KeyEvent.VK_DOWN:
					btnPanel.clickSave(); //saveBtn.doClick();
					break;
				case KeyEvent.VK_ESCAPE:
					System.exit(0);
					break;
				}
			}
		};
		kfm.addKeyEventDispatcher(ked);
	}

	public void loadLinks(String boardName) throws Exception {
		imgLinks = new LinkedList<String>();
		/*Thread collectThread = */KCImageCollector.collectImgLinksForBoardConcurrent(boardName, lfcallback);
		imgLinks.add(KRAUTMARIE_LINK);
		curImgIndex = -1;

		preloadMgr = new PreloadManager(this);

		showNextImg();
	}

	public void showNextImg() throws Exception {
		if(imgLinks != null && !imgLinks.isEmpty()) {
			curImgIndex++;
			if(curImgIndex >= imgLinks.size()) {
				curImgIndex = imgLinks.size() - 1;
			}
			
			if(preloadMgr.preloadsInvalidated(curImgIndex))
				preloadMgr.updatePreloads(curImgIndex);
			
			paintCurImg();			
		}
	}

	private final static String KRAUTMARIE_LINK = "http://krautchan.net/images/krautmarie-kcwallpaper-klein.jpg";

	private void paintCurImg() throws Exception {
		String curLink = imgLinks.get(curImgIndex);

		if(curLink.equals(KRAUTMARIE_LINK)) {
			imgPanel.setToImageFromLink(KRAUTMARIE_LINK);
			imgIdLbl.setText("Current image: Krautmarie");
			return;
		}

		if(preloadMgr.isImgPreloaded(curImgIndex)) {
			imgPanel.setToImage(preloadMgr.getPreloadedImg(curImgIndex));
		} else {
			imgPanel.setToImageFromLink(KCImageCollector.thumbnailLinkFromImgLink(curLink));
		}
		imgIdLbl.setText("Current image: " + curLink + " (" + (curImgIndex+1) + "/" + imgLinks.size() + ")");
	}

	private void showPrevImg() throws Exception {
		if(imgLinks != null) {
			curImgIndex--;
			curImgIndex = curImgIndex < 0 ? 0 : curImgIndex;
			paintCurImg();
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

	String getCurImgLink() {
		return imgLinks.get(curImgIndex);
	}

	private void initFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(initialDim);
		setResizable(true);
		setLocationRelativeTo(null);
	}

	public ImagePanel getImagePanel() {
		return imgPanel;
	}

	public void tryShowNext() throws Exception {
		if(imgLinks.size() == 1) {
			JOptionPane.showMessageDialog(null, NO_IMGS_LOADED_MSG, "Info", JOptionPane.INFORMATION_MESSAGE);
		}
		showNextImg();
	}

	public void tryShowPrev() throws Exception {
		if(imgLinks.size() == 1) {
			JOptionPane.showMessageDialog(null, NO_IMGS_LOADED_MSG, "Info", JOptionPane.INFORMATION_MESSAGE);
		}
		showPrevImg();
	}

	public List<String> getImageLinks() {
		return imgLinks;
	}

	public PreviewFrame getPreviewFrame() {
		return previewFrame;
	}
}
