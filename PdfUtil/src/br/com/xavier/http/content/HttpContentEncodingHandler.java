package br.com.xavier.http.content;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;

public class HttpContentEncodingHandler {

	private DeflateHandler deflateHandler;
	private GzipHandler gzipHandler;
	
	public HttpContentEncodingHandler() {
		this.deflateHandler = new DeflateHandler();
		this.gzipHandler = new GzipHandler();
	}

	public byte[] encodeData(byte[] data, List<String> encodeOrder) throws DataFormatException, IOException {
		for (String encoding : encodeOrder) {
			switch (encoding) {
			case "gzip":
				data = deflateHandler.encodeData(data);
				break;
			case "deflate":
				data = gzipHandler.encodeData(data); 
				break;
			}
		}
		return data;
	}
	
	public byte[] decodeData(byte[] data, String contentEncodingHeaderValue) throws DataFormatException, IOException {
		List<String> encodingsList = Arrays.asList(contentEncodingHeaderValue.split(","));
		Collections.reverse(encodingsList);
		return decodeData(data, encodingsList);
	}

	public byte[] decodeData(byte[] data, List<String> encodeOrder) throws DataFormatException, IOException {
		for (String encoding : encodeOrder) {
			switch (encoding) {
			case "gzip":
				data = deflateHandler.decodeData(data);
				break;
			case "deflate":
				data = gzipHandler.decodeData(data); 
				break;
			}
		}
		return data;
	}
}
