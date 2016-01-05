package br.com.xavier.provider.content;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.InvalidCredentialsException;

import br.com.xavier.pdf.signature.domain.AuthenticationData;

public abstract class OutputProvider extends ContentProvider {
	
	public OutputProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
		super(url, authenticationData);
	}

	public abstract boolean outputData(byte[] data) throws Exception;
}
