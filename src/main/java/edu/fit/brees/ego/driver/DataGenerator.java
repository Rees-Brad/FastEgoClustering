/*
 * 
 * Copyright 2014 Bradley S. Rees
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 */
package edu.fit.brees.ego.driver;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.collections15.Factory;

import edu.fit.brees.ego.jung.JungEdge;
import edu.fit.brees.ego.jung.JungNetwork;
import edu.fit.brees.ego.jung.JungVertex;
import edu.fit.brees.ego.util.Metrics;
import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;


/**
 * This class creates random networks 
 * The class uses the data generator in the JUNG library
 * 
 * 
 * @author bradrees
 *
 */
public class DataGenerator 
{
	public static int	vertexFactoryID			= 1;
	public static int	edgeFactoryID			= 1;

	private String 	fileName				= null;			// -f
	private int		numberOfNodes			= 0;
	private double	density					= 0;
	private boolean verbose					= false;
	
	
	/**
	 * Print the usage statement
	 */
	public static void printUsage() 
	{
		System.out.println("FastEgoDriver <arguments>");
		System.out.println("-n <int>   \tNumber of vertices");
		System.out.println("-e <int>   \tNumber of edges");
		System.out.println("-d <int>   \tDensity");
		System.out.println("-f <int>   \tOutput File Name+ Path");
		System.out.println("[-v]       \t print to screen");
	}

	
	
	public DataGenerator(String[] args)
	{
		// parse arguments
		parseArgs(args);
	}
	
	public void run()
	{
		Writer out;

		Factory<JungVertex> vertexFactory = new Factory<JungVertex>() {
			public JungVertex create() {
				return new JungVertex(DataGenerator.vertexFactoryID, "vertex:" + DataGenerator.vertexFactoryID++);
			}
		};
		
		Factory<JungEdge> edgeFactory = new Factory<JungEdge>() {
			public JungEdge create() {
				return new JungEdge(DataGenerator.edgeFactoryID, "edge:" + DataGenerator.edgeFactoryID++);
			}
		};	
		
		Factory<UndirectedGraph<JungVertex, JungEdge>> graphFactory = 
				new Factory<UndirectedGraph<JungVertex, JungEdge>>() {
			public UndirectedGraph<JungVertex, JungEdge> create() {
				return new JungNetwork();
			}
		};			
		
		 GraphGenerator <JungVertex, JungEdge>	gen;
		 
		 gen = new ErdosRenyiGenerator<>(
					 graphFactory, vertexFactory, edgeFactory,
					 numberOfNodes, density
					 );
		 
		 /*
		 else {
			 gen = new BarabasiAlbertGenerator<>(
					 graphFactory, vertexFactory, edgeFactory,
					 numberOfNodes, 
					 );
		 }
		*/ 
		 
		 
		Graph<JungVertex, JungEdge> network = gen.create();
		
		System.out.println("Metrics:");
		System.out.println("\tNodes:  \t" + Metrics.order(network));
		System.out.println("\tEdges:  \t" + Metrics.size(network));
		System.out.println("\tDensity:\t" + Metrics.density(network));
		

		try {
			if ( verbose ) {
				out = new BufferedWriter(new OutputStreamWriter( System.out ));
			} else {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
			}
			
			//writer.save(network, out);
			out.write("# Metrics: \n");
			out.write("#\tNodes:  \t" + Metrics.order(network) + "\n");
			out.write("#\tEdges:  \t" + Metrics.size(network) + "\n");
			out.write("#\tDensity:\t" + Metrics.density(network) + "\n");
			out.write("*Vertices\t" + numberOfNodes + "\n");
			out.write("*Edges\n");
			
			
			Collection<JungEdge> edges = network.getEdges();
			for ( JungEdge e : edges ) {
				Pair<JungVertex> pair = network.getEndpoints(e);
				out.write(pair.getFirst().getId() + "\t" + pair.getSecond().getId() + "\n");
			}


			out.flush();
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * Parse the input arguments
	 * 
	 * @param args
	 */
	private void parseArgs(String [] args)
	{
		int i = 0;
		String arg;
		
		while (i < args.length && args[i].startsWith("-")) 
		{
			arg = args[i++];
			
			switch (arg) {
			
			case "-f":
				fileName =  args[i++];
				break;
			case "-n":
				numberOfNodes = Integer.valueOf(args[i++]);
				break;
			case "-d":
				density = Double.valueOf(args[i++]);
				break;
				case "-v":
					verbose = true;
					i++;
					break;
			default:
				System.out.println("Unknown argument " + arg + "  exiting");
				for ( String s : args)
					System.out.println("\tArgs:  " + s);
				System.exit(-1);
				break;
			}
		}
	}	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if ( args.length < 4 ) {
			DataGenerator.printUsage();
		} else {
			DataGenerator fe = new DataGenerator(args);
			fe.run();
		}
	}

}
