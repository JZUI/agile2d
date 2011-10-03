/****************************************************************************************
 * Copyright (C) 2011, Jean-Daniel Fekete, Emmanuel Pietriga and Rodrigo de Almeida	*
 * -------------------------------------------------------------------------		*
 * This software is published under the terms of the BSD Software License    		*
 * a copy of which has been included with this distribution in the           		*
 * license-agile2d.txt file.                                                 		*
 ***************************************************************************************/

package agile2d;

import agile2d.*;

import java.lang.Math;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;


/**
 * <b>CompareRoutines</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 */

/*
//The Class CompareRoutines assembles all the routines that HAVE TO BE called both in the AgileGraphics2d Rendering Context and in
//the traditional Graphics2D one ; that's why the params of the called methods are "hardcoded".
//(BEGIN Example)
//..
//AgileGraphics2D agile = new AgileGraphics2D();
//CompareRoutines.drawRect(agile);
//Graphics2D j2d = new Graphcis2D();
//CompareRoutines.drawRect(j2d);
//..
//(END EXAMPLE)
//Obs: the graphical output of the two display is supposed to be quite similars
*/

class CompareRoutines {

	public static void drawRect(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		g2d.drawRect(45, 60, 185, 400);
	}

	public static void clearRect(Graphics2D g2d){
		//draw some stuff and then try to clear it
		g2d.setColor(Color.BLACK);
		g2d.fillRect(160, 160, 330, 330);
	        g2d.clearRect(150, 150, 300, 300);
	}

	public static void drawLine(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
	        g2d.drawLine(200, 30, 250, 500);
	}

	public static void drawOval(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
        	g2d.drawOval(200, 200, 100, 100);
	}
	
	public static void drawRoundRect(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		g2d.drawRoundRect(100, 100, 350, 50, 50, 100);
	}

	public static void drawString(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		g2d.drawString("DefautSet Float", 40.5f, 50.5f);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
//		g2d.setFont(new Font("Garuda", Font.BOLD, 24));
		g2d.drawString("Sans Serif", 40, 80);

		g2d.setFont(new Font("Serif", Font.PLAIN, 24));
		g2d.drawString("Serif, Plain, 24", 40, 110);

		g2d.setFont(new Font("Monospaced", Font.PLAIN, 24));
		g2d.drawString("Monospaced, Plain, 24", 40, 140);

		g2d.setFont(new Font("Dialog", Font.PLAIN, 24));
		g2d.drawString("Dialog, Plain, 24", 40, 170);

		g2d.setFont(new Font("DialogInput", Font.PLAIN, 24));
		g2d.drawString("DialogInput, Plain, 24", 40, 200);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
		g2d.drawString("SansSerif, Plain, 24", 40, 230);

		g2d.setFont(new Font("SansSerif", Font.ITALIC, 36));
		g2d.drawString("SansSerif, Italic, 36", 40, 270);

		g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
		g2d.drawString("SansSerif, Bold, 36", 40, 310);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 72));
		g2d.drawString("Big Text", 40, 400);

		g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
		g2d.drawString("Sans Serif, 36 (2)", 40, 450);
	}

