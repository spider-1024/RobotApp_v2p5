/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp_v2p5;

import com.RobotMsgv2p0.RobotControlMsgGen;

/**
 *
 * @author owner
 */
public class RobotTestMode {
    
    int loopCnt = 0;
    
    // constructor for test mode
    public RobotTestMode () {
        
    }
    
    public RobotControlMsgGen runTest(int testNumber) {
        
        
        RobotControlMsgGen robotCntrlMsg = new RobotControlMsgGen();
        
       
        if ((loopCnt % 10) == 0)
           System.out.println("Starting Test Mode " + testNumber);
                       
        if (testNumber == 1) {
            robotCntrlMsg.driveSpeed = 254;  // move forward full speed
            robotCntrlMsg.driveDirection = 'F';
        }
        else if (testNumber == 2) {
            robotCntrlMsg.steeringValue = 25;
            robotCntrlMsg.driveDirection = 'F';
        }
        else if (testNumber == 3) {
            robotCntrlMsg.steeringValue = 75;
        }
        else {
            robotCntrlMsg.driveSpeed = 0;  // stop
            robotCntrlMsg.steeringValue = 50;
        }
        
        return robotCntrlMsg;
    }
} // end RobotTestMode
