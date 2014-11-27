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
import java.util.Hashtable;
import java.util.Stack;
import java.util.ArrayList;

import edu.fit.brees.ego.community.EgoCommunity;
import edu.fit.brees.ego.graph.EgoVertex;
import edu.fit.brees.ego.jung.JungNetwork;
import edu.fit.brees.ego.jung.JungVertex;


public class ExtractEgoCommunities
{
	/**
	 * Given an EgoVertex, this method returns a collection of friendship-group graphs centered on 
	 * the passed in ego-node
	 * 
	 * @param egoNet	- the graph from which friendship groups are extracted
	 * @param egoNode	- the central ego node
	 * @return
	 */
	public static ArrayList<EgoCommunity> extract(EgoVertex baseVertex) throws Exception
	{
		Hashtable<Integer, EgoCommunity>	groups 	= new Hashtable<>();	// what will be returned
		
		Stack<JungVertex> allVertices 	= new Stack<>();
		Stack<JungVertex> vertexToProcess = new Stack<>();
		
		// create the JUNG graph that supports the ego net detection
		JungNetwork egoNet	= new JungNetwork();
		JungVertex	egoNode = new JungVertex(baseVertex.getId(), baseVertex.getName());
		egoNet.addVertex(egoNode);
		
		// the portions of the ego-community ID
		int 	baseID 	= baseVertex.getId();
		int		groupID	= 0;						// this will be incremented 
		
		
		// get the neighbors
		Collection<EgoVertex> neighbors = baseVertex.getNeighbors();
		
		// step though each neighbor a
		for ( EgoVertex friend : neighbors)
		{
			JungVertex node = new JungVertex(friend.getId(), friend.getName());
			egoNet.addEdge("edge", egoNode, node);		
			allVertices.push(node);
		}
			
		// get the half-hops: i.e. the between neighbor edges
		for ( EgoVertex friend : neighbors)
		{
			JungVertex freindNode = egoNet.findVertex(friend.getId() );
			
			Collection<EgoVertex> oneHop = friend.getNeighbors();
			
			for (EgoVertex f2 : oneHop)
			{
				JungVertex f2Node = egoNet.findVertex(f2.getId());

				if (f2Node != null)
				{
					egoNet.addEdge("edge", freindNode, f2Node);
				}
			}
		}
	
		// remove the ego-vertex.  This will also cause all edges to the vertex to be removed
		egoNet.removeVertex(egoNode);
		
		// this is just a modified BFS
		while ( ! allVertices.isEmpty() ) {
			groupID++;
			
			EgoCommunity ec = new EgoCommunity();
			float id = Float.parseFloat(baseID + "." + groupID);			
			ec.setId(id);				// set the ego-community ID
			ec.addToList(baseID);		// add the ego-vertex to the list
			ec.setChanged(true);		//  mark it as being changed
			groups.put(groupID, ec);	// save in the hashtable for processing
			
			JungVertex jVertex =  allVertices.pop();
			jVertex.setTag(groupID);
			vertexToProcess.push(jVertex);

			while (! vertexToProcess.isEmpty() ) {
				JungVertex v = vertexToProcess.pop();
				
				EgoCommunity group = groups.get(v.getTag());		// the the ego-community
				group.addToList(v.getId());							// add this vertex to it based on the ID

				Collection<JungVertex> egoNeighbors = egoNet.getNeighbors(v);

				if ( egoNeighbors != null)		// removing vertices could leave this as a singleton
				{
					for ( JungVertex v2 : egoNeighbors) {
						v2.setTag(groupID);
						vertexToProcess.push(v2);
						allVertices.remove(v2);
					}
				}
				
				// remove the vertex from the egonet.  
				// This will keep the process from coming back around to the same vertex
				egoNet.removeVertex(v);
			}
		}
			
		ArrayList<EgoCommunity> answer = new ArrayList<>(groups.values());
		return answer;
	}	
}
