package edu.uky.cs.nil.sg;

import java.util.ArrayList;

import edu.uky.cs.nil.sg.ExpandedNode.EdgeStub;
import edu.uky.cs.nil.tt.world.Action;
import edu.uky.cs.nil.tt.world.Effect;
import edu.uky.cs.nil.tt.world.Ending;

/**
 * A thread which repeatedly {@link GraphExpander#pop() pops} the next state
 * node that needs to be expanded, expands it, and then {@link
 * GraphExpander#push(ExpandedNode) pushes} it so that the newly generated edges
 * can be added to the story graph.
 * 
 * @author Stephen G. Ware
 */
class NodeExpander extends Thread {
	
	/** The Tandem Tales graph expander whose nodes this thread expands */
	public final GraphExpander graph;
	
	/**
	 * Creates a new Tandem Tales state node expander thread.
	 * 
	 * @param graph the Tandem Tales graph expander this thread works for
	 */
	public NodeExpander(GraphExpander graph) {
		this.graph = graph;
	}
	
	@Override
	public void run() {
		ArrayList<EdgeStub> stubs = new ArrayList<>();
		// Get the first node to expand.
		ExpandedNode node = graph.pop();
		while(node != null) {
			// Empty the list of edge stubs.
			stubs.clear();
			// Only expand non-terminal nodes.
			if(getEnding(node.before) == null) {
				// Expand all actions whose preconditions are met.
				for(Action action : graph.world.getActions()) {
					if(graph.world.getPrecondition(action).test(node.before)) {
						edu.uky.cs.nil.tt.world.State after = node.before;
						for(Effect effect : graph.world.getEffects(action))
							after = effect.apply(node.before, after);
						stubs.add(new EdgeStub(action, after));
					}
				}
			}
			// Register the list of expanded actions and states.
			node.edges = stubs.toArray(new EdgeStub[stubs.size()]);
			graph.push(node);
			// Get the next node to expand.
			node = graph.pop();
		}
	}
	
	private final Ending getEnding(edu.uky.cs.nil.tt.world.State state) {
		for(Ending ending : graph.world.getEndings())
			if(graph.world.getCondition(ending).test(state))
				return ending;
		return null;
	}
}