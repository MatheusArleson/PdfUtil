package br.com.xavier.provider.content.input;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.ftp.FTPUtil;
import br.com.xavier.pdf.signature.domain.AuthenticationData;
import br.com.xavier.provider.content.InputProvider;

public class InputFtpProvider extends InputProvider {

	private int timeOutSeconds;
	private boolean connectWithPassiveMode;
	
	protected InputFtpProvider(
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
	public byte[] provideData() throws IOException {
		
		FTPUtil ftpClient = new FTPUtil(
			getUrl().getHost(), getUrl().getPort(),
			getAuthenticationData().getUserName(), 
			getAuthenticationData().getUserPassword(),
			timeOutSeconds, connectWithPassiveMode
		);
		
		try {
			String filePath = getUrl().getPath();
			byte[] data = ftpClient.downloadFile(filePath);
			return data;
		} catch(IOException e) {
			throw e;
		}		
	}
}
