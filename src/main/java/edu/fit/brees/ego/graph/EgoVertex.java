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
package edu.fit.brees.ego.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.fit.brees.ego.community.EgoCommunity;
import edu.fit.brees.ego.jung.JungNetwork;
import edu.fit.brees.ego.jung.JungVertex;
import edu.fit.brees.ego.util.SetUtil;
import edu.fit.brees.ego.util.ExtractEgoCommunities;


/**
 * An EgoVertex is the central part of this algorithm and represents a vertex in the network.
 * Each vertex is independent, containing all the information it needs to operate.
 * There is one main method, execute, that is called multiple time until the vertex returns a done message
 * 
 * 
 * @author bradrees
 *
 */
@SuppressWarnings("unused")
public class EgoVertex implements Callable<Integer>
{

	// Name
	private String	name		= "unset";

	// base ID
	private int		myId			= -99;		// a negative indicated unset
	
	// Ego-Communities
	private ArrayList<EgoCommunity> egoCommunities		= null;
	
	// Non-propagating Vertices
	// called special since those vertices are treated differently
	private Map<Integer, EgoCommunity> special		= null;

	// neighbors - associates ID with EgoVertex
	private Map<Integer, EgoVertex> neighbors		= null;
	
	private ProcessingStatus	processingStatus			= null;
	
	private float	threshold		= 0.75f;
	
	private Hashtable<Float, HashSet<Integer>> communities;
	private Hashtable<Float, HashSet<Integer>> tmpCommunities;
	
	
	// JUNG based egonet Graph
	JungNetwork egoNet	=	null;
	JungVertex  egoNode	= 	null;		// this vertex in the graph 
	
	
	//--- status flags ----
	protected boolean	idChanged						= false;			// a ego-community ID has changed
	protected boolean	egoCommunitiesFoundCompleted	= false;			// have the ego-communities been found
	protected boolean	findSpecialCompleted			= false;			// have special vertices been found

	// Possible processing operations
	public static final int NOOP	= 0;
	public static final int WAIT	= 1;
	public static final int RAN		= 2;
	
	
	
	
	/**
	 * 
	 * @param agentName
	 * @param agentID
	 * @param clusterType
	 * @param swarmStatus
	 */
	public EgoVertex(String vertexName, int vertexID, float threshold, ProcessingStatus status)
	{
		this.name 				= vertexName;
		this.myId 				= vertexID;
		this.threshold			= threshold;
		this.processingStatus	= status;
		
		special				= new Hashtable<Integer, EgoCommunity> ();
		neighbors 			= new Hashtable<Integer, EgoVertex>();
		egoCommunities		= new ArrayList<EgoCommunity>();
		
		// since this is all new, there is no change
		idChanged = false;
	}
	
	
	/**
	 * 
	 * This is the main processing method.  It steps through each phase based on the state of the 
	 * EgoVertex and independent of the other EgoVertex(es).  This method is repeatedly called until 
	 * there is no processing left to be done
	 * 
	 * @return - status (NOOP | RAN | WAIT)
	 */
	public int execute()
	{
		//------- Phase 1 -----------
		// The first thing that needs to be done is to find all the ego-communities
		// see if the ego-communities need to be found
		//
		if (egoCommunitiesFoundCompleted == false)
		{
			extractEgoCommunities();
			egoCommunitiesFoundCompleted = true;
			idChanged = true;
		
			return EgoVertex.RAN;
		}
		
		
		//------- Phase 2 -----------
		// find the vertices that are different (do not propagate - called special)
		if ( findSpecialCompleted == false)
		{
			// make sure that all the neighbors are ready to be evaluated
			if ( checkNeighborStatus() == false ) {
				return EgoVertex.WAIT;
			}
			
			determineSpecialNodes();
			findSpecialCompleted = true;
			
			return EgoVertex.RAN;
		}
	
		
		//------- Phase 3 -----------
		// Propagate the information
		// This section is run multiple times
		
		// Has an ID within this EgoVertex changed?
		if ( idChanged )
		{
			pushIdChange();
			return EgoVertex.RAN;
		}
		
		// nothing needed to be done.  
		processingStatus.incDoneCount();
		
		return EgoVertex.NOOP;		// starting state;
	}
	
	
	

