package br.com.xavier.pdf.http.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import br.com.xavier.pdf.http.Handler;

public class GzipHandler extends Handler {
	
	protected GzipHandler() {
	}

	@Override
	public byte[] encodeData(byte[] data) throws IOException {
		ByteArrayInputStream bais = null;
		GZIPOutputStream gzos = null;
		ByteArrayOutputStream baos = null;

		try {
			bais = new ByteArrayInputStream(data);
			gzos = new GZIPOutputStream(baos);
			baos = new ByteArrayOutputStream();
			IOUtils.copy(bais, gzos);
			
			baos.flush();
			return baos.toByteArray();
		} catch(IOException e) {
			throw e;
		} finally {
			if(baos != null){
				baos.close();
			}
			
			if(gzos != null){
				gzos.close();
			}
			
			if(bais != null){
				bais.close();
			}
		}
	}
	
	@Override
	public byte[] decodeData(byte[] encodedData) throws IOException {
		
		ByteArrayInputStream bais = null;
		GZIPInputStream gzis = null;
		ByteArrayOutputStream baos = null;
		
		try {
			bais = new ByteArrayInputStream(encodedData);
			gzis = new GZIPInputStream(bais);
			baos = new ByteArrayOutputStream();
			IOUtils.copy(gzis, baos);
			
			baos.flush();
			return baos.toByteArray();
		} catch(IOException e) {
			throw e;
		} finally {
			if(baos != null){
				baos.close();
			}
			
			if(gzis != null){
				gzis.close();
			}
			
			if(bais != null){
				bais.close();
			}
		}
	}
}
