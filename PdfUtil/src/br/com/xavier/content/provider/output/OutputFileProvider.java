package br.com.xavier.content.provider.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.content.provider.OutputProvider;
import br.com.xavier.pdf.signature.domain.AuthenticationData;

public class OutputFileProvider extends OutputProvider {

	private File file;

	public String getFilePath() {
		return file.getAbsolutePath();
	}
	
	protected OutputFileProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
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
	public boolean outputData(byte[] data) throws URISyntaxException, IOException {
		File f = new File(getUrl().toURI());
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.flush();
		fos.close();
		return true;
	}
}
