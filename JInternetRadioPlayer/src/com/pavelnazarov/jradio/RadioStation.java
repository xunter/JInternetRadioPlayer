package com.pavelnazarov.jradio;

import java.io.InputStream;
import java.util.Observable;

public interface RadioStation {
	String getUrl();
	InputStream getInputStream();
	void startBroadcast();
	void shutdown();
	Observable getMetadataChangedEvent();
	Observable getHeadersAvailableEvent();
	String getStreamTitle();
	String getName();
}
