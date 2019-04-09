package textgen;

import java.util.AbstractList;

/**
 * A class that implements a doubly linked list
 * 
 * @author UC San Diego Intermediate Programming MOOC team
 *
 * @param <E>
 *            The type of the elements stored in the list
 */
public class MyLinkedList<E> extends AbstractList<E> {
	LLNode<E> head;
	LLNode<E> tail;
	int size;

	/** Create a new empty LinkedList */
	public MyLinkedList() {
		// TODO: Implement this method
		this.size = 0;
		head = new LLNode<E>(null);
		tail = new LLNode<E>(null);

		head.next = tail;
		tail.prev = head;
	}

	/**
	 * Appends an element to the end of the list
	 * 
	 * @param element
	 *            The element to add
	 */
	public boolean add(E element) {
		// TODO: Implement this method
		if (element == null){
			throw new NullPointerException("");
		}

		LLNode<E> node = new LLNode<>(element);

		if (size == 0) {
			node.next = head.next;
			node.prev = node.next.prev;
			node.next.prev = node;
			node.prev.next = node;
			size++;
			return true;
		} else {
			LLNode<E> predptr = head;
			for (int i = 0; i < size; i++) {
				predptr = predptr.next;
				if (predptr.next == null) {
					System.out.println("Cannot Go Further i = " + i);
				}
			}
			node.next = predptr.next;
			node.prev = node.next.prev;
			node.next.prev = node;
			node.prev.next = node;
			size++;
			return true;
		}
	}

	/**
	 * Get the element at position index
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public E get(int index) {
		// TODO: Implement this method.
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Get Operation Failed");
		}

		LLNode<E> predptr = head;
		for (int i = 0; i < index+1; i++) {
			predptr = predptr.next;
			if (predptr.next == null) {
				System.out.println("Cannot Go Further i = " + i);
			}
		}

		return predptr.data;
	}

	/**
	 * Add an element to the list at the specified index
	 * 
	 * @param The
	 *            index where the element should be added
	 * @param element
	 *            The element to add
	 */
	public void add(int index, E element) {
		// TODO: Implement this method
		if (element == null){
			throw new NullPointerException("");
		}

		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Add Operation Failed");
		}
		
		int n = index;
	
		LLNode<E> node = new LLNode<>(element);
		LLNode<E> predptr = head;

		for (int i = 0; i < n; i++) {
			predptr = predptr.next;
			if (predptr.next == null) {
				System.out.println("Cannot Go Further i = " + i);
			}
		}
		node.next = predptr.next;
		node.prev = node.next.prev;
		node.next.prev = node;
		node.prev.next = node;
		size++;
	}

	/** Return the size of the list */
	public int size() {
		// TODO: Implement this method
		return size;
	}

	/**
	 * Remove a node at the specified index and return its data element.
	 * 
	 * @param index
	 *            The index of the element to remove
	 * @return The data element removed
	 * @throws IndexOutOfBoundsException
	 *             If index is outside the bounds of the list
	 * 
	 */
	public E remove(int index) {
		// TODO: Implement this method
		
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Remove Opertion Failed");
		}
		
		LLNode<E> predptr = head;

		for (int i = 0; i < index+1; i++) {
			predptr = predptr.next;
			if (predptr.next == null) {
				System.out.println("Cannot Go Further i = " + i);
			}
		}
		E r = predptr.data;
		predptr.prev.next = predptr.next;
		predptr.next.prev = predptr.prev;
		size--;
		return r;
	}

	/**
	 * Set an index position in the list to a new element
	 * 
	 * @param index
	 *            The index of the element to change
	 * @param element
	 *            The new element
	 * @return The element that was replaced
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public E set(int index, E element) {
		// TODO: Implement this method
		if (element == null){
			throw new NullPointerException("");
		}
		if (index < 0 || size == 0 || index > size) {
			throw new IndexOutOfBoundsException("Set Operation Failed");
		}
		
		LLNode<E> predptr = head;

		for (int i = 0; i < index + 1 ; i++) {
			predptr = predptr.next;
			if (predptr.next == null) {
				System.out.println("Cannot Go Further i = " + i);
			}
		}

		E r = predptr.data;
		predptr.data = element;

		return r;
	}
}

class LLNode<E> {
	LLNode<E> prev;
	LLNode<E> next;
	E data;

	// TODO: Add any other methods you think are useful here
	// E.g. you might want to add another constructor

	public LLNode(E e) {
		this.data = e;
		this.prev = null;
		this.next = null;
	}

}
