# Tandem Tales Story Graph Generator

This utility reads a
[Tandem Tales story world](https://github.com/sgware/tt-server) JSON file and
uses brute-force breadth-first search to generate a
[story graph](https://github.com/sgware/story-graph) of all states which are
reachable from the starting state of the story.

This application was built using
[Story Graph Tools](https://github.com/sgware/story-graph-tools).

## Pre-Compiled Executable

The pre-compiled JAR file containing all dependencies can be
[downloaded here](build/jar).

The JavaDoc API for all Java source files can be
[found here](http://sgware.github.io/tt-sg-gen).

## Build from Source

The Tandem Tales Story Graph Generator is written in Java and published as a
Maven project.

Four dependencies need to be installed in Maven:
- [Google GSON](https://github.com/google/gson)
- [Tandem Tales Server](https://github.com/sgware/tt-server)
- [Story Graph](https://github.com/sgware/story-graph)
- [Story Graph Tools](https://github.com/sgware/story-graph-tools)

Assuming you have [Git](https://git-scm.com/install), the
[Java Development Kit](https://www.oracle.com/java/technologies/downloads/), and
[Maven](https://maven.apache.org/) installed and on your path, you can download
the dependencies and compile this application like this:
```
git clone https://github.com/google/gson.git
cd gson
mvn clean install
cd ..
git clone https://github.com/sgware/tt-server
cd tt-server
mvn clean install
cd ..
git clone https://github.com/sgware/story-graph
cd story-graph
mvn clean install
cd ..
git clone https://github.com/sgware/story-graph-tools
cd story-graph-tools
mvn clean install
cd ..
git clone https://github.com/sgware/tt-sg-gen
cd tt-sg-gen
mvn clean install
```

## Usage

Assuming [Git](https://git-scm.com/install) and
[Java](https://www.oracle.com/java/technologies/downloads/) are on your path:
```
# Download this application.
git clone https://github.com/sgware/tt-sg-gen
cd tt-sg-gen/build/jar
# Display the help message.
java -jar tt-sg-gen.jar -h
# Download the Tandem Tales Server to get tutorial.json.
git clone https://github.com/sgware/tt-server
cp tt-server/worlds/tutorial.json .
# Generate the full story graph for the tutorial world.
java -jar tt-sg-gen.jar tutorial.json
# Download Story Graph Tools so you can explore the graph.
git clone https://github.com/sgware/story-graph-tools
cp story-graph-tools/build/jar/sg-explore.jar .
java -jar sg-explore.jar tutorial.zip
```

A Tandem Tales story world does not define utility functions for the author or
the story's characters, so every character's utility will always be 0 by
default. You can provide a function which defines each character's utility in
a state as a JSON file like
`[tutorial-utilities.json](tutorial-utilities.json)`.
```
# Generate the story graph using custom utility functions.
java -jar tt-sg-gen.jar tutorial.json -u tutorial-utilities.json
```

Story graphs get big fast. You can limit the depth of the generation with the
`-d` option like this:
```
# Only generate the graph to depth 5.
java -jar tt-sg-gen.jar tt-server/worlds/tutorial.json -d 5
```
A depth of 5 means the graph will contain all states which can be reached by a
series of 5 actions from the initial state. States at depth 5 will still have
actions available, but only if they lead back to states generated at depth 5 or
below.

This tool is multi-threaded, allowing many thread to work on expanding the nodes
in the graph simultaneously. The process is deterministic, meaning the graph
generated will always be the same no matter how many threads you use. However,
because all of these threads must synchronize on the story graph when they
modify it, running more than a few threads simultaneously has diminishing
returns.
```
# Generate the story graph using 10 simultaneous threads.
java -jar tt-sg-gen.jar tutorial.json -t 10
```

## License

The Tandem Tales Story Graph Generator was developed by Stephen G. Ware PhD,
Associate Professor of Computer Science at the University of Kentucky.
Development was sponsored in part by a grant from the US National Science
Foundation, #2145153.

Tandem Tales and the Story Graph library are released under the GNU General
Public License version 3.0 (GPL 3). This means you are free to share and modify
this software, even for commercial purposes, as long as you give credit to the
original creators and you also release your modifications under the GPL 3
license. See the license file for details. The University of Kentucky retains
all right not specifically granted.

To license this project for something not compatible with the terms of the GPL
license, contact the University of Kentucky Office of Technology
Commercialization at <otcinfo@uky.edu>.

# Version History

- Version 1.0.0: First public release.