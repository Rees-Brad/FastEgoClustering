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

import edu.fit.brees.ego.graph.EgoVertex;
import edu.fit.brees.ego.jung.JungNetwork;
import edu.fit.brees.ego.jung.JungVertex;

public class CreateJungNetwork 
{

	
	public static JungNetwork create( Collection<EgoVertex> nodes )
	{
		
		JungNetwork graph = new JungNetwork();
		
		// Create all the vertices
		for ( EgoVertex n : nodes) {
			JungVertex v = new JungVertex(n.getId(), n.getName());
			graph.addVertex(v);
		}
		
		// now add all the edges
		for ( EgoVertex n : nodes) {
			Collection<EgoVertex> friends = n.getNeighbors();
			
			JungVertex a = graph.findVertex(n.getId());
			
			for ( EgoVertex v2 : friends) {
				JungVertex b = graph.findVertex(v2.getId());
			
				graph.addEdge(" ", a, b);
			}
		}
		return graph;
		
	}
	
	
	
	
	
}
