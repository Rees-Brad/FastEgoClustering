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
package edu.fit.brees.ego.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import edu.fit.brees.ego.graph.EgoVertex;
import edu.fit.brees.ego.graph.ProcessingStatus;


/**
 * This class reads in graph data in the form of
 * 
 * # comment line
 * *vertices	##    the number of vertices
 *    If the vertices have attributes they are listed next 
 *    vertex id		name	weight
 * *Edges
 * 		vertex id	vertex id	weight
 * 
 * 
 * @author bradrees
 *
 */
@SuppressWarnings("unused")
public class DataReader
{
	private final static int	VERTEX	= 0;
	private final static int 	EDGE		= 1;
	
	private int startValue	= 1;

	private int	processing = -1;

	private float threshold	= 0.75f;
	
	private ProcessingStatus	status = null;
	
	private int numberOfNodes		= 0;
	private int numberOfEdges		= 0;
	private boolean verbose			= false;
	
	
	public int getNumberOfNodes() {
		return numberOfNodes;
	}


	public int getNumberOfEdges() {
		return numberOfEdges;
	}


	public DataReader()
	{
		;
	}
	
	public DataReader(boolean verbose)
	{
		this.verbose = verbose;
	}
	
	public void read(String inputFile, Map<Integer, EgoVertex> vertexMap, int startIndex, float threshold, 
			ProcessingStatus status)
	{
		startValue = startIndex;
		this.threshold = threshold;
		this.status = status;
		doRead(inputFile, vertexMap);
	}

	

	
	
	private void doRead(String inputFile, Map<Integer, EgoVertex> vertexMap)
	{
		int edgeCount	= 0;
		int loopCounter	= 0;
		int lineCounter	= 0;
		String line = null;
		
		if ( verbose) {
			System.out.println("------\nDataReader READING:  " + inputFile);
		}
		
		
		try
		{
			// INPUT
			FileReader fileReader = new FileReader(inputFile);
			BufferedReader buffRead = new BufferedReader(fileReader);
			
			line = buffRead.readLine();

			while (line != null)
			{
				// just for debug purpose - prints a dot every 1000 lines read
				++loopCounter;
				
				if ( loopCounter > 1000)
				{
					//System.out.print(".");
					loopCounter = 0;
					
					++lineCounter;
					
					if ( lineCounter == 50)
					{
						//System.out.print("\n");
						lineCounter = 0;
					}
				}
				
				//----------------------------------------------------------------------
				//----------------------------------------------------------------------				
				//----------------------------------------------------------------------
				
				// line type
				if ( line.startsWith("#") )
				{
					// comment line so skip
					//System.out.println("Found Comment ");
				}
				else if ( line.startsWith("*") )
				{
					// need to determine if it is Vertex, Arcs, or Edges
					if (line.contains("Vertices"))
					{
						processing = VERTEX;
						
						// extract the number of vertices from the line 
						int idx = line.indexOf("Vertices");
						idx += 9;
						
						int nodeCount = Integer.parseInt(line.substring(idx).trim());
						//System.out.println("Found Vertex; " + nodeCount);
						
						createAgents(vertexMap, nodeCount);
					}
					
					else if(line.contains("Edges"))
					{
						processing = EDGE;
					}	
					else if(line.contains("Arcs"))
					{
						processing = EDGE;
					}	
					else
						processing = -1;
				}
				else
				{
					StringTokenizer strTok = new StringTokenizer(line);
					
					switch(processing)
					{
						case VERTEX:
							int id = Integer.parseInt(strTok.nextToken());
							String label = strTok.nextToken();
							break;			
							
						case EDGE:
							int wt = 1;
							Integer a = new Integer(strTok.nextToken());
							Integer b = new Integer(strTok.nextToken());
							
							//if(strTok.hasMoreTokens()) 
							//	wt = Integer.parseInt(strTok.nextToken());
							
							String lb = new String( a + "-" + b);
							
							//System.out.println(" -- creating edge " + lb); 	/* a debug line */
							
							// the Agent enforces a single link per neighbor, so don't worry about it here
							EgoVertex agentA = vertexMap.get(a);
							EgoVertex agentB = vertexMap.get(b);
							
							if ( agentA == null)
							{
								agentA = createAgent(vertexMap, a);
							}

							if ( agentB == null)
							{
								agentB = createAgent(vertexMap, b);
							}
							
							agentA.addNeighbor(agentB);
							agentB.addNeighbor(agentA);
							edgeCount++;

							break;
							
						default:
							//System.out.println("do nothing on line: ");
							System.exit(-1);
					}
				}
				
				// get the next line
				line = buffRead.readLine();
			}
			
			buffRead.close();
			fileReader.close();
		} 
		catch (IOException ioe)
		{
			System.err.println("ERROR: ");
			System.out.println("line " + line);
			ioe.printStackTrace();
			System.exit(-1);
		}
		
		if ( verbose ) {
			System.out.println("\n\tDONE Reading  ");
			System.out.println("\tCreated " + (edgeCount) + "  edges");
			System.out.println("\tCreated " + vertexMap.size() + " Vertices\n");
		}
		
		this.numberOfEdges = edgeCount;

	}

	
	/**
	 * 
	 * @param swarm
	 * @param count
	 */
	protected void createAgents(Map<Integer, EgoVertex> vertexMap, int count)
	{
		int id = this.startValue;

		for ( int n = 0; n <  count; n++)
		{
			String name = new String("ID: " + n);
			EgoVertex agent = new EgoVertex(name, id, threshold, status);

			vertexMap.put(id, agent);
			++id;
		}
		
		//System.out.println("\tCreated " + count + " SwarmAgents");
		this.numberOfNodes = count;
	}
	
	
	
	/**
	 * 
	 * @param swarm
	 * @param count
	 */
	protected EgoVertex createAgent(Map<Integer, EgoVertex> vertexMap, int id)
	{
		String name = new String("ID: " + id	);
		EgoVertex agent = new EgoVertex(name, id, threshold, status);

		vertexMap.put(id, agent);
		
		++numberOfNodes;
		return agent;
	}
	
}
