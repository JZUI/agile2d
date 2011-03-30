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
import org.apache.batik.ext.awt.image.GraphicsUtil;


public class TestAgileSample {
    static GLProfile glp;
    static Frame frame;
    static Frame frameg2d;
    static GLCanvas glCanvas;
    static G2DSample g2dCanvas;
    static AgileSample sample; 
    static int width, height, win_x, win_y;
    BufferedImage imgG2d, imgAg2d;
    final int CAP_OFFSET = 15;

    @BeforeClass
    public static void initClass() {
        width = 512;
        height = 512;
		win_x=80;
		win_y=120;
        glp = GLProfile.getDefault();
        Assert.assertNotNull(glp);
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());

	frame = new Frame("AgileCanvas Test");
        Assert.assertNotNull(frame);

	glCanvas = new GLCanvas(caps);
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
	endUnit(context, true, 150);
    }

    @Test
    public void testDrawRect() throws InterruptedException {
	writePreviousDiff("cRect.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyDrawRect());
	endUnit(context, true, 150);
    }

    @Test
    public void testDrawRoundRect() throws InterruptedException {
	writePreviousDiff("rect.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyDrawRoundRect());
	endUnit(context, true, 150);
    }

    @Test
    public void testDrawLine() throws InterruptedException {
	writePreviousDiff("roundRect.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyDrawLine());
	endUnit(context, true, 150);
    }

   @Test
    public void testDrawOval() throws InterruptedException {
	writePreviousDiff("line.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyDrawOval());
	endUnit(context, true, 150);
    }


   @Test
    public void testDrawString() throws InterruptedException {
	writePreviousDiff("oval.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyDrawString());
	endUnit(context, true, 1500);
    }

   @Test
    public void testFillOval() throws InterruptedException {
	writePreviousDiff("String.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyFillOval());
	endUnit(context, true, 150);
    }

   @Test
    public void testDrawAlpha() throws InterruptedException {
	writePreviousDiff("FillOval.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyDrawAlpha());
	endUnit(context, true, 3500);
   }

   @Test
    public void testGradient() throws InterruptedException {
	writePreviousDiff("drawAlpha.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyGradient());
	endUnit(context, true, 800);
   }

   @Test
    public void testStrokes() throws InterruptedException {
	writePreviousDiff("drawGradient.png");
        AglTestContext context = new AglTestContext(new AglTestStrategyStrokes());
	endUnit(context, true, 1000);
   }

   @Test
    public void testSetGetColor() throws InterruptedException {
	writePreviousDiff("testStrokes.png");
        AglTestContext context = new AglTestContext(new AglTestStrategySetGetColor());
	endUnit(context, false, 30);
	Color[] color_tmp = (Color[])context.getObjectsStrategy();
        Assert.assertEquals(color_tmp[0].getRGB(), color_tmp[1].getRGB());
    }


   @Test
    public void testSetGetBackground() throws InterruptedException {
        AglTestContext context = new AglTestContext(new AglTestStrategySetGetBackground());
	endUnit(context, false, 30);
	Color[] color_tmp = (Color[])context.getObjectsStrategy();
        Assert.assertEquals(color_tmp[0].getRGB(), color_tmp[1].getRGB());
    }

   @Test
    public void testSetGetClipRect() throws InterruptedException {
        AglTestContext context = new AglTestContext(new AglTestStrategySetGetClipRect());
	endUnit(context, false, 30);
	Rectangle[] clip_tmp = (Rectangle[])context.getObjectsStrategy();
        Assert.assertEquals(clip_tmp[0], clip_tmp[1]);
    }

   @Test
    public void testSetGetFont() throws InterruptedException {
        AglTestContext context = new AglTestContext(new AglTestStrategySetGetFont());
	endUnit(context, false, 30);
	Font[] font_tmp = (Font[])context.getObjectsStrategy();
        Assert.assertEquals(font_tmp[0], font_tmp[1]);
    }

   @Test
    public void testGetFontMetrics() throws InterruptedException {
        AglTestContext context = new AglTestContext(new AglTestStrategyGetFontMetrics());
	endUnit(context, false, 120);
	FontMetrics[] fontM_tmp = (FontMetrics[])context.getObjectsStrategy();
        Assert.assertNotNull(fontM_tmp[0]);
    }

   @Test
    public void testSetGetTransform() throws InterruptedException {
        AglTestContext context = new AglTestContext(new AglTestStrategySetGetTransform());
	endUnit(context, false, 30);
	AffineTransform[] transf_tmp = (AffineTransform[])context.getObjectsStrategy();
        Assert.assertEquals(transf_tmp[0], transf_tmp[1]);
    }

   @Test
    public void testSetGetStroke() throws InterruptedException {
        AglTestContext context = new AglTestContext(new AglTestStrategySetGetStroke());
	endUnit(context, false, 10);
	BasicStroke[] strk_tmp = (BasicStroke[])context.getObjectsStrategy();
        Assert.assertEquals(strk_tmp[0], strk_tmp[1]);
    }

   //End test : It must be ***THE LAST ONE*** to be called in the list of tests
   @Test
    public void testMustBeLastOne() throws InterruptedException {
	
        frame.setVisible(false);
        Assert.assertEquals(false, frame.isVisible());
        frame.remove(glCanvas);
        frame.dispose();
        frame=null;
        glCanvas=null;
	}

    public void endUnit(AglTestContext _context, boolean updateBothContexts, int delay) throws InterruptedException {
	sample.setContext(_context);
	glCanvas.repaint();
	if(updateBothContexts==true){
		g2dCanvas.setContext(_context);	
		g2dCanvas.repaint();
	}
	else{
		g2dCanvas.setContext(null);
	}
	try { Thread.sleep(delay);} catch (InterruptedException e) {}
    }

    public void writePreviousDiff(String imgName){
		File outputfile;
		imgAg2d = imgG2d = null;
		try{
			Robot rob = new Robot();
			imgAg2d = rob.createScreenCapture(new Rectangle(win_x+CAP_OFFSET, win_y+CAP_OFFSET, width-(2*CAP_OFFSET), height-(2*CAP_OFFSET)));
			imgG2d = rob.createScreenCapture(new Rectangle(win_x+width+CAP_OFFSET, win_y+CAP_OFFSET, width-(2*CAP_OFFSET), height-(2*CAP_OFFSET)));
  	        } catch (AWTException e) {   }
		//
		//Create bufferImage which is the difference of the two images
		BufferedImage image_dif = batikBuildDiffImage(imgAg2d, imgG2d);
		//
		try{
			outputfile = new File(imgName);
			ImageIO.write(image_dif, "png", outputfile);
						
			outputfile = new File("g2d_"+imgName);
			ImageIO.write(imgG2d, "png", outputfile);

			outputfile = new File("ag2d_"+imgName);
			ImageIO.write(imgAg2d, "png", outputfile);
			
			
		}catch (IOException e) { }
	}

public static BufferedImage batikBuildDiffImage(BufferedImage ref, BufferedImage gen) {
        BufferedImage diff = new BufferedImage(ref.getWidth(),
                                               ref.getHeight(),
                                               BufferedImage.TYPE_INT_RGB);
        WritableRaster refWR = ref.getRaster();
        WritableRaster genWR = gen.getRaster();
        WritableRaster dstWR = diff.getRaster();

        boolean refPre = ref.isAlphaPremultiplied();
        if (!refPre) {
            ColorModel     cm = ref.getColorModel();
            cm = GraphicsUtil.coerceData(refWR, cm, true);
            ref = new BufferedImage(cm, refWR, true, null);
        }
        boolean genPre = gen.isAlphaPremultiplied();
        if (!genPre) {
            ColorModel     cm = gen.getColorModel();
            cm = GraphicsUtil.coerceData(genWR, cm, true);
            gen = new BufferedImage(cm, genWR, true, null);
        }


        int w=ref.getWidth();
        int h=ref.getHeight();

        int y, i,val;
        int [] refPix = null;
        int [] genPix = null;
        for (y=0; y<h; y++) {
            refPix = refWR.getPixels  (0, y, w, 1, refPix);
            genPix = genWR.getPixels  (0, y, w, 1, genPix);

            for (i=0; i<refPix.length; i++) {
                val = ((refPix[i]-genPix[i])*10)+128;
                if ((val & 0xFFFFFF00) != 0)
                    if ((val & 0x80000000) != 0) val = 0;
                    else                         val = 255;
                genPix[i] = val;
            }
            dstWR.setPixels(0, y, w, 1, genPix);
        }

        if (!genPre) {
            ColorModel cm = gen.getColorModel();
            cm = GraphicsUtil.coerceData(genWR, cm, false);
        }
        
        if (!refPre) {
            ColorModel cm = ref.getColorModel();
            cm = GraphicsUtil.coerceData(refWR, cm, false);
        }

        return diff;
    }

}
