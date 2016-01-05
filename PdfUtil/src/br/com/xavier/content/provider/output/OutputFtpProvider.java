package br.com.xavier.content.provider.output;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.content.provider.OutputProvider;
import br.com.xavier.ftp.FTPUtil;
import br.com.xavier.pdf.signature.domain.AuthenticationData;

public class OutputFtpProvider extends OutputProvider {

	protected OutputFtpProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
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
	public boolean outputData(byte[] data) throws IOException {
		FTPUtil ftpClient = new FTPUtil(
			getUrl().getHost(), getUrl().getPort(),
			getAuthenticationData().getUserName(), 
			getAuthenticationData().getUserPassword(), 30
		);
		
		try {
			String filePath = getUrl().getPath();
			ftpClient.connect();
			ftpClient.login();
			ftpClient.uploadFile(filePath, data);
			return true;
		} catch(IOException e) {
			throw e;
		} finally {
			if(ftpClient != null && ftpClient.isConnected()){
				ftpClient.logout();
			}
		}
	}

}
