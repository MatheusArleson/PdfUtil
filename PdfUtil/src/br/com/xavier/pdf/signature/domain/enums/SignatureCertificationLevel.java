package br.com.xavier.pdf.signature.domain.enums;

public enum SignatureCertificationLevel {
	
	NO_CERTIFICATION(0),
	CERTIFIED_NO_CHANGES_ALLOWED(1),
	CERTIFIED_FORM_FILLING(2),
	CERTIFIED_FORM_FILLING_AND_ANNOTATIONS(3);
	
	private int certificationLevel;
	
	private SignatureCertificationLevel(int certificationLevel) {
		this.certificationLevel = certificationLevel;
	}

	public int getCertificationLevel() {
		return certificationLevel;
	}
	
	public static SignatureCertificationLevel getSignatureCertificationLevel(int signatureCertificationLevel){
		for (SignatureCertificationLevel s : values()) {
			if(s.getCertificationLevel() == signatureCertificationLevel){
				return s;
			}
		}
		return null;
	}
}
