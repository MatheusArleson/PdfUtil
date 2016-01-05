package br.com.xavier.http;

import java.io.IOException;

public interface Encoder {
	public abstract byte[] encodeData(byte[] data) throws IOException;
}
