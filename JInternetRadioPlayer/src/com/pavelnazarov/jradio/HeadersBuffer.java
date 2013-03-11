package com.pavelnazarov.jradio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class HeadersBuffer {
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	
	private int state;
	
	public HeadersBuffer() {
		state = 0x0;
	}
	
	public boolean isAvailable() {
		return state == 0xf;
	}
	
	public byte[] getHeadersInByteArray() {
		return outputStream.toByteArray();
	}
	
	public void fillBuffer(InputStream in) throws IOException {
		boolean newlinePrev = false;
		boolean endHeaders = false;
		while (!endHeaders) {
			int b = in.read();
			boolean isNChar = b == (byte)'\n';
			boolean isRChar = b == (byte)'\r';
			if (newlinePrev) {
				if (isNChar) {
					endHeaders = true;
				} else {
					if (!isRChar) {
						newlinePrev = false;
					}
				}
			} else {
				if (isNChar) {
					newlinePrev = true;
				}
			}
			outputStream.write(b);
		}
		state = 0xf;
	}
}
