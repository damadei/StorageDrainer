package com.microsoft.ocp.storage.drainer;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ConfigBuilder;
import com.microsoft.ocp.storage.drainer.config.ConfigType;
import com.microsoft.ocp.storage.drainer.config.ParseException;

public class AzureSizingJobDriver extends SizingJobDriverBase {

	public static void main(String[] args) throws ParseException {
		Config config = ConfigBuilder.build(args, ConfigType.AZURE_SIZING);

		performSizing(config);
	}
}