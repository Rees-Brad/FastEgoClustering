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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.fit.brees.ego.jung.JungEdge;
import edu.fit.brees.ego.jung.JungNetwork;
import edu.fit.brees.ego.jung.JungVertex;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;



public class Visualize 
{
	public static final int CIRCLE		= 0;
	public static final int SPRING		= 1;
	public static final int FR			= 2;
	public static final int ISOM		= 3;
	public static final int DAG			= 4;
	public static final int SPRING2		= 5;
	
	public static final int[] colors 	= {
		0xffffff,									// white	
		0xff0000,									// red
		0x00ff00,									// lime
		0x0000ff,									// blue
		0xffff00,									// yellow
		0x00FFFF,									// cyan
		0xFF00FF,									// megenta 
		0xC0C0C0,									// silver
		0x808080,									// gray
		0x800000,									// maroon
		0xFF7F50,									// coral
		0x808000,									// olive
		0x008000,									// green
		0xFFA500,									// orange
		0x800080,									// purple
		0x008080,									// teal
		0x98FB98,									// pale green
		0x000080,									// navy
		0x87CEFA									// light sky blue
	};
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void visualize(JungNetwork graph, String title, int type)
	{
		// The Layout<V, E> is parameterized by the vertex and edge types 
		Layout<JungVertex, JungEdge> layout	= null;
		
		switch (type)
		{
			case CIRCLE:
				layout = new CircleLayout<JungVertex, JungEdge>(graph); 
				break;
				
			case SPRING:
				layout = new SpringLayout<JungVertex, JungEdge>(graph); 
				break;
				
			case FR:
				layout = new FRLayout<JungVertex, JungEdge>(graph); 
				break;
				
			case ISOM:
				layout = new ISOMLayout<JungVertex, JungEdge>(graph); 
				break;
				
			case DAG:
				layout = new DAGLayout<JungVertex, JungEdge>(graph);
				break;

			case SPRING2:
				layout = new SpringLayout2<JungVertex, JungEdge>(graph);
				break;
				
			default:
				layout = new KKLayout<JungVertex, JungEdge>(graph); 
		}
		
		layout.setSize(new Dimension(800,800)); // sets the initial size of the space 
		
		// The BasicVisualizationServer<V,E> is parameterized by the edge types 
		BasicVisualizationServer<JungVertex,JungEdge> vv = 
			new BasicVisualizationServer<JungVertex,JungEdge>(layout);
		
		vv.setPreferredSize(new Dimension(900,900)); //Sets the viewing area size  
		vv.scaleToLayout(new LayoutScalingControl() );
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller()); 
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());        
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR); 
        
		JFrame frame = new JFrame(title); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.getContentPane().add(vv); 
		frame.pack(); 
		frame.setVisible(true); 
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void visualizeColor(JungNetwork graph, String title, int type)
	{
		
		// Painter
        Transformer<JungVertex,Paint> vertexPaint = new Transformer<JungVertex,Paint>() 
        { 
            public Paint transform(JungVertex i) 
            {         	
        		int size = colors.length;
        		
        		int colorIdx = i.getTag() % size;
        		
        		//System.out.println("Tag " + i.getTag() + " is color " + color);
        		
        		Color color = new Color(colors[colorIdx]);
        		
        		int red = color.getRed();
        		int green = color.getGreen();
        		int blue = color.getBlue();
        		
        		red = green = blue = (int) (red *0.299 + green * 0.587 + blue * 0.114);
        		
        		return color;
        		//return new Color(red, green, blue);
            } 
        }; 
        
        
        Transformer <JungVertex, String> vertexLable = new Transformer<JungVertex, String>()
        {
        	public String transform(JungVertex v) {
                return (new String( " " +v.getId()) );
            }
        };
		
		
		
		
		
		// The Layout<V, E> is parameterized by the vertex and edge types 
		Layout<JungVertex, JungEdge> layout	= null;
		
		switch (type)
		{
			case CIRCLE:
				layout = new CircleLayout<>(graph); 
				break;
				
			case SPRING:
				layout = new SpringLayout<>(graph); 
				break;
				
			case FR:
				layout = new FRLayout<>(graph); 
				break;
				
			case ISOM:
				layout = new ISOMLayout<>(graph); 
				break;
				
			case DAG:
				layout = new DAGLayout<>(graph);
				break;

			case SPRING2:
				layout = new SpringLayout2<>(graph);
				break;
				
			default:
				layout = new KKLayout<>(graph); 
		}
		
		layout.setSize(new Dimension(1200,1200)); // sets the initial size of the space 
		
		// The BasicVisualizationServer<V,E> is parameterized by the edge types 
		BasicVisualizationServer<JungVertex, JungEdge> vv = new BasicVisualizationServer<>(layout);
		
		vv.setPreferredSize(new Dimension(1000,1000)); //Sets the viewing area size  
		vv.scaleToLayout(new LayoutScalingControl() );
        vv.getRenderContext().setVertexLabelTransformer(vertexLable); 
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR); 
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());        

        
		JFrame frame = new JFrame(title); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.getContentPane().add(vv); 
		frame.pack(); 
		frame.setVisible(true); 
	}
	
	
	
	
	
	
}
