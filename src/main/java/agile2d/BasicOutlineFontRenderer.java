/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;

import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

/**
 * Interface to render Fonts from their outlines
 * @author
 * @version $Revision: 1.4 $
 */

public interface BasicOutlineFontRenderer {
	public boolean installFont(GLAutoDrawable drawable, Font font_, double scale, boolean aa, boolean ufm);
	public boolean addTesselation(GLAutoDrawable drawable, int c);
}
