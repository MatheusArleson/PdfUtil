package br.com.xavier.pdf.provider;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.pdf.pdf.signature.domain.AuthenticationData;

public abstract class InputProvider extends Provider {
	
	public InputProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
		super(url, authenticationData);
	}

	public abstract byte[] provideData() throws Exception;
}