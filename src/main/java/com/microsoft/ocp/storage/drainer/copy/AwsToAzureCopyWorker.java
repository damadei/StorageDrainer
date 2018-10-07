package com.microsoft.ocp.storage.drainer.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.StringUtils;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.ocp.storage.drainer.config.Config;

public class AwsToAzureCopyWorker implements Serializable {

	private static final long serialVersionUID = -3601299424483215830L;

	private transient Logger logger = null;

	private transient AmazonS3 s3client = null;

	private transient CloudStorageAccount storageAccount;

	private transient CloudBlobClient blobClient;

	private Config config;

	public AwsToAzureCopyWorker(Config config) {
		this.config = config;
	}

	private synchronized void ensureProperInit() {
		if (logger == null) {
			logger = Logger.getLogger(AwsToAzureCopyWorker.class);
		}

		if (s3client == null) {
			if (StringUtils.isNullOrEmpty(System.getProperty("aws.accessKeyId"))) {
				System.setProperty("aws.accessKeyId", config.getAwsAccountId());
			}

			if (StringUtils.isNullOrEmpty(System.getProperty("aws.secretKey"))) {
				System.setProperty("aws.secretKey", config.getAwsAccountKey());
			}

			s3client = AmazonS3ClientBuilder.standard().withRegion(config.getAwsRegion()).build();
		}
		try {
			if (storageAccount == null) {
				storageAccount = CloudStorageAccount.parse(config.getTargetAzureStorageKey());
			}

			if (blobClient == null) {
				blobClient = storageAccount.createCloudBlobClient();
				logger.info("Creating Azure Blob Client instance");
			}
		} catch (InvalidKeyException | URISyntaxException e) {
			logger.fatal("Invalid Azure Storage connectivity info", e);
			throw new IllegalArgumentException(e);
		}
	}

	public void copyFile(String fileKey, Long fileLen) throws IOException {
		ensureProperInit();

		try {
			CloudBlobContainer container = blobClient.getContainerReference(config.getTargetContainer());
			container.createIfNotExists();

			long start = System.currentTimeMillis();
			S3Object object = s3client.getObject(new GetObjectRequest(config.getSourceBucket(), fileKey));

			try (InputStream objectData = object.getObjectContent()) {
				CloudBlockBlob blob = container.getBlockBlobReference(fileKey);
				blob.deleteIfExists();
				blob.upload(objectData, fileLen);

				long end = System.currentTimeMillis();
				logger.info("Time to copy file " + fileKey + ": " + (end - start) + " ms. (Size: " + fileLen
						+ ") - Thread: " + Thread.currentThread().getName());
			}

		} catch (Exception e) {
			logger.fatal("ERROR - Error during copy of file: " + fileKey, e);
			throw new RuntimeException(e);
		}
	}
}
