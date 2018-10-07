package com.microsoft.ocp.storage.drainer.util;

import java.util.HashMap;
import java.util.Map;

public class MapFilter {

	public static Map<String, Long> filter(final Map<String, Long> src, final Map<String, Long> target) {

		if (target == null || target.size() == 0) {
			return src;
		} else {

			Map<String, Long> resultMap = new HashMap<>();

			src.entrySet().forEach(entry -> {
				Long val = target.get(entry.getKey());

				if (val == null || !val.equals(entry.getValue())) {
					resultMap.put(entry.getKey(), entry.getValue());
				}
			});

			return resultMap;
		}
	}
}
