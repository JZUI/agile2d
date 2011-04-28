/*****************************************************************************
* Copyright (C) 2011, Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
* ------------------------------------------------------------------------- *
* This software is published under the terms of the BSD Software License    *
* a copy of which has been included with this distribution in the           *
* license-agile2d.txt file.                                                 *
*****************************************************************************/

package agile2d;
import agile2d.AgileSample;

/**
* <b>TestAgileSamples</b>
* Inspired from the test of TestGearsAWT.java in JOGL
* @author Rodrigo de Almeida
* @version $Revision$
*//**
**/

import java.lang.Math;

import java.awt.Graphics2D;
import java.awt.Frame;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.awt.image.BufferedImage;
import java.awt.AWTException;

import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;


public class TestAgileSample {
	static GLProfile glp;
	static Frame frame;
	static Frame frameg2d;
	static GLCanvas glCanvas;
	static G2DSample g2dCanvas;
	static AgileSample sample; 
	static private int width, height, win_x, win_y;
	//Number of offset pixels (in each direction) while applying the jittering to the mask	
	final static private int TOLERANCE_PIXELS = 1;
	final static private String RESULT_IMG_DIR = "testResults";
	final static private int RGB_TOLERANCE = 15;

	@BeforeClass
		public static void initClass() {
			width = 512;
			height = 512;
			win_x=80;
			win_y=120;
			glp = GLProfile.getDefault();
			Assert.assertNotNull(glp);
			GLCapabilities glCaps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
			glCaps.setDoubleBuffered(true);// request double buffer display mode
				glCaps.setSampleBuffers(false);
				glCaps.setNumSamples(2);
				glCaps.setStencilBits(1);
				
				frame = new Frame("AgileCanvas Test");
				Assert.assertNotNull(frame);
				
				glCanvas = new GLCanvas(glCaps);
				
				Assert.assertNotNull(glCanvas);
				frame.add(glCanvas);
				frame.setLocation(win_x, win_y);
				frame.setSize(width, height);				
				
				sample = new AgileSample(null);
				glCanvas.addGLEventListener(sample);
				
				frame.setUndecorated(true);
				frame.setVisible(true);
				
				glCanvas.repaint();
				
				Assert.assertNotNull(frame);
				Assert.assertNotNull(glCanvas);				
				
				//Normal Frame
				frameg2d = new Frame("G2DCanvas Test");
				g2dCanvas = new G2DSample();
				frameg2d.add(g2dCanvas);
				frameg2d.setLocation(win_x+width+50, win_y);
				frameg2d.setSize(width, height);
				frameg2d.setUndecorated(true);
				frameg2d.setVisible(true);
				
				g2dCanvas.repaint();
				System.out.println("Number of tolerated dirty pixels when comparing the reference rendering with the Agile rendering: "+TOLERANCE_PIXELS);
		}
	
	@AfterClass
		public static void releaseClass() {
			
		}
	
	
	// TESTS SEQUENCE

