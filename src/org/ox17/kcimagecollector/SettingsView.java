package org.ox17.kcimagecollector;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class SettingsView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextField boardNameField;

	public SettingsView() {
		super("Settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel settingsPanel = initSettingsPanel();
		add(settingsPanel, BorderLayout.CENTER);

		JButton okBtn = new JButton("Apply");
		okBtn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {			
				try {
					String boardName = boardNameField.getText();
					validateBoardName(boardName);
					ImageViewer iv;
					iv = new ImageViewer();
					iv.setVisible(true);
					iv.loadLinks(boardName.replace("/", ""));
					setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
					Helpers.showException(e);
				}
			}
		});
		add(okBtn, BorderLayout.SOUTH);
		
		setSize(300, 200);
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private JPanel initSettingsPanel() {
		JPanel settingsPanel = new JPanel(new GridLayout(2,2));
		JLabel hubLbl = new JLabel("Hub: ");
		settingsPanel.add(hubLbl);
		JComboBox hubCombo = new JComboBox(new String[] {"Krautchan"});
		settingsPanel.add(hubCombo);
		JLabel boardNameLbl = new JLabel("Board name:");
		settingsPanel.add(boardNameLbl);
		boardNameField = new JTextField("/b/");
		settingsPanel.add(boardNameField);
		return settingsPanel;
	}

	private void validateBoardName(String boardName) throws InvalidBoardNameException {
		if(boardName.charAt(0) != '/' // must contain slash at beginning
			|| boardName.charAt(boardName.length()-1) != '/' // must contain slash at end
				|| boardName.substring(1, boardName.length()-2).contains("/") // must not contain slash in between
				|| !boardName.toLowerCase().equals(boardName)) // must not contain upper case
			throw new InvalidBoardNameException(boardName);
	}
}
