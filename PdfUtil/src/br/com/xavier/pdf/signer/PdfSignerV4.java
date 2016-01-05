package br.com.xavier.pdf.signer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAConformanceException;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAStamper;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CertificateUtil;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

import br.com.xavier.content.provider.InputProvider;
import br.com.xavier.content.provider.OutputProvider;
import br.com.xavier.content.provider.input.InputProviderFactory;
import br.com.xavier.content.provider.output.OutputProviderFactory;
import br.com.xavier.pdf.signature.domain.PdfDocument;
import br.com.xavier.pdf.signature.domain.enums.SignatureCertificationLevel;
import br.com.xavier.pdf.watermark.PdfWatermarker;
import br.com.xavier.smartcard.certificate.CertificateProcessor;
import br.com.xavier.util.StringValidator;

import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

public class PdfSignerV4 {
	
	public static void main(String[] args) throws Exception {
		System.out.println("INIT");
		
		CertificateProcessor cp = new CertificateProcessor();
		PdfSignerV4 signer = new PdfSignerV4(cp);
		SignatureProcessSettings sps = new SignatureProcessSettings(false, null, false, null, false, false, false, false);
		
		InputProvider inputProvider = InputProviderFactory.getInputProvider(new URL("file:///C:/Users/iCE/Desktop/hello.pdf"));
		OutputProvider outputProvider = OutputProviderFactory.getOutputProvider(new URL("file:///C:/Users/iCE/Desktop/hello_signed.pdf"));
		
		PdfDocument pd = new PdfDocument(inputProvider, outputProvider, "motivo", "local", "contato", 1, SignatureCertificationLevel.CERTIFIED_NO_CHANGES_ALLOWED);
		
		List<PdfDocument> pdfDocumentsList = new ArrayList<PdfDocument>();
		pdfDocumentsList.add(pd);
		
		signer.sign("mayara".toCharArray(), sps, pdfDocumentsList);
		
		pd.dispatchDocument();
		
		System.out.println("FIM");
	}
	
	private static final String tsaUrl = new String("http://timestamping.edelweb.fr/service/tsp");
	private static final String digestAlgorithm = DigestAlgorithms.SHA256;
	private static final CryptoStandard cryptographySpecification = CryptoStandard.CMS;
	
	private static final String signatureFieldBaseName = new String("Signature");
	private static final BaseColor signatureWatermarkColor = BaseColor.GRAY;
	
	private ArrayList<SignatureProcessObserver> observers = new ArrayList<SignatureProcessObserver>();

	private CertificateProcessor certificateProcessor;
	
	public PdfSignerV4(CertificateProcessor certificateProcessor) {
		this.certificateProcessor = certificateProcessor;
	}
	
	public PdfSignerV4() {
		this.certificateProcessor = new CertificateProcessor();
	}
	
	public void registerObserver(SignatureProcessObserver observer) {
        observers.add(observer);
    }

    public void notifyListeners(String currentStatus) {
        for(SignatureProcessObserver observer : observers) {
            observer.informStatus(currentStatus);
        }
    }
    