	/**
	 * This process is done using JUNG (should be updated)
	 * 
	 * Note: Hopefully neighbors have been added before this call
	 */
	private void extractEgoCommunities()
	{
		try
		{
			// get the ego-communities
			//ExtractEgoCommunities extractor = new ExtractEgoCommunities();
			egoCommunities = ExtractEgoCommunities.extract(this);
		} 
		catch (Exception e)
		{
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	/**
	 * Step through all the identified EgoCommunities (EC) and ask each node for their 
	 * Corresponding EC.  Compare the two set to determine if they represent the same group.  
	 * If not, mark that node as "special" (that means that the ID changes are not propagated out)
	 * 
	 * Since a node can only appear in one FG at this time, there is no need to 
	 * map nodes back to ECs
	 * 
	 */
	private void determineSpecialNodes()
	{
		
		//System.out.println("Determine Special for " + this.myId);
		
		// look at each EgoCommunity 
		for ( EgoCommunity ecSet : egoCommunities)
		{
			HashSet<Integer> myView = ecSet.getMembers();		// all members of the ego-community
			
			for ( Integer nodeId : myView)						// steps through each member
			{
				if ( nodeId != this.myId)	// don't compare against myself
				{
					// convert from an ID to the EgoVertex
					EgoVertex agent = neighbors.get(nodeId.intValue());

					// get their view of the EgoCommunity
					HashSet<Integer> theirViewOfEC = agent.getEgoCommunity(this.myId);

					// are the views similar?
					boolean similar = SetUtil.egoSimalarity(myView, theirViewOfEC, threshold);

					if ( similar == false)
					{
						// this is a special node
						EgoCommunity spec = new EgoCommunity();
						spec.addToList(nodeId);
						spec.setId(ecSet.getId());

						special.put(nodeId.intValue(), spec);

						//System.out.println("\t----- Added " + nodeId + " as special  -- value was " + dif);
						//System.out.println("Set A: " + nodeSet + "  and Set B: " + theirViewOfFG);
					}
				}
			}
		}
	}
	
	
	/**
	 * make sure that all neighbors are ready
	 * 
	 * @return
	 */
	private boolean checkNeighborStatus()
	{
		// get the list of neighbors
		Collection<EgoVertex> agents = neighbors.values();
		
		for ( EgoVertex agent : agents )
		{
			if (agent.isBuildEcCompleted() == false)
				return false;
		}
		
		return true;
	}

	
	
	/**
	 * Push out the ID of any EgopCommunity that has changed.
	 * 
	 * 
	 */
	public void pushIdChange()
	{
		synchronized(this) {
			this.idChanged = false;
		}
		
		// step through all the friendship-groups
		for ( EgoCommunity ec : egoCommunities)
		{
			// only care if the ID has changed
			if ( ec.isChanged() )
			{
				// get the new EgoCiommunity ID
				float index = ec.getId();								 
				
				// Get a list of all the member of this EgoCommunity
				HashSet<Integer> members = ec.getMembers();
					
				// step through all members
				for ( int idx : members )
				{
					if ( idx != myId )	// don't tell ourselves
					{
						// find the agent
						EgoVertex agent  = this.neighbors.get( idx );

						if ( agent != null)
						{
							agent.notifyGroupIdChange( this.myId, index);
						}
						else
						{
							System.out.println("\tERROR: On Agent (" + this.myId + ")   the agent id " + idx + " is NULL");
						}
					}
				}
			
				// mark this EgoCommunity has unchanged 
				ec.setChanged(false);
			}
		}
	}
	
	
	/**
	 * This method is called when a "neighbor" wants to pass on their "proposed" 
	 * group ID.  
	 * 
	 * @param callersID
	 * @param newGroupId
	 * @return
	 */
	public void notifyGroupIdChange(int callersID, float newGroupId)
	{
		//System.out.println("\tIn " + this.myId + " looking for " + callersID);
				
		// If the caller is in the "special" list, then record the new ID in the
		// special list only and do NOT mark it as a change
		
		EgoCommunity specialSet = special.get(callersID);
		
		if ( specialSet != null )
		{			
			specialSet.setId(newGroupId);
			specialSet.setChanged(false);
			//System.out.println("\t\tNode " + callersID + " is special to " + this.myId);
		}
		else
		{
			// Find the group containing the caller
			EgoCommunity ec = findEgo(callersID);
			
			if ( ec != null ) {
				// We only care if the new ID is smaller than the current
				if ( newGroupId < ec.getId() )
				{
					ec.setId(newGroupId);
					ec.setChanged(true);
					this.idChanged = true;
					processingStatus.resetDonCount();
				}	
			}
			
			
			/*
			for ( EgoCommunity agentSet : egoCommunities)
			{
				if ( agentSet.conatins(callersID) )
				{
					//System.out.println("\tFound in FG " + agentSet.getFg());

					// We only care if the new ID is smaller than the current
					if ( newGroupId < agentSet.getId() )
					{
						agentSet.setId(newGroupId);
						agentSet.setChanged(true);
						this.idChanged = true;
						processingStatus.resetDonCount();
					}
				}
			}
			*/
		}
	}
	
	
	
	
	/**
	 * has an EgoCommunity ID changed?
	 * 
	 * @return
	 */
	public boolean hasAnyEgoChanged()
	{
		return idChanged;
	}



	
	/**
	 * addNeighbor - add the passed SwarmAgent as a neighbor of this one
	 * 
	 * @param n
	 * @return
	 */
	public void addNeighbor(EgoVertex n)
	{
		Integer idx = new Integer(n.getId());		// get the id of the agent
					
		EgoVertex old = neighbors.put(idx, n);		// add the agent to the hash
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getNeighborCount()
	{
		return neighbors.size();
	}
	
	
	public Collection<EgoVertex> getNeighbors()
	{
		return this.neighbors.values();
	}
	
	
	/**
	 * return the Friendship-Group Set containing the swarmId
	 * 
	 * @param swarmId
	 * @return - the FG array or NULL is not found
	 * 
	 */
	public HashSet<Integer> getEgoCommunity(Integer id)
	{
		for (EgoCommunity ec : egoCommunities)
		{
			if ( ec.conatins(id) )
				return ec.getMembers();
		}
		
		return null;
	}
	
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public EgoCommunity findEgo(Integer id)
	{
		for (EgoCommunity ec : egoCommunities)
		{
			if ( ec.conatins(id) )
				return ec;
		}
		
		return null;
	}
		
	
	
	
	public Collection<EgoCommunity> getAllEgoCommunity()
	{
		return egoCommunities;
	}

	public Collection<EgoCommunity> getSpecialGroups()
	{
		return special.values();
	}	

	public String getName()
	{
		return name;
	}


	public int getId()
	{
		return myId;
	}
	
	
	/**
	 * this is simply a debug method.
	 * 
	 * Dump the list of friendship-groups and the assigned ID
	 */
	public void dumpFGs()
	{	
		System.out.println("FG of Swarm Agent: " + this.myId);
		
		for ( EgoCommunity ec : egoCommunities)
		{			
			System.out.println("\t" + ec.getId() + " - " + ec.getMembers() );
		}
		
		System.out.println(" ");
	}
	

	
	/**
	 * 
	 */
	public void dumpFgCommunities()
	{	
		System.out.println("FG of Swarm Agent: " + this.myId);
		
		for ( EgoCommunity ec : egoCommunities)
		{	
			if ( ec.size() > 2)
				System.out.println("\t" + ec.getId() + " - " + ec.getMembers() );
		}
		
		Collection<EgoCommunity> specials = special.values();
		
		for ( EgoCommunity ec :  specials)
		{	
				System.out.println("\t" + ec.getId() + " - " + ec.getMembers() );
		}		
		
		System.out.println("");
	}


	/**
	 * 
	 * @return
	 */
	public boolean isBuildEcCompleted()
	{
		return this.egoCommunitiesFoundCompleted;
	}
	


	/**
	 * Runnable entry point
	 */
	//public void run()
	//{
	//	execute();
	//}


	/**
	 * Callable entry point
	 * 
	 */
	public Integer call() throws Exception
	{
		return execute();
	}


	public void setIdChanged(boolean idChanged) {
		this.idChanged = idChanged;
		
		for ( EgoCommunity ec :  egoCommunities	)
			ec.setChanged(idChanged);
		
	}


	public void setCommunities(Hashtable<Float, HashSet<Integer>> communities) {
		this.communities = communities;
	}


	public void setTmpCommunities(Hashtable<Float, HashSet<Integer>> tmpCommunities) {
		this.tmpCommunities = tmpCommunities;
	}
	
	
}
