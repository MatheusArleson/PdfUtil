package br.com.xavier.pdf.signature.domain;

import br.com.xavier.pdf.signature.domain.enums.SignatureCertificationLevel;
import br.com.xavier.provider.content.InputProvider;
import br.com.xavier.provider.content.OutputProvider;

public class PdfDocument {
	
	private InputProvider	sourceProvider;	
	private OutputProvider	outputProvider;
	
	private byte[]	documentData;
	private boolean isDocumentDataSigned;
	
	private String signatureReason;
	private String signatureLocation;
	private String signerContact;
	private int signaturePage;
	private SignatureCertificationLevel signatureCertificationLevel;

	public PdfDocument(
		InputProvider sourceProvider, OutputProvider outputProvider, 
		String signatureReason, String signatureLocation, 
		String signerContact, int signaturePage,
		SignatureCertificationLevel signatureCertificationLevel
	) {
		super();
		this.sourceProvider = sourceProvider;
		this.outputProvider = outputProvider;
		this.documentData = null;
		this.isDocumentDataSigned = false;
		this.signatureReason = signatureReason;
		this.signatureLocation = signatureLocation;
		this.signerContact = signerContact;
		this.signaturePage = signaturePage;
		this.signatureCertificationLevel = signatureCertificationLevel;
	}

	public byte[] obtainDocumentData() throws Exception{
		if(documentData == null && !isDocumentDataSigned){
			documentData = sourceProvider.provideData();
		}
		return documentData;
	}
	
	public void dispatchDocument() throws Exception{
		outputProvider.outputData(documentData);
	}

	public void setSignedDocumentData(byte[] data){
		this.documentData = data;
		this.isDocumentDataSigned = true;
	}

	public InputProvider getSourceProvider() {
		return sourceProvider;
	}

	public OutputProvider getOutputProvider() {
		return outputProvider;
	}

	public boolean isDocumentDataSigned() {
		return isDocumentDataSigned;
	}

	public String getSignatureReason() {
		return signatureReason;
	}

	public String getSignatureLocation() {
		return signatureLocation;
	}

	public String getSignerContact() {
		return signerContact;
	}

	public int getSignaturePage() {
		return signaturePage;
	}

	public SignatureCertificationLevel getSignatureCertificationLevel() {
		return signatureCertificationLevel;
	}
}