	@Test
		public void testDrawRect() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawRect());
			endUnit(context, true, 600, "rect", true, false);
		}

	@Test
		public void prepareCanvas() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyClearRect());
			endUnit(context, true, 600, "clearRect", true, false);
		}
	
	@Test
		public void testDrawRoundRect() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawRoundRect());
			endUnit(context, true, 600, "roundRect", true, false);
		}
	
	@Test
		public void testDrawLine() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawLine());
			endUnit(context, true, 600, "line", true, false);
		}
	
	@Test
		public void testDrawOval() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawOval());
			endUnit(context, true, 600, "oval", true, false);
		}

	@Test
		public void transforms() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyTransforms());
			endUnit(context, true, 1500, "transforms", true, false);
		}
	
	@Test
		public void testDrawString() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawString());
			endUnit(context, true, 4000, "string", true, false);
		}
	
	@Test
		public void testFillOval() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyFillOval());
			endUnit(context, true, 600, "fillOval", true, false);
		}
	
	@Test
		public void testDrawAlpha() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawAlpha());
			endUnit(context, true, 3500, "alpha", false, true);
		}
	
	@Test
		public void testGradient() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyGradient());
			endUnit(context, true, 1500, "gradient", false, true);
		}
	
	@Test
		public void testStrokes() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyStrokes());
			endUnit(context, true, 1500, "strokes", true, false);
		}

	@Test
		public void testCurves() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyCurves());
			endUnit(context, true, 1500, "curves", true, false);
		}

	@Test
		public void testSetGetColor() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetColor());
			endUnit(context, false, 150, "", false, false);
			Color[] color_tmp = (Color[])context.getObjectsStrategy();
			Assert.assertEquals(color_tmp[0].getRGB(), color_tmp[1].getRGB());
		}
	
	
	@Test
		public void testSetGetBackground() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetBackground());
			endUnit(context, false, 150, "", false, false);
			Color[] color_tmp = (Color[])context.getObjectsStrategy();
			Assert.assertEquals(color_tmp[0].getRGB(), color_tmp[1].getRGB());
		}
	
	@Test
		public void testSetGetClipRect() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetClipRect());
			endUnit(context, false, 150, "", false, false);
			Rectangle[] clip_tmp = (Rectangle[])context.getObjectsStrategy();
			Assert.assertEquals(clip_tmp[0], clip_tmp[1]);
		}
	
	@Test
		public void testSetGetFont() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetFont());
			endUnit(context, false, 150, "", false, false);
			Font[] font_tmp = (Font[])context.getObjectsStrategy();
			Assert.assertEquals(font_tmp[0], font_tmp[1]);
		}
	
	@Test
		public void testGetFontMetrics() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyGetFontMetrics());
			endUnit(context, false, 200, "", false, false);
			FontMetrics[] fontM_tmp = (FontMetrics[])context.getObjectsStrategy();
			Assert.assertNotNull(fontM_tmp[0]);
		}
	
	@Test
		public void testSetGetTransform() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetTransform());
			endUnit(context, false, 400, "", false, false);
			AffineTransform[] transf_tmp = (AffineTransform[])context.getObjectsStrategy();
			Assert.assertEquals(transf_tmp[0], transf_tmp[1]);
		}
	
	@Test
		public void testSetGetStroke() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetStroke());
			endUnit(context, false, 150, "", false, false);
			BasicStroke[] strk_tmp = (BasicStroke[])context.getObjectsStrategy();
			Assert.assertEquals(strk_tmp[0], strk_tmp[1]);
		}
	
	//End test : must be ***THE LAST ONE*** to be called in the list of tests
	@Test
		public void testMustBeLastOne() throws InterruptedException {
			frame.setVisible(false);
			Assert.assertEquals(false, frame.isVisible());
			frame.remove(glCanvas);
			frame.dispose();
			frame=null;
			glCanvas=null;
		}
	
	private void endUnit(AglTestContext _context, boolean updateBothContexts, int delay, String basename, boolean testShape, boolean testColor) throws InterruptedException {
		
		//do what must be done in the Agile2D (opengl) context and call its drawing routines
		sample.setContext(_context);
		glCanvas.repaint();
		
		if(updateBothContexts==true){
			//do what must be done in the Graphics2D context and call its drawing routines
			g2dCanvas.setContext(_context);	
			g2dCanvas.repaint();
		}
		else{
			g2dCanvas.setContext(null);
		}
		try { Thread.sleep(delay);} catch (InterruptedException e) {}

		if(testShape==true || testColor==true)
			compareRenderings(basename, testShape, testColor);		
	}
	
	
	private void compareRenderings(String baseName, boolean testShape, boolean testColor){
		System.out.println("Comparing "+baseName);
		BufferedImage imgG2d, imgAg2d, img_mask, jit_mask, img_masked, img_diff, img_report;
		boolean shape_ok, color_ok, build_diff, all_ok;
		File outputfile;
		int[] maxRGB = new int[1];
		img_diff=null;

		all_ok = true;
		color_ok = true;
		//get Agile Front Buffer thanks to glReadPixels
		imgAg2d = sample.getBufferedImage();
		//get G2D image drawing directly on a BufferedImage		
		imgG2d = g2dCanvas.getBufferedImage();
		//Create mask
		img_mask = buildMask(imgG2d);
		//The 2nd argument of jitterMask is the number of tolerance pixels (in each direction) that we want to apply to the mask
		jit_mask = jitterMask(img_mask, TOLERANCE_PIXELS);
		//apply mask
		img_masked = applyMask(imgAg2d, jit_mask);
		if(isAllWhite(img_masked) == true)
			shape_ok=true;
		else
			shape_ok = all_ok = false;
		

		//Set variable build_diff (get diff image only when there's an error or when we should compare pixel rgb values)
		if(!shape_ok || testColor==true)
			build_diff=true;
		else
			build_diff=false;


		//build diff
		if(build_diff==true){

			img_diff = buildDiff(imgG2d, imgAg2d, maxRGB);
		}
		
		if(testColor && build_diff){
			color_ok=true;
			if(maxRGB[0]>RGB_TOLERANCE)
				color_ok = all_ok = false;							
		}
			
		if(all_ok==false){
			try{					
/*
				//write jittered mask image
				outputfile = new File(RESULT_IMG_DIR+"/jit_"+baseName+".png");
				ImageIO.write(jit_mask, "png", outputfile);

				outputfile = new File(RESULT_IMG_DIR+"/masked_"+baseName+".png");
				ImageIO.write(img_masked, "png", outputfile);			
			
				outputfile = new File(RESULT_IMG_DIR+"/mask_"+baseName+".png");
				ImageIO.write(img_mask, "png", outputfile);

				outputfile = new File(RESULT_IMG_DIR+"/ag2d_"+baseName+".png");
				ImageIO.write(imgAg2d, "png", outputfile);
				
				outputfile = new File(RESULT_IMG_DIR+"/g2d_"+baseName+".png");
				ImageIO.write(imgG2d, "png", outputfile);
				
				outputfile = new File(RESULT_IMG_DIR+"/diff_"+baseName+".png");
				ImageIO.write(img_diff, "png", outputfile);

				outputfile = new File(RESULT_IMG_DIR+"/diff_"+baseName+".png");
				ImageIO.write(img_diff, "png", outputfile);
*/

				if(testShape==false)
					img_report = buildReport(baseName, imgG2d, imgAg2d, img_diff, null, testShape);					
				else
					img_report = buildReport(baseName, imgG2d, imgAg2d, img_diff, img_masked, testShape);

				outputfile = new File(RESULT_IMG_DIR+"/report_"+baseName+".png");
				ImageIO.write(img_report, "png", outputfile);
				img_report.flush();

			}catch (IOException e) { }
		}										 

		//check if result image (after applying the mask) has NO DIRTY PIXELS
		Assert.assertTrue(shape_ok);
		Assert.assertTrue(color_ok);

		//Flush buffered images
		imgAg2d.flush();
		imgG2d.flush();		
		img_mask.flush();
		jit_mask.flush();
		img_masked.flush();
		if(build_diff)
			img_diff.flush();
	}
	

        private BufferedImage buildReport(String testName, BufferedImage ref, BufferedImage gen, BufferedImage diff, BufferedImage afterMask, boolean testShape){
		BufferedImage report = new BufferedImage((2*ref.getWidth())+100, 2*(ref.getHeight())+200, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d_ = (Graphics2D)report.createGraphics();
		g2d_.setColor(Color.BLACK);
		g2d_.setBackground(Color.GRAY);
		g2d_.clearRect(0, 0, 1300, 1300);
		g2d_.setFont(new Font("SansSerif", Font.BOLD, 36));
		g2d_.drawString("Testing routine: \""+testName+"\"", 30, 50);
		g2d_.setFont(new Font("SansSerif", Font.PLAIN, 18));
		//Draw first line (reference and test rendering)
		g2d_.drawString("\"Reference\" rendering (Graphics2d)", 30, 110);				
		g2d_.drawImage(ref, 30, 120, null);
		g2d_.drawString("\"Test\" rendering (Agile2d)", 60+ref.getWidth(), 110);	
		g2d_.drawImage(gen, 60+ref.getWidth(), 120, null);
		//Draw second line (masked and diff rendering)
		g2d_.drawString("Difference rendering: test minus reference", 30, 170+ref.getHeight());	
		g2d_.drawImage(diff, 30, 180+ref.getHeight(), null);
		if(testShape==true){
			g2d_.drawString("Test rendering after mask (image should be all white)",  60+ref.getWidth(), 170+ref.getHeight());	
			g2d_.drawImage(afterMask, 60+ref.getWidth(), 180+ref.getHeight(), null);
		}
		return report;
	}

	private BufferedImage buildMask(BufferedImage ref) {
		int x, y;
		int w=ref.getWidth();
		int h=ref.getHeight();
		BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);				
		
		for (y=0; y<h; y++) {
			//get a row of pixels from the reference image
			for(x=0; x<w; x++){
				if( (ref.getRGB(x, y) & 0x00ffffff) == 0x00ffffff) 
					mask.setRGB(x,y, 0x00ffffff);
				else
					mask.setRGB(x,y, 0xffffffff);
			}
		}
		return mask;
	}

	private BufferedImage jitterMask(BufferedImage mask, int jitDist){
		BufferedImage jitMask = new BufferedImage(mask.getWidth(), mask.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d_ = (Graphics2D)jitMask.createGraphics();
		int i, j;
		//Draw first base mask
		g2d_.drawImage(mask, 0, 0, null);
		//Jitter drawing other offset masks with a tolerance given by jitDist
		for(i=-jitDist; i<=jitDist; i++){
			for(j=-jitDist; j<=jitDist; j++){
				//mask the 8 neighbour pixels around each masked pixel
				if( !(i==0 && j==0) )
					g2d_.drawImage(mask, i, j, null);
			}
		}
		return jitMask;
	}

	private BufferedImage applyMask(BufferedImage gen, BufferedImage mask) {
		BufferedImage masked = new BufferedImage(gen.getWidth(), gen.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d_ = (Graphics2D)masked.createGraphics();
		g2d_.drawImage(gen, 0, 0, null);
		g2d_.drawImage(mask, 0, 0, null);	
		return masked;
	}


	private BufferedImage buildDiff(BufferedImage ref, BufferedImage gen, int[] maxRGB) {
		int w=ref.getWidth();
		int h=ref.getHeight();
		BufferedImage diffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		WritableRaster refWR = ref.getRaster();
		WritableRaster genWR = gen.getRaster();
		WritableRaster diffWR = diffImg.getRaster();

		int x, y, x_max;
		int [] refPix = null;
		int [] genPix = null;
		int [] diffPix = new int[w*4];
		int r,g,b;
		int drgb;
		int dist_max=0;

		for(y=0; y<h; y++){
			refPix = refWR.getPixels(0, y, w, 1, refPix);
			genPix = genWR.getPixels(0, y, w, 1, genPix);
			for(x=0; x<refPix.length; x+=4){
				r= Math.abs(genPix[x  ]-refPix[x  ]);
				g= Math.abs(genPix[x+1]-refPix[x+1]);
				b= Math.abs(genPix[x+2]-refPix[x+2]);
				drgb = distRGB(refPix[x], refPix[x+1], refPix[x+2], genPix[x], genPix[x+1], genPix[x+2]);
				diffPix[x  ] = r;
				diffPix[x+1] = g;
				diffPix[x+2] = b;
				//Alpha
				diffPix[x+3] = 0xff;
				//keeps the max rgb difference value
				if(drgb>dist_max)
					dist_max=drgb;				
			}
			diffWR.setPixels(0, y, w, 1, diffPix);
		}
		maxRGB[0] = dist_max;
		System.out.println("Max difference found in RGB space: "+maxRGB[0]+" (Tolerance = "+RGB_TOLERANCE+". Black/White diff = 443)");
		return diffImg;
	}

	private int distRGB(int r1, int g1, int b1, int r2, int g2, int b2){
		int dist = (int)Math.sqrt(
				(double)( Math.pow(r2-r1, 2.0) + Math.pow(g2-g1, 2.0) + Math.pow(b2-b1, 2.0) )
				);
		return dist;		
	}

	//Apply mask pixel per pixel
	private boolean isAllWhite(BufferedImage masked){
		WritableRaster maskedWR = masked.getRaster();
		int w=masked.getWidth();
		int h=masked.getHeight();
		int x, y, x_max;
		int [] maskedPix = null;
		for(y=0; y<h; y++){
			maskedPix = maskedWR.getPixels(0, y, w, 1, maskedPix);
			x_max = maskedPix.length;
			for(x=0; x < x_max; x++){
				if(maskedPix[x] != 0xff){
					//System.out.println("1st dirty pixel coordinate: <"+(x/4)+", "+y+" >");
					return false;
				}
			}
		}
		return true;
	}	
}
