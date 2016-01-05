package br.com.xavier.provider.content.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.AccessDeniedException;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.pdf.signature.domain.AuthenticationData;
import br.com.xavier.provider.content.InputProvider;

public class InputFileProvider extends InputProvider {
	
	private File file;

	public String getFilePath() {
		return file.getAbsolutePath();
	}

	protected InputFileProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
		super(url, authenticationData);
	}

	@Override
	public String getValidProtocol() {
		return new String("file");
	}
	
	@Override
	public boolean isAuthenticationRequired() {
		return false;
	}
	
	@Override
	public byte[] provideData() throws URISyntaxException, FileNotFoundException, AccessDeniedException, IOException {
		
		file = new File(getUrl().toURI());
		
		if(!file.exists()){
			throw new FileNotFoundException();
		}
		
		if(!file.canRead()){
			throw new AccessDeniedException(file.getAbsolutePath());
		}
		
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(file);
			byte[] buffer = new byte[fis.available()];
			
			fis.read(buffer);
			fis.close();
			
			return buffer;
		} catch(IOException e){
			throw e;
		} finally {
			if(fis != null){
				IOUtils.closeQuietly(fis);
			}
		}
	}
}
