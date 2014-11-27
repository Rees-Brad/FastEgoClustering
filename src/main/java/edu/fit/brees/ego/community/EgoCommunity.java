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
package edu.fit.brees.ego.community;

import java.io.PrintStream;
import java.util.HashSet;


/**
 * This class capture the list of BVertices that comprises the ego-community
 * In this case, just the index of the vertices is being saved
 * 
 * 
 * @author Brad Rees
 *
 */
public class EgoCommunity
{
	
	/* use a HashSet to prevent duplicates from being added */
	protected HashSet<Integer> members		=	null;
	
	float	id		= (float) -99.9;

	boolean changed		= false;
	
	public EgoCommunity()
	{
		members = new HashSet<>();
	}
	
	public HashSet<Integer> getMembers()
	{
		return members;
	}

	public float getId()
	{
		return id;
	}


	/**
	 * Set the ID of this EgoCommunity.  
	 * The act of setting the id also causes the changed flag
	 * to be set to true.
	 * 
	 * @param id
	 */
	public void setId(float id)
	{
		this.id = id;
		this.changed = true;
	}


	public boolean isChanged()
	{
		return changed;
	}


	public void setChanged(boolean changed)
	{
		this.changed = changed;
	}


	public void addToList(int agentId)
	{
		this.members.add(agentId);
	}
	

	/**
	 * Is this index in the set?
	 * 
	 * @param idx
	 * @return
	 */
	public boolean conatins(int idx)
	{
		return members.contains(idx);		
	}
	
	public int size()
	{
		return members.size();
	}
	
	public void dump(PrintStream out)
	{
		out.print("ID: " + id + " [");
		
		for (int x : members)
		{
			out.print(x + ", ");
		}
		
		out.println("]");
	}

}
