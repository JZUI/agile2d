/*****************************************************************************
 * Copyright (C) 2011, Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

/**
 * <b>AglTestContext</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 */

package agile2d.test;

import java.awt.Graphics2D;

class AglTestContext {
 
    private TestStrategy strategy;
 
    // Constructor
    public AglTestContext(TestStrategy strategy) {
        this.strategy = strategy;
    }
 
    public void drawStrategy(Graphics2D gSample) {
        strategy.draw(gSample);
    }

    public Object[] getObjectsStrategy() {
        return strategy.getObjects();
    }
}
