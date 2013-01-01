package org.ox17.kcimagecollector;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonPanel extends JPanel {

	private JButton backBtn, discardBtn, saveBtn, scaleBtn, copyBtn;
	private ImageViewer viewer;

	public ButtonPanel(ImageViewer viewer) {
		super(new FlowLayout());
		this.viewer = viewer;
		initButtons();
		addButtons();
	}

	private void initButtons() {
		initBackButton();
		initDiscardButton();
		initSaveButton();
		initScaleButton();
		initCopyButton();
	}

	private void addButtons() {
		add(backBtn);
		add(saveBtn);
		add(discardBtn);
		add(scaleBtn);
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
