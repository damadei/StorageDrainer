package com.microsoft.ocp.storage.drainer;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ConfigBuilder;
import com.microsoft.ocp.storage.drainer.config.ConfigType;
import com.microsoft.ocp.storage.drainer.config.InventoryDirection;
import com.microsoft.ocp.storage.drainer.config.ParseException;
import com.microsoft.ocp.storage.drainer.listing.AwsS3Client;
import com.microsoft.ocp.storage.drainer.listing.AzureBlobStorageClient;
import com.microsoft.ocp.storage.drainer.listing.AzureBlobStorageClientType;
import com.microsoft.ocp.storage.drainer.util.MapConversion;
import com.microsoft.ocp.storage.drainer.util.MapFilter;

import scala.Tuple2;

public class InventoryJobDriver extends JobDriverBase {
	private static Logger logger = Logger.getLogger(InventoryJobDriver.class);

	public static void main(String[] args) throws FileNotFoundException, ParseException {

		SparkConf conf = new SparkConf().setAppName("StorageDrainer-InventoryJob");

		if (isLocalEnv()) {
			conf.setMaster("local");
		}

		JavaSparkContext sc = new JavaSparkContext(conf);
	
		Config config = ConfigBuilder.build(args, ConfigType.INVENTORY);

		JavaRDD<String> linesRDD = getTopLevelFolders(sc, config);

		logger.info("Expanding list to get files inside each bucket");
		JavaPairRDD<String, Long> keysToCopy = linesRDD.flatMapToPair(new PairFlatMapFunction<String, String, Long>() {
			private static final long serialVersionUID = 985658371589272568L;

			@Override
			public Iterator<Tuple2<String, Long>> call(String prefix) throws Exception {
				Map<String, Long> sourceObjects = null;
				Map<String, Long> targetObjects = null;

				AwsS3Client awsS3Client = new AwsS3Client(config);
				AzureBlobStorageClient targetBlobStorageClient = new AzureBlobStorageClient(config,
						AzureBlobStorageClientType.TARGET);
				AzureBlobStorageClient sourceBlobStorageClient = new AzureBlobStorageClient(config,
						AzureBlobStorageClientType.SOURCE);

				Map<String, Long> resultMap = null;
				if (config.getDirection() == InventoryDirection.AWS_TO_AZURE) {
					sourceObjects = awsS3Client.listObjects(prefix);
					targetObjects = targetBlobStorageClient.listObjects(prefix);

				} else if (config.getDirection() == InventoryDirection.AZURE_TO_AWS) {
					sourceObjects = targetBlobStorageClient.listObjects(prefix);
					targetObjects = awsS3Client.listObjects(prefix);

				} else if (config.getDirection() == InventoryDirection.AZURE_TO_AZURE) {
					sourceObjects = sourceBlobStorageClient.listObjects(prefix);
					targetObjects = targetBlobStorageClient.listObjects(prefix);

				} else {
					throw new IllegalArgumentException("Unsupported direction type: " + config.getDirection());
				}

				resultMap = MapFilter.filter(sourceObjects, targetObjects);
				logger.info("After filtering, found " + resultMap.size() + " different file(s) for direction "
						+ config.getDirection().name());

				return MapConversion.toTuple2List(resultMap).iterator();
			}
		});

		JavaRDD<String> paths = keysToCopy.map(new Function<Tuple2<String, Long>, String>() {

			private static final long serialVersionUID = -6113205924586687552L;

			public String call(Tuple2<String, Long> fileTup) throws Exception {
				return StringUtils.substringBefore(fileTup._1, "/");
			};
		});

		JavaPairRDD<String, Integer> pathsAndCount = paths.mapToPair(new PairFunction<String, String, Integer>() {

			private static final long serialVersionUID = 5394894863563290446L;

			public Tuple2<String, Integer> call(String x) {
				return new Tuple2<String, Integer>(x, 1);
			}
		});

		JavaPairRDD<String, Integer> pathsAndCountAggregated = pathsAndCount
				.reduceByKey(new Function2<Integer, Integer, Integer>() {

					private static final long serialVersionUID = -397274309892294644L;

					@Override
					public Integer call(Integer a, Integer b) throws Exception {
						return a + b;
					}
				});

		pathsAndCountAggregated = pathsAndCountAggregated.repartition(1);
		keysToCopy = keysToCopy.repartition(1);

		String basePath = config.getOutputDir() + "/inventory/" + System.currentTimeMillis();

		pathsAndCountAggregated.saveAsTextFile(basePath + "/aggregate");
		keysToCopy.saveAsTextFile(basePath + "/detailed");

		sc.close();
	}

	private static boolean isLocalEnv() {
		return Config.isLocalEnv();
	}
}