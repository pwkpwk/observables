package com.ambientbytes.observables;

import static org.junit.Assert.*;

import org.junit.Test;

public class MutableObservableListTests {

	@Test
	public void newAdd2ItemsAdded() {
		MutableObservableList<Object> mol = new MutableObservableList<>();
		Object value1 = new Object();
		Object value2 = new Object();

		mol.insert(0, value1);
		mol.insert(1, value2);

		assertEquals(2, mol.getSize());
		assertSame(value1, mol.getAt(0));
		assertSame(value2, mol.getAt(1));
	}
	
	@Test
	public void removeItemRemoved() {
		MutableObservableList<Object> mol = new MutableObservableList<>();
		Object value1 = new Object();
		Object value2 = new Object();

		mol.insert(0, value1);
		mol.insert(1, value2);
		mol.remove(0, 1);

		assertEquals(1, mol.getSize());
		assertSame(value2, mol.getAt(0));
	}
	
	@Test
	public void clearCleared() {
		MutableObservableList<Object> mol = new MutableObservableList<>();
		Object value1 = new Object();
		Object value2 = new Object();

		mol.insert(0, value1);
		mol.insert(1, value2);
		mol.clear();

		assertEquals(0, mol.getSize());
	}

}
