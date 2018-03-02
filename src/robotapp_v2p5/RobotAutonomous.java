/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp_v2p5;

import com.RobotMsgv2p0.RobotControlMsgGen;
import com.RobotMsgv2p0.RobotStatusMsg;

/**
 *
 * @author owner
 */
public class RobotAutonomous {
    
    enum StateType {stateInit, stateWait, stateDrive1, stateTurn1, stateDrive2, stateTurn2,
                    stateDrive3, stateFinal}
//    boolean stateEnterFlag = true;
    
    // Misc
    RobotTimer robotTimer;
    int loopCnt = 0;
    int startingDistance, totalDistance;
    
    StateType stateType;
    
    /**
     * Constructor for class
     */
    RobotAutonomous() {
      stateType = StateType.stateInit;
      robotTimer = new RobotTimer(0);  
      startingDistance = 0;
    }
    
    /**
     * method : robotAutomomousRun
     * 
     * This method is called every 50mSec 
     * @param sensorInfo
     * @return 
     */
    public RobotControlMsgGen robotAutonomousRun(RobotStatusMsg sensorInfo) {
        
        RobotControlMsgGen robotCntrlMsg = new RobotControlMsgGen();
        
        if ((loopCnt % 20) == 0) 
          System.out.println("T " + robotTimer.getElapsedTimeMsec() + " S " + stateType + 
                   " D " + sensorInfo.distanceTravelled);
              
        switch (stateType) {
            case stateInit : // Initial State
                               
                
                robotCntrlMsg.steeringValue = 50; // no turning
                robotCntrlMsg.driveSpeed = 0; // no forward motion
                robotTimer.markTime(2000);
                stateType = StateType.stateWait;
                startingDistance = sensorInfo.distanceTravelled;  // set starting value
             
                break;
            case stateWait :
                
                if (robotTimer.timerExpired() == false)
                    stateType = StateType.stateWait;
                else
                    stateType = StateType.stateDrive1;
                
                robotCntrlMsg.steeringValue = 50; // no turning
                robotCntrlMsg.driveSpeed = 0; // start forward motion
                break;
            case stateDrive1 : 
                //  Drive forward 1.5 meters
                if (sensorInfo.distanceTravelled - startingDistance < 150) {
                    robotCntrlMsg.steeringValue = 50;
                    robotCntrlMsg.driveSpeed = 200;
                    robotCntrlMsg.driveDirection = 'F';
                }
                else {
                    robotCntrlMsg.steeringValue = 50;
                    robotCntrlMsg.driveSpeed = 0;
                    robotTimer.markTime(3000);
                    stateType = StateType.stateTurn1;
                }
                    
                break;
            case stateTurn1 :
                // In this state we will turn the wheels
                
                if (robotTimer.timerExpired() == true) {
                    stateType = StateType.stateDrive2;
                    startingDistance = sensorInfo.distanceTravelled;  // make relative
                }
                
                robotCntrlMsg.steeringValue = 25;
                robotCntrlMsg.driveSpeed = 0;  

                break;
            case stateDrive2 :
                //  Drive forward to complete the turn
                if (sensorInfo.distanceTravelled - startingDistance >= 30) {
                    robotTimer.markTime(3000);
                    stateType = StateType.stateTurn2;
                }

                robotCntrlMsg.steeringValue = 25;
                robotCntrlMsg.driveSpeed = 200;                
                
                break;
            case stateTurn2 :
                
                // In this state we will turn the wheels to straight                
                if (robotTimer.timerExpired() == true) {
                   stateType = StateType.stateDrive3;
                   startingDistance = sensorInfo.distanceTravelled;
                }

                robotCntrlMsg.steeringValue = 50;
                robotCntrlMsg.driveSpeed = 0;                
                
                break;
            case stateDrive3 :
                //  Drive forward 1.5 meters
                if (sensorInfo.distanceTravelled - startingDistance >= 150) {
                    stateType = StateType.stateInit;
                }

                robotCntrlMsg.steeringValue = 50;
                robotCntrlMsg.driveSpeed = 200;
                break;
            case stateFinal :
                robotCntrlMsg.steeringValue = 50;
                robotCntrlMsg.driveSpeed = 0;
                break;

            
        } // end switch
        
        loopCnt++;
        return robotCntrlMsg;
    }  // end robotAutomomousRun
    
    // This method will reset the state machine to be called externally
    public void robotAutonomousResetState() {
        stateType = StateType.stateInit;
    }
}
