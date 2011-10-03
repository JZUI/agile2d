/*****************************************************************************
 * Copyright (C) 2011, Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

/**
 * <b>AglConcreteTestStrategies</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 */

package agile2d;

import agile2d.*;

import java.lang.Math;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.awt.Graphics2D;

class AglTestStrategyClearRect implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.clearRect(gSample);
    }
     public Object[] getObjects() {return null;}
}


class AglTestStrategyDrawRect implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawRect(gSample);
     }    
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawRoundRect implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawRoundRect(gSample);
     }    
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawLine implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawLine(gSample);
     }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawOval implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawOval(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawString implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawString(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawStringSize implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawStringSize(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawStringScale implements TestStrategy {
    public void draw(Graphics2D gSample) {
	CompareRoutines.drawStringScale(gSample);
   }
    public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawGlyphVector implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawGlyphVector(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawGlyphVectorSize implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawGlyphVectorSize(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawGlyphVectorScale implements TestStrategy {
    public void draw(Graphics2D gSample) {
	CompareRoutines.drawGlyphVectorScale(gSample);
   }
    public Object[] getObjects() {return null;}
}

class AglTestStrategyFillOval implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.fillOval(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyDrawAlpha implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.drawAlpha(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyGradient implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.gradient(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyStrokes implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.strokes(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategyTransforms implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.transforms(gSample);
    }
     public Object[] getObjects() {return null;}
}


class AglTestStrategyCurves implements TestStrategy {
     public void draw(Graphics2D gSample) {
	CompareRoutines.curves(gSample);
    }
     public Object[] getObjects() {return null;}
}

class AglTestStrategySetGetClipRect implements TestStrategy {
     private Rectangle clipToSet, clipFound;
     private int x, y, w, h;
     public void draw(Graphics2D gSample) {
	x = y = (int)Math.round(Math.random()*100);
	w = h = (int)Math.round(Math.random()*400);
	clipToSet = new Rectangle(x, y, w, h);
        gSample.setClip(x, y, w, h);
	clipFound = gSample.getClipBounds();//Call getClipBounds(Rectangle r) so we don't need to test the latter
	//draw a yellow rect just to see if the clip works
	gSample.setColor(Color.YELLOW);	
	gSample.fillRect(0, 0, 600, 600);
    }
    public Object[] getObjects(){
	Rectangle clip_tmp[]={clipToSet, clipFound};
	return (Object[])clip_tmp;
    }
}

class AglTestStrategySetGetStroke implements TestStrategy {
     private BasicStroke strkToSet, strkFound;
     public void draw(Graphics2D gSample) {
	strkToSet = new BasicStroke((float)Math.round(Math.random()*50.0), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.5f, new float[] {2.0f,2.0f}, 5.0f);
        gSample.setStroke(strkToSet);
	strkFound = (BasicStroke)gSample.getStroke();
    }
    public Object[] getObjects(){
	BasicStroke strk_tmp[]={strkToSet, strkFound};
	return (Object[])strk_tmp;
    }
}

class AglTestStrategySetGetFont implements TestStrategy {
     private Font fontToSet, fontFound;
     public void draw(Graphics2D gSample) {
	fontToSet = new Font("Monospaced", Font.ITALIC, 36);
        gSample.setFont(fontToSet);
	fontFound = gSample.getFont();
	gSample.drawString("SetGetFont test", 50, 50);
    }
    public Object[] getObjects(){
	Font font_tmp[]={fontToSet, fontFound};
	return (Object[])font_tmp;
    }
}

class AglTestStrategyGetFontMetrics implements TestStrategy {
     private FontMetrics fontMFound;
     private Font font_tmp;
     public void draw(Graphics2D gSample) {
	font_tmp = new Font("Monospaced", Font.BOLD, 48);
        gSample.setFont(font_tmp);
	//First, check if both methods (with and without argument) return same objects
	if(gSample.getFontMetrics().equals(gSample.getFontMetrics(font_tmp)))
		fontMFound = gSample.getFontMetrics();
	else
		fontMFound = null;
    }
    public Object[] getObjects(){
	//We will test only if the first objetc != Null
	FontMetrics fontM_tmp[]={fontMFound, null};
	return (Object[])fontM_tmp;
    }
}

class AglTestStrategySetGetTransform implements TestStrategy {
     private AffineTransform transfToSet, transfFound;
     public void draw(Graphics2D gSample) {
	gSample.setColor(Color.YELLOW);	
	gSample.fillRect(50, 50, 200, 200);
	transfToSet = new AffineTransform();
	transfToSet.translate(100,100);
	transfToSet.scale(2,2);
	transfToSet.rotate(1.5);
        gSample.setTransform(transfToSet);
	transfFound = gSample.getTransform();
	gSample.setColor(Color.RED);	
	gSample.fillRect(0, 0, 200, 200);
    }
    public Object[] getObjects(){
	AffineTransform transf_tmp[]={transfToSet, transfFound};
	return (Object[])transf_tmp;
    }
}

class AglTestStrategySetGetColor implements TestStrategy {
     private Color colorToSet, colorFound;
     public void draw(Graphics2D gSample) {
	colorToSet = new Color((int)Math.round(Math.random()*650000));
        gSample.setColor(colorToSet);
	colorFound = gSample.getColor();
	gSample.drawLine(251, 251, 500, 500);
    }
    public Object[] getObjects(){
	Color color_tmp[]={colorToSet, colorFound};
	return (Object[])color_tmp;
    }
}

class AglTestStrategySetGetBackground implements TestStrategy {
     private Color colorToSet, colorFound;
     public void draw(Graphics2D gSample) {
	colorToSet = new Color((int)Math.round(Math.random()*650000));
	gSample.setBackground(colorToSet);
	colorFound = gSample.getBackground();
	gSample.clearRect(200, 200, 200, 200);
     }
    public Object[] getObjects(){
	Color color_tmp[]={colorToSet, colorFound};
	return (Object[])color_tmp;
    }
}
