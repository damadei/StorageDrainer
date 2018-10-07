package com.microsoft.ocp.storage.drainer;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ConfigBuilder;
import com.microsoft.ocp.storage.drainer.config.ConfigType;
import com.microsoft.ocp.storage.drainer.config.ParseException;
import com.microsoft.ocp.storage.drainer.copy.AzureToAzureCopyWorker;
import com.microsoft.ocp.storage.drainer.listing.AzureBlobStorageClient;
import com.microsoft.ocp.storage.drainer.listing.AzureBlobStorageClientType;
import com.microsoft.ocp.storage.drainer.util.MapConversion;
import com.microsoft.ocp.storage.drainer.util.MapFilter;

import scala.Tuple2;

public class AzureToAzureCopyDriver extends JobDriverBase {
	private static Logger logger = Logger.getLogger(AzureToAzureCopyDriver.class);

	public static void main(String[] args) throws ParseException, FileNotFoundException {

		SparkConf conf = new SparkConf().setAppName("StorageDrainer-AzureToAzureCopy");

		if (isLocalEnv()) {
			conf.setMaster("local");
		}

		JavaSparkContext sc = new JavaSparkContext(conf);

		Config config = ConfigBuilder.build(args, ConfigType.AZURE_TO_AZURE_COPY);

		JavaRDD<String> linesRDD = getTopLevelFolders(sc, config);

		logger.info("Expanding list to get files inside each bucket");
		JavaPairRDD<String, Long> keysToCopy = linesRDD.flatMapToPair(new PairFlatMapFunction<String, String, Long>() {
			private static final long serialVersionUID = 985658371589272568L;

			@Override
			public Iterator<Tuple2<String, Long>> call(String prefix) throws Exception {
				AzureBlobStorageClient sourceClient = new AzureBlobStorageClient(config,
						AzureBlobStorageClientType.SOURCE);
				Map<String, Long> sourceObjects = sourceClient.listObjects(prefix);

				AzureBlobStorageClient targetClient = new AzureBlobStorageClient(config,
						AzureBlobStorageClientType.TARGET);
				Map<String, Long> targetObjects = targetClient.listObjects(prefix);

				Map<String, Long> resultMap = MapFilter.filter(sourceObjects, targetObjects);

				logger.info("After filtering, found " + resultMap.size()
						+ " objects to copy from source to target for prefix " + prefix);

				return MapConversion.toTuple2List(resultMap).iterator();
			}
		});

		keysToCopy = keysToCopy.repartition(config.getCopyPartitions());

		AzureToAzureCopyWorker worker = new AzureToAzureCopyWorker(config);

		keysToCopy.foreach(new VoidFunction<Tuple2<String, Long>>() {
			private static final long serialVersionUID = 2545000124195992817L;

			@Override
			public void call(Tuple2<String, Long> fileKey) throws Exception {
				worker.copyFile(fileKey._1(), fileKey._2());
			}
		});

		sc.close();
	}

	private static boolean isLocalEnv() {
		return Config.isLocalEnv();
	}
}