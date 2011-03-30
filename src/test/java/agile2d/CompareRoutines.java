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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.geom.*;


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
	public static void clearRect(Graphics2D g2d){
	        g2d.clearRect(150, 150, 300, 300);
	}

	public static void drawLine(Graphics2D g2d){
	        g2d.drawLine(200, 30, 250, 500);
	}

	public static void drawOval(Graphics2D g2d){
        	g2d.drawOval(200, 200, 100, 100);
	}

	public static void drawRect(Graphics2D g2d){
		g2d.drawRect(45, 60, 185, 400);
	}
	
	public static void drawRoundRect(Graphics2D g2d){
		g2d.drawRoundRect(100, 100, 350, 50, 50, 100);
	}

	public static void drawString(Graphics2D g2d){
		g2d.drawString("DefautSet Float", 40.5f, 50.5f);
		g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
		g2d.drawString("DefautSet Int", 40, 80);
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
	}

	public static void fillOval(Graphics2D g2d){
		g2d.setColor(Color.RED);
	        g2d.fillOval(100, 100, 200, 100);
	}

	public static void gradient(Graphics2D g2d) {
		GradientPaint gradient = new GradientPaint(0, 0, Color.red, 175, 175, Color.yellow,true); // true (last arg) means repeat pattern
		g2d.setPaint(gradient);
		g2d.fillOval(200, 200, 80, 280);
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
        	    g2d.fill(new Ellipse2D.Float(x,y,ew,eh));
        	}
	}



public static void strokes(Graphics2D g2d) {
	int w, h, x, y, dist, nb_strokes;
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


}

