/*****************************************************************************
 * Copyright (C) 2011 INRIA Aviz Project and In-Situ Project 		     *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d.benchmark;

import agile2d.benchmark.Chrono;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;

public class AnimeBenchmark{
	public final static int WIN_W = 150;
	public final static int WIN_H = 150;		
	private final static int NB_FONTS=18;
	private final static int NB_REPETITIONS=2;
	private final static float INIT_FONT_SIZE = 6.0f;
	private final static float MAX_SCALE = 9.0f;

	private double incrementor = 1.0;
	private double zFactor = 1.00;	
	//private Thread thread;
	private static Chrono chrono;
	private int frame_counter;
	private static Font[] allFonts;
	private static Font[] someFonts = new Font[NB_FONTS];

	static{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		allFonts = ge.getAllFonts();
		for(int i=0; i<NB_FONTS; i++)
			someFonts[i] = allFonts[i].deriveFont(INIT_FONT_SIZE);
	}

	public AnimeBenchmark(Chrono ch_){
		chrono = ch_;
		chrono.start();
	}	
   
    public long getFPS(){   	
    	chrono.stop();
    	long duration_sec = chrono.getDuration()/1000; 
    	chrono.start();
    	return (this.frame_counter/duration_sec);
    }
    
    public void step() {
    		//Incrementor ]0, 2*PI[
    		incrementor += 0.025;
    		incrementor %= (2*Math.PI);
    		//zFactor ]1.0, MAX_SCALE[ 
    		zFactor = MAX_SCALE*(Math.sin(incrementor)+1.1);
    		//Gets the fps once per cycle (when the angle approaches "0")
    		if(incrementor<0.025){
    			System.out.println("FPS: "+this.getFPS());
    			resetCounter();
    			System.out.println("zFactor: "+zFactor);
    		}   		
    }
    
    public void resetCounter(){
    	this.frame_counter = 0;    	
    }

    public void increment(){
    	this.frame_counter++;    	
    }    
    
	// Sample display to test text rendering performance during zooming
	public void drawBigText(int x, int y, Graphics2D g2_) {
		g2_.scale(this.zFactor, this.zFactor);
		for(int i=0; i<(NB_REPETITIONS*NB_FONTS); i++){
			g2_.setFont(someFonts[i%NB_FONTS]);
			g2_.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 2, ((i+1)*INIT_FONT_SIZE));
		}
	}
}