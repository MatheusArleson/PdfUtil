package br.com.xavier.pdf.smartcard.signer;

public interface SignatureProcessObserver {
	
	void informStatus(String currentStatus);
}
