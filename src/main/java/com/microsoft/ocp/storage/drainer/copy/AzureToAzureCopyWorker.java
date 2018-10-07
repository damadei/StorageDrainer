package com.microsoft.ocp.storage.drainer.copy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.apache.log4j.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.ocp.storage.drainer.config.Config;

public class AzureToAzureCopyWorker implements Serializable {

	private static final long serialVersionUID = -3601299424483215830L;

	private transient Logger logger = null;

	private transient CloudStorageAccount targetStorageAccount;
	private transient CloudBlobClient targetBlobClient;

	private transient CloudStorageAccount sourceStorageAccount;
	private transient CloudBlobClient sourceBlobClient;
	
	private Config config;

	public AzureToAzureCopyWorker(Config config) {
		this.config = config;
	}

	private synchronized void ensureProperInit() {
		if (logger == null) {
			logger = Logger.getLogger(AzureToAzureCopyWorker.class);
		}

		try {
			if (targetStorageAccount == null) {
				targetStorageAccount = CloudStorageAccount.parse(config.getTargetAzureStorageKey());
			}

			if (targetBlobClient == null) {
				targetBlobClient = targetStorageAccount.createCloudBlobClient();
				logger.info("Creating target Azure Blob Client instance");
			}
		} catch (InvalidKeyException | URISyntaxException e) {
			logger.fatal("Invalid Target Azure Storage connectivity info", e);
			throw new IllegalArgumentException(e);
		}
		
		try {
			if (sourceStorageAccount == null) {
				sourceStorageAccount = CloudStorageAccount.parse(config.getSourceAzureStorageKey());
			}

			if (sourceBlobClient == null) {
				sourceBlobClient = sourceStorageAccount.createCloudBlobClient();
				logger.info("Creating source Azure Blob Client instance");
			}
		} catch (InvalidKeyException | URISyntaxException e) {
			logger.fatal("Invalid Source Azure Storage connectivity info", e);
			throw new IllegalArgumentException(e);
		}
	}

	public void copyFile(String fileKey, Long fileLen) throws IOException {
		ensureProperInit();

		try {
			CloudBlobContainer targetContainer = targetBlobClient.getContainerReference(config.getTargetContainer());
			targetContainer.createIfNotExists();

			long start = System.currentTimeMillis();

			CloudBlobContainer sourceContainer = sourceBlobClient.getContainerReference(config.getSourceContainer());
			CloudBlockBlob object = sourceContainer.getBlockBlobReference(fileKey);

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			object.download(buffer);

			ByteArrayInputStream inputBuffer = new ByteArrayInputStream(buffer.toByteArray());

			CloudBlockBlob blob = targetContainer.getBlockBlobReference(fileKey);
			blob.deleteIfExists();
			blob.upload(inputBuffer, fileLen);

			long end = System.currentTimeMillis();
			logger.info("Time to copy file " + fileKey + ": " + (end - start) + " ms. (Size: " + fileLen
					+ ") - Thread: " + Thread.currentThread().getName());

		} catch (Exception e) {
			logger.fatal("ERROR - Error during copy of file: " + fileKey, e);
			throw new RuntimeException(e);
		}
	}
}
