package com.microsoft.ocp.storage.drainer;

import java.io.FileNotFoundException;
import java.util.Map;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ParseException;
import com.microsoft.ocp.storage.drainer.util.MapFilter;
import com.microsoft.ocp.storage.drainer.worker.AzureToAzureCopyWorker;

public class AzureToAzureCopyDriver extends AzureToAzureCopyDriverBase {

	private static final long serialVersionUID = -368414447871640276L;

	public static void main(String[] args) throws ParseException, FileNotFoundException {
		AzureToAzureCopyDriver driver = new AzureToAzureCopyDriver();
		driver.run(args, "StorageDrainer-AzureToAzureCopy");
	}

	@Override
	protected AzureToAzureCopyWorker newWorkerInstance(Config config) {
		return new AzureToAzureCopyWorker(config, false);
	}

	@Override
	protected Map<String, Long> filter(Map<String, Long> source, Map<String, Long> target) {
		return MapFilter.filter(source, target, false);
	}
}