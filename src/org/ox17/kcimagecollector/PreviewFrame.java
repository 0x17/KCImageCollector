package org.ox17.kcimagecollector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PreviewFrame extends JFrame {

	private static final long serialVersionUID = 4680424458007936810L;
	
	private ImagePanel imgPanel;
	private String lastImgUrl;
	private JLabel topLbl = new JLabel(":3");

	public PreviewFrame() throws Exception {
		super("Preview");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLayout(new BorderLayout());

		add(topLbl, BorderLayout.NORTH);

		imgPanel = new ImagePanel();
		add(imgPanel, BorderLayout.CENTER);

		initButtonPane();
		initSize();

		setLocationRelativeTo(null);
	}

	private void initButtonPane() {
		JPanel btnPane = new JPanel(new FlowLayout());
		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveImg();
				} catch(Exception ex) {
					Helpers.showException(ex);
					ex.printStackTrace();
				}
			}
		});
		JButton hideBtn = new JButton("Hide");
		hideBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnPane.add(saveBtn);
		btnPane.add(hideBtn);
		add(btnPane, BorderLayout.SOUTH);
	}

	private void initSize() {
		Dimension screenDims = Helpers.getScreenDims();
		int size = (screenDims.getWidth() < screenDims.getHeight()) ? (int)(screenDims.getWidth() * 0.8f) : (int)(screenDims.getHeight() * 0.8f);
		setSize(size, size);
		setResizable(false);
	}

	private void saveImg() throws Exception {
		if(lastImgUrl == null) return;
		Helpers.loadUrlIntoFile(lastImgUrl, new File(Helpers.getImgFilenameFromUrl(lastImgUrl)));
	}

	public void previewImgWithUrl(String imgUrl) throws Exception {
		topLbl.setText(imgUrl);
		this.lastImgUrl = imgUrl;
		setVisible(true);
		imgPanel.setToImageFromLink(imgUrl);
	}
}
