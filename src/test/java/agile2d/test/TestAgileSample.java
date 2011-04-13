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
import java.awt.Robot;
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
	static int width, height, win_x, win_y;
	//BufferedImage imgG2d, imgAg2d, img_mask, img_masked;
	final int CAP_OFFSET = 15;
	
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
				glCaps.setSampleBuffers(true);
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
				frameg2d.setLocation(win_x+width, win_y);
				frameg2d.setSize(width, height);
				frameg2d.setUndecorated(true);
				frameg2d.setVisible(true);
				
				g2dCanvas.repaint();
		}
	
	@AfterClass
		public static void releaseClass() {
			
		}
	
	
	// TESTS SEQUENCE
	
	@Test
		public void prepareCanvas() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyClearRect());
			endUnit(context, true, 200, "clearRect");
		}
	
	@Test
		public void testDrawRect() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawRect());
			endUnit(context, true, 400, "rect");
		}
	
	@Test
		public void testDrawRoundRect() throws InterruptedException {
			//writePreviousDiff("rect");
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawRoundRect());
			endUnit(context, true, 200, "roundRect");
		}
	
	@Test
		public void testDrawLine() throws InterruptedException {
			//writePreviousDiff("roundRect");
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawLine());
			endUnit(context, true, 200, "line");
		}
	
	@Test
		public void testDrawOval() throws InterruptedException {
			//writePreviousDiff("line");
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawOval());
			endUnit(context, true, 200, "oval");
		}
	
	@Test
		public void testDrawString() throws InterruptedException {
			//writePreviousDiff("oval");
				AglTestContext context = new AglTestContext(new AglTestStrategyDrawString());
				endUnit(context, true, 4000, "string");
		}
	
	@Test
		public void testFillOval() throws InterruptedException {
			//writePreviousDiff("String");
			AglTestContext context = new AglTestContext(new AglTestStrategyFillOval());
			endUnit(context, true, 150, "fillOval");
		}
	
	@Test
		public void testDrawAlpha() throws InterruptedException {
			//writePreviousDiff("FillOval");
			AglTestContext context = new AglTestContext(new AglTestStrategyDrawAlpha());
			endUnit(context, true, 3500, "alpha");
		}
	
	@Test
		public void testGradient() throws InterruptedException {
			//writePreviousDiff("drawAlpha");
			AglTestContext context = new AglTestContext(new AglTestStrategyGradient());
			endUnit(context, true, 800, "gradient");
		}
	
	@Test
		public void testStrokes() throws InterruptedException {
			//writePreviousDiff("drawGradient");
			AglTestContext context = new AglTestContext(new AglTestStrategyStrokes());
			endUnit(context, true, 1500, "strokes");
		}
	
	@Test
		public void testSetGetColor() throws InterruptedException {
			//writePreviousDiff("testStrokes");
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetColor());
			endUnit(context, false, 150, "");
			Color[] color_tmp = (Color[])context.getObjectsStrategy();
			Assert.assertEquals(color_tmp[0].getRGB(), color_tmp[1].getRGB());
		}
	
	
	@Test
		public void testSetGetBackground() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetBackground());
			endUnit(context, false, 150, "");
			Color[] color_tmp = (Color[])context.getObjectsStrategy();
			Assert.assertEquals(color_tmp[0].getRGB(), color_tmp[1].getRGB());
		}
	
	@Test
		public void testSetGetClipRect() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetClipRect());
			endUnit(context, false, 150, "");
			Rectangle[] clip_tmp = (Rectangle[])context.getObjectsStrategy();
			Assert.assertEquals(clip_tmp[0], clip_tmp[1]);
		}
	
	@Test
		public void testSetGetFont() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetFont());
			endUnit(context, false, 150, "");
			Font[] font_tmp = (Font[])context.getObjectsStrategy();
			Assert.assertEquals(font_tmp[0], font_tmp[1]);
		}
	
	@Test
		public void testGetFontMetrics() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategyGetFontMetrics());
			endUnit(context, false, 200, "");
			FontMetrics[] fontM_tmp = (FontMetrics[])context.getObjectsStrategy();
			Assert.assertNotNull(fontM_tmp[0]);
		}
	
	@Test
		public void testSetGetTransform() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetTransform());
			endUnit(context, false, 400, "");
			AffineTransform[] transf_tmp = (AffineTransform[])context.getObjectsStrategy();
			Assert.assertEquals(transf_tmp[0], transf_tmp[1]);
		}
	
	@Test
		public void testSetGetStroke() throws InterruptedException {
			AglTestContext context = new AglTestContext(new AglTestStrategySetGetStroke());
			endUnit(context, false, 150, "");
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
	
	public void endUnit(AglTestContext _context, boolean updateBothContexts, int delay, String basename) throws InterruptedException {
		sample.setContext(_context);
		glCanvas.repaint();
		
		//get Agile Ilage thanks to glReadPixels
//		imgAg2d = sample.getBufferedImage();
		
		if(updateBothContexts==true){
			g2dCanvas.setContext(_context);	
			g2dCanvas.repaint();
//			imgG2d = g2dCanvas.getBufferedImage();
		}
		else{
			g2dCanvas.setContext(null);
		}
		try { Thread.sleep(delay);} catch (InterruptedException e) {}

		if(updateBothContexts==true)
			writePreviousDiff(basename);					
	}
	
	
	public void writePreviousDiff(String baseName){
		BufferedImage imgG2d, imgAg2d, img_mask, img_masked;
		File outputfile;
		//get Agile Front Buffer thanks to glReadPixels
		imgAg2d = sample.getBufferedImage();
		//get G2D image drawing directly on a BufferedImage		
		imgG2d = g2dCanvas.getBufferedImage();
		//Create mask
		img_mask = buildMask(imgG2d);
		//apply mask
		img_masked = applyMask(imgAg2d, img_mask);
//		if(isAllWhite(img_masked))
//			System.out.println(baseName+" is all white");


		try{
			
			//write mask image file
			outputfile = new File("mask_"+baseName+".png");
			ImageIO.write(img_mask, "png", outputfile);
			
			outputfile = new File("masked_"+baseName+".png");
			ImageIO.write(img_masked, "png", outputfile);
			
//			outputfile = new File("masked_"+baseName+".png");
//			ImageIO.write(img_masked, "png", outputfile);

/*						
			outputfile = new File("ag2d_"+baseName+".png");
			ImageIO.write(imgAg2d, "png", outputfile);
			
			outputfile = new File("g2d_"+baseName+".png");
			ImageIO.write(imgG2d, "png", outputfile);
*/						 
		}catch (IOException e) { }
		Assert.assertTrue(isAllWhite(img_masked));
		//Flush buffered images
		imgAg2d.flush();
		imgG2d.flush();		
		img_mask.flush();
		img_masked.flush();
	}
	
	
	public static BufferedImage buildMask(BufferedImage ref) {
		BufferedImage mask = new BufferedImage(ref.getWidth(),ref.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		int w=ref.getWidth();
		int h=ref.getHeight();
		WritableRaster mskWR = mask.getRaster();
				
		int x, y;
		int [] mskPix = new int[w];
		
		for (y=0; y<h; y++) {
			//get a row of pixels from the reference image
			for(x=0; x<w; x++){
				if( (ref.getRGB(x, y) & 0x00ffffff) == 0x00ffffff) 
					mskPix[x] = 255;
				else
					mskPix[x] = 0; 
			}
			mskWR.setPixels(0, y, w, 1, mskPix);
		}
		return mask;
	}
	
	//Apply mask pixel per pixel
	public int getNewPix(int pixel, int mask){
		if((mask ^ 0xFF) == 0)
			return pixel;
		else
			return 0xFFFFFFFF;
	}
				
	public BufferedImage applyMask(BufferedImage gen, BufferedImage mask) {
		BufferedImage masked = new BufferedImage(gen.getWidth(), gen.getHeight(), BufferedImage.TYPE_INT_ARGB);
		WritableRaster maskWR = mask.getRaster();

		int w=gen.getWidth();
		int h=gen.getHeight();
		int x, y;
		int [] maskPix = null;
		
		for (y=0; y<h; y++) {
			for(x=0; x <w; x++){
				maskPix = maskWR.getPixel(x, y, maskPix);
				masked.setRGB(x, y, getNewPix(gen.getRGB(x, y), maskPix[0]));
			}
		}
		
		return masked;
	}

	//Apply mask pixel per pixel
	public boolean isAllWhite(BufferedImage masked){
		WritableRaster maskedWR = masked.getRaster();
		int w=masked.getWidth();
		int h=masked.getHeight();
		int x, y, x_max;
		int [] maskedPix = null;
		for(y=0; y<h; y++){
			maskedPix = maskedWR.getPixels(0, y, w, 1, maskedPix);
			x_max = maskedPix.length;
			for(x=0; x < x_max; x++){
				if(maskedPix[x] != 255){
//					System.out.println("1st dirty pixel coordinate: <"+(x/4)+", "+y+" >");
					return false;
				}
			}
		}
		return true;
	}

	
}
