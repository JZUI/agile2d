/************************************************************************************
 * Copyright (C) 2012, Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *        
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * ---------------------------------------------------------------------------------*
 * This software is published under the terms of the BSD Software License    	    *
 ************************************************************************************/
package agile2d.benchmark;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;

public class AnimeBenchmark{
	public final static int WIN_W = 960;
	public final static int WIN_H = 900;
	public final static int NB_FONTS=6;
	public final static int NB_REPETITIONS=2;
	public final static int MAX_NB_FONTS=12;
	public final static int MAX_NB_REPETITIONS=15;
	public final static int MAX_NB_SHAPES=3000;
	public final static int MAX_NB_IMAGES=15;
	public final static int NB_SHAPE_TYPES = 3;
	public final static float INIT_FONT_SIZE = 6.0f;
	public final static float MAX_SCALE = 9.0f;
	public final static String PATH_TO_IMAGES = "/";//src/main/resources/";

	public static int tick_interval;

	private final static long DURATION_FPS = 10000;
	private static int nb_fonts=0;
	private static int nb_repetitions=0;
	private static int nb_emptyOvals=0;
	private static int nb_fullOvals=0;
	private static int nbRects=0;
	private static int nbImages=0;
	private static int[][] shapeCoord = new int[MAX_NB_SHAPES][4];
	private static double shapeRotation[] = new double[MAX_NB_SHAPES];
	private static int[][] imgCoord = new int[MAX_NB_IMAGES][4];
	private static double imgRotation[] = new double[MAX_NB_IMAGES];
	private static double zFactor = 1.00;
	private static Color[] shapeColor = new Color[MAX_NB_SHAPES];
	private static Font[] allFonts;
	private static Font[] someFonts = new Font[MAX_NB_FONTS];
	private static BufferedImage[] bufferedImages = new BufferedImage[MAX_NB_IMAGES];
	private static String[] availableNames = {"imgBench1.jpg", "imgBench2.jpg", "imgBench3.jpg", "imgBench4.jpg", "imgBench5.jpg", "imgBench6.jpg", "imgBench7.jpg", "imgBench8.jpg", "imgBench9.jpg", "imgBench10.jpg", "imgBench11.jpg", "imgBench12.jpg", "imgBench13.jpg", "imgBench14.jpg", "imgBench15.jpg"};
	private static String[] imageNames = new String[MAX_NB_IMAGES];
	private final static AffineTransform idTransform = new AffineTransform();

