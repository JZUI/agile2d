/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;


import java.awt.Font;
import java.awt.font.GlyphMetrics;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GLAutoDrawable;

import agile2d.geom.VertexArrayList;


/**
 * Render Fonts from their outlines
 *
 * @author 
 * @version $Revision: 1.4 $
 */

public abstract class BasicOutlineFontRenderer extends BasicFontRenderer {

	protected GlyphMetrics metrics[];
	protected Tesselator tesselator;

	static class CacheInfo {
		Font font;
		GlyphMetrics metrics[];
		VertexArrayList vertices[];
	
		CacheInfo(Font font_) {
			this.font = font_;
			metrics = new GlyphMetrics[256];
		}
	}

	private LinkedList cache = new LinkedList();
	private int maxCacheLength = 20;

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

	public abstract boolean installFont(GLAutoDrawable drawable, Font font_, double scale, boolean aa, boolean ufm);
	protected abstract boolean addTesselation(GLAutoDrawable drawable, int c);
} 
