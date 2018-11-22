package com.microsoft.ocp.storage.drainer.util;

import java.util.HashMap;
import java.util.Map;

public class MapFilter {

	public static Map<String, Long> filter(final Map<String, Long> src, final Map<String, Long> target) {
		return filter(src, target, false);
	}

	public static Map<String, Long> filter(final Map<String, Long> src, final Map<String, Long> target,
			boolean compareByNameOnly) {

		if (target == null || target.size() == 0) {
			return src;
		} else {

			Map<String, Long> resultMap = new HashMap<>();

			src.entrySet().forEach(entry -> {
				Long val = target.get(entry.getKey());

				if (compareByNameOnly && val == null) {
					resultMap.put(entry.getKey(), entry.getValue());
				} else if (!compareByNameOnly) {
					if (val == null || !val.equals(entry.getValue())) {
						resultMap.put(entry.getKey(), entry.getValue());
					}
				}
			});

			return resultMap;
		}
	}
}
