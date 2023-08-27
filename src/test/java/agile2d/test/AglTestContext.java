/*
 * Copyright (C) 2011 Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

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
