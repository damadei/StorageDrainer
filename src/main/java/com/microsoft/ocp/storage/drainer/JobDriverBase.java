package com.microsoft.ocp.storage.drainer;

import java.util.ArrayList;
import java.util.Set;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.InventoryDirection;
import com.microsoft.ocp.storage.drainer.listing.AwsS3Client;
import com.microsoft.ocp.storage.drainer.listing.AzureBlobStorageClient;
import com.microsoft.ocp.storage.drainer.listing.AzureBlobStorageClientType;

public class JobDriverBase {

	public static JavaRDD<String> getTopLevelFolders(JavaSparkContext sc, Config config) {
		if (config.isHasInputFile()) {
			JavaRDD<String> linesRDD = sc.textFile(config.getInputFile(), config.getListPartitions());
			return linesRDD;

		} else {
			Set<String> topLevelFolders = null;

			if ((config.isInventory() && config.getDirection() == InventoryDirection.AZURE_TO_AWS)
					|| config.isAzureSizing()) {
				AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient(config);
				topLevelFolders = blobStorageClient.listTopLevelFolders();

			} else if (config.isMigration() || config.isAwsSizing()
					|| (config.isInventory() && config.getDirection() == InventoryDirection.AWS_TO_AZURE)) {
				AwsS3Client awsS3Client = new AwsS3Client(config);
				topLevelFolders = awsS3Client.listTopLevelFolders();

			} else if (config.isAzureToAzureCopy()
					|| (config.isInventory() && config.getDirection() == InventoryDirection.AZURE_TO_AZURE)) {
				AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient(config,
						AzureBlobStorageClientType.SOURCE);
				topLevelFolders = blobStorageClient.listTopLevelFolders();
			}

			JavaRDD<String> topLevelDirsRDD = sc.parallelize(new ArrayList<String>(topLevelFolders),
					config.getListPartitions());
			return topLevelDirsRDD;
		}
	}

}
