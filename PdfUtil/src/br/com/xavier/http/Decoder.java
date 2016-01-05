package br.com.xavier.http;

public interface Decoder {
	public abstract byte[] decodeData(byte[] encodedData) throws Exception;
}
