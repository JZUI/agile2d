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
	public final static int WIN_W = 800;
	public final static int WIN_H = 600;		
	public final static int NB_FONTS=6;
	public final static int NB_REPETITIONS=2;
	public final static int MAX_NB_FONTS=12;
	public final static int MAX_NB_REPETITIONS=15;
	public final static float INIT_FONT_SIZE = 6.0f;
	public final static float MAX_SCALE = 9.0f;
	
	private static Font[] allFonts;
	private static Font[] someFonts = new Font[MAX_NB_FONTS];

	private Chrono chrono;
	private double incrementor = 1.0;
	private double zFactor = 1.00;	
	//private Thread thread;
	private int frame_counter;
	public static int nb_fonts, nb_repetitions, nb_shapes;
	
	static{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		allFonts = ge.getAllFonts();
		for(int i=0; i<MAX_NB_FONTS; i++)
			someFonts[i] = allFonts[i].deriveFont(INIT_FONT_SIZE);
		nb_fonts = 1;
		nb_repetitions = 1;
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
            //System.out.println("In step");
    		//Incrementor ]0, 2*PI[
    		incrementor += 0.025;
    		incrementor %= (2*Math.PI);
    		//zFactor ]1.0, MAX_SCALE[ 
    		zFactor = MAX_SCALE*(Math.sin(incrementor)+1.1);
    		//Gets the fps once per cycle (when the angle approaches "0")
    		if(incrementor<0.025){
    			System.out.println("FPS: "+this.getFPS());
    			resetCounter();    			
    		}   		
    }
    
    public void resetCounter(){
    	this.frame_counter = 0;    	
    }

    public void increment(){
    	this.frame_counter++;    	
    }    
    
    public double getZ(){
    	return this.zFactor;    	
    }

    public Font getFont(int i_){
    	return someFonts[i_%NB_FONTS];  	
    }
    
	// Sample display to test text rendering performance during zooming
	public void drawBigText(int x, int y, Graphics2D g2_) {
		
		g2_.scale(this.zFactor, this.zFactor);
		System.out.println("Printing "+nb_repetitions*nb_fonts+" lines");
		
		//for(int i=0; i<(NB_REPETITIONS*NB_FONTS); i++){
		for(int i=0; i<(nb_repetitions*nb_fonts); i++){
    		//System.out.println("Inside for Loop");
		    
			//g2_.setFont(someFonts[i%NB_FONTS]);
			g2_.setFont(someFonts[i%nb_fonts]);
			g2_.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 2, ((i+1)*INIT_FONT_SIZE));
		}
	}
	
	public static void setNbFonts(int n){
		nb_fonts = n;		
	}

	public static void setNbRepetitions(int n){
		nb_repetitions = n;		
	}

	
}