package skio-src;

// We're leveraging JGraphT - a popular, Java library for graphs
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge; 
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ski-O problem implementation which finds the fastest path given start finish,
 * while making sure all the controls are visited in the right order
 */
public class SkiO {

	/*
	 *  Helper class to store info about each track from the map file.
	 *  Track splits two junctions, having different times depending on which way
	 *  you go
	 */
	private static class Track {

		String srcID;
		String dstID;
		double length; 
		double revLength;
		
		Track ( String srcID, String dstID, double length, double revLength ) {
			this.srcID = srcID;
			this.dstID = dstID;
			this.length = length;
			this.revLength = revLength;
		}
	}

	public static void main ( String[] args ) {

		/*
		 *  Extract the map file and the course file names from command line,
		 *  output error if input is in unexpected format
		 */
		
		if ( args.length != 2 ) {
			System.err.println("Usage: java SkiO <map_file> <course_file>");
			
			// Stop the program if we don’t have the right number of files
			return;
		}

		String mapFile = args[0]; 
		String courseFile = args[1]; 

		try {
			
			// Tracks as vertices
			Map<String,Track> tracks = readMapFile(mapFile);
			
			// Courses as edges
			List<String> course = readCourseFile(courseFile);

			// Initiate the graph from read file contents
			SimpleDirectedWeightedGraph<String,DefaultWeightedEdge> graph =
					buildGraph(tracks,course);

			// Find the shortest path from start to finish
			getPath(graph,course,tracks);

		} catch ( IOException e ) {
			System.err.println("Error reading files: " + e.getMessage());
		}
	}

