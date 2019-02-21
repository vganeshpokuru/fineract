package org.apache.fineract.portfolio.mpesa;

public class BankWalletNumberVerificationDataWalletVerified {
	private boolean isWalletVerified;
	private boolean isMobileVerified;
	private String generateString;

	public BankWalletNumberVerificationDataWalletVerified(boolean isWalletVerified, boolean isMobileVerified) {
		this.isWalletVerified = isWalletVerified;
		this.isMobileVerified = isMobileVerified;
	}

	public boolean getIsWalletVerified() {
		return this.isWalletVerified;
	}
	
	public boolean getIsMobileVerified() {
		return this.isMobileVerified;
	}

	public void setIsWalletVerified(boolean isVerified) {
		this.isWalletVerified = isVerified;
	}
	
	public void setIsMobileVerified(boolean isVerified) {
		this.isMobileVerified = isVerified;
	}
	
	public void setGeneratedString(String generatedString) {
		this.generateString = generatedString;
	}
}
