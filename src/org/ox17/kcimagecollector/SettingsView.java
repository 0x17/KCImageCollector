package org.ox17.kcimagecollector;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.*;

public class SettingsView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextField boardNameField;

	public SettingsView() {
		super("Settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JLabel topLbl = new JLabel("<html><div align=\"center\"><h1>KCImageCollector</h1>" + getRandomMsg() + "<br /><h3>Settings:</h3></div></html>");
		topLbl.setHorizontalAlignment(JLabel.CENTER);
		add(topLbl, BorderLayout.NORTH);

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
		
		setSize(300, 280);
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private String getRandomMsg() {
		final String[] randomMessages = new String[] {
				"Der Mensch ist eine <i>energetische Matrix</i>!",
				"<i>Muss</i> man wissen!",
				"Die <i>Strahlenwaffen</i> des Super Cyber Kellerdrachen!",
				"Abooooooooow!",
				"Bernd, stell dir vor, du schlägst jemanden so hart, <i>dass er zu einer Tür wird</i>.",
				"Vor einem Jahr hab ich mir für ca. 300 Euro die <i>Casio EX-S770</i> Kamera gekauft.",
				"Ich bin Bernd! Nein, nicht ein Bernd, der Bernd... <i>es gibt nur einen</i>!",
				"Georg Schnurer ist nicht nur <i>EXPERTE</i> in allen Computerbereichen sondern auch ein halbwegs etabliertes Mem im Krautkanal.",
				"Mensch Bernd. Ist das nicht der <i>Megahammer</i>? Du da und ich hier und wir beide trotzdem da?",
				"Pornos sind voll krank und ihr <i>Krautchanloser</i> seid es auch."
		};
		return randomMessages[new Random().nextInt(randomMessages.length)];
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
