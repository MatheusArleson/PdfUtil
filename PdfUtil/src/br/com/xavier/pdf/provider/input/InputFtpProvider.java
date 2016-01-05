package br.com.xavier.pdf.provider.input;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.ftp.FTPUtil;
import br.com.xavier.pdf.pdf.signature.domain.AuthenticationData;
import br.com.xavier.pdf.provider.InputProvider;

public class InputFtpProvider extends InputProvider {

	protected InputFtpProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
		super(url, authenticationData);
	}
	
	@Override
	public String getValidProtocol() {
		return new String("ftp");
	}
	
	@Override
	public boolean isAuthenticationRequired() {
		return true;
	}
	
	@Override
	public byte[] provideData() throws IOException {
		
		FTPUtil ftpClient = new FTPUtil(
			getUrl().getHost(), getUrl().getPort(),
			getAuthenticationData().getUserName(), 
			getAuthenticationData().getUserPassword(), 30
		);
		
		try {
			String filePath = getUrl().getPath();
			ftpClient.connect();
			ftpClient.login();
			byte[] data = ftpClient.downloadFile(filePath);
			return data;
		} catch(IOException e) {
			throw e;
		} finally {
			if(ftpClient != null && ftpClient.isConnected()){
				ftpClient.logout();
			}
		}
		
	}
}
