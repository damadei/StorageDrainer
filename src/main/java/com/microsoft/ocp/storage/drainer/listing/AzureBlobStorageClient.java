package com.microsoft.ocp.storage.drainer.listing;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.ocp.storage.drainer.config.Config;

public class AzureBlobStorageClient extends ObjectStorageClientBase implements Serializable {

	private static final long serialVersionUID = -591430588329523661L;

	private transient Logger logger = Logger.getLogger(AzureBlobStorageClient.class.getCanonicalName());

	private transient CloudStorageAccount storageAccount;

	private transient CloudBlobClient blobClient;

	private Config config;

	private String containerName;

	private AzureBlobStorageClientType type;

	public AzureBlobStorageClient(Config config) {
		this(config, AzureBlobStorageClientType.TARGET);
	}

	public AzureBlobStorageClient(Config config, AzureBlobStorageClientType type) {
		this.config = config;
		this.type = type;
	}

	private void ensureInitialized() {
		try {
			if (storageAccount == null) {
				String azureStorageKey = null;

				if (type == AzureBlobStorageClientType.SOURCE) {
					azureStorageKey = config.getSourceAzureStorageKey();
					this.containerName = config.getSourceContainer();
				} else {
					azureStorageKey = config.getTargetAzureStorageKey();
					this.containerName = config.getTargetContainer();
				}

				storageAccount = CloudStorageAccount.parse(azureStorageKey);
			}

			if (blobClient == null) {
				blobClient = storageAccount.createCloudBlobClient();
			}
		} catch (InvalidKeyException | URISyntaxException e) {
			logger.fatal("Invalid Azure Storage connectivity info", e);
			throw new IllegalArgumentException(e);
		}
	}

	public Map<String, Long> listObjects(String prefix) {
		ensureInitialized();

		prefix = ensurePrefixLooksLikeDirectory(prefix);

		logger.info("Starting to list objects for prefix: " + prefix + " in Azure");

		Map<String, Long> resultMap = new HashMap<>();
		try {
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			if (!container.exists()) {
				container.createIfNotExists();

			} else {
				Iterable<ListBlobItem> listIterable = container.listBlobs(prefix, true);
				listIterable.forEach(x -> {
					CloudBlockBlob blockBlob = (CloudBlockBlob) x;
					Long blobLen = blockBlob.getProperties().getLength();
					String name = blockBlob.getName();

					resultMap.put(name, blobLen);
				});

			}

			logger.info("Found " + resultMap.size() + " objects in Azure blob for prefix: " + prefix);
			return resultMap;

		} catch (URISyntaxException use) {
			logger.error("URI exception listing blobs", use);
			throw new RuntimeException(use);

		} catch (StorageException se) {
			logger.error("Error listing blobs in Azure", se);
			logger.error("Error listing blobs in Azure. Extended info: " + se.getExtendedErrorInformation());
			throw new RuntimeException(se);
		}
	}

	public Set<String> listTopLevelFolders() {
		ensureInitialized();

		Set<String> topLevelFolders = new TreeSet<>();
		try {
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			if (!container.exists()) {
				container.createIfNotExists();

			} else {
				Iterable<ListBlobItem> listIterable = container.listBlobs();
				listIterable.forEach(x -> {
					if (x instanceof CloudBlobDirectory) {
						topLevelFolders.add(((CloudBlobDirectory) x).getPrefix());
					}
				});
			}

			logger.info("Found " + topLevelFolders.size() + " top level folders");
			return topLevelFolders;

		} catch (URISyntaxException use) {
			logger.error("URI exception listing top level folders in Azure", use);
			throw new RuntimeException(use);

		} catch (StorageException se) {
			logger.error("Error listing top level folders in Azure", se);
			logger.error(
					"Error listing top level folders in Azure. Extended info: " + se.getExtendedErrorInformation());
			throw new RuntimeException(se);
		}
	}
}