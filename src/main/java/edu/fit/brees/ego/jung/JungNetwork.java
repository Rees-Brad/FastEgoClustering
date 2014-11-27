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
package edu.fit.brees.ego.jung;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;


import java.util.Set;

public class JungNetwork extends UndirectedSparseGraph<JungVertex, JungEdge>
{
	private static final long	serialVersionUID	= 190470075572556544L;
	private int		edgeId							= 0;
	
	public JungVertex findVertex(int id)
	{
		
		Set<JungVertex> keys = this.vertices.keySet();
		
		for ( JungVertex v : keys)
		{
			if (v.getId() == id )
				return v;
		}
		
		return null;
	}
	
	public boolean addEdge(String txt, JungVertex a, JungVertex b)
	{
		
		JungEdge e = new JungEdge(edgeId++, txt);
		return addEdge(e, a, b);
		
	}
	

	public JungVertex addVertex(int id, String name)
	{
		JungVertex node = new JungVertex(id, name);
		addVertex(node);
		return node;
	}

}
