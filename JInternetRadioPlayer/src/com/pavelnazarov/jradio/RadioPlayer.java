package com.pavelnazarov.jradio;

public interface RadioPlayer {
	RadioStation getCurrentRadioStation();
	boolean isPlaying();
	void play(RadioStation radioStation);
	void pause();
	void stop();
	void resume();
}
