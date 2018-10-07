package com.microsoft.ocp.storage.drainer.util;

import scala.Tuple2;

import java.util.*;

public class MapConversion {

	public static <T1, T2> List<Tuple2<T1, T2>> toTuple2List(final Map<T1, T2> src) {

		List<Tuple2<T1, T2>> resultList = new ArrayList<>();

		if (src != null && src.size() > 0) {
			src.entrySet().stream().forEach(entry -> {
				Tuple2<T1, T2> tuple = new Tuple2<T1, T2>(entry.getKey(), entry.getValue());
				resultList.add(tuple);
			});
		}
		
		return resultList;
	}
}
