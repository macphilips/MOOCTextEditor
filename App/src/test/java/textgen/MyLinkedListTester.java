/**
 * 
 */
package textgen;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author UC San Diego MOOC team
 *
 */
public class MyLinkedListTester {

	private static final int LONG_LIST_LENGTH = 10;

	private MyLinkedList<String> shortList;
	private MyLinkedList<Integer> emptyList;
	private MyLinkedList<Integer> longerList;
	private MyLinkedList<Integer> list1;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Feel free to use these lists, or add your own
		shortList = new MyLinkedList<String>();
		shortList.add("A");
		shortList.add("B");
		emptyList = new MyLinkedList<Integer>();
		longerList = new MyLinkedList<Integer>();
		for (int i = 0; i < LONG_LIST_LENGTH; i++) {
			longerList.add(i);
		}
		list1 = new MyLinkedList<Integer>();
		list1.add(65);
		list1.add(21);
		list1.add(42);

	}

	/**
	 * TextStyleInfo if the get method is working correctly.
	 */
	/*
	 * You should not need to add much to this method. We provide it as an
	 * example of a thorough test.
	 */
	@Test
	public void testGet() {
		// test empty list, get should throw an exception
		try {
			emptyList.get(0);
			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {

		}

		// test short list, first contents, then out of bounds
		assertEquals("Check first", "A", shortList.get(0));
		assertEquals("Check second", "B", shortList.get(1));

		try {
			shortList.get(-1);
			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {

		}
		try {
			shortList.get(2);
			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {

		}
		// test longer list contents
		for (int i = 0; i < LONG_LIST_LENGTH; i++) {
			assertEquals("Check " + i + " element", (Integer) i, longerList.get(i));
		}

		// test off the end of the longer array
		try {
			longerList.get(-1);
			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {

		}
		try {
			longerList.get(LONG_LIST_LENGTH);
			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {
		}

	}

	/**
	 * TextStyleInfo removing an element from the list. We've included the example from
	 * the concept challenge. You will want to add more tests.
	 */
	@Test
	public void testRemove() {
		try {
			MyLinkedList<Integer> list1 = new MyLinkedList<Integer>();
			list1.add(65);
			list1.add(21);
			list1.add(42);

			int a = list1.remove(0);
			assertEquals("Remove: check a is correct ", 65, a);
			assertEquals("Remove: check element 0 is correct ", (Integer) 21, list1.get(0));
			assertEquals("Remove: check size is correct ", 2, list1.size());
			
			

			 a = list1.remove(-1);
				fail("Check out of bounds");
				
			 a = list1.remove(1000);
				fail("Check out of bounds");

		} catch (IndexOutOfBoundsException e) {

		} catch (NullPointerException e) {

		}
		
		try{

			list1.remove(-1);	
			fail("Check out of bounds");
			
		} catch (IndexOutOfBoundsException e) {

		
		}
		
		try{

			list1.remove(111111);	
			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {

		}

		// TODO: Add more tests here
	}

	/**
	 * TextStyleInfo adding an element into the end of the list, specifically public
	 * boolean add(E element)
	 */
	@Test
	public void testAddEnd() {
		// TODO: implement this test
		try {
			MyLinkedList<Integer> list1 = new MyLinkedList<Integer>();
			list1.add(23);
			assertEquals("Remove: check size is correct ", (Integer) 23, list1.get(0));

			list1.add(99);
			assertEquals("Remove: check size is correct ", (Integer) 99, list1.get(1));

			list1.add(59);
			assertEquals("Remove: check size is correct ", (Integer) 59, list1.get(2));

			list1.add(49);
			assertEquals("Remove: check size is correct ", (Integer) 49, list1.get(3));

			list1.add(null);
			fail("Check out of bounds");
		} catch (NullPointerException e) {

		}
	}

	/** TextStyleInfo the size of the list */
	@Test
	public void testSize() {
		// TODO: implement this test

		MyLinkedList<Integer> list1 = new MyLinkedList<Integer>();
		list1.add(65);
		list1.add(21);
		list1.add(42);
		assertEquals("Remove: check size is correct ", 3, list1.size());
	}

	/**
	 * TextStyleInfo adding an element into the list at a specified index, specifically:
	 * public void add(int index, E element)
	 */
	@Test
	public void testAddAtIndex() {
		// TODO: implement this test
		MyLinkedList<Integer> list1 = new MyLinkedList<Integer>();
		list1.add(0, 23);
		
		try {

			assertEquals("Remove: check size is correct ", (Integer) 23, list1.get(0));

			list1.add(1, 663);
			assertEquals("Remove: check size is correct ", (Integer) 663, list1.get(1));
			

			list1.add(0, 12);
			assertEquals("Remove: check size is correct ", (Integer) 12, list1.get(0));

			assertEquals("Remove: check size is correct ", 3, list1.size());
			
			
			list1.add(0, null);	


			
		} catch (IndexOutOfBoundsException e) {

		} catch (NullPointerException e) {

		}
		try{

			list1.add(-1, 333);	
			fail("Check out of bounds");
			
		} catch (IndexOutOfBoundsException e) {

		
		}
		
		try{

			list1.add(111111, 222);	

			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {

		}


	}

	/** TextStyleInfo setting an element in the list */
	@Test
	public void testSet() {
		// TODO: implement this test
		try {
			MyLinkedList<Integer> list1 = new MyLinkedList<Integer>();
			list1.add(0, 23);

			list1.set(0, 33);
			assertEquals("Remove: check size is correct ", (Integer) 33, list1.get(0));

			list1.set(0, null);
			fail("Check out of bounds");
			list1.set(239023, 2);
			fail("Check out of bounds");
			list1.set(-1, 55);
			fail("Check out of bounds");

		} catch (IndexOutOfBoundsException e) {

		} catch (NullPointerException e) {

		}
		try{

			list1.set(-1, 333);	
			fail("Check out of bounds");
			
		} catch (IndexOutOfBoundsException e) {

		
		}
		
		try{

			list1.set(111111, 222);	

			fail("Check out of bounds");
		} catch (IndexOutOfBoundsException e) {

		}

	}

	// TODO: Optionally add more test methods.

}
