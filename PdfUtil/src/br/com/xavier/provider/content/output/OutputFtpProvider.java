package br.com.xavier.provider.content.output;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.ftp.FTPUtil;
import br.com.xavier.pdf.signature.domain.AuthenticationData;
import br.com.xavier.provider.content.OutputProvider;

public class OutputFtpProvider extends OutputProvider {
	
	private int timeOutSeconds;
	private boolean connectWithPassiveMode;

	protected OutputFtpProvider(
		URL url, AuthenticationData authenticationData, int timeOutSeconds, boolean connectWithPassiveMode
	) throws MalformedURLException, InvalidCredentialsException {
		super(url, authenticationData);
		this.timeOutSeconds = timeOutSeconds;
		this.connectWithPassiveMode = connectWithPassiveMode;
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
			getAuthenticationData().getUserPassword(), 
			timeOutSeconds, connectWithPassiveMode
		);
		
		try {
			String filePath = getUrl().getPath();
			ftpClient.uploadFile(filePath, data);
			return true;
		} catch(IOException e) {
			throw e;
		}
	}
}
