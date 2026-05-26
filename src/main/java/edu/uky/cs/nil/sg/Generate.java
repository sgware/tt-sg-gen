package edu.uky.cs.nil.sg;

import java.io.File;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;

import edu.uky.cs.nil.tt.world.Action;
import edu.uky.cs.nil.tt.world.Constant;
import edu.uky.cs.nil.tt.world.Entity;
import edu.uky.cs.nil.tt.world.LogicalWorld;
import edu.uky.cs.nil.tt.world.State;
import edu.uky.cs.nil.tt.world.Variable;

/**
 * A {@link StoryGraphTool tool} that generates a {@link StoryGraph story graph}
 * from a {@link LogicalWorld Tandem Tales logical story world}.
 * 
 * @author Stephen G. Ware
 */
public class Generate extends StoryGraphTool {
	
	/** Specifies the name of the output story graph file */
	public final Option OUTPUT = new Option("o", "FILE", "output file (default: world name)");
	
	/** Specifies the file from which to read the character utility functions */
	public final Option UTILITY = new Option("u", "FILE", "JSON map of character/ending pairs to utility values (default: always 0)");
	
	/** Specifies the max depth of the expansion (0 for no limit) */
	public final Option DEPTH = new Option("d", "NUMBER", "maximum depth to expand (default: no limit)", "0");
	
	/**
	 * Specifies the number of threads that will run simultaneously to expand
	 * nodes
	 */
	public final Option THREADS = new Option("t", "NUMBER", "number of threads to run simultaneously (default: 3)", "3");
	
	/**
	 * Constructs a new Tandem Tales story graph generator with a set of
	 * arguments.
	 * 
	 * @param arguments the arguments that configure the tool
	 */
	public Generate(ToolArguments arguments) {
		super(arguments);
	}
	
	/**
	 * Constructs a new Tandem Tales story graph generator from an array of
	 * arguments.
	 * 
	 * @param args the arguments that configure the tool
	 */
	public Generate(String[] args) {
		this(new ToolArguments(args));
	}
	
	@Override
	public String getName() {
		return "Tandem Tales Story Graph Generator";
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public String getAuthors() {
		return "Stephen G. Ware";
	}
	
	@Override
	public String getDescription() {
		return "Explores a Tandem Tales story world breadth-first to generate a graph of all possible actions and states. The first argument must be a Tandem Tales story world JSON file.";
	}
	
	@Override
	public List<Option> getOptions() {
		List<Option> list = super.getOptions();
		list.add(OUTPUT);
		list.add(UTILITY);
		list.add(DEPTH);
		list.add(THREADS);
		return list;
	}
	
	/**
	 * Configures and runs the tool according to its command line arguments.
	 * 
	 * @param args the command line arguments that configure the tool
	 */
	public static void main(String[] args) {
		new Generate(args).run();
	}
	
	@Override
	public void run(Status status) throws Exception {
		// Read arguments.
		status.setMessage("Reading Tandem Tales world from \"" + arguments.get(0) + "\"...");
		LogicalWorld world = LogicalWorld.read(new File(arguments.get(0)));
		BiFunction<String, State, Double> utilities = new UtilityMap();
		if(arguments.contains(UTILITY)) {
			status.setMessage("Reading utility functions from \"" + arguments.getValue(UTILITY) + "\"...");
			utilities = UtilityMap.read(new File(arguments.getValue(UTILITY)));
		}
		int max = Integer.parseInt(arguments.getValue(DEPTH));
		int threads = Integer.parseInt(arguments.getValue(THREADS));
		// Create story graph elements.
		status.setMessage("Creating story graph elements...");
		StoryGraph graph = new StoryGraph();
		graph.setTitle(world.getName());
		TreeSet<Entity> characters = new TreeSet<>();
		for(Action action : world.getActions())
			for(Entity consenting : action.getConsenting())
				characters.add(consenting);
		for(Entity character : characters)
			graph.characters.add(character.getName());
		Entity player = world.getPlayer();
		if(player != null)
			graph.characters.setPlayer(graph.characters.get(player.getName()), true);
		for(Variable variable : world.getVariables())
			graph.fluents.add(variable.getName());
		graph.values.add(Constant.NULL.toString());
		graph.values.add(Constant.FALSE.toString());
		graph.values.add(Constant.TRUE.toString());
		for(Entity entity : world.getEntities())
			graph.values.add(entity.getName());
		for(Action action : world.getActions())
			graph.actions.add(action.getName());
		// Expand story graph.
		new GraphExpander(world, utilities, graph, max, threads).run(status);
		// Write story graph to file.
		File output = new File(world.getName() + ".zip");
		if(arguments.contains(OUTPUT))
			output = new File(arguments.getValue(OUTPUT));
		graph.write(output);
		status.setMessage("Story graph generated: " + graph.nodes.size() + " nodes, " + graph.edges.temporal.size() + " edges.");
	}
}