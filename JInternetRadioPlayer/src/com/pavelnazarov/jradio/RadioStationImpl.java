package com.pavelnazarov.jradio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.net.SocketFactory;


public class RadioStationImpl implements RadioStation {
	private final String url;
	private PipedInputStream pipedInputStream;
	private PipedOutputStream pipedOutputStream;
	private volatile boolean alive;
	private final Object aliveLocker = new Object();
	private Thread thread;
	private final Observable metadataChangedEvent = new AlwaysObservable();
	private final Observable headersAvailableEvent = new AlwaysObservable();
	private String streamTitle;
	private Map<String, String> headers;
	
	
	public RadioStationImpl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	@Override
	public void startBroadcast() {
		synchronized (aliveLocker) {
			if (alive) {
				return;
			}
			alive = true;
		}
		
		RadioStationBroadcastWorker worker = this.new RadioStationBroadcastWorker();
		thread = new Thread(worker);
		thread.setDaemon(true);
		thread.start();

		try {
			pipedOutputStream = new PipedOutputStream();
			pipedInputStream = new PipedInputStream(pipedOutputStream);//1048576 - 1Mb
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
		
	class RadioStationBroadcastWorker implements Runnable {
		public void run() {
			Socket sock = null;
			RadioInputStream radioInputStream = null;
			try {
				try {
					URL urlAddress = new URL(url);					
					sock = openSocket(urlAddress);
					radioInputStream = new RadioInputStream(sock.getInputStream());
					final RadioInputStream radioInputStreamFinal = radioInputStream;
					sendHTTPPackageForStartTranslation(urlAddress, sock);
										
					
					radioInputStream.getMetadataChangedEvent().addObserver(new Observer() {
						
						@Override
						public void update(Observable o, Object arg) {
							streamTitle = radioInputStreamFinal.getStreamTitle();
							metadataChangedEvent.notifyObservers();
						}
					});
					
					radioInputStream.getHeadersAvailableEvent().addObserver(new Observer() {
						
						@Override
						public void update(Observable o, Object arg) {
							headers = radioInputStreamFinal.getHeaders();	
							headersAvailableEvent.notifyObservers();
						}
					});
					
					boolean headersAvailable = false;
					boolean metadataAvailable = false;
					Map<String, String> headers = null;
					Map<String, String> metadata = null;
					while (true) {
						if (thread.isInterrupted()) {
							break;
						}
						int available = radioInputStream.available();
						if (available > 0) {						
							//System.out.println("Available: "+ available);
							byte[] buffer = new byte[available];
							//System.out.println("Reading...");
							int readCount = radioInputStream.read(buffer, 0, available);
							//System.out.println("Read: "+ readCount);
							//System.out.println("Writing...");
							pipedOutputStream.write(buffer, 0, readCount);
						}
						Thread.yield();
					}
				} finally {
					radioInputStream.close();
					sock.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void shutdown() {
		synchronized (aliveLocker) {
			if (alive) {
				thread.interrupt();
				try {
					thread.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				alive = false;
				try {
					pipedOutputStream.close();
					pipedInputStream.close();
				} catch (IOException e) {
					System.out.println("During closing piped stream an error has occurred! Exception is "+e);
				}
			}
		}
	}
	
	private static Socket openSocket(URL url) throws IOException {			
		Socket socket = SocketFactory.getDefault().createSocket();
		InetAddress inetAddress = InetAddress.getByName(url.getHost());
		SocketAddress socketAddress = new InetSocketAddress(inetAddress, 80);
		socket.connect(socketAddress);
		return socket;			
	}
	
	private void sendHTTPPackageForStartTranslation(URL urlAddress, Socket socket) throws IOException {
		String httpPackage = buildHTTPPackage(urlAddress);
		Writer writer = new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"));
		writer.write(httpPackage);
		writer.flush();		
	}
	
	private static String buildHTTPPackage(URL url) {
		StringBuilder sb = new StringBuilder();
		sb.append("GET ");
		sb.append(url.getPath());
		sb.append(" HTTP/1.1");
		sb.append("\n");
		sb.append("Icy-MetaData:1\n\n");
		return sb.toString();
	}

	public InputStream getInputStream() {		
		return pipedInputStream;
	}

	public Observable getMetadataChangedEvent() {
		return metadataChangedEvent;
	}

	@Override
	public String getStreamTitle() {
		return streamTitle;
	}
	
	public Observable getHeadersAvailableEvent() {
		return headersAvailableEvent;
	}
	
	public String getName() {
		return headers.get("icy-name");
	}
}
