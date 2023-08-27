/*
 * Copyright (C) 2011 Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

/**
 * <b>TestStrategy Interface</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 * Based on the STRATEGY pattern
 */

package agile2d.test;

import java.awt.Graphics2D;


interface TestStrategy {
    void draw(Graphics2D gSample); 
    Object[] getObjects();
}
