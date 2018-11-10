package org.apache.fineract.portfolio.mpesa;

public class BankWalletNumberVerificationData {
	private boolean isVerified;

	public BankWalletNumberVerificationData(boolean isVerified) {
		this.isVerified = isVerified;
	}

	public boolean getIsVerified() {
		return this.isVerified;
	}

	public void setIsVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
	
}