	private static Map<String,Track> readMapFile ( String mapFile )
			throws IOException {
		
		Map<String,Track> tracks = new HashMap<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(mapFile))) {
			
			String line; 
			while ( (line = br.readLine()) != null ) {
				
				// Split the string and store
				String[] parts = line.trim().split("\\s+");
				
				// Check for correct input & format of string
				if ( parts.length != 6 ) continue;
				
				String trackId = parts[0];
				String srcId = parts[1];
				String dstId = parts[2];
				double length = Double.parseDouble(parts[4]);
				double revLength = Double.parseDouble(parts[5]);
				tracks.put(trackId,new Track(srcId,dstId,length,revLength));
			}
		}
		
		return tracks;
	}

	private static List<String> readCourseFile ( String courseFile )
			throws IOException {
		
		List<String> course = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(courseFile))) {

			String startLine = br.readLine();

			if ( startLine != null ) {

				// Split the string and store
				String[] startParts = startLine.trim().split("\\s+");
				
				// Check for correct input & format of string
				if ( startParts.length == 2 && startParts[0].equals("start") ) {
					
					course.add(startParts[1]);
				}
			}

			String finishLine = br.readLine();

			if ( finishLine != null ) {
				
				// Split the string and store
				String[] finishParts = finishLine.trim().split("\\s+");
				
				// Check for correct input & format of string
				if ( finishParts.length == 2 && finishParts[0].equals("finish") ) {

					course.add(finishParts[1]);
				}
			}


			String controlsLine = br.readLine();

			if ( controlsLine != null ) {

				// Split the string and store
				String[] controlParts = controlsLine.trim().split("\\s+");

				// Check for correct input & format of string
				if ( controlParts.length >= 2 && controlParts[0].equals("controls") ) {
					
					int numControls = Integer.parseInt(controlParts[1]);

					for ( int i = 2 ; i < controlParts.length
							&& i < 2 + numControls ; i++ ) {

						course.add(course.size() - 1,"C_" + controlParts[i]);
					}
				}
			}
		}

		
		return course;
	}

	private static SimpleDirectedWeightedGraph<String,DefaultWeightedEdge> buildGraph ( Map<String,Track> tracks,
	                                                                                    List<String> course ) {

		SimpleDirectedWeightedGraph<String,DefaultWeightedEdge> graph =
				new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		for ( Track track : tracks.values() ) {
			
			// Add the source junction 
			graph.addVertex(track.srcID);
			// Add the destination junction 
			graph.addVertex(track.dstID);
		}

		for ( String vertexId : course ) {

			if ( vertexId.startsWith("C_") ) {
				
				graph.addVertex(vertexId);
			}
		}

		// Match the edges and their corresponding weights
		for ( Map.Entry<String,Track> entry : tracks.entrySet() ) {
			
			String trackID = entry.getKey();
			Track track = entry.getValue();

			DefaultWeightedEdge edgeForward = graph.addEdge(track.srcID,track.dstID);
			
			// JGraphT returns null if the edge already exists (no duplicates)
			if ( edgeForward != null ) {

				graph.setEdgeWeight(edgeForward,track.length);
			}

			// Account for both directions when adding edges
			DefaultWeightedEdge edgeReverse = graph.addEdge(track.dstID,track.srcID);
			if ( edgeReverse != null ) {

				graph.setEdgeWeight(edgeReverse,track.revLength);
			}

			if ( course.contains("C_" + trackID) ) {
				
				// The control vertex for this track (ex: "C_T214")
				String controlID = "C_" + trackID;
				
				// Add an edge from the control to the source junction (ex: C_T214 → J183)
				DefaultWeightedEdge controlToSrc = graph.addEdge(controlID,track.srcID);
				
				if ( controlToSrc != null ) {
					
					// Split the track’s length for edge weight (leveraging control points)
					graph.setEdgeWeight(controlToSrc,track.length / 2.0);
				}

				DefaultWeightedEdge controlToDst = graph.addEdge(controlID,track.dstID);
				
				if ( controlToDst != null ) {
					
					// Split the track’s length for edge weight (leveraging control points)
					graph.setEdgeWeight(controlToDst,track.length / 2.0);
				}

				DefaultWeightedEdge srcToControl = graph.addEdge(track.srcID,controlID);
				
				if ( srcToControl != null ) {

					// Split the track’s length for edge weight (leveraging control points)
					graph.setEdgeWeight(srcToControl,track.revLength / 2.0);
				}
				
				DefaultWeightedEdge dstToControl = graph.addEdge(track.dstID,controlID);
				
				if ( dstToControl != null ) {
					
					// Split the track’s length for edge weight (leveraging control points)
					graph.setEdgeWeight(dstToControl,track.revLength / 2.0);
				}
			}
		}

		return graph;
	}

	private static void getPath ( SimpleDirectedWeightedGraph<String,DefaultWeightedEdge> graph,
	                                           List<String> course,
	                                           Map<String,Track> tracks ) {
		
		// Make a Dijkstra’s algorithm object using our graph implementation
		DijkstraShortestPath<String,DefaultWeightedEdge> dijkstra =
				new DijkstraShortestPath<>(graph);

		// Keep track of the total time it takes to go through the whole course.
		double totalDistance = 0.0;
		
		// Make a list to store the junctions we visit (excluding control points)
		List<String> junctionPath = new ArrayList<>();

		for ( int i = 0 ; i < course.size() - 1 ; i++ ) {
			
			String sourceID = course.get(i);
			String targetID = course.get(i + 1);

			// Implement Dijkstra’s to find the shortest path from src to dst.
			org.jgrapht.GraphPath<String,DefaultWeightedEdge> path =
					dijkstra.getPath(sourceID,targetID);

			// If path is null we output an error and stop.
			if ( path == null ) {
				System.err
				.println("No path exists between " + sourceID + " and " + targetID);
				return;
			}

			totalDistance += path.getWeight();

			// Get all the vertices (junctions) in this path
			for ( String vertexId : path.getVertexList() ) {
				
				// If the vertex is a junction add it to the list.
				if ( vertexId.startsWith("J") ) {
					
					junctionPath.add(vertexId);
				}
			}
		}

		 System.out.println("Total Distance:\t" + totalDistance);
     System.out.println("\nJunctions Visited:");
     int printCap = 10;
     
     for (String junction : junctionPath) {
    	 
    	 if (printCap != 0) {
    		 System.out.printf("-> { ID: %s }\t", junction); printCap--;
    	 }
    	 else {
    		 System.out.printf("-> { ID: %s }\n", junction);
    		 printCap = 10;
    	 }
     }
	}
}
