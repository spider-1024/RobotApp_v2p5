/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp_v2p5;

/**
 *
 * @author BSchlad
 * 
 * The purpose of this class is to support all of the Sensor processing 
 * from the controller
 */
public class RobotSensor {
    
    //    static final double CM_TO_TICK_CONV = 1.5278;  // number of CM per tick (outdoor wheels)
    static final double CM_TO_TICK_CONV = 1.1775;  // number of CM per tick  (indoor wheels)

 
    // Misc
    private int batteryAvg, batterySampleCnt;
    private int distanceAvg, distanceSampleCnt;
    private int steeringAvg, steeringSampleCnt;
    private int distRotAvg, distRotSampleCnt;
    private int speedCntAvg, speedCntSampleCnt;
    private int ticksCurrentTotal, ticksUpdateTotal, ticksIntervalValue;
    
    public RobotSensor() {
                // Analog Input data
        batteryAvg = 0;
        batterySampleCnt =0;
        distanceAvg = 0;
        distanceSampleCnt = 0;
        steeringAvg = 0;
        steeringSampleCnt = 0;
        distRotAvg = 0;
        distRotSampleCnt = 0;
        speedCntAvg = 0;
        speedCntSampleCnt = 0;
        ticksCurrentTotal = 0;
        ticksUpdateTotal = 0;
        ticksIntervalValue = 0;
    }
    

    
    /**
     * Method : processAnalogInputs
     * 
     * The Arduino will send analog updates to the robot every 100mSec.  The robot will keep 
     * a running average of these values so that if and when a value is sent to the Driver
     * Station it represents the average of what has already been received.  This eliminates
     * any need to synchronize the results with the Driver Station.
    */
 
    public void processAnalogInputs(RobotControl robotController) {
         
        // Get Battery value : Index = 0
        int val = robotController.getAnalogValue(0);
        if (batterySampleCnt == 0) 
            batteryAvg = val;
        else
            batteryAvg = batteryAvg + (val - batteryAvg)/batterySampleCnt;
        batterySampleCnt++;
        
        // Get Distance Sensor value : Index = 1
        val = robotController.getAnalogValue(1);
        if (distanceSampleCnt == 0)
            distanceAvg = val;
        else
            distanceAvg = distanceAvg + (val - distanceAvg)/distanceSampleCnt;
        distanceSampleCnt++;       
        
        // Get Distance rotation value : Index = 2
        val = robotController.getAnalogValue(2);
        if (distRotSampleCnt == 0)
            distRotAvg = val;
        else distRotAvg = distRotAvg + (val - distRotAvg)/distRotSampleCnt;
        
        // Get Steering rotation value : Index = 3
        val = robotController.getAnalogValue(3);
        if (steeringSampleCnt == 0)
            steeringAvg = val;
        else
            steeringAvg = steeringAvg + (val - steeringAvg)/steeringSampleCnt;
        steeringSampleCnt++;
        
        // Get speedCnt Value : Index = 4
        // here we need to keep track of total distance travelled.  The counter from
        // the arduino can only hold up to 999 ticks.  So need to track rollover.
        int tmp = robotController.getAnalogValue(4);
        
        // check for rollover
        if (tmp < ticksIntervalValue)
            ticksUpdateTotal = ticksUpdateTotal + 1000;
        ticksIntervalValue = tmp;   // set this to check against the next pass
                   
        ticksCurrentTotal = ticksUpdateTotal + ticksIntervalValue;
    }
    
    // method : getBatteryLevel
    // This method returns the current battery level and resets the average
    public int getBatteryLevel() {
        int tmp = batteryAvg;
        batteryAvg = 0;
        batterySampleCnt = 0;
        
        // Actual battery level is a scaled version. We will scale to the correct value
        // and then multiply by 100 to pass as an integer to the Driver Station.
        // 999 from Arduino = 5v
        // 5/(0.364) = 13.7v  (0.364 is the voltage divider)
        // To convert to true voltage. Arduino value * 13.7/999;
        // Multiply by 10 so that 
        
        tmp = (int)(tmp * (13.2/999) * 10);
        
        return tmp;      
    }
    
    // method : getDistanceLevel
    // This method returns the current distance meter and resets the average
    // Will need to calibrate to actual distance in future.
    public int getDistanceSensor() {
        int tmp = distanceAvg;
        distanceAvg = 0;
        distanceSampleCnt = 0;
        return tmp;
    }
    
    /**
     * method : getDistanceTraveled
     * This method returns the distance traveled by the robot in cm.
     * This method simply converts the number of ticks from the optical sensor
     * into units of centimeters.  This is cumulative distance.
     * 
     * 
    */
    public int getDistanceTraveled() {
        return (int) (ticksCurrentTotal * CM_TO_TICK_CONV);
    }
    /**
     * Method: getSteeringAngle
     * 
     * This method returns the current angle the steering sensor is reporting.
     * It is scaled 
     * 
     */
    public int getSteeringAngle() {
                
        int angle = steeringAvg;
        
        // reset the average
        steeringAvg = 0;
        steeringSampleCnt = 0;
        
        return angle;
    } 
    
    /**
     * Method getDistanceRotationAngle
     * 
     * This method returns the angle that the distance sensor is pointed at
     * @return 
     */
    
    public int getDistanceRotationAngle() {
        int angle;
        
        angle = distRotAvg * 180/1024;
        
        return angle;
    }
}
