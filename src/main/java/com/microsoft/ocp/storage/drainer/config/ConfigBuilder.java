package com.microsoft.ocp.storage.drainer.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

public class ConfigBuilder implements ArgumentNamesConstants {

	private Config config;
	private CommandLine cli;

	private ConfigBuilder(String[] args, ConfigType type) throws ParseException {
		this.config = new Config();
		this.cli = CommandLineBuilder.build(args, type);
	}

	private ConfigBuilder addCommonOptions(ConfigType configType) throws ParseException {
		config.setConfigType(configType);

		if (cli.hasOption(INPUT_FILE_ARG)) {
			config.setInputFile(cli.getOptionValue(INPUT_FILE_ARG));
		}

		String lpStr = cli.getOptionValue(LIST_PARTITIONS_COUNT_ARG);

		if (!StringUtils.isNumeric(lpStr)) {
			throw new ParseException("Parameter " + LIST_PARTITIONS_COUNT_ARG + " should be a number");
		} else {
			config.setListPartitions(Integer.parseInt(lpStr));
		}

		return this;
	}

	private ConfigBuilder addAwsOptions() {
		config.setAwsAccountId(cli.getOptionValue(AWS_ACCOUNT_ID_ARG));
		config.setAwsAccountKey(cli.getOptionValue(AWS_ACCOUNT_KEY_ARG));
		config.setSourceBucket(cli.getOptionValue(AWS_SOURCE_BUCKET_ARG));
		config.setAwsRegion(cli.getOptionValue(AWS_REGION_ARG));

		return this;
	}

	private ConfigBuilder addAzureTargetBlobStorageOptions() {
		config.setTargetAzureStorageKey(cli.getOptionValue(AZURE_TARGET_STORAGE_KEY_ARG));
		config.setTargetContainer(cli.getOptionValue(AZURE_TARGET_STORAGE_CONTAINER_ARG));

		return this;
	}

	private ConfigBuilder addAzureSourceBlobStorageOptions() {
		config.setSourceAzureStorageKey(cli.getOptionValue(AZURE_SOURCE_STORAGE_KEY_ARG));
		config.setSourceContainer(cli.getOptionValue(AZURE_SOURCE_STORAGE_CONTAINER_ARG));

		return this;
	}

	private ConfigBuilder addCopyOptions() throws ParseException {
		String cpStr = cli.getOptionValue(COPY_PARTITIONS_COUNT_ARG);

		if (!StringUtils.isNumeric(cpStr)) {
			throw new ParseException("Parameter " + COPY_PARTITIONS_COUNT_ARG + " should be a number");
		} else {
			config.setCopyPartitions(Integer.parseInt(cpStr));
		}

		return this;
	}

	private ConfigBuilder addInventoryOptions() throws ParseException {
		config.setOutputDir(cli.getOptionValue(OUTPUT_PATH_ARG));
		config.setDirection(InventoryDirection.parse(cli.getOptionValue(INVENTORY_DIRECTION_ARG)));

		return this;
	}

	public Config build() throws ParseException {
		return config;
	}

	public static Config build(String[] args, ConfigType type) throws ParseException {

		ConfigBuilder builder = new ConfigBuilder(args, type);

		builder.addCommonOptions(type);

		if (type == ConfigType.INVENTORY) {
			builder.addInventoryOptions().addAzureTargetBlobStorageOptions();

			if (builder.config.getDirection() == InventoryDirection.AZURE_TO_AZURE) {
				builder.addAzureSourceBlobStorageOptions();
			} else {
				builder.addAwsOptions();
			}

		} else if (type == ConfigType.MIGRATION) {
			builder.addAwsOptions().addAzureTargetBlobStorageOptions().addCopyOptions();

		} else if (type == ConfigType.AWS_SIZING) {
			builder.addAwsOptions();

		} else if (type == ConfigType.AZURE_SIZING) {
			builder.addAzureTargetBlobStorageOptions();

		} else if (type == ConfigType.AZURE_TO_AZURE_COPY) {
			builder.addAzureTargetBlobStorageOptions().addAzureSourceBlobStorageOptions().addCopyOptions();
		}

		return builder.build();
	}

}
