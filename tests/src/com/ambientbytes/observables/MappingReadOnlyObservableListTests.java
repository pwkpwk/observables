package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MappingReadOnlyObservableListTests {
	
	private static final class IntegerToStringMapper implements IItemMapper<Integer, String> {
		@Override
		public String map(Integer item) {
			return "item:" + item.toString();
		}
	}
	
	@Mock IListObserver stringObserver;
	@Mock IItemMapper<Integer, String> mockMapper;
	@Mock IReadWriteMonitor mockMonitor;
	@Mock IResource rLock;
	@Mock IResource wLock;
	@Captor ArgumentCaptor<Collection<String>> stringsCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockMonitor.acquireRead()).thenReturn(rLock);
		when(mockMonitor.acquireWrite()).thenReturn(wLock);
	}

	@Test
	public void newMappingReadOnlyObservableListAddsMappedItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:1", "item:2", "item:3" }) {
			assertEquals(s, mol.getAt(index++));
		}
	}

	@Test
	public void addToSourceAddsMappedItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);

		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:1", "item:2", "item:3" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(3)).added(anyInt(), anyInt());
	}

	@Test
	public void removeFromSourceRemovesMappedItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		
		mutator.remove(0, 2);
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:3" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).removing(eq(0), eq(2));
		verify(stringObserver, times(1)).removed(eq(0), eq(2));
	}

	@Test
	public void moveUpNoOverlapMovesMappedItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(0);
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(6);
		mutator.add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		
		mutator.move(0, 4, 2);
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:2", "item:3", "item:4", "item:5", "item:0", "item:1", "item:6", "item:7" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(0), eq(4), eq(2));
	}

	@Test
	public void moveUpOverlapMovesMappedItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(0);
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(6);
		mutator.add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		
		mutator.move(0, 2, 5);
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:5", "item:6", "item:0", "item:1", "item:2", "item:3", "item:4", "item:7" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(0), eq(2), eq(5));
	}

	@Test
	public void moveDownNoOverlapMovesMappedItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(0);
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(6);
		mutator.add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		
		mutator.move(3, 0, 2);
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:3", "item:4", "item:0", "item:1", "item:2", "item:5", "item:6", "item:7" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(3), eq(0), eq(2));
	}

	@Test
	public void moveDownOverlapMovesMappedItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(0);
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(6);
		mutator.add(7);
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		
		mutator.move(1, 0, 7);
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:1", "item:2", "item:3", "item:4", "item:5", "item:6", "item:7", "item:0" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).moved(eq(1), eq(0), eq(7));
	}

	@Test
	public void resetSourceResets() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(0);
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(6);
		mutator.add(7);
		final MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		Collection<Integer> newSourceValues = new ArrayList<>();
		newSourceValues.add(10);
		newSourceValues.add(20);
		newSourceValues.add(30);
		final List<String> capturedValues = new ArrayList<>();
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				for (int i = 0; i < mol.getSize(); ++i) {
					capturedValues.add(mol.getAt(i));
				}
				return null;
			}
		}).when(stringObserver).resetting();
		
		mutator.reset(newSourceValues);
		
		assertEquals(source.getSize(), mol.getSize());
		int index = 0;
		for (String s : new String[] { "item:10", "item:20", "item:30" }) {
			assertEquals(s, mol.getAt(index++));
		}
		verify(stringObserver, times(1)).resetting();
		verify(stringObserver, times(1)).reset();
		String[] testValues = { "item:0", "item:1", "item:2", "item:3", "item:4", "item:5", "item:6", "item:7" };
		for (index = 0; index < capturedValues.size(); ++index) {
			assertEquals(testValues[index], capturedValues.get(index));
		}
	}

	@Test
	public void unlinkNoMoreUpdates() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, new IntegerToStringMapper(), new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		mol.unlink();
		
		mutator.add(0);
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(6);
		mutator.add(7);
		mutator.move(1, 0, 7);
		mutator.remove(0, 5);

		assertEquals(0, mol.getSize());
		verify(stringObserver, never()).moved(anyInt(), anyInt(), anyInt());
		verify(stringObserver, never()).added(anyInt(), anyInt());
		verify(stringObserver, never()).removing(anyInt(), anyInt());
		verify(stringObserver, never()).removed(anyInt(), anyInt());
	}
		
	@Test
	public void setCallsMapper() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(0);
		when(mockMapper.map(eq(Integer.valueOf(10)))).thenReturn("10");
		MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, mockMapper, new DummyReadWriteMonitor());
		
		verify(mockMapper, times(1)).map(eq(Integer.valueOf(0)));
		mutator.set(0, 10);
		
		verify(mockMapper, times(1)).map(eq(Integer.valueOf(10)));
		assertEquals("10", mol.getAt(0));
	}
	
	@Test
	public void setReportsChange() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(0);
		mutator.add(1);
		mutator.add(2);
		when(mockMapper.map(eq(Integer.valueOf(10)))).thenReturn("10");
		when(mockMapper.map(eq(Integer.valueOf(1)))).thenReturn("1");
		final MappingReadOnlyObservableList<Integer, String> mol = new MappingReadOnlyObservableList<>(source, mockMapper, new DummyReadWriteMonitor());
		mol.addObserver(stringObserver);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock arg0) throws Throwable {
				assertEquals("1", mol.getAt(1));
				return null;
			}
		}).when(stringObserver).changing(eq(1), eq(1));
		
		mutator.set(1, 10);

		verify(stringObserver, times(1)).changing(eq(1), eq(1));
		verify(stringObserver, times(1)).changed(eq(1), eq(1));
	}

}
