package br.com.xavier.provider.content;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.pdf.signature.domain.AuthenticationData;
import br.com.xavier.util.StringValidator;

public abstract class ContentProvider {

	private URL url;
	private AuthenticationData authenticationData;
	
	protected ContentProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
		isValidProtocol(url);
		checkAuthenticationData(authenticationData);
		this.url = url;
		this.authenticationData = authenticationData;
	}
	
	//FIXME see better exception than malformedUrlException...
	private boolean isValidProtocol(URL url) throws MalformedURLException {
		if(url.getProtocol() != null && url.getProtocol().equals(getValidProtocol())){
			return true;
		}
		throw new MalformedURLException();
	}
	
	private void checkAuthenticationData(AuthenticationData authenticationData) throws InvalidCredentialsException{
		if(isAuthenticationRequired()){
			if(!StringValidator.validateString(authenticationData.getUserName()) 
			|| !StringValidator.validateString(authenticationData.getUserPassword())){
				throw new InvalidCredentialsException();
			}
		}
	}
	
	public abstract String getValidProtocol();
	public abstract boolean isAuthenticationRequired();

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public AuthenticationData getAuthenticationData() {
		return authenticationData;
	}

	public void setAuthenticationData(AuthenticationData authenticationData) {
		this.authenticationData = authenticationData;
	}
}