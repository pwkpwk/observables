package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MappingReadOnlyObservableListTests {
	
	private static final class IntegerToStringMapper implements IItemMapper<Integer, String> {
		@Override
		public String map(Integer item) {
			return "item:" + item.toString();
		}
	}
	
	@Mock
	IListObserver<String> stringObserver;
	
	@Captor
	ArgumentCaptor<Collection<String>> stringsCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newMappingReadOnlyObservableListAddsMappedItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:1", "item:2", "item:3" }) {
			assertEquals(s, mol.getAt(index++));
		}
	}

	@Test
	public void addToSourceAddsMappedItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:1", "item:2", "item:3" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(3)).added(anyInt(), anyInt());
	}

	@Test
	public void removeFromSourceRemovesMappedItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);
		
		ol.mutator().remove(0, 2);
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:3" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).removing(eq(0), eq(2));
		verify(stringObserver, times(1)).removed(eq(0), eq(2));
	}

	@Test
	public void moveUpNoOverlapMovesMappedItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(0);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(6);
		ol.mutator().add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);
		
		ol.mutator().move(0, 4, 2);
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:2", "item:3", "item:4", "item:5", "item:0", "item:1", "item:6", "item:7" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(0), eq(4), eq(2));
	}

	@Test
	public void moveUpOverlapMovesMappedItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(0);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(6);
		ol.mutator().add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);
		
		ol.mutator().move(0, 2, 5);
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:5", "item:6", "item:0", "item:1", "item:2", "item:3", "item:4", "item:7" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(0), eq(2), eq(5));
	}

	@Test
	public void moveDownNoOverlapMovesMappedItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(0);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(6);
		ol.mutator().add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);
		
		ol.mutator().move(3, 0, 2);
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:3", "item:4", "item:0", "item:1", "item:2", "item:5", "item:6", "item:7" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(3), eq(0), eq(2));
	}

	@Test
	public void moveDownOverlapMovesMappedItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(0);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(6);
		ol.mutator().add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);
		
		ol.mutator().move(1, 0, 7);
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:1", "item:2", "item:3", "item:4", "item:5", "item:6", "item:7", "item:0" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(1), eq(0), eq(7));
	}

	@Test
	public void resetSourceResets() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(0);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(6);
		ol.mutator().add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);
		Collection<Integer> newSourceValues = new ArrayList<>();
		newSourceValues.add(10);
		newSourceValues.add(20);
		newSourceValues.add(30);
		
		ol.mutator().reset(newSourceValues);
		
		assertEquals(ol.list().getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:10", "item:20", "item:30" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).reset(stringsCaptor.capture());
		String[] capturedValues = { "item:0", "item:1", "item:2", "item:3", "item:4", "item:5", "item:6", "item:7" };
		index = 0;
		for (String s : stringsCaptor.getValue()) {
			assertEquals(capturedValues[index++], s);
		}
	}

	@Test
	public void unlinkNoMoreUpdates() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(ol.list(), new IntegerToStringMapper());
		mol.addObserver(stringObserver);
		mol.unlink();
		
		ol.mutator().add(0);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(6);
		ol.mutator().add(7);
		ol.mutator().move(1, 0, 7);
		ol.mutator().remove(0, 5);

		assertEquals(0, mol.getSize());
		verify(stringObserver, never()).moved(anyInt(), anyInt(), anyInt());
		verify(stringObserver, never()).added(anyInt(), anyInt());
		verify(stringObserver, never()).removing(anyInt(), anyInt());
		verify(stringObserver, never()).removed(anyInt(), anyInt());
	}

}
