package com.microsoft.ocp.storage.drainer.config;

import java.io.Serializable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CommandLineBuilder implements Serializable, ArgumentNamesConstants {

	private static final long serialVersionUID = -768858440086799106L;

	private Options options = new Options();

	private CommandLineBuilder() {
	}

	private CommandLineBuilder addCommonOptions() {
		Option f = Option.builder(INPUT_FILE_ARG).hasArg().desc("Input file").build();
		Option listpartitions = Option.builder(LIST_PARTITIONS_COUNT_ARG).required().hasArg().desc("List partitions")
				.build();

		options.addOption(f);
		options.addOption(listpartitions);

		return this;
	}

	private CommandLineBuilder addAwsOptions() {
		return addAwsOptions(true);
	}

	private CommandLineBuilder addAwsOptions(boolean required) {
		Option awsaccid = Option.builder(AWS_ACCOUNT_ID_ARG).required().hasArg().desc("AWS Access Id").build();
		Option awsacckey = Option.builder(AWS_ACCOUNT_KEY_ARG).required().hasArg().desc("AWS Access Key").build();
		Option srcbucket = Option.builder(AWS_SOURCE_BUCKET_ARG).required().hasArg().desc("Source bucket").build();
		Option awsregion = Option.builder(AWS_REGION_ARG).required().hasArg().desc("AWS Region").build();

		options.addOption(srcbucket);
		options.addOption(awsaccid);
		options.addOption(awsacckey);
		options.addOption(awsregion);

		return this;
	}

	private CommandLineBuilder addAzureTargetBlobStorageOptions() {
		Option targetAzKey = Option.builder(AZURE_TARGET_STORAGE_KEY_ARG).required().hasArg()
				.desc("Azure target storage account key").build();
		Option targetContainer = Option.builder(AZURE_TARGET_STORAGE_CONTAINER_ARG).required().hasArg()
				.desc("Azure target blob storage container name").build();

		options.addOption(targetAzKey);
		options.addOption(targetContainer);

		return this;
	}

	private CommandLineBuilder addAzureSourceBlobStorageOptions() {
		Option sourceAzKey = Option.builder(AZURE_SOURCE_STORAGE_KEY_ARG).required().hasArg()
				.desc("Azure source storage account key").build();
		Option sourceContainer = Option.builder(AZURE_SOURCE_STORAGE_CONTAINER_ARG).required().hasArg()
				.desc("Azure source blob storage container name").build();

		options.addOption(sourceAzKey);
		options.addOption(sourceContainer);

		return this;
	}

	private CommandLineBuilder addCopyOptions() {
		Option copypartitions = Option.builder(COPY_PARTITIONS_COUNT_ARG).required().hasArg().desc("Copy partitions")
				.build();
		options.addOption(copypartitions);

		return this;
	}

	private CommandLineBuilder addInventoryOptions() {
		Option outputDir = Option.builder(OUTPUT_PATH_ARG).required().hasArg().desc("Output Dir").build();
		Option inventoryType = Option.builder(INVENTORY_DIRECTION_ARG).required().hasArg()
				.desc("Inventory direction: aws-to-azure or azure-to-aws").build();

		options.addOption(outputDir);
		options.addOption(inventoryType);

		return this;
	}

	private CommandLine build(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			throw new ParseException(e);
		}

		return cmd;
	}

	public static CommandLine build(String[] args, ConfigType type) throws ParseException {
		CommandLineBuilder builder = new CommandLineBuilder();
		builder.addCommonOptions();

		if (type == ConfigType.INVENTORY) {
			builder.addInventoryOptions().addAzureTargetBlobStorageOptions();

			InventoryDirection direction = getDirection(args);

			if (direction == InventoryDirection.AWS_TO_AZURE || direction == InventoryDirection.AZURE_TO_AWS) {
				builder.addAwsOptions();
			} else if (direction == InventoryDirection.AZURE_TO_AZURE) {
				builder.addAzureSourceBlobStorageOptions();
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

		return builder.build(args);
	}

	private static InventoryDirection getDirection(String[] args) throws ParseException {
		return InventoryDirection.guessFromArgs(args);
	}

}
