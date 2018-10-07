package com.microsoft.ocp.storage.drainer;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ConfigBuilder;
import com.microsoft.ocp.storage.drainer.config.ConfigType;
import com.microsoft.ocp.storage.drainer.config.ParseException;

public class AwsSizingJobDriver extends SizingJobDriverBase {

	public static void main(String[] args) throws ParseException {
		Config config = ConfigBuilder.build(args, ConfigType.AWS_SIZING);

		performSizing(config);
	}
}