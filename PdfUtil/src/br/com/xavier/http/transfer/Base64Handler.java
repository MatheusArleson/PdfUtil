package br.com.xavier.http.transfer;

import org.apache.commons.codec.binary.Base64;

import br.com.xavier.http.Handler;

public class Base64Handler extends Handler {

	protected Base64Handler() {
		
	}
	
	@Override
	public byte[] encodeData(byte[] data) {
		return Base64.encodeBase64(data);
	}
	
	@Override
	public byte[] decodeData(byte[] encodedData) {
		return Base64.decodeBase64(encodedData);
	}
}
