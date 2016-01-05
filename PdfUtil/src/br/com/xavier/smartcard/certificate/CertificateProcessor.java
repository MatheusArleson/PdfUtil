package br.com.xavier.smartcard.certificate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.pdf.security.CertificateUtil;

import br.com.xavier.ntp.SntpClient;
import sun.security.pkcs11.SunPKCS11;

public class CertificateProcessor {

	private static final String tokenConfigurationBase64String	= new String("bmFtZSA9IFNtYXJ0Q2FyZA0KbGlicmFyeSA9IGM6L3dpbmRvd3Mvc3lzdGVtMzIvYWV0cGtzczEuZGxs");
	private static final int crlPacketLenght = 10*1024;
	private static final String transformationAlgorithm = new String("RSA");
	
	private String providerName;
	private SntpClient sntpClient;
	
	private KeyStore keyStore;
	private Certificate[] certificateChain;
	private String firstAlias;
	
	/*
	 * Constructor
	 */
	public CertificateProcessor() {
		this.providerName = initializeProviders();
		this.sntpClient = new SntpClient();
	}
	
	public String getProviderName() {
		return providerName;
	}
	
	/*
	 * Public Methods  
	 */
	public void loadKeystore(char[] cardPassword) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		this.firstAlias = null;
		this.certificateChain = null;
		