	public void sign(
		char[] cardPassword, SignatureProcessSettings settings, List<PdfDocument> pdfDocumentsList 
	) throws IOException, GeneralSecurityException, DocumentException, HttpException, CertificateException {
		
		notifyListeners("Lendo SmartCard.");
		certificateProcessor.loadKeystore(cardPassword);
		
		notifyListeners("Obtendo Alias do Certificado.");
		String firstAlias = certificateProcessor.getFirstAlias();
		
		if(settings.isValidateSignerCertificateAlias()){
			String validSignerCertificateAlias = settings.getValidSignerCertificateAlias();
			if(!StringValidator.validateString(validSignerCertificateAlias) || !validSignerCertificateAlias.equalsIgnoreCase(firstAlias)){
				notifyListeners("Certificado Não Autorizado.");
				throw new CertificateException(
					"USU\u00C1RIO N\u00C3O AUTORIZADO A ASSINAR."
					+ "\nCERTIFICADO ESPERADO: " + validSignerCertificateAlias
					+ "\nCERTIFICADO NO CARTAO: " + firstAlias
				);
			}
		}
		
		notifyListeners("Lendo Cadeia de Certificados.");
		Certificate[] certificateChain = certificateProcessor.getCertificateChain();
		
		if(settings.isCheckCertificateValidity()){
			notifyListeners("Checando Validade Do Certificado.");
			boolean isCertificateChainValid = certificateProcessor.isFirstCertificateChainValid();
			
			if(isCertificateChainValid == false){
				notifyListeners("Certificado Inválido.");
				throw new CertificateException("CERTIFICADO EXPIRADO");
			}
		}
		
		if(settings.isCheckCertificateRevocation()){
			notifyListeners("Checando Revogação do Certificado.");
			boolean isCertificateChainRevoked = certificateProcessor.isFirstCertificateRevoked();
			
			if(isCertificateChainRevoked == true){
				notifyListeners("Certificado Revogado");
				throw new CertificateException("CERTIFICADO REVOGADO");
			}
		}
		
		notifyListeners("Obtendo Chave Privada.");
		PrivateKey certificatePrivateKey = certificateProcessor.getFirstCertificatePrivateKey(cardPassword);
		
		notifyListeners("Gerando Carimbo de Tempo.");
		TSAClientBouncyCastle tsaClient = generateTSAClientBouncyCastleInstance(certificateChain);
		
		List<CrlClient> crlList = null;
		if(settings.isEmbedCRLZip()){
			notifyListeners("Gerando Lista de CRL.");
			crlList = generateCRLClientList(certificateChain);
		}
		
		sign(
			pdfDocumentsList, settings, firstAlias, certificateChain, certificatePrivateKey, 
			digestAlgorithm, certificateProcessor.getProviderName(), cryptographySpecification, 
			crlList, tsaClient, 0
		);
	}
	
