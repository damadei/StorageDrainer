package com.microsoft.ocp.storage.drainer;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;

import com.microsoft.ocp.storage.drainer.config.Config;
import com.microsoft.ocp.storage.drainer.config.ConfigType;
import com.microsoft.ocp.storage.drainer.listing.AwsS3Client;
import com.microsoft.ocp.storage.drainer.listing.AzureBlobStorageClient;
import com.microsoft.ocp.storage.drainer.util.MapConversion;
import com.microsoft.ocp.storage.drainer.util.TextUtils;

import scala.Tuple2;

public class SizingJobDriverBase extends JobDriverBase {
	private static Logger logger = Logger.getLogger(SizingJobDriverBase.class);

	static void performSizing(Config config) {

		boolean isAzureSizing = config.getConfigType() == ConfigType.AZURE_SIZING;
		boolean isAwsSizng = !isAzureSizing;

		SparkConf conf = new SparkConf()
				.setAppName(isAzureSizing ? "StorageDrainer-Sizing-Azure" : "StorageDrainer-Sizing-AWS");

		if (isLocalEnv()) {
			conf.setMaster("local");
		}

		JavaSparkContext sc = new JavaSparkContext(conf);

		JavaRDD<String> linesRDD = getTopLevelFolders(sc, config);

		JavaPairRDD<String, Long> keys = linesRDD.flatMapToPair(new PairFlatMapFunction<String, String, Long>() {
			private static final long serialVersionUID = 985658371589272568L;

			@Override
			public Iterator<Tuple2<String, Long>> call(String prefix) throws Exception {
				Map<String, Long> objects = null;

				if (isAwsSizng) {
					AwsS3Client amazonClient = new AwsS3Client(config);
					objects = amazonClient.listObjects(prefix);
				} else {
					AzureBlobStorageClient blobClient = new AzureBlobStorageClient(config);
					objects = blobClient.listObjects(prefix);
				}

				return MapConversion.toTuple2List(objects).iterator();
			}
		});

		Function2<Tuple2<Long, Long>, Tuple2<String, Long>, Tuple2<Long, Long>> agg = new Function2<Tuple2<Long, Long>, Tuple2<String, Long>, Tuple2<Long, Long>>() {
			private static final long serialVersionUID = 6772763581160074418L;

			@Override
			public Tuple2<Long, Long> call(Tuple2<Long, Long> target, Tuple2<String, Long> fileKey) throws Exception {
				return new Tuple2<Long, Long>(target._1() + 1, target._2() + fileKey._2());
			}
		};

		Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Tuple2<Long, Long>> combineAgg = new Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Tuple2<Long, Long>>() {
			private static final long serialVersionUID = 9157515152531461963L;

			@Override
			public Tuple2<Long, Long> call(Tuple2<Long, Long> a, Tuple2<Long, Long> b) throws Exception {
				return new Tuple2<Long, Long>(a._1() + b._1(), a._2() + b._2());
			}
		};

		Tuple2<Long, Long> zeroVal = new Tuple2<Long, Long>(0L, 0L);

		Tuple2<Long, Long> result = keys.aggregate(zeroVal, agg, combineAgg);

		logger.info("############# The aggregate value is: " + result._1() + " files. Size: "
				+ TextUtils.readableFileSize(result._2()) + " (" + result._2() + " bytes) #############");

		sc.close();
	}

	private static boolean isLocalEnv() {
		return Config.isLocalEnv();
	}
}