	private int frame_counter;
	private long lastFPS;
	private double incrementor = 1.0;
	private Chrono chrono;

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
			shapeCoord[i][0] = shapeRand.nextInt(BenchmarkGUI.CANVAS_W)-50;
			shapeCoord[i][1] = shapeRand.nextInt(BenchmarkGUI.CANVAS_H)-50;
			shapeCoord[i][2] = shapeRand.nextInt(120)+20;
			shapeCoord[i][3] = shapeRand.nextInt(120)+20;
			shapeColor[i] = new Color(colorRand.nextInt(255), colorRand.nextInt(255), colorRand.nextInt(255));
			shapeRotation[i] = rotateRand.nextDouble()*(2*Math.PI);
		}

		Random imgRand = new Random();
		int init_img_xy = 5;
		int img_xy_step = 10;
		for(int i=0; i<MAX_NB_IMAGES; i++){
			imageNames[i]= availableNames[i];
			imgCoord[i][0] = init_img_xy+(img_xy_step*i);
			imgCoord[i][1] = init_img_xy+(img_xy_step*i);
			imgCoord[i][2] = 0;//width. the actual width&height of the image will be attributed as soon an image is loaded
			imgCoord[i][3] = 0;//height
			imgRotation[i] = imgRand.nextDouble()*(Math.PI/4);
			bufferedImages[i]=null;
		}
	}

	public AnimeBenchmark(Chrono ch){
		chrono = ch;
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

	public int getLastFPS(){
		return (int)lastFPS;
	}

	public void step() {
		//Incrementor ]0, 2*PI[
		incrementor += 0.025;
		incrementor %= (2*Math.PI);
		//zFactor ]1.0, MAX_SCALE[
		zFactor = MAX_SCALE*(Math.sin(incrementor)+1.1);
		//Gets the fps every DURATION_FPS (i.e., 10 secs)
		long temp_duration= chrono.getTempDuration();
		if(temp_duration>DURATION_FPS){
			computeFPS();
		}
	}

	public void resetCounter(){
		this.frame_counter = 0;
	}

	public void increment(){
		this.frame_counter++;
	}

	public Font getFont(int i){
		return someFonts[i%NB_FONTS];
	}

	// Sample display to test text rendering performance during zooming
	public static void drawBigText(int x, int y, Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.scale(zFactor, zFactor);
		for(int i=0; i<(nb_repetitions*nb_fonts); i++){
			g2.setFont(someFonts[i%nb_fonts]);
			g2.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 2, ((i+1)*INIT_FONT_SIZE));
		}
		g2.setTransform(idTransform);
	}


	public static void drawRects(Graphics2D g2){
		for(int i=0; i<nbRects; i+=2){
			g2.setColor(shapeColor[i]);
			//g2.rotate(shapeRotation[i]);
			g2.scale(zFactor, zFactor);
			g2.drawRect(shapeCoord[i][0], shapeCoord[i][1], shapeCoord[i][2], shapeCoord[i][3]);
			g2.setTransform(idTransform);
			g2.setColor(shapeColor[i+1]);
			//g2.rotate(shapeRotation[i+1]);
			g2.scale(zFactor, zFactor);
			g2.fillRect(shapeCoord[i+1][0], shapeCoord[i+1][1], shapeCoord[i+1][2], shapeCoord[i+1][3]);
			g2.setTransform(idTransform);
		}
	}

	public static void drawImages(Graphics2D g2){
		for(int i=0; i<nbImages; i++){
			g2.scale(zFactor, zFactor);
			g2.drawImage((Image)bufferedImages[i], imgCoord[i][0], imgCoord[i][1], imgCoord[i][2], imgCoord[i][3], null);
			g2.setTransform(idTransform);
		}
	}

	public static void drawEmptyOvals(Graphics2D g2){
		int first_emptyOval = (MAX_NB_SHAPES/3);
		for(int i=first_emptyOval; i<(first_emptyOval+nb_emptyOvals); i++){
			g2.setColor(shapeColor[i]);
			g2.scale(zFactor, zFactor);
			g2.drawOval(shapeCoord[i][0], shapeCoord[i][1], shapeCoord[i][2], shapeCoord[i][3]);
			g2.setTransform(idTransform);
		}
	}

	public static void drawFullOvals(Graphics2D g2){
		int first_fillOval = 2*(MAX_NB_SHAPES/3);
		for(int i=first_fillOval; i<(first_fillOval+nb_fullOvals); i++){
			g2.setColor(shapeColor[i]);
			g2.scale(zFactor, zFactor);
			g2.fillOval(shapeCoord[i][0], shapeCoord[i][1], shapeCoord[i][2], shapeCoord[i][3]);
			g2.setTransform(idTransform);
		}
	}

	public static void setNbFonts(int n){
		nb_fonts = n;
	}

	public static void setNbRepetitions(int n){
		nb_repetitions = n;
	}

	public static void setNbRects(int n){
		nbRects = n;
	}

	public static void setNbFullOvals(int n){
		nb_fullOvals = n;
	}

	public static void setNbEmptyOvals(int n){
		nb_emptyOvals = n;
	}

	public static void setNbImages(int n){
		//Check if nothing has changed
		if(n == nbImages)
			return;
		//Check either if images have to be unloaded and un-set width/height vars
		else if(n<nbImages){
			for(int i=n; i<nbImages; i++){
				bufferedImages[i] = null;
				imgCoord[i][2] = 0;
				imgCoord[i][3] = 0;
			}
		}
		//or if images have to be loaded and set width/height vars
		else if(n>nbImages){
			for(int i=nbImages; i<n; i++) {
			    URL url = AnimeBenchmark.class.getResource(PATH_TO_IMAGES+imageNames[i]);
			    if (url != null)
				try {
					bufferedImages[i] = ImageIO.read(url);
					imgCoord[i][2] = bufferedImages[i].getWidth();
					imgCoord[i][3] = bufferedImages[i].getHeight();
				} catch (IOException e) {
				    System.err.println("Problem while loading image file: "+imageNames[i]);

				}
			    else {
			        System.err.println("Image file does not exist: "+imageNames[i]);
                    bufferedImages[i] = null;
                    imgCoord[i][2] = 0;
                    imgCoord[i][3] = 0;
			    }
			}
		}
		nbImages=n;
	}
}