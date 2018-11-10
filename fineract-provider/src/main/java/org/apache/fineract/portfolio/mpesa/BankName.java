package org.apache.fineract.portfolio.mpesa;

public class BankName{
	private String bankId;
	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	private String bankName;
	
	public BankName(String bankId, String bankName) {
		this.bankId = bankId;
		this.bankName = bankName;
	}
	
	public BankName getBankName(String bankId, String bankName) {
		return this.getBankName(bankId, bankName);
	}
}