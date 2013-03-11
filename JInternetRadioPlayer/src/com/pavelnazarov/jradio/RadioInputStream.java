package com.pavelnazarov.jradio;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadioInputStream extends FilterInputStream {
	
	private final Observable metadataChangedEvent = new AlwaysObservable();
	private final Observable headersAvailableEvent = new AlwaysObservable();
	
	private boolean isHeadersAvailable;
	private Map<String, String> headers;
	private Map<String, String> metaData;
	private int bitRate;
	private int metaRate;
	private boolean isFirstAccess;
	private int metaint;
	private int metadataCounter;
	private boolean useMetadata;
	
	private String streamTitle;
	private String streamUrl;
	private final String METADATA_REGULAR_EXPRESSION = "StreamTitle='(.*)';StreamUrl='(.*)'";
	private Pattern metadataPattern = Pattern.compile(METADATA_REGULAR_EXPRESSION);
	
	private final Object readHeadersTaskLocker = new Object();
	
	private int state;
	
	private int lengthToMetaData;
	
	public int getBitRate() {
		return bitRate;
	}
	
	public String getStreamTitle() {
		return streamTitle;
	}
			
	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, String> getMetaData() {
		return metaData;
	}
	
	@Override
	public int available() throws IOException {
		if (state == 1) {
			return 0;
		} else {
			return in.available();
		}
	}
	
	@Override
	public int read() throws IOException {	
		boolean headersRead = false;
		if (!isHeadersAvailable) {
			state = 1;
			readHeaders();
			
			isHeadersAvailable = true;
			isFirstAccess = false;
			state = 0;
			
			String metaintText = headers.get("icy-metaint");
			useMetadata = metaintText != null;
			if (useMetadata) {
				int metaint = Integer.parseInt(metaintText);
				this.metaint = metaint;			
			}			
			
			String brText = headers.get("icy-br");
			if (brText != null) {
				bitRate = Integer.parseInt(brText);
			}

			headersAvailableEvent.notifyObservers();
			
			headersRead = true;
		}
		if (useMetadata) {
			if (this.metadataCounter == this.metaint) {
				this.readMetadata();
				this.metadataCounter = 0;
			}
			this.metadataCounter++;
		}
		return in.read();
	}
	
	protected void readHeaders() throws IOException {
		HeadersBuffer headersBuffer = new HeadersBuffer();
		headersBuffer.fillBuffer(in);
		HeadersParser parser = new HeadersParser();
		String headersText = parser.parseToString(headersBuffer.getHeadersInByteArray());
		System.out.println("Headers: "+headersText);
		headers = parser.parse(headersText);
	}
	
	protected void readMetadata() throws IOException {
		int firstByte = in.read();
		if (firstByte == 0) return;
		int metaLen = firstByte * 16;
		byte[] buffer = new byte[metaLen];
		int readCount = 0;
		int readCountAcc = readCount;
		int needBytesCount = metaLen;
		while ((needBytesCount = metaLen - readCountAcc) > 0) {
			readCount = in.read(buffer, readCountAcc, needBytesCount);			
			readCountAcc += readCount;
		}
		
		String metadataText = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer), Charset.forName("UTF-8")));
			CharBuffer charBuffer = CharBuffer.allocate(metaLen);
			reader.read(charBuffer);
			charBuffer.rewind();
			metadataText = charBuffer.toString();
		} finally {
			reader.close();
		}
				
		//System.out.println("Metadata Length: " + metaLen);	
		//System.out.println("Metadata Text: " + metadataText);

		Matcher matcher = metadataPattern.matcher(metadataText);
		matcher.find();
		String newStreamTitle = matcher.group(1);
		String newStreamUrl = matcher.group(2);

		boolean metadataChanged = false;
		
		if (!newStreamTitle.equals(streamTitle)) {
			metadataChanged = true;
			streamTitle = newStreamTitle;
		}

		if (!newStreamUrl.equals(streamUrl)) {
			metadataChanged = true;
			streamUrl = newStreamUrl;
		}
		
		if (metadataChanged) {
			metadataChangedEvent.notifyObservers();
		}		
	}
		
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int counter = -1;
		for (int i = off; i < len; i++) {
			int bt = this.read();
			if (bt == -1) break;
			b[i] = (byte)bt;
			if (counter == -1) {
				counter = 0;
			}
			counter++;
		}
		return counter;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length); 
	}
	
	public RadioInputStream(InputStream inputStream) {
		super(inputStream);
		isFirstAccess = true;
	}

	public Observable getMetadataChangedEvent() {
		return metadataChangedEvent;
	}

	public Observable getHeadersAvailableEvent() {
		return headersAvailableEvent;
	}

}
