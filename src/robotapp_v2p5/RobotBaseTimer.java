/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp_v2p5;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author BSchlad
 */
public class RobotBaseTimer implements Runnable {
    
    private BlockingQueue<Integer> queue;
    private long currentTimeStamp;
    private long initialTimeStamp;
    
 
    static int objectCnt = 0;
    
    
    public RobotBaseTimer(BlockingQueue<Integer> queue) {
        this.queue = queue;
        initialTimeStamp = System.currentTimeMillis();
    }
    
    
    public void run () {
        try {
            
            while (true) {
                
               currentTimeStamp = System.currentTimeMillis();
               int elapsedTime = (int) (currentTimeStamp - initialTimeStamp);
               
               
               queue.put(elapsedTime);           
               Thread.sleep(1000);
            }
            
        } catch (InterruptedException ie) {
        }
    }
    
}
