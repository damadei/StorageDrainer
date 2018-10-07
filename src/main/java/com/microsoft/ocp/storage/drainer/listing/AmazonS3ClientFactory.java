package com.microsoft.ocp.storage.drainer.listing;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.microsoft.ocp.storage.drainer.config.Config;

public class AmazonS3ClientFactory {

	private Config config;

	public AmazonS3ClientFactory(Config config) {
		this.config = config;
	}

	public AmazonS3 createAmazonS3() {
		return AmazonS3ClientBuilder.standard().withRegion(config.getAwsRegion()).build();
	}
}
