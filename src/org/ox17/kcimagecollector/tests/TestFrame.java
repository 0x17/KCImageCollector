package org.ox17.kcimagecollector.tests;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ox17.kcimagecollector.Helpers;
import org.ox17.kcimagecollector.ImagePanel;

@SuppressWarnings("serial")
public class TestFrame extends JFrame {
	
	public static void main(String[] args) throws Exception {
		new TestFrame().setVisible(true);
	}
	
	protected static final String IMG_URL = "http://krautchan.net/files/1356871178001.jpg";
	private ImagePanel ip;
	private Image preloadedImg;
	
	public TestFrame() throws Exception {
		super("TestFrame");
		setLayout(new BorderLayout());
		setSize(1024, 768);
		ip = new ImagePanel();
		add(ip, BorderLayout.CENTER);
		
		JPanel lowerPane = new JPanel(new FlowLayout());		
		add(lowerPane, BorderLayout.SOUTH);
		
		JButton loadAndShowBtn = new JButton("Load&Show");
		loadAndShowBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ip.setToImageFromLink(IMG_URL);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		lowerPane.add(loadAndShowBtn);
		
		JButton showImgBtn = new JButton("Show");
		showImgBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(preloadedImg != null) {
					ip.setToImage(preloadedImg);
				}
			}
		});
		lowerPane.add(showImgBtn);
		
		JButton preloadImgBtn = new JButton("Preload");
		preloadImgBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					preload();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		lowerPane.add(preloadImgBtn);
	}
	
	private void preload() throws Exception {
		System.out.println("Preload starting...");
		MediaTracker mt = new MediaTracker(ip);
		preloadedImg = Helpers.imgFromUrl(IMG_URL);
		mt.addImage(preloadedImg, 0);
		Toolkit.getDefaultToolkit().prepareImage(preloadedImg, preloadedImg.getWidth(this), preloadedImg.getHeight(this), this);
		mt.waitForAll();
		System.out.println("Preload done!");
	}
}