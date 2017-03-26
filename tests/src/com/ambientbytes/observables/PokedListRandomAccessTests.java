package com.ambientbytes.observables;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PokedListRandomAccessTests {

	@Before
	public void setUp() throws Exception {
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void newPokedListRandomAccessNegativeHoleIndexThrows() {
		new PokedListRandomAccess<>(new ArrayList<Integer>(), -1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void newPokedListRandomAccessHoleIndexTooLargeThrows() {
		new PokedListRandomAccess<>(new ArrayList<Integer>(), 1);
	}

	@Test
	public void getSizeReturnsReducedSize() {
		List<Integer> data = new ArrayList<>();
		data.add(1);
		data.add(2);
		data.add(3);
		data.add(4);
		IRandomAccess<Integer> ra = new PokedListRandomAccess<>(data, 1);
		
		assertEquals(data.size() - 1, ra.size());
	}

	@Test
	public void getGetsCorrectValues() {
		List<Integer> data = new ArrayList<>();
		data.add(1);
		data.add(2);
		data.add(3);
		data.add(4);
		IRandomAccess<Integer> ra = new PokedListRandomAccess<>(data, 1);
		
		assertEquals(1, ra.get(0).intValue());
		assertEquals(3, ra.get(1).intValue());
		assertEquals(4, ra.get(2).intValue());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getBeyondSizeThrows() {
		List<Integer> data = new ArrayList<>();
		data.add(1);
		data.add(2);
		data.add(3);
		data.add(4);
		IRandomAccess<Integer> ra = new PokedListRandomAccess<>(data, 1);
		
		ra.get(3);
	}
}
