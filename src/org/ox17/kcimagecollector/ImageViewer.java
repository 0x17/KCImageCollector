package org.ox17.kcimagecollector;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

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
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if(img != null)
				g.drawImage(img, 0, 0, this);
		}		
	}
	
	private static final long serialVersionUID = 1L;

	private ImagePanel imgPanel;
	private JLabel topLbl = new JLabel(":3");
	private JButton backBtn, discardBtn, saveBtn;
	private KCImageCollector kic = new KCImageCollector();
	private List<String> imgLinks = null;
	private int curImgIndex;
	
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
		curImgIndex = -1;
		showNextImg();
	}

	public void showNextImg() throws Exception {
		if(imgLinks != null) {
			curImgIndex++;
			paintCurImg();			 
		}
	}
	
	private void paintCurImg() throws Exception {
		String curLink = imgLinks.get(curImgIndex);
		imgPanel.setToImageFromLink(KCImageCollector.thumbnailLinkFromImgLink(curLink));
		Helpers.log("Showing image: " + curLink + " (" + (curImgIndex+1) + "/" + imgLinks.size() + ")");		
	}

	public void showPrevImg() throws Exception {
		if(imgLinks != null) {
			curImgIndex--;
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
		discardBtn = new JButton("Discard");
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
		backBtn = new JButton("Back");
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
		add(lowerPane, BorderLayout.SOUTH);
	}

	private void initFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(800, 600);
		setResizable(false);
		setLocationRelativeTo(null);
		add(topLbl, BorderLayout.NORTH);
	}

	public static void main(String[] args) throws Exception {
		ImageViewer iv = new ImageViewer();
		iv.setVisible(true);
		iv.loadLinks();
	}
}
