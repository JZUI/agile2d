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
class OutlineFontRenderer extends BasicFontRenderer implements BasicOutlineFontRenderer {
    GlyphMetrics metrics[];
    VertexArrayList vertices[];
    VertexArrayList verticesGlyphs[];
    Tesselator tesselator;
    int listBase;    
    int listBaseGlyphs;
    Font listFont[] = new Font[256]; // character font currently in display list
    
	
	static class CacheInfo {
        Font font;
        GlyphMetrics metrics[];
        VertexArrayList vertices[];

        CacheInfo(Font font_) {
            this.font = font_;
            metrics = new GlyphMetrics[256];
        }
    }

    LinkedList cache = new LinkedList();
    int maxCacheLength = 20;

    public CacheInfo findCached(Font font_) {
        CacheInfo info = null;
        boolean first = true;
        for (Iterator it = cache.iterator(); it.hasNext();) {
            info = (CacheInfo) it.next();
            if (info.font.equals(font_)) {
                if (!first) {
                    it.remove();
                    cache.addFirst(info);
                }
                return info;
            }
            first = false;
        }
        info = new CacheInfo(font_);
        cache.addFirst(info);
        setMaxCacheLength(maxCacheLength);

        return info;
    }

    /**
     * Returns the maxCacheLength.
     * @return int
     */
    public int getMaxCacheLength() {
        return maxCacheLength;
    }

    /**
     * Sets the maxCacheLength.
     * @param maxCacheLength The maxCacheLength to set
     */
    public void setMaxCacheLength(int maxCacheLength) {
        if (maxCacheLength < 0)
            maxCacheLength = 0;
        this.maxCacheLength = maxCacheLength;
        while (cache.size() > maxCacheLength)
            cache.removeLast();
    }

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

			if(listBase == 0) {
	                GL2 gl = drawable.getGL().getGL2();
        	        listBase = gl.glGenLists(256);
		    }

	      this.vertices = new VertexArrayList[256];
          info.vertices = this.vertices;
	      for(int i=0; i<256; i++)
	      	info.vertices[i]=null;

	      //tesselate all chars for the requested Font
	      //for (int i = 0; i < latin1Chars.length; i++) {
	      //	      addTesselation(drawable, latin1Chars[i]);
	      //}

        }
        //Check wether it is the same font of that used to create the present glyphvector
        //and if not, create a new glyphvector with the present font
        else if(glyphs.getFont() != font_){
			setup();
		}
        installed = true;
        return true;
    }


    public boolean prepareGlyphVertices(GLAutoDrawable drawable) {
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


    protected VertexArrayList getGlyphVertices(GLAutoDrawable drawable, int c, VertexArrayList vertices_[], GlyphVector g) {
        if (vertices_[c] == null) {
            addTesselation(drawable, c, g);
        }
        return vertices_[c];
    }


    
    protected boolean installChar(GLAutoDrawable drawable, int c, int listBase_, VertexArrayList vList_[]) {

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


    protected boolean installGlyph(GLAutoDrawable drawable, int c, int listBase_, VertexArrayList vList_[], GlyphVector g) {
	VertexArrayList v = getGlyphVertices(drawable, c, vList_, g);
        if (v == null)
	 	return false;
        GL2 gl = drawable.getGL().getGL2();
        gl.glNewList(listBase_ + c, GL2.GL_COMPILE);
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
            if (installChar(drawable, c, listBase, vertices)) {
                GlyphMetrics m = metrics[c];
                gl.glCallList(listBase + c);
                gl.glTranslated(m.getAdvanceX(), m.getAdvanceY(), 0.0d);
            }
        }
        installed = false;
    }


    public void render(GLAutoDrawable drawable, GlyphVector g) {
        int i;
        GL2 gl = drawable.getGL().getGL2();
        for (i = 0; i < g.getNumGlyphs(); i++) {
           if (installGlyph(drawable, i, listBaseGlyphs, verticesGlyphs, g) ) {
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


    public boolean addTesselation(GLAutoDrawable drawable, int c) {
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

    public boolean addTesselation(GLAutoDrawable drawable, int glyphIndex, GlyphVector g) {
        Shape s = g.getGlyphOutline(glyphIndex);
        VertexArrayList v = new VertexArrayList();
	VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(v);
        tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
        verticesGlyphs[glyphIndex] = v;
        return true;
    }
} 

