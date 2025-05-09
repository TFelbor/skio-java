# Ski Orienteering Path Optimizer

A Java-based application that calculates the optimal path for a ski orienteering course using Dijkstra's shortest path algorithm. This tool helps competitors find the fastest route through a course while ensuring all control points are visited in the correct order.

## Overview

Ski orienteering combines skiing and navigation skills where competitors must visit a series of control points in a specified order as quickly as possible. This project implements an algorithm to find the optimal path between control points, taking into account:

- Variable track conditions that affect speed in different directions
- Required visitation of all control points in sequence
- Complex junction networks with multiple possible routes

## Features

- Parses custom map and course files
- Constructs a weighted directed graph representation of the ski tracks
- Places control points on specific tracks
- Finds the optimal route using Dijkstra's shortest path algorithm
- Outputs total distance/time and the sequence of junctions to visit

## Requirements

- Java 8 or higher
- JGraphT library (for graph data structures and algorithms)

## Installation

1. Clone this repository:
```bash
git clone https://github.com/yourusername/ski-orienteering-optimizer.git
cd ski-orienteering-optimizer
```

2. Ensure JGraphT is in your classpath or build path. You can add it using Maven by adding this to your pom.xml:
```xml
<dependency>
    <groupId>org.jgrapht</groupId>
    <artifactId>jgrapht-core</artifactId>
    <version>1.5.2</version>
</dependency>
```

## Usage

Run the program with the map file and course file as arguments:

```bash
java -cp .:path/to/jgrapht.jar skio_src.SkiO WSOC15-sprint.map WSOC15-sprint.course
```

### Input Files

#### Map File (WSOC15-sprint.map)

The map file describes the ski tracks network with each line representing a track in the following format:

```
[TrackID] [SourceJunctionID] [DestinationJunctionID] [Type] [ForwardTime] [ReverseTime]
```

For example:
```
T214 J183 J184 track 120.3 135.7
```

This means track T214 connects junction J183 to J184, with a traversal time of 120.3 seconds in the forward direction and 135.7 seconds in the reverse direction.

#### Course File (WSOC15-sprint.course)

The course file defines the start point, finish point, and the control points to visit in order:

```
start [JunctionID]
finish [JunctionID]
controls [NumberOfControls] [ControlTrackID1] [ControlTrackID2] ...
```

For example:
```
start J1
finish J99
controls 3 T5 T42 T67
```

This means the course starts at junction J1, finishes at junction J99, and competitors must visit control points on tracks T5, T42, and T67 in that order.

## Output

The program outputs:

1. The total distance (or time) for the optimal route
2. A list of junctions to visit in sequence

Example:
```
Total Distance: 873.5

Junctions Visited:
-> { ID: J1 }	-> { ID: J4 }	-> { ID: J12 }	-> { ID: J14 }	-> { ID: J23 }	-> { ID: J27 }	-> { ID: J33 }	-> { ID: J42 }	-> { ID: J47 }	-> { ID: J52 }	
-> { ID: J58 }	-> { ID: J63 }	-> { ID: J68 }	-> { ID: J72 }	-> { ID: J79 }	-> { ID: J85 }	-> { ID: J91 }	-> { ID: J95 }	-> { ID: J99 }
```

## How It Works

1. The program reads and parses the map and course files
2. It constructs a directed graph where:
   - Junctions are vertices
   - Tracks are edges with weights corresponding to traversal times
   - Control points are special vertices connected to their corresponding tracks
3. Dijkstra's algorithm finds the shortest path between consecutive points on the course
4. The results are combined to generate the optimal overall route

## Implementation Details

- The `Track` class stores information about each ski track
- The `readMapFile` method parses the map file into Track objects
- The `readCourseFile` method parses the course file into a list of control points
- The `buildGraph` method constructs the directed weighted graph
- The `getPath` method applies Dijkstra's algorithm to find the optimal path

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Uses JGraphT library for graph algorithms
- Inspired by real-world ski orienteering competitions and the World Ski Orienteering Championships (WSOC)