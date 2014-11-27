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

import edu.fit.brees.ego.community.EgoCommunity;
import edu.fit.brees.ego.graph.EgoVertex;
import edu.fit.brees.ego.graph.ProcessingStatus;
import edu.fit.brees.ego.jung.JungNetwork;
import edu.fit.brees.ego.jung.JungVertex;
import edu.fit.brees.ego.loader.CommunityReader;
import edu.fit.brees.ego.loader.DataReader;
import edu.fit.brees.ego.loader.GmlDataReader;
import edu.fit.brees.ego.scoring.Score;
import edu.fit.brees.ego.util.CreateJungNetwork;
import edu.fit.brees.ego.util.Visualize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Main Driver class
 * 
 *  Calling the application with no arguments will cause the usage to be printed.  
 *  
 *  This is the main entry point for executing the FastEgoClustering algorithm
 * 
 * @author bradrees
 *
 */
public class FastEgoDriver 
{
	// user set-able variables
	private int		numberOfThreads			= 0;			// -t
	private String 	fileName				= null;			// -f
	private float	threshold				= 0.75f;		// -o
	private boolean	verbose					= false;		// -v
	private boolean runOtherSteps			= false;		//  auto set
	private boolean runMetrics				= false;		// -m
	private boolean compareGroups			= false;		// -g
	private boolean displayGraph			= false;		// -d
	private boolean skipDetection			= false;		// -z
	private boolean processSpecial			= false;		// -s
	
	private int		startIdx				= 1;			// -s		
	
	// some info on network
	private int		numberOfNodes			= 0;
	private int		numberOfEdges			= 0;
	
	private final	ProcessingStatus	status;
		
	/** Map of all Vertices */
	private Map<Integer, EgoVertex> 	vertexHash		= null;

	// all identified
	private Hashtable<Float, HashSet<Integer>> communities		= null;
	private Hashtable<Float, HashSet<Integer>> tmpCommunities	= null;;
	
	// timing
	private long loadStartTime;
	private long loadEndTime;
	
	private long computeStartTime;
	private long computeEndTime;
	
	private long collectStartTime;
	private long collectEndTime;
	
	private long totalStartTime;
	private long totalEndTime;
	
	private int	iterationCount;
	
	
	
