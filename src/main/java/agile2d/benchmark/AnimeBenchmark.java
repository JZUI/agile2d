/*****************************************************************************
 * Copyright (C) 2011 INRIA Aviz Project and In-Situ Project 		     *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d.benchmark;

import agile2d.benchmark.Chrono;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.util.Random;

public class AnimeBenchmark{
	public final static int WIN_W = 960;
	public final static int WIN_H = 900;		
	public final static int NB_FONTS=6;
	public final static int NB_REPETITIONS=2;
	public final static int MAX_NB_FONTS=12;
	public final static int MAX_NB_REPETITIONS=15;
	public final static int MAX_NB_SHAPES=3000;
	public final static float INIT_FONT_SIZE = 6.0f;
	public final static float MAX_SCALE = 9.0f;
	private final static long DURATION_FPS= 10000;

	private static Font[] allFonts;
	private static Font[] someFonts = new Font[MAX_NB_FONTS];

	public final static int NB_SHAPE_TYPES = 3;
	private static int[][] shapeCoord = new int[MAX_NB_SHAPES][4];
	private static Color[] shapeColor = new Color[MAX_NB_SHAPES];
	private static double shapeRotation[] = new double[MAX_NB_SHAPES];

	private Chrono chrono;
	private double incrementor = 1.0;	
	private int frame_counter;
	private long lastFPS;
	
	private static double zFactor = 1.00;
	public static int nb_fonts, nb_repetitions;
	public static int nb_emptyOvals, nb_fullOvals, nb_rects;
	public static int tick_interval;
	public static int current_strat;

	static{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		allFonts = ge.getAllFonts();
		for(int i=0; i<MAX_NB_FONTS; i++)
			someFonts[i] = allFonts[i].deriveFont(INIT_FONT_SIZE);
		nb_fonts = 1;
		nb_repetitions = 1;
		tick_interval = (MAX_NB_SHAPES/NB_SHAPE_TYPES)/10;

		Random shapeRand = new Random();
		Random colorRand = new Random();
		Random rotateRand = new Random();
		for(int i=0; i<MAX_NB_SHAPES; i++){
			shapeCoord[i][0] = shapeRand.nextInt(BenchmarkGUI.CANVAS_W)-100;
			shapeCoord[i][1] = shapeRand.nextInt(BenchmarkGUI.CANVAS_H)-100;			
			shapeCoord[i][2] = shapeRand.nextInt(120)+20;
			shapeCoord[i][3] = shapeRand.nextInt(120)+20;
			shapeColor[i] = new Color(colorRand.nextInt(255), colorRand.nextInt(255), colorRand.nextInt(255));
			shapeRotation[i] = rotateRand.nextDouble()*(2*Math.PI);
		}
	}

	public AnimeBenchmark(Chrono ch_){
		chrono = ch_;
		chrono.start();
		lastFPS=0;
	}

	private void computeFPS(){
		chrono.stop();
		long duration_sec = chrono.getDuration()/1000; 
		chrono.start();
		if(duration_sec>0)
			lastFPS = (this.frame_counter/duration_sec);  
		else 
			lastFPS=0;
		resetCounter();
	}
	
	private long getFPS(){
		return lastFPS;
	}

	public int getLastFPS(){
		return (int)lastFPS;
	}

	public void step() {
		//Incrementor ]0, 2*PI[
		incrementor += 0.025;
		incrementor %= (2*Math.PI);
		//zFactor ]1.0, MAX_SCALE[ 
		zFactor = MAX_SCALE*(Math.sin(incrementor)+1.1);
		//Gets the fps once per cycle (when the angle approaches "0")
		long temp_duration= chrono.getTempDuration();
		//if(incrementor<0.001){
		if(temp_duration>DURATION_FPS){
			computeFPS();
			System.out.println("FPS: "+this.getFPS());
		}  
	}

	public void resetCounter(){
		this.frame_counter = 0;    	
	}

	public void increment(){
		this.frame_counter++;    	
	}    

	public Font getFont(int i_){
		return someFonts[i_%NB_FONTS];  	
	}

	// Sample display to test text rendering performance during zooming
	public static void drawBigText(int x, int y, Graphics2D g2_) {

		g2_.scale(zFactor, zFactor);

		//for(int i=0; i<(NB_REPETITIONS*NB_FONTS); i++){
		for(int i=0; i<(nb_repetitions*nb_fonts); i++){
			//System.out.println("Inside for Loop");

			//g2_.setFont(someFonts[i%NB_FONTS]);
			g2_.setFont(someFonts[i%nb_fonts]);
			g2_.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 2, ((i+1)*INIT_FONT_SIZE));
		}
	}


	public static void drawRects(Graphics2D g2_){
		for(int i=0; i<nb_rects; i+=2){
			g2_.setColor(shapeColor[i]);
			g2_.rotate(shapeRotation[i]);
			g2_.drawRect(shapeCoord[i][0], shapeCoord[i][1], shapeCoord[i][2], shapeCoord[i][3]);
			g2_.setColor(shapeColor[i+1]);
			g2_.rotate(shapeRotation[i+1]);
			g2_.fillRect(shapeCoord[i+1][0], shapeCoord[i+1][1], shapeCoord[i+1][2], shapeCoord[i+1][3]);
		}
	}

	public static void drawEmptyOvals(Graphics2D g2_){
		int first_emptyOval = (MAX_NB_SHAPES/3);
		for(int i=first_emptyOval; i<(first_emptyOval+nb_emptyOvals); i++){
			g2_.setColor(shapeColor[i]);
			g2_.rotate(shapeRotation[i]);
			g2_.drawOval(shapeCoord[i][0], shapeCoord[i][1], shapeCoord[i][2], shapeCoord[i][3]);
		}
	}

	public static void drawFullOvals(Graphics2D g2_){
		int first_fillOval = 2*(MAX_NB_SHAPES/3);
		for(int i=first_fillOval; i<(first_fillOval+nb_fullOvals); i++){
			g2_.setColor(shapeColor[i]);
			g2_.rotate(shapeRotation[i]);
			g2_.fillOval(shapeCoord[i][0], shapeCoord[i][1], shapeCoord[i][2], shapeCoord[i][3]);
		}
	}

	public static void setNbFonts(int n){
		nb_fonts = n;		
	}

	public static void setNbRepetitions(int n){
		nb_repetitions = n;		
	}

	public static void setNbRects(int n){
		nb_rects = n;		
	}

	public static void setNbFullOvals(int n){
		nb_fullOvals = n;		
	}	
	
	public static void setNbEmptyOvals(int n){
		nb_emptyOvals = n;		
	}
}