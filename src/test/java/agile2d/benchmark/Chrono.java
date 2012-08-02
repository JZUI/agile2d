/************************************************************************************
 * Copyright (C) 2012, Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *        
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * ---------------------------------------------------------------------------------*
 * This software is published under the terms of the BSD Software License    	    *
 ************************************************************************************/

package agile2d.benchmark;

public final class Chrono{
    private long begin, end;	    
 
    public Chrono(){
    	begin = end = 0;
    }
    
    public void start(){
        begin = System.currentTimeMillis();
    }
 
    public void stop(){	    	
        end = System.currentTimeMillis();
    }
    public long getDuration() {
        return end-begin;
    }
    
    public long getTempDuration() {
        return System.currentTimeMillis()-begin;
    }
}