	/**
	 * main 
	 * 
	 * This method simply creates the FastEgoDriver object and starts processing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if ( args.length < 4 ) {
			FastEgoDriver.printUsage();
		} else {
			FastEgoDriver fe = new FastEgoDriver(args);
			fe.run();
			fe.other();
		}
	}
	

	/**
	 * parse the arguments and create all needed variables
	 * 
	 * @param args
	 */
	public FastEgoDriver(String[] args)
	{
		totalStartTime = System.currentTimeMillis();
		
		// parse arguments
		parseArgs(args);
		
		status 			= new ProcessingStatus();
		vertexHash		= new Hashtable <> ();
	}
		
		
	public void run() 
	{
		//-----------------------------------------------------
		// Step 1: load the base Graph
		loadStartTime = System.currentTimeMillis();
		loadData();
		loadEndTime = System.currentTimeMillis();
		
		if ( skipDetection)
			return;
		
		// Step 2
		computeStartTime = System.currentTimeMillis();
		runDetection();
		computeEndTime = System.currentTimeMillis();
		
		// Step 3
		collectStartTime = System.currentTimeMillis();
		extractCommunities();
		collectEndTime = System.currentTimeMillis();
		
		if ( processSpecial)
			processSpecialCaseCommunities();
		
		if ( verbose ) {
			printCommunityStats(communities);
			
			if ( processSpecial )
				printCommunityStats(tmpCommunities);

			System.out.println();
			System.out.println("Data Load time: " + toSeconds(loadEndTime - loadStartTime) );     ;
			System.out.println("Detection Time: "+ toSeconds(computeEndTime - computeStartTime) );
			System.out.println("Group Time: " + toSeconds(collectEndTime - collectStartTime) );
		} else {
			printCompact();
		}
	}
		
		
	/**
	 * Read in the network data
	 * 
	 * @param fileName
	 */
	private void loadData()
	{
		if (verbose)
			System.out.println("Read in data");
				
		// Is this a GML file (Graph Modeling Language)
		if ( fileName.endsWith(".gml") )
		{
			GmlDataReader gmlRead = new GmlDataReader();
			startIdx = gmlRead.read(this.fileName, this.vertexHash, threshold, status);
			numberOfNodes = gmlRead.getNumberOfNodes();
			numberOfEdges = gmlRead.getNumberOfEdges();
		}
		else
		{
			DataReader  dr = new DataReader(verbose);
			dr.read(fileName, vertexHash, startIdx, threshold, status);
			numberOfNodes = dr.getNumberOfNodes();
			numberOfEdges = dr.getNumberOfEdges();
		}
		
		
		for ( EgoVertex v : vertexHash.values()) {
			v.setCommunities(communities);
			v.setTmpCommunities(tmpCommunities);
		}
	}
			
	
	/**
	 * Run the detection process
	 * 
	 * @return runtime
	 */
	private void runDetection()
	{
		iterationCount 	= 0;
		
		final  ExecutorService es 	= Executors.newFixedThreadPool(numberOfThreads);

		int numberOfEgoVertex = vertexHash.size();
	
		if ( verbose) 
			System.out.println("START (" + numberOfEgoVertex + ") with " + numberOfThreads + " threads");
		
		try
		{
			Collection<EgoVertex> agentList = vertexHash.values();
			
			// loop until all EgoVertex are done
			int extraLoop = 4;
			while ( extraLoop > 2) 
			{
				extraLoop = 0;
				status.resetDonCount();
				
				for (EgoVertex e : agentList )
					e.setIdChanged(true);
				
				while (status.getDoneCount() < numberOfEgoVertex )
				{
					++iterationCount;
					++extraLoop;
					status.resetDonCount();

					es.invokeAll(agentList);
						
					if ( verbose)
						System.out.println("\tDone Iteration " + iterationCount + "  Extra = " + extraLoop);
				}
			}
			
			es.shutdown();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(-1);
		}

		if (verbose)
			System.out.println("DONE");
	}
	

	
	
