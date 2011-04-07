/*****************************************************************************
 * Copyright (C) 2011, Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

package agile2d;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.BasicStroke;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * <b>G2DSample</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 */

public class G2DSample extends Canvas {
    AglTestContext context;
    private int width, height;
    BufferedImage buf_img;

    public void paint(Graphics g) {
	width = super.getWidth();
	height = super.getWidth();
	Graphics2D g2d_ = (Graphics2D) g;
	buf_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

	Graphics2D g2d = (Graphics2D)buf_img.createGraphics();
	
//      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

	//Paint sample primitives

	g2d.setBackground(Color.WHITE);
	g2d.clearRect(0, 0, width, height);
	g2d.setStroke(new BasicStroke(3));

	if(context!=null){
             context.drawStrategy(g2d);	
	}
	g2d_.drawImage(buf_img, 0, 0, null);
    }

    public BufferedImage getBufferedImage(){
	return buf_img;	
   } 

    public void setContext(AglTestContext context){
	this.context = context;
    }
}