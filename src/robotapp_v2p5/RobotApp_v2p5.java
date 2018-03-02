/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp_v2p5;

import java.io.IOException;
import com.RobotMsgv2p0.*;
import static java.lang.Thread.NORM_PRIORITY;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author BSchlad
 */
public class RobotApp_v2p5 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
     
    // We will implement a BlockingQueue to utilize an independent timer
    // to provide a constant 50mSec trigger for the main processing loop.
    // This should provide a 50mSec timebase independent of the processing 
    // time of the loop. This also de-couples us from using the 
    // messages from the Driver Station to control timing
    BlockingQueue<Integer> rQueue = new ArrayBlockingQueue<>(20);
    Thread RobotBaseTimer = new Thread(new RobotBaseTimer(rQueue));
    RobotBaseTimer.start();
     
    // Start the main robot task
    RobotRunBase dsb = new RobotRunBase(rQueue);

    }
    
}

class RobotRunBase implements Runnable {

    
    // Misc
    int loopCnt;
    private BlockingQueue<Integer> rQueue;

    // define objects
    RobotControlMsgXbox drvStRcvMsg;
    RobotControl robotController;
    RobotSupport robotSupport;
    RobotTestMode robotTestMode;
    RobotAutonomous robotAutonomousMode;
    RobotStatusMsg robotStatus;
    RobotDrvStation robotDrvStation;
    RobotSensor robotSensorInfo;
    
    
    // Constructor
    public RobotRunBase(BlockingQueue<Integer> rQueue) {
        
        // Initialize the robot
        System.out.println("Initializing Robot");
        
        // Create necessary classes
        robotController = new RobotControl();
        robotSupport = new RobotSupport();
        robotTestMode = new RobotTestMode();
        robotAutonomousMode = new RobotAutonomous();
        robotStatus = new RobotStatusMsg();
        robotDrvStation = new RobotDrvStation();
        robotSensorInfo = new RobotSensor();
        
        
        robotController.setDriverStationLED(false);
        
         // robotState : This reflects the state of the robot. 
        //  DISABLE: It has been disabled by the driver station
        //  ENABLE: It has been enabled by the driver station
        //  READY: It has been disabled by the driver station and the Arduino is active
        //  ACTIVE: It has been enabled by the driver station and the Arduino is active
        robotStatus.robotState = RobotControlMsg.RobotState.DISABLE;
        
        // setup queue
        this.rQueue = rQueue;
       
       // start thread
       Thread t = new Thread(this);
       t.setPriority(NORM_PRIORITY - 1);
       t.start();
        
    }
    
    /*
     * This method processes the information from the Driver Station and information
     * on the arduino status and determines the operational state of the robot
     *
     * The method also generates the response message to the driver Station.
     * It creates a message based on operational status information, any
     * information from sensors, information from automous mode that we wish
     * to send back to the Driver Station.
    */
 
    private void processRobotStatus(RobotControlMsgXbox robotRcvMsg) {
        
         
        // Send response back to Driver Station
        if (robotController.getRobotControlArduinoStatus())
            robotStatus.robotState = RobotControlMsg.RobotState.READY;
        else
            robotStatus.robotState = RobotControlMsg.RobotState.DISABLE;
            
        if (robotRcvMsg.operationalState == RobotControlMsg.OperationalState.ENABLE)
        {
            if (robotController.getRobotControlArduinoStatus())
            {
              robotStatus.robotState = RobotControlMsg.RobotState.ACTVE;
              robotController.setOperationalStatusLED(true);
              RobotSupport.enableFiles();
            }
        }
        else if (robotRcvMsg.operationalState == RobotControlMsg.OperationalState.DISABLE) {
            robotController.setOperationalStatusLED(false);
            RobotSupport.disableFiles();
            robotAutonomousMode.robotAutonomousResetState(); 
                
            if (robotController.getRobotControlArduinoStatus())
                robotStatus.robotState = RobotControlMsg.RobotState.READY;
            else
                robotStatus.robotState = RobotControlMsg.RobotState.DISABLE;
                
        }
        
        // Update the response message with Sensor information
        robotSensorInfo.processAnalogInputs(robotController);
        robotStatus.distanceLevel = robotSensorInfo.getDistanceSensor();
        robotStatus.batteryLevel = robotSensorInfo.getBatteryLevel();
        robotStatus.steeringAngle = robotSensorInfo.getSteeringAngle();
        robotStatus.distRotAngle = robotSensorInfo.getDistanceRotationAngle();
        robotStatus.distanceTravelled = robotSensorInfo.getDistanceTraveled();
            
    }
    
    /*
     * Simple utility method to return a boolean if the robot is in the
     * active state.
    */
    private boolean getRobotActiveStatus() {
        
        return robotStatus.robotState == RobotControlMsg.RobotState.ACTVE;
    }
    
    
    // run loop for all activities
    @Override
    public void run() {
        
        
        RobotControlMsgGen controlMsg = new RobotControlMsgGen();
             
        System.out.println("Initializing Control");
        
        try {
            robotController.robotControlInitialize();
        } catch (IOException ie){}
        
        System.out.println("System Loop Starting");

        
        // Main processing loop for all Driver Station Activities
        try {
            while (true) {
                                             
                // Get new message from Driver Station
                drvStRcvMsg = robotDrvStation.robotDriverStationGetMsg();
                
                //  Check for any control status updates from driver station
                //  and update sensor info
                processRobotStatus(drvStRcvMsg);
                
                // Process message from Driver Station
                if (getRobotActiveStatus()) {
                    
                   // check operation mode
                   if (drvStRcvMsg.operationalMode == RobotControlMsg.OperationalMode.TELEOP) {
                       controlMsg = robotDrvStation.processDriverStationMessage(drvStRcvMsg);
                   }
                   
                   if (drvStRcvMsg.operationalMode == RobotControlMsg.OperationalMode.TESTMODE) {
                       controlMsg = robotTestMode.runTest(drvStRcvMsg.testNumber);                            
                   } // end test mode operation
                   
                   if (drvStRcvMsg.operationalMode == RobotControlMsg.OperationalMode.AUTONOMOUS) {
                       controlMsg = robotAutonomousMode.robotAutonomousRun(robotStatus);   
                   } // end autonomous mode
                   
                }
                       
                // Update motor controllers
                if (getRobotActiveStatus()) {
                    robotController.processMotorControl(controlMsg);
                }
                
                // Send Response update to driver station
 //               robotDrvStation.robotDriverStationSendMsg(robotStatus);
 
                // Take Care of loop control
                // This call will block waiting for the Queue Timer to send a
                // message to re-start the loop
                loopCnt = rQueue.take();   
                
                System.out.println("lc= " + loopCnt);
                
                Thread.sleep(10);
            } 
        } catch (InterruptedException ie){}
    }  
    
} // RobotRunBase