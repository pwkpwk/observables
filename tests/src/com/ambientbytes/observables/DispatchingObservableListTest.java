package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class DispatchingObservableListTest {

	@Test
	public void newDispatchingObservableListCopiesData() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		mol.mutator().add(Integer.valueOf(10));
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);

		assertEquals(1, dol.getSize());
		assertEquals(10, dol.getAt(0).intValue());
	}

}
