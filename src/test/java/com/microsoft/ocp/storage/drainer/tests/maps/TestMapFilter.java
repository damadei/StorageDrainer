package com.microsoft.ocp.storage.drainer.tests.maps;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.ocp.storage.drainer.util.MapFilter;

public class TestMapFilter {

	private Map<String, Long> src = null;

	@Before
	public void setupTest() {
		src = new HashMap<>();
		src.put("/myfiles/1.txt", 100L);
		src.put("/myfiles/2.txt", 200L);
		src.put("/myfiles/3.txt", 300L);
	}

	@Test
	public void testEmptyDestMap() {
		Map<String, Long> tgt = new HashMap<>();
		Map<String, Long> resultMap = MapFilter.filter(src, tgt);
		assertEquals(resultMap.size(), src.size());
		assertEquals(resultMap, src);
	}

	@Test
	public void testOneItemInTargetMap() {
		Map<String, Long> tgt = new HashMap<>();
		tgt.put("/myfiles/3.txt", 300L);
		
		Map<String, Long> resultMap = MapFilter.filter(src, tgt);
		assertEquals(resultMap.size(), (src.size()-1));
	}

	@Test
	public void testAllItemsEqual() {
		Map<String, Long> tgt = new HashMap<>(src);
		Map<String, Long> resultMap = MapFilter.filter(src, tgt);
		assertEquals(resultMap.size(), 0);
	}

	@Test
	public void testItemMismatchSize() {
		Map<String, Long> tgt = new HashMap<>(src);
		src.put("/myfiles/1.txt", 100L);
		src.put("/myfiles/2.txt", 200L);
		src.put("/myfiles/3.txt", 50L); //size mismatch

		Map<String, Long> resultMap = MapFilter.filter(src, tgt);
		assertEquals(resultMap.size(), (src.size()-2));
	}

	@Test
	public void testAllSizesMismatch() {
		Map<String, Long> tgt = new HashMap<>(src);
		tgt.entrySet().stream().forEach(entry -> {
			entry.setValue(entry.getValue()+1);
		});

		Map<String, Long> resultMap = MapFilter.filter(src, tgt);
		assertEquals(resultMap.size(), src.size());
	}

	@Test
	public void testByNameOnly() {
		Map<String, Long> tgt = new HashMap<>(src);
		tgt.entrySet().stream().forEach(entry -> {
			entry.setValue(entry.getValue()+1);
		});

		Map<String, Long> resultMap = MapFilter.filter(src, tgt, true);
		assertEquals(resultMap.size(), 0);
	}
	
	@Test
	public void testByNameWithDifferentItems() {
		Map<String, Long> tgt = new HashMap<>();

		Map<String, Long> resultMap = MapFilter.filter(src, tgt, true);
		assertEquals(resultMap.size(), 3);
	}
}
