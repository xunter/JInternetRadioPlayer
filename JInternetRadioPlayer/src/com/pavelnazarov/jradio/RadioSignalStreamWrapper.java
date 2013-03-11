package com.pavelnazarov.jradio;

import java.io.InputStream;

public class RadioSignalStreamWrapper implements RadioSignal {
	private final InputStream inputStream;
	
	public RadioSignalStreamWrapper(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	
	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return inputStream;
	}

}