	private void sign(
		List<PdfDocument> documentsList, SignatureProcessSettings settings, String firstAlias, Certificate[] certificateChain, 
		PrivateKey certificatePrivateKey, String digestAlgorithm, String providerName, CryptoStandard subfilter, 
		Collection<CrlClient> crlList, TSAClient tsaClient, int estimatedSignedFileSize
	) throws DocumentException, IOException, GeneralSecurityException {
		
		String baseStr = new String("Assinando [#1/" + documentsList.size() + "]");
		
		for (int i = 0; i < documentsList.size(); i++) {
			notifyListeners(baseStr.replace("#1", String.valueOf(i+1)));
			
			PdfDocument pdfDoc = documentsList.get(i);
			InputStream inputPdfStream = null;
			try {
				inputPdfStream = new ByteArrayInputStream(pdfDoc.obtainDocumentData());
			} catch(Exception e) {
				//this exception is already caught on data aquisition
			}
			
			ByteArrayOutputStream outputPdfStream = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[inputPdfStream.available()];
			inputPdfStream.read(buffer);
			inputPdfStream.close();
			
			PdfReader reader = null;
			PdfStamper stamper = null;
			try {
				reader = new PdfReader(buffer);
				stamper = PdfAStamper.createSignature(
					reader, outputPdfStream, '\0', null, true, PdfAConformanceLevel.PDF_A_1B
				);
			} catch(PdfAConformanceException e){
				reader = new PdfReader(buffer);
				stamper = PdfStamper.createSignature(reader, outputPdfStream, '\0', null, true);
			}

			AcroFields readerFields = reader.getAcroFields();
			ArrayList<String> signatureFieldsNames = readerFields.getSignatureNames();
			
			if(settings.isPutWatermark() && signatureFieldsNames.size() == 0){
				PdfWatermarker.applyWatermarkOnAllPages(
					reader, stamper, settings.getWatermarkText(), signatureWatermarkColor, 1.0f
				);
			}

			// Creating the appearance
			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
			appearance.setReason(pdfDoc.getSignatureReason());
			appearance.setLocation(pdfDoc.getSignatureLocation());
			appearance.setContact(pdfDoc.getSignerContact());
			appearance.setCertificationLevel(pdfDoc.getSignatureCertificationLevel().getCertificationLevel());

			String signatureFieldName = generateSignatureFieldName(signatureFieldsNames);
			if(settings.isSignatureVisible()){
				BaseFont bf = BaseFont.createFont("/oracle/forms/aptools/pdf/resources/font/arial.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
				appearance.setLayer2Font(new Font(bf));
				appearance.setLayer2Text("DOCUMENTO ASSINADO DIGITALMENTE POR: " + firstAlias);
				appearance.setRenderingMode(RenderingMode.DESCRIPTION);
				
				Rectangle pageSize = reader.getPageSize(pdfDoc.getSignaturePage());
				
				int numberOfVisibleSignatureFieldsOnSignaturePage = numberOfVisibleSignatureFieldsOnSignaturePage(readerFields, signatureFieldsNames, pdfDoc.getSignaturePage());			
				int signatureFieldLowerLeftX = 2;
				int signatureFieldLowerLeftY = (numberOfVisibleSignatureFieldsOnSignaturePage * 20);
				int signatureFieldUpperRightX = (int) (pageSize.getWidth() - 2.0f);
				int signatureFieldUpperRightY = (numberOfVisibleSignatureFieldsOnSignaturePage * 20) + 20;
				
				Rectangle signatureFieldRectangle = new Rectangle(
						signatureFieldLowerLeftX, signatureFieldLowerLeftY, signatureFieldUpperRightX, signatureFieldUpperRightY
				);
				
				appearance.setVisibleSignature(signatureFieldRectangle, pdfDoc.getSignaturePage(), signatureFieldName);
			} else {
				appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), pdfDoc.getSignaturePage(), signatureFieldName);
			}
			
			// Making signature field read-only
//				AcroFields stamperFields = stamper.getAcroFields();
//				stamperFields.setFieldProperty(signatureFieldName, "setfflags", PdfFormField.FF_READ_ONLY, null);
			
			OcspClient ocspClient = new OcspClientBouncyCastle();
	
			// Creating the signature
			ExternalSignature pks = new PrivateKeySignature(certificatePrivateKey, digestAlgorithm, providerName);
			ExternalDigest digest = new BouncyCastleDigest();
			MakeSignature.signDetached(appearance, digest, pks, certificateChain, crlList, ocspClient, tsaClient, estimatedSignedFileSize, subfilter);
	
			stamper.close();
			reader.close();
			outputPdfStream.flush();
			outputPdfStream.close();
			
			pdfDoc.setSignedDocumentData(outputPdfStream.toByteArray());
		}
	}
	
	private TSAClientBouncyCastle generateTSAClientBouncyCastleInstance(Certificate[] certificateChain){
		TSAClientBouncyCastle tsaClient = null;
		String certificatetsaUrl = null;
		
		for (int i = 0; i < certificateChain.length; i++) {
			X509Certificate cert = (X509Certificate) certificateChain[i];
			certificatetsaUrl = CertificateUtil.getTSAURL(cert);
			if (certificatetsaUrl != null) {
				break;
			}
		}
		
		if(certificatetsaUrl != null){
			tsaClient = new TSAClientBouncyCastle(certificatetsaUrl);
		} else {
			tsaClient = new TSAClientBouncyCastle(tsaUrl, null, null);
		}
		
		//DEBUG Verificando o timestamp criado
//		tsaClient.setTSAInfo(new TSAInfoBouncyCastle() {
//			public void inspectTimeStampTokenInfo(TimeStampTokenInfo info) {
//				System.out.println("#> TimeStamp: ");
//				System.out.println(info.getGenTime());
//			}
//		});
		return tsaClient;
	}
	
	private List<CrlClient> generateCRLClientList(Certificate[] chain){ 
		List<CrlClient> crlList = new ArrayList<CrlClient>();
		crlList.add(new CrlClientOnline(chain));
		return crlList;
	}
	
	private String generateSignatureFieldName(ArrayList<String> signatureFieldsNames){
		int count = 0;
		while(signatureFieldsNames.contains(signatureFieldBaseName.concat(String.valueOf(count)))){
			count++;
		}
		return signatureFieldBaseName.concat(String.valueOf(count));
	}
	
	private int numberOfVisibleSignatureFieldsOnSignaturePage(AcroFields readerFields, ArrayList<String> signatureFieldsNames, int signaturePage) {
		int count = 0;
		for (String signatureFieldName : signatureFieldsNames) {
			Item i = readerFields.getFieldItem(signatureFieldName);
			int page = i.getPage(0);
			if(page == signaturePage){
				PdfDictionary pdct = i.getMerged(0); 
				PdfNumber flags = pdct.getAsNumber(PdfName.F); 
				if ((flags.intValue() & PdfAnnotation.FLAGS_HIDDEN) == 0) {
					count = count + 1;
				} 
			}
		}
		return count;
	}
}
