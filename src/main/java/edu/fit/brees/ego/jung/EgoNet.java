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

import java.util.Collection;


/**
 * This is a version of a JungNetwork that contains some extra fields
 * 
 * @author bradrees
 *
 */
public class EgoNet extends JungNetwork
{
	private static final long	serialVersionUID	= 1L;
	
	private	double 	density		=	0.0;
	private int		id			= 	0;
	private boolean	inCommunity	= 	false;
	private JungVertex	egoNode		= null;


	public EgoNet(JungVertex ego)
	{
		super();
		
		egoNode = ego;
		
		this.addVertex(egoNode);
	}
	
	protected EgoNet()
	{
		super();
	}
	
	public double getDensity()
	{
		return density;
	}
	
	public void setDensity(double density)
	{
		this.density = density;
	}
	
	
	public int getId()
	{
		return id;
	}
	
	
	public void setId(int id)
	{
		this.id = id;
	}
	


	
	public boolean isInCommunity()
	{
		return inCommunity;
	}

	public boolean isInCommunity(int communityId)
	{
		Collection<JungVertex> nodes = this.getVertices();
		
		for ( JungVertex n : nodes)
		{
			if ( n != egoNode)
				//if (n.isPartOfCluster(communityId))
					return true;
		}
		
		return false;
	}
	
	
	public void setInCommunity(boolean inCommunity)
	{
		this.inCommunity = inCommunity;
	}
	
	
	public JungVertex getEgoNode()
	{
		return egoNode;
	}
	
	
	
	
}
