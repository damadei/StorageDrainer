package com.microsoft.ocp.storage.drainer.listing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringUtils;
import com.microsoft.ocp.storage.drainer.config.Config;

public class AwsS3Client extends ObjectStorageClientBase implements Serializable {

	private static final long serialVersionUID = 6368380929673359611L;

	private transient Logger logger = null;

	private transient AmazonS3 s3client = null;

	private Config config;

	public AwsS3Client(Config config) {
		this.config = config;
	}

	private synchronized void ensureProperInit() {
		if (logger == null) {
			logger = Logger.getLogger(AwsS3Client.class.getCanonicalName());
		}

		if (s3client == null) {
			if (StringUtils.isNullOrEmpty(System.getProperty("aws.accessKeyId"))) {
				System.setProperty("aws.accessKeyId", config.getAwsAccountId());
			}

			if (StringUtils.isNullOrEmpty(System.getProperty("aws.secretKey"))) {
				System.setProperty("aws.secretKey", config.getAwsAccountKey());
			}

			this.s3client = new AmazonS3ClientFactory(config).createAmazonS3();
		}
	}

	public Map<String, Long> listObjects(String prefix) {
		return listObjects(prefix, null);
	}

	public Map<String, Long> listObjects(String prefix, String delimiter) {
		ensureProperInit();

		prefix = ensurePrefixLooksLikeDirectory(prefix);

		try {
			logger.info("Starting to list objects for prefix in AWS S3: " + prefix);
			ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(config.getSourceBucket());

			if (delimiter != null) {
				req = req.withDelimiter(delimiter);
			}

			if (prefix != null) {
				req = req.withPrefix(prefix);
			}

			ListObjectsV2Result result;
			Map<String, Long> resultMap = new HashMap<>();

			do {
				result = s3client.listObjectsV2(req);

				for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
					logger.debug(objectSummary.getKey());
					resultMap.put(objectSummary.getKey(), objectSummary.getSize());
				}

				req.setContinuationToken(result.getNextContinuationToken());

			} while (result.isTruncated() == true);

			logger.info("Found " + resultMap.size() + " objects in AWS S3 for prefix: " + prefix);

			return resultMap;

		} catch (AmazonServiceException ase) {
			String logMessage = "Caught an AmazonServiceException, " + "which means your request made it "
					+ System.lineSeparator() + "to Amazon S3, but was rejected with an error response "
					+ "for some reason. " + System.lineSeparator() + "Error Message:    " + ase.getMessage()
					+ System.lineSeparator() + "HTTP Status Code: " + ase.getStatusCode() + System.lineSeparator()
					+ "AWS Error Code:   " + ase.getErrorCode() + System.lineSeparator() + "Error Type:       "
					+ ase.getErrorType() + System.lineSeparator() + "Request ID:       " + ase.getRequestId();

			logger.error(logMessage, ase);

			throw new RuntimeException(ase);

		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, " + "which means the client encountered "
					+ "an internal error while trying to communicate" + " with S3, "
					+ "such as not being able to access the network.", ace);

			throw new RuntimeException(ace);
		}
	}

	public Set<String> listTopLevelFolders() {
		ensureProperInit();

		try {
			ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(config.getSourceBucket())
					.withDelimiter("/");

			ListObjectsV2Result result;

			Set<String> prefixes = new HashSet<>();

			do {
				result = s3client.listObjectsV2(req);
				prefixes.addAll(result.getCommonPrefixes());

				List<S3ObjectSummary> objs = result.getObjectSummaries();

				objs.stream().forEach(item -> {
					prefixes.add(item.getKey());
				});

			} while (result.isTruncated() == true);

			logger.info(
					"Found " + prefixes.size() + " top level dirs in AWS S3 for bucket: " + config.getSourceBucket());

			return prefixes;

		} catch (AmazonServiceException ase) {
			String logMessage = "Caught an AmazonServiceException, " + "which means your request made it "
					+ System.lineSeparator() + "to Amazon S3, but was rejected with an error response "
					+ "for some reason. " + System.lineSeparator() + "Error Message:    " + ase.getMessage()
					+ System.lineSeparator() + "HTTP Status Code: " + ase.getStatusCode() + System.lineSeparator()
					+ "AWS Error Code:   " + ase.getErrorCode() + System.lineSeparator() + "Error Type:       "
					+ ase.getErrorType() + System.lineSeparator() + "Request ID:       " + ase.getRequestId();

			logger.error(logMessage, ase);

			throw new RuntimeException(ase);

		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, " + "which means the client encountered "
					+ "an internal error while trying to communicate" + " with S3, "
					+ "such as not being able to access the network.", ace);

			throw new RuntimeException(ace);
		}
	}
}