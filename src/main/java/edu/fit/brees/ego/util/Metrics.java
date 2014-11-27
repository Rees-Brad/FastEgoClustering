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
package edu.fit.brees.ego.util;

import java.util.Collection;

import edu.fit.brees.ego.jung.JungEdge;
import edu.fit.brees.ego.jung.JungVertex;
import edu.uci.ics.jung.graph.Graph;


public class Metrics 
{

	/**
	 * Return the Order of the graph (e.g. the number of nodes)
	 * 
	 * @param graph
	 * @return - order
	 */
	public static int order(Graph<JungVertex, JungEdge> graph)
	{
		return graph.getVertexCount();
	}
	
	
	/**
	 * Return the size of the graph (e.g. the number of edges)
	 * 
	 * @param graph
	 * @return - size
	 */
	public static int size(Graph<JungVertex, JungEdge> graph)
	{
		return graph.getEdgeCount();
	}
	
	
	/**
	 * Return the density of the graph, given as
	 * 
	 *      2 * E
	 * d = ---------
	 *     N * (N-1)
	 *     
	 * @param graph
	 * @return
	 */
	public static double density(Graph<JungVertex, JungEdge> graph)
	{
		float numberOfNodes = graph.getVertexCount();
		float numberOfEdges = graph.getEdgeCount();
		
		double density = ( 2.0 * numberOfEdges) / (numberOfNodes * (numberOfNodes-1));
		return density;
	}
	
	/** 
	 * Return the average degree
	 * 
	 *  (2 * num Edges )  / ( num of nodes) 
	 * 
	 * @param graph
	 * @return
	 */
	public static double averageDegree(Graph<JungVertex, JungEdge> graph)
	{
		float numberOfNodes = graph.getVertexCount();
		float numberOfEdges = graph.getEdgeCount();
		
		double avgDegree = ( 2.0 * numberOfEdges) / (numberOfNodes);
		return avgDegree;
	}
	
	public static int maxDegree(Graph<JungVertex, JungEdge> graph)
	{
		int degree = 0;
		
		Collection<JungVertex> nodes = graph.getVertices();
		
		for ( JungVertex v : nodes) {
			int d = graph.degree(v);
			
			if ( d > degree)
				degree = d;
		}
		
		return degree;
	}

	
	
	
	
	
	
	/**
	 * Return the diameter of the graph.
	 * Given as the longest shortest-path in the graph
	 * 
	 * 
	 * @param graph
	 * @return
	 */
	public static int diameter(Graph<JungVertex, JungEdge> graph)
	{
		
		return 1;
	}
	
	
	
}