		keyStore = KeyStore.getInstance("PKCS11");
		keyStore.load(null, cardPassword);
	}
	
	public Certificate[] getCertificateChain() throws KeyStoreException {
		if(certificateChain == null){
			certificateChain = keyStore.getCertificateChain(getFirstAlias()); 
		}
		return certificateChain;
	}
	
	public String getFirstAlias() throws KeyStoreException, NoSuchElementException {
		if(firstAlias == null){
			firstAlias = getAllAliases().nextElement(); 
		}
		return firstAlias;
	}
	
	public boolean isFirstCertificateChainValid() throws KeyStoreException, IOException {
		return isCertificateChainValid(getCertificateChain());
	}
	
	public boolean isFirstCertificateRevoked() throws CertificateException, CRLException, KeyStoreException, NoSuchElementException, HttpException, IOException{
		return isCertificateRevoked(getCertificateChain());
	}
	
	public PrivateKey getFirstCertificatePrivateKey(char[] password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchElementException {
		return (PrivateKey) keyStore.getKey(getFirstAlias(), password);
	}
	
	public String getFirstCertificatePublicKeyBase64String() throws KeyStoreException  {
		return Base64.encodeBase64String(getFirstCertificatePublicKeyBytes());
	}
	
	public byte[] encryptWithCardCertificatePrivateKey(char[] password, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(transformationAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, getFirstCertificatePrivateKey(password));
		byte[] cipherData = cipher.doFinal(message.getBytes());
		return cipherData;
	}
	
	public String decryptWithCertificatePublicKeyBase64String(byte[] message, String certificatePublicKeyBase64String) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		Cipher cipher = Cipher.getInstance(transformationAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, generatePublicKey(certificatePublicKeyBase64String));
		byte[] cipherData = cipher.doFinal(message);
		return new String(cipherData);
	}
	
	public String decryptWithCardPublicKey(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, KeyStoreException {
		Cipher cipher = Cipher.getInstance(transformationAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, getFirstCertificatePublicKey());
		byte[] cipherData = cipher.doFinal(message);
		return new String(cipherData);
	}
	
	/*
	 * Internal Methods
	 */
	private String initializeProviders() {
		String providerName = null;
		BouncyCastleProvider providerBC = new BouncyCastleProvider();
		Security.addProvider(providerBC);
		
		byte[] decodedConfigurationFile = Base64.decodeBase64(tokenConfigurationBase64String);
		InputStream is = new ByteArrayInputStream(decodedConfigurationFile);
		Provider providerPKCS11 = new SunPKCS11(is);
		Security.addProvider(providerPKCS11);
		
		providerName =  providerPKCS11.getName();
		return providerName;
	}
	
	private Enumeration<String> getAllAliases() throws KeyStoreException {
		return keyStore.aliases();
	}
	
	private X509Certificate getFirstCertificate() throws KeyStoreException {
		return (X509Certificate) keyStore.getCertificate(getFirstAlias());
	}
	
	private PublicKey getFirstCertificatePublicKey() throws KeyStoreException{
		return getFirstCertificate().getPublicKey();
	}
	
	private byte[] getFirstCertificatePublicKeyBytes() throws KeyStoreException{
		return getFirstCertificatePublicKey().getEncoded();
	}
	
	private PublicKey generatePublicKey(String certificatePublicKeyBase64String) throws NoSuchAlgorithmException, InvalidKeySpecException{
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(certificatePublicKeyBase64String));
		KeyFactory keyFactory = KeyFactory.getInstance(transformationAlgorithm);
		PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		return pubKey;
	}
	
	/*
	 * Certificate Validation Methods 
	 */
	
	private boolean isCertificateChainValid(Certificate[] chain) throws IOException {		
		for (int i = 0; i < chain.length; i++) {
			X509Certificate certificate = (X509Certificate) chain[i];
			Date now = sntpClient.getTime();
			
			try {
				certificate.checkValidity(now);
			} catch (CertificateExpiredException  | CertificateNotYetValidException e) {
				return false;
			}
			
			Date certificateExpirationDate = certificate.getNotAfter();
			
			Calendar actualCalendar = Calendar.getInstance();
			Calendar certificateCalendar = Calendar.getInstance();
			
			actualCalendar.setTime(now);
			certificateCalendar.setTime(certificateExpirationDate);
			
			boolean isSameYear = (actualCalendar.get(Calendar.YEAR) == certificateCalendar.get(Calendar.YEAR));
			boolean isSameDayOfYear = (actualCalendar.get(Calendar.DAY_OF_YEAR) == certificateCalendar.get(Calendar.DAY_OF_YEAR));
			
			if(isSameYear && isSameDayOfYear){
				return false;
			}
		}
		return true;
	}
	
	private boolean isCertificateRevoked(Certificate[] CertificateChain)
		throws HttpException, IOException, CertificateException, CRLException {

		LinkedHashSet<URL> crlURLsSet = new LinkedHashSet<URL>();
		for (int i = 0; i < CertificateChain.length; i++) {
			X509Certificate cert = (X509Certificate) CertificateChain[i];
			crlURLsSet.add(new URL(CertificateUtil.getCRLURL(cert)));
		}

		CloseableHttpClient client = HttpClientBuilder.create().build();
		ArrayList<byte[]> crlFileByteArrayList = new ArrayList<byte[]>();

		for (int i = 0; i < crlURLsSet.size(); i++) {
			URL crlURL = (URL) crlURLsSet.toArray()[i];

			HttpGet httpGet = new HttpGet(crlURL.toExternalForm());
			HttpResponse httpGetResponse = client.execute(httpGet);
			HttpEntity httpGetEntity = httpGetResponse.getEntity();

			int httpGetResponseStatusCode = httpGetResponse.getStatusLine().getStatusCode();

			if (httpGetEntity != null && httpGetResponseStatusCode == HttpStatus.SC_OK) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

				InputStream is = httpGetEntity.getContent();
				int lido;
				byte[] lcrBuffer = new byte[crlPacketLenght];
				while ((lido = is.read(lcrBuffer)) > 0) {
					bos.write(lcrBuffer, 0, lido);
				}
				is.close();
				crlFileByteArrayList.add(bos.toByteArray());
			} else {
				throw new HttpException(new String("" + httpGetResponseStatusCode));
			}
		}
		client.close();
		
		ArrayList<X509CRL> X509CRLList = new ArrayList<X509CRL>();
		CertificateFactory cf;
		cf = CertificateFactory.getInstance("X.509");

		boolean isRevoked = false;

		for (int i = 0; i < crlFileByteArrayList.size(); i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(crlFileByteArrayList.get(i));
			X509CRLList.add((X509CRL) cf.generateCRL(bais));
			bais.close();
		}

		for (int i = 0; i < X509CRLList.size(); i++) {
			X509CRL crl = X509CRLList.get(i);
			for (int j = 0; j < CertificateChain.length; j++) {
				if (crl.isRevoked(CertificateChain[j]) == true) {
					isRevoked = true;
					break;
				}
			}
		}
		return isRevoked;
	}
	
}
