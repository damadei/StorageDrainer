package com.microsoft.ocp.storage.drainer;

import java.io.FileNotFoundException;
import java.util.Map;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ParseException;
import com.microsoft.ocp.storage.drainer.util.MapFilter;
import com.microsoft.ocp.storage.drainer.worker.AzureToAzureCopyWorker;

public class AzureToAzureGZipDriver extends AzureToAzureCopyDriverBase {

	private static final long serialVersionUID = -8090511338738166033L;

	public static void main(String[] args) throws ParseException, FileNotFoundException {
		AzureToAzureGZipDriver driver = new AzureToAzureGZipDriver();
		driver.run(args, "StorageDrainer-AzureToAzureGZip");
	}

	@Override
	protected AzureToAzureCopyWorker newWorkerInstance(Config config) {
		return new AzureToAzureCopyWorker(config, true);
	}
	
	@Override
	protected Map<String, Long> filter(Map<String, Long> source, Map<String, Long> target) {
		return MapFilter.filter(source, target, true);
	}

}