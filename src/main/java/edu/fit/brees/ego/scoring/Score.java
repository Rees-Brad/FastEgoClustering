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
package edu.fit.brees.ego.scoring;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Score 
{

	private String tmpDir	= "/tmp";
	private String exec		= "/Users/bradrees/dev/mutual3/mutual";
	
	
	public Score() {;}
	
	
	/**
	 * 
	 * @param found
	 * @param real
	 * @return
	 * @throws Exception
	 */
	public double computeMutalInformationScore(
			Hashtable<Float, HashSet<Integer>> found, 
			Hashtable<Integer, HashSet<Integer>> real) throws Exception
	{
		double answer = 0;
		
		String realFile 	= tmpDir + "/RealClustrer.dat";
		String foundFile	= tmpDir + "/FoundCluster.dat";
		String cmd 			= exec + " " + realFile + " " + foundFile;
		
		writeData(real.values(), realFile);
		writeData(found.values(), foundFile);
		

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(cmd);
		p.waitFor();
		
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";

		while ((line = b.readLine()) != null) {
		  System.out.println(line);
		}

		b.close();
		
		
		// remove the files
		deleteFile(realFile);
		deleteFile(foundFile);
		
		
		return answer;
	}


	/**
	 * 
	 * @param dataSet
	 * @param w
	 */
	private void writeData(Collection<HashSet<Integer>> dataSet, String fileName) throws Exception
	{
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));

		for ( Set<Integer> data : dataSet)
		{
			for ( Integer x : data)	
				w.write(x + " ");
			
			w.write("\n");
		}
		
		w.flush();
		w.close();
	}
	
	
	private void deleteFile(String fileName)
	{
		File file = new File(fileName);
		file.delete();
	}
	
	
	
	
	public String getTmpDir() {
		return tmpDir;
	}


	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}
	

}
