package br.com.xavier.pdf.http.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import br.com.xavier.pdf.http.Handler;

public class DeflateHandler extends Handler {
	
	protected DeflateHandler() {
	}
	
	@Override
	public byte[] encodeData(byte[] data) throws IOException {
		
		DeflaterOutputStream dos = null;
		ByteArrayOutputStream baos = null;
		
		try {
			baos = new ByteArrayOutputStream();
			dos = new DeflaterOutputStream(baos);
			
			dos.write(data);
			dos.flush();
			dos.close();
			
			baos.flush();
			baos.close();
			
			return baos.toByteArray();
		} catch(IOException e) {
			throw e;
		}
	}

	@Override
	public byte[] decodeData(byte[] encodedData) throws IOException {
		
		InflaterInputStream ios = null;
		ByteArrayInputStream bais = null;
		ByteArrayOutputStream baos = null;
		
		try {
			bais = new ByteArrayInputStream(encodedData);
			baos = new ByteArrayOutputStream();
			
			ios = new InflaterInputStream(bais);
			
			int data;
			while ((data = ios.read()) != -1) {
				baos.write(data);
				baos.flush();
			}
			
			ios.close();
			bais.close();
			
			baos.flush();
			baos.close();
			
			return baos.toByteArray();
		} catch(IOException e) {
			throw e;
		}
	}
}
