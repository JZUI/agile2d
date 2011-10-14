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
}