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


/**
 * This class is used to keep track of the status of all running threads.
 * 
 * (this should really be a singleton)
 * 
 * @author bradrees
 *
 */
public class ProcessingStatus 
{

	protected int	doneCount	= 0;

	public ProcessingStatus()
	{
		doneCount = 0;
	}
	
	public int getDoneCount()
	{
		return doneCount;
	}

	public synchronized void incDoneCount()
	{
		++doneCount;
	}
	
	public synchronized void decDoneCount()
	{
		--doneCount;
	}
	
	
	public synchronized void resetDonCount()
	{
		doneCount = 0;
	}
	
	
	
}
