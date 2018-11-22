package com.microsoft.ocp.storage.drainer.config;

import java.io.Serializable;

public class Config implements Serializable {

	private static final long serialVersionUID = 3606978506721579987L;

	static {
		System.setProperty("java.security.egd", "file:/dev/./urandom");
	}

	public static boolean isLocalEnv() {
		return "true".equalsIgnoreCase(System.getProperty("local.env"));
	}

	private ConfigType configType;
	private String targetAzureStorageKey;
	private String sourceAzureStorageKey;
	private String inputFile;
	private String sourceBucket;
	private String sourceContainer;
	private String targetContainer;
	private String awsAccountId;
	private String awsAccountKey;
	private String awsRegion;
	private String outputDir;
	private int listPartitions;
	private int copyPartitions;
	private boolean hasInputFile;
	private String type;
	private InventoryDirection direction;

	public boolean isInventory() {
		return configType == ConfigType.INVENTORY;
	}

	public boolean isMigration() {
		return configType == ConfigType.MIGRATION;
	}

	public boolean isSizing() {
		return configType == ConfigType.AWS_SIZING || configType == ConfigType.AZURE_SIZING;
	}

	public boolean isAwsSizing() {
		return configType == ConfigType.AWS_SIZING;
	}

	public boolean isAzureSizing() {
		return configType == ConfigType.AZURE_SIZING;
	}

	public boolean isAzureToAzureCopy() {
		return configType == ConfigType.AZURE_TO_AZURE_COPY;
	}
	
	public ConfigType getConfigType() {
		return configType;
	}

	public void setConfigType(ConfigType configType) {
		this.configType = configType;
	}

	public String getSourceAzureStorageKey() {
		return sourceAzureStorageKey;
	}

	public void setSourceAzureStorageKey(String sourceAzureStorageKey) {
		this.sourceAzureStorageKey = sourceAzureStorageKey;
	}

	public String getTargetAzureStorageKey() {
		return targetAzureStorageKey;
	}

	public void setTargetAzureStorageKey(String targetAzureStorageKey) {
		this.targetAzureStorageKey = targetAzureStorageKey;
	}

	public void setSourceContainer(String sourceContainer) {
		this.sourceContainer = sourceContainer;
	}

	public String getSourceContainer() {
		return sourceContainer;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;

		if (this.inputFile != null && inputFile.trim().length() > 0) {
			this.setHasInputFile(true);
		}
	}

	public String getSourceBucket() {
		return sourceBucket;
	}

	public void setSourceBucket(String sourceBucket) {
		this.sourceBucket = sourceBucket;
	}

	public String getTargetContainer() {
		return targetContainer;
	}

	public void setTargetContainer(String targetContainer) {
		this.targetContainer = targetContainer;
	}

	public String getAwsAccountId() {
		return awsAccountId;
	}

	public void setAwsAccountId(String awsAccountId) {
		this.awsAccountId = awsAccountId;
	}

	public String getAwsAccountKey() {
		return awsAccountKey;
	}

	public void setAwsAccountKey(String awsAccountKey) {
		this.awsAccountKey = awsAccountKey;
	}

	public String getAwsRegion() {
		return awsRegion;
	}

	public void setAwsRegion(String awsRegion) {
		this.awsRegion = awsRegion;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public int getListPartitions() {
		return listPartitions;
	}

	public void setListPartitions(int listPartitions) {
		this.listPartitions = listPartitions;
	}

	public int getCopyPartitions() {
		return copyPartitions;
	}

	public void setCopyPartitions(int copyPartitions) {
		this.copyPartitions = copyPartitions;
	}

	public boolean isHasInputFile() {
		return hasInputFile;
	}

	public void setHasInputFile(boolean hasInputFile) {
		this.hasInputFile = hasInputFile;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public InventoryDirection getDirection() {
		return direction;
	}

	public void setDirection(InventoryDirection direction) {
		this.direction = direction;
	}
}