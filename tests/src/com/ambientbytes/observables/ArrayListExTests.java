package com.ambientbytes.observables;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ArrayListExTests {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void removeRangeAtBeginningRemoves() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.remove(0, 3);
		
		assertEquals(3, list.size());
		assertEquals(4, list.get(0).intValue());
		assertEquals(5, list.get(1).intValue());
		assertEquals(6, list.get(2).intValue());
	}

	@Test
	public void removeRangeAtEndRemoves() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.remove(3, 3);
		
		assertEquals(3, list.size());
		assertEquals(1, list.get(0).intValue());
		assertEquals(2, list.get(1).intValue());
		assertEquals(3, list.get(2).intValue());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void removeRangeBeyondEndThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.remove(3, 4);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void removeNegativeLengthThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.remove(3, -2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void removeNegativeStartThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.remove(-2, 2);
	}
	
	@Test
	public void moveUpNoOverlapMoves() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(0, 4, 2);
		assertEquals(3, list.get(0).intValue());
		assertEquals(4, list.get(1).intValue());
		assertEquals(5, list.get(2).intValue());
		assertEquals(6, list.get(3).intValue());
		assertEquals(1, list.get(4).intValue());
		assertEquals(2, list.get(5).intValue());
	}
	
	@Test
	public void moveUpOverlapMoves() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(0, 2, 4);
		assertEquals(5, list.get(0).intValue());
		assertEquals(6, list.get(1).intValue());
		assertEquals(1, list.get(2).intValue());
		assertEquals(2, list.get(3).intValue());
		assertEquals(3, list.get(4).intValue());
		assertEquals(4, list.get(5).intValue());
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveUpTooMuchThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(0, 2, 10);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveUpTooFarThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(0, 5, 2);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveNegativeSourceThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>(6);
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(-3, 2, 2);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveNegativeDestinationThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(3, -2, 2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void moveNegativeLengthThrows() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(0, 5, -2);
	}
	
	@Test
	public void moveDownNoOverlapMoves() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(4, 0, 2);
		assertEquals(5, list.get(0).intValue());
		assertEquals(6, list.get(1).intValue());
		assertEquals(1, list.get(2).intValue());
		assertEquals(2, list.get(3).intValue());
		assertEquals(3, list.get(4).intValue());
		assertEquals(4, list.get(5).intValue());
	}
	
	@Test
	public void moveDownOverlapMoves() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(2, 0, 3);
		assertEquals(3, list.get(0).intValue());
		assertEquals(4, list.get(1).intValue());
		assertEquals(5, list.get(2).intValue());
		assertEquals(1, list.get(3).intValue());
		assertEquals(2, list.get(4).intValue());
		assertEquals(6, list.get(5).intValue());
	}
	
	@Test
	public void moveSamePlaceNoChanges() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(2, 2, 3);
		assertEquals(1, list.get(0).intValue());
		assertEquals(2, list.get(1).intValue());
		assertEquals(3, list.get(2).intValue());
		assertEquals(4, list.get(3).intValue());
		assertEquals(5, list.get(4).intValue());
		assertEquals(6, list.get(5).intValue());
	}
	
	@Test
	public void moveZeroLengthNoChanges() {
		ArrayListEx<Integer> list = new ArrayListEx<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		list.move(2, 3, 0);
		assertEquals(1, list.get(0).intValue());
		assertEquals(2, list.get(1).intValue());
		assertEquals(3, list.get(2).intValue());
		assertEquals(4, list.get(3).intValue());
		assertEquals(5, list.get(4).intValue());
		assertEquals(6, list.get(5).intValue());
	}

}
