package com.microsoft.ocp.storage.drainer.tests.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ConfigBuilder;
import com.microsoft.ocp.storage.drainer.config.ConfigType;
import com.microsoft.ocp.storage.drainer.config.InventoryDirection;
import com.microsoft.ocp.storage.drainer.config.ParseException;

public class TestConfig {

	private static final String LP_ARG_NAME = "-lp";
	private static final String LP_ARG_VALUE = "10";
	private static final String CP_ARG_NAME = "-cp";
	private static final String CP_ARG_VALUE = "100";
	private static final String S3BUCKET_ARG_NAME = "-s3bucket";
	private static final String S3BUCKET_ARG_VALUE = "s3bucket";
	private static final String AWSACCID_ARG_NAME = "-awsaccid";
	private static final String AWSACCID_ARG_VALUE = "awsaccid";
	private static final String AWSACCKEY_ARG_NAME = "-awsacckey";
	private static final String AWSACCKEY_ARG_VALUE = "awsacckey";
	private static final String AWSREGION_ARG_NAME = "-awsregion";
	private static final String AWSREGION_ARG_VALUE = "us-east-1";

	private static final String TARGET_AZKEY_ARG_NAME = "-targetazkey";
	private static final String TARGET_AZKEY_ARG_VALUE = "azkey123";

	private static final String TARGET_BLOB_CONTAINER_ARG_NAME = "-targetblobcontainer";
	private static final String TARGET_BLOB_CONTAINER_ARG_VALUE = "blobcontainer";

	private static final String SOURCE_AZKEY_ARG_NAME = "-sourceazkey";
	private static final String SOURCE_AZKEY_ARG_VALUE = "azkey123";

	private static final String SOURCE_BLOB_CONTAINER_ARG_NAME = "-sourceblobcontainer";
	private static final String SOURCE_BLOB_CONTAINER_ARG_VALUE = "blobcontainer";

	private static final String OUTPUT_DIR_ARG_NAME = "-o";
	private static final String OUTPUT_DIR_ARG_VALUE = "output";
	private static final String DIRECTION_ARG_NAME = "-direction";
	private static final String DIRECTION_AWS_TO_AZURE_ARG_VALUE = "aws-to-azure";
	private static final String DIRECTION_AZURE_TO_AWS_ARG_VALUE = "azure-to-aws";
	private static final String DIRECTION_AZURE_TO_AZURE_ARG_VALUE = "azure-to-azure";

	@Before
	public void setupTest() {
	}

	private List<String> getMigrationArgs() {
		String[] args = { LP_ARG_NAME, LP_ARG_VALUE, CP_ARG_NAME, CP_ARG_VALUE, S3BUCKET_ARG_NAME, S3BUCKET_ARG_VALUE,
				AWSACCID_ARG_NAME, AWSACCID_ARG_VALUE, AWSACCKEY_ARG_NAME, AWSACCKEY_ARG_VALUE, AWSREGION_ARG_NAME,
				AWSREGION_ARG_VALUE, TARGET_AZKEY_ARG_NAME, TARGET_AZKEY_ARG_VALUE, TARGET_BLOB_CONTAINER_ARG_NAME,
				TARGET_BLOB_CONTAINER_ARG_VALUE };

		return Arrays.asList(args);
	}

	private List<String> getInventoryArgs() {
		String[] args = { LP_ARG_NAME, LP_ARG_VALUE, OUTPUT_DIR_ARG_NAME, OUTPUT_DIR_ARG_VALUE, S3BUCKET_ARG_NAME,
				S3BUCKET_ARG_VALUE, AWSACCID_ARG_NAME, AWSACCID_ARG_VALUE, AWSACCKEY_ARG_NAME, AWSACCKEY_ARG_VALUE,
				AWSREGION_ARG_NAME, AWSREGION_ARG_VALUE, TARGET_AZKEY_ARG_NAME, TARGET_AZKEY_ARG_VALUE,
				TARGET_BLOB_CONTAINER_ARG_NAME, TARGET_BLOB_CONTAINER_ARG_VALUE, DIRECTION_ARG_NAME,
				DIRECTION_AWS_TO_AZURE_ARG_VALUE };

		return Arrays.asList(args);
	}

