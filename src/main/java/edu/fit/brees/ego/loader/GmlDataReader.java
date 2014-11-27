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

import edu.fit.brees.ego.graph.EgoVertex;
import edu.fit.brees.ego.graph.ProcessingStatus;


/**
 * This class reads GML graph data in the form of
 * The id of a vertex is always a number
 * 
 * 
 * 
 * # comment line
 * 
 *   node
 * [
 *   id 1
 *   label "This is Node A"
 * ]
 * 
 * node [
 *   id 2
 *   label "This is Node B"
 *   graphics [
 *     x 22.222
 *     y 33.33
 *     w 34.000
 *     h 55.000
 *     type "oval"
 *   ]
 *  ]
 * edge
 * [
 *   label "string"
 *   source A
 *   target B
 *   value 	floatWeight
 * ]
 * 
 * 
 * 
 * 
 * @author bradrees
 *
 */
public class GmlDataReader
{	
	private 	BufferedReader 		buffRead			= null;
	private 	ProcessingStatus 	status 				= null;
	private 	float				overlapThreshold	= 0.0f;
	private		int					firstIndex			= 99999;
	
	// global just so that they can be used in debug statements if an error occurs
	private int edgeCount = 0;
	private int nodeCount = 0;
	
	public int getNumberOfNodes() {
		return nodeCount;
	}


	public int getNumberOfEdges() {
		return edgeCount;
	}
	
	public  GmlDataReader()
	{
		
	}

	
	/**
	 * 
	 * @param inputFile
	 * @param vertexMap
	 * @param overlap
	 * @param status
	 * @return int - the starting index value (typically 0 or 1)
	 */
	public int read(String inputFile, Map<Integer, EgoVertex> vertexMap, 
			float overlap, ProcessingStatus status)
	{
		// save some values that will be passed into every vertex
		this.overlapThreshold = overlap;
		this.status 		= status;
		
		//System.out.println("READING:  " + inputFile);
		
		try
		{
			// INPUT
			FileReader fileReader = new FileReader(inputFile);
			buffRead = new BufferedReader(fileReader);
			
			String line = buffRead.readLine().trim();
			
			while (line != null)
			{
				// get a line and remove all leading white space
				line = line.trim();
				
				// line type
				switch (line)  {
				case "#":
					//System.out.println("Found Comment ");
					break;
				case "node":
				case "node [":
					getNode(vertexMap, line);
					nodeCount++;
					break;
				case "edge":
				case "edge [":
					getEdge(vertexMap, line);
					edgeCount++;
					break;
				default:
					//ignore this line
				}
				// get the next line
				line = buffRead.readLine();
			}
		} 
		catch (IOException ioe)
		{
			System.err.println("ERROR: ");
			ioe.printStackTrace();
			System.exit(-1);
		}
		
		//System.out.println("\n\tDONE Reading  ");
		//System.out.println("\tCreated " + nodeCount );
		//System.out.println("\tCreated " + edgeCount + " edges\n");
		
		return this.firstIndex;
	}
	
	
	/**
	 * Read in a Node block
	 * 
	 * @param vertexMap
	 * @return
	 * @throws IOException
	 */
	private boolean getNode(Map<Integer, EgoVertex> vertexMap, String lastLine) throws IOException
	{
		// could be a [ or the ID line
		String nodeLine = buffRead.readLine().trim();
		
		if ( ! lastLine.contains("["))	{	
			if ( ! nodeLine.startsWith("[") ) {
				System.err.println("Last line: " + lastLine);
				System.err.println("this line: " + nodeLine);
				System.err.println("nodes procceded:" + this.nodeCount);
				throw new IOException("Node [ missing on line " + nodeLine);
			}
			
			// get the next lines - must be the node ID:   <tab>id #
			nodeLine = buffRead.readLine().trim();
		}
		
		
		// get the ID after removing the "id" part
		int id = Integer.parseInt(nodeLine.substring(3).trim());
		
		if ( id < firstIndex)
			firstIndex = id;
				
		// get the label 
		nodeLine = buffRead.readLine().trim();
		
		// remove the "label" part
		String label = nodeLine.substring(6).trim();		
		
		EgoVertex agent = new EgoVertex(label, id, this.overlapThreshold, this.status);
		vertexMap.put(id, agent);
		
		// ignore the rest of the lines
		
		return true;
	}
	
	
	/**
	 * 
	 * @param vertexMap
	 * @return
	 * @throws IOException
	 */
	protected boolean getEdge(Map<Integer, EgoVertex> vertexMap, String lastLine) throws IOException
	{
		// next line could be [
		String line = buffRead.readLine().trim();
		
		if ( ! lastLine.contains("["))	{	
			if ( ! line.startsWith("[") ) {
				System.err.println("Last line: " + lastLine);
				System.err.println("this line: " + line);
				System.err.println("edges procceded:" + this.edgeCount);
				System.exit(-1);
			}
			
			line = buffRead.readLine().trim();		
		}
		
		// if there is a label, then it will be the first line
		if ( line.contains("label") ) {
			//label = line.substring(5).trim();
			line = buffRead.readLine().trim();		
		}
		
		// get the source node ID:   <tab>id #
		int a = Integer.parseInt(line.substring(6).trim() );
	
		// get the target node ID:   <tab>id #
		line = buffRead.readLine().trim();
		int b = Integer.parseInt(line.substring(6).trim() );
		

		EgoVertex agentA = vertexMap.get(a);
		EgoVertex agentB = vertexMap.get(b);
		
		agentA.addNeighbor(agentB);
		agentB.addNeighbor(agentA);
			
		return true;		
	}
}
