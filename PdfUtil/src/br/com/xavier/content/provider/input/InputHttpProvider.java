package br.com.xavier.content.provider.input;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.DataFormatException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import br.com.xavier.content.provider.InputProvider;
import br.com.xavier.http.content.HttpContentEncodingHandler;
import br.com.xavier.http.transfer.HttpContentTransferEncodingHandler;
import br.com.xavier.pdf.signature.domain.AuthenticationData;

public class InputHttpProvider extends InputProvider {
	
	private static final String contentCodificationHeader = new String("Content-Encoding");
	private static final String contentTransferCodificationHeader = new String("Content-Transfer-Encoding");

	private HttpContentEncodingHandler contentEncodingHandler;
	private HttpContentTransferEncodingHandler contentTransferEncodingHandler;
	
	protected InputHttpProvider(URL url, AuthenticationData authenticationData) throws MalformedURLException, InvalidCredentialsException {
		super(url, authenticationData);
		this.contentEncodingHandler = new HttpContentEncodingHandler();
		this.contentTransferEncodingHandler = new HttpContentTransferEncodingHandler();
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
	public byte[] provideData() throws URISyntaxException, ClientProtocolException, IOException, DataFormatException {
		CloseableHttpClient client = null;
		InputStream responseInputStream = null;
		ByteArrayOutputStream baos = null;
		
		try {
			client = HttpClientBuilder.create().build();
			HttpGet httpGet = new HttpGet(getUrl().toURI());
			HttpResponse httpGetResponse = client.execute(httpGet);
			
			HttpEntity httpGetEntity = httpGetResponse.getEntity();
			int httpGetResponseStatusCode = httpGetResponse.getStatusLine().getStatusCode();
			
			if (httpGetEntity != null && httpGetResponseStatusCode == HttpStatus.SC_OK) {
				responseInputStream = httpGetEntity.getContent();
				baos = new ByteArrayOutputStream();
				IOUtils.copy(responseInputStream, baos);
				baos.flush();
				byte[] content = baos.toByteArray();
				
				//FIXME ver qual a ordem de decodificacao
				if(isResponseContentEncoded(httpGetResponse)){
					return decodeResponseContent(content, getResponseContentEncodingHeaderValue(httpGetResponse));
				}
				
				if(isResponseTransferEncoded(httpGetResponse)) {
					return decodeResponseTransferContent(content, getResponseContentTransferEncodingHeaderValue(httpGetResponse));
				} else {
					return content;
				}
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
			if(baos != null){
				IOUtils.closeQuietly(baos);
			}
			
			if(responseInputStream != null){
				IOUtils.closeQuietly(responseInputStream);
			}
			
			if(client != null){
				client.close();
			}
		}
	}
	
	private boolean isResponseContentEncoded(HttpResponse response){
		Header encodingHeader = response.getFirstHeader(contentCodificationHeader);
		if(encodingHeader != null && encodingHeader.getValue() != null && !encodingHeader.getValue().trim().isEmpty()){
			return true;
		}
		return false;
	}
	
	private boolean isResponseTransferEncoded(HttpResponse response){
		Header transferEncodingHeader = response.getFirstHeader(contentTransferCodificationHeader);
		if(transferEncodingHeader != null && transferEncodingHeader.getValue() != null && !transferEncodingHeader.getValue().trim().isEmpty()){
			return true;
		}
		return false;
	}
	
	private String getResponseContentEncodingHeaderValue(HttpResponse response){
		return response.getFirstHeader(contentCodificationHeader).getValue();
	}
	
	private String getResponseContentTransferEncodingHeaderValue(HttpResponse response){
		return response.getFirstHeader(contentTransferCodificationHeader).getValue();
	}
	
	private byte[] decodeResponseContent(byte[] content, String contentEncodingHeaderValue) throws IOException, DataFormatException {
		return contentEncodingHandler.decodeData(content, contentEncodingHeaderValue);
	}
	
	private byte[] decodeResponseTransferContent(byte[] content, String contentTransferEncodingValue){
		return contentTransferEncodingHandler.decodeData(content, contentTransferEncodingValue);
	}
}