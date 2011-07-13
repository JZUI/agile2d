/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;


import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import agile2d.geom.VertexArray;
import agile2d.geom.VertexArrayList;


/**
 * Render Fonts from their outlines
 *
 * @author Jean-Daniel Fekete
 * @version $Revision: 1.3 $
 */
class OutlineFontRenderer extends BasicOutlineFontRenderer {
	private VertexArrayList vertices[];
	private VertexArrayList verticesGlyphs[];
	private Font listFont[] = new Font[256]; // character font currently in display list
	private int listBaseFont;  
	private int listBaseGlyphs;
	private static int GLYPHVECTOR_MAX_LENGTH = 256; 

	public OutlineFontRenderer(Tesselator tesselator) {
		this.tesselator = tesselator;
	}

	public boolean installFont(GLAutoDrawable drawable, Font font_, double scale, boolean aa, boolean ufm) {
		//Check if the requested font has already been installed
		if (this.font != null && this.font.equals(font_)) {
			installed = true;
			return true;
		}
		CacheInfo info = findCached(font_);
		if (info == null) {
			installed = false;
			return false;
		}
		this.font = info.font;
		this.frc = new FontRenderContext(null, aa, ufm);
		this.metrics = info.metrics;
		this.vertices = info.vertices;

		if (this.vertices == null) {
			setup();

			if(listBaseFont == 0) {
				GL2 gl = drawable.getGL().getGL2();
				listBaseFont = gl.glGenLists(256);
			}

			this.vertices = new VertexArrayList[256];
			info.vertices = this.vertices;
		}
		//Check wether it is the same font of that used to create the present glyphvector
		//and if not, create a new glyphvector with the present font
		else if(glyphs.getFont() != font_){
			setup();
		}
		installed = true;
		return true;
	}

    /*
     * Prepare Vertex Array Lists for a GlyphVector 
     * 
     */
	public boolean prepareGlyphVertices(GLAutoDrawable drawable) {
		//Each vertexArray stores vertex coordinates for a polygon,
		//each vertexArrayList stores the vertexArrays for a glyph,
		//and an ARRAY of vertexArrayList stores a GlyphVector
		verticesGlyphs = new VertexArrayList[256];
		GL2 gl = drawable.getGL().getGL2();
		listBaseGlyphs = gl.glGenLists(256);
		return true;
	}


	protected VertexArrayList getVertices(GLAutoDrawable drawable, int c, VertexArrayList vertices_[]) {
		if (vertices_[c] == null) {
			addTesselation(drawable, c);
		}
		return vertices_[c];
	}


	protected VertexArrayList getGlyphVertices(GLAutoDrawable drawable, int g, VertexArrayList vertices_[], GlyphVector gv) {
		if (vertices_[g] == null) {
			addTesselation(drawable, g, gv);
		}
		return vertices_[g];
	}

	protected boolean installChar(GLAutoDrawable drawable, int c, int listBase_, VertexArrayList vList_[]) {

		//WHAT listFont[] array SERVES TO ?!		
		/*
        if (listFont[c] == this.font){
        	return true;
		}*/

		VertexArrayList v = getVertices(drawable, c, vList_);

		//vertices for this character havent't been generated
		if (v == null)
			return false;
		GL2 gl = drawable.getGL().getGL2();
		gl.glNewList(listBase_ + c, GL2.GL_COMPILE);
		for (int i = 0; i < v.size(); i++)
			ShapeManager.render(gl, v.getVertexArrayAt(i), null);
		gl.glEndList();
		//listFont[c] = this.font;
		return true;
	}


	protected boolean installGlyph(GLAutoDrawable drawable, int g, int listBase_, VertexArrayList vList_[], GlyphVector gv) {
		VertexArrayList v = getGlyphVertices(drawable, g, vList_, gv);
		if (v == null)
			return false;
		GL2 gl = drawable.getGL().getGL2();
		gl.glNewList(listBase_ + g, GL2.GL_COMPILE);
		for (int i = 0; i < v.size(); i++)
			ShapeManager.render(gl, v.getVertexArrayAt(i), null);
		gl.glEndList();
		return true;
	}


	public void render(GLAutoDrawable drawable, String string, double scale, Font font_) {
		if (!installed)
			return;
		int i;

		GL2 gl = drawable.getGL().getGL2();

		for (i = 0; i < string.length(); i++) {
			int c = string.charAt(i);
			if (c > metrics.length)
				continue;
			if (installChar(drawable, c, listBaseFont, vertices)) {
				GlyphMetrics m = metrics[c];
				gl.glCallList(listBaseFont + c);
				gl.glTranslated(m.getAdvanceX(), m.getAdvanceY(), 0.0d);
			}
		}
		installed = false;
	}


	public void render(GLAutoDrawable drawable, GlyphVector gv) {
		int i;
		GL2 gl = drawable.getGL().getGL2();
		for (i = 0; i < gv.getNumGlyphs(); i++) {
			if (installGlyph(drawable, i, listBaseGlyphs, verticesGlyphs, gv) ) {
				gl.glCallList(listBaseGlyphs + i);
			}
		}
	}

	/*
    public void render(GLAutoDrawable drawable, GlyphVector g, double scale) {
        if (!installed)
            return;
        int i;

        GL2 gl = drawable.getGL().getGL2();

        for (i = 0; i < g.getNumGlyphs(); i++) {
            int c = g.getGlyphCode(i);

	    System.out.println("Code in renderGlyph: "+c);
            if (c > metrics.length)
                continue;
            if (installChar(drawable, c, listBaseGlyphs, verticesGlyphs)) {
   	        System.out.println("Dans installChar of renderGlyph");
                GlyphMetrics m = metrics[c];
                gl.glCallList(listBaseGlyphs + c);
//              gl.glCallList(listBase+c);
                gl.glTranslated(m.getAdvanceX(), m.getAdvanceY(), 0);
            }
        }
        installed = false;
    }
	 */


	protected boolean addTesselation(GLAutoDrawable drawable, int c) {
		int charIndex = latin1Chars[c];
		Shape s = glyphs.getGlyphOutline(charIndex);
		if (s == null)
			return false;
		metrics[charIndex] = glyphs.getGlyphMetrics(charIndex);
		VertexArrayList v = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(v);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		vertices[charIndex] = v;
		return true;
	}

	public boolean addTesselation(GLAutoDrawable drawable, int glyphIndex, GlyphVector gv) {
		Shape s = gv.getGlyphOutline(glyphIndex);
		VertexArrayList v = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(v);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		verticesGlyphs[glyphIndex] = v;
		return true;
	}
} 

