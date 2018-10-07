package com.microsoft.ocp.storage.drainer.config;

public interface ArgumentNamesConstants {

	public static final String INPUT_FILE_ARG = "f";
	public static final String LIST_PARTITIONS_COUNT_ARG = "lp";
	public static final String AWS_ACCOUNT_ID_ARG = "awsaccid";
	public static final String AWS_ACCOUNT_KEY_ARG = "awsacckey";
	public static final String AWS_SOURCE_BUCKET_ARG = "s3bucket";
	public static final String AWS_REGION_ARG = "awsregion";
	public static final String AZURE_TARGET_STORAGE_KEY_ARG = "targetazkey";
	public static final String AZURE_SOURCE_STORAGE_KEY_ARG = "sourceazkey";
	public static final String AZURE_TARGET_STORAGE_CONTAINER_ARG = "targetblobcontainer";
	public static final String AZURE_SOURCE_STORAGE_CONTAINER_ARG = "sourceblobcontainer";
	public static final String COPY_PARTITIONS_COUNT_ARG = "cp";
	public static final String OUTPUT_PATH_ARG = "o";
	public static final String INVENTORY_DIRECTION_ARG = "direction";
}
