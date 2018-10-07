package com.microsoft.ocp.storage.drainer.tests.maps;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.ocp.storage.drainer.util.MapConversion;
import com.microsoft.ocp.storage.drainer.util.MapFilter;

import static org.junit.Assert.*;

import scala.Tuple2;

public class TestMapConversion {

	private Map<String, Long> src = null;

	@Before
	public void setupTest() {
		src = new HashMap<>();
		src.put("/myfiles/1.txt", 100L);
		src.put("/myfiles/2.txt", 200L);
		src.put("/myfiles/3.txt", 300L);
	}

	@Test
	public void testConversion() {
		List<Tuple2<String, Long>> resultList = MapConversion.toTuple2List(src);

		assertTrue(resultList.size() == src.size());
		System.out.println(resultList);

		Optional<Map.Entry<String, Long>> first = src.entrySet().stream().findFirst();

		boolean[] results = {false};
			
		resultList.stream().forEach(tuple -> {
			if(tuple._1.equals(first.get().getKey()) && tuple._2.equals(first.get().getValue())) {
				results[0] = true;
			}
		});

		assertTrue(results[0]);
	}
}