	private List<String> getInventoryAzureToAzureArgs() {
		String[] args = { LP_ARG_NAME, LP_ARG_VALUE, OUTPUT_DIR_ARG_NAME, OUTPUT_DIR_ARG_VALUE, TARGET_AZKEY_ARG_NAME,
				TARGET_AZKEY_ARG_VALUE, TARGET_BLOB_CONTAINER_ARG_NAME, TARGET_BLOB_CONTAINER_ARG_VALUE,
				SOURCE_AZKEY_ARG_NAME, SOURCE_AZKEY_ARG_VALUE, SOURCE_BLOB_CONTAINER_ARG_NAME,
				SOURCE_BLOB_CONTAINER_ARG_VALUE, DIRECTION_ARG_NAME, DIRECTION_AZURE_TO_AZURE_ARG_VALUE };

		return Arrays.asList(args);
	}

	private List<String> getAzureToAzureCopyArgs() {
		String[] args = { LP_ARG_NAME, LP_ARG_VALUE, TARGET_AZKEY_ARG_NAME, TARGET_AZKEY_ARG_VALUE,
				TARGET_BLOB_CONTAINER_ARG_NAME, TARGET_BLOB_CONTAINER_ARG_VALUE, SOURCE_AZKEY_ARG_NAME,
				SOURCE_AZKEY_ARG_VALUE, SOURCE_BLOB_CONTAINER_ARG_NAME, SOURCE_BLOB_CONTAINER_ARG_VALUE,
				CP_ARG_NAME, CP_ARG_VALUE };

		return Arrays.asList(args);
	}

	private List<String> getAwsSizingArgs() {
		String[] args = { LP_ARG_NAME, LP_ARG_VALUE, S3BUCKET_ARG_NAME, S3BUCKET_ARG_VALUE, AWSACCID_ARG_NAME,
				AWSACCID_ARG_VALUE, AWSACCKEY_ARG_NAME, AWSACCKEY_ARG_VALUE, AWSREGION_ARG_NAME, AWSREGION_ARG_VALUE };

		return Arrays.asList(args);
	}

	private List<String> getAzureSizingArgs() {
		String[] args = { LP_ARG_NAME, LP_ARG_VALUE, TARGET_AZKEY_ARG_NAME, TARGET_AZKEY_ARG_VALUE,
				TARGET_BLOB_CONTAINER_ARG_NAME, TARGET_BLOB_CONTAINER_ARG_VALUE };

		return Arrays.asList(args);
	}

	@Test
	public void testAzureSizingConfig() throws ParseException {
		String[] args = getAzureSizingArgs().toArray(new String[] {});

		Config config = ConfigBuilder.build(args, ConfigType.AZURE_SIZING);
		assertEquals(config.getListPartitions(), Integer.parseInt(LP_ARG_VALUE));
		assertEquals(config.getTargetAzureStorageKey(), TARGET_AZKEY_ARG_VALUE);
		assertEquals(config.getTargetContainer(), TARGET_BLOB_CONTAINER_ARG_VALUE);
	}

	@Test
	public void testAwsSizingConfig() throws ParseException {
		String[] args = getAwsSizingArgs().toArray(new String[] {});

		Config config = ConfigBuilder.build(args, ConfigType.AWS_SIZING);
		assertEquals(config.getListPartitions(), Integer.parseInt(LP_ARG_VALUE));
		assertEquals(config.getAwsAccountId(), AWSACCID_ARG_VALUE);
		assertEquals(config.getAwsAccountKey(), AWSACCKEY_ARG_VALUE);
		assertEquals(config.getAwsRegion(), AWSREGION_ARG_VALUE);
	}