/*
	public static void drawStringSize(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		final int SIZE_INIT = 10;
		final int SIZE_MAX = 300;
		int x_ = 2;
		int y_ = 40;
		for(int size_=SIZE_INIT; size_<SIZE_MAX; size_+=2){
			if( (x_+size_) > 512 ){
				x_ = 2;
				y_ += size_;
				if( y_ > 512 )
					return;
			}
			else
				x_ += (int)(size_/2);
			g2d.setFont(new Font("SansSerif", Font.PLAIN, size_));
			g2d.drawString("a", x_, y_);
		}
	}
*/
	
	public static void drawStringSize(Graphics2D g2d){
		final int SIZE_INIT = 10;
		final int SIZE_MAX = 120;
		final int SIZE_STEP = 4;
		g2d.setColor(Color.BLACK);
		
		//double current_size = SIZE_INIT;
		int x_ = 2;
		int init_y = 0;
		int cursor_y = init_y;
		
		for(int sz_= SIZE_INIT; sz_<SIZE_MAX; sz_+=SIZE_STEP){
			//current_size = init_size+sz_;
			cursor_y += sz_;
			//g2d.scale(scl_, scl_);
			g2d.setFont(new Font("Serif", Font.BOLD, sz_));
			g2d.drawString("abcdefghijklmnopqrstuvwxyz", x_, cursor_y);
			//g2d.scale(1.0/scl_, 1.0/scl_);
		}
	}	
	
	
	
	public static void drawStringScale(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Serif", Font.ITALIC, 10));
		final double SCALE_INIT = 1.0;
		final double SCALE_MAX = 8.0;
		final double SCALE_STEP = 0.45;
		final double init_size = 10.5;
		double current_size = init_size;
		int x_ = 2;
		int init_y = 0;
		double cursor_y = init_y;
		
		for(double scl_= SCALE_INIT; scl_<SCALE_MAX; scl_+=SCALE_STEP){
			current_size = init_size*scl_;
			cursor_y += current_size;
			g2d.scale(scl_, scl_);
			g2d.drawString("abcdefghijklmnopqrstuvwxyz", x_, (int)(cursor_y/scl_));
			g2d.scale(1.0/scl_, 1.0/scl_);
		}
	}	
	
	

	public static void drawGlyphVector(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		//small glyph
		String st = "Small GlyphVector";
		Font font_ = new Font("SansSerif", Font.BOLD, 12);
		g2d.setFont(font_);
		FontRenderContext fontRendContext = g2d.getFontRenderContext();
	        GlyphVector glyphVectorSmall = font_.createGlyphVector(fontRendContext, st);
        	g2d.drawGlyphVector(glyphVectorSmall, 50, 120);
		//medium glyph
		st = "Medium";
		font_ = new Font("SansSerif", Font.BOLD, 72);
		g2d.setFont(font_);
		fontRendContext = g2d.getFontRenderContext();
	        GlyphVector glyphVectorMedium = font_.createGlyphVector(fontRendContext, st);
        	g2d.drawGlyphVector(glyphVectorMedium, 50, 90);
		//big glyph
		st = "Big";
		font_ = new Font("Monospaced", Font.PLAIN, 180);
		g2d.setFont(font_);
		fontRendContext = g2d.getFontRenderContext();
	        GlyphVector glyphVectorBig = font_.createGlyphVector(fontRendContext, st);
        	g2d.drawGlyphVector(glyphVectorBig, 50, 300);
	}

	public static void drawGlyphVectorSize(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		final int SIZE_INIT = 10;
		final int SIZE_MAX = 300;
		final String SAMPLE_STRING = "g";
		int x_ = 2;
		int y_ = 40;
		for(int size_=SIZE_INIT; size_<SIZE_MAX; size_+=2){
			if( (x_+size_) > 512 ){
				x_ = 2;
				y_ += size_;
				if( y_ > 512 )
					return;
			}
			else
				x_ += (int)(size_/2);
			Font font_ = new Font("SansSerif", Font.PLAIN, size_);
			g2d.setFont(font_);
			FontRenderContext fontRendContext = g2d.getFontRenderContext();
		        GlyphVector glyphVector_ = font_.createGlyphVector(fontRendContext, SAMPLE_STRING);
        		g2d.drawGlyphVector(glyphVector_, x_, y_);
		}
	}


	public static void fillOval(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
	        g2d.fillOval(100, 100, 200, 100);
	}

	public static void gradient(Graphics2D g2d) {
		GradientPaint gradient = new GradientPaint(0, 0, Color.red, 175, 175, Color.yellow,true); // true (last arg) means repeat pattern
		g2d.setPaint(gradient);
		//g2d.fillOval(200, 200, 80, 280);
		g2d.fillRect(200, 100, 150, 350);
	}	

	public static void drawAlpha(Graphics2D g2d) {
		int w, h;
	        Color reds[] = { Color.red.darker(), Color.red };
		//Dimensions of the canvas
		w = h = 500;
         	//Fills 18 Ellipse2D.Float objects, which get smaller as N increases
	        for (int N = 0; N < 18; N++) {
	            float i = (N + 2) / 2.0f;
	            float x = (float) (5+i*(w/2/10));
	            float y = (float) (5+i*(h/2/10));
	            float ew = (w-10)-(i*w/10);
	            float eh = (h-10)-(i*h/10);
	            //assigns a higher value of alpha, corresponding to a higher value of N
	            float alpha = (N == 0) ? 0.1f : 1.0f / (19.0f - N);
        	    // sets the ellipse to a darker version of red if N < 16
        	    if ( N >= 16 ) {
        	        g2d.setColor(reds[N-16]);
        	    } else {
        	        g2d.setColor(new Color(0f, 0f, 0f, alpha));
        	    }
        	    //g2d.fill(new Ellipse2D.Float(x,y,ew,eh));
		    g2d.fill(new Rectangle.Float(x,y,ew,eh));
        	}
	}


