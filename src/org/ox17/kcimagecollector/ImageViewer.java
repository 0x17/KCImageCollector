package org.ox17.kcimagecollector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ImageViewer extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static final int NUM_PRELOADED_THUMBS = 20;
	protected static final String NO_IMGS_LOADED_MSG = "No images loaded yet. Please wait for \"Found x images...\" message!";
	
	private Map<Integer, Image> preloadedThumbs = new HashMap<Integer, Image>(NUM_PRELOADED_THUMBS);
	
	private ImagePanel imgPanel;
	private JLabel topLbl = new JLabel(":3"), imgIdLbl = new JLabel(":3");
	private JButton backBtn, discardBtn, saveBtn, scaleBtn, copyBtn;
	private List<String> imgLinks;
	private int curImgIndex;

	private Dimension initialDim = new Dimension(800, 600);
	private JProgressBar pbar;
	
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
		initButtons();		
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
		lowerPane.add(initButtonPanel(), BorderLayout.CENTER);
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
		imgLinks = new LinkedList<String>();
		/*Thread collectThread = */KCImageCollector.collectImgLinksForBoardConcurrent("b", lfcallback);
		imgLinks.add("http://krautchan.net/files/1331112741002.png");
		curImgIndex = -1;
		showNextImg();
	}

	public void showNextImg() throws Exception {
		if(imgLinks != null && !imgLinks.isEmpty()) {
			curImgIndex++;
			if(curImgIndex >= imgLinks.size()) {
				curImgIndex = imgLinks.size() - 1;
			}
			
			if(preloadsInvalidated())
				updatePreloads();
			
			paintCurImg();			
		}
	}
	
	private boolean preloadsInvalidated() {
		return !preloadedThumbs.containsKey(curImgIndex+1);
	}

	private void updatePreloads() throws Exception {		
		MediaTracker tracker = new MediaTracker(this);
		
		int[] addedIndices = new int[NUM_PRELOADED_THUMBS];
		int j = 0;
		for(int i = curImgIndex + 1; i < curImgIndex + 1 + NUM_PRELOADED_THUMBS && i < imgLinks.size(); i++) {
			addedIndices[j++] = i;
			if(!preloadedThumbs.containsKey(i)) {
				Image imgObj = Helpers.imgFromUrl(KCImageCollector.thumbnailLinkFromImgLink(imgLinks.get(i)));
				preloadedThumbs.put(i, imgObj);
				int w = imgPanel.getImgWidth(imgObj);
				int h = imgPanel.getImgHeight(imgObj);
				tracker.addImage(imgObj, i, w, h);
				Toolkit.getDefaultToolkit().prepareImage(imgObj, w, h, imgPanel);
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

	private void paintCurImg() throws Exception {
		String curLink = imgLinks.get(curImgIndex);
		if(preloadedThumbs.containsKey(curImgIndex)) {
			imgPanel.setToImage(preloadedThumbs.get(curImgIndex));
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
	
	private void initButtons() {
		initBackButton();
		initDiscardButton();
		initSaveButton();
		initScaleButton();
		initCopyButton();
	}
	
	private void initCopyButton() {
		copyBtn = new JButton("Copy");
		copyBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String curImgLink = getCurImgLink();
				StringSelection selection = new StringSelection(curImgLink);
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    clipboard.setContents(selection, selection);
			}
		});
	}
	
	private String getCurImgLink() {
		return imgLinks.get(curImgIndex);
	}

	private void initScaleButton() {
		scaleBtn = new JButton("Maximize");
		scaleBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				imgPanel.toggleScale();
				scaleBtn.setText(imgPanel.isScaled() ? "Minimize" : "Maximize");
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
					if(imgLinks.size() == 1) {
						JOptionPane.showMessageDialog(null, NO_IMGS_LOADED_MSG, "Info", JOptionPane.INFORMATION_MESSAGE);
					}
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
					if(imgLinks.size() == 1) {
						JOptionPane.showMessageDialog(null, NO_IMGS_LOADED_MSG, "Info", JOptionPane.INFORMATION_MESSAGE);
					}
					showPrevImg();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JPanel initButtonPanel() {	
		JPanel buttonPane = new JPanel(new FlowLayout());
		buttonPane.add(backBtn);	
		buttonPane.add(saveBtn);
		buttonPane.add(discardBtn);
		buttonPane.add(scaleBtn);
		buttonPane.add(copyBtn);
		return buttonPane;
	}

	private void initFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(initialDim);
		setResizable(true);
		setLocationRelativeTo(null);
	}

	public static void main(String[] args) throws Exception {
		ImageViewer iv = new ImageViewer();
		iv.setVisible(true);
		iv.loadLinks();
	}
}
