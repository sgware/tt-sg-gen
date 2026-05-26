package edu.uky.cs.nil.sg;

import edu.uky.cs.nil.tt.world.Action;
import edu.uky.cs.nil.tt.world.State;

/**
 * A simple structure to hold a {@link #before Tandem Tales state} that needs to
 * be expanded and all of its outgoing temporal edges once it has been expanded.
 * 
 * @author Stephen G. Ware
 */
class ExpandedNode implements Comparable<ExpandedNode> {
	
	/**
	 * An edge stub represents the {@link TemporalEdge#label label} and {@link
	 * TemporalEdge#head head} of a {@link TemporalEdge}.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class EdgeStub {
		
		/** The action which leads to the next state */
		public final Action action;
		
		/** The state that results from taking the action */
		public final State after;
		
		/**
		 * Creates a new temporal edge stub.
		 * 
		 * @param action the label of the edge
		 * @param after the head node of the edge
		 */
		public EdgeStub(Action action, State after) {
			this.action = action;
			this.after = after;
		}
		
		@Override
		public String toString() {
			return action.toString();
		}
	}
	
	/** The Tandem Tales state to be expanded */
	public final State before;
	
	/** The story graph node corresponding to the state */
	public final Node tail;
	
	/** The temporal edges which have this node as a tail */
	public EdgeStub[] edges;
	
	/**
	 * Creates a new expanded state node structure.
	 * 
	 * @param before the Tandem Tales state to be expanded
	 * @param tail the story graph node corresponding to the state
	 */
	public ExpandedNode(State before, Node tail) {
		this.before = before;
		this.tail = tail;
	}
	
	@Override
	public String toString() {
		return "node " + tail.getID();
	}
	
	@Override
	public int compareTo(ExpandedNode other) {
		return this.tail.compareTo(other.tail);
	}
}