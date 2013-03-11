package com.pavelnazarov.jradio;

public class BasicRadioPlayer implements RadioPlayer {
	private RadioStation rs;
	private Thread playThread;
	private boolean playing;	
	
	public BasicRadioPlayer() {
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	@Override
	public RadioStation getCurrentRadioStation() {
		return rs;
	}

	@Override
	public void play(RadioStation radioStation) {
		this.rs = radioStation;		
		playing = true;
		playThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				playWorker();
			}
		});
		playThread.setDaemon(true);
		playThread.start();
	}
	
	protected void playWorker() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void pause() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop() {		
		playing = false;
		playThread.interrupt();
	}

	@Override
	public void resume() {
		throw new UnsupportedOperationException();
	}
}
