package org.apache.fineract.portfolio.mpesa;

public class MpesaBalanceData {
	
	String responseCode;
	String responseMessage;
	String availableBalance;
	String ledgerBalance;

	public MpesaBalanceData(String responseCode, String responseMessage, String availableBalance,
			String ledgerBalance) {
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.availableBalance = availableBalance;
		this.ledgerBalance = ledgerBalance;
	}
}
