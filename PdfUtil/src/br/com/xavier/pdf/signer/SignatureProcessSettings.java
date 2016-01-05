package br.com.xavier.pdf.signer;

public class SignatureProcessSettings {
	
	private boolean	validateSignerCertificateAlias;
	private String	validSignerCertificateAlias;
	private boolean	putWatermark;
	private String	watermarkText;
	private boolean checkCertificateValidity;
	private boolean checkCertificateRevocation;
	private boolean	embedCRLZip;
	private boolean	isSignatureVisible;
	
	public SignatureProcessSettings(
		boolean validateSignerCertificateAlias, String validSignerCertificateAlias, boolean putWatermark,
		String watermarkText, boolean checkCertificateValidity, boolean checkCertificateRevocation, 
		boolean embedCRLZip, boolean isSignatureVisible
	) {
		super();
		this.validateSignerCertificateAlias = validateSignerCertificateAlias;
		this.validSignerCertificateAlias = validSignerCertificateAlias;
		this.putWatermark = putWatermark;
		this.watermarkText = watermarkText;
		this.checkCertificateValidity = checkCertificateValidity;
		this.checkCertificateRevocation = checkCertificateRevocation;
		this.embedCRLZip = embedCRLZip;
		this.isSignatureVisible = isSignatureVisible;
	}

	public boolean isValidateSignerCertificateAlias() {
		return validateSignerCertificateAlias;
	}

	public void setValidateSignerCertificateAlias(
			boolean validateSignerCertificateAlias) {
		this.validateSignerCertificateAlias = validateSignerCertificateAlias;
	}

	public String getValidSignerCertificateAlias() {
		return validSignerCertificateAlias;
	}

	public void setValidSignerCertificateAlias(String validSignerCertificateAlias) {
		this.validSignerCertificateAlias = validSignerCertificateAlias;
	}

	public boolean isPutWatermark() {
		return putWatermark;
	}

	public void setPutWatermark(boolean putWatermark) {
		this.putWatermark = putWatermark;
	}

	public String getWatermarkText() {
		return watermarkText;
	}

	public void setWatermarkText(String watermarkText) {
		this.watermarkText = watermarkText;
	}

	public boolean isCheckCertificateValidity() {
		return checkCertificateValidity;
	}

	public void setCheckCertificateValidity(boolean checkCertificateValidity) {
		this.checkCertificateValidity = checkCertificateValidity;
	}

	public boolean isCheckCertificateRevocation() {
		return checkCertificateRevocation;
	}

	public void setCheckCertificateRevocation(boolean checkCertificateRevocation) {
		this.checkCertificateRevocation = checkCertificateRevocation;
	}

	public boolean isEmbedCRLZip() {
		return embedCRLZip;
	}

	public void setEmbedCRLZip(boolean embedCRLZip) {
		this.embedCRLZip = embedCRLZip;
	}

	public boolean isSignatureVisible() {
		return isSignatureVisible;
	}

	public void setSignatureVisible(boolean isSignatureVisible) {
		this.isSignatureVisible = isSignatureVisible;
	}
}