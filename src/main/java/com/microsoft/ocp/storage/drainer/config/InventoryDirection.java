package com.microsoft.ocp.storage.drainer.config;

import org.apache.commons.lang3.ArrayUtils;

public enum InventoryDirection {

	AWS_TO_AZURE, AZURE_TO_AWS, AZURE_TO_AZURE;

	public static final String AZURE_TO_AWS_INV_DIRECTION = "azure-to-aws";
	public static final String AWS_TO_AZURE_INV_DIRECTION = "aws-to-azure";
	public static final String AZURE_TO_AZURE_INV_DIRECTION = "azure-to-azure";

	public static InventoryDirection parse(String val) throws ParseException {
		if (val.equals(AWS_TO_AZURE_INV_DIRECTION)) {
			return AWS_TO_AZURE;
		} else if (val.equals(InventoryDirection.AZURE_TO_AWS_INV_DIRECTION)) {
			return AZURE_TO_AWS;
		} else if (val.equals(InventoryDirection.AZURE_TO_AZURE_INV_DIRECTION)) {
			return AZURE_TO_AZURE;
		} else {
			throw new ParseException("Direction option should be " + InventoryDirection.AWS_TO_AZURE_INV_DIRECTION
					+ " or " + InventoryDirection.AZURE_TO_AWS_INV_DIRECTION + " or "
					+ InventoryDirection.AZURE_TO_AZURE_INV_DIRECTION);
		}
	}

	public static InventoryDirection guessFromArgs(String[] args) throws ParseException {
		if (ArrayUtils.contains(args, AZURE_TO_AWS_INV_DIRECTION)) {
			return InventoryDirection.AZURE_TO_AWS;
		} else if (ArrayUtils.contains(args, AWS_TO_AZURE_INV_DIRECTION)) {
			return InventoryDirection.AWS_TO_AZURE;
		} else if (ArrayUtils.contains(args, AZURE_TO_AZURE_INV_DIRECTION)) {
			return InventoryDirection.AZURE_TO_AZURE;
		} else {
			throw new ParseException(
					"Unable to determine inventory direction from parameters: " + ArrayUtils.toString(args));
		}
	}
}
