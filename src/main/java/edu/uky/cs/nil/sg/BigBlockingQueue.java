package edu.uky.cs.nil.sg;

import java.util.concurrent.Semaphore;

/**
 * A thread-safe {@link BigQueue} where the {@link #pop()} operation blocks
 * until an element is available.
 * 
 * @param <T> type of element stored in the queue
 * @author Stephen G. Ware
 */
public class BigBlockingQueue<T> {
	
	/** Queue in which elements are stored */
	private final BigQueue<T> queue;
	
	/** Semaphore used to block until an elements is available */
	private final Semaphore semaphore;
	
	/**
	 * Creates a new blocking queue that stores its elements in an existing
	 * queue.
	 * 
	 * @param queue the queue where elements will be stored by this blocking
	 * queue
	 */
	public BigBlockingQueue(BigQueue<T> queue) {
		this.queue = queue;
		if(queue.size() > Integer.MAX_VALUE)
			this.semaphore = new Semaphore(Integer.MAX_VALUE);
		else
			this.semaphore = new Semaphore((int) queue.size());
	}
	
	/**
	 * Creates a new empty blocking queue.
	 */
	public BigBlockingQueue() {
		this(new BigQueue<>());
	}
	
	@Override
	public synchronized boolean equals(Object other) {
		return other instanceof BigBlockingQueue otherQueue && this.queue.equals(otherQueue.queue);
	}
	
	@Override
	public synchronized int hashCode() {
		return queue.hashCode();
	}
	
	@Override
	public synchronized String toString() {
		return queue.toString();
	}
	
	/**
	 * Returns the number of elements currently stored in the queue.
	 * 
	 * @return the number of elements
	 */
	public synchronized long size() {
		return queue.size();
	}
	
	/**
	 * Blocks until at least one elements is in the queue and then returns, but
	 * does not remove, the element that has been on the queue the longest and
	 * which will be remove by the next call to {@link #pop() pop}.
	 * 
	 * @return the element at the front of the queue
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * for an element to become available
	 */
	public T peek() throws InterruptedException {
		semaphore.acquire();
		synchronized(this) {
			semaphore.release();
			return queue.peek();
		}
	}
	
	/**
	 * Adds an element to the back of the queue. This element will not be {@link
	 * #pop() popped} until all elements on the queue at the time it was pushed
	 * have been popped.
	 * 
	 * @param element the element to add to the queue
	 */
	public synchronized void push(T element) {
		queue.push(element);
		if(queue.size() < Integer.MAX_VALUE)
			semaphore.release();
	}
	
	/**
	 * Blocks until at least one element is in the queue and then removes and
	 * returns the element that has been on the queue the longest.
	 * 
	 * @return the element that has been on the queue longest
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * for an element to become available
	 */
	public T pop() throws InterruptedException {
		semaphore.acquire();
		synchronized(this) {
			if(queue.size() > Integer.MAX_VALUE)
				semaphore.release();
			return queue.pop();
		}
	}
}