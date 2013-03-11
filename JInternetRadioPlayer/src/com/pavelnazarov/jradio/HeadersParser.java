package com.pavelnazarov.jradio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadersParser {
	private final Pattern pattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9-]+):\\s?(.+)$", Pattern.MULTILINE); 
	
	/**
	 * Parse (convert) byte array to String
	 * @param bytes array
	 * @return headers string
	 * */
	public String parseToString(byte[] bytes) throws IOException {
		ByteArrayInputStream bytesInputStream = null;
		try {
			bytesInputStream = new ByteArrayInputStream(bytes);
			Reader reader = new InputStreamReader(bytesInputStream, Charset.forName("UTF-8"));
			CharBuffer charBuffer = CharBuffer.allocate(bytes.length);
			reader.read(charBuffer);
			charBuffer.rewind();
			String headersText = charBuffer.toString();
			return headersText;
		} finally {
			bytesInputStream.close();			
		}
	}
	
	/**
	 * parse headers text to map
	 * @param headersText
	 * @return map
	 * */
	public Map<String, String> parse(String headersText) {
		Matcher matcher = pattern.matcher(headersText);
		Map<String, String> map = new HashMap<String, String>();			
		while (matcher.find() == true) {
			map.put(matcher.group(1), matcher.group(2));
		}		
		return map;
	}
}
