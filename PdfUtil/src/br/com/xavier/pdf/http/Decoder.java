package br.com.xavier.pdf.http;

public interface Decoder {
	public abstract byte[] decodeData(byte[] encodedData) throws Exception;
}
