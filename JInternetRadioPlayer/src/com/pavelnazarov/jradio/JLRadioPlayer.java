package com.pavelnazarov.jradio;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class JLRadioPlayer extends BasicRadioPlayer {
	private JLPlayerInputStreamWrapper jlPlayerInputStream;
	
	class JLPlayerInputStreamWrapper extends FilterInputStream {
		protected JLPlayerInputStreamWrapper(InputStream in) {
			super(in);
		}

		private volatile boolean stopWorking;
		
		public void stopWorking() {
			stopWorking = true;
		}
		
		@Override
		public int read() throws IOException {
			if (stopWorking) {
				return -1;
			}
			return super.read();
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (stopWorking) {
				return 0;
			}
			return super.read(b, off, len);
		}
		
		@Override
		public int available() throws IOException {
			if (stopWorking) {
				return 0;
			}
			return super.available();
		}
	}
	
	@Override
	protected void playWorker() {
		try {
			JLPlayerInputStreamWrapper jlInputStream = this.new JLPlayerInputStreamWrapper(getCurrentRadioStation().getInputStream());
			jlPlayerInputStream = jlInputStream;
			Player player = new Player(jlInputStream);
			player.play();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {
		jlPlayerInputStream.stopWorking();
		super.stop();
	}
}
