package edu.uky.cs.nil.sg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.world.Expression;
import edu.uky.cs.nil.tt.world.Proposition;
import edu.uky.cs.nil.tt.world.State;

/**
 * A simple {@link BiFunction} that defines what utility value a Tandem Tales
 * character has in a given {@link State state} and which can be easily
 * serialized to and deserialized from a JSON file.
 * <p>
 * This object exists because a Tandem Tales story world does not define the
 * utility of its characters, so supplementary information is needed to set the
 * {@link Node#getUtility(Character) utility values} of the authors and the
 * characters in the {@link Node nodes} of a {@link StoryGraph story graph}.
 * 
 * @author Stephen G. Ware
 */
public class UtilityMap implements BiFunction<String, State, Double> {
	
	/**
	 * A simple {@link Function} that defines what utility its Tandem Tales
	 * {@link #character character} has in a given {@link State state}.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Utility implements Function<State, Double> {
		
		/** The name of a Tandem Tales character, or null for the author */
		public final String character;
		
		/**
		 * An ordered list of condition/expression pairs which define the
		 * character's utility. Think of these like the branches of an
		 * if/else if expression. The first branch whose expression holds
		 * in the given state will be used to define the character's utility.
		 */
		private final Branch[] branches;
		
		/**
		 * Creates a new utility function for a given character.
		 * 
		 * @param character the name of a Tandem Tales character, or null to
		 * represent the author
		 * @param branches an ordered sequence of condition/expression pairs
		 * which define the character's utility in a state
		 */
		public Utility(String character, Branch...branches) {
			this.character = character;
			edu.uky.cs.nil.tt.Utilities.requireAllNonNull(branches, "branch");
			this.branches = branches;
		}
		
		/**
		 * Creates a new utility function for the story author.
		 * 
		 * @param branches an ordered sequence of condition/expression pairs
		 * which define the author's utility in a state
		 */
		public Utility(Branch...branches) {
			this(null, branches);
		}
		
		@Override
		public String toString() {
			return (character == null ? "author" : character) + " utility function";
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * Returns this utility this function's {@link #character character} has
		 * in the given state.
		 * <p>
		 * This method checks {@link #branches this utility function's branches}
		 * in order until it finds one where the {@link Branch#condition
		 * condition} holds in the given state, and it then returns the value of
		 * {@link Branch#value the branch's value} when evaluated in that state.
		 * If no branch's condition holds in the given state, 0 is returned.
		 */
		@Override
		public Double apply(State state) {
			for(Branch branch : branches)
				if(branch.condition.test(state))
					return branch.value.evaluate(state).toNumber();
			return 0.0;
		}
	}
	
	/**
	 * A condition and expression pair which are used in a {@link Utility
	 * utility function} to define conditions under which a character has
	 * a certain utility.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Branch {
		
		/** The condition under which this branch applies */
		public final Proposition condition;
		
		/**
		 * An expression representing a character's utility in the state where
		 * this branch's condition holds
		 */
		public final Expression value;
		
		/**
		 * Creates a new utility branch condition/value pair.
		 * 
		 * @param condition the condition under which this branch applies
		 * @param value an expression representing a character's utility in the
		 * state where this branch's condition holds
		 */
		public Branch(Proposition condition, Expression value) {
			edu.uky.cs.nil.tt.Utilities.requireNonNull(condition, "condition");
			this.condition = condition;
			edu.uky.cs.nil.tt.Utilities.requireNonNull(value, "value");
			this.value = value;
		}
		
		@Override
		public String toString() {
			return condition + " -> " + value;
		}
	}
	
	/** The utility functions of the author and characters */
	private final Utility[] utilities;
	
	/** A map to easily retrieve a specific character's utility function */
	private transient Map<String, Utility> map;
	
	/**
	 * Constructs a new utility bifunction from a set of character utility
	 * functions.
	 * 
	 * @param utilities a set of utility functions for the author and characters
	 * of a Tandem Tales story world
	 */
	public UtilityMap(Utility...utilities) {
		this.utilities = utilities;
	}
	
	@Override
	public String toString() {
		return utilities.length + " utility functions";
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Finds the {@link Utility utility function} for the character with the
	 * given name (or the author, if the name is null) and returns {@link
	 * Utility#apply(State) that character's utility} in the given state.
	 * <p>
	 * If the given character does not have a utility function, 0 is returned.
	 */
	@Override
	public Double apply(String character, State state) {
		if(map == null) {
			map = new HashMap<>();
			for(Utility utility : utilities)
				map.put(utility.character, utility);
		}
		Utility utility = map.get(character);
		if(utility == null)
			return 0.0;
		else
			return utility.apply(state);
	}
	
	/**
	 * Deserializes a utility bifunction from a reader using JSON.
	 * 
	 * @param reader the JSON source
	 * @return a utility bifunction
	 * @throws IOException if a problem occurs while reading or deserializing
	 * the bifunction
	 */
	public static UtilityMap read(Reader reader) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		Expression.configure(builder);
		Gson gson = builder.create();
		return gson.fromJson(reader, UtilityMap.class);
	}
	
	/**
	 * Deserializes a utility bifunction from a JSON file.
	 * 
	 * @param file the JSON file
	 * @return a utility bifunction
	 * @throws IOException if a problem occurs while reading or deserializing
	 * the bifunction
	 */
	public static UtilityMap read(File file) throws IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			return read(reader);
		}
	}
	
	/**
	 * Serializes this utility bifunction to a writer using JSON.
	 * 
	 * @param writer the JSON destination
	 * @throws IOException if a problem occurs while writing or serializing
	 * this bifunction
	 */
	public void write(Writer writer) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		Expression.configure(builder);
		Gson gson = builder.setPrettyPrinting().create();
		gson.toJson(this, writer);
	}
	
	/**
	 * Serializes this utility bifunction to a JSON file.
	 * 
	 * @param file the JSON file
	 * @throws IOException if a problem occurs while writing or serializing
	 * this bifunction
	 */
	public void write(File file) throws IOException {
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			write(writer);
		}
	}
}