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

class CharKey implements Comparable<CharKey>  
{  
	private String _key;
	private int _size;

	CharKey(char c_, Font f_)  
	{  
		_size = f_.getSize();
		_key = Character.toString(c_)+"_"+f_.getFontName()+"_"+f_.getStyle()+"_"+_size;

	}  

	@Override  
	public int compareTo(CharKey other)  
	{  
		return _key.compareTo(other._key);  
	}  

	@Override  
	public boolean equals(Object other)  
	{  
		return (other != null) && (getClass() == other.getClass()) &&   
				_key.equals(((CharKey)other)._key);  
	}  

	@Override  
	public int hashCode()  
	{  
		return _key.hashCode();  
	}  

	@Override  
	public String toString()  
	{  
		return _key;  
	}  

	public int size()  
	{  
		return _size;  
	}

}  


class OutlineRoughFontRenderer extends BasicFontRenderer {
	GlyphMetrics metrics[];
	VertexArrayList vertices[];
	VertexArrayList presentVAL;
	//	VertexArrayList verticesGlyphs[];
	Tesselator tesselator;
	int listBase;    
	//	int listBaseGlyphs;

	Font listFont[] = new Font[256]; // character font currently in display list
	private static final int MIN_PRE_RENDER_FONT_SIZE = 24;
	private static final int MAX_PRE_RENDER_FONT_SIZE = 1024;
	private static final int INIT_FONT_SIZE_LENGTH = 64;

	//This is the "by default" value of this parameter
	//the user may though control it through a render quality hint
	//That's the role of method FontManager.setRoughOutlineQuality(hint_)
	private static final float FONT_SIZE_INTERVAL_FACTOR = 1.08f;

	private static int font_size_length;
	private static int listFontSizes[];

	private SoftHashMap charSoftHashMap;

	/** 
	 * Tesselates a shape and stores the result in a VertexArrayList
	 */
	static class VATesselatorVisitor implements TesselatorVisitor {	
		VertexArrayList list;
		VertexArray last;

		VATesselatorVisitor(VertexArrayList list) {
			this.list = list;
		}

		/**
		 * @see agile2d.TesselatorVisitor#begin(int)
		 */
		public void begin(int mode) {
			VertexArray v = new VertexArray();
			v.setMode(mode);
			list.add(v);
			last = v;
		}

		/**
		 * @see agile2d.TesselatorVisitor#addVertex(double[])
		 */
		public void addVertex(double[] coords) {
			last.addVertex(coords);
		}

		/**
		 * @see agile2d.TesselatorVisitor#addVertex(double, double)
		 */
		public void addVertex(double x, double y) {
			last.addVertex(x, y);
		}

		/**
		 * @see agile2d.TesselatorVisitor#end()
		 */
		public void end() {
			last = null;
		}

		// TesselatorVisitor
		public void combine(double coords[/*3*/], Object data[/*4xn*/],  float weight[/*4*/], Object[/*3*/] dataOut) {
			Tesselator.defaultCombine(coords, data, weight, dataOut);
		}

		public void error(int errorCode) {
			Tesselator.defaultError(errorCode);
		}   
	}

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

	private void generateSizesList(int start_size_, int max_size_){
		
		listFontSizes = new int[INIT_FONT_SIZE_LENGTH];
		float size_=(float)start_size_;
		//The firt accepted size is the "starting size"
		listFontSizes[0] = (int)start_size_;
		int i=1;
		do{
			size_ *= FONT_SIZE_INTERVAL_FACTOR;
			listFontSizes[i]=(int)Math.round(size_);
			i++;
		}while( (i<INIT_FONT_SIZE_LENGTH) && (size_ < max_size_) );
		font_size_length = i;
		System.out.print("Sizes: ");
		int j;
		for(j=0; j<font_size_length; j++)
			System.out.print(listFontSizes[j]+", ");
		System.out.println("End");
	}    

	public int getNextUpperSize(int reqSize_){
		int length_ = listFontSizes.length;
		for(int i=0; i<length_; i++){
			if( listFontSizes[i] >= reqSize_ )
				return listFontSizes[i];
		}
		System.err.println("Warning: Can't find any size larger than the required size (which is "+reqSize_+").\n Using Max size, which is "+listFontSizes[length_-1]);
		return listFontSizes[length_-1];
	}


	public OutlineRoughFontRenderer(Tesselator tesselator) {
		this.tesselator = tesselator;
		generateSizesList(MIN_PRE_RENDER_FONT_SIZE, MAX_PRE_RENDER_FONT_SIZE);
		charSoftHashMap = new SoftHashMap(256);
	}

	public boolean installFont(GLAutoDrawable drawable, Font font_, double scale, boolean aa, boolean ufm) {
		//Check if the requested font has already been installed
		System.out.println("Scale :"+scale);
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

		//if the vertices array associated to this font is null...
		if (this.vertices == null) {
			//Create a glyphVector from all characters of the present font
			setup();

			if(listBase == 0) {
				GL2 gl = drawable.getGL().getGL2();
				listBase = gl.glGenLists(256);
			}

			this.vertices = new VertexArrayList[256];
			info.vertices = this.vertices;
			for(int i=0; i<256; i++)
				info.vertices[i]=null;

		}
		//Check wether it is the same font that was used to to create the present glyphvector
		//If it's not, create a new glyphvector from the present font
		else if(glyphs.getFont() != font_){
			setup();
		}
		installed = true;
		return true;
	}