	@Test
	public void testInventoryConfigAzureToAws() throws ParseException {
		List<String> inventoryArgs = new ArrayList<>(getInventoryArgs());
		inventoryArgs.remove(inventoryArgs.size() - 1);
		inventoryArgs.add(DIRECTION_AZURE_TO_AWS_ARG_VALUE);

		String[] args = inventoryArgs.toArray(new String[] {});

		Config config = ConfigBuilder.build(args, ConfigType.INVENTORY);
		assertEquals(config.getListPartitions(), Integer.parseInt(LP_ARG_VALUE));
		assertEquals(config.getOutputDir(), OUTPUT_DIR_ARG_VALUE);
		assertEquals(config.getDirection(), InventoryDirection.AZURE_TO_AWS);
		assertEquals(config.getSourceBucket(), S3BUCKET_ARG_VALUE);
		assertEquals(config.getAwsAccountId(), AWSACCID_ARG_VALUE);
		assertEquals(config.getAwsAccountKey(), AWSACCKEY_ARG_VALUE);
		assertEquals(config.getAwsRegion(), AWSREGION_ARG_VALUE);
		assertEquals(config.getTargetAzureStorageKey(), TARGET_AZKEY_ARG_VALUE);
		assertEquals(config.getTargetContainer(), TARGET_BLOB_CONTAINER_ARG_VALUE);
	}

	@Test
	public void testInventoryConfigAwsToAzure() throws ParseException {
		String[] args = getInventoryArgs().toArray(new String[] {});

		Config config = ConfigBuilder.build(args, ConfigType.INVENTORY);
		assertEquals(config.getListPartitions(), Integer.parseInt(LP_ARG_VALUE));
		assertEquals(config.getOutputDir(), OUTPUT_DIR_ARG_VALUE);
		assertEquals(config.getDirection(), InventoryDirection.AWS_TO_AZURE);
		assertEquals(config.getSourceBucket(), S3BUCKET_ARG_VALUE);
		assertEquals(config.getAwsAccountId(), AWSACCID_ARG_VALUE);
		assertEquals(config.getAwsAccountKey(), AWSACCKEY_ARG_VALUE);
		assertEquals(config.getAwsRegion(), AWSREGION_ARG_VALUE);
		assertEquals(config.getTargetAzureStorageKey(), TARGET_AZKEY_ARG_VALUE);
		assertEquals(config.getTargetContainer(), TARGET_BLOB_CONTAINER_ARG_VALUE);
	}

	@Test
	public void testInventoryConfigAzureToAzure() throws ParseException {
		String[] args = getInventoryAzureToAzureArgs().toArray(new String[] {});

		Config config = ConfigBuilder.build(args, ConfigType.INVENTORY);
		assertEquals(config.getListPartitions(), Integer.parseInt(LP_ARG_VALUE));
		assertEquals(config.getOutputDir(), OUTPUT_DIR_ARG_VALUE);
		assertEquals(config.getDirection(), InventoryDirection.AZURE_TO_AZURE);
		assertEquals(config.getTargetAzureStorageKey(), TARGET_AZKEY_ARG_VALUE);
		assertEquals(config.getTargetContainer(), TARGET_BLOB_CONTAINER_ARG_VALUE);
		assertEquals(config.getSourceAzureStorageKey(), SOURCE_AZKEY_ARG_VALUE);
		assertEquals(config.getSourceContainer(), SOURCE_BLOB_CONTAINER_ARG_VALUE);
	}

	@Test
	public void testCopyAzureToAzure() throws ParseException {
		String[] args = getAzureToAzureCopyArgs().toArray(new String[] {});

		Config config = ConfigBuilder.build(args, ConfigType.AZURE_TO_AZURE_COPY);
		assertEquals(config.getListPartitions(), Integer.parseInt(LP_ARG_VALUE));
		assertEquals(config.getTargetAzureStorageKey(), TARGET_AZKEY_ARG_VALUE);
		assertEquals(config.getTargetContainer(), TARGET_BLOB_CONTAINER_ARG_VALUE);
		assertEquals(config.getSourceAzureStorageKey(), SOURCE_AZKEY_ARG_VALUE);
		assertEquals(config.getSourceContainer(), SOURCE_BLOB_CONTAINER_ARG_VALUE);
		assertEquals(config.getCopyPartitions(), Integer.parseInt(CP_ARG_VALUE));
	}

