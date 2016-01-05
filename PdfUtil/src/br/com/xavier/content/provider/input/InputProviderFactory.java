package br.com.xavier.content.provider.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.content.provider.InputProvider;
import br.com.xavier.pdf.signature.domain.AuthenticationData;

public class InputProviderFactory {
	
	private InputProviderFactory(){}
	
	public static InputProvider getInputProvider(URL url) throws MalformedURLException, InvalidCredentialsException{
		if(url == null || url.getProtocol() == null || url.getProtocol().trim().isEmpty()){
			throw new MalformedURLException("BAD URL. NO PROTOCOL.");
		}
				
		String userInfo = url.getUserInfo();
		if(userInfo != null && userInfo.contains(":")){
			String[] userData = userInfo.split(Pattern.quote(":"));
			String userName = userData[0];
			String userPassword = userData[1];
			return getInputProvider(url, userName, userPassword);
		} else {
			return getInputProvider(url, null, null);
		}
	}
	
	public static InputProvider getInputProvider(URL url, String userName, String userPassword) throws MalformedURLException, InvalidCredentialsException{
		if(url == null || url.getProtocol() == null || url.getProtocol().trim().isEmpty()){
			throw new MalformedURLException("BAD URL. NO PROTOCOL.");
		}
		
		AuthenticationData ad = new AuthenticationData(userName, userPassword);
		
		String protocol = url.getProtocol();
		switch (protocol) {
		case "http":
			return new InputHttpProvider(url, ad);
		case "file":
			return new InputFileProvider(url, ad);
		case "ftp":
			return new InputFtpProvider(url, ad);
		}
		
		throw new MalformedURLException("NO PROTOCOL SUPPORT FOR : " + protocol);
	}

}