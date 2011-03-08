/*****************************************************************************
 * Copyright (C) 2006 Jean-Daniel Fekete and INRIA, France                  *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the X11 Software License    *
 * a copy of which has been included with this distribution in the           *
 * license.txt file.                                                         *
 *****************************************************************************/
package agile2d;

/**
 * Default implementation of a TesselatorVisitor.
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision: 1.1 $
 */
public class TesselatorAdapter implements TesselatorVisitor {
    /**
     * {@inheritDoc}
     */
    public void begin(int mode) {
    }

    /**
     * {@inheritDoc}
     */
    public void addVertex(double[] coords) {
    }

public void addVertex(double x, double y){
    }

    /**
     * {@inheritDoc}
     */
    public void end() {
    }

    /**
     * {@inheritDoc}
     */
    public void error(int errorCode) {
        Tesselator.defaultError(errorCode);
    }

    /**
     * {@inheritDoc}
     */

    public void combine(
            double[] coords,
            //Object[] vertex_data,
	    double[] vertex_data,
            float[] weight,
            double[] dataOut) {
        Tesselator.defaultCombine(coords, vertex_data, weight, dataOut);
    }

}
