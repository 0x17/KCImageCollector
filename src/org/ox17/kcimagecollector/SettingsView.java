package org.ox17.kcimagecollector;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class SettingsView extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public SettingsView() {
		super("Settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(3, 2));

		JLabel hubLbl = new JLabel("Hub: ");
		add(hubLbl);

		JComboBox hubCombo = new JComboBox(new String[] {"Krautchan"});
		add(hubCombo);
		
		JLabel boardNameLbl = new JLabel("Board name:");
		add(boardNameLbl);
		final JTextField boardNameField = new JTextField("/b/");
		add(boardNameField); 
		
		JLabel emptyLbl = new JLabel();
		add(emptyLbl);
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
		add(okBtn);
		
		setSize(300, 200);
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private void validateBoardName(String boardName) throws InvalidBoardNameException {
		if(boardName.charAt(0) != '/' // must contain slash at beginning
			|| boardName.charAt(boardName.length()-1) != '/' // must contain slash at end
				|| boardName.substring(1, boardName.length()-2).contains("/") // must not contain slash in between
				|| !boardName.toLowerCase().equals(boardName)) // must not contain upper case
			throw new InvalidBoardNameException(boardName);
	}

	private class InvalidBoardNameException extends Exception {
		private final String boardName;

		public InvalidBoardNameException(String boardName) {
			super();
			this.boardName = boardName;
		}
		@Override
		public String getMessage() {
			return "Invalid board name: "+boardName
					+". Always use board names like \"/c/\" where c is at least one lowercase character!";
		}
	}
}
