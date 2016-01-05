package br.com.xavier.provider.content.output;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.pdf.signature.domain.AuthenticationData;
import br.com.xavier.provider.content.OutputProvider;

public class OutputProviderFactory {
	
	private OutputProviderFactory(){}
	
	public static OutputProvider getOutputProvider(URL url) throws MalformedURLException, InvalidCredentialsException{
		if(url == null || url.getProtocol() == null || url.getProtocol().trim().isEmpty()){
			throw new MalformedURLException("BAD URL. NO PROTOCOL.");
		}
				
		String userInfo = url.getUserInfo();
		if(userInfo != null && userInfo.contains(":")){
			String[] userData = userInfo.split(Pattern.quote(":"));
			String userName = userData[0];
			String userPassword = userData[1];
			return getOutputProvider(url, userName, userPassword);
		} else {
			return getOutputProvider(url, null, null);
		}
	}
	
	public static OutputProvider getOutputProvider(URL url, String userName, String userPassword) throws MalformedURLException, InvalidCredentialsException{
		if(url == null || url.getProtocol() == null || url.getProtocol().trim().isEmpty()){
			throw new MalformedURLException("BAD URL. NO PROTOCOL.");
		}
		
		AuthenticationData ad = new AuthenticationData(userName, userPassword);
		
		String protocol = url.getProtocol();
		switch (protocol) {
		case "http":
			return new OutputHttpProvider(url, ad);
		case "file":
			return new OutputFileProvider(url, ad);
		case "ftp":
			return new OutputFtpProvider(url, ad, 30, true);
		}
		
		throw new MalformedURLException("NO PROTOCOL SUPPORT FOR : " + protocol);
	}

}