	/**
	 * Take the labels from each vertex and organize them into communities
	 */
	private void extractCommunities()
	{		
		// Create a space to collect the Communities
		communities 	= new Hashtable<>();
		tmpCommunities	= new Hashtable<>();
		
		// get the list of all EgoVertex
		Collection<EgoVertex> agentList = vertexHash.values();
		
		for ( EgoVertex agent : agentList)
		{
			int vertexID = agent.getId();		
			
			// Get all the EgoCommunities associated with this EgoVertex
			Collection<EgoCommunity> ecList = agent.getAllEgoCommunity();
			
			// step through each of the EgoCommunity
			for ( EgoCommunity ec : ecList)
			{
				processEgoCommunity(ec, vertexID);
			}
			
			/*
			 * look at the special list 
			 */
			ecList = agent.getSpecialGroups();

			for ( EgoCommunity ec : ecList)
			{
				processEgoCommunity(ec, vertexID);
			}
		}			
	}
	
	
	/**
	 * 
	 * @param ec
	 * @param id
	 */
	private void processEgoCommunity(EgoCommunity ec, int vertexID)
	{
		int size = ec.size();
		
		if ( size > 2 || size == 1) {
			Float commID = ec.getId();

			// Get the community Set from the master list
			HashSet<Integer> community = communities.get(commID);

			if (community == null) {
				// this is the first time seeing this
				community = new HashSet<Integer>();
				communities.put(commID, community);
			}

			community.add(vertexID);
		}
		else  // the size is 2 or less
		{
			Float commID = ec.getId();

			// Get the community Set from the master list
			HashSet<Integer> community = communities.get(commID);

			if (community == null) {
				// this is the first time seeing this
				community = new HashSet<Integer>();
				tmpCommunities.put(commID, community);
			}
			
			Set<Integer> members = ec.getMembers();
			for ( Integer x : members)
				community.add(x);
		}
	}
	
	
	/**
	 * Process the communities flag as 'special' (meaning that they consist of only two vertices)
	 * 
	 */
	private void processSpecialCaseCommunities()
	{
		List<Float> toDelete	= new ArrayList<>();
		
		Collection<HashSet<Integer>> comm = communities.values();
		
		//-- list of special communities --
		Set<Float> keys = tmpCommunities.keySet();
		
		// Step through all the special communities
		for ( Float idx : keys) 
		{
			// get a tmp community
			Set<Integer> tmp = tmpCommunities.get(idx);
		
			// get the parts that comprise the community
			Integer [] data = tmp.toArray(new Integer[0]);
		
			// If there are only two elements in the special community the,
			// if both are already in other communities, then delete them
			if ( tmp.size() == 2) 
			{
				boolean aFound = false;
				boolean bFound = false;
				
				for ( HashSet<Integer> ego : comm ) {
					if ( ego.contains(data[0]) ) {
						aFound = true;
						break;
					}
				}
			
				for ( HashSet<Integer> ego : comm ) {
					if ( ego.contains(data[1]) ) {
						bFound = true;
						break;
					}
				}
			
				if ( aFound && bFound ) {
					toDelete.add(idx);
				} else
				{
					if ( aFound && ! bFound)
					{
						if ( checkSpecial(data[0], data[1]) )
							toDelete.add(idx);	
					} else {
						if ( checkSpecial(data[1], data[0]) )
							toDelete.add(idx);						
					}
				}
			}
		}
		
		for ( Float k : toDelete) {
			tmpCommunities.remove(k);
		}
	}
	
	
	private boolean checkSpecial(Integer a, Integer b)
	{
		// Add B to community A
		EgoVertex va = this.vertexHash.get(a);
		
		ArrayList<EgoCommunity> c = (ArrayList<EgoCommunity>)va.getAllEgoCommunity();
		
		if ( c.size() == 1) {
			EgoCommunity ec = c.get(a);
			
			HashSet<Integer> cluster = communities.get(ec.getId());
			cluster.add(b);
			return true;
		}
		return false;
	}
		
		
	/**
	 * Run other post-detection tasks
	 */
	public void other()
	{
		if ( ! runOtherSteps )
			return;
				
		if ( runMetrics ) 
			computeMetrics();
	
		
		if ( compareGroups )
			computeScore();
		
		
		if ( displayGraph)
			displayJungGraph();
	}
	
	
	/**
	 * 
	 */
	private void computeMetrics()
	{
		float edgeCount = 0;
		int max = 0;
			
		Collection<EgoVertex> nodes = this.vertexHash.values();
		for ( EgoVertex v : nodes ) {
			if ( v.getNeighborCount() > max)
				max = v.getNeighborCount();
			
			edgeCount += v.getNeighborCount();;
		}
		
		System.out.println("Grap[h Metrics:");
		System.out.println("\tVertices:  \t"  + numberOfNodes );
		System.out.println("\tEdges:     \t"  + (edgeCount/2)  );
		System.out.println("\tDensity:   \t"  + ( edgeCount) / (numberOfNodes * (numberOfNodes-1)) );
		System.out.println("\tAvd Degree:\t"  + ( edgeCount) / (numberOfNodes) );		
		System.out.println("\tMax Degree:\t"  + max);	
	}
	
	
	/**
	 * If the dataset has a file containing the set of communities, use that to score the results
	 * 
	 */
	private void computeScore()
	{
		try {
			//--- Read in the community list
			CommunityReader cr = new CommunityReader();
			cr.setVerbose(verbose);

			String groupsFile = fileName.replace("network", "community");	// get path to community file

			Hashtable<Integer, HashSet<Integer>> groups = cr.read(groupsFile);		// read in the data

			Score scorer = new Score();
			scorer.computeMutalInformationScore(communities, groups);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	/**
	 * 
	 */
	private void displayJungGraph()
	{
		// create the JUNG network
		Collection<EgoVertex> nodes = vertexHash.values();
		JungNetwork graph = CreateJungNetwork.create(nodes);  
		
		
		//----------------------------------------------------
		//--- Read in the community list

		if ( fileName.contains("network") ) {
			CommunityReader cr = new CommunityReader();
			cr.setVerbose(verbose);


			String groupsFile = fileName.replace("network", "community");	// get path to community file

			Hashtable<Integer, HashSet<Integer>> groups = cr.read(groupsFile);		// read in the data

			System.out.println("\tLFR Communities:        " + groups.size());

			if ( ! this.skipDetection)
				System.out.println("\tEgoCluster Communities: " + communities.size());

			Set<Integer> keys = groups.keySet();

			for ( Integer key : keys) {
				Collection<Integer> verts = groups.get(key);

				for ( Integer v : verts) {
					JungVertex n = graph.findVertex(v);
					n.setTag(key);
				}
			}
		}

		Visualize.visualizeColor(graph, "100", Visualize.SPRING2);

	}
	
	
	
	
	public void printCommunityStats(Hashtable<Float, HashSet<Integer>> comm)
	{
		Set<Float> keys = comm.keySet();
				
		System.out.println();
		
		System.out.println("-------------------------------------------");
		System.out.println("Found Communities: " + keys.size());
		
		for ( Float k : keys)
		{
			HashSet<Integer> set = comm.get(k);
			
			System.out.println("Community (" + k + ")" );
			System.out.println("\t" + set);
			System.out.println();
		}
	}
	
	
	private void printCompact()
	{
		System.out.print( toSeconds(totalEndTime - totalStartTime) );					// "Total time: "
		System.out.print("\t" + toSeconds(loadEndTime - loadStartTime) );				// "Data Load time: "
		System.out.print("\t" + toSeconds(computeEndTime - computeStartTime) );		// "Detection Time: "+ 
		System.out.print("\t" + toSeconds(collectEndTime - collectStartTime) );		// "Group Time: " + 
		System.out.print("\t" + numberOfThreads);
		System.out.print("\t" + threshold);
		System.out.print("\t" + communities.size() );
		System.out.print("\t" + iterationCount);
		System.out.print("\t" + numberOfNodes);
		System.out.print("\t" + numberOfEdges);
		System.out.println();
	}
	
	
	private double toSeconds(long time) {
		return ((double)time)/1000;
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
			
			case "-d":
				displayGraph = true;
				runOtherSteps = true;
				break;
			case "-g":
				compareGroups = true;
				runOtherSteps = true;
				break;
			case "-f":
				fileName =  args[i++];
				break;
			case "-m":
				runMetrics = true;
				runOtherSteps = true;
				break;
			case "-o":
				threshold = Float.valueOf(args[i++]);
				break;
			case "-s":
				processSpecial = true;
				break;
			case "-t":
				numberOfThreads = Integer.valueOf(args[i++]);
				break;
			case "-v":
				verbose = true;
				break;
			case "-z":
				skipDetection = true;
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
	 * Print the usage statement
	 */
	public static void printUsage() 
	{
		System.out.println("FastEgoDriver <arguments>");
		System.out.println("-t <int>   \tNumber of threads defaukt is (cores * 2)");
		System.out.println("-o <float> \tThe Overlap Threshold");
		System.out.println("-f <path>   \tFile Name");
		System.out.println("-v          \tVerbose output");
		System.out.println("-m          \tGraph and commubnity metrics");
		System.out.println("-d          \tDisplay the network (JUNG)");
		System.out.println("-z          \tSkip Detection (using if doing -d or -m)");
		System.out.println("-g          \tScore Results");
		System.out.println("-s          \tProcess special vertices");	

	}

}