	public void render(GLAutoDrawable drawable, String string, double scale, Font font_) {
		if (!installed)
			return;
		GL2 gl = drawable.getGL().getGL2();

		int i;
		for (i = 0; i < string.length(); i++) {
			int c = string.charAt(i);
			if (c > metrics.length)
				continue;
			presentVAL = null;
			if (installChar(drawable, c)) {
				//Get the metrics for each character
				GlyphMetrics m = metrics[c];				
				//DRAW A CHARACTER
				//i.e.: each VertexArrayList (many VertexArrays) corresponds to
				//a character (many polygons) and each vertexArray corresponds to
				//a convex polygon composing the character
				gl.glPushMatrix();
				gl.glScaled(scale, scale, 1.0);
				for (int j = 0; j < presentVAL.size(); j++)
					ShapeManager.render(gl, presentVAL.getVertexArrayAt(j), null);
				gl.glPopMatrix();
				gl.glTranslated((m.getAdvanceX())*scale, (m.getAdvanceY())*scale, 0.0d);
			}
		}
		installed = false;
	}


	protected boolean installChar(GLAutoDrawable drawable, int c) {

		GL2 gl = drawable.getGL().getGL2();

		//int roughSize_ = this.getNextUpperSize(font.getSize());
		
		
		//get VertexArray list of the character c
		CharKey tempKey_ = new CharKey( (char) c, this.font);
		presentVAL = (VertexArrayList)charSoftHashMap.get(tempKey_);

		//If data doesn't exist (still / anymore), make it;
		if (presentVAL == null){
			
			//addTesselation(drawable, latin1Chars[c], c);
			addTesselation(drawable, c);
			System.out.println("No vertexArray for character: "+tempKey_.toString());
		}
		else
			System.out.println("Found VertexArray for character: "+tempKey_.toString());

		//System.out.println("Number of vertexArrays: "+vAL_.size());
		return true;
	}

	public boolean addTesselation(GLAutoDrawable drawable, int c) {
		int charIndex = latin1Chars[c];
		Shape s = this.glyphs.getGlyphOutline(charIndex);
		if (s == null){
			System.out.println("Warning: There are no glyphs for this font");
			return false;
		}	
		metrics[charIndex] = glyphs.getGlyphMetrics(charIndex);
		
		presentVAL = new VertexArrayList();
		VATesselatorVisitor visitor = new VATesselatorVisitor(presentVAL);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		
		CharKey tempCharKey_ = new CharKey((char)charIndex, this.font);
		charSoftHashMap.put((Object)tempCharKey_, (Object)presentVAL);		
		
		return true;
	}

	public void release(GLAutoDrawable drawable) {
		installed = false;
	}

	/*
	protected VertexArrayList getCharVertices(GLAutoDrawable drawable, int c) {
		CharKey tempKey_ = new CharKey( (char) c, this.font);
		VertexArrayList vAL_ = (VertexArrayList)charSoftHashMap.get(tempKey_);
		return vAL_;
	}
	*/
} 

/*	 
	 protected VertexArrayList getVertices(GLAutoDrawable drawable, int c, VertexArrayList vertices_[]) {
		 if (vertices_[c] == null) {
			 addTesselation(drawable, latin1Chars[c], c);
		 }
		 return vertices_[c];
	 }


protected boolean installChar(GLAutoDrawable drawable, int c, int listBase_, VertexArrayList vList_[]) {

	 //get vertex array list of the character c

	 VertexArrayList v = getVertices(drawable, c, vList_);

	 if (v == null)
		 return false;

	CharKey tempKey_ = new CharKey( (char) c, this.font);
	VertexArrayList v_ = (VertexArrayList)charSoftHashMap.get(tempKey_);


	 GL2 gl = drawable.getGL().getGL2();

	if (v_ == null)
		addTesselation(drawable, latin1Chars[c], c);


//	 gl.glNewList(listBase_ + c, GL2.GL_COMPILE);

	 //DRAW A CHARACTER
	 //i.e.: each VertexArrayList (many VertexArrays) corresponds to
	 //a character and each vertexArray corresponds to
	 //a convex polygon composing the character

//	 for (int i = 0; i < v.size(); i++)
//		 ShapeManager.render(gl, v.getVertexArrayAt(i), null);

//	 gl.glEndList();

	 return true;
}
 */

/*
	 public boolean prepareGlyphVertices(GLAutoDrawable drawable) {
		 verticesGlyphs = new VertexArrayList[256];  
		 GL2 gl = drawable.getGL().getGL2();
		 listBaseGlyphs = gl.glGenLists(256);
		 return true;
	 }

	 protected VertexArrayList getGlyphVertices(GLAutoDrawable drawable, int c, VertexArrayList vertices_[], GlyphVector g) {
		 if (vertices_[c] == null) {
			 addTesselation(drawable, c, g);
		 }
		 return vertices_[c];
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

	 public boolean addTesselation(GLAutoDrawable drawable, int glyphIndex, GlyphVector g) {
		 Shape s = g.getGlyphOutline(glyphIndex);
		 VertexArrayList v = new VertexArrayList();
		 VATesselatorVisitor visitor = new VATesselatorVisitor(v);
		 tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		 verticesGlyphs[glyphIndex] = v;
		 return true;
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
 */

