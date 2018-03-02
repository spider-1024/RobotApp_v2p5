/*
 * This class provides all support for interfacing to the driver station 
 *
 *  This 
 */
package robotapp_v2p5;

import com.RobotMsgv2p0.RobotControlMsg;
import com.RobotMsgv2p0.RobotControlMsgGen;
import com.RobotMsgv2p0.RobotControlMsgXbox;
import com.RobotMsgv2p0.RobotStatusMsg;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.NORM_PRIORITY;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author BSchlad
 */
public class RobotDrvStation implements Runnable {
 
    static boolean objectFlag = false;
    
    long markTimeStamp;
    
    // Network variables
    ServerSocket dSListener;
    Socket dsSocket;
    RobotControlMsgXbox dsRcvMsg;
    RobotStatusMsg robotStatusMsg;
    boolean dsRcvMsgAvail;
    boolean connectionStatus;
    ObjectOutputStream output;
    ObjectInputStream input;
    
    /* 
    Constructor for the RobotDrvStation Class
    */
    public RobotDrvStation() {
        
        // make sure there is only one of these
        if (objectFlag == true)
            return;
        
        // Initialize Driver Station Interface Status
        setDSMsgRcvStatus(false);   
        connectionStatus = false;
        
        // Create an initial message
        dsRcvMsg = new RobotControlMsgXbox();
        
               // start thread
       Thread t = new Thread(this);
       t.setPriority(NORM_PRIORITY - 1);
       t.start();
        
    }
    
    /**
     * This method will setup the connection to the DriverStation
     */
    public void robotDriverStationInit() {
        
        System.out.println("Initializing Network");
        setDriverStationConnectionStatus(false);

        
        try {
            dSListener = new ServerSocket(1034);
            dsSocket = dSListener.accept();
            
            output =  new ObjectOutputStream( dsSocket.getOutputStream());  
            input  =  new ObjectInputStream (dsSocket.getInputStream());
 
        }
        catch (IOException e){
            System.out.println("Error Initializing Socket " + e);
            setDriverStationConnectionStatus(false);
            
            try {
               dSListener.close();
            } catch (IOException ep) {}
               
            return;
        }
        
        System.out.println("Success Initializing Network");
        setDriverStationConnectionStatus(true);
        
    }
    

    /**
     * This method will read and send data from the driver station
     */
    
    private void robotDriverStationReadMsg() {
        
        // Read data from Socket
        try {
            
             dsRcvMsg = new RobotControlMsgXbox();
             dsRcvMsg = (RobotControlMsgXbox) input.readObject();    
             setDSMsgRcvStatus(true);
             

         } catch ( IOException e ) {
            System.out.println( "I/O error " + e ); // I/O error
            setDriverStationConnectionStatus(false);

            try {
                dsSocket.close();
            } catch (IOException ep) {}
        }         
        catch ( ClassNotFoundException e2 ) {
            System.out.println( e2 ); // unknown type of request object
         }
    
    }
    
    /* 
     * returns the current message recieved from the Driver Station
    */
    public RobotControlMsgXbox robotDriverStationGetMsg() {
        return dsRcvMsg;
    }
    
    /*
     * This method will do the Driver Station Housekeeping and then send 
     * the messag to the Driver Station
     */    
    public void robotDriverStationSendMsg(RobotStatusMsg robotStatusMsg) {
        
        
        // Read data from Socket
        try {
                      
           // Setup and send response message
           // only send the response back every 100mSec
           if (getElapsedTimeMsec() < 100)
               return;
            
                              
            if (dsSocket.isConnected()) {
                
               output.writeObject( robotStatusMsg );
               output.reset();
            }
            else
                setDriverStationConnectionStatus(false);
            
            // Dont forget to reset the mark time
            markTime();


         } catch ( IOException e ) {
            System.out.println( "I/O error " + e ); // I/O error
            setDriverStationConnectionStatus(false);
            try {
                dsSocket.close();
            } catch (IOException ep) {}
        }         
    
}
    
    
    public void setDriverStationConnectionStatus(boolean val) {
        connectionStatus = val;
    }  
    
    public boolean getDriverStationConnectionStatus() {
        return connectionStatus;
    }
    
    public boolean getDSMsgRcvStatus() {
        return dsRcvMsgAvail;
    }
    
    public void setDSMsgRcvStatus(boolean val) {
        dsRcvMsgAvail = val;
    }
    
    
    // getElapsedTime and markTime returns the time between 
    // when the time was marked.  Its used for checking relative 
    // time differences
    public long getElapsedTimeMsec() {
        long p = System.currentTimeMillis() - markTimeStamp;
        return (p);      
    }
    
    public void markTime(){
        markTimeStamp = System.currentTimeMillis();
    }
    
        /** 
     * Method: processDriverStationMessage
     * 
     * This method is responsible for parsing the message from the driver 
     * station and converting it to a generic robot control object.
     * 
     * msg.ZRot - Right Trigger.  values = 0 - 100  Forward Speed
     * msg.Zaxis - Left Trigger.  values = 0 - 100  Reverse Speed
     * msg.button5 - Right button. 0 o 1  Reverse Selection
     * msg.XRot - Right stick 1 - 100 (51 represents center position)
     * 
     * This method is called every time a new message is received from the
     * driver station.
     * 
     * @param : msg - control information from DriverStation 
     * 
     */
    
     public RobotControlMsgGen processDriverStationMessage(RobotControlMsgXbox msg) {
        
        // convert from xbox controller message to generic control message
        RobotControlMsgGen cntrlMsg = new RobotControlMsgGen();
        
        if (msg.button5 == 1) {
            cntrlMsg.driveSpeed = (int) ((msg.Zaxis * 254)/100);
            cntrlMsg.driveDirection = 'R';
        }
        else {
            cntrlMsg.driveSpeed = msg.ZRot;
            cntrlMsg.driveDirection = 'F';
        }
        
        cntrlMsg.steeringValue = msg.XRot;
 
        return cntrlMsg;
     }  // end processDriverStationMessage
   
    // run loop for all activities
    public void run() {
        
        
        // Main processing loop for all Driver Station Activities
        try {
            while (true) {
               
                if (getDriverStationConnectionStatus()) {
                    
                    // this is a blocking call to wait for message from driver station
                    robotDriverStationReadMsg();
                }
                else
                    robotDriverStationInit();
      

                Thread.sleep(50);
            } 
        } catch (InterruptedException ie){}
    }  
}
