/*****************************************************************************
 * Copyright (C) 2011, Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

/**
 * <b>TestStrategy Interface</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 * Based on the STRATEGY pattern
 */

package agile2d;

import java.awt.Graphics2D;


interface TestStrategy {
    void draw(Graphics2D gSample); 
    Object[] getObjects();
}
