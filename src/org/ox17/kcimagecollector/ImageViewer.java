package org.ox17.kcimagecollector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MediaTracker;
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
import javax.swing.JProgressBar;

public class ImageViewer extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static final int NUM_PRELOADED_THUMBS = 20;
	
	private Map<Integer, Image> preloadedThumbs = new HashMap<Integer, Image>(NUM_PRELOADED_THUMBS);
	
	private ImagePanel imgPanel;
	private JLabel topLbl = new JLabel(":3");
	private JButton backBtn, discardBtn, saveBtn, scaleBtn;
	private KCImageCollector kic = new KCImageCollector();
	private List<String> imgLinks = null;
	private int curImgIndex;

	private Dimension initialDim = new Dimension(800, 600);
	private IterationState iterationState = new IterationState("b");
	private JProgressBar pbar;
	private ProgressCallback pcallback = new ProgressCallback() {
		@Override
		public void update(int progress) {
			if(progress == -1) {
				pbar.setIndeterminate(true);
			}
			else {
				if(pbar.isIndeterminate())
					pbar.setIndeterminate(false);
				
				pbar.setValue(progress);
			}
			
			repaint();
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
		initImgPanel();
		initButtons();		
		initLowerPanel();		
		initKeyEventDispatcher();
	}

	private void initLowerPanel() {
		JPanel lowerPane = new JPanel(new BorderLayout());
		lowerPane.add(initButtonPanel(), BorderLayout.CENTER);
		add(lowerPane, BorderLayout.SOUTH);		
		pbar = new JProgressBar();
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
		imgLinks = kic.startImgLinkIteration(iterationState, 15, pcallback);
		curImgIndex = -1;
		showNextImg();
	}

	public void showNextImg() throws Exception {
		if(imgLinks != null) {
			curImgIndex++;
			if(curImgIndex >= imgLinks.size()) {
				if(iterationState.done)
					curImgIndex = imgLinks.size() - 1;
				else {
					imgLinks.addAll(kic.continueImgLinkIteration(iterationState, 15, pcallback));
				}
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
		pbar.setIndeterminate(true);
		repaint();
		
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
		
		pbar.setIndeterminate(false);
		repaint();
	}

	private void paintCurImg() throws Exception {
		String curLink = imgLinks.get(curImgIndex);
		if(preloadedThumbs.containsKey(curImgIndex)) {
			imgPanel.setToImage(preloadedThumbs.get(curImgIndex));
		} else {
			imgPanel.setToImageFromLink(KCImageCollector.thumbnailLinkFromImgLink(curLink));
		}
		Helpers.log("Showing image: " + curLink + " (" + (curImgIndex+1) + "/" + imgLinks.size() + ")");		
	}

	public void showPrevImg() throws Exception {
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

	private JPanel initButtonPanel() {	
		JPanel buttonPane = new JPanel(new FlowLayout());
		buttonPane.add(backBtn);	
		buttonPane.add(saveBtn);
		buttonPane.add(discardBtn);
		buttonPane.add(scaleBtn);
		return buttonPane;
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
