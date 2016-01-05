package br.com.xavier.pdf.signer;

public interface SignatureProcessObserver {
	
	void informStatus(String currentStatus);
}
