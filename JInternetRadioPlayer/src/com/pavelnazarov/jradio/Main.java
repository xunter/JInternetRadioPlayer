package com.pavelnazarov.jradio;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


public class Main {
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			System.out.println("Starting radio station...");
			String url = "http://scfire-ntc-aa02.stream.aol.com/stream/1002.asx";
			String urlDnB = "http://pub4.di.fm:80/di_drumandbass";

			final RadioStation rs = new RadioStationImpl(urlDnB);
			rs.getMetadataChangedEvent().addObserver(new Observer() {
				
				@Override
				public void update(Observable o, Object arg) {
					System.out.println("StreamTitle: " + rs.getStreamTitle());
				}
			});
			rs.startBroadcast();
			InputStream radioInputStream = rs.getInputStream();
			
			Thread playThread = new Thread(new PlayTask(radioInputStream));
			playThread.start();
						
			System.out.println("Runned.");
			
			playThread.join();
			playThread.interrupt();
			rs.shutdown();
			System.out.println("Stopped");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static class PlayTask implements Runnable {
		private final InputStream in;
		
		public PlayTask(InputStream in) {
			this.in = in;
		}
		
		public void run() {
			try {
				new Player(in).play();
			} catch (JavaLayerException e) {
				e.printStackTrace();
			}
		}
	}
}
