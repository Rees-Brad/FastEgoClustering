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


/**
 * Define a base Vertex class for JUNG
 * 
 * 
 * @author Brad Rees
 *
 */
public class JungVertex 
{
	private int		id		= -1;
	private String	name	= null;
	private int		tag		= 0;

	/**
	 * create a base node with a name and an ID
	 * 
	 * @param id
	 * @param name
	 */
	public JungVertex (int id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/** 
	 * get the ID
	 * 
	 * @return
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * get the name
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	public String toString()
	{
		return name;
	}

	public int getTag()
	{
		return tag;
	}

	public void setTag(int tag)
	{
		this.tag = tag;
	}
	
	
}
