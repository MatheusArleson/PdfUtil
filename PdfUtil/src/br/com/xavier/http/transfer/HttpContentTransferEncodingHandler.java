package br.com.xavier.http.transfer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HttpContentTransferEncodingHandler {

	private Base64Handler base64Handler;
	
	public HttpContentTransferEncodingHandler() {
		this.base64Handler = new Base64Handler();
	}

	public byte[] encodeData(byte[] data, List<String> encodeOrder) {
		for (String encoding : encodeOrder) {
			switch (encoding) {
			case "base64":
				data = base64Handler.encodeData(data);
				break;
			}
		}
		return data;
	}
	
	public byte[] decodeData(byte[] data, String contentEncodingHeaderValue) {
		List<String> encodingsList = Arrays.asList(contentEncodingHeaderValue.split(","));
		Collections.reverse(encodingsList);
		return decodeData(data, encodingsList);
	}

	public byte[] decodeData(byte[] data, List<String> encodeOrder) {
		for (String encoding : encodeOrder) {
			switch (encoding) {
			case "base64":
				data = base64Handler.decodeData(data);
				break;
			}
		}
		return data;
	}
}
