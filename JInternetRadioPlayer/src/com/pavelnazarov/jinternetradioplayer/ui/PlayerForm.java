package com.pavelnazarov.jinternetradioplayer.ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.pavelnazarov.jradio.JLRadioPlayer;
import com.pavelnazarov.jradio.RadioPlayer;
import com.pavelnazarov.jradio.RadioStation;
import com.pavelnazarov.jradio.RadioStationImpl;

public class PlayerForm extends JFrame {
	
	protected final JMenuBar menuBar = new JMenuBar();
	protected final JMenu fileMenu = new JMenu("File");
	protected final JMenuItem exitMenuItem = new JMenuItem("Exit");

	protected final JMenu playerMenu = new JMenu("Player");
	protected final JMenuItem playMenuItem = new JMenuItem("Play");
	protected final JMenuItem stopMenuItem = new JMenuItem("Stop");
	
	protected final JMenu helpMenu = new JMenu("?");
	protected final JMenuItem aboutMenuItem = new JMenuItem("About");
	
	
	protected final JTextField urlTextField = new JTextField();
	protected final JLabel stationLabel = new JLabel();
	protected final JLabel streamTitleLabel = new JLabel();
	protected final JLabel stationNameLabel = new JLabel();
	
	private RadioPlayer radioPlayer;
	private RadioStation radioStation;
	
	public PlayerForm(RadioPlayer radioPlayer) {
		this.radioPlayer = radioPlayer;
		setSize(500, 300);

		Container contentPane = getContentPane();
		GridBagLayout gbl = new GridBagLayout();
		contentPane.setLayout(gbl);
		contentPane.add(new JLabel("URL"), new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.BASELINE, new Insets(5,5,5,5), 0, 0));
		contentPane.add(urlTextField, new GridBagConstraints(1,0,1,1,1,1,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5), 0, 0));
		
		contentPane.add(new JLabel("Name: "), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.BASELINE, new Insets(5,5,5,5), 0, 0));
		contentPane.add(stationNameLabel, new GridBagConstraints(1,1,1,1,1,1,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5), 0, 0));
		
		contentPane.add(new JLabel("Title: "), new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.BASELINE, new Insets(5,5,5,5), 0, 0));
		contentPane.add(streamTitleLabel, new GridBagConstraints(1,2,1,1,1,1,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5), 0, 0));
		
		urlTextField.setText("http://pub4.di.fm:80/di_drumandbass");
		
		initMenuBar();
		this.pack();
		this.setMinimumSize(this.getSize());
		setSize(800,150);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
	
	protected String getRadioStationUrl() {
		return urlTextField.getText();
	}
	
	protected void initMenuBar() {
		menuBar.add(fileMenu);
		menuBar.add(playerMenu);
		menuBar.add(helpMenu);
		fileMenu.add(exitMenuItem);
		playerMenu.add(playMenuItem);
		playerMenu.add(stopMenuItem);
		stopMenuItem.setEnabled(false);
		helpMenu.add(aboutMenuItem);
		
		exitMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (radioPlayer != null) {
					radioPlayer.stop();					
				}
				if (radioStation != null) {
					radioStation.shutdown();
				}
				setVisible(false);
				
			}
		});
		
		playMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String stationUrl = getRadioStationUrl();
				if (radioStation != null) {
					radioStation.shutdown();
				}
				radioStation = new RadioStationImpl(stationUrl);
				initRadioStation(radioStation);
				radioStation.startBroadcast();
				radioPlayer.play(radioStation);
				playMenuItem.setEnabled(false);
				stopMenuItem.setEnabled(true);
			}
		});
		
		stopMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				radioPlayer.stop();
				stopMenuItem.setEnabled(false);
				playMenuItem.setEnabled(true);
			}
		});
		
		aboutMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Created by Pavel Nazarov www.pavelnazarov.com as UI Swing Internet Radio Player 2013-03-10.");
				
			}
		});
		
		setJMenuBar(menuBar);
	}
	
	protected void setPlayerTitle(String streamTitle) {
		setTitle(streamTitle);
		streamTitleLabel.setText(streamTitle);
	}
	
	protected void setRadioStationName(String name) {
		stationNameLabel.setText(name);
	}
	
	protected void initRadioStation(RadioStation radioStation) {
		final RadioStation finalRS = radioStation;
		finalRS.getMetadataChangedEvent().addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				String streamTitle = finalRS.getStreamTitle();
				setPlayerTitle(streamTitle);				
			}
		});
		radioStation.getHeadersAvailableEvent().addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				String stationName = finalRS.getName();
				setRadioStationName(stationName);
			}
		});
	}
}
