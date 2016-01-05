package br.com.xavier.provider.content.output;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import br.com.xavier.pdf.signature.domain.AuthenticationData;
import br.com.xavier.provider.content.OutputProvider;

public class OutputHttpProvider extends OutputProvider {

	protected OutputHttpProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
		super(url, authenticationData);
	}
	
	@Override
	public String getValidProtocol() {
		return new String("http");
	}
	
	@Override
	public boolean isAuthenticationRequired() {
		return false;
	}

	@Override
	public boolean outputData(byte[] data) throws URISyntaxException, IOException {
		CloseableHttpClient client = null;
		
		try {
			byte[] base64EncodedData = Base64.encodeBase64(data);
			ByteArrayEntity bae = new ByteArrayEntity(base64EncodedData);
			
			client = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(getUrl().toURI());
			httpPost.setEntity(bae);
			httpPost.setHeader("Content-Transfer-Encoding", "base64");
			
			HttpResponse httpPostResponse = client.execute(httpPost);
			int httpGetResponseStatusCode = httpPostResponse.getStatusLine().getStatusCode();
			
			if (httpGetResponseStatusCode == HttpStatus.SC_OK) {
				return true;
			} else {
				throw new IOException("SERVER RETURNED STATUS CODE " + httpGetResponseStatusCode);
			}
		} catch (URISyntaxException e) {
			throw e;
		} catch(ClientProtocolException e) {
			throw e;
		} catch(IOException e) {
			throw e;
		} finally {
			if(client != null){
				client.close();
			}
		}
	}
}