public static void strokes(Graphics2D g2d) {
	int w, h, x, y, dist, nb_strokes;
	g2d.setColor(Color.BLACK);
	h = w = 440;
	x = y = 50;
	dist = 20;
	nb_strokes=6;

	//Draw basic dotted round line
        BasicStroke dotted = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6,6,6,6}, 0);
        g2d.setStroke(dotted);
        g2d.drawLine(x, y, w, y);

	//Draw basic dotted round line
        BasicStroke bs[] = new BasicStroke[nb_strokes];
        float j = 1.1f;
        for (int i = 0; i < nb_strokes; i++, j += 1.0f) {
            y += dist;
            float dash[] = { j };
            BasicStroke b = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
            g2d.setStroke(b);
            g2d.drawLine(x, y, w, y);
            bs[i] = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        }
        for (int i = 0; i < nb_strokes; i++) {
	    y += dist;
            g2d.setStroke(bs[i]);
	    g2d.drawLine(x, y, w, y);
        }
    }

	public static void transforms(Graphics2D g2d){
		double mat[] = {0.8, 0.13, 0.15, 1, 22.287617012502135, 25.73362893827651};
		g2d.setColor(Color.BLACK);
		g2d.setTransform(new AffineTransform(mat));
		g2d.rotate(0.5);
		g2d.rotate(-0.07, 50.0, -50.0);
		g2d.translate(-50, -150);
		g2d.scale(1.7, 1.1);
		g2d.shear(-0.5, -0.3);
		g2d.fillRect(200, 200, 100, 90);
	}

    public static void curves(Graphics2D g2) {
	int w = 512;
	int h = 512;
        int y = 0;
        g2.setColor(Color.black);

        g2.setStroke(new BasicStroke(5.0f));

        float yy = 20;

	// draws 3 quad curves and 3 cubic curves.
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                Shape shape = null;

                if (i == 0) {
                     shape = new QuadCurve2D.Float(w*.1f,yy,w*.5f,50,w*.9f,yy);
                } else {
                     shape = new CubicCurve2D.Float(w*.1f,yy,w*.4f,yy-15,
                                            w*.6f,yy+15,w*.9f,yy);
                }
                if (j != 2)
                    g2.draw(shape);

                if (j == 1 ) {

		    /*
                     * creates an iterator object to iterate the boundary
		     * of the curve.
                     */
                    PathIterator f = shape.getPathIterator(null);

		    /*
                     * while iteration of the curve is still in process
                     * fills rectangles at the endpoints and control
                     * points of the curve.
                     */
                    while ( !f.isDone() ) {
                        float[] pts = new float[6];
                        switch ( f.currentSegment(pts) ) {
                            case PathIterator.SEG_MOVETO:
                            case PathIterator.SEG_LINETO:
                                g2.fill(new Rectangle2D.Float(pts[0], pts[1], 5, 5));
                                break;
                            case PathIterator.SEG_CUBICTO :
                            case PathIterator.SEG_QUADTO :
                                g2.fill(new Rectangle2D.Float(pts[0], pts[1], 5, 5));
                                if (pts[2] != 0) {
                                    g2.fill(new Rectangle2D.Float(pts[2], pts[3], 5, 5));
                                }
                                if (pts[4] != 0) {
                                    g2.fill(new Rectangle2D.Float(pts[4], pts[5], 5, 5));
                                }
                        }
                        f.next();
                    }
               
                } else if (j == 2) {
		    // draws red ellipses along the flattened curve.
                    PathIterator p = shape.getPathIterator(null);
                    FlatteningPathIterator f = new FlatteningPathIterator(p,0.1);
                    while ( !f.isDone() ) {
                        float[] pts = new float[6];
                        switch ( f.currentSegment(pts) ) {
                            case PathIterator.SEG_MOVETO:
                            case PathIterator.SEG_LINETO:
                                g2.fill(new Ellipse2D.Float(pts[0], pts[1],3,3));
                        }
                        f.next();
                    }
                }
                yy += h/6;
            }
            yy = h/2+15;
        }
    }




}

