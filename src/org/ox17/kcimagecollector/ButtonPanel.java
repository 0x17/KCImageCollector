package org.ox17.kcimagecollector;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ButtonPanel extends JPanel {
	private static final long serialVersionUID = 6060989310045317163L;
	
	private JButton backBtn;
	private JButton discardBtn;
	private JButton saveBtn;
	private JButton scaleBtn;
	private JButton previewBtn;
	private JButton copyBtn;
	
	private ImageViewer viewer;
	private final PreviewFrame previewFrame;

	public ButtonPanel(ImageViewer viewer) {
		super(new FlowLayout());
		this.viewer = viewer;
		this.previewFrame = viewer.getPreviewFrame();
		initButtons();
		addButtons();
	}

	private void initButtons() {
		initBackButton();
		initDiscardButton();
		initSaveButton();
		initScaleButton();
		initPreviewButton();
		initCopyButton();
	}

	private void initPreviewButton() {
		previewBtn = new JButton("Preview");
		previewBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					previewFrame.previewImgWithUrl(viewer.getCurImgLink());
				} catch(Exception ex) {
					Helpers.showException(ex);
					ex.printStackTrace();
				}
			}
		});
	}

	private void addButtons() {
		add(backBtn);
		add(saveBtn);
		add(discardBtn);
		add(scaleBtn);
		add(previewBtn);
		add(copyBtn);
	}

	private void initCopyButton() {
		copyBtn = new JButton("Copy");
		copyBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String curImgLink = viewer.getCurImgLink();
				StringSelection selection = new StringSelection(curImgLink);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});
	}



	private void initScaleButton() {
		scaleBtn = new JButton("Maximize");
		scaleBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				ImagePanel imgPanel = viewer.getImagePanel();
				ImageScaler imgScaler = imgPanel.getScaler();
				imgScaler.toggleScale();
				scaleBtn.setText(imgScaler.isScaled() ? "Minimize" : "Maximize");
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
					viewer.saveCurImg();
					viewer.showNextImg();
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
					viewer.tryShowNext();
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
					viewer.tryShowPrev();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void clickPrev() {
		backBtn.doClick();
	}

	public void clickNext() {
		discardBtn.doClick();
	}

	public void clickSave() {
		saveBtn.doClick();
	}
}
