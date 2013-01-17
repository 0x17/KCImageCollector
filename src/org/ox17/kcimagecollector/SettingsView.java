package org.ox17.kcimagecollector;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("rawtypes")
public class SettingsView extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JComboBox boardCombo;
	private List<Board> boards;

	public SettingsView() throws Exception {
		super("Settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		boards = determineBoards();

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
					String boardName = boards.get(boardCombo.getSelectedIndex()).getSlashedName();
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

	private List<String> randomMessages;

	private String getRandomMsg() throws Exception {
		if(randomMessages == null) {
			randomMessages = new LinkedList<String>();
			FileReader fr = new FileReader("data/messages.txt");
			BufferedReader br = new BufferedReader(fr);
			String line;
			while(br.ready()) {
				line = br.readLine().replace("\n","");
				if(line.isEmpty()) continue;
				randomMessages.add(line);
			}
			br.close();
			fr.close();
		}
		return randomMessages.get(new Random().nextInt(randomMessages.size()));
	}

	private static class Board {
		public String name;
		public String description;
		public Board(String name, String description) {
			this.name = name;
			this.description = description;
		}
		@Override
		public String toString() {
			return "Board{" + "name='" + name + '\'' + ", description='" + description + '\'' + '}';
		}

		public String getSlashedName() {
			return "/"+name+"/";
		}
	}

	private static List<Board> determineBoards() throws Exception {
		List<Board> boards = new LinkedList<Board>();
		String navSrc = Helpers.loadUrlIntoStr("http://krautchan.net/nav");
		Pattern p = Pattern.compile("<li id=\"board_\\w+\" class=\"board_newposts\"><a href=\"/\\w+/\" target=\"main\">/(\\w+)/ - (.+?)</a></li>");
		Matcher m = p.matcher(navSrc);
		while(m.find()) {
			if(m.groupCount() == 2) {
				boards.add(new Board(m.group(1), m.group(2)));
			}
		}
		return boards;
	}

	@SuppressWarnings("unchecked")
	private JPanel initSettingsPanel() {
		JPanel settingsPanel = new JPanel(new GridLayout(2,2));
		JLabel hubLbl = new JLabel("Hub: ");
		settingsPanel.add(hubLbl);
		
		JComboBox hubCombo = new JComboBox(new String[] {"Krautchan"});
		settingsPanel.add(hubCombo);
		JLabel boardNameLbl = new JLabel("Board name:");
		boardCombo = new JComboBox();

		for(Board board : boards) {
			boardCombo.addItem("/" + board.name + "/ - " + board.description);
		}

		settingsPanel.add(boardNameLbl);
		settingsPanel.add(boardCombo);
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
