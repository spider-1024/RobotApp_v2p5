/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp_v2p5;

/**
 *
 * @author owner
 */
public class RobotTimer {
     
    private long markTimeStamp;
    private long timePeriod;

    RobotTimer(int i) {
        timePeriod = i;
        markTime(0);
    }
    
    // getElapsedTime and markTime returns the time between 
    // when the time was marked.  Its used for checking relative 
    // time differences
    public long getElapsedTimeMsec() {
        long p = System.currentTimeMillis() - markTimeStamp;
        return (p);      
    }
    
    public boolean timerExpired() {
        if (getElapsedTimeMsec() > timePeriod)
            return true;
        else 
            return false;
    }
    
    public void markTime(int i){
        timePeriod = i;
        markTimeStamp = System.currentTimeMillis();
    }
    
}
