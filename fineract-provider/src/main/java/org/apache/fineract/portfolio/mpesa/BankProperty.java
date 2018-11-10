package org.apache.fineract.portfolio.mpesa;

public class BankProperty{
	private String propertyName;
	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return PropertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.PropertyValue = propertyValue;
	}

	private String PropertyValue;
	
	public BankProperty(String proprtyName, String propertyValue) {
		this.propertyName = proprtyName;
		this.PropertyValue = propertyValue;
	}
	
	public BankProperty getBankName(String propertyName, String propertyValue) {
		return this.getBankName(propertyName, propertyValue);
	}
}