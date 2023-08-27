/*
 * Copyright (C) 2011 Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d.test;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * <b>G2DSample</b>
 * * @author Rodrigo de Almeida
 ** @version $Revision$
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
	//Paint sample primitives

	g2d.setBackground(Color.WHITE);
	g2d.clearRect(0, 0, width, height);


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
