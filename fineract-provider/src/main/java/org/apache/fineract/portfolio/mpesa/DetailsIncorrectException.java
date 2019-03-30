package org.apache.fineract.portfolio.mpesa;

import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

public class DetailsIncorrectException  extends PlatformApiDataValidationException {
	public DetailsIncorrectException() {
	super("check the details entered", "check the details entered",null);
	}
}
