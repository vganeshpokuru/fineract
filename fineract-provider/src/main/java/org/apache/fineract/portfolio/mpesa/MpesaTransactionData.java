package org.apache.fineract.portfolio.mpesa;

public class MpesaTransactionData {

	String responseCode;
	String responseMessage;
	String rrn;
	String fee;
	String reference;
	String beneficiaryDetails;

	public MpesaTransactionData(String responseCode, String responseMessage, String rrn, String fee, String reference,
			String beneficiaryDetails) {
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.rrn = rrn;
		this.fee = fee;
		this.reference = reference;
		this.beneficiaryDetails = beneficiaryDetails;
	}

}