	@Test
	public void testMigrationConfig() throws ParseException {
		String[] args = getMigrationArgs().toArray(new String[] {});

		Config config = ConfigBuilder.build(args, ConfigType.MIGRATION);
		assertEquals(config.getListPartitions(), Integer.parseInt(LP_ARG_VALUE));
		assertEquals(config.getCopyPartitions(), Integer.parseInt(CP_ARG_VALUE));
		assertEquals(config.getSourceBucket(), S3BUCKET_ARG_VALUE);
		assertEquals(config.getAwsAccountId(), AWSACCID_ARG_VALUE);
		assertEquals(config.getAwsAccountKey(), AWSACCKEY_ARG_VALUE);
		assertEquals(config.getAwsRegion(), AWSREGION_ARG_VALUE);
		assertEquals(config.getTargetAzureStorageKey(), TARGET_AZKEY_ARG_VALUE);
		assertEquals(config.getTargetContainer(), TARGET_BLOB_CONTAINER_ARG_VALUE);
	}

	@Test
	public void testMigrationConfigWithMoreArgs() throws ParseException {
		String[] args = getInventoryArgs().toArray(new String[] {});

		try {
			ConfigBuilder.build(args, ConfigType.MIGRATION);
		} catch (ParseException e) {
			assertEquals(e.getMessage(), "org.apache.commons.cli.UnrecognizedOptionException: Unrecognized option: -o");
			return;
		}

		fail("Should not reach here");
	}

	@Test
	public void testMissingArgsForMigration() {
		String[] args = {};

		try {
			ConfigBuilder.build(args, ConfigType.MIGRATION);
		} catch (ParseException e) {
			assertEquals(e.getMessage(),
					"org.apache.commons.cli.MissingOptionException: Missing required options: lp, s3bucket, awsaccid, awsacckey, awsregion, targetazkey, targetblobcontainer, cp");
			return;
		}

		fail("Should not reach here");
	}

	@Test
	public void testMissingArgsForInventory() {
		String[] args = {};

		try {
			ConfigBuilder.build(args, ConfigType.INVENTORY);
		} catch (ParseException e) {
			assertEquals("Unable to determine inventory direction from parameters: {}", e.getMessage());
			return;
		}

		fail("Should not reach here");
	}

	@Test
	public void testMissingArgsForAwsSizing() {
		String[] args = {};

		try {
			ConfigBuilder.build(args, ConfigType.AWS_SIZING);
		} catch (ParseException e) {
			assertEquals(e.getMessage(),
					"org.apache.commons.cli.MissingOptionException: Missing required options: lp, s3bucket, awsaccid, awsacckey, awsregion");
			return;
		}

		fail("Should not reach here");
	}

	@Test
	public void testMissingArgsForAzureBlobSizing() {
		String[] args = {};

		try {
			ConfigBuilder.build(args, ConfigType.AZURE_SIZING);
		} catch (ParseException e) {
			assertEquals(e.getMessage(),
					"org.apache.commons.cli.MissingOptionException: Missing required options: lp, targetazkey, targetblobcontainer");
			return;
		}

		fail("Should not reach here");
	}

	@Test
	public void testMissingArgsForAzureToAzureCopy() {
		String[] args = {};

		try {
			ConfigBuilder.build(args, ConfigType.AZURE_TO_AZURE_COPY);
		} catch (ParseException e) {
			assertEquals(e.getMessage(),
					"org.apache.commons.cli.MissingOptionException: Missing required options: lp, targetazkey, targetblobcontainer, sourceazkey, sourceblobcontainer, cp");
			return;
		}

		fail("Should not reach here");
	}

	@Test
	public void testMissingArgsForAzureToAzureInventory() {
		String[] args = { "azure-to-azure" };

		try {
			ConfigBuilder.build(args, ConfigType.INVENTORY);
		} catch (ParseException e) {
			assertEquals(
					"org.apache.commons.cli.MissingOptionException: Missing required options: lp, o, direction, targetazkey, targetblobcontainer, sourceazkey, sourceblobcontainer",
					e.getMessage());
			return;
		}

		fail("Should not reach here");
	}
}