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



import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Contains some public static methods that act on sets 
 * 
 * @author bradrees
 *
 */
public class SetUtil
{

	
	
	/**
	 * Determine if the sets are similar within a threshold
	 * 
	 * @param a
	 * @param b
	 * @param threshold
	 * @return T/F
	 */
	static public boolean egoSimalarity(Set<Integer> a, Set<Integer> b, float threshold)
	{
		int minSize = 999;
		
		int intersectSize = SetUtil.intersection(a, b).size();

		if ( a.size() < b.size())
			minSize = (int)( (float)a.size() * threshold);
		else
			minSize = (int)( (float)b.size() * threshold);
		
		if (intersectSize >= minSize)
			return true;
		else
			return false;
	}
	
	
	/**
	 * Return the Intersection of two sets
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	static public Set<Integer> intersection(Set<Integer> a, Set<Integer> b)
	{
		Set<Integer> s = new HashSet<Integer>(a);
		s.retainAll(b);		
		return s;
	}
	
	
	/**
	 * Return the size of the intersection
	 * 
	 * @param me
	 * @param them
	 * @return
	 */
	static public int intersectionSize(Set<Integer> me, Set<Integer> them)
	{
		return SetUtil.intersection(me, them).size();
	}
	
	
	/**
	 * Return the Union of two sets
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	static public Set<Integer> union(Set<Integer> a, Set<Integer> b)
	{
		Set<Integer> s = new HashSet<Integer>(a);
		s.addAll(b);
		return s;
	}
	
	
	/**
	 * Compute the Jacaard Index
	 * 
	 * @param me
	 * @param them
	 * @return
	 */
	static public float jacaardIndex(Set<Integer> me, Set<Integer> them)
	{
		float intersectSize = (float)SetUtil.intersection(me, them).size();
		float unionSize 	= (float)SetUtil.union(me, them).size();
		
		return (intersectSize / unionSize);
	}
	
	/**
	 * Compute the Overlap Coefficient 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	static public float overlapCoefficient(Set<Integer> a, Set<Integer> b)
	{
		float answer = 0;
		
		float intersectSize = (float)SetUtil.intersection(a, b).size();
		
		if ( a.size() < b.size())
			answer = intersectSize / a.size();
		else
			answer = intersectSize / b.size();
		
		return answer;
	}
	
	
	/**
	 * Remove any proper subset
	 * 
	 * @param communities
	 */
	static public void removeSubsets(Map<Float, HashSet<Integer>> communities)
	{
		Set<Float> toDelete = new HashSet<>();
		
		
		Set<Float> keys = communities.keySet();
		int size = keys.size();
		
		Float[] keyArray = keys.toArray( new Float[size]);
		
		HashSet<Integer> X	= null;
		HashSet<Integer> Y	= null;
		float id = 0;
		
		for ( int x = 0; x < size -1; x++)
		{
			id = keyArray[x];
			X = communities.get(id);
			
			for (int y = x+1; y < size; y++)
			{
				id = keyArray[y];
				Y = communities.get(id);
				
				// only compare if X is smaller than Y
				if ( X.size() < Y.size())
				{
					if (SetUtil.overlapCoefficient(X, Y) == 1)
					{
						toDelete.add(id);
						continue;
					}
				}
			}
		}
		
		for ( Float f : toDelete)
			communities.remove(f);
	}
	
	
	/**
	 * Return the size of the smaller set
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	static public int sizeOfSmaller(Set<Integer> a, Set<Integer> b)
	{
		if ( a.size() < b.size())
			return a.size(); 
		else
			return b.size();
	}
	
	
	/** 
	 * Is A a proper set of B
	 * 
	 * @param A
	 * @param B
	 * @return
	 */
	static public boolean properSubset(Set<Integer> A, Set<Integer> B)
	{
		return B.containsAll(A);
	}
	
}
