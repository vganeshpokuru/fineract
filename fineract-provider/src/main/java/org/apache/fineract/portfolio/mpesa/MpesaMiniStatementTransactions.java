package org.apache.fineract.portfolio.mpesa;

public class MpesaMiniStatementTransactions {
	
	String responseCode;
	String responseMessage;
	String transactionType;
	String walletAccount;
	String amount;
	String reference;
	String provider;
	String beneficiaryAccount;
	String beneficiaryDetails;
	String transactionDate;
	
	public MpesaMiniStatementTransactions(String responseCode, String responseMessage, String transactionType,
			String walletAccount, String amount, String reference, String provider, String beneficiaryAccount,
			String beneficiaryDetails, String transactionDate) {
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.transactionType = transactionType;
		this.walletAccount = walletAccount;
		this.amount = amount;
		this.reference = reference;
		this.provider = provider;
		this.beneficiaryAccount = beneficiaryAccount;
		this.beneficiaryDetails = beneficiaryDetails;
		this.transactionDate = transactionDate;
	}
	